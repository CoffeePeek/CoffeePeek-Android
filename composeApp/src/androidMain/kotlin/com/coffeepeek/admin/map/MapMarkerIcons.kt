package com.coffeepeek.admin.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import androidx.core.content.ContextCompat
import com.coffeepeek.R
import com.coffeepeek.domain.model.CoffeeShopType
import com.yandex.runtime.image.ImageProvider

internal object MapMarkerIcons {

    private const val PIN_W = 34f * 1.1f
    private const val PIN_H = 44f * 1.1f

    private val cache = mutableMapOf<String, ImageProvider>()

    fun provider(context: Context, type: String, selected: Boolean): ImageProvider {
        val key = "$type-$selected"
        return cache.getOrPut(key) {
            ImageProvider.fromBitmap(createBitmap(context.applicationContext, type, selected))
        }
    }

    fun anchor(): PointF {
        val pad = 3f
        return PointF(
            (pad + PIN_W / 2f) / (PIN_W + pad * 2f),
            (pad + PIN_H - 2f) / (PIN_H + pad * 2f),
        )
    }

    private fun createBitmap(context: Context, type: String, selected: Boolean): Bitmap {
        val density = context.resources.displayMetrics.density
        val pad = 3f * density
        val pinW = PIN_W * density
        val pinH = PIN_H * density
        val width = (pinW + pad * 2f).toInt().coerceAtLeast(1)
        val height = (pinH + pad * 2f).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val path = pinPath(pinW, pinH).apply { offset(pad, pad) }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = pinColor(type, selected)
            setShadowLayer(2.4f * density, 0f, 1.2f * density, 0x511A1412)
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeWidth = (if (selected) 1.5f else 1.15f) * density
            color = pinStrokeColor(selected)
        }

        canvas.drawPath(path, fillPaint)

        ContextCompat.getDrawable(context, mascotRes(type))?.let { mascot ->
            canvas.save()
            canvas.clipPath(path)
            val sx = pinW / 40f
            val sy = pinH / 52f
            val left = (pad - 2f * sx).toInt()
            val top = (pad - 5f * sy).toInt()
            mascot.setBounds(left, top, (left + 44f * sx).toInt(), (top + 48f * sy).toInt())
            mascot.draw(canvas)
            canvas.restore()
        }

        canvas.drawPath(path, strokePaint)
        return bitmap
    }

    private fun pinPath(width: Float, height: Float): Path {
        val sx = width / 40f
        val sy = height / 52f
        return Path().apply {
            moveTo(20f * sx, 1.6f * sy)
            cubicTo(29.2f * sx, 1.6f * sy, 36.8f * sx, 9.3f * sy, 36.8f * sx, 18.8f * sy)
            cubicTo(36.8f * sx, 29.8f * sy, 20f * sx, 50.4f * sy, 20f * sx, 50.4f * sy)
            cubicTo(20f * sx, 50.4f * sy, 3.2f * sx, 29.8f * sy, 3.2f * sx, 18.8f * sy)
            cubicTo(3.2f * sx, 9.3f * sy, 10.8f * sx, 1.6f * sy, 20f * sx, 1.6f * sy)
            close()
        }
    }

    private fun mascotRes(type: String): Int = when (type) {
        CoffeeShopType.SPECIALTY -> R.drawable.maskot_with_bean
        CoffeeShopType.CAFE -> R.drawable.maskot_with_dessert
        else -> R.drawable.maskot_with_cup
    }

    private fun pinColor(type: String, selected: Boolean): Int {
        if (selected) return 0xFFFEF3C7.toInt() // PrimaryLight — как подсветка в nav
        return when (type) {
            CoffeeShopType.SPECIALTY -> 0xFFF8F1DD.toInt() // GoldWarmSoft
            CoffeeShopType.CAFE -> 0xFFF3F4F6.toInt() // LightBadge — нейтральный серый
            else -> 0xFFFEF3C7.toInt() // PrimaryLight
        }
    }

    private fun pinStrokeColor(selected: Boolean): Int =
        if (selected) 0xFFCA8A04.toInt() else 0xFF78716C.toInt() // PrimaryDark / LightTextSecondary
}
