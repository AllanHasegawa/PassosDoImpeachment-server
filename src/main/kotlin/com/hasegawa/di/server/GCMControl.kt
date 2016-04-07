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
import io.dropwizard.lifecycle.Managed
import org.glassfish.jersey.client.ClientProperties
import org.slf4j.LoggerFactory
import rx.Observer
import rx.Subscription
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class GCMControl(val gcmKey: String, val client: Client) : Managed {


    private var gcmSubscription: Subscription? = null
    private val gcmMessageSubject: PublishSubject<Any> = PublishSubject.create()
    override fun start() {
        gcmSubscription = gcmMessageSubject
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
        gcmMessageSubject.onCompleted()
        if (gcmSubscription != null && !gcmSubscription!!.isUnsubscribed) {
            gcmSubscription?.unsubscribe()
        }
    }

    private data class SyncData(@JsonProperty val sync: Boolean)
    private data class SyncRequest(@JsonProperty val to: String,
                                   @JsonProperty val data: SyncData)

    fun sendSyncRequest() {
        val body = SyncRequest(TO_SYNC, SyncData(true))
        gcmMessageSubject.onNext(body)
    }


    private data class NewsData(@JsonProperty val notificationMessage: String)

    private data class NewsNotification(@JsonProperty val to: String,
                                        @JsonProperty val data: NewsData)

    fun sendNewsNotification(message: String) {
        val body = NewsNotification(TO_NEWS, NewsData(message))
        gcmMessageSubject.onNext(body)
    }

    companion object {
        private var log = LoggerFactory.getLogger(GCMControl::class.java)
        private const val TO_SYNC = "/topics/sync"
        private const val TO_NEWS = "/topics/news"
        private const val GCM_TARGET = "https://gcm-http.googleapis.com"
        private const val GCM_PATH = "gcm/send"
        private const val GCM_TIMEOUT = 33 * 1000
    }
}
