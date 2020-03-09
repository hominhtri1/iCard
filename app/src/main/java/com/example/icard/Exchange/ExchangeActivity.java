package com.example.icard.Exchange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.icard.Card;
import com.example.icard.Collection.MyCard.MyCardActivity;
import com.example.icard.Collection.MyCard.MyCardListActivity;
import com.example.icard.Exchange.ReadCard.ReadCardActivity;
import com.example.icard.Exchange.ReadCard.ReadCardPreviewActivity;
import com.example.icard.Exchange.ShowCard.ShowCardListActivity;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ExchangeActivity extends AppCompatActivity
{
    public Uri targetUri = null;
    public static Card card;

    public int CAMERA_PERMISSION_REQUEST = 1;
    public static int REQUEST_IMAGE_CAPTURE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exchange_layout);

        Button exchangeMenuReadQRButton = findViewById(R.id.exchange_menu_read_QR_button);
        exchangeMenuReadQRButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ExchangeActivity.this, ReadCardActivity.class);
                startActivity(intent);
            }
        });

        Button exchangeMenuReadCardButton = findViewById(R.id.exchange_menu_read_card_button);
        exchangeMenuReadCardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ContextCompat.checkSelfPermission(ExchangeActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(ExchangeActivity.this,
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
                            targetUri = FileProvider.getUriForFile(ExchangeActivity.this,
                                    "com.example.android.fileprovider", photoFile);

                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                }
            }
        });

        Button exchangeMenuShowCardButton = findViewById(R.id.exchange_menu_show_card_button);
        exchangeMenuShowCardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ExchangeActivity.this, ShowCardListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

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

                    card = new Card(url);

                    Intent intent = new Intent(ExchangeActivity.this, ReadCardPreviewActivity.class);
                    intent.putExtra("from", 1);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(ExchangeActivity.this, "Upload unsuccessful",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
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
                        targetUri = FileProvider.getUriForFile(ExchangeActivity.this,
                                "com.example.android.fileprovider", photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        }
    }
}