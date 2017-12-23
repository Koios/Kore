package org.xbmc.kore.ui.widgets;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.HostConnection;
import org.xbmc.kore.jsonrpc.method.Favourites;


public class FavouriteButtonHolder {
    final private StarButton starButton;
    final private String title;
    final private String path;
    final private HostConnection connection;
    final private Handler handler;
    final private Context context;

    public FavouriteButtonHolder(StarButton starButton, String title, String path, boolean isFavourite,
                                 HostConnection connection, Handler handler,
                                 Context context) {
        this.starButton = starButton;
        this.title = title;
        this.path = path;

        this.connection = connection;
        this.handler = handler;
        this.context = context;

        starButton.setFilled(isFavourite);
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavouriteButtonHolder.this.onClick();
            }
        });
    }

    /// Update favourite state after outside change.
    /// Does not request a state change from kodi.
    public void update(boolean isFavourite) {
        starButton.setFilled(isFavourite);
    }

    public void requestChange(final boolean shallBeFavourite, HostConnection connection,
                              Handler callbackHandler, final Context context) {
        ApiCallback<Boolean> callback = new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean stateChanged) {
                starButton.setFilled(shallBeFavourite);
            }

            @Override
            public void onError(int errorCode, String description) {
                // TODO: make translatable
                Toast.makeText(context, "Failed to change favourite state: " + description,
                        Toast.LENGTH_LONG).show();
            }
        };

        new Favourites.Set(title, path, shallBeFavourite)
                .execute(connection, callback, callbackHandler);
    }

    private void onClick() {
        boolean shallBeFavourite = ! starButton.getFilled();
        requestChange(shallBeFavourite, connection, handler, context);
    }
}
