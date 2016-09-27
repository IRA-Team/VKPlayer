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

package com.irateam.vkplayer.util

import com.irateam.vkplayer.model.Audio
import java.util.*

object Comparators {

    val ARTIST_COMPARATOR = Comparator<Audio> { audio1, audio2 ->
        audio1.artist?.compareTo(audio2.artist) ?: -1
    }

    val ARTIST_REVERSE_COMPARATOR = Comparator<Audio> { audio1, audio2 ->
        audio2.artist?.compareTo(audio1.artist) ?: -1
    }

    val TITLE_COMPARATOR = Comparator<Audio> { audio1, audio2 ->
        audio1.title?.compareTo(audio2.title) ?: -1
    }

    val TITLE_REVERSE_COMPARATOR = Comparator<Audio> { audio1, audio2 ->
        audio2.title?.compareTo(audio1.title) ?: -1
    }

    val LENGTH_COMPARATOR = Comparator<Audio> { audio1, audio2 ->
        audio1.duration.compareTo(audio2.duration)
    }
}