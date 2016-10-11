package br.iesb.messapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class ContactActivity extends AppCompatActivity {

    private EditText textName, textEmail, textPhone, textAddress, textSkype;
    private Contact contact;
    private String contactId;
    private String userId;
    private ImageView imageContact;
    private Drawable drawableCam;
    PictureManager pictureManager;

    private Realm realm;
    private RealmConfiguration realmConfig;

    private DatabaseReference mDatabase;

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

        mDatabase = FirebaseDatabase.getInstance().getReference(userId);

        textName = (EditText) findViewById(R.id.text_name_contact);
        textEmail = (EditText) findViewById(R.id.text_email_contact);
        textPhone = (EditText) findViewById(R.id.text_phone_contact);
        textAddress = (EditText) findViewById(R.id.text_address_contact);
        textSkype = (EditText) findViewById(R.id.text_skype_contact);
        imageContact = (ImageView) findViewById(R.id.image_contact_edit);
        drawableCam = imageContact.getDrawable();

        LoadContact();

        if (contact == null) {
            String contactId = "";
            if (savedInstanceState != null) {
                contactId = savedInstanceState.getString("contactId");
            }
            if (contactId == "") {
                this.contactId = UUID.randomUUID().toString();
            } else {
                this.contactId = contactId;
            }
        } else {
            this.contactId = contact.getId();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        if (contact == null){
            menu.findItem(R.id.delete_contact).setVisible(false);
        }
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
            saveContact();
        } else if (id == R.id.delete_contact){
            deleteContact();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickImageContact (View view){
        pictureManager = new PictureManager();
        pictureManager.loadPicture(this, R.id.image_contact_edit);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (imageContact.getDrawable() != drawableCam){
            imageContact.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("contactId", contactId);
    }

    private void LoadContact(){
        String contactId = getIntent().getStringExtra("contactId");
        if (contactId != null && !contactId.isEmpty()){
            contact = realm.where(Contact.class).equalTo("id", contactId).findFirst();
            textName.setText(contact.getName());
            textEmail.setText(contact.getEmail());
            textPhone.setText(contact.getPhone());
            textAddress.setText(contact.getAddress());
            textSkype.setText(contact.getSkypeId());
        }
    }

    public void saveContact(){
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
                    contact.setId(contactId);
                } else {
                    realm.cancelTransaction();
                    Toast.makeText(this, getResources().getString(R.string.contact_already_registered), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (isContactAlreadyRegistered(email, contact.getId())){
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
            final Contact contactFirebase = realm.copyFromRealm(contact);

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mDatabase.child(contactId).setValue(contactFirebase);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    String TAG = ContactActivity.class.getSimpleName();
                    Log.i(TAG, databaseError.getMessage());
                    Log.i(TAG, databaseError.getDetails());
                }
            });

            if (pictureManager != null){
                pictureManager.savePictureFile(getResources().getString(R.string.image_directory), contactId);
            }

            Toast.makeText(this, getResources().getString(R.string.contact_saved_success), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, errorMsg.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteContact(){
        realm.beginTransaction();
        contact.deleteFromRealm();
        realm.commitTransaction();
        setResult(RESULT_OK);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDatabase.child(contactId).removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String TAG = ContactActivity.class.getSimpleName();
                Log.i(TAG, databaseError.getMessage());
                Log.i(TAG, databaseError.getDetails());
            }
        });
        finish();
    }

    private boolean isContactAlreadyRegistered(String email){
        RealmResults<Contact> results = realm.where(Contact.class).equalTo("email", email).findAll();
        return !results.isEmpty();
    }

    private boolean isContactAlreadyRegistered(String email, String id){
        RealmResults<Contact> results = realm.where(Contact.class).equalTo("email", email).notEqualTo("id", id).findAll();
        return !results.isEmpty();
    }
}
