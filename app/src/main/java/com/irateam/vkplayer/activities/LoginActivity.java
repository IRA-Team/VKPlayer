package com.irateam.vkplayer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.irateam.vkplayer.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.button_vk_login).setOnClickListener((v) -> {
            VKSdk.login(LoginActivity.this, VKScope.AUDIO);
        });
        findViewById(R.id.button_github).setOnClickListener((v) -> {
            Uri uri = Uri.parse("https://github.com/IRA-Team/VKPlayer");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                startActivity(new Intent(LoginActivity.this, ListActivity.class));
                finish();
            }

            @Override
            public void onError(VKError error) {
            }
        })) ;
    }
}
