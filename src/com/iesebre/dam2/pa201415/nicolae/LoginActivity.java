package com.iesebre.dam2.pa201415.nicolae;

import java.io.InputStream;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.iesebre.dam2.pa201415.nicolae.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener,ConnectionCallbacks,OnConnectionFailedListener {
	
	// Google client to interact with Google API
	private GoogleApiClient mGoogleApiClient;
	
	private static final int RC_SIGN_IN_GOOGLE = 0;
	
	private static final String TAG = "LoginActivity";
	
	private boolean mSignInClicked;
	
	// Profile pic image size in pixels
	private static final int PROFILE_PIC_SIZE = 400;

	private static final int REQUEST_CODE_GOOGLE_LOGIN = 10;
	
	private static final int SOCIAL_LOGIN_GOOGLE = 101;
	private static final int SOCIAL_LOGIN_FACEBOOk = 102;
	private static final int SOCIAL_LOGIN_TWITTER = 103;
	
	private boolean OnStartAlreadyConnected = false;
	
	private int socialLoginType=-1;
	
	/**
	 * A flag indicating that a PendingIntent is in progress and prevents us
	 * from starting further intents.
	 */
	private boolean mIntentInProgress;
	
	private ConnectionResult mConnectionResult;
	
	private Button btnGoogleSignIn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		
		btnGoogleSignIn = (Button) findViewById(R.id.btn_google_sign_in);
		
		// Button click listeners
		btnGoogleSignIn.setOnClickListener(this);
		
		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this).addApi(Plus.API)
		.addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		Log.d(TAG, "onActivityResult!");
		
		switch (requestCode) {
		case RC_SIGN_IN_GOOGLE:
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
			break;

		case REQUEST_CODE_GOOGLE_LOGIN:
			Log.d(TAG, "LOGOUT from google!");
			//LOGOUT from google
			if (responseCode == RESULT_OK) {
				Log.d(TAG, "LOGOUT from google OK. Signing Out from Gplus!");
				mGoogleApiClient.connect();
				
				this.OnStartAlreadyConnected = true;
				this.socialLoginType=SOCIAL_LOGIN_GOOGLE;
				
				signOutFromGplus();
			}
			break;
		default:
			break;
		}
		
		
	}
	
	protected void onStart() {
		Log.d(TAG, "onStart!");
		super.onStart();
		
		if (!this.OnStartAlreadyConnected) {
			//NOT ALREADY CONNECTED: CONNECT!
			Log.d(TAG, "NOT Already connected! Connecting to...");
			mGoogleApiClient.connect();
			Log.d(TAG, "...connected to Google API!");
		} else{
			Log.d(TAG, "Already connected!");
		}
		
	}

	protected void onStop() {
		Log.d(TAG, "onStop!");
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			Log.d(TAG, "onStop mGoogleApiClient isConnected. Disconnecting...!");
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Sign-in into google
	 * */
	private void signInWithGplus() {
		Log.d(TAG, "signInWithGplus!");
		if (!mGoogleApiClient.isConnecting()) {
			Log.d(TAG, "Error signing in!");
			mSignInClicked = true;
			resolveSignInError();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_google_sign_in:
			// Signin button clicked
			signInWithGplus();
			break;
			/*
		case R.id.btn_sign_out:
			// Signout button clicked
			signOutFromGplus();
			break;
		case R.id.btn_revoke_access:
			// Revoke access button clicked
			revokeGplusAccess();
			break;*/
		}
	}
	
	/**
	 * Sign-out from google
	 * */
	private void signOutFromGplus() {
		Log.d(TAG, "signOutFromGplus!");
		if (mGoogleApiClient.isConnected()) {
			Log.d(TAG, "signOutFromGplus is connected then disconnecting!");
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "onConnected!");
		// TODO Auto-generated method stub
		
		mSignInClicked = false;
		
		//Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
		Log.d(TAG, "User is connected!");
		
		this.socialLoginType=SOCIAL_LOGIN_GOOGLE;

		// Get user's information
		getProfileInformation();

		Intent i = new Intent(LoginActivity.this, MainActivity.class);
		startActivityForResult(i, REQUEST_CODE_GOOGLE_LOGIN);
		
		// Update the UI after signin
		//updateUI(true);
		
	}
	
	/**
	 * Fetching user's information name, email, profile pic
	 * */
	private void getProfileInformation() {
		try {
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
				Person currentPerson = Plus.PeopleApi
						.getCurrentPerson(mGoogleApiClient);
				String personName = currentPerson.getDisplayName();
				String personPhotoUrl = currentPerson.getImage().getUrl();
				String personGooglePlusProfile = currentPerson.getUrl();
				String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

				Log.d(TAG, "Name: " + personName + ", plusProfile: "
						+ personGooglePlusProfile + ", email: " + email
						+ ", Image: " + personPhotoUrl);

				//TODO: Save to shared preferences?
			

				// by default the profile url gives 50x50 px image only
				// we can replace the value with whatever dimension we want by
				// replacing sz=X
				personPhotoUrl = personPhotoUrl.substring(0,
						personPhotoUrl.length() - 2)
						+ PROFILE_PIC_SIZE;

				//new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);

			} else {
				Toast.makeText(getApplicationContext(),
						"Person information is null", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub
		mGoogleApiClient.connect();
		//updateUI(false);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}

		if (!mIntentInProgress) {
			// Store the ConnectionResult for later usage
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}
	}
	
	/**
	 * Method to resolve any signin errors
	 * */
	private void resolveSignInError() {
		if (mConnectionResult!=null){
			if (mConnectionResult.hasResolution()) {
				try {
					mIntentInProgress = true;
					mConnectionResult.startResolutionForResult(this, RC_SIGN_IN_GOOGLE);
				} catch (SendIntentException e) {
					mIntentInProgress = false;
					mGoogleApiClient.connect();
				}
			}
		}
	}
	
	/**
	 * Background Async task to load user profile picture from url
	 * */
	private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public LoadProfileImage(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}

}
