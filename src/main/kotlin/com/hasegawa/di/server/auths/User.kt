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

import java.security.Principal
import java.util.*

class User(
        val id: UUID,
        val username: String,
        val passwordHash: String,
        val role: Role,
        val publicName: String) : Principal {
    override fun getName(): String = username

    enum class Role(val level: Int) {
        User(0x0),
        Editor(0x42),
        Admin(0x9000),
        God(0x33333),
        Judge(0xb00b5);

        override fun toString(): String = super.toString().toUpperCase()

        companion object {
            fun fromString(str: String): Role {
                return when (str.toUpperCase()) {
                    USER -> User
                    EDITOR -> Editor
                    ADMIN -> Admin
                    GOD -> God
                    JUDGE -> Judge
                    else -> throw RuntimeException("Role unknown: $str")
                }
            }

            fun fromInt(i: Int): Role {
                return when (i) {
                    0x0 -> User
                    0x42 -> Editor
                    0x9000 -> Admin
                    0x33333 -> God
                    0xb00b5 -> Judge
                    else -> throw RuntimeException("Role level unknown: $i")
                }
            }

            const val USER = "USER"
            const val EDITOR = "EDITOR"
            const val ADMIN = "ADMIN"
            const val GOD = "GOD"
            const val JUDGE = "JUDGE"
        }
    }
}
