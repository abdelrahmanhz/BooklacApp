package com.appbook.booklac.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appbook.booklac.Books;
import com.appbook.booklac.PostActivity;
import com.appbook.booklac.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder>{

    Context context;

    List<Books> booksList;

    public PostAdapter(Context context, ArrayList<Books> booksList) {
        this.context = context;
        this.booksList = booksList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_book, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String uId = booksList.get(position).getUserId();
        String uName = booksList.get(position).getUserName();
        String uEmail = booksList.get(position).getUserEmail();
        String uDp = booksList.get(position).getUserDp();
        String pId = booksList.get(position).getPostId();
        String bName = booksList.get(position).getBookName();
        String bAuthor = booksList.get(position).getBookAuthor();
        String bDescription = booksList.get(position).getBookDescription();
        String bImg = booksList.get(position).getBookImg();
        String pTime = booksList.get(position).getPostTime();
        String uPhone = booksList.get(position).getuPhone();

        holder.userNameRv.setText(uName);
        holder.bookNameRv.setText(bName);
        holder.bookAuthorRv.setText(bAuthor);

            try {
                if(!bImg.equals("noImg")) {
                    Picasso.get().load(bImg).into(holder.bookImgRv);
                }
                else{
                    holder.bookImgRv.setImageResource(R.drawable.baseline_visibility_off_24);
                }
            }
            catch (Exception e){

            }




    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        TextView userNameRv, bookNameRv, bookAuthorRv;
        ImageView bookImgRv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            userNameRv = itemView.findViewById(R.id.user_name_rv);
            bookNameRv = itemView.findViewById(R.id.book_name_rv);
            bookAuthorRv = itemView.findViewById(R.id.book_author_rv);
            bookImgRv = itemView.findViewById(R.id.book_img_rv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(v.getContext(), PostActivity.class);
                    intent.putExtra("username", booksList.get(getAdapterPosition()).getUserName());
                    intent.putExtra("bookName", booksList.get(getAdapterPosition()).getBookName());
                    intent.putExtra("bookAuthor", booksList.get(getAdapterPosition()).getBookAuthor());
                    intent.putExtra("bookDescription", booksList.get(getAdapterPosition()).getBookDescription());
                    intent.putExtra("bookImg", booksList.get(getAdapterPosition()).getBookImg());
                    intent.putExtra("userPhone", booksList.get(getAdapterPosition()).getuPhone());
                    v.getContext().startActivity(intent);

                }
            });
        }
    }
}
