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
        val number: EditText = view.getViewById(R.id.number)
        number.setText((defaultNumber ?: 0).toString())

        return AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok) { dialog, button ->
                    onNumberPickedListener?.invoke(number.text.toString().toInt())
                }
                .setNegativeButton(R.string.cancel) { dialog, button ->
                    dismiss()
                }
                .setView(view)
                .create()
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