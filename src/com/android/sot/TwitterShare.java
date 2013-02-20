package com.android.sot;

import twitter4j.StatusUpdate;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.android.sot.twitter.TwitterApp;
import com.android.sot.twitter.TwitterApp.TwDialogListener;

public class TwitterShare extends Activity 
{
	private static final String TWITTER_CONSUMER_KEY = "DXK4ODyu8qkPvilLLP03Mw";
	private static final String TWITTER_SECRET_KEY = "qms5OltiM1bZDw4vKPq5dP5Ioj9QId7Gfwmu7hkQ";
	
	private Handler mHandler;
	private TwitterApp mTwitter;
	private ProgressDialog progressdialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter_share);
		
		mHandler = new Handler();
		mTwitter = new TwitterApp(this, TWITTER_CONSUMER_KEY, TWITTER_SECRET_KEY);
		mTwitter.setListener(mTwLoginDialogListener);
		
		Button btnShare = (Button) findViewById(R.id.btnShare);
		btnShare.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				if (mTwitter.hasAccessToken()) 
					postMsgOnTwitter("Hi, This is from Android App");
				else 
					mTwitter.authorize();
			}
		});
	}
	
	private TwDialogListener mTwLoginDialogListener = new TwDialogListener() 
	{
		@Override
        public void onComplete(String value) 
        {
			postMsgOnTwitter("Hi, This is from Android App");
        }
		
		@Override
        public void onError(String value) 
        {
        	showToast("Twitter login failed");
            mTwitter.resetAccessToken();
        }
    };
    
    private void postMsgOnTwitter(final String msg)
    {
    	showProgressDialog("sending tweet..");
		
		new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				StatusUpdate status = new StatusUpdate(msg);
				
				try 
				{
					mTwitter.updateStatus(status);
					
					hideProgressDialog();
					
					showToast("Posted successfully.");
				} 
				catch (Exception e) 
				{
					hideProgressDialog();
					
					showToast("Failed to Tweet");
					e.printStackTrace();
				}
			}
		}).start();
	}
   
    public void showToast(final String msg) 
	{
		mHandler.post(new Runnable() 
		{
			@Override
			public void run() 
			{
				Toast.makeText(TwitterShare.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
    
    
    
    /** This will shows a progress dialog with loading text, this is useful to call when some other functionality is taking place. **/
	public void showProgressDialog(String msg)
	{
		runOnUiThread(new RunShowLoader(msg, false));
	}
	
	/** 
	 * Implementing Runnable for runOnUiThread(), This will show a progress dialog
	 */
	class RunShowLoader implements Runnable
	{
		private String strMsg;
		private boolean isCancalable;
		
		public RunShowLoader(String strMsg, boolean isCancalable) 
		{
			this.strMsg = strMsg;
			this.isCancalable = isCancalable;
		}
		
		@Override
		public void run() 
		{
			try
			{
				if(progressdialog == null ||(progressdialog != null && !progressdialog.isShowing()))
				{
					progressdialog = ProgressDialog.show(TwitterShare.this, "", strMsg);
					progressdialog.setCancelable(isCancalable);
				}
			}
			catch(Exception e)
			{
				progressdialog = null;
				e.printStackTrace();
			}
		}
	}
	
	/** For hiding progress dialog **/
	public void hideProgressDialog()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run() 
			{
				try
				{
					if(progressdialog != null && progressdialog.isShowing())
					{
						progressdialog.dismiss();
					}
					progressdialog = null;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
