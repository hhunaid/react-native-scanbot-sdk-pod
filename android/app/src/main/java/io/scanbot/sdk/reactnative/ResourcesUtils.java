/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.content.Context;
import android.content.res.Resources;

public final class ResourcesUtils {

    private ResourcesUtils() {}

    public static int getResId(final String defType, final String name, final Context context) {
        final int id = context.getResources().getIdentifier(name, defType, context.getPackageName());
        if (id == 0) {
            throw new Resources.NotFoundException("Resource not found: " + defType + "/" + name);
        }
        return id;
    }

}
