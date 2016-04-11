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
package com.hasegawa.di.passgen

import org.mindrot.jbcrypt.BCrypt
import java.util.*

/**
 * Created by hasegawa on 3/26/2016.
 */
class MainApp {
    companion object {
        @JvmStatic fun main(args: Array<String>) {

            if (args.size != 3) {
                println("With gradle, use: -PappArgs=\"['<username>', '<password>', 'role']\" to pass arguments.")
                error("Pass the password you want to hash/salt as argument to this program.")
            }

            val id = UUID.randomUUID().toString()
            val username = args[0]
            val hash = BCrypt.hashpw(args[1], BCrypt.gensalt())
            val role_level = when (args[2].toUpperCase()) {
                "USER" -> 0
                "EDITOR" -> 0x42
                "ADMIN" -> 0x9000
                else -> throw RuntimeException("Role unknown.")
            }

            println("insert into users (id, username, password_hash, role_level, public_name) " +
                    "values ('$id', '$username', '$hash', $role_level, '$username')"
            )
        }
    }
}