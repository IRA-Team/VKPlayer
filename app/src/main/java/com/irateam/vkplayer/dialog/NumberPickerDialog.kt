/*
 * Copyright (C) 2016 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.EditText
import com.irateam.vkplayer.R
import com.irateam.vkplayer.util.extension.getViewById

class NumberPickerDialog : DialogFragment() {

    private var defaultNumber: Int? = null
    var onNumberPickedListener: ((Int) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            defaultNumber = getInt(DEFAULT_NUMBER)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity.layoutInflater.inflate(R.layout.dialog_number_picker, null)
        val numberLayout: TextInputLayout = view.getViewById(R.id.number_layout)
        val number: EditText = view.getViewById(R.id.number)
        number.setText((defaultNumber ?: 0).toString())

        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.title_sync_audio_count)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel) { dialog, button ->
                    dismiss()
                }
                .setView(view)
                .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val error = validate(number)
                numberLayout.error = error
                if (error == null) {
                    onNumberPickedListener?.invoke(number.text.toString().toInt())
                    dismiss()
                }
            }
        }

        return dialog
    }

    private fun validate(numberEditText: EditText): String? {
        val number = try {
            numberEditText.text.toString().toInt()
        } catch (e: NumberFormatException) {
            return activity.getString(R.string.error_wrong_number_format)
        }

        return when (number) {
            0 -> activity.getString(R.string.error_sync_count_must_be_greater) + " 0"
            else -> null
        }
    }

    companion object {

        val TAG: String = NumberPickerDialog::class.java.name
        val DEFAULT_NUMBER = "DEFAULT_NUMBER"

        fun newInstance(defaultNumber: Int): NumberPickerDialog {
            val dialog = NumberPickerDialog()

            val args = Bundle()
            args.putInt(DEFAULT_NUMBER, defaultNumber)

            dialog.arguments = args

            return dialog
        }
    }
}