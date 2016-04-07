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

import com.hasegawa.di.server.daos.UsersDao
import io.dropwizard.auth.Authenticator
import io.dropwizard.auth.basic.BasicCredentials
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class DiAuthenticator(val usersDao: UsersDao) : Authenticator<BasicCredentials, User> {
    override fun authenticate(credentials: BasicCredentials?): Optional<User>? {
        val user = usersDao.findByUsername(credentials!!.username)
        if (user != null && BCrypt.checkpw(credentials.password, user.passwordHash)) {
            return Optional.of(user)
        }
        return Optional.empty()
    }
}
