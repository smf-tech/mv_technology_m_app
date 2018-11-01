package com.mv.Widgets;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class MyEditTextView extends EditText {

    private Context context;
    private AttributeSet attrs;
    private int defStyle;

    public MyEditTextView(Context context) {
        super(context);
        this.context = context;
      //  init();
    }

    public MyEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
     //   init();
    }

    public MyEditTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.attrs = attrs;
        this.defStyle = defStyle;
     //   init();
    }

    /*private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Shivaji.ttf");
        this.setTypeface(font);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Shivaji.ttf");
        super.setTypeface(tf, style);
    }

    @Override
    public void setTypeface(Typeface tf) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Shivaji.ttf");
        super.setTypeface(tf);
    }*/
}