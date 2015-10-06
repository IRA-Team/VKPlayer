package com.irateam.vkplayer.player;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.vk.sdk.api.model.VKApiAudio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
            Log.i("Uri", uri);
            int index = Integer.parseInt(uri.substring(1));
            Log.i("Index", String.valueOf(index));
            VKApiAudio vkApiAudio = Player.getInstance().getAudio(index);
            Log.i("Audio", vkApiAudio.artist);
            url = new URL(vkApiAudio.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection != null) {
                Log.i("Connection", String.valueOf(true));
                Log.i("Connection", String.valueOf(connection.getURL()));
            }
            input = connection.getInputStream();
            File directory = new File(Environment.getExternalStorageDirectory() + "/VKPlayer/");
            directory.mkdirs();
            output = new FileOutputStream(directory.getPath() + "/" + index + ".mp3");
            new DownloadTask().execute();
            Thread.sleep(200);
            fileLength = connection.getContentLength();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }

            System.out.println("fis start");
            fis = new FileInputStream(directory.getPath() + "/" + index + ".mp3");

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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

