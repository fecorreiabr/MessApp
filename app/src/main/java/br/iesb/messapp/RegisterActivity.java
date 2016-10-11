package br.iesb.messapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText textName;
    private EditText textEmail;
    private EditText textPwd;
    private ImageView imageUser;
    private Drawable drawableCam;
    private String userId;
    private PictureManager pictureManager;

    private Realm realm;
    private RealmConfiguration realmConfig;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;

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
        imageUser = (ImageView)findViewById(R.id.img_user_register);
        drawableCam = imageUser.getDrawable();

        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        /*String userId = "";
        if (savedInstanceState != null) {
            userId = savedInstanceState.getString("userId");
        }
        if (userId == "") {
            this.userId = UUID.randomUUID().toString();
        } else {
            this.userId = userId;
        }*/

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

    public void onClickImageUser (View view){
        pictureManager = new PictureManager();
        pictureManager.loadPicture(this, R.id.img_user_register);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (imageUser.getDrawable() != drawableCam){
            imageUser.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("userId", userId);
    }*/

    private void RegisterUser(){
        final String name = textName.getText().toString();
        final String email = textEmail.getText().toString();
        final String pwd = textPwd.getText().toString();
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
            //if (!isUserAlreadyRegistered(email)){
            Utility.showProgressDialog(
                    this,
                    getResources().getString(R.string.progress_register_title),
                    getResources().getString(R.string.progress_register_desc)
            );
            firebaseAuth.createUserWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (!task.isSuccessful()){

                                try{
                                    throw  task.getException();
                                } catch(FirebaseAuthUserCollisionException e){
                                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.email_already_registerd), Toast.LENGTH_LONG).show();
                                } catch(Exception e){
                                    Log.e(RegisterActivity.this.getClass().getSimpleName(), e.getMessage());
                                }

                            } else {
                                FirebaseUser firebaseUser = task.getResult().getUser();
                                userId = firebaseUser.getUid();

                                realm.beginTransaction();
                                User user = realm.createObject(User.class);
                                user.setId(userId);
                                user.setName(name);
                                user.setEmail(email);
                                //user.setPwd(pwd);
                                realm.commitTransaction();
                                if (pictureManager != null) {
                                    pictureManager.savePictureFile(getResources().getString(R.string.image_directory), userId);
                                }

                                realm.beginTransaction();
                                Contact contact = realm.createObject(Contact.class);
                                contact.setId(userId);
                                contact.setName(name);
                                contact.setEmail(email);
                                contact.setOwner(userId);
                                realm.commitTransaction();

                                Utility.saveLogin(RegisterActivity.this, userId);

                                User user1 = realm.copyFromRealm(user);

                                mDatabase.child(userId).setValue(user1);
                                //mDatabase.child(userId).child("name").setValue(name);
                                //mDatabase.child(userId).child("email").setValue(email);

                                Utility.saveLogin(RegisterActivity.this, userId);
                                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.user_registered_success), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            Utility.hideProgressDialog();
                        }
                    });

            /*}
            else {
                errorMsg.append(getResources().getString(R.string.email_already_registerd));
            }*/
        } else {
            Toast.makeText(this, errorMsg.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void registerUserFirebase(){
        mDatabase.child(userId).setValue(userId);
    }


    /*private boolean isUserAlreadyRegistered(String email){
        RealmResults<User> results =  realm.where(User.class).equalTo("email", email).findAll();
        return !results.isEmpty();
    }*/


}
