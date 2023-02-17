package com.kaleyra.collaboration_suite_core_ui

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.doesFileExists
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ContextExtensionsTest {

    @Test
    fun uriNotValid_doesFileExists() {
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
}