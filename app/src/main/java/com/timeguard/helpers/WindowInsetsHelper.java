package com.timeguard.helpers;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class WindowInsetsHelper {
    private WindowInsetsHelper() {
    }

    /**
     * Adds system bars (status/navigation) insets to the view's existing padding.
     * Useful for Android 15+ edge-to-edge enforcement (targetSdk 35) to avoid UI overlapping
     * the status bar and the gesture/navigation area.
     */
    public static void applySystemBarsPadding(View view) {
        if (view == null) return;

        final int initialLeft = view.getPaddingLeft();
        final int initialTop = view.getPaddingTop();
        final int initialRight = view.getPaddingRight();
        final int initialBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    initialLeft + bars.left,
                    initialTop + bars.top,
                    initialRight + bars.right,
                    initialBottom + bars.bottom
            );
            return insets;
        });

        ViewCompat.requestApplyInsets(view);
    }
}

