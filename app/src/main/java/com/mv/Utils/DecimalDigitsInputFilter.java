package com.mv.Utils;

import android.text.InputFilter;
import android.text.Spanned;

public class DecimalDigitsInputFilter implements InputFilter {

    private int maxDigitsBeforeDecimalPoint;
    private int maxDigitsAfterDecimalPoint;

    public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
        maxDigitsBeforeDecimalPoint = digitsBeforeZero;
        maxDigitsAfterDecimalPoint = digitsAfterZero;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        StringBuilder builder = new StringBuilder(dest);
        builder.replace(dstart, dend, source.subSequence(start, end).toString());

        if (!builder.toString().matches(
                "(([1-9]{1})([0-9]{0," + (maxDigitsBeforeDecimalPoint - 1) + "})?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?")) {

            if (source.length() == 0) {
                return dest.subSequence(dstart, dend);
            }
            return "";
        }

        return null;
    }
}