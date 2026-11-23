package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImagePickerHelper {
  private const val MAX_DIM = 480
  private const val MAX_BASE64_CHARS = 300_000

  suspend fun encodeUriToBase64(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
      try {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream, null, boundsOpts)
        }
        val srcW = boundsOpts.outWidth
        val srcH = boundsOpts.outHeight
        if (srcW <= 0 || srcH <= 0) return@withContext null

        var sampleSize = 1
        while ((srcW / sampleSize) > MAX_DIM * 2 || (srcH / sampleSize) > MAX_DIM * 2) {
          sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream, null, decodeOpts)
        } ?: return@withContext null

        val scale = minOf(
          MAX_DIM.toFloat() / decoded.width.coerceAtLeast(1),
          MAX_DIM.toFloat() / decoded.height.coerceAtLeast(1),
          1f,
        )
        val scaled = if (scale < 1f) {
          Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true,
          ).also {
            if (it !== decoded) decoded.recycle()
          }
        } else {
          decoded
        }

        var quality = 78
        var encoded: String
        do {
          val out = ByteArrayOutputStream()
          scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
          encoded = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
          quality -= 12
        } while (encoded.length > MAX_BASE64_CHARS && quality >= 30)

        scaled.recycle()
        encoded.takeIf { it.length <= MAX_BASE64_CHARS }
      } catch (_: Exception) {
        null
      }
    }

  fun decodeBase64ToImageBitmap(base64: String?): ImageBitmap? {
    if (base64.isNullOrBlank()) return null
    return try {
      val bytes = Base64.decode(base64, Base64.DEFAULT)
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
      null
    }
  }
}
