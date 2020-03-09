package com.example.icard.Exchange.ReadCard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.icard.Card;
import com.example.icard.Exchange.ExchangeActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ReadCardActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler
{
    public ZXingScannerView mScannerView = null;

    public int CAMERA_PERMISSION_REQUEST = 1;

    public static Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(ReadCardActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(ReadCardActivity.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
        else
        {
            mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
            setContentView(mScannerView);
            mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            mScannerView.startCamera();          // Start camera on resume
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == CAMERA_PERMISSION_REQUEST)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
                setContentView(mScannerView);
                mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
                mScannerView.startCamera();          // Start camera on resume
            }
            else
            {
                Intent intent = new Intent(ReadCardActivity.this, ExchangeActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if  (mScannerView != null)
        {
            mScannerView.stopCamera();           // Stop camera on pause
        }
    }

    @Override
    public void handleResult(Result rawResult)
    {
        String code = rawResult.getText();
        String[] codeSplit = code.split(" ");

        String userID = codeSplit[0];
        String ID = codeSplit[1];

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = mDatabase.child(userID).child("my-card").child(ID);
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Toast.makeText(ReadCardActivity.this, "Processing", Toast.LENGTH_LONG).show();

                String url = dataSnapshot.child("url").getValue().toString();

                List<String> metadata = new ArrayList<>();

                for (DataSnapshot metadataSnapshot : dataSnapshot.child("metadata").getChildren())
                {
                    String curMetadata = metadataSnapshot.getValue().toString();
                    metadata.add(curMetadata);
                }

                card = new Card(url, metadata);

                Intent intent = new Intent(ReadCardActivity.this, ReadCardPreviewActivity.class);
                intent.putExtra("from", 0);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e("iCard", databaseError.toException().toString());
            }
        });
    }
}