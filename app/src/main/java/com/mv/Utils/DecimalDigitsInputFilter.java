package com.mv.Utils;

import android.text.InputFilter;
import android.text.Spanned;

public class DecimalDigitsInputFilter implements InputFilter {

    private String pattern;

    public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero, int type) {
        switch (type) {
            case Constants.INPUT_DECIMAL_RANGE:
                pattern = "(([1-9]{1})([0-9]{0," + (digitsBeforeZero - 1) + "})?)?(\\.[0-9]{0," + digitsAfterZero + "})?";
                break;

            case Constants.INPUT_TEXT_LENGTH:
                pattern = "(([a-zA-Z 0-9]{0," + (digitsBeforeZero) + "})?)?";
                break;
        }
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        StringBuilder builder = new StringBuilder(dest);
        builder.replace(dstart, dend, source.subSequence(start, end).toString());

        if (!builder.toString().matches(pattern)) {
            if (source.length() == 0) {
                return dest.subSequence(dstart, dend);
            }
            return "";
        }

        return null;
    }
}