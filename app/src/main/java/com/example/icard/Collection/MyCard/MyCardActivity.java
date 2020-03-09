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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.icard.Exchange.ExchangeActivity;
import com.example.icard.Exchange.ReadCard.ReadCardActivity;
import com.example.icard.MainActivity;
import com.example.icard.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class MyCardActivity extends AppCompatActivity
{
    public ImageView myCardImage;
    public LinearLayout myCardMetadataList;
    public EditText myCardMetadata;

    public static Uri targetUri;
    public List<String> metadata = new ArrayList<>();

    public int CAMERA_PERMISSION_REQUEST = 1;
    public static int REQUEST_IMAGE_PICK = 2;
    public static int REQUEST_IMAGE_CAPTURE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_card_layout);

        targetUri = null;

        myCardImage = findViewById(R.id.my_card_image);
        myCardMetadataList = findViewById(R.id.my_card_metadata_list);
        myCardMetadata = findViewById(R.id.my_card_metadata);

        Button myCardAddCardButton = findViewById(R.id.my_card_add_card_button);
        myCardAddCardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        Button myCardTakePhotoButton = findViewById(R.id.my_card_take_photo_button);
        myCardTakePhotoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ContextCompat.checkSelfPermission(MyCardActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MyCardActivity.this,
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
                            targetUri = FileProvider.getUriForFile(MyCardActivity.this,
                                    "com.example.android.fileprovider", photoFile);

                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                }
            }
        });

        Button myCardAddMetadataButton = findViewById(R.id.my_card_add_metadata_button);
        myCardAddMetadataButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String curMetadata = myCardMetadata.getText().toString();

                if (!curMetadata.equals(""))
                {
                    metadata.add(curMetadata);

                    myCardMetadata.setText("");

                    TextView metadataView = new TextView(MyCardActivity.this);

                    metadataView.setText(curMetadata);
                    metadataView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                    metadataView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    myCardMetadataList.addView(metadataView);
                }
            }
        });

        Button myCardSubmitButton = findViewById(R.id.my_card_submit_button);
        myCardSubmitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (targetUri == null)
                {
                    Toast.makeText(MyCardActivity.this, "Photo missing",
                            Toast.LENGTH_LONG).show();

                    return;
                }

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
                            DatabaseReference myRef = mDatabase.child(MainActivity.userID).child("my-card").push();

                            myRef.child("url").setValue(url);

                            for (String curMetadata : metadata)
                            {
                                myRef.child("metadata").push().setValue(curMetadata);
                            }

                            Intent intent = new Intent(MyCardActivity.this, MyCardListActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(MyCardActivity.this, "Upload unsuccessful",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
            Glide.with(MyCardActivity.this).load(targetUri).into(myCardImage);
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
                        targetUri = FileProvider.getUriForFile(MyCardActivity.this,
                                "com.example.android.fileprovider", photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        }
    }
}