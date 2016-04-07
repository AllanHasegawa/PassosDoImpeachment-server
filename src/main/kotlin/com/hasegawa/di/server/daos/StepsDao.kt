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

import com.hasegawa.di.server.pokos.Step
import com.hasegawa.di.server.utils.toUnixTimestamp
import org.joda.time.DateTime
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.sqlobject.*
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.ResultSet
import java.util.*

@RegisterMapper(StepMapper::class)
abstract class StepsDao {
    @CreateSqlObject
    abstract fun createStepsHistoryDao(): StepsHistoryDao

    @SqlQuery("select * from $TABLE_NAME order by position")
    abstract fun findAll(): List<Step>

    @SqlUpdate("insert into $TABLE_NAME " +
            "($COL_ID," +
            "$COL_USERS_ID," +
            "$COL_POSITION," +
            "$COL_TITLE," +
            "$COL_DESCRIPTION," +
            "$COL_PUBLISHED," +
            "$COL_POSSIBLE_DATE) " +
            "values (:$COL_ID," +
            ":$COL_USERS_ID," +
            ":$COL_POSITION," +
            ":$COL_TITLE," +
            ":$COL_DESCRIPTION," +
            "false," +
            ":$COL_POSSIBLE_DATE)")
    abstract fun insert(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_USERS_ID) usersId: UUID,
            @Bind(COL_POSITION) position: Int,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_DESCRIPTION) description: String,
            @Bind(COL_POSSIBLE_DATE) possibleDate: String)


    @SqlUpdate("update $TABLE_NAME set $COL_POSITION=:$COL_POSITION," +
            "$COL_TITLE=:$COL_TITLE," +
            "$COL_DESCRIPTION=:$COL_DESCRIPTION," +
            "$COL_PUBLISHED=:$COL_PUBLISHED," +
            "$COL_POSSIBLE_DATE=:$COL_POSSIBLE_DATE," +
            "$COL_COMPLETED=:$COL_COMPLETED " +
            "where $COL_ID=:$COL_ID")
    abstract protected fun justUpdate(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_POSITION) position: Int,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_DESCRIPTION) description: String,
            @Bind(COL_PUBLISHED) published: Boolean,
            @Bind(COL_COMPLETED) completed: Boolean,
            @Bind(COL_POSSIBLE_DATE) possibleDate: String)

    @Transaction
    fun update(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_POSITION) position: Int,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_DESCRIPTION) description: String,
            @Bind(COL_PUBLISHED) published: Boolean,
            @Bind(COL_COMPLETED) completed: Boolean,
            @Bind(COL_POSSIBLE_DATE) possibleDate: String) {
        val stepsHistoryDao = createStepsHistoryDao()

        val step = findById(id)
        stepsHistoryDao.insert(UUID.randomUUID(),
                step.id!!,
                step.usersId!!,
                step.position!!,
                step.title!!,
                step.description!!,
                step.published!!,
                step.completed!!,
                step.possibleDate!!,
                DateTime.now().toUnixTimestamp()
        )

        justUpdate(id, position, title, description, published, completed, possibleDate)
    }

    @SqlQuery("select * from $TABLE_NAME where $COL_ID = :$COL_ID")
    abstract fun findById(@Bind(COL_ID) id: UUID): Step

    abstract fun close()

    companion object {
        const val TABLE_NAME = "steps"

        const val COL_ID = "id"
        const val COL_USERS_ID = "users_id"
        const val COL_POSITION = "position"
        const val COL_TITLE = "title"
        const val COL_DESCRIPTION = "description"
        const val COL_COMPLETED = "completed"
        const val COL_PUBLISHED = "published"
        const val COL_POSSIBLE_DATE = "possible_date"
    }
}

class StepMapper : ResultSetMapper<Step> {
    override fun map(index: Int, r: ResultSet?, ctx: StatementContext?): Step? {
        return Step(
                r!!.getObject(StepsDao.COL_ID) as UUID,
                r.getObject(StepsDao.COL_USERS_ID) as UUID,
                r.getInt(StepsDao.COL_POSITION),
                r.getString(StepsDao.COL_TITLE),
                r.getString(StepsDao.COL_DESCRIPTION),
                r.getString(StepsDao.COL_POSSIBLE_DATE),
                r.getBoolean(StepsDao.COL_COMPLETED),
                r.getBoolean(StepsDao.COL_PUBLISHED)
        )
    }
}
