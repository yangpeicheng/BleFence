package com.ypc.blefence;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.Set;

/**
 * Created by yangpc on 2017/11/15.
 */

public class ValidAddressPref {
    private static final String PREF_SEARCH_QUERY="searchQuery";

    public static Set<String> getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(PREF_SEARCH_QUERY,null);
    }

    public static void setStoredQury(Context context,Set<String> set){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet(PREF_SEARCH_QUERY,set)
                .apply();
    }
}
