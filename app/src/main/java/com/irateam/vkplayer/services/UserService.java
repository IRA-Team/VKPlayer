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

package com.irateam.vkplayer.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;

import org.json.JSONException;

public class UserService {

    public static final String USER_LINK = "http://vk.com/id";
    private static final String USER_ID = "user_id";
    private static final String USER_FIRST_NAME = "user_first_name";
    private static final String USER_SECOND_NAME = "user_second_name";
    private static final String USER_PHOTO_URL = "user_photo_url";

    private Context context;
    private SharedPreferences sharedPreferences;

    public UserService(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context); //TODO: Rework do database
    }

    public void getCurrentUser(final Listener listener) {
        VKApiUser user = new VKApiUser();
        user.id = sharedPreferences.getInt(USER_ID, -1);
        if (user.id != -1) {
            user.first_name = sharedPreferences.getString(USER_FIRST_NAME, "");
            user.last_name = sharedPreferences.getString(USER_SECOND_NAME, "");
            user.photo_100 = sharedPreferences.getString(USER_PHOTO_URL, "");
            listener.onFinished(user);
        } else {
            VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_100")).executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        VKApiUser user = new VKApiUser().parse(response.json.getJSONArray("response").getJSONObject(0));
                        sharedPreferences.edit()
                                .putInt(USER_ID, user.id)
                                .putString(USER_FIRST_NAME, user.first_name)
                                .putString(USER_SECOND_NAME, user.last_name)
                                .putString(USER_PHOTO_URL, user.photo_100)
                                .apply();
                        listener.onFinished(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });
        }
    }

    public interface Listener {
        void onFinished(VKApiUser user);
    }
}
