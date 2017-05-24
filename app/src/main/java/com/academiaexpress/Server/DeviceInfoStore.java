package com.academiaexpress.Server;

import android.content.Context;
import android.content.SharedPreferences;

import com.academiaexpress.Data.User;

@SuppressWarnings("unused")
public class DeviceInfoStore {
    public static void saveUser(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        prefs.edit().putString(ServerConfig.INSTANCE.getUserEntity(), user.asString()).apply();
    }

    public static void resetUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        prefs.edit().putString(ServerConfig.INSTANCE.getUserEntity(), "null").apply();
    }

    public static String getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        return prefs.getString(ServerConfig.INSTANCE.getUserEntity(), "null");
    }

    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        prefs.edit().putString(ServerConfig.INSTANCE.getTokenEntity(), token).apply();
    }

    public static void resetToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        prefs.edit().putString(ServerConfig.INSTANCE.getTokenEntity(), "null").apply();
    }

    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        return prefs.getString(ServerConfig.INSTANCE.getTokenEntity(), "null");
    }
}
