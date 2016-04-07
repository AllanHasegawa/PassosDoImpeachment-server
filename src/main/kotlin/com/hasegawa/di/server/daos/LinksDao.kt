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

import com.hasegawa.di.server.pokos.StepLink
import com.hasegawa.di.server.utils.toUnixTimestamp
import org.joda.time.DateTime
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.sqlobject.*
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.ResultSet
import java.util.*

@RegisterMapper(LinkMapper::class)
abstract class LinksDao {
    @CreateSqlObject
    abstract fun createLinksHistoryDao(): LinksHistoryDao

    @SqlQuery("select * from $TABLE_NAME where $COL_STEP_ID = :$COL_STEP_ID order by $COL_TIME_CREATED")
    abstract fun findAllByStepId(@Bind(COL_STEP_ID) stepId: UUID): List<StepLink>

    @SqlQuery("select * from $TABLE_NAME where $COL_ID=:$COL_ID order by $COL_TIME_CREATED")
    abstract fun findById(@Bind(COL_ID) id: UUID): StepLink

    @SqlUpdate("insert into $TABLE_NAME (" +
            "$COL_ID," +
            "$COL_USERS_ID," +
            "$COL_STEP_ID," +
            "$COL_TITLE," +
            "$COL_URL," +
            "$COL_TIME_CREATED," +
            "$COL_PUBLISHED" +
            ") values (" +
            ":$COL_ID," +
            ":$COL_USERS_ID," +
            ":$COL_STEP_ID," +
            ":$COL_TITLE," +
            ":$COL_URL," +
            ":$COL_TIME_CREATED," +
            "false)")
    abstract fun insert(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_USERS_ID) usersId: UUID,
            @Bind(COL_STEP_ID) stepId: UUID,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_URL) url: String,
            @Bind(COL_TIME_CREATED) timeCreated: Long
    )


    @SqlUpdate("update $TABLE_NAME set " +
            "$COL_STEP_ID=:$COL_STEP_ID," +
            "$COL_TITLE=:$COL_TITLE," +
            "$COL_URL=:$COL_URL," +
            "$COL_PUBLISHED=:$COL_PUBLISHED " +
            "where $COL_ID=:$COL_ID")
    abstract protected fun justUpdate(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_STEP_ID) stepId: UUID,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_URL) url: String,
            @Bind(COL_PUBLISHED) published: Boolean
    )

    @Transaction
    fun update(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_STEP_ID) stepId: UUID,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_URL) url: String,
            @Bind(COL_PUBLISHED) published: Boolean
    ) {
        val linksHistoryDao = createLinksHistoryDao()

        val link = findById(id)
        linksHistoryDao.insert(
                UUID.randomUUID(),
                link.id!!,
                link.usersId!!,
                link.title!!,
                link.url!!,
                link.timeCreated!!,
                link.published!!,
                DateTime.now().toUnixTimestamp()
        )

        justUpdate(id, stepId, title, url, published)
    }

    companion object {
        const val TABLE_NAME = "links"


        const val COL_ID = "id"
        const val COL_USERS_ID = "users_id"
        const val COL_STEP_ID = "steps_id"
        const val COL_TITLE = "title"
        const val COL_URL = "url"
        const val COL_TIME_CREATED = "time_created"
        const val COL_PUBLISHED = "published"
    }
}


class LinkMapper : ResultSetMapper<StepLink> {
    override fun map(index: Int, r: ResultSet?, ctx: StatementContext?): StepLink? {
        return StepLink(
                r!!.getObject(LinksDao.COL_ID) as UUID,
                r.getObject(LinksDao.COL_USERS_ID) as UUID,
                r.getString(LinksDao.COL_TITLE),
                r.getString(LinksDao.COL_URL),
                r.getLong(LinksDao.COL_TIME_CREATED),
                r.getBoolean(LinksDao.COL_PUBLISHED)
        )
    }
}
