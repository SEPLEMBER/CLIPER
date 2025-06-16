package com.nestsecure.zeroless;

import android.text.method.TransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;

public class PartialMaskTransformation implements TransformationMethod {
    private static final int VISIBLE_CHARS = 5;

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        if (source.length() <= VISIBLE_CHARS) {
            return source;
        }

        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            if (i < VISIBLE_CHARS) {
                masked.append(source.charAt(i));
            } else {
                masked.append('*');
            }
        }
        return masked;
    }

    @Override
    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, android.graphics.Rect previouslyFocusedRect) {
        // No action needed
    }
}
