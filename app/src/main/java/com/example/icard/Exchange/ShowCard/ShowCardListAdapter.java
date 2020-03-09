package com.example.icard.Exchange.ShowCard;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.icard.Card;
import com.example.icard.CardListViewHolder;
import com.example.icard.R;

import java.util.List;

public class ShowCardListAdapter extends RecyclerView.Adapter<CardListViewHolder>
{
    List<Card> cList;
    Context context;

    ShowCardListAdapter(List<Card> cList, Context context)
    {
        this.cList = cList;
        this.context = context;
    }

    @Override
    public CardListViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        final View itemView = inflater.inflate(R.layout.card_list_item, parent, false);

        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int position = ShowCardListActivity.cardList.getChildLayoutPosition(v);

                Intent intent = new Intent(itemView.getContext(), ShowCardActivity.class);

                intent.putExtra("index", position);

                itemView.getContext().startActivity(intent);
            }
        });

        return new CardListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CardListViewHolder holder, int position)
    {
        Card c = cList.get(position);

        Glide.with(context).load(c.url).into(holder.image);

        if (c.meta.size() > 0)
            holder.text.setText(c.meta.get(0));

        if (c.meta.size() > 1)
            holder.text2.setText(c.meta.get(1));
    }

    @Override
    public int getItemCount()
    {
        return cList.size();
    }
}