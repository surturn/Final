package com.sydney.sydney.data.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.sydney.sydney.data.repo.AppException
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Shrinks a picked photo before upload. Phone photos are often 5 to 10 MB, which both breaks the
 * 5 MB storage rule and wastes mobile data for everyone who later loads the image. The result is a
 * JPEG with its longest edge at most [MAX_EDGE] pixels and the camera rotation applied.
 */
class ImageProcessor(private val context: Context) {

    suspend fun compress(uriString: String): ByteArray = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        // A bounds-only decode returns null by design and fills [bounds] instead, so the null check
        // is on the stream, not on the decode result.
        resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
            true
        } ?: throw AppException("Could not read the selected image")
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw AppException("The selected file is not a valid image")
        }

        // Decode at a power-of-two fraction first so a 12 MP photo never sits in memory at full size.
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_EDGE) sample *= 2
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
            ?: throw AppException("Could not read the selected image")

        val rotation = try {
            resolver.openInputStream(uri)?.use { ExifInterface(it).rotationDegrees } ?: 0
        } catch (e: Exception) {
            0
        }

        val longest = maxOf(decoded.width, decoded.height)
        val scale = if (longest > MAX_EDGE) MAX_EDGE.toFloat() / longest else 1f
        val result = if (rotation == 0 && scale == 1f) {
            decoded
        } else {
            val matrix = Matrix().apply {
                postRotate(rotation.toFloat())
                postScale(scale, scale)
            }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        }

        val out = ByteArrayOutputStream()
        result.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        if (result !== decoded) result.recycle()
        decoded.recycle()
        out.toByteArray()
    }

    private companion object {
        const val MAX_EDGE = 1280
        const val JPEG_QUALITY = 85
    }
}
