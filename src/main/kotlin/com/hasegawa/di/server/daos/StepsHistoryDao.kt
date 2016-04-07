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
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import java.util.*

abstract class StepsHistoryDao {

    @SqlUpdate("insert into $TABLE_NAME " +
            "($COL_ID, $COL_STEPS_ID, $COL_USERS_ID, $COL_POSITION," +
            "$COL_TITLE, $COL_DESCRIPTION, $COL_PUBLISHED," +
            "$COL_COMPLETED, $COL_TIME_CHANGED, $COL_POSSIBLE_DATE) " +
            "values (:$COL_ID, :$COL_STEPS_ID, :$COL_USERS_ID," +
            ":$COL_POSITION, :$COL_TITLE, :$COL_DESCRIPTION, :$COL_PUBLISHED," +
            ":$COL_COMPLETED, :$COL_TIME_CHANGED, :$COL_POSSIBLE_DATE)")
    abstract fun insert(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_STEPS_ID) stepsId: UUID,
            @Bind(COL_USERS_ID) usersId: UUID,
            @Bind(COL_POSITION) position: Int,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_DESCRIPTION) description: String,
            @Bind(COL_PUBLISHED) published: Boolean,
            @Bind(COL_COMPLETED) completed: Boolean,
            @Bind(COL_POSSIBLE_DATE) possibleDate: String,
            @Bind(COL_TIME_CHANGED) timeChanged: Long
    )

    companion object {
        const val TABLE_NAME = "steps_history"

        const val COL_ID = "id"
        const val COL_STEPS_ID = "steps_id"
        const val COL_USERS_ID = "users_id"
        const val COL_POSITION = "position"
        const val COL_TITLE = "title"
        const val COL_DESCRIPTION = "description"
        const val COL_PUBLISHED = "published"
        const val COL_COMPLETED = "completed"
        const val COL_TIME_CHANGED = "time_changed"
        const val COL_POSSIBLE_DATE = "possible_date"
    }
}
