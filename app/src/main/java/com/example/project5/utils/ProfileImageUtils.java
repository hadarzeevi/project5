package com.example.project5.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.Random;

public class ProfileImageUtils {

    public static Bitmap generateProfileBitmap(String letter) {
        int size = 200;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // צבע רקע רנדומלי
        int[] colors = {
                Color.parseColor("#f59e0b"), // כתום
                Color.parseColor("#10b981"), // ירוק
                Color.parseColor("#3b82f6"), // כחול
                Color.parseColor("#ec4899"), // ורוד
                Color.parseColor("#8b5cf6")  // סגול
        };
        int bgColor = colors[new Random().nextInt(colors.length)];
        canvas.drawColor(bgColor);

        // ציור האות
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float x = size / 2;
        float y = size / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2;
        canvas.drawText(letter.toUpperCase(), x, y, paint);

        return bitmap;
    }
}
