package com.gbros.tabslite.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.gbros.tabslite.utilities.FAVORITES_PLAYLIST_ID
import com.gbros.tabslite.utilities.TOP_TABS_PLAYLIST_ID

/**
 * The Data Access Object for the Tab Full class.
 */
@Dao
interface TabDao {
    @Query("SELECT * FROM tabs WHERE id = :tabId")
    suspend fun getTab(tabId: Int): Tab

    @Query("SELECT * FROM tabs WHERE id IN (:tabIds)")
    fun getTabs(tabIds: List<Int>): LiveData<List<Tab>>

    fun getFavoriteTabs(): LiveData<List<TabFullWithPlaylistEntry>> = getPlaylistTabs(FAVORITES_PLAYLIST_ID)

    fun getPopularTabs(): LiveData<List<TabFullWithPlaylistEntry>> = getPlaylistTabs(TOP_TABS_PLAYLIST_ID)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM tabs INNER JOIN playlist_entry ON tabs.id = playlist_entry.tab_id INNER JOIN playlist ON playlist_entry.playlist_id = playlist.id WHERE playlist_entry.playlist_id = :playlistId")
    fun getPlaylistTabs(playlistId: Int): LiveData<List<TabFullWithPlaylistEntry>>

    @Query("SELECT EXISTS(SELECT 1 FROM tabs WHERE id = :tabId AND content != '' LIMIT 1)")
    suspend fun exists(tabId: Int): Boolean

    @Query("SELECT * FROM tabs WHERE song_name LIKE :songName + '%'")
    fun getTabsByName(songName: String): LiveData<List<Tab>>

    @Query("SELECT * FROM tabs WHERE song_id = :songId")
    fun getTabsBySongId(songId: Int): LiveData<List<Tab>>

    @Query("SELECT * FROM tabs WHERE artist_name LIKE '%' + :artist + '%'")
    fun getTabsByArtist(artist: String): LiveData<List<Tab>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tab: Tab)
}