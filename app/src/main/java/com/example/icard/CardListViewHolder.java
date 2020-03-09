package com.example.icard;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class CardListViewHolder extends RecyclerView.ViewHolder
{
    public ImageView image;
    public TextView text;
    public TextView text2;

    public CardListViewHolder(View view)
    {
        super(view);

        image = view.findViewById(R.id.card_list_item_image);
        text = view.findViewById(R.id.card_list_item_text);
        text2 = view.findViewById(R.id.card_list_item_text_2);
    }
}