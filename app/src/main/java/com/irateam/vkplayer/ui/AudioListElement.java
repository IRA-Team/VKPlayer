package com.irateam.vkplayer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.irateam.vkplayer.R;

public class AudioListElement extends FrameLayout implements Checkable {

    TextView title;
    TextView artist;
    TextView duration;
    ImageView cover;

    boolean checked;
    private Drawable coverDrawable;

    public AudioListElement(Context context) {
        super(context);
    }

    public AudioListElement(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.player_list_element_layout, this);
        title = (TextView) findViewById(R.id.player_list_element_song_name);
        artist = (TextView) findViewById(R.id.player_list_element_author);
        duration = (TextView) findViewById(R.id.player_list_element_duration);
        cover = (ImageView) findViewById(R.id.player_list_element_cover);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AudioListElement,
                0, 0);

        try {
            String title = a.getString(R.styleable.AudioListElement_song_name);
            if (title != null) {
                setTitle(title);
            }

            String artist = a.getString(R.styleable.AudioListElement_artist);
            if (artist != null) {
                setArtist(artist);
            }

            int duration = a.getInteger(R.styleable.AudioListElement_duration, 0);
            if (duration > 0) {
                setDuration(duration);
            }

            int cover = a.getResourceId(R.styleable.AudioListElement_cover, 0);
            if (cover != 0) {
                setCover(context.getResources().getDrawable(cover));
            }

            checked = a.getBoolean(R.styleable.AudioListElement_checked, false);

        } finally {
            a.recycle();
        }


    }

    public AudioListElement(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AudioListElement(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public String getTitle() {
        return title.getText().toString();
    }

    public void setArtist(String artist) {
        this.artist.setText(artist);
    }

    public String getArtist() {
        return artist.getText().toString();
    }

    public void setCover(Drawable cover) {
        coverDrawable = cover;
        this.cover.setImageDrawable(cover);
    }

    public Drawable getCover() {
        return coverDrawable;
    }

    public void setDuration(int duration) {
        this.duration.setText(String.format("%02d:%02d", duration / 60, duration % 60));
    }

    public int getDuration() {
        return Integer.valueOf(duration.getText().toString());
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (checked) {
            setBackgroundColor(getResources().getColor(R.color.player_list_element_checked_color));
            Drawable[] layers = new Drawable[2];
            layers[0] = coverDrawable;
            layers[1] = getResources().getDrawable(R.drawable.player_list_element_check_overlay);
            cover.setImageDrawable(new LayerDrawable(layers));
        } else {
            setBackgroundColor(getResources().getColor(R.color.player_list_element_color));
            cover.setImageDrawable(coverDrawable);
        }
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        checked = !checked;
        setChecked(checked);
    }

    public void setCoverOnClickListener(OnClickListener listener) {
        cover.setOnClickListener(listener);
    }
}
