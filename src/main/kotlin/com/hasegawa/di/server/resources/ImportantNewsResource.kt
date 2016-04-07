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
package com.hasegawa.di.server.resources

import com.codahale.metrics.annotation.Timed
import com.fasterxml.jackson.annotation.JsonProperty
import com.hasegawa.di.server.GCMControl
import com.hasegawa.di.server.MainApplication
import com.hasegawa.di.server.auths.User
import com.hasegawa.di.server.daos.ImportantNewsDao
import com.hasegawa.di.server.pokos.ImportantNews
import com.hasegawa.di.server.utils.toUnixTimestamp
import io.dropwizard.auth.Auth
import org.hibernate.validator.constraints.NotEmpty
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.security.RolesAllowed
import javax.validation.Valid
import javax.ws.rs.GET
import javax.ws.rs.OPTIONS
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/importantNews")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
class ImportantNewsResource(val newsDao: ImportantNewsDao, val gcmControl: GCMControl) {

    @OPTIONS
    fun corsPreflight() = Response.ok().build()

    fun checkNewsCache() {
        if (newsDirty.get()) {
            synchronized(newsLock) {
                if (newsDirty.get()) {
                    val newsFiltered = newsDao.findAll().filter { it.published!! }
                    newsResponseCache = MainApplication.jacksonMapper.writeValueAsString(newsFiltered)

                    val news = newsDao.findAll()
                    newsEditorResponseCache = MainApplication.jacksonMapper.writeValueAsString(news)
                    newsDirty.set(false)
                    log.info("ImportantNews cached.")
                }
            }
        }
    }

    @GET
    @Timed
    fun news(): String {
        checkNewsCache()
        return newsResponseCache
    }

    @GET
    @Path("/editor")
    @Timed
    @RolesAllowed(User.Role.EDITOR)
    fun newsEditor(): String {
        checkNewsCache()
        return newsEditorResponseCache
    }

    data class NewsPost(
            @NotEmpty val title: String? = null,
            @NotEmpty val url: String? = null,
            @NotEmpty val published: Boolean? = null,
            @NotEmpty val date: Long? = null,
            @JsonProperty val tldr: String? = null,
            @JsonProperty val appMessage: String? = null,
            @NotEmpty val sendAppNotification: Boolean? = null
    )

    @POST
    @Timed
    @RolesAllowed(User.Role.EDITOR)
    fun newsPost(@Auth user: User, @Valid body: NewsPost): ImportantNews {
        val id = UUID.randomUUID()
        newsDao.insert(
                id,
                user.id,
                body.title!!,
                body.url!!,
                body.published!!,
                body.date!!,
                body.tldr,
                body.appMessage,
                body.sendAppNotification!!,
                DateTime.now().toUnixTimestamp()
        )
        newsDirty.set(true)
        if (body.sendAppNotification == true) {
            gcmControl.sendNewsNotification(body.appMessage!!)
        } else {
            gcmControl.sendSyncRequest()
        }
        return newsDao.findById(id)
    }

    data class NewsPut(
            @JsonProperty val id: String? = null,
            @NotEmpty val title: String? = null,
            @NotEmpty val url: String? = null,
            @NotEmpty val published: Boolean? = null,
            @NotEmpty val date: Long? = null,
            @JsonProperty val tldr: String? = null
    )

    @PUT
    @Path("/{id}")
    @Timed
    @RolesAllowed(User.Role.EDITOR)
    fun newsPut(@Auth user: User, @PathParam("id") id: String, @Valid body: NewsPut): ImportantNews {
        val uuid = UUID.fromString(id)
        newsDao.update(
                uuid,
                user.id,
                body.title!!,
                body.url!!,
                body.published!!,
                body.date!!,
                body.tldr!!
        )
        newsDirty.set(true)
        gcmControl.sendSyncRequest()
        return newsDao.findById(uuid)
    }

    companion object {
        private lateinit var newsResponseCache: String
        private lateinit var newsEditorResponseCache: String
        private var newsDirty = AtomicBoolean(true)
        private var newsLock = Any()


        private var log = LoggerFactory.getLogger(ImportantNewsResource::class.java)
    }
}
