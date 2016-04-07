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
package com.hasegawa.di.server.daos

import com.hasegawa.di.server.auths.User
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.ResultSet
import java.util.*

@RegisterMapper(UserMapper::class)
interface UsersDao {
    @SqlQuery("select * from $TABLE_NAME " +
            "where $COL_USERNAME=:$COL_USERNAME")
    fun findByUsername(@Bind(COL_USERNAME) username: String): User?

    companion object {
        const val TABLE_NAME = "users"

        const val COL_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD_HASH = "password_hash"
        const val COL_ROLE_LEVEL = "role_level"
        const val COL_PUBLIC_NAME = "public_name"
    }
}

class UserMapper : ResultSetMapper<User> {
    override fun map(index: Int, r: ResultSet?, ctx: StatementContext?): User? {
        return User(
                r!!.getObject(UsersDao.COL_ID) as UUID,
                r.getString(UsersDao.COL_USERNAME),
                r.getString(UsersDao.COL_PASSWORD_HASH),
                User.Role.fromInt(r.getInt(UsersDao.COL_ROLE_LEVEL)),
                r.getString(UsersDao.COL_PUBLIC_NAME)
        )
    }
}
