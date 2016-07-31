/*
 * Copyright (C) 2015 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.api.service

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.irateam.vkplayer.api.AbstractQuery
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.api.VKQuery
import com.irateam.vkplayer.models.User
import com.irateam.vkplayer.util.isNetworkAvailable
import com.vk.sdk.api.VKApiConst
import com.vk.sdk.api.VKParameters
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import com.vk.sdk.api.methods.VKApiUsers
import com.vk.sdk.api.model.VKApiUser

class UserService {

    private val context: Context
    private val sharedPreferences: SharedPreferences

    constructor(context: Context) {
        this.context = context
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getCurrent(): Query<User> = if (context.isNetworkAvailable()) {
        val params = VKParameters.from(VKApiConst.FIELDS, VKApiUser.FIELD_PHOTO_100)
        val request = VKApiUsers().get(params)
        CurrentUserQuery(request)
    } else {
        CachedCurrentUserQuery()
    }

    private fun parseFromPreferences(): User {
        val id = sharedPreferences.getInt(USER_ID, -1)
        val firstName = sharedPreferences.getString(USER_FIRST_NAME, "")
        val lastName = sharedPreferences.getString(USER_SECOND_NAME, "")
        val photo100px = sharedPreferences.getString(USER_PHOTO_URL, "")

        return User(id, firstName, lastName, photo100px)
    }

    private fun parseFromVKResponse(response: VKResponse): User {
        val responseBody = response.json.getJSONArray("response")
        val rawUser = responseBody.getJSONObject(0)

        return User(
                rawUser.optInt("id"),
                rawUser.optString("first_name"),
                rawUser.optString("last_name"),
                rawUser.optString("photo_100"))
    }

    private fun saveToPreferences(user: User) = sharedPreferences.edit()
            .putInt(USER_ID, user.id)
            .putString(USER_FIRST_NAME, user.firstName)
            .putString(USER_SECOND_NAME, user.lastName)
            .putString(USER_PHOTO_URL, user.photo100px)
            .apply()

    private inner class CurrentUserQuery(request: VKRequest) : VKQuery<User>(request) {

        override fun parse(response: VKResponse): User {
            val user = parseFromVKResponse(response)
            saveToPreferences(user)
            return user
        }
    }

    private inner class CachedCurrentUserQuery : AbstractQuery<User>() {
        override fun query(): User = parseFromPreferences()
    }


    companion object {

        private val USER_ID = "user_id"
        private val USER_FIRST_NAME = "user_first_name"
        private val USER_SECOND_NAME = "user_second_name"
        private val USER_PHOTO_URL = "user_photo_url"
    }
}
