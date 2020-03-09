package com.example.icard;

import java.util.ArrayList;
import java.util.List;

public class Card
{
    public String url;
    public List<String> meta;
    public List<String> notes;
    public String ID = "";

    public Card(String url, List<String> meta, List<String> notes, String ID)
    {
        this.url = url;
        this.meta = new ArrayList<>(meta);
        this.notes = new ArrayList<>(notes);
        this.ID = ID;
    }

    public Card(String url, List<String> meta, List<String> notes)
    {
        this.url = url;
        this.meta = new ArrayList<>(meta);
        this.notes = new ArrayList<>(notes);
    }

    public Card(String url, List<String> meta, String ID)
    {
        this.url = url;
        this.meta = new ArrayList<>(meta);
        this.ID = ID;
    }

    public Card(String url, List<String> meta)
    {
        this.url = url;
        this.meta = new ArrayList<>(meta);
    }

    public Card(String url)
    {
        this.url = url;
        this.meta = new ArrayList<>();
    }
}