package org.xbmc.kore.ui.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import org.xbmc.kore.R;


/**
 * A button which toggles between a picture of a filled and an outlined star.
 */
public class StarButton extends AppCompatImageButton {

    static int starFilled = R.drawable.ic_star_white_24dp;
    static int starOutline = R.drawable.ic_star_unfilled;
    static float alphaFilled = 1.0f;
    static float alphaOutline = 0.4f;

    boolean isFilled = false;

    public StarButton(Context context) {
        super(context);
        setFilled(isFilled);
    }

    // TODO: silly constructor duplication
    public StarButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFilled(isFilled);
    }

    public StarButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFilled(isFilled);
    }

    public void setFilled(boolean isFilled) {
        setAlpha(isFilled ? alphaFilled : alphaOutline);
        setImageResource(isFilled ? starFilled : starOutline);
        this.isFilled = isFilled;
    }

    public boolean getFilled() {
        return isFilled;
    }
}
