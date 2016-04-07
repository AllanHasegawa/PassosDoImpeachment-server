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
package com.hasegawa.di.server.auths

import io.dropwizard.auth.UnauthorizedHandler
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class DiUnauthorizedHandler : UnauthorizedHandler {
    override fun buildResponse(prefix: String?, realm: String?): Response? {
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity("Credentials are required to access this resource.")
                .build();
    }
}
