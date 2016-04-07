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

import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import org.skife.jdbi.v2.sqlobject.Transaction
import java.util.*

abstract class GCMDao {

    @SqlQuery("select count(*) from $TABLE_NAME where token=:$COL_TOKEN")
    abstract fun findByToken(@Bind(COL_TOKEN) token: String): Long

    @SqlUpdate("insert into $TABLE_NAME (" +
            "$COL_ID, $COL_TOKEN, $COL_TIME_CREATED)" +
            "values (" +
            ":$COL_ID, :$COL_TOKEN, :$COL_TIME_CREATED)")
    abstract fun justInsertToken(@Bind(COL_ID) id: UUID,
                                 @Bind(COL_TOKEN) token: String,
                                 @Bind(COL_TIME_CREATED) timeCreated: Long)

    @Transaction
    fun insertToken(@Bind(COL_ID) id: UUID,
                    @Bind(COL_TOKEN) token: String,
                    @Bind(COL_TIME_CREATED) timeCreated: Long) {
        val numTokens = findByToken(token)
        if (numTokens == 0L) {
            justInsertToken(id, token, timeCreated)
        }
    }


    companion object {
        const val TABLE_NAME = "gcm_tokens"

        const val COL_ID = "id"
        const val COL_TOKEN = "token"
        const val COL_TIME_CREATED = "time_created"
    }
}
