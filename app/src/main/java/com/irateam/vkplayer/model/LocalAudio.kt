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

package com.irateam.vkplayer.model

import android.os.Parcel
import android.os.Parcelable

class LocalAudio : Audio {

    val path: String

    constructor(artist: String,
                title: String,
                duration: Int,
                path: String) : super(path, artist, title, duration) {

        this.path = path
    }

    private constructor(parcel: Parcel) : super(parcel) {
        this.path = parcel.readString()
    }

    override fun getSource(): String {
        return path
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(path)
    }

    override fun clone() = LocalAudio(
            artist = artist,
            title = title,
            duration = duration,
            path = path)

    companion object {

        var CREATOR: Parcelable.Creator<LocalAudio> = object : Parcelable.Creator<LocalAudio> {

            override fun createFromParcel(source: Parcel): LocalAudio {
                return LocalAudio(source)
            }

            override fun newArray(size: Int): Array<LocalAudio?> {
                return arrayOfNulls(size)
            }
        }
    }
}
