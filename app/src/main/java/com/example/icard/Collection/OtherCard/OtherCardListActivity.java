package com.example.icard.Collection.OtherCard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.icard.Card;
import com.example.icard.MainActivity;
import com.example.icard.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class OtherCardListActivity extends AppCompatActivity
{
    public static List<Card> cList;
    public static List<Card> cListCur;

    public static RecyclerView cardList;
    public EditText otherCardListSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_card_list_layout);

        otherCardListSearch = findViewById(R.id.other_card_list_search_text);

        cardList = findViewById(R.id.other_card_list_recycler);
        cardList.setLayoutManager(new LinearLayoutManager(OtherCardListActivity.this));

        Button otherCardListSearchButton = findViewById(R.id.other_card_list_search_button);
        otherCardListSearchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String search = otherCardListSearch.getText().toString();

                if (search.equals(""))
                {
                    cListCur = new ArrayList<>(cList);
                }
                else
                {
                    cListCur = new ArrayList<>();

                    for (Card card : cList)
                    {
                        boolean match = false;

                        for (String metaItem : card.meta)
                        {
                            if (metaItem.contains(search))
                            {
                                match = true;
                                break;
                            }
                        }

                        for (String noteItem : card.notes)
                        {
                            if (noteItem.contains(search))
                            {
                                match = true;
                                break;
                            }
                        }

                        if (match)
                        {
                            cListCur.add(card);
                        }
                    }
                }

                OtherCardListAdapter otherCardListAdapter = new OtherCardListAdapter(cListCur, OtherCardListActivity.this);
                cardList.setAdapter(otherCardListAdapter);
            }
        });

        cList = new ArrayList<>();

        try
        {
            FileInputStream fis = openFileInput("otherCardFile");
            ObjectInputStream ois = new ObjectInputStream(fis);

            try
            {
                int len = (int) ois.readObject();

                for (int i = 0; i < len; ++i)
                {
                    List<String> metadata = new ArrayList<>();

                    int tLen = (int) ois.readObject();

                    for (int j = 0; j < tLen; ++j)
                    {
                        String tMeta = (String) ois.readObject();
                        metadata.add(tMeta);
                    }

                    List<String> notes = new ArrayList<>();

                    int tLen2 = (int) ois.readObject();

                    for (int j = 0; j < tLen2; ++j)
                    {
                        String tNotes = (String) ois.readObject();
                        notes.add(tNotes);
                    }

                    String url = (String) ois.readObject();

                    cList.add(new Card(url, metadata, notes));
                }
            }
            catch (Exception e)
            {
                Log.e("iMeal", e.toString());
            }
        }
        catch (Exception e)
        {
            Log.e("iMeal", "Can't read");
        }

        cListCur = new ArrayList<>(cList);

        OtherCardListAdapter otherCardListAdapter = new OtherCardListAdapter(cListCur, OtherCardListActivity.this);
        cardList.setAdapter(otherCardListAdapter);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference otherRef = mDatabase.child(MainActivity.userID).child("other-cards");
        otherRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                cList = new ArrayList<>();

                for (DataSnapshot cardSnapshot : dataSnapshot.getChildren())
                {
                    String url = cardSnapshot.child("url").getValue().toString();

                    List<String> metadata = new ArrayList<>();

                    for (DataSnapshot metadataSnapshot : cardSnapshot.child("metadata").getChildren())
                    {
                        String curMetadata = metadataSnapshot.getValue().toString();
                        metadata.add(curMetadata);
                    }

                    List<String> notes = new ArrayList<>();

                    for (DataSnapshot notesSnapshot : cardSnapshot.child("notes").getChildren())
                    {
                        String curNotes = notesSnapshot.getValue().toString();
                        notes.add(curNotes);
                    }

                    String ID = cardSnapshot.getKey();

                    cList.add(new Card(url, metadata, notes, ID));
                }

                try
                {
                    FileOutputStream fos = openFileOutput("otherCardFile", Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);

                    int len = cList.size();
                    oos.writeObject(len);

                    for (Card cTemp : cList)
                    {
                        int tLen = cTemp.meta.size();
                        oos.writeObject(tLen);

                        for (String tMeta : cTemp.meta)
                        {
                            oos.writeObject(tMeta);
                        }

                        int tLen2 = cTemp.notes.size();
                        oos.writeObject(tLen2);

                        for (String tNotes : cTemp.notes)
                        {
                            oos.writeObject(tNotes);
                        }

                        oos.writeObject(cTemp.url);
                    }

                    oos.close();
                }
                catch (Exception e)
                {
                    Log.e("iMeal", "Can't write");
                }

                cListCur = new ArrayList<>(cList);

                OtherCardListAdapter otherCardListAdapter = new OtherCardListAdapter(cListCur, OtherCardListActivity.this);
                cardList.setAdapter(otherCardListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e("iCard", databaseError.toException().toString());
            }
        });
    }
}