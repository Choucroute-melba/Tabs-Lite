package com.gbros.tabslite.data.playlist

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gbros.tabslite.data.playlist.Playlist.Companion.FAVORITES_PLAYLIST_ID

/**
 * The Data Access Object for the Chord Variation class.
 */
@Dao
interface PlaylistEntryDao {
    @Query("SELECT * FROM playlist_entry")
    suspend fun getAllPlaylistEntries(): List<DataPlaylistEntry>

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId")
    fun getLivePlaylistItems(playlistId: Int): LiveData<List<DataPlaylistEntry>>

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId AND next_entry_id IS NULL")
    suspend fun getLastEntryInPlaylist(playlistId: Int): DataPlaylistEntry?

    @Query("SELECT * FROM playlist_entry WHERE entry_id = :entryId")
    suspend fun getEntryById(entryId: Int): DataPlaylistEntry?

    @Query("UPDATE playlist_entry SET next_entry_id = :nextEntryId WHERE entry_id = :thisEntryId")
    suspend fun setNextEntryId(thisEntryId: Int?, nextEntryId: Int?)

    @Query("UPDATE playlist_entry SET prev_entry_id = :prevEntryId WHERE entry_id = :thisEntryId")
    suspend fun setPrevEntryId(thisEntryId: Int?, prevEntryId: Int?)

    @Query("""
            UPDATE playlist_entry SET next_entry_id = (CASE entry_id
                    when :srcPrv then :srcNxt
                    when :src then :destNxt
                    when :destPrv then :src
                    else next_entry_id
                    END),
                prev_entry_id = (CASE entry_id
                    when :srcNxt then :srcPrv
                    when :src then :destPrv
                    when :destNxt then :src
                    else prev_entry_id
                    END)
            """)
    suspend fun moveEntry(srcPrv: Int?, srcNxt: Int?, src: Int, destPrv: Int?, destNxt: Int?)

    /**
     * Move an entry to before another entry
     */
    suspend fun moveEntryBefore(entry: IDataPlaylistEntry, beforeEntry: IDataPlaylistEntry) {
        moveEntry(entry.prevEntryId, entry.nextEntryId, entry.entryId, beforeEntry.prevEntryId, beforeEntry.entryId)
    }

    /**
     * Move an entry to after another entry
     */
    suspend fun moveEntryAfter(entry: IDataPlaylistEntry, afterEntry: IDataPlaylistEntry) {
        moveEntry(entry.prevEntryId, entry.nextEntryId, entry.entryId, afterEntry.entryId, afterEntry.nextEntryId)
    }

    @Transaction
    suspend fun removeEntryFromPlaylist(entry: IDataPlaylistEntry) {
        if (entry.prevEntryId != null) {
            // Update the next entry ID of the previous entry to skip the removed entry
            setNextEntryId(entry.prevEntryId, entry.nextEntryId)
        }

        if (entry.nextEntryId != null) {
            // Update the previous entry ID of the next entry to skip the removed entry
            setPrevEntryId(entry.nextEntryId, entry.prevEntryId)
        }

        // Remove the entry itself
        deleteEntry(entry.entryId)
    }

    @Update
    fun update(entry: DataPlaylistEntry)

    @Query("INSERT INTO playlist_entry (playlist_id, tab_id, next_entry_id, prev_entry_id, date_added, transpose) VALUES (:playlistId, :tabId, :nextEntryId, :prevEntryId, :dateAdded, :transpose)")
    suspend fun insert(playlistId: Int, tabId: Int, nextEntryId: Int?, prevEntryId: Int?, dateAdded: Long, transpose: Int)

    suspend fun insertToFavorites(tabId: Int, transpose: Int)
        = insert(FAVORITES_PLAYLIST_ID, tabId, null, null, System.currentTimeMillis(), transpose)

    @Transaction
    suspend fun addToPlaylist(playlistId: Int, tabId: Int, transpose: Int) {
        val lastEntry = getLastEntryInPlaylist(playlistId = playlistId)
        val newEntry = DataPlaylistEntry(entryId = 0, playlistId = playlistId, tabId = tabId, nextEntryId = null, prevEntryId = lastEntry?.entryId, dateAdded = System.currentTimeMillis(), transpose = transpose )
        val newEntryId = insert(newEntry).toInt()

        if (lastEntry != null) {
            val updatedLastEntry = DataPlaylistEntry(entryId = lastEntry.entryId, playlistId = lastEntry.playlistId, tabId = lastEntry.tabId, nextEntryId = newEntryId, prevEntryId = lastEntry.prevEntryId, dateAdded = lastEntry.dateAdded, transpose = lastEntry.transpose)
            update(updatedLastEntry)
        }
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: DataPlaylistEntry): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entries: List<DataPlaylistEntry>)

    @Query("DELETE FROM playlist_entry WHERE entry_id = :entry_id")
    suspend fun deleteEntry(entry_id: Int)

    @Query("DELETE FROM playlist_entry WHERE playlist_id = :playlistId AND tab_id = :tabId")
    suspend fun deleteTabFromPlaylist(tabId: Int, playlistId: Int)

    suspend fun deleteTabFromFavorites(tabId: Int) = deleteTabFromPlaylist(tabId, FAVORITES_PLAYLIST_ID)

    @Query("DELETE FROM playlist_entry WHERE playlist_id = :playlistId")
    fun clearPlaylist(playlistId: Int)

    fun clearTopTabsPlaylist() = clearPlaylist(Playlist.TOP_TABS_PLAYLIST_ID)

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId")
    suspend fun getAllEntriesInPlaylist(playlistId: Int): List<DataPlaylistEntry>

    suspend fun getSortedEntriesInPlaylist(playlistId: Int): List<DataPlaylistEntry> {
        val allEntries = getAllEntriesInPlaylist(playlistId = playlistId)
        return DataPlaylistEntry.sortLinkedList(allEntries)
    }

    suspend fun getSelfContainedPlaylists(playlists: List<IPlaylist>): List<SelfContainedPlaylist> {
        val selfContainedPlaylists: MutableList<SelfContainedPlaylist> = mutableListOf()
        for (playlist in playlists) {
            selfContainedPlaylists.add(SelfContainedPlaylist(playlist, getSortedEntriesInPlaylist(playlist.playlistId) as List<IPlaylistEntry>))
        }

        return selfContainedPlaylists
    }

    @Query("SELECT EXISTS(SELECT * FROM playlist_entry WHERE playlist_id = $FAVORITES_PLAYLIST_ID AND tab_id = :tabId)")
    fun tabExistsInFavoritesLive(tabId: Int): LiveData<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM playlist_entry WHERE playlist_id = $FAVORITES_PLAYLIST_ID AND tab_id = :tabId)")
    suspend fun tabExistsInFavorites(tabId: Int): Boolean

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = $FAVORITES_PLAYLIST_ID AND tab_id = :tabId")
    suspend fun getFavoritesPlaylistEntry(tabId: Int): DataPlaylistEntry?

    @Query("UPDATE playlist_entry SET transpose = :transpose WHERE playlist_id = $FAVORITES_PLAYLIST_ID AND tab_id = :tabId")
    suspend fun updateFavoriteTabTransposition(tabId: Int, transpose: Int)

    @Query("UPDATE playlist_entry SET transpose = :transpose WHERE entry_id = :entryId")
    suspend fun updateEntryTransposition(entryId: Int, transpose: Int)
}