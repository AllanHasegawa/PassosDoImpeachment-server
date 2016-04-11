/*******************************************************************************
 * Copyright 2016 Allan Yoshio Hasegawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hasegawa.di.server

import com.fasterxml.jackson.annotation.JsonProperty
import com.hasegawa.di.server.utils.toUnixTimestamp
import io.dropwizard.lifecycle.Managed
import org.glassfish.jersey.client.ClientProperties
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import rx.Observer
import rx.Subscription
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class GCMControl(val gcmKey: String, val client: Client) : Managed {

    private var gcmSendToClientsSubscription: Subscription? = null
    private val gcmToClientsSubject: PublishSubject<Any> = PublishSubject.create()

    // key = ip as String
    // value = List of unix time stamps
    private val invalidTokensIps = HashMap<String, ArrayList<Long>>()
    private val invalidTokensIpsLock = Any()

    override fun start() {
        gcmSendToClientsSubscription = gcmToClientsSubject
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map {
                    val response = client
                            .property(ClientProperties.CONNECT_TIMEOUT, GCM_TIMEOUT)
                            .property(ClientProperties.READ_TIMEOUT, GCM_TIMEOUT)
                            .target(GCM_TARGET)
                            .path(GCM_PATH)
                            .request(MediaType.APPLICATION_JSON)
                            .header("Authorization", "key=$gcmKey")
                            .header("Content-Type", MediaType.APPLICATION_JSON)
                            .post(Entity.entity(it, MediaType.APPLICATION_JSON))
                    response
                }
                .subscribe(object : Observer<Response> {
                    override fun onError(e: Throwable?) {
                        log.info("Error sending GCM message.", e)
                    }

                    override fun onNext(t: Response) {
                        log.info("GCM message sent successfully.")
                        log.info("${t.toString()}")
                    }

                    override fun onCompleted() {
                    }
                })
    }

    override fun stop() {
        gcmToClientsSubject.onCompleted()
        if (gcmSendToClientsSubscription != null && !gcmSendToClientsSubscription!!.isUnsubscribed) {
            gcmSendToClientsSubscription?.unsubscribe()
        }
    }

    private data class SyncData(@JsonProperty val sync: Boolean)
    private data class SyncRequest(@JsonProperty val to: String,
                                   @JsonProperty val data: SyncData)

    fun sendSyncRequest() {
        val body = SyncRequest(TO_SYNC, SyncData(true))
        gcmToClientsSubject.onNext(body)
    }


    private data class NewsData(@JsonProperty val notificationMessage: String)

    private data class NewsNotification(@JsonProperty val to: String,
                                        @JsonProperty val data: NewsData)

    fun sendNewsNotification(message: String) {
        val body = NewsNotification(TO_NEWS, NewsData(message))
        gcmToClientsSubject.onNext(body)
    }


    data class GCMDryRunResponseResult(
            @JsonProperty val error: String? = null,
            @JsonProperty val message_id: String? = null)

    data class GCMDryRunResponse(
            @JsonProperty val multicast_id: String? = null,
            @JsonProperty val success: Int? = null,
            @JsonProperty val failure: Int? = null,
            @JsonProperty val canonical_ids: Int? = null,
            @JsonProperty val results: List<GCMDryRunResponseResult>? = null)

    private data class SendToken(
            @JsonProperty val to: String,
            @JsonProperty val dry_run: Boolean
    )

    fun blockingValidateToken(token: String, request: HttpServletRequest): Boolean {
        val response = client
                .property(ClientProperties.CONNECT_TIMEOUT, GCM_TIMEOUT)
                .property(ClientProperties.READ_TIMEOUT, GCM_TIMEOUT)
                .target(GCM_TARGET)
                .path(GCM_PATH)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "key=$gcmKey")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(Entity.entity(SendToken(token, true), MediaType.APPLICATION_JSON))
        if (response != null && response.status == Response.Status.OK.statusCode) {
            val dryRunResponse = response.readEntity(GCMDryRunResponse::class.java)
            val validToken = dryRunResponse.success == 1
            if (!validToken) {
                log.error("GCM Invalid Token Response: " + dryRunResponse.toString())
                synchronized(invalidTokensIpsLock) {
                    val numberOfInvalidations = ipToNumberOfInvalidations(request)
                    if (numberOfInvalidations >= MAX_NUMBER_OF_INVALID_TOKENS) {
                        tempBanIpWithCsf(request)
                    }
                }
                return false
            }
            return true
        }
        return false
    }

    private fun ipToNumberOfInvalidations(request: HttpServletRequest): Int {
        val ip = request.remoteAddr
        val filteredList = ArrayList<Long>()
        val now = DateTime.now().toUnixTimestamp()
        invalidTokensIps[ip]?.filterTo(filteredList) {
            it > (now - TIMESPAN_FOR_INVALID_TOKENS)
        }
        filteredList.add(now)
        invalidTokensIps.put(ip, filteredList)
        return filteredList.size
    }

    private fun tempBanIpWithCsf(request: HttpServletRequest) {
        log.info("Temporary banning: ${request.remoteHost} (${request.remoteAddr}:${request.remotePort})")
        val ip = request.remoteAddr
        try {
            val process = Runtime.getRuntime().exec("csf -td $ip $IP_TEMP_BAN_TIME")
            val input = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = null
            while (true) {
                line = input.readLine()
                if (line == null) {
                    break
                }
                log.info(line)
            }
            process.waitFor(10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            log.error("Error trying to run CSF to ban ip.", e)
        }
    }

    companion object {
        private var log = LoggerFactory.getLogger(GCMControl::class.java)
        private const val TO_SYNC = "/topics/sync"
        private const val TO_NEWS = "/topics/news"
        private const val GCM_TARGET = "https://gcm-http.googleapis.com"
        private const val GCM_PATH = "gcm/send"
        private const val GCM_TIMEOUT = 33 * 1000

        private const val MAX_NUMBER_OF_INVALID_TOKENS = 10
        private const val TIMESPAN_FOR_INVALID_TOKENS = 24 * 60 * 60 * 1000
        private const val IP_TEMP_BAN_TIME = "24h"
    }
}
