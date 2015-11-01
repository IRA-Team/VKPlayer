package com.irateam.vkplayer.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.irateam.vkplayer.models.Audio;

public class AlbumCoverUtils {

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    //TODO: Remove after refactoring class Audio
    public static TextDrawable createFromAudio(Audio audio) {
        String artist = audio.artist.trim();
        String character;
        if (artist.length() > 0) {
            character = String.valueOf(artist.charAt(0));
        } else {
            character = " ";
        }
        return TextDrawable.builder()
                .buildRound(character, ColorGenerator.MATERIAL.getColor(audio.artist.trim()));
    }

    public static Bitmap createBitmapFromAudio(Audio audio) {
        return drawableToBitmap(createFromAudio(audio));
    }
}
