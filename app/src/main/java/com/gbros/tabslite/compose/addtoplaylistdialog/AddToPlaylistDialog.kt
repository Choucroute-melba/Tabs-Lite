package com.gbros.tabslite.compose.addtoplaylistdialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun AddToPlaylistDialog(tabId: Int, transpose: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val playlists by db.playlistDao().getPlaylists().observeAsState(initial = listOf())
    var selectedPlaylist: Playlist? by remember { mutableStateOf(null) }
    var confirmedPlaylist: Playlist? by remember { mutableStateOf(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    AlertDialog(
        icon = {
            Icon(ImageVector.vectorResource(R.drawable.ic_playlist_add), contentDescription = "Add to playlist")
        },
        title = {
            Text(text = "Add to playlist")
        },
        text = {
            Row {
                Column(
                    Modifier.weight(1f)
                ) {
                    PlaylistDropdown(playlists = playlists, selectedPlaylist = selectedPlaylist, onSelectionChange = { selectedPlaylist = it })
                }
                Column(

                ) {
                    Button(
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                        onClick = {
                            showCreatePlaylistDialog = true
                        },
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New playlist")
                    }
                }
            }
        },
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = {
                    confirmedPlaylist = selectedPlaylist
                },
                enabled = selectedPlaylist != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Dismiss")
            }
        }
    )

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(onConfirm = { showCreatePlaylistDialog = false }, onDismiss = { showCreatePlaylistDialog = false })
    }

    LaunchedEffect(key1 = confirmedPlaylist) {
        val copyOfConfirmedlaylist = confirmedPlaylist
        if (copyOfConfirmedlaylist != null) {
            db.playlistEntryDao().addToPlaylist(playlistId = copyOfConfirmedlaylist.playlistId, tabId = tabId, transpose = transpose)
            onConfirm()
        }
    }

}

@Composable @Preview
private fun AddToPlaylistDialogPreview() {
    AppTheme {
        AddToPlaylistDialog(1, 0, {}, {})
    }
}