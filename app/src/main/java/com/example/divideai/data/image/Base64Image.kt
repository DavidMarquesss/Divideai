package com.example.divideai.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream

/**
 * Helpers para codificar imagens escolhidas pelo usuário em uma string Base64
 * compacta — o suficiente para caber em um documento do Firestore (1MB).
 *
 * Como não usamos Firebase Storage (exige plano Blaze), salvamos a própria
 * imagem inline. Para manter o tamanho baixo, reduzimos para um lado máximo
 * configurável e comprimimos como JPEG com qualidade moderada.
 */
object Base64Image {

    /** Avatar redondinho: ~10–20 KB depois de codificado. */
    const val SIZE_AVATAR = 240
    /** Comprovante de despesa: mais detalhes, ainda assim leve. */
    const val SIZE_RECEIPT = 720
    /** Qualidade JPEG padrão — bom equilíbrio entre tamanho e nitidez. */
    const val QUALITY_DEFAULT = 70

    /**
     * Lê a imagem em [uri], reduz para caber em um quadrado [maxSize]x[maxSize],
     * respeita a orientação EXIF e devolve a representação Base64 (sem prefixo
     * `data:image/...`).  Retorna `null` se não conseguir abrir/decodificar.
     */
    fun encodeFromUri(
        context: Context,
        uri: Uri,
        maxSize: Int = SIZE_AVATAR,
        quality: Int = QUALITY_DEFAULT
    ): String? {
        return try {
            val scaled = decodeScaled(context, uri, maxSize)
            if (scaled == null) {
                Log.w(TAG, "decodeScaled returned null for $uri")
                return null
            }
            val rotated = applyExifOrientation(context, uri, scaled)
            val baos = ByteArrayOutputStream()
            rotated.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            if (rotated !== scaled) scaled.recycle()
            rotated.recycle()
            Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "encodeFromUri failed for $uri", e)
            null
        }
    }

    private const val TAG = "Base64Image"

    /** Decodifica uma string Base64 (gerada por [encodeFromUri]) em [Bitmap]. */
    fun decode(base64: String?): Bitmap? {
        if (base64.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun decodeScaled(context: Context, uri: Uri, maxSize: Int): Bitmap? {
        // Bufferiza os bytes uma única vez — alguns URIs do Photo Picker
        // não permitem reabrir o stream várias vezes.
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: run {
                Log.w(TAG, "openInputStream returned null for $uri")
                return null
            }
        Log.d(TAG, "Loaded ${bytes.size} bytes from $uri")

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            Log.w(TAG, "Could not decode bounds (w=${bounds.outWidth} h=${bounds.outHeight}) mime=${bounds.outMimeType}")
            return null
        }

        var sample = 1
        while (bounds.outWidth / sample > maxSize || bounds.outHeight / sample > maxSize) {
            sample *= 2
        }

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: run {
            Log.w(TAG, "decodeByteArray returned null (sample=$sample)")
            return null
        }

        val longer = maxOf(raw.width, raw.height)
        if (longer <= maxSize) return raw
        val scale = maxSize.toFloat() / longer
        val w = (raw.width * scale).toInt().coerceAtLeast(1)
        val h = (raw.height * scale).toInt().coerceAtLeast(1)
        val resized = Bitmap.createScaledBitmap(raw, w, h, true)
        if (resized !== raw) raw.recycle()
        return resized
    }

    private fun applyExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val matrix = android.graphics.Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
