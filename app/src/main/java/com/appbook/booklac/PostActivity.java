package com.appbook.booklac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PostActivity extends AppCompatActivity {

    TextView bookDesc, userName, authorName, bookName;
    ImageView bookImg, userPhone;
    String bkName, bkDesc, authName, uName, uPhone, bkImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        initComponent();
        fetchSpesificMovie();
        userPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:".concat(uPhone)));
                startActivity(intent);
            }
        });
    }

    private void initComponent() {
        bookDesc = findViewById(R.id.book_description_fr);
        userName = findViewById(R.id.user_name_fr);
        authorName = findViewById(R.id.book_author_fr);
        bookName = findViewById(R.id.book_name_fr);
        bookImg = findViewById(R.id.book_image_fr);
        userPhone = findViewById(R.id.user_phone_fr);
    }

    private void fetchSpesificMovie() {

        bkName = getIntent().getStringExtra("bookName");
        bookName.setText(bkName);
        bkDesc = getIntent().getStringExtra("bookDescription");
        bookDesc.setText(bkDesc);
        authName = getIntent().getStringExtra("bookAuthor");
        authorName.setText(authName);
        uName = getIntent().getStringExtra("username");
        userName.setText(uName);
        uPhone = getIntent().getStringExtra("userPhone");
        bkImg = getIntent().getStringExtra("bookImg");
        if(!bkImg.equals("noImg")) {
            Picasso.get().load(bkImg).into(bookImg);
        }
        else {
            bookImg.setImageResource(R.drawable.baseline_visibility_off_24);
        }

    }
}