package com.example.icard.Collection.OtherCard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.icard.MainActivity;
import com.example.icard.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OtherCardInfoActivity extends AppCompatActivity
{
    public LinearLayout otherCardInfoNotesList;
    public EditText otherCardInfoNotes;

    public List<String> notes;
    public String ID;
    public List<EditText> notesViewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_card_info_layout);

        otherCardInfoNotesList = findViewById(R.id.other_card_info_notes_list);
        otherCardInfoNotes = findViewById(R.id.other_card_info_notes);

        Intent intent = getIntent();
        int index = intent.getIntExtra("index", -1);
        Card card = OtherCardListActivity.cListCur.get(index);

        notes = new ArrayList<>(card.notes);
        ID = card.ID;

        ImageView otherCardInfoImage = findViewById(R.id.other_card_info_image);
        Glide.with(OtherCardInfoActivity.this).load(card.url).into(otherCardInfoImage);

        LinearLayout otherCardInfoMetadataList = findViewById(R.id.other_card_info_metadata_list);

        for (String curMetadata : card.meta)
        {
            TextView metadataView = new TextView(OtherCardInfoActivity.this);

            metadataView.setText(curMetadata);
            metadataView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            metadataView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            otherCardInfoMetadataList.addView(metadataView);
        }

        for (String curNotes : notes)
        {
            EditText notesView = new EditText(OtherCardInfoActivity.this);

            notesView.setText(curNotes);
            notesView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            notesView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            otherCardInfoNotesList.addView(notesView);

            notesViewList.add(notesView);
        }

        Button otherCardInfoAddNotesButton = findViewById(R.id.other_card_info_add_notes_button);
        otherCardInfoAddNotesButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String curNotes = otherCardInfoNotes.getText().toString();

                if (!curNotes.equals(""))
                {
                    notes.add(curNotes);

                    otherCardInfoNotes.setText("");

                    EditText notesView = new EditText(OtherCardInfoActivity.this);

                    notesView.setText(curNotes);
                    notesView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                    notesView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    otherCardInfoNotesList.addView(notesView);

                    notesViewList.add(notesView);
                }
            }
        });

        Button otherCardInfoUpdateButton = findViewById(R.id.other_card_info_update_button);
        otherCardInfoUpdateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notes = new ArrayList<>();

                for (EditText notesView : notesViewList)
                {
                    String curNotes = notesView.getText().toString();
                    notes.add(curNotes);
                }

                if (ID.equals(""))
                    return;

                DatabaseReference mDatabase =
                        FirebaseDatabase.getInstance().getReference();
                DatabaseReference myRef = mDatabase.child(MainActivity.userID).child("other-cards").child(ID);

                myRef.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot notesSnapshot : dataSnapshot.child("notes").getChildren())
                        {
                            notesSnapshot.getRef().removeValue();
                        }

                        for (String curNotes : notes)
                        {
                            if (!curNotes.equals(""))
                            {
                                dataSnapshot.getRef().child("notes").push().setValue(curNotes);
                            }
                        }

                        Intent intent = new Intent(OtherCardInfoActivity.this, OtherCardListActivity.class);
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

        Button otherCardInfoDeleteButton = findViewById(R.id.other_card_info_delete_button);
        otherCardInfoDeleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ID.equals(""))
                    return;

                DatabaseReference mDatabase =
                        FirebaseDatabase.getInstance().getReference();
                DatabaseReference otherRef = mDatabase.child(MainActivity.userID).child("other-cards").child(ID);

                otherRef.removeValue();

                Intent intent = new Intent(OtherCardInfoActivity.this, OtherCardListActivity.class);
                startActivity(intent);
            }
        });
    }
}