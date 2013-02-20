package com.android.sot.twitter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.view.Window;


public class TwitterApp {
    private Twitter mTwitter;
    private PreferenceUtils mSession;
    private CommonsHttpOAuthConsumer mHttpOauthConsumer;
    private OAuthProvider mHttpOauthprovider;
    private AccessToken mAccessToken;
    private String mConsumerKey;
    private String mSecretKey;
    private ProgressDialog mProgressDlg;
    private TwDialogListener mListener;
    private Activity context;


    public static final String  OAUTH_CALLBACK_SCHEME   = "x-oauthflow-twitter";
    public static final String  OAUTH_CALLBACK_HOST     = "callback";
    public static final String  CALLBACK_URL      = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
    private static final String TWITTER_ACCESS_TOKEN_URL = "http://api.twitter.com/oauth/access_token";
    private static final String TWITTER_AUTHORZE_URL = "http://api.twitter.com/oauth/authorize";
    private static final String TWITTER_REQUEST_URL = "http://api.twitter.com/oauth/request_token";
    
    public TwitterApp(Activity context, String consumerKey, String secretKey) 
    {
    	this.context = context;
    	
        mTwitter = new TwitterFactory().getInstance();
        mSession = new PreferenceUtils(context);
        mProgressDlg = new ProgressDialog(context);

        mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mConsumerKey = consumerKey;
        mSecretKey = secretKey;

        mHttpOauthConsumer = new CommonsHttpOAuthConsumer(mConsumerKey, mSecretKey);

        String request_url=TWITTER_REQUEST_URL;
        String access_token_url=TWITTER_ACCESS_TOKEN_URL;
        String authorize_url=TWITTER_AUTHORZE_URL;

        mHttpOauthprovider = new DefaultOAuthProvider(request_url, access_token_url, authorize_url);
        mAccessToken = mSession.getAccessToken();

        configureToken(); 
    }
    
    public void setListener(TwDialogListener listener) {
        mListener = listener;
    }

    private void configureToken() {
        if (mAccessToken != null)
        {
            mTwitter.setOAuthConsumer(mConsumerKey, mSecretKey);
            mTwitter.setOAuthAccessToken(mAccessToken);
        }
    }

    public boolean hasAccessToken() {
        return (mAccessToken == null) ? false : true;
    }

    public void resetAccessToken() {
        if (mAccessToken != null) {
            mSession.resetAccessToken();

            mAccessToken = null;
        }
    }

    public String getUsername() {
        return mSession.getUsername();
    }
    
    public long getUserId() {
        return mSession.getUserId();
    }
    
    public String getUserScreenName() {
        return mSession.getUserScreenName();
    }
    
    public String getToken() {
        return mSession.getToken();
    }
    
    public String getTokenSecret() {
        return mSession.getTokenSecret();
    }
    
    public Status updateStatus(String status) throws Exception {
        try {
            return mTwitter.updateStatus(status);
        } catch (TwitterException e) {
            throw e;
        }
    }
    
    public Status updateStatus(StatusUpdate status) throws Exception {
        try {
        	return mTwitter.updateStatus(status);
        } catch (TwitterException e) {
            throw e;
        }
    }
    
    public void authorize() {
        mProgressDlg.setMessage("Loading...");
        mProgressDlg.show();

        new Thread() {
            @Override
            public void run() {
                String authUrl = "";
                int what = 1;

                try {
                    authUrl = mHttpOauthprovider.retrieveRequestToken(
                            mHttpOauthConsumer, CALLBACK_URL);
                    what = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.sendMessage(mHandler
                        .obtainMessage(what, 1, 0, authUrl));
            }
        }.start();
    }

    public void processToken(String callbackUrl) {
        mProgressDlg.setMessage("Finalizing ...");
        mProgressDlg.show();

        final String verifier = getVerifier(callbackUrl);

        new Thread() {
            @Override
            public void run() {
                int what = 1;

                try {
                    mHttpOauthprovider.retrieveAccessToken(mHttpOauthConsumer,
                            verifier);

                    mAccessToken = new AccessToken(
                            mHttpOauthConsumer.getToken(),
                            mHttpOauthConsumer.getTokenSecret());

                    configureToken();

                    User user = mTwitter.verifyCredentials();
                    
                    mSession.storeAccessToken(mAccessToken, user.getName());

                    mSession.storeUserCredentials(user.getId(), user.getScreenName());
                    
                    what = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
            }
        }.start();
    }

    private String getVerifier(String callbackUrl) {
        String verifier = "";

        try {
            callbackUrl = callbackUrl.replace(OAUTH_CALLBACK_SCHEME, "http");

            URL url = new URL(callbackUrl);
            String query = url.getQuery();

            String array[] = query.split("&");

            for (String parameter : array) {
                String v[] = parameter.split("=");

                if (URLDecoder.decode(v[0]).equals(
                        oauth.signpost.OAuth.OAUTH_VERIFIER)) {
                    verifier = URLDecoder.decode(v[1]);
                    break;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return verifier;
    }

    private void showLoginDialog(String url) {
        final TwDialogListener listener = new TwDialogListener() {

            public void onComplete(String value) {
                processToken(value);
            }

            public void onError(String value) {
                mListener.onError("Failed opening authorization page");
            }
        };

        new TwitterDialog(context, url, listener).show();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mProgressDlg.dismiss();

            if (msg.what == 1) {
                if (msg.arg1 == 1)
                    mListener.onError("Error getting request token");
                else
                    mListener.onError("Error getting access token");
            } else {
                if (msg.arg1 == 1)
                    showLoginDialog((String) msg.obj);
                else
                    mListener.onComplete("");
            }
        }
    };

    public interface TwDialogListener {
        public void onComplete(String value);

        public void onError(String value);
    }
}