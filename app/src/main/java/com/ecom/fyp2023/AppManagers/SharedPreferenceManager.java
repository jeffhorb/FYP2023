package com.ecom.fyp2023.AppManagers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {


    Context context;

    public SharedPreferenceManager(Context context) {
            this.context = context;
    }
    public void saveLoginDetails(String email, String password) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Email", email);
        editor.putString("Password", password);
        editor.apply();
    }

    public boolean isUserLogedOut() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        boolean isEmailEmpty = sharedPreferences.getString("Email", "").isEmpty();
        boolean isPasswordEmpty = sharedPreferences.getString("Password", "").isEmpty();
        return isEmailEmpty || isPasswordEmpty;
    }
    public void clearSession() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.apply();
    }

    public void saveTaskProgress(String taskDetails, boolean isComplete) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TaskProgress", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(taskDetails, isComplete);
        editor.apply();
    }

    public boolean getTaskProgress(String taskDetails, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TaskProgress", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(taskDetails, defaultValue);
    }

}
