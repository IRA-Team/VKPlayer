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

import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE

interface DialogClickCallback : DialogInterface.OnClickListener {

    var positiveButtonClickListener: ((DialogInterface) -> Unit)?
    var negativeButtonClickListener: ((DialogInterface) -> Unit)?

    override fun onClick(dialogInterface: DialogInterface, button: Int) {
        when(button) {
            BUTTON_POSITIVE -> positiveButtonClickListener?.invoke(dialogInterface)
            BUTTON_NEGATIVE -> negativeButtonClickListener?.invoke(dialogInterface)
        }
    }

}