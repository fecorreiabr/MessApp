package br.iesb.messapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Felipe on 11/09/2016.
 */
public class PictureManager extends Fragment {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int REQUEST_CAMERA = 5000;
    public static final int REQUEST_FILE = 6000;
    public static final String FRAGMENT_TRANSACTION_TAG = "getimage";

    public ImageView imageView;
    private AppCompatActivity activity;
    private FragmentManager fragManager;
    private int userChoosenTask;
    private String folder;
    private String fileName;

    public  PictureManager() {
    }

    public void loadPicture(String folder, String fileName, final AppCompatActivity activity, @IdRes int imageView){
        this.activity = activity;
        this.fragManager = activity.getSupportFragmentManager();
        this.folder = folder;
        this.fileName = fileName;
        this.imageView = (ImageView) activity.findViewById(imageView);

        FragmentTransaction fragmentTransaction = this.fragManager.beginTransaction();
        fragmentTransaction.add(this, FRAGMENT_TRANSACTION_TAG);
        fragmentTransaction.commit();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        selectImage();
    }

    public void selectImage (){

        //final CharSequence[] items = { "Take Photo", "Choose from Library",
        //        "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.title_dialog_add_photo));
        builder.setItems(activity.getResources().getStringArray(R.array.photo_options), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = checkPermission(activity);
                if (item == 0) {
                    userChoosenTask = REQUEST_CAMERA;
                    if (result)
                        cameraIntent();

                } else if (item == 1) {
                    userChoosenTask = REQUEST_FILE;
                    if (result)
                        galleryIntent();
                } else if (item == 2) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
        fragManager.beginTransaction().remove(this).commit();
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(activity.getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageView.setImageBitmap(savePictureFile(bm));
        }

    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        imageView.setImageBitmap(savePictureFile(thumbnail));
    }

    private Bitmap savePictureFile(Bitmap bitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String externalStorage = Environment.getExternalStorageDirectory().toString();
        File mFolder = new File(externalStorage + folder);

        if (!mFolder.exists()){
            mFolder.mkdir();
        }

        File destination = new File(mFolder.getAbsolutePath(),
                fileName + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private void cameraIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_FILE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission ( final Context context)
    {
        boolean returnValue;
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle(context.getResources().getString(R.string.title_permission_necessary));
                    alertBuilder.setMessage(context.getResources().getString(R.string.msg_permission_external_storage));
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                returnValue = false;
            } else {
                returnValue = true;
            }

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.CAMERA)) {
                    AlertDialog.Builder alertBuilder2 = new AlertDialog.Builder(context);
                    alertBuilder2.setCancelable(true);
                    alertBuilder2.setTitle(context.getResources().getString(R.string.title_permission_necessary));
                    alertBuilder2.setMessage(context.getResources().getString(R.string.msg_permission_camera));
                    alertBuilder2.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                        }
                    });
                    AlertDialog alert = alertBuilder2.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                }
                returnValue = false;
            } else {
                returnValue = returnValue & true;
            }
        } else {
            returnValue = true;
        }
        return returnValue;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask == REQUEST_CAMERA)
                        cameraIntent();
                    else if(userChoosenTask == REQUEST_FILE)
                        galleryIntent();
                } else {
//code for deny
                }
                break;
        }
    }
}
