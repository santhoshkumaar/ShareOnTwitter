package com.android.sot.twitter;

import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceUtils 
{
	private SharedPreferences sharedPref;
    private Editor editor;

    private static final String TWEET_AUTH_KEY = "auth_key";
    private static final String TWEET_AUTH_SECRET_KEY = "auth_secret_key";
    private static final String TWEET_USER_NAME = "user_name";
    private static final String TWEET_USER_ID = "user_id";
    private static final String TWEET_USER_SCREEN_NAME = "user_screen_name";
    
    private static final String SHARED = "SOT_Preferences";

    public PreferenceUtils(Context context)
    {
        sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);

        editor = sharedPref.edit();
    }

    public void storeAccessToken(AccessToken accessToken, String username) 
    {
        editor.putString(TWEET_AUTH_KEY, accessToken.getToken());
        editor.putString(TWEET_AUTH_SECRET_KEY, accessToken.getTokenSecret());
        editor.putString(TWEET_USER_NAME, username);

        editor.commit();
    }
    
    public void storeUserCredentials(long userId, String userScreenName)
    {
    	editor.putLong(TWEET_USER_ID, userId);
    	editor.putString(TWEET_USER_SCREEN_NAME, userScreenName);
    
    	editor.commit();
    }

    public void resetAccessToken() 
    {
        editor.putString(TWEET_AUTH_KEY, null);
        editor.putString(TWEET_AUTH_SECRET_KEY, null);
        editor.putString(TWEET_USER_NAME, null);

        editor.commit();
    }

    public String getUsername() 
    {
        return sharedPref.getString(TWEET_USER_NAME, "");
    }
    
    public long getUserId() 
    {
        return sharedPref.getLong(TWEET_USER_ID, 0);
    }
    
    public String getUserScreenName() 
    {
        return sharedPref.getString(TWEET_USER_SCREEN_NAME, "");
    }
    
    public String getToken() 
    {
        return sharedPref.getString(TWEET_AUTH_KEY, "");
    }
    
    public String getTokenSecret() 
    {
        return sharedPref.getString(TWEET_AUTH_SECRET_KEY, "");
    }

    public AccessToken getAccessToken() 
    {
        String token = sharedPref.getString(TWEET_AUTH_KEY, null);
        String tokenSecret = sharedPref.getString(TWEET_AUTH_SECRET_KEY, null);

        if (token != null && tokenSecret != null)
            return new AccessToken(token, tokenSecret);
        else
            return null;
    }
    
	public void clearCredentials() 
	{
		editor.remove(TWEET_AUTH_KEY);
		editor.remove(TWEET_AUTH_SECRET_KEY);
		editor.remove(TWEET_USER_NAME);
		editor.remove(TWEET_USER_ID);
		editor.remove(TWEET_USER_SCREEN_NAME);
		editor.commit();
	}
}
