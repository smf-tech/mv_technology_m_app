package com.mv.Widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

/**
 * @author Dattatray Nande
 */
public class MyFontButton extends AppCompatButton {

	private static final String TAG = "TextView";

	private Typeface typeface;

	public MyFontButton(Context context) {
		super(context);
	}

	public MyFontButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		//setCustomFont(context, attrs);
	}

	public MyFontButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//setCustomFont(context, attrs);
	}

	/*private void setCustomFont(Context ctx, AttributeSet attrs) {
		TypedArray styledAttrs;
		styledAttrs = ctx.obtainStyledAttributes(attrs, R.styleable.TypefacedTextView);
		String fontName = styledAttrs.getString(R.styleable.TypefacedTextView_typeface);
		//setCustomFont(ctx, fontName);
		styledAttrs.recycle();
	}*/

	/*private boolean setCustomFont(Context ctx, String asset) {
		try {
			if (typeface == null) {
				Log.i(TAG, "asset:: " + "fonts/" + asset);
				typeface = Typeface.createFromAsset(ctx.getAssets(),
						"fonts/Shivaji.ttf");
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Could not get typeface: " + e.getMessage());
			return false;
		}

		setTypeface(typeface);
		return true;
	}*/

}