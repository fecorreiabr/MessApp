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

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class ContactActivity extends AppCompatActivity {

    private EditText textName, textEmail, textPhone, textAddress, textSkype;
    private Contact contact;
    private String userId;

    private Realm realm;
    private RealmConfiguration realmConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realmConfig = new RealmConfiguration.Builder(this).build();
        realm = Realm.getInstance(realmConfig);

        userId = getIntent().getStringExtra("userId");

        textName = (EditText) findViewById(R.id.text_name_contact);
        textEmail = (EditText) findViewById(R.id.text_email_contact);
        textPhone = (EditText) findViewById(R.id.text_phone_contact);
        textAddress = (EditText) findViewById(R.id.text_address_contact);
        textSkype = (EditText) findViewById(R.id.text_skype_contact);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.save_contact) {
            SaveContact();
        }

        return super.onOptionsItemSelected(item);
    }

    public void SaveContact(){
        String name = textName.getText().toString();
        String email = textEmail.getText().toString();
        String phone = textPhone.getText().toString();
        String address = textAddress.getText().toString();
        String skype = textSkype.getText().toString();
        StringBuilder errorMsg = new StringBuilder();

        if (TextUtils.isEmpty(name)){
            errorMsg.append(getResources().getString(R.string.name_incorrect_register));
            errorMsg.append("\n");
        }
        if (TextUtils.isEmpty(email) || !Utility.isValidEmail(email)){
            errorMsg.append(getResources().getString(R.string.email_incorrect_register));
            errorMsg.append("\n");
        }
        if (!TextUtils.isEmpty(phone) && !Utility.isValidPhone(phone)){
            errorMsg.append(getResources().getString(R.string.phone_incorrect_contact));
            errorMsg.append("\n");
        }

        if (TextUtils.isEmpty(errorMsg.toString())) {

            realm.beginTransaction();
            if (contact == null){
                if (!isContactAlreadyRegistered(email)) {
                    contact = realm.createObject(Contact.class);
                    contact.setId(UUID.randomUUID().toString());
                } else {
                    realm.cancelTransaction();
                    Toast.makeText(this, getResources().getString(R.string.contact_already_registered), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            contact.setName(name);
            contact.setEmail(email);
            contact.setPhone(phone);
            contact.setAddress(address);
            contact.setSkypeId(skype);
            contact.setOwner(userId);
            realm.commitTransaction();

            Toast.makeText(this, getResources().getString(R.string.contact_saved_success), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, errorMsg.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isContactAlreadyRegistered(String email){
        RealmResults<Contact> results = realm.where(Contact.class).equalTo("email", email).findAll();
        return !results.isEmpty();
    }
}
