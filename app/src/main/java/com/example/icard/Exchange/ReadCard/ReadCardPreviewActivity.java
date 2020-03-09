package com.example.icard.Exchange.ReadCard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.icard.Card;
import com.example.icard.Exchange.ExchangeActivity;
import com.example.icard.MainActivity;
import com.example.icard.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ReadCardPreviewActivity extends AppCompatActivity
{
    public EditText readCardPreviewNotes;
    public LinearLayout readCardPreviewNotesList;

    public List<String> notes = new ArrayList<>();
    public Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_card_preview_layout);

        readCardPreviewNotes = findViewById(R.id.read_card_preview_notes);
        readCardPreviewNotesList = findViewById(R.id.read_card_preview_notes_list);

        Intent intent = getIntent();
        int index = intent.getIntExtra("from", -1);

        if (index == 0)
            card = ReadCardActivity.card;
        else
            card = ExchangeActivity.card;

        ImageView readCardPreviewImage = findViewById(R.id.read_card_preview_image);
        Glide.with(ReadCardPreviewActivity.this).load(card.url).into(readCardPreviewImage);

        LinearLayout readCardPreviewMetadataList =
                findViewById(R.id.read_card_preview_metadata_list);

        for (String curMetadata : card.meta)
        {
            TextView metadataView = new TextView(ReadCardPreviewActivity.this);

            metadataView.setText(curMetadata);
            metadataView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            metadataView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            readCardPreviewMetadataList.addView(metadataView);
        }

        Button readCardPreviewAddNotesButton = findViewById(R.id.read_card_preview_add_notes_button);
        readCardPreviewAddNotesButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String curNotes = readCardPreviewNotes.getText().toString();

                if (!curNotes.equals(""))
                {
                    notes.add(curNotes);

                    readCardPreviewNotes.setText("");

                    TextView metadataView = new TextView(ReadCardPreviewActivity.this);

                    metadataView.setText(curNotes);
                    metadataView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                    metadataView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    readCardPreviewNotesList.addView(metadataView);
                }
            }
        });

        Button readCardPreviewOKButton = findViewById(R.id.read_card_preview_OK_button);
        readCardPreviewOKButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                DatabaseReference otherRef = mDatabase.child(MainActivity.userID).child("other-cards").push();

                otherRef.child("url").setValue(card.url);

                for (String curMetadata : card.meta)
                {
                    otherRef.child("metadata").push().setValue(curMetadata);
                }

                for (String curNotes : notes)
                {
                    otherRef.child("notes").push().setValue(curNotes);
                }

                Intent intent = new Intent(
                        ReadCardPreviewActivity.this, ExchangeActivity.class);
                startActivity(intent);
            }
        });
    }
}