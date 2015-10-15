package com.irateam.vkplayer.adapter;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TimePicker;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.RecyclerViewSettings;
import com.irateam.vkplayer.viewholders.SettingsRecyclerViewHolder;

import java.util.ArrayList;
import java.util.Calendar;

public class SettingsRecyclerViewAdapter extends RecyclerView.Adapter<SettingsRecyclerViewHolder> {

    private ArrayList<RecyclerViewSettings> settings;

    public SettingsRecyclerViewAdapter(ArrayList<RecyclerViewSettings> settings) {
        this.settings = settings;
    }

    @Override
    public SettingsRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.settings_recycler_view_item, viewGroup, false);
        return new SettingsRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SettingsRecyclerViewHolder holder, int position) {
        holder.name.setText(settings.get(position).getName());
        holder.other.setText(settings.get(position).getOther());
        holder.aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.aSwitch.isChecked()) {
                    //AlertDialog with numberPicker
                    Context context = v.getContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    final EditText numberEditText = new EditText(context);
                    numberEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    final FrameLayout parent = new FrameLayout(context);
                    parent.addView(numberEditText, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER));
                    builder.setView(parent);
                    builder.setTitle("Enter the numer");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            holder.other.append("\n" + numberEditText.getText());
                        }
                    });
                    final AlertDialog alertDialog = builder.create();

                    //TimePickerDialog
                    Calendar c = Calendar.getInstance();
                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                    int mMinute = c.get(Calendar.MINUTE);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            alertDialog.show();
                            holder.other.setText(hourOfDay + ":" + minute + " ");
                        }
                    }, mHour, mMinute, true);
                    timePickerDialog.show();
                } else
                    holder.other.setText("");

            }
        });
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }
}
