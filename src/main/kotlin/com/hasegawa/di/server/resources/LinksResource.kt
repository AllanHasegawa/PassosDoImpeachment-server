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
import com.hasegawa.di.server.auths.User
import com.hasegawa.di.server.daos.LinksDao
import com.hasegawa.di.server.pokos.StepLink
import com.hasegawa.di.server.utils.toUnixTimestamp
import io.dropwizard.auth.Auth
import org.joda.time.DateTime
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
class LinksResource(val stepId: UUID, val linksDao: LinksDao, val gcmControl: GCMControl) {
    @OPTIONS
    fun corsPreflight() = Response.ok().build()

    //    @GET
    //    @Timed
    //    fun links(): List<StepLink> {
    //        val list = linksDao.findAllByStepId(stepId)
    //        return list
    //    }

    data class LinkPutBody(
            @JsonProperty val id: String? = null,
            @JsonProperty val title: String? = null,
            @JsonProperty val url: String? = null,
            @JsonProperty val published: Boolean? = null)

    @PUT
    @Path("/{id}")
    @Timed
    @RolesAllowed(User.Role.EDITOR)
    fun linksPut(@PathParam("id") id: String, @Valid body: LinkPutBody): StepLink {
        val uuid = UUID.fromString(id)
        linksDao.update(uuid, stepId, body.title!!, body.url!!, body.published!!)
        StepsResource.stepsCacheDirty.set(true)
        gcmControl.sendSyncRequest()
        return linksDao.findById(uuid)
    }

    data class LinkPostBody(@JsonProperty val title: String? = null,
                            @JsonProperty val url: String? = null)

    @POST
    @Timed
    @RolesAllowed(User.Role.EDITOR)
    fun linkPost(@Auth user: User, @Valid body: LinkPostBody): StepLink {
        val id = UUID.randomUUID()
        linksDao.insert(
                id,
                user.id,
                stepId,
                body.title!!,
                body.url!!,
                DateTime.now().toUnixTimestamp()
        )
        StepsResource.stepsCacheDirty.set(true)
        gcmControl.sendSyncRequest()
        return linksDao.findById(id)
    }

    //    @DELETE
    //    @Path("/{id}")
    //    @Timed
    //    @RolesAllowed(User.Role.EDITOR)
    //    fun linkDelete(
    //            @PathParam("id") id: String
    //    ): Response {
    //        val uuid = UUID.fromString(id)
    //        linksDao.delete(uuid)
    //        StepsResource.stepsCacheDirty.set(true)
    //        return Response.ok().build()
    //    }
}
