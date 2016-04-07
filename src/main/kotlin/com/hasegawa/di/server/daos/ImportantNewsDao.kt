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

import com.hasegawa.di.server.pokos.ImportantNews
import com.hasegawa.di.server.utils.toUnixTimestamp
import org.joda.time.DateTime
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import org.skife.jdbi.v2.sqlobject.Transaction
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.ResultSet
import java.util.*

@RegisterMapper(ImportantNewsDao.ImportantNewsMapper::class)
abstract class ImportantNewsDao {
    @SqlQuery("select * from $TABLE_NAME where $COL_ID=:$COL_ID")
    abstract fun findById(@Bind(COL_ID) id: UUID): ImportantNews

    @SqlQuery("select * from $TABLE_NAME order by $COL_DATE desc")
    abstract fun findAll(): List<ImportantNews>

    @SqlUpdate("insert into $TABLE_NAME (" +
            "$COL_ID," +
            "$COL_USERS_ID," +
            "$COL_TITLE," +
            "$COL_URL," +
            "$COL_PUBLISHED," +
            "$COL_DATE," +
            "$COL_TLDR," +
            "$COL_APP_MESSAGE," +
            "$COL_SEND_APP_NOTIFICATION," +
            "$COL_TIME_CREATED" +
            ") values (" +
            ":$COL_ID," +
            ":$COL_USERS_ID," +
            ":$COL_TITLE," +
            ":$COL_URL," +
            ":$COL_PUBLISHED," +
            ":$COL_DATE," +
            ":$COL_TLDR," +
            ":$COL_APP_MESSAGE," +
            ":$COL_SEND_APP_NOTIFICATION," +
            ":$COL_TIME_CREATED" +
            ")")
    abstract fun insert(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_USERS_ID) usersId: UUID,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_URL) url: String,
            @Bind(COL_PUBLISHED) published: Boolean,
            @Bind(COL_DATE) date: Long,
            @Bind(COL_TLDR) tldr: String?,
            @Bind(COL_APP_MESSAGE) appMessage: String?,
            @Bind(COL_SEND_APP_NOTIFICATION) sendAppNotification: Boolean,
            @Bind(COL_TIME_CREATED) timeCreated: Long
    )

    @Transaction
    fun update(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_USERS_ID) usersId: UUID,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_URL) url: String,
            @Bind(COL_PUBLISHED) published: Boolean,
            @Bind(COL_DATE) date: Long,
            @Bind(COL_TLDR) tldr: String?
    ) {
        val importantNews = findById(id)

        insertHistory(
                UUID.randomUUID(),
                importantNews.id!!,
                importantNews.usersId!!,
                importantNews.title!!,
                importantNews.url!!,
                importantNews.published!!,
                importantNews.date!!,
                importantNews.tldr,
                importantNews.appMessage,
                importantNews.sendAppNotification!!,
                importantNews.timeCreated!!,
                DateTime.now().toUnixTimestamp()
        )

        justUpdate(
                id,
                usersId,
                title,
                url,
                published,
                date,
                tldr
        )
    }

    @SqlUpdate("update $TABLE_NAME set " +
            "$COL_USERS_ID=:$COL_USERS_ID," +
            "$COL_TITLE=:$COL_TITLE," +
            "$COL_URL=:$COL_URL," +
            "$COL_PUBLISHED=:$COL_PUBLISHED," +
            "$COL_DATE=:$COL_DATE," +
            "$COL_TLDR=:$COL_TLDR" +
            " where $COL_ID=:$COL_ID")
    abstract protected fun justUpdate(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_USERS_ID) usersId: UUID,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_URL) url: String,
            @Bind(COL_PUBLISHED) published: Boolean,
            @Bind(COL_DATE) date: Long,
            @Bind(COL_TLDR) tldr: String?
    )


    @SqlUpdate("insert into ${TABLE_NAME}_history (" +
            "$COL_ID," +
            "$COL_IMPORTANT_NEWS_ID," +
            "$COL_USERS_ID," +
            "$COL_TITLE," +
            "$COL_URL," +
            "$COL_PUBLISHED," +
            "$COL_DATE," +
            "$COL_TLDR," +
            "$COL_APP_MESSAGE," +
            "$COL_SEND_APP_NOTIFICATION," +
            "$COL_TIME_CREATED," +
            "$COL_TIME_CHANGED " +
            ") values (" +
            ":$COL_ID," +
            ":$COL_IMPORTANT_NEWS_ID," +
            ":$COL_USERS_ID," +
            ":$COL_TITLE," +
            ":$COL_URL," +
            ":$COL_PUBLISHED," +
            ":$COL_DATE," +
            ":$COL_TLDR," +
            ":$COL_APP_MESSAGE," +
            ":$COL_SEND_APP_NOTIFICATION," +
            ":$COL_TIME_CREATED," +
            ":$COL_TIME_CHANGED" +
            ")")
    abstract protected fun insertHistory(
            @Bind(COL_ID) id: UUID,
            @Bind(COL_IMPORTANT_NEWS_ID) importantNewsId: UUID,
            @Bind(COL_USERS_ID) usersId: UUID,
            @Bind(COL_TITLE) title: String,
            @Bind(COL_URL) url: String,
            @Bind(COL_PUBLISHED) published: Boolean,
            @Bind(COL_DATE) date: Long,
            @Bind(COL_TLDR) tldr: String?,
            @Bind(COL_APP_MESSAGE) appMessage: String?,
            @Bind(COL_SEND_APP_NOTIFICATION) sendAppNotification: Boolean,
            @Bind(COL_TIME_CREATED) timeCreated: Long,
            @Bind(COL_TIME_CHANGED) timeChanged: Long
    )

    companion object {
        const val TABLE_NAME = "important_news"

        const val COL_ID = "id"
        const val COL_USERS_ID = "users_id"
        const val COL_TITLE = "title"
        const val COL_URL = "url"
        const val COL_PUBLISHED = "published"
        const val COL_DATE = "date"
        const val COL_TLDR = "tldr"
        const val COL_APP_MESSAGE = "app_message"
        const val COL_SEND_APP_NOTIFICATION = "send_app_notification"
        const val COL_TIME_CREATED = "time_created"

        // vals for the "important_news_history" table
        const val COL_IMPORTANT_NEWS_ID = "important_news_id"
        const val COL_TIME_CHANGED = "time_changed"
    }

    class ImportantNewsMapper : ResultSetMapper<ImportantNews> {
        override fun map(index: Int, r: ResultSet?, ctx: StatementContext?): ImportantNews? {
            var appMessage: String? = r!!.getString(COL_APP_MESSAGE)
            if (r.wasNull()) appMessage = null

            var tldr: String? = r.getString(COL_TLDR)
            if (r.wasNull()) tldr = null

            return ImportantNews(
                    r.getObject(COL_ID) as UUID,
                    r.getObject(COL_USERS_ID) as UUID,
                    r.getString(COL_TITLE),
                    r.getString(COL_URL),
                    r.getBoolean(COL_PUBLISHED),
                    r.getLong(COL_DATE),
                    tldr,
                    appMessage,
                    r.getBoolean(COL_SEND_APP_NOTIFICATION),
                    r.getLong(COL_TIME_CREATED)
            )
        }
    }
}

