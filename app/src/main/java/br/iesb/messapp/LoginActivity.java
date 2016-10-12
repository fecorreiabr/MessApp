package br.iesb.messapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_GOOGLE_LOGIN = 931;

    private Realm realm;
    private RealmConfiguration realmConfig;
    private String userId;
    private User user;
    private User facebookUser;
    private EditText textEmail;
    private EditText textPwd;
    private LoginButton loginButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    GoogleApiClient googleApiClient;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);

        textEmail = (EditText)findViewById(R.id.text_email_login);
        textPwd = (EditText)findViewById(R.id.text_pwd_login);

        firebaseAuth = FirebaseAuth.getInstance();
        setFacebookLogin();
        setGoogleLogin();
        //setmAuthListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isUserLogged()){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("id", userId);
            startActivity(intent);
            finish();
        } else {
            LoginManager.getInstance().logOut();
            if (googleApiClient.isConnected()){
                Auth.GoogleSignInApi.signOut(googleApiClient);
            }
        }
        //firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GOOGLE_LOGIN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                Utility.showProgressDialog(
                        LoginActivity.this,
                        getString(R.string.progress_login_title),
                        getString(R.string.progress_login_desc));
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.w("GOOGLE", "Google login error: " + result.getStatus().getStatusMessage());
                Toast.makeText(LoginActivity.this, "Falha de autenticação.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    /*private void setmAuthListener(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User signed in
                } else {
                    //User signed out
                }
            }
        };
    }*/

    private void handleFacebookAccessToken(AccessToken token) {
        final String TAG = "FACEBOOK";
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Utility.hideProgressDialog();
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Falha de autenticação.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = task.getResult().getUser();
                            if (user != null){
                                String userId = user.getUid();
                                facebookUser.setId(userId);
                                loginFromProvider(facebookUser);
                            }
                        }


                        // ...
                    }
                });
    }

    private void setFacebookLogin(){
        final String TAG = "FACEBOOK";
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_facebook);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Utility.showProgressDialog(
                        LoginActivity.this,
                        getString(R.string.progress_login_title),
                        getString(R.string.progress_login_desc));
                Log.d(TAG, "Facebook login success!");
                AccessToken accessToken = loginResult.getAccessToken();

                String token = accessToken.getToken();
                Log.d(TAG, "Token: " + token);

                String userId = accessToken.getUserId();
                Log.d(TAG, "UId: " + userId);

                facebookUser = new User();
                facebookUser.setPwd(token);

                GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        FacebookRequestError error = response.getError();
                        if (error != null){
                            Log.e(TAG, error.getErrorMessage());
                        } else {
                            try {
                                String jsonResult = String.valueOf(object);
                                Log.d(TAG, "Facebook data: " + jsonResult);

                                String email = object.getString("email");
                                String name = object.getString("name");

                                facebookUser.setEmail(email);
                                facebookUser.setName(name);

                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "email,id,name,picture");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();

                handleFacebookAccessToken(accessToken);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook login canceled.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "Facebook login error.");
                error.printStackTrace();
            }
        });
    }

    private void saveProviderUser(final User user){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(user);
            }
        });
    }

    private void setGoogleLogin(){
        findViewById(R.id.login_google).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickGoogleLogin(view);
            }
        });
        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_google_web_client_id))
                .requestEmail().requestProfile()
                .build();

        GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.e("GOOGLE", connectionResult.getErrorMessage());
                Toast.makeText(LoginActivity.this, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
            }
        };

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, onConnectionFailedListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
    }

    public void onClickGoogleLogin(View view){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, REQUEST_GOOGLE_LOGIN);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account){
        final String TAG = "FIREBASE";
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Boolean taskSuccess = task.isSuccessful();
                        Log.d(TAG, "firebaseAuthWithGoogle: onComplete: " + taskSuccess);

                        if (!taskSuccess) {
                            Utility.hideProgressDialog();
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Falha de autenticação.",
                                    Toast.LENGTH_SHORT).show();
                            if (googleApiClient.isConnected()){
                                Auth.GoogleSignInApi.signOut(googleApiClient);
                            }
                        } else {
                            FirebaseUser user = task.getResult().getUser();
                            if (user != null){
                                User googleUser = new User();
                                String userId = user.getUid();
                                googleUser.setId(userId);
                                googleUser.setName(account.getDisplayName());
                                googleUser.setEmail(user.getEmail());
                                googleUser.setPwd(account.getIdToken());
                                loginFromProvider(googleUser);
                            }
                        }
                    }
                });
    }

    private void createContactFromUser(User user){
        Contact contact = realm.where(Contact.class).equalTo("id", user.getId()).findFirst();
        if (contact == null) {
            realm.beginTransaction();
            contact = realm.createObject(Contact.class);
            contact.setId(user.getId());
            contact.setEmail(user.getEmail());
            contact.setName(user.getName());
            contact.setOwner(user.getId());
            realm.commitTransaction();
        }
    }

    private void loginFromProvider(User user){
        userId = user.getId();
        saveProviderUser(user);
        createContactFromUser(user);
        Utility.saveLogin(LoginActivity.this, userId);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("id", userId);
        Utility.hideProgressDialog();
        startActivity(intent);
        finish();
    }

    public void onClickRegister (View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onClickLogin (View view){
        String email = textEmail.getText().toString();
        String pwd = textPwd.getText().toString();
        //user = Login(email, pwd);

        Utility.showProgressDialog(
                this,
                getResources().getString(R.string.progress_login_title),
                getResources().getString(R.string.progress_login_desc));
        firebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.user_or_pwd_incorrect_login), Toast.LENGTH_LONG).show();
                }
                else {
                    FirebaseUser user = task.getResult().getUser();
                    if (user != null){
                        String userId = user.getUid();
                        Utility.saveLogin(LoginActivity.this, userId);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("id", userId);
                        startActivity(intent);
                        finish();
                    }
                }
                Utility.hideProgressDialog();
            }
        });

        /*if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("id", user.getId());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getResources().getString(R.string.user_or_pwd_incorrect_login), Toast.LENGTH_LONG).show();
        }*/

    }

    /*private User Login (String email, String pwd){
        RealmResults<User> results = realm.where(User.class).equalTo("email", email).equalTo("pwd", pwd).findAll();
        if (!results.isEmpty()){
            return results.first();
        } else {
            return null;
        }
    }*/

    private Boolean isUserLogged(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultValue = "no_user";
        userId = sharedPreferences.getString(
                getString(R.string.preference_uid), defaultValue);
        Boolean userLogged = sharedPreferences.getBoolean(
                getString(R.string.preference_user_logged), false);
        return !userId.equals("no_user") && userLogged;
    }
}
