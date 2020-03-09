package com.example.icard.Exchange.ShowCard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.icard.Card;
import com.example.icard.MainActivity;
import com.example.icard.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ShowCardActivity extends AppCompatActivity
{
    public ImageView showCardImage;
    public Button showCardModeButton;

    public static int index;
    public static int mode;
    public static Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_card_layout);

        showCardImage = findViewById(R.id.show_card_image);
        showCardModeButton = findViewById(R.id.show_card_mode_button);

        Intent intent = getIntent();
        index = intent.getIntExtra("index", -1);
        card = ShowCardListActivity.cList.get(index);

        SharedPreferences sharedPref = this.getSharedPreferences("MySettings", Context.MODE_PRIVATE);
        mode = sharedPref.getInt("Mode", 0);

        if (mode == 0)
        {
            showCardModeButton.setText("Photo");

            String ID = card.ID;

            if (!ID.equals(""))
            {
                String code = MainActivity.userID + " " + ID;

                QRCodeWriter writer = new QRCodeWriter();

                try
                {
                    BitMatrix bitMatrix = writer.encode(code, BarcodeFormat.QR_CODE, 512, 512);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    showCardImage.setImageBitmap(bmp);
                }
                catch (WriterException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            showCardModeButton.setText("QR");

            String url = card.url;

            Glide.with(this).load(url).into(showCardImage);
        }

        showCardModeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mode = 1 - mode;

                SharedPreferences sharedPref = ShowCardActivity.this.getSharedPreferences("MySettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("Mode", mode);
                editor.commit();

                if (mode == 0)
                {
                    showCardModeButton.setText("Photo");

                    String ID = card.ID;

                    if (!ID.equals(""))
                    {
                        String code = MainActivity.userID + " " + ID;

                        QRCodeWriter writer = new QRCodeWriter();

                        try
                        {
                            BitMatrix bitMatrix = writer.encode(code, BarcodeFormat.QR_CODE, 512, 512);
                            int width = bitMatrix.getWidth();
                            int height = bitMatrix.getHeight();
                            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                            for (int x = 0; x < width; x++) {
                                for (int y = 0; y < height; y++) {
                                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                                }
                            }

                            showCardImage.setImageBitmap(bmp);
                        }
                        catch (WriterException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                else
                {
                    showCardModeButton.setText("QR");

                    String url = card.url;

                    Glide.with(ShowCardActivity.this).load(url).into(showCardImage);
                }
            }
        });
    }
}