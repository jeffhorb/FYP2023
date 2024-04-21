package com.ecom.fyp2023.AppManagers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {

    private static final String PREF_NAME = "GitHubPrefs";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private static final String KEY_GROUP_ID = "groupId";
    private static final String KEY_USER_AUTH_ID = "userAuthId";

    private static final String KEY_GROUP_NAME = "groupName";
    private static final String KEY_GROUP_DESCRIPTION = "groupDescription";


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

    public void saveGroupId(String groupId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_GROUP_ID, groupId);
        editor.apply();
    }

    public String getGroupId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_GROUP_ID, null);
    }

    public void saveGroupName(String groupName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_GROUP_NAME, groupName);
        editor.apply();
    }

    public String getGroupName() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_GROUP_NAME, null);
    }

    public void saveGroupDescription (String groupDescription) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_GROUP_DESCRIPTION, groupDescription);
        editor.apply();
    }

    public String getGroupDescription() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_GROUP_DESCRIPTION, null);
    }


    public void saveUserAuthId(String userAuthId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_AUTH_ID, userAuthId);
        editor.apply();
    }

    public String getUserAuthId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_AUTH_ID, null);
    }

    public void clearSavedIds() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_GROUP_ID);
        editor.remove(KEY_USER_AUTH_ID);
        editor.remove(KEY_GROUP_NAME);
        editor.remove(KEY_GROUP_DESCRIPTION);
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


    public void saveNoteToSharedPreferencesForTask(String note, String taskId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Notes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Task-" + taskId, note);
        editor.apply();
    }

    public String getStoredNoteForTask(String taskId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Notes", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Task-" + taskId, "");
    }

    public void clearStoredNote() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Notes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("Note");
        editor.apply();
    }

    // Method to save text in RichEditor to SharedPreferences for a specific file
    public void saveRichEditorText(String fileDocId, String text) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RichEditorContent", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(fileDocId, text); // Use fileDocId as the key to differentiate files
        editor.apply();
    }

    // Method to retrieve text from SharedPreferences for a specific file
    public String getRichEditorText(String fileDocId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RichEditorContent", Context.MODE_PRIVATE);
        return sharedPreferences.getString(fileDocId, ""); // Use fileDocId as the key to retrieve specific file content
    }

    // Method to save client secret
    public void saveClientSecret(String clientSecret) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CLIENT_SECRET, clientSecret);
        editor.apply();
    }

    // Method to retrieve client secret
    public String getClientSecret() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_CLIENT_SECRET, null);
    }

    // Method to save access token
    public void saveAccessToken(String accessToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.apply();
    }

    // Method to retrieve access token
    public String getAccessToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    // Method to clear stored client secret, access token, and other data
    public void clearStoredData() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_CLIENT_SECRET);
        editor.remove(KEY_ACCESS_TOKEN);
        // Add more removals for other stored data if needed
        editor.apply();
    }

}
