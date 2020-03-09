package com.example.icard.Collection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.icard.Collection.MyCard.MyCardListActivity;
import com.example.icard.Collection.OtherCard.OtherCardListActivity;
import com.example.icard.R;

public class CollectionActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_layout);

        Button collectionMenuMyCardButton = findViewById(R.id.collection_menu_my_card_button);
        collectionMenuMyCardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(CollectionActivity.this, MyCardListActivity.class);
                startActivity(intent);
            }
        });

        Button collectionMenuOtherCardsButton = findViewById(R.id.collection_menu_other_cards_button);
        collectionMenuOtherCardsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(CollectionActivity.this, OtherCardListActivity.class);
                startActivity(intent);
            }
        });
    }
}