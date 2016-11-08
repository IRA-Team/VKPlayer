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

package com.irateam.vkplayer.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.irateam.vkplayer.R
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById(R.id.button_vk_login)?.setOnClickListener {
            VKSdk.login(this@LoginActivity, VKScope.AUDIO)
        }

        findViewById(R.id.button_github)?.setOnClickListener {
            val uri = Uri.parse("https://github.com/IRA-Team/VKPlayer")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        VKSdk.onActivityResult(requestCode, resultCode, data, VKAccessTokenCallback())
    }

    private inner class VKAccessTokenCallback : VKCallback<VKAccessToken> {

        override fun onResult(res: VKAccessToken) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        override fun onError(error: VKError) {

        }
    }
}
