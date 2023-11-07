package com.kaleyra.video_common_ui

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.doesFileExists
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ContextExtensionsTest {

    // TODO do these tests make sense?
    @Test
    fun validUri_doesFileExists_true() {
        val contextMock = mockk<Context>(relaxed = true)
        val contentResolverMock = mockk<ContentResolver>(relaxed = true)
        val matrixCursor = mockk<MatrixCursor>(relaxed = true)
        val uriMock = mockk<Uri>()
        every { contextMock.contentResolver } returns contentResolverMock
        every { contentResolverMock.query(uriMock, null, null, null, null) } returns matrixCursor
        every { matrixCursor.moveToFirst() } returns true
        val result = contextMock.doesFileExists(uriMock)
        assertEquals(true, result)
    }

    @Test
    fun notValidUri_doesFileExists_false() {
        val contextMock = mockk<Context>(relaxed = true)
        val contentResolverMock = mockk<ContentResolver>(relaxed = true)
        val matrixCursor = mockk<MatrixCursor>(relaxed = true)
        val uriMock = mockk<Uri>()
        every { contextMock.contentResolver } returns contentResolverMock
        every { contentResolverMock.query(uriMock, null, null, null, null) } returns matrixCursor
        every { matrixCursor.moveToFirst() } returns false
        val result = contextMock.doesFileExists(uriMock)
        assertEquals(false, result)
    }

}