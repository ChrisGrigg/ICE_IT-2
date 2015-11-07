package com.iceit.framework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.iceit.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    public static final String PROFILE_FILE = "ProfileFile";

    private CallbackManager callbackManager;
    private TextView info;
    private LoginButton loginButton;
    private String fullName;
    private SharedPreferences.Editor editor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

		View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        info = (TextView)rootView.findViewById(R.id.info);

        initDB();
        getDBData();

        loginButton = (LoginButton)rootView.findViewById(R.id.login_button);
        loginButton.setFragment(this);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
//                info.setText("User ID: " + loginResult.getAccessToken().getUserId());
                /* make the API call */
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                /* handle the result */
                                try {
                                    JSONObject jObj = response.getJSONObject();
                                    fullName = jObj.getString("name");

//                                    info.setText("Name: " + fullName);
                                    saveToDB(fullName);

                                } catch (JSONException e) {
                                    // Do something with the exception
                                    info.setText("Error: " + e.getMessage());
                                }
                            }
                        }
                ).executeAsync();
            }

            @Override
            public void onCancel() {
                info.setText("Login attempt cancelled.");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt failed.");
            }
        });

        return rootView;
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void saveToDB(String data) {
        editor.putString("fullName", fullName);

        // Commit the edits!
        editor.commit();
        getDBData();
    }

    private void initDB() {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getActivity().getSharedPreferences(PROFILE_FILE, 0);
        editor = settings.edit();
    }

    private void getDBData() {
        SharedPreferences fullNameFile = getActivity().getSharedPreferences(PROFILE_FILE, 0);
        String storedFullName = fullNameFile.getString("fullName", "");

        if(storedFullName != "") {
            info.setText("Name: " + storedFullName);
        }
    }

    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
            if (accessToken2 == null) {
                // Log out logic
                editor.clear();
                editor.commit();
                info.setText("");
            }
        }
    };
}
