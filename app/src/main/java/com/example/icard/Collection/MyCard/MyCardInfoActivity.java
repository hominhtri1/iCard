package com.example.icard.Collection.MyCard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.icard.Card;
import com.example.icard.Exchange.ExchangeActivity;
import com.example.icard.Exchange.ReadCard.ReadCardActivity;
import com.example.icard.MainActivity;
import com.example.icard.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MyCardInfoActivity extends AppCompatActivity
{
    public ImageView myCardInfoImage;
    public LinearLayout myCardInfoMetadataList;
    public EditText myCardInfoMetadata;

    public Uri targetUri = null;
    public List<String> metadata;
    public String ID;
    public List<EditText> metadataViewList = new ArrayList<>();

    public int CAMERA_PERMISSION_REQUEST = 1;
    public static int REQUEST_IMAGE_PICK = 2;
    public static int REQUEST_IMAGE_CAPTURE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_card_info_layout);

        myCardInfoImage = findViewById(R.id.my_card_info_image);
        myCardInfoMetadataList = findViewById(R.id.my_card_info_metadata_list);
        myCardInfoMetadata = findViewById(R.id.my_card_info_metadata);

        Intent intent = getIntent();
        int index = intent.getIntExtra("index", -1);
        Card card = MyCardListActivity.cList.get(index);

        metadata = new ArrayList<>(card.meta);
        ID = card.ID;

        Glide.with(MyCardInfoActivity.this).load(card.url).into(myCardInfoImage);

        Button myCardInfoChangeImageButton = findViewById(R.id.my_card_info_change_image_button);
        myCardInfoChangeImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        Button myCardInfoTakePhotoButton = findViewById(R.id.my_card_info_take_photo_button);
        myCardInfoTakePhotoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ContextCompat.checkSelfPermission(MyCardInfoActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MyCardInfoActivity.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                }
                else
                {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                    {
                        File photoFile = null;

                        try
                        {
                            photoFile = createImageFile();
                        }
                        catch (IOException ex)
                        {
                        }

                        // Continue only if the File was successfully created
                        if (photoFile != null)
                        {
                            targetUri = FileProvider.getUriForFile(MyCardInfoActivity.this,
                                    "com.example.android.fileprovider", photoFile);

                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                }
            }
        });

        for (String curMetadata : metadata)
        {
            EditText metadataView = new EditText(MyCardInfoActivity.this);

            metadataView.setText(curMetadata);
            metadataView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            metadataView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            myCardInfoMetadataList.addView(metadataView);

            metadataViewList.add(metadataView);
        }

        Button myCardInfoAddMetadataButton = findViewById(R.id.my_card_info_add_metadata_button);
        myCardInfoAddMetadataButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String curMetadata = myCardInfoMetadata.getText().toString();

                if (!curMetadata.equals(""))
                {
                    metadata.add(curMetadata);

                    myCardInfoMetadata.setText("");

                    EditText metadataView = new EditText(MyCardInfoActivity.this);

                    metadataView.setText(curMetadata);
                    metadataView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                    metadataView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    myCardInfoMetadataList.addView(metadataView);

                    metadataViewList.add(metadataView);
                }
            }
        });

        Button myCardInfoUpdateButton = findViewById(R.id.my_card_info_update_button);
        myCardInfoUpdateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (targetUri != null)
                {
                    String imageName = UUID.randomUUID().toString();

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    final StorageReference testRef = storageRef.child(imageName);
                    UploadTask uploadTask = testRef.putFile(targetUri);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                    {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                        {
                            if (!task.isSuccessful())
                            {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return testRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task)
                        {
                            if (task.isSuccessful())
                            {
                                Uri downloadUri = task.getResult();
                                String url = downloadUri.toString();

                                DatabaseReference mDatabase =
                                        FirebaseDatabase.getInstance().getReference();
                                DatabaseReference myRef = mDatabase.child(MainActivity.userID).child("my-card").child(ID);

                                myRef.child("url").setValue(url);
                            }
                            else
                            {
                                Toast.makeText(MyCardInfoActivity.this, "Upload unsuccessful",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                metadata = new ArrayList<>();

                for (EditText metadataView : metadataViewList)
                {
                    String curMetadata = metadataView.getText().toString();

                    if (!curMetadata.equals(""))
                    {
                        metadata.add(curMetadata);
                    }
                }

                if (ID.equals(""))
                    return;

                DatabaseReference mDatabase =
                        FirebaseDatabase.getInstance().getReference();
                DatabaseReference myRef = mDatabase.child(MainActivity.userID).child("my-card").child(ID);

                myRef.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot metadataSnapshot : dataSnapshot.child("metadata").getChildren())
                        {
                            metadataSnapshot.getRef().removeValue();
                        }

                        for (String curMetadata : metadata)
                        {
                            dataSnapshot.getRef().child("metadata").push().setValue(curMetadata);
                        }

                        Intent intent = new Intent(MyCardInfoActivity.this, MyCardListActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                        Log.e("iCard", databaseError.toException().toString());
                    }
                });
            }
        });

        Button myCardInfoDeleteButton = findViewById(R.id.my_card_info_delete_button);
        myCardInfoDeleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ID.equals(""))
                    return;

                DatabaseReference mDatabase =
                        FirebaseDatabase.getInstance().getReference();
                DatabaseReference myRef = mDatabase.child(MainActivity.userID).child("my-card").child(ID);

                myRef.removeValue();

                Intent intent = new Intent(MyCardInfoActivity.this, MyCardListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK)
        {
            targetUri = data.getData();
        }

        try
        {
            BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
            Glide.with(MyCardInfoActivity.this).load(targetUri).into(myCardInfoImage);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == CAMERA_PERMISSION_REQUEST)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                {
                    File photoFile = null;

                    try
                    {
                        photoFile = createImageFile();
                    }
                    catch (IOException ex)
                    {
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null)
                    {
                        targetUri = FileProvider.getUriForFile(MyCardInfoActivity.this,
                                "com.example.android.fileprovider", photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        }
    }
}