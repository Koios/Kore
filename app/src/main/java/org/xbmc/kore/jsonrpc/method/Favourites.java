/*
 * Copyright 2017 XBMC Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xbmc.kore.jsonrpc.method;

import android.os.Handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.ApiException;
import org.xbmc.kore.jsonrpc.ApiList;
import org.xbmc.kore.jsonrpc.ApiMethod;
import org.xbmc.kore.jsonrpc.HostConnection;
import org.xbmc.kore.jsonrpc.type.FavouriteType;
import org.xbmc.kore.jsonrpc.type.ListType;
import org.xbmc.kore.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All JSON RPC methods in Favourites.*
 */
public class Favourites {
    private static final String TAG = LogUtils.makeLogTag(Favourites.class);

    /**
     * Retrieves the Details of the Favourites.
     */
    public static class GetFavourites extends ApiMethod<ApiList<FavouriteType.DetailsFavourite>> {
        public static final String METHOD_NAME = "Favourites.GetFavourites";
        private static final String LIST_NODE = "favourites";

        /**
         * Default ctor, gets all the properties by default.
         */
        public GetFavourites() {
            addParameterToRequest("properties", new String[]{
                    FavouriteType.DetailsFavourite.WINDOW, FavouriteType.DetailsFavourite.WINDOW_PARAMETER,
                    FavouriteType.DetailsFavourite.THUMBNAIL, FavouriteType.DetailsFavourite.PATH
            });
        }

        @Override
        public String getMethodName() {
            return METHOD_NAME;
        }

        @Override
        public ApiList<FavouriteType.DetailsFavourite> resultFromJson(ObjectNode jsonObject) throws ApiException {
            ListType.LimitsReturned limits = new ListType.LimitsReturned(jsonObject);

            JsonNode resultNode = jsonObject.get(RESULT_NODE);
            ArrayNode items = resultNode.has(LIST_NODE) && !resultNode.get(LIST_NODE).isNull() ?
                    (ArrayNode) resultNode.get(LIST_NODE) : null;
            if (items == null) {
                return new ApiList<>(Collections.<FavouriteType.DetailsFavourite>emptyList(), limits);
            }
            ArrayList<FavouriteType.DetailsFavourite> result = new ArrayList<>(items.size());
            for (JsonNode item : items) {
                result.add(new FavouriteType.DetailsFavourite(item));
            }
            return new ApiList<>(result, limits);
        }
    }

    public static class Toggle extends ApiMethod<String> {
        // Although the API function name suggests that it only ADDS favourites, it actually toggles
        // their favourite state; i.e. if there already is a favourite with the same path, that
        // favourite is removed.
        public final static String METHOD_NAME = "Favourites.AddFavourite";

        public Toggle(String title, String path) {
            super();
            addParameterToRequest("type", "media");
            addParameterToRequest("title", title);
            addParameterToRequest("path", path);
        }

        @Override
        public String getMethodName() { return METHOD_NAME; }

        @Override
        public String resultFromJson(ObjectNode jsonObject) throws ApiException {
            return jsonObject.get(RESULT_NODE).textValue();
        }
    }

    private static boolean containsFavourite(final List<FavouriteType.DetailsFavourite> list,
                                             final String path) {
        // TODO: this is very inelegant
        for (FavouriteType.DetailsFavourite elem : list)
            if(elem.path.equals(path)) return true;
        return false;
    }

    /**
     * Convenience ApiMethod which calls GetFavourites, Toggle under the hood.
     *
     * Same usage as an ApiMethod<Boolean> extension; the boolean parameter is true IFF the item
     * was NOT a favourite just before we toggled it.
     */
    public static class Add {
        public Add(String title, String path) {
            set = new Set(title, path, true);
        }

        public void execute(final HostConnection hostConnection,
                            final ApiCallback<Boolean> callback,
                            final Handler handler) {
            set.execute(hostConnection, callback, handler);
        }

        private Set set;
    }

    /**
     * Convenience ApiMethod which calls GetFavourites, Toggle under the hood.
     *
     * Same usage as an ApiMethod<Boolean> extension; the boolean parameter is true IFF the 'favourite'
     * state was changed by this ApiMethod. False means that something external changed the state to
     * the desired state.
     */
    public static class Set {
        public Set(String title, String path, boolean shallBeFavourite) {
            this.title = title;
            this.path = path;
            this.shallBeFavourite = shallBeFavourite;
        }

        public void execute(final HostConnection hostConnection,
                            final ApiCallback<Boolean> callback,
                            final Handler handler) {
            GetFavourites getFavourites = new GetFavourites();
            getFavourites.execute(hostConnection, new ApiCallback<ApiList<FavouriteType.DetailsFavourite>>() {
                @Override
                public void onSuccess(ApiList<FavouriteType.DetailsFavourite> result) {
                    LogUtils.LOGD(TAG, String.format("Successfully retrieved favourites " +
                            "to set favourite state for: %1$s", path));

                    final boolean isCurrentlyFavourite = containsFavourite(result.items, path);
                    adjustStateIfNecessary(shallBeFavourite, isCurrentlyFavourite,
                            hostConnection, callback, handler);
                }

                @Override
                public void onError(int errorCode, String description) {
                    LogUtils.LOGE(TAG, String.format("Failed to get favourites " +
                            "while trying to change favourite state for: %1$s", path));
                    callback.onError(errorCode, description);
                }
            }, handler);
        }

        private void adjustStateIfNecessary(boolean shallBeFavourite, boolean isCurrentlyFavourite,
                                            HostConnection hostConnection, ApiCallback<Boolean> callback,
                                            Handler handler) {
            if(shallBeFavourite == isCurrentlyFavourite) {
                LogUtils.LOGI(TAG, String.format("Current favourite state '%1$b' " +
                        "is already equal to desired state for item: %1$s", shallBeFavourite, path));
                callback.onSuccess(false);
            }else {
                LogUtils.LOGI(TAG, String.format("Need to change favourite state " +
                                "from '%1$b' currently to '%1$b' desired for item: %1$s", path,
                        isCurrentlyFavourite, shallBeFavourite));

                doAdjustState(shallBeFavourite, hostConnection, callback, handler);
            }
        }

        private void doAdjustState(boolean shallBeFavourite, HostConnection hostConnection,
                                   final ApiCallback<Boolean> callback, Handler handler) {
            Toggle toggle = new Toggle(title, path);
            toggle.execute(hostConnection, new ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    LogUtils.LOGI(TAG, String.format("Successfully toggled favourite state for: %1$s", path));
                    callback.onSuccess(true);
                }

                @Override
                public void onError(int errorCode, String description) {
                    LogUtils.LOGE(TAG, String.format("Failed to toggle favourite state for: %1$s", path));
                    callback.onError(errorCode, description);
                }
            }, handler);
        }

        private final String title;
        private final String path;
        private final boolean shallBeFavourite;
    }
}
