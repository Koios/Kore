package org.xbmc.kore.ui.widgets;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.widget.Toast;

import org.xbmc.kore.R;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.method.Favourites;


/**
 * A button to add/remove items to the favourites
 * Using a star as an icon which is either filled or just showing an outline.
 */
public class FavouriteToggle extends AppCompatImageButton {

    static int starFilled = R.drawable.ic_star_white_24dp;
    static int starOutline = R.drawable.ic_star_unfilled;
    static float alphaFilled = 1.0f;
    static float alphaOutline = 0.2f;

    boolean isFavourite = false;

    public FavouriteToggle(Context context) {
        super(context);
        setFavouriteStatus(isFavourite);
    }

    // TODO: silly constructor duplication
    public FavouriteToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFavouriteStatus(isFavourite);
    }

    public FavouriteToggle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFavouriteStatus(isFavourite);
    }

    public void setFavouriteStatus(boolean isFavourite) {
        setAlpha(isFavourite ? alphaFilled : alphaOutline);
        setImageResource(isFavourite ? starFilled : starOutline);
        this.isFavourite = isFavourite;
    }

    public void toggleFavouriteStatus() {
        setFavouriteStatus(!isFavourite);
    }

    // TODO: split into pure UI button and button + action (with built-in click event handler)?
    public void kodiToggleFavourite(HostManager hostManager, Handler callbackHandler,
                                    String title, String path) {
        Favourites.Toggle t = new Favourites.Toggle(title, path);

        t.execute(hostManager.getConnection(), new ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                toggleFavouriteStatus();
            }

            @Override
            public void onError(int errorCode, String description) {
                Toast.makeText(getContext(), "Failed to change favourite state: " + description,
                        Toast.LENGTH_LONG).show();
            }
        }, callbackHandler);
    }
}
