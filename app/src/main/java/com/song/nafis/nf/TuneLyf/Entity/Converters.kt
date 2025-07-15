package com.song.nafis.nf.TuneLyf.Entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.song.nafis.nf.TuneLyf.ApiModel.JamendoTrack

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromSongList(value: List<JamendoTrack>?): String? {
        if (value == null) return null
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSongList(value: String?): List<JamendoTrack>? {
        if (value == null) return null
        val listType = object : TypeToken<List<JamendoTrack>>() {}.type
        return gson.fromJson(value, listType)
    }
}
