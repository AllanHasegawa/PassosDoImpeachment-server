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

import com.fasterxml.jackson.databind.ObjectMapper
import com.hasegawa.di.server.GCMControl.GCMControlConfig
import com.hasegawa.di.server.auths.DiAuthenticator
import com.hasegawa.di.server.auths.DiAuthorizer
import com.hasegawa.di.server.auths.DiUnauthorizedHandler
import com.hasegawa.di.server.auths.User
import com.hasegawa.di.server.daos.GCMDao
import com.hasegawa.di.server.daos.ImportantNewsDao
import com.hasegawa.di.server.daos.LinksDao
import com.hasegawa.di.server.daos.StepsDao
import com.hasegawa.di.server.daos.StepsHistoryDao
import com.hasegawa.di.server.daos.UsersDao
import com.hasegawa.di.server.resources.CommandsResource
import com.hasegawa.di.server.resources.GCMResource
import com.hasegawa.di.server.resources.ImportantNewsResource
import com.hasegawa.di.server.resources.StepsResource
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle
import io.dropwizard.Application
import io.dropwizard.assets.AssetsBundle
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.basic.BasicCredentialAuthFilter
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.db.DataSourceFactory
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.migrations.MigrationsBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.eclipse.jetty.servlets.CrossOriginFilter
import org.glassfish.jersey.filter.LoggingFilter
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature
import java.util.EnumSet
import java.util.logging.Logger
import javax.servlet.DispatcherType
import javax.servlet.FilterRegistration

class MainApplication : Application<ServerConfiguration>() {

    val migrationBundle = object : MigrationsBundle<ServerConfiguration>() {
        override fun getDataSourceFactory(configuration: ServerConfiguration): DataSourceFactory {
            return configuration.dataSourceFactory;
        }
    }

    override fun getName(): String {
        return "di-server"
    }

    override fun initialize(bootstrap: Bootstrap<ServerConfiguration>?) {
        bootstrap!!.addBundle(TemplateConfigBundle())
        bootstrap.addBundle(migrationBundle)
        bootstrap.addBundle(AssetsBundle(WEB_APP_ROOT_PATH, "/", "index.html", "web app root"))
        jacksonMapper = bootstrap.objectMapper
    }

    override fun run(configuration: ServerConfiguration?, environment: Environment?) {
        // Enable CORS headers
        val cors: FilterRegistration.Dynamic =
                environment!!.servlets().addFilter("CORS", CrossOriginFilter::class.java)

        // Configure CORS parameters
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization");
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "false");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*");

        val client = JerseyClientBuilder(environment)
                .using(configuration!!.getJerseyClientConfiguration())
                .build(name)

        val factory = DBIFactory()
        val jdbi = factory.build(environment, configuration.dataSourceFactory, "postgresql")

        val stepsDao = jdbi.onDemand(StepsDao::class.java)
        val linksDao = jdbi.onDemand(LinksDao::class.java)
        val usersDao = jdbi.onDemand(UsersDao::class.java)
        val stepsHistoryDao = jdbi.onDemand(StepsHistoryDao::class.java)
        val importantNewsDao = jdbi.onDemand(ImportantNewsDao::class.java)
        val gcmDao = jdbi.onDemand(GCMDao::class.java)

        val gcmControlConfig = GCMControlConfig(
                configuration.gcmKey!!,
                configuration.gcmInvalidTokensBeforeBan!!,
                configuration.gcmTimeSpanToStoreInvalidTokens!!,
                configuration.gcmTempBanCsfTimeInSeconds!!)

        val gcmControl = GCMControl(gcmControlConfig, client)
        environment.lifecycle().manage(gcmControl)

        environment.jersey().register(AuthDynamicFeature(
                BasicCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(DiAuthenticator(usersDao))
                        .setAuthorizer(DiAuthorizer())
                        .setRealm("AUTH REALM")
                        .setUnauthorizedHandler(DiUnauthorizedHandler())
                        .buildAuthFilter()
        ))
        environment.jersey().register(RolesAllowedDynamicFeature::class.java)
        environment.jersey().register(AuthValueFactoryProvider.Binder(User::class.java))

        environment.jersey().register(StepsResource(stepsDao, linksDao, gcmControl))
        environment.jersey().register(CommandsResource())
        environment.jersey().register(ImportantNewsResource(importantNewsDao, gcmControl))
        environment.jersey().register(GCMResource(gcmDao, gcmControl))

        environment.jersey().register(LoggingFilter(Logger.getLogger("InboundRequestResponse"), true));

    }

    companion object {
        const val WEB_APP_ROOT_PATH = "/webapp/"

        lateinit var jacksonMapper: ObjectMapper

        @Throws(Exception::class)
        @JvmStatic fun main(args: Array<String>) {
            MainApplication().run(*args)
        }
    }
}