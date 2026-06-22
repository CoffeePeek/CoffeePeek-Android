package com.coffeepeek.admin.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.coffeepeek.R
import com.yandex.runtime.image.ImageProvider

internal object MapMarkerIcons {

    private var defaultProvider: ImageProvider? = null
    private var selectedProvider: ImageProvider? = null

    fun provider(context: Context, selected: Boolean): ImageProvider {
        val appContext = context.applicationContext
        return if (selected) {
            selectedProvider ?: ImageProvider.fromBitmap(createBitmap(appContext, true))
                .also { selectedProvider = it }
        } else {
            defaultProvider ?: ImageProvider.fromBitmap(createBitmap(appContext, false))
                .also { defaultProvider = it }
        }
    }

    fun anchor(): PointF = PointF(0.5f, 0.5f)

    private fun createBitmap(context: Context, selected: Boolean): Bitmap {
        val density = context.resources.displayMetrics.density
        val size = (44f * density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val center = size / 2f
        val radius = center - 2f * density
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (selected) 0xFF1C1917.toInt() else 0xFFFFFFFF.toInt()
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f * density
            color = if (selected) 0xFFEAB308.toInt() else 0xFFE7E5E4.toInt()
        }

        canvas.drawCircle(center, center, radius, fillPaint)
        canvas.drawCircle(center, center, radius, strokePaint)

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_map_cafe) ?: return bitmap
        val iconSize = (22f * density).toInt()
        val left = ((size - iconSize) / 2f).toInt()
        val top = ((size - iconSize) / 2f).toInt()
        icon.setBounds(left, top, left + iconSize, top + iconSize)
        DrawableCompat.setTint(
            icon,
            if (selected) 0xFFEAB308.toInt() else 0xFF1C1917.toInt(),
        )
        icon.draw(canvas)

        return bitmap
    }
}
