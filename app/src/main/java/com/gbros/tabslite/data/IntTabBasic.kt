package com.gbros.tabslite.data

import java.util.*
import kotlin.collections.ArrayList

interface IntTabBasic : IntSong {
    val tabId: Int
    val type: String
    val part: String
    val version: Int
    val votes: Int
    val rating: Double
    val date: Int
    val status: String
    val presetId: Int
    val tabAccessType: String
    val tpVersion: Int
    val tonalityName: String
    val versionDescription: String

    // in JSON these are in a separate sublevel "recording"
    val recordingIsAcoustic: Boolean
    val recordingTonalityName: String
    val recordingPerformance: String
    val recordingArtists: ArrayList<String>

    val favoriteTime: Long?

    fun getUrl(): String{
        // only allowed chars are alphanumeric and dash.
        val artist = artistName.trim().toLowerCase(Locale.US).replace(' ', '-').replace("[^\\w\\d-]".toRegex(), "")
        val name = songName.trim().toLowerCase(Locale.US).replace(' ', '-').replace("[^\\w\\d-]".toRegex(), "")
        var url = "https://tabslite.com/tab/"
//        if(artist.isNotBlank() && name.isNotBlank()) {
//            url += "$artist/$name-$type-"
//        }
        url += tabId.toString()
        if (this is TabFull && transposed != 0){
            url += "?tsp=$transposed"
        }

        return url
    }
}