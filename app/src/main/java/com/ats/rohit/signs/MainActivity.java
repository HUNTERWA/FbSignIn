package com.ats.rohit.signs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    CallbackManager callbackManager;
    TextView txtEmail,txtBirthday;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        callbackManager=CallbackManager.Factory.create();

        LoginButton loginButton=findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile","email","user_birthday","user_friends"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                progressDialog=new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Retrieving data...");
                progressDialog.show();

                String accessToken=loginResult.getAccessToken().getToken();

                GraphRequest request=GraphRequest.newMeRequest(loginResult.getAccessToken(),new GraphRequest                                                        .GraphJSONObjectCallback()
                {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response)
                    {
                        Log.d("responseIs",""+response.toString());
                        progressDialog.dismiss();
                        getData(object);
                        /*try
                        {
                            Log.d("nameIs",object.getString("first_name"));
                            Log.d("lastNameIs",object.getString("last_name"));
                            Log.d("Gender",object.getString("gender"));
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }*/

                    }
                });

                Bundle parameters=new Bundle();
                parameters.putString("fields","id,email,birthday,friends");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel()
            {
                Log.d("LogIn","GotCencelled");
            }

            @Override
            public void onError(FacebookException error)
            {
                Log.d("causeOfErrorIs",error+"");
            }
        });

        if (AccessToken.getCurrentAccessToken()!=null)
        {//if already login
            Log.d("logedIn",AccessToken.getCurrentAccessToken().getUserId()+"");
        }
        printKeyHash();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    private void printKeyHash()
    {
        try
        {
            PackageInfo packageInfo=getPackageManager().getPackageInfo("com.ats.rohit.signs", PackageManager.GET_SIGNATURES);
            for(Signature signature:packageInfo.signatures)
            {
                MessageDigest messageDigest=MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(messageDigest.digest(),Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    public void getData(JSONObject object)
    {
        try
        {
            URL profile_picture=new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=250&height=250");
            //Picasso.with(this).load(profile_picture.toString).into(imageView);
            Log.d("emailIs",object.getString("email"));
            Log.d("birthDayIs",object.getString("birthday"));
            /*Log.d("nameIs",object.getString("first_name"));
            Log.d("lastNameIs",object.getString("last_name"));
            Log.d("Gender",object.getString("gender"));*/



            //Log.d("friendsAre",object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
