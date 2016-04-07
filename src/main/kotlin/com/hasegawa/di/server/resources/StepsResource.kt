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
import com.hasegawa.di.server.daos.LinksDao
import com.hasegawa.di.server.daos.StepsDao
import com.hasegawa.di.server.pokos.Step
import io.dropwizard.auth.Auth
import org.glassfish.jersey.client.ClientProperties
import org.hibernate.validator.constraints.NotEmpty
import org.slf4j.LoggerFactory
import rx.Observable
import rx.Observer
import rx.schedulers.Schedulers
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.security.RolesAllowed
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.OPTIONS
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/steps")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON)
class StepsResource(val stepsDao: StepsDao,
                    val linksDao: LinksDao,
                    val gcmControl: GCMControl) {

    @OPTIONS
    fun corsPreFlight() = Response.ok().build()

    fun recacheSteps() {
        val stepsNotFiltered =
                stepsDao.findAll().map { it.links = linksDao.findAllByStepId(it.id!!); it }
        stepsResponseCache = MainApplication.jacksonMapper.writeValueAsString(stepsNotFiltered)

        val stepsFiltered =
                stepsDao.findAll().filter { it.published!! }
                        .map {
                            it.links =
                                    linksDao.findAllByStepId(it.id!!)
                                            .filter { it.published!! }
                            it
                        }
                        .map {
                            FilteredStepResponse(
                                    it.title!!,
                                    it.description!!,
                                    it.possibleDate!!,
                                    it.position!!,
                                    it.completed!!,
                                    it.links!!.map { FilteredLinkResponse(it.title!!, it.url!!) }
                            )
                        }
        filteredStepsResponseCache = MainApplication.jacksonMapper.writeValueAsString(stepsFiltered)
        log.info("Steps cached.")
    }

    fun checkRecacheSteps() {
        if (stepsCacheDirty.get()) {
            synchronized(stepsCacheLock) {
                if (stepsCacheDirty.get()) {
                    recacheSteps()
                    stepsCacheDirty.set(false)
                }
            }
        }
    }

    data class FilteredLinkResponse(
            val title: String,
            val url: String
    )

    data class FilteredStepResponse(
            val title: String,
            val description: String,
            val possibleDate: String,
            val position: Int,
            val completed: Boolean,
            val links: List<FilteredLinkResponse>
    )

    @GET
    fun steps(): String {
        checkRecacheSteps()
        return filteredStepsResponseCache
    }

    @GET
    @Path("/editor")
    @RolesAllowed(User.Role.EDITOR)
    fun stepsEditor(): String {
        checkRecacheSteps()
        return stepsResponseCache
    }

    data class StepPutBody(
            @JsonProperty val id: String? = null,
            @NotEmpty val position: Int? = null,
            @NotEmpty val title: String? = null,
            @NotEmpty val description: String? = null,
            @NotEmpty val published: Boolean? = null,
            @NotEmpty val completed: Boolean? = null,
            @NotEmpty val possibleDate: String? = null
    )

    @PUT
    @Path("/{id}")
    @Timed
    @RolesAllowed(User.Role.EDITOR)
    fun stepPut(@PathParam("id") id: String,
                @Valid body: StepPutBody): Any {
        val uuid = UUID.fromString(id)
        stepsDao.update(uuid,
                body.position!!,
                body.title!!,
                body.description!!,
                body.published!!,
                body.completed!!,
                body.possibleDate!!)
        stepsCacheDirty.set(true)
        gcmControl.sendSyncRequest()
        return stepsDao.findById(uuid)
    }

    data class StepPostBody(
            @JsonProperty val position: Int? = null,
            @JsonProperty val title: String? = null,
            @JsonProperty val description: String? = null,
            @JsonProperty val possibleDate: String? = null
    )

    @POST
    @Timed
    @RolesAllowed(User.Role.EDITOR)
    fun stepPost(
            @Auth user: User,
            @Valid body: StepPostBody): Step {
        val uuid = UUID.randomUUID()
        stepsDao.insert(
                uuid,
                user.id,
                body.position!!,
                body.title!!,
                body.description!!,
                body.possibleDate!!)
        stepsCacheDirty.set(true)
        gcmControl.sendSyncRequest()
        return stepsDao.findById(uuid)
    }

    @GET
    @Path("/{id}")
    @Timed
    fun step(@PathParam("id") id: String): Any {
        return stepsDao.findById(UUID.fromString(id))
    }

    @Path("/{step_id}/links")
    fun links(@PathParam("step_id") stepId: String): LinksResource {
        return LinksResource(UUID.fromString(stepId), linksDao, gcmControl)
    }

    companion object {
        private val stepsCacheLock = Any()
        var stepsCacheDirty = AtomicBoolean(true)

        lateinit var filteredStepsResponseCache: String
        lateinit var stepsResponseCache: String

        private var log = LoggerFactory.getLogger(StepsResource::class.java)
    }
}
