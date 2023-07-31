package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.sharedfolder.SharedFile
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

internal object FileShareMapper {

    fun Flow<CallUI>.toSharedFilesUi(): Flow<Set<SharedFileUi>> {
        val sharedFiles = hashMapOf<String, SharedFileUi>()
        val cancelledUploads = hashMapOf<String, SharedFileUi>()

        return combine(this.toFiles(), this.toMe()) { f, m -> Pair(f, m) }
            .flatMapLatest { (files, me) ->
                if (files.isEmpty()) flowOf(setOf())
                else files
                   .map { it.mapToSharedFileUi(me.userId) }
                   .merge()
                   .transform { sharedFileUi ->
                       if (!sharedFileUi.isCancelledUpload()) {
                           sharedFiles[sharedFileUi.id] = sharedFileUi
                       } else {
                           sharedFiles.remove(sharedFileUi.id)
                           cancelledUploads[sharedFileUi.id] = sharedFileUi
                       }

                       val values = sharedFiles.values
                       if (values.size + cancelledUploads.values.size == files.size) {
                           emit(values.toSet())
                       }
                   }
            }.distinctUntilChanged()
    }

    private fun SharedFileUi.isCancelledUpload(): Boolean =
        isMine && state == SharedFileUi.State.Cancelled

    private fun Flow<Call>.toFiles(): Flow<Set<SharedFile>> =
        this.map { it.sharedFolder }
            .flatMapLatest { it.files }
            .distinctUntilChanged()

    private fun Flow<Call>.toMe(): Flow<CallParticipant.Me> =
        this.flatMapLatest { it.participants }
            .map { it.me }
            .distinctUntilChanged()

    fun SharedFile.mapToSharedFileUi(myUserId: String): Flow<SharedFileUi> {
        val sharedFile = this@mapToSharedFileUi
        val uri = ImmutableUri(sharedFile.uri)
        return combine(
            sharedFile.sender.combinedDisplayName,
            sharedFile.state.map { it.mapToSharedFileUiState(sharedFile.size) }
        ) { displayName, state ->
            SharedFileUi(
                id = sharedFile.id,
                name = sharedFile.name,
                uri = uri,
                size = sharedFile.size,
                sender = displayName ?: sharedFile.sender.userId,
                time = sharedFile.creationTime,
                state = state,
                isMine = sharedFile.sender.userId == myUserId
            )
        }.distinctUntilChanged()
    }

    fun SharedFile.State.mapToSharedFileUiState(fileSize: Long): SharedFileUi.State {
        return when (this) {
            SharedFile.State.Available -> SharedFileUi.State.Available
            SharedFile.State.Cancelled -> SharedFileUi.State.Cancelled
            is SharedFile.State.Error -> SharedFileUi.State.Error
            is SharedFile.State.InProgress -> SharedFileUi.State.InProgress((this.progress / fileSize.toFloat()).coerceIn(0.0f, 1.0f))
            SharedFile.State.Pending -> SharedFileUi.State.Pending
            is SharedFile.State.Success -> SharedFileUi.State.Success(ImmutableUri(this.uri))
        }
    }
}