package com.sujalamsufalam.decorators;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.sujalamsufalam.R;

import java.util.Collection;
import java.util.HashSet;

/**
 * Decorate several days with a dot
 */
public class EventDecorator implements DayViewDecorator {

    private final Drawable mDrawable;
    private HashSet<CalendarDay> dates;

    public EventDecorator(Context context, Collection<CalendarDay> dates, Drawable drawable) {
        if (drawable == null)
            mDrawable = context.getResources().getDrawable(R.drawable.circle_background);
        else
            mDrawable = drawable;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(mDrawable);
    }
}
