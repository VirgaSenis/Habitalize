package com.iunus.habitualize;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by Iunus on 17/01/2017.
 * From http://stackoverflow.com/questions/14212518/is-there-a-way-to-define-a-min-and-max-value-for-edittext-in-android
 */

public class InputFilterMinMax implements InputFilter {
    private int min, max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private boolean isInRange(int input) {
        return (input <= this.max) && (input >= this.min);
    }

}
