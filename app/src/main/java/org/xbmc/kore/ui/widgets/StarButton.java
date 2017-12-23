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
        this(context, null);
    }

    public StarButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageButtonStyle);
    }

    public StarButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFilled(isFilled);
        // if the ImageButton is focusable (which is set in the super constructor), then it blocks
        // click events on PARENT elements like the item of a ListView
        setFocusable(false);
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
