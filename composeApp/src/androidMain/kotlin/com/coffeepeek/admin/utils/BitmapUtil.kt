package com.coffeepeek.admin.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import kotlin.io.encoding.Base64

object BitmapUtil {


    fun load(
        contentResolver: ContentResolver,
        uri: Uri,
        maxHeightWidth: Int = 1_280,
        maxByteSize: IntRange = 200_000..280_000
    ): Bitmap {
        val start = System.currentTimeMillis()
        val stream = contentResolver.openInputStream(uri) ?: throw FileNotFoundException()
        val bytes = stream.readBytes()

        val exif = ExifInterface(bytes.inputStream())
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            .changeSize(maxHeightWidth)
            .compress(maxByteSize)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun load(
        byteArray: ByteArray,
        maxHeightWidth: Int = 1_280,
        maxByteSize: IntRange = 200_000..280_000
    ): Bitmap {
        val exif = ExifInterface(byteArray.inputStream())
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            .changeSize(maxHeightWidth)
            .compress(maxByteSize)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    }


    fun Bitmap.changeSize(maxSize: Int): Bitmap {
        var width = width
        var height = height
        val ratio = width.toFloat() / height.toFloat()
        if (width > height) {
            width = maxSize
            height = (width / ratio).toInt()
        } else {
            height = maxSize
            width = (height * ratio).toInt()
        }
        return this.scale(width, height)
    }

    fun Bitmap.compressToSize(sizeRange: IntRange): ByteArray {
        var minq = 0
        var maxq = 100
        var stream = ByteArrayOutputStream()
        while (
            stream.size() > sizeRange.max()
            || stream.size() < sizeRange.min()
        ) {
            val q = (maxq + minq) / 2
            if (q < 2) break
            stream = ByteArrayOutputStream()
            this.compress(Bitmap.CompressFormat.JPEG, q, stream)
            if (stream.size() > sizeRange.max()) maxq = q
            else if (stream.size() < sizeRange.min()) minq = q
        }
        return stream.toByteArray()
    }

    fun Bitmap.compress(sizeRange: IntRange): Bitmap {
        val arr = compressToSize(sizeRange)
        return BitmapFactory.decodeByteArray(arr, 0, arr.size)
    }

    fun Bitmap.toByteArray(
        format: Bitmap.CompressFormat? = null,
        quality: Int = 100
    ): ByteArray {
        val type = when {
            format != null -> format
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Bitmap.CompressFormat.WEBP_LOSSLESS
            else -> Bitmap.CompressFormat.JPEG
        }

        val stream = ByteArrayOutputStream()
        this.compress(type, quality, stream)
        return stream.toByteArray()
    }

    fun ByteArray.toBase64(): String {
        return Base64.encode(this, 0, this.size)
    }


}