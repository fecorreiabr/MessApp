package br.iesb.messapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText textName;
    private EditText textEmail;
    private EditText textPwd;

    private Realm realm;
    private RealmConfiguration realmConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);

        textName = (EditText)findViewById(R.id.text_name_register);
        textEmail = (EditText)findViewById(R.id.text_email_register);
        textPwd = (EditText)findViewById(R.id.text_pwd_register);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.save_register) {
            RegisterUser();
        }

        return super.onOptionsItemSelected(item);
    }

    private void RegisterUser(){
        String name = textName.getText().toString();
        String email = textEmail.getText().toString();
        String pwd = textPwd.getText().toString();
        StringBuilder errorMsg = new StringBuilder();
        if (TextUtils.isEmpty(name)){
            errorMsg.append(getResources().getString(R.string.name_incorrect_register));
            errorMsg.append("\n");
        }
        if (TextUtils.isEmpty(email) || !Utility.isValidEmail(email)){
            errorMsg.append(getResources().getString(R.string.email_incorrect_register));
            errorMsg.append("\n");
        }
        if (TextUtils.isEmpty(pwd) || !Utility.isValidPassword(pwd)){
            errorMsg.append(getResources().getString(R.string.pwd_incorrect_register));
            errorMsg.append("\n");
        }

        if (TextUtils.isEmpty(errorMsg.toString())) {
            if (!isUserAlreadyRegistered(email)){
                realm.beginTransaction();
                User user = realm.createObject(User.class);
                user.setName(name);
                user.setEmail(email);
                user.setPwd(pwd);
                realm.commitTransaction();

                realm.beginTransaction();
                Contact contact = realm.createObject(Contact.class);
                contact.setName(name);
                contact.setEmail(email);
                realm.commitTransaction();

                Toast.makeText(this, getResources().getString(R.string.user_registered_success), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            else {
                errorMsg.append(getResources().getString(R.string.email_already_registerd));
            }
        }

        Toast.makeText(this, errorMsg.toString(), Toast.LENGTH_LONG).show();

    }

    private boolean isUserAlreadyRegistered(String email){
        RealmResults<User> results =  realm.where(User.class).equalTo("email", email).findAll();
        return !results.isEmpty();
    }

}
