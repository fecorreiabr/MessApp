package br.iesb.messapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class LoginActivity extends AppCompatActivity {

    private Realm realm;
    private RealmConfiguration realmConfig;
    private String userId;
    private User user;
    private EditText textEmail;
    private EditText textPwd;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (isUserLogged()){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("id", userId);
            startActivity(intent);
            finish();
            return;
        }

        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);

        textEmail = (EditText)findViewById(R.id.text_email_login);
        textPwd = (EditText)findViewById(R.id.text_pwd_login);

        firebaseAuth = FirebaseAuth.getInstance();
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
                        Utility.SaveLogin(LoginActivity.this, userId);
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

    private User Login (String email, String pwd){
        RealmResults<User> results = realm.where(User.class).equalTo("email", email).equalTo("pwd", pwd).findAll();
        if (!results.isEmpty()){
            return results.first();
        } else {
            return null;
        }
    }

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
