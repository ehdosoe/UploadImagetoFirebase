package com.ehdosoe.newmac6.firebasedatabasetest;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private ImageView img;
    private EditText editText;
    private Uri imgUri;

    public static final String FB_STORAGE_PATH="image/";
    public static final String FB_DATABASE_PATH="image";
    public static final int REQUEST_CODE=1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStorageRef= FirebaseStorage.getInstance().getReference();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference(FB_DATABASE_PATH);

        img=(ImageView)findViewById(R.id.imageView);
        editText=(EditText)findViewById(R.id.txt);
    }

    public void btnBrowse(View view) {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE && resultCode==RESULT_OK && data !=null && data.getData()!=null){
            imgUri=data.getData();

            try{
                Bitmap  bm= MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);
                img.setImageBitmap(bm);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getImageExt(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void btnUpload(View view) {
        if (imgUri!=null){
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setTitle("Uploading Image");
            dialog.show();

            //Get the storage reference
            StorageReference storageReference=mStorageRef.child(FB_STORAGE_PATH+System.currentTimeMillis()+"."+getImageExt(imgUri));

            //Add file to reference
            storageReference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "Successfuly uploaded", Toast.LENGTH_SHORT).show();
                    ImageUpload imageUpload=new ImageUpload(editText.getText().toString(),taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());

                    String uploadID=mDatabaseRef.push().getKey();
                    mDatabaseRef.child(uploadID).setValue(imageUpload);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress=(100*taskSnapshot.getBytesTransferred())/ taskSnapshot.getTotalByteCount();
                            dialog.setMessage("Uploaded"+(int)progress+"%");
                        }
                    });
        }else {
            Toast.makeText(this, "Please select an Image", Toast.LENGTH_SHORT).show();
        }
    }
}
