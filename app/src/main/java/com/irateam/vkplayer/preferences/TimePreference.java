package com.irateam.vkplayer.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TimePicker;

import com.irateam.vkplayer.R;

public class TimePreference extends DialogPreference {
    private int hour;
    private int minute;
    private TimePicker picker = null;

    public static int getHour(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[0]);
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[1]);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true);
        setPositiveButtonText(context.getString(R.string.ok));
        setNegativeButtonText(context.getString(R.string.cancel));
    }

    @Override
    protected View onCreateDialogView() {
        return picker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        ViewParent oldParent = picker.getParent();
        if (oldParent != v) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(picker);
            }
            picker.setCurrentHour(hour);
            picker.setCurrentMinute(minute);
        }

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            hour = picker.getCurrentHour();
            minute = picker.getCurrentMinute();
            String time = String.format("%02d", hour) + ":" + String.format("%02d", minute);
            if (callChangeListener(time)) {
                persistString(time);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time;
        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            time = defaultValue.toString();
        }
        hour = getHour(time);
        minute = getMinute(time);
    }
}
