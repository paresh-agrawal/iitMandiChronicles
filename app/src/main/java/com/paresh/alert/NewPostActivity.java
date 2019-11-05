package com.paresh.alert;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class NewPostActivity extends AppCompatActivity {

    //private Toolbar new_post_toolbar;
    private ImageView newPostImage;
    private TextInputEditText newPostDesc,newPostTitle;
    private Button newPostBtn;
    private Uri postImageUri=null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private UploadTask upload_task, thumb_upload_task;
    private LinearLayout layout;
    private Bitmap compressedThumbImageBitmap,compressedImageBitmap;
    private TimePicker timePicker;
    private DatePicker datePicker;
    private int width=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage = findViewById(R.id.iv_add_photo);
        newPostDesc = findViewById(R.id.tv_desc);
        newPostTitle = findViewById(R.id.tv_title);
        newPostBtn = findViewById(R.id.bt_add_post);
        timePicker = findViewById(R.id.time_picker);
        datePicker = findViewById(R.id.date_picker);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        /*layout = (LinearLayout)findViewById(R.id.linear_layout);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    NewPostActivity.this.layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    NewPostActivity.this.layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                width  = layout.getMeasuredWidth();
                newPostImage.requestLayout();
                if(width!=0){
                    newPostImage.getLayoutParams().height = (int) Math.round(3.0*width/4.0);
                }

            }
        });*/

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(NewPostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    } else{
                        if(ContextCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(NewPostActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                        }else{
                            //Toast.makeText(SetupActivity.this, "Everything fine", Toast.LENGTH_SHORT).show();
                            imagePicker();
                        }
                    }
                }else{
                    imagePicker();
                }
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d("time", String.format("%02d", timePicker.getHour())+ ":" + String.format("%02d", timePicker.getMinute()));

                }
                Log.d("date", String.format("%02d", datePicker.getDayOfMonth()) + "/" + String.format("%02d", datePicker.getMonth()+1)+"/"+String.format("%02d", datePicker.getYear()));
                final String postDesc = newPostDesc.getText().toString();
                final String postTitle = newPostTitle.getText().toString();
                final String userImageUrl = mAuth.getCurrentUser().getPhotoUrl().toString();

                if(!TextUtils.isEmpty(postDesc) && !TextUtils.isEmpty(postTitle)  && postImageUri!=null){
                    final ProgressDialog progressDialog = new ProgressDialog(NewPostActivity.this,
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Uploading...");
                    progressDialog.show();

                    final String randomName = UUID.randomUUID().toString();

                    File newImageFile = new File(postImageUri.getPath());

                    try {
                        compressedImageBitmap = new Compressor(NewPostActivity.this).setMaxWidth(640).setQuality(80).compressToBitmap(newImageFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos_original = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos_original);
                    byte[] data_original = baos_original.toByteArray();
                    final StorageReference filePath = storageReference.child("post_image").child(randomName+".jpg");
                    upload_task = filePath.putBytes(data_original);

                    Task<Uri> urlTask = upload_task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            final String downloadUri = task.getResult().toString();

                            if (task.isSuccessful()) {

                                File newImageFile = new File(postImageUri.getPath());

                                try {
                                    compressedThumbImageBitmap = new Compressor(NewPostActivity.this).setMaxWidth(160).setQuality(5).compressToBitmap(newImageFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedThumbImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();
                                final StorageReference thumbFilePath = storageReference.child("post_image/thumbs").child(randomName+".jpg");
                                thumb_upload_task = thumbFilePath.putBytes(data);

                                Task<Uri> uriTask = thumb_upload_task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }

                                        // Continue with the task to get the download URL
                                        return thumbFilePath.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        String downloadThumbUrl = task.getResult().toString();
                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url",downloadUri);
                                        postMap.put("thumb", downloadThumbUrl);
                                        postMap.put("desc",postDesc);
                                        postMap.put("title",postTitle);
                                        postMap.put("user_image",userImageUrl);
                                        //postMap.put("title",currentUserId);
                                        //postMap.put("timestamp", FieldValue.serverTimestamp());


                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful()){
                                                    Log.d("DocRef", String.valueOf(task));
                                                    Toast.makeText(NewPostActivity.this, "Post was added.", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }else{
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this, "Error : "+error, Toast.LENGTH_SHORT).show();
                                                }
                                                progressDialog.dismiss();
                                            }
                                        });
                                    }
                                });
                                /*uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String downloadThumbUrl = taskSnapshot.getD

                                    }
                                });*/





                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error : " + error, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                        }
                    });
                }else {
                    if(postImageUri==null){
                        Toast.makeText(NewPostActivity.this, "Add Image", Toast.LENGTH_SHORT).show();
                    }else if(TextUtils.isEmpty(postTitle)){
                        Toast.makeText(NewPostActivity.this, "Add Title", Toast.LENGTH_SHORT).show();
                    }else if(TextUtils.isEmpty(postDesc)){
                        Toast.makeText(NewPostActivity.this, "Add Desc", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void imagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(NewPostActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //File newImageFile = new File(result.getUri().getPath());
                postImageUri = result.getUri();
                newPostImage.setPadding(0,0,0,0);
                newPostImage.setBackgroundColor(Color.TRANSPARENT);
                newPostImage.setImageURI(postImageUri);
                /*try {
                    compressedThumbImageBitmap = new Compressor(NewPostActivity.this).setMaxWidth(512).setMaxHeight(384).setQuality(5).compressToBitmap(newImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(NewPostActivity.this,"Error : "+error,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
