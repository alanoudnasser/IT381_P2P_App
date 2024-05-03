package com.example.wifidirect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifidirect.MainActivity;
import com.example.wifidirect.R;
import com.example.wifidirect.models.BookModel;

import java.util.ArrayList;

public class BookNamesAdapter extends RecyclerView.Adapter<BookNamesAdapter.MyViewHolder> {


    Context mContext;
    BookModel bookModel;
    MainActivity mainActivity;
    public BookNamesAdapter(Context mContext, MainActivity mainActivity,BookModel bookModel){
        this.mContext = mContext;
        this.bookModel = bookModel;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public BookNamesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookNamesAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.book_name, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookNamesAdapter.MyViewHolder holder, int position) {
        holder.bookNameDelIV.setOnClickListener(view->{
            mainActivity.deleteBookNameFromList(bookModel.bookNames.get(position));
        });


        holder.bookNameTV.setText(bookModel.bookNames.get(position));
    }

    @Override
    public int getItemCount() {
        return bookModel.bookNames.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView bookNameTV;
        ImageView bookNameDelIV;

        MyViewHolder(View view) {
            super(view);

            bookNameTV = view.findViewById(R.id.bookNameTV);

            bookNameDelIV = view.findViewById(R.id.bookNameDelIV);
        }
    }
}
