package com.irateam.vkplayer.player;

import android.os.AsyncTask;
import android.os.Environment;

import com.irateam.vkplayer.player.Player;
import com.vk.sdk.api.model.VKApiAudio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class ServerProxy extends NanoHTTPD {

    FileInputStream fis = null;
    InputStream input = null;
    OutputStream output = null;
    HttpURLConnection connection = null;
    URL url = null;
    int fileLength;

    public ServerProxy() {
        super(8080);
    }


    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header, Map<String, String> parameters,
                          Map<String, String> files) {

        try {
            int index = Integer.parseInt(uri.substring(1));
            VKApiAudio vkApiAudio = Player.getInstance().getAudio(index);
            url = new URL(vkApiAudio.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            input = connection.getInputStream();
            output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Music/" + index + ".mp3");
            new DownloadTask().execute();
            //Thread.sleep(200000);
            fileLength = connection.getContentLength();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }
            fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/Music/" + index + ".mp3");

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new NanoHTTPD.Response(Response.Status.OK, "audio/mpeg", fis);
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... sUrl) {
            try {
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }
}

