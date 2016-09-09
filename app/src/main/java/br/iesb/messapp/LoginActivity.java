package br.iesb.messapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class LoginActivity extends AppCompatActivity {

    private Realm realm;
    private RealmConfiguration realmConfig;
    private User user;
    private EditText textEmail;
    private EditText textPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);

        textEmail = (EditText)findViewById(R.id.text_email_login);
        textPwd = (EditText)findViewById(R.id.text_pwd_login);
    }

    public void onClickRegister (View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onClickLogin (View view){
        String email = textEmail.getText().toString();
        String pwd = textPwd.getText().toString();

        if (Login(email, pwd)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, getResources().getString(R.string.user_or_pwd_incorrect_login), Toast.LENGTH_LONG).show();
        }

    }

    private boolean Login (String email, String pwd){
        RealmResults<User> results = realm.where(User.class).equalTo("email", email).equalTo("pwd", pwd).findAll();
        return !results.isEmpty();
    }
}
