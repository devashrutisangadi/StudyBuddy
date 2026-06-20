package com.example.studybuddy.ui.home;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Draws a single-path "folder" silhouette: a small tab on the top-left
 * blending into a rounded rectangle body, like a physical file folder.
 * Proportions are a direct port of the approved reference path, originally
 * authored as: M10,22 L10,18 Q10,14 14,14 L34,14 Q37,14 38,17 L40,22
 *              L86,22 Q90,22 90,26 L90,66 Q90,70 86,70 L14,70 Q10,70 10,66 Z
 * in a 100x80 viewBox. All points below are that same path, scaled by
 * (w/100, h/80) so it reproduces exactly regardless of the view's size.
 */
public class FolderShapeDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public FolderShapeDrawable(int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setFillColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(@NonNull android.graphics.Rect bounds) {
        super.onBoundsChange(bounds);
        buildPath(bounds.width(), bounds.height());
    }

    private void buildPath(float w, float h) {
        path.reset();

        // The original reference path's own bounding box is x:[10,90], y:[14,70]
        // in its 100x80 viewBox. We map that box onto the FULL view bounds
        // (0,0)-(w,h) so the shape always fills the view with no leftover
        // empty margin, keeping text padding aligned to the visible shape.
        float srcMinX = 10f, srcMaxX = 90f;
        float srcMinY = 14f, srcMaxY = 70f;

        float sx = w / (srcMaxX - srcMinX);
        float sy = h / (srcMaxY - srcMinY);

        // Re-map each original point: (origPoint - srcMin) * scale
        path.moveTo((10 - srcMinX) * sx, (22 - srcMinY) * sy);
        path.lineTo((10 - srcMinX) * sx, (18 - srcMinY) * sy);
        path.quadTo((10 - srcMinX) * sx, (14 - srcMinY) * sy, (14 - srcMinX) * sx, (14 - srcMinY) * sy);
        path.lineTo((34 - srcMinX) * sx, (14 - srcMinY) * sy);
        path.quadTo((37 - srcMinX) * sx, (14 - srcMinY) * sy, (38 - srcMinX) * sx, (17 - srcMinY) * sy);
        path.lineTo((40 - srcMinX) * sx, (22 - srcMinY) * sy);
        path.lineTo((86 - srcMinX) * sx, (22 - srcMinY) * sy);
        path.quadTo((90 - srcMinX) * sx, (22 - srcMinY) * sy, (90 - srcMinX) * sx, (26 - srcMinY) * sy);
        path.lineTo((90 - srcMinX) * sx, (66 - srcMinY) * sy);
        path.quadTo((90 - srcMinX) * sx, (70 - srcMinY) * sy, (86 - srcMinX) * sx, (70 - srcMinY) * sy);
        path.lineTo((14 - srcMinX) * sx, (70 - srcMinY) * sy);
        path.quadTo((10 - srcMinX) * sx, (70 - srcMinY) * sy, (10 - srcMinX) * sx, (66 - srcMinY) * sy);
        path.close();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}