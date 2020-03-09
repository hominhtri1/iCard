package com.example.icard.Collection.MyCard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class MyCardListActivity extends AppCompatActivity
{
    public static List<Card> cList;
    public static RecyclerView cardList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_card_list_layout);

        cardList = findViewById(R.id.my_card_list_recycler);
        cardList.setLayoutManager(new LinearLayoutManager(MyCardListActivity.this));

        Button myCardListAddButton = findViewById(R.id.my_card_list_add_button);
        myCardListAddButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MyCardListActivity.this, MyCardActivity.class);
                startActivity(intent);
            }
        });

        cList = new ArrayList<>();

        try
        {
            FileInputStream fis = openFileInput("myCardFile");
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

                    String url = (String) ois.readObject();

                    cList.add(new Card(url, metadata));
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

        MyCardListAdapter myCardListAdapter = new MyCardListAdapter(cList, MyCardListActivity.this);
        cardList.setAdapter(myCardListAdapter);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = mDatabase.child(MainActivity.userID).child("my-card");
        myRef.addValueEventListener(new ValueEventListener()
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

                    String ID = cardSnapshot.getKey();

                    cList.add(new Card(url, metadata, ID));
                }

                try
                {
                    FileOutputStream fos = openFileOutput("myCardFile", Context.MODE_PRIVATE);
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

                        oos.writeObject(cTemp.url);
                    }

                    oos.close();
                }
                catch (Exception e)
                {
                    Log.e("iMeal", "Can't write");
                }

                MyCardListAdapter myCardListAdapter = new MyCardListAdapter(cList, MyCardListActivity.this);
                cardList.setAdapter(myCardListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e("iCard", databaseError.toException().toString());
            }
        });
    }
}