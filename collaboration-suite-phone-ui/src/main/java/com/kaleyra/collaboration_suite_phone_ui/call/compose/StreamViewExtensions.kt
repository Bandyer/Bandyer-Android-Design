package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.graphics.Matrix
import androidx.compose.ui.unit.IntSize
import com.kaleyra.collaboration_suite.phonebox.StreamView
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object StreamViewExtensions {

    fun VideoStreamView.getSize(): Flow<IntSize> = videoSize.map {
        IntSize(it.width, it.height)
    }

    fun VideoStreamView.getTranslation(): Flow<FloatArray> =
        this.getTransformationMatrix().map { matrix ->
            if (!matrix.isAffine) floatArrayOf(0f, 0f)
            else {
                val values = FloatArray(9)
                matrix.getValues(values)
                val tx = values[Matrix.MTRANS_X]
                val ty = values[Matrix.MTRANS_Y]
                floatArrayOf(tx, ty)
            }
        }

    fun VideoStreamView.getScale(): Flow<FloatArray> =
        this.getTransformationMatrix().map { matrix ->
            if (!matrix.isAffine) floatArrayOf(0f, 0f)
            else {
                val values = FloatArray(9)
                matrix.getValues(values)
                val tx = values[Matrix.MSCALE_X]
                val ty = values[Matrix.MSCALE_Y]
                floatArrayOf(tx, ty)
            }
        }

    private fun VideoStreamView.getTransformationMatrix(): Flow<Matrix> {
        return this.state
            .filterIsInstance<StreamView.State.Rendering>()
            .flatMapLatest { it.matrix }
    }
}