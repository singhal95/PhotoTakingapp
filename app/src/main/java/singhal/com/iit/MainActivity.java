package singhal.com.iit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> arrayList;
    private static final int RECORD_REQUEST_CODE = 101;
    FloatingActionButton floatingActionButton;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    GridView gridView;
    private DatabaseReference mPhotoDatabaseREF;
    private StorageReference mStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView=findViewById(R.id.list_item);
        floatingActionButton=findViewById(R.id.fab);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPhotoDatabaseREF=mFirebaseDatabase.getReference();
        mStorageRef=mFirebaseStorage.getReference();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE},RECORD_REQUEST_CODE);
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
            progress.setMessage("Downloading images.......");
            progress.show();
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Uri selectedImage = getImageUri(getApplicationContext(), photo);
           // Uri selectedImage = (Uri) data.getExtras().get("data");
            StorageReference Ref=mStorageRef.child(String.valueOf(Math.random()));
            Ref.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progress.dismiss();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(MainActivity.this,"Successfully uploaded",Toast.LENGTH_SHORT).show();
                    mPhotoDatabaseREF.push().setValue(downloadUrl.toString());
                }
            });


        } else if (requestCode == 2 && resultCode != Activity.RESULT_CANCELED) {
            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
            progress.setMessage("Uploading images.......");
            progress.show();
            Uri selectedImage = data.getData();
            StorageReference Ref=mStorageRef.child(selectedImage.getLastPathSegment());
            Ref.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progress.dismiss();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(MainActivity.this,"Successfully uploaded",Toast.LENGTH_SHORT).show();
                  mPhotoDatabaseREF.push().setValue(downloadUrl.toString());

                    //String id=mPhotoDatabaseREF.push().getKey();
                  //  mPhotoDatabaseREF.child(id).setValue(downloadUrl);
                }
            });


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case RECORD_REQUEST_CODE:
                if (grantResults.length !=0
                        &&  (grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED ) ){
                    final AlertDialog.Builder alert=new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    AlertDialog dialog=alert.create();
                    dialog.setTitle("Choose From");
                    final CharSequence[] items = {
                            "Camera", "Gallery"
                    };
                    alert.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1);
                                    dialog.dismiss();
                                    break;
                                case 1:
                                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),2);
                                    dialog.dismiss();
                                    break;

                            }
                        }
                    });
                    alert.show();

                } else {
                    AlertDialog.Builder alert=new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    alert.setTitle("Warning");
                    alert.setMessage("Yow have to give permission as app will crash and will not work properly");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            makeRequest();
                        }
                    });
                    alert.show();

                }
        }
    }

    @Override
    protected void onResume() {
        final ProgressDialog progress = new ProgressDialog(MainActivity.this);
        progress.setMessage("Downloading images.......");
        progress.show();
        arrayList=new ArrayList<>();
        mPhotoDatabaseREF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayList.clear();
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String value=dataSnapshot1.getValue(String.class);
                    arrayList.add(value);
                }
                progress.dismiss();
                Adapter adapter=new Adapter(arrayList,MainActivity.this);
                gridView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.dismiss();
                Toast.makeText(MainActivity.this,"Check Your Internet Connection",Toast.LENGTH_SHORT).show();
            }
        });
        super.onResume();
    }
}
