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

public class BookCategoryAdapter extends RecyclerView.Adapter<BookCategoryAdapter.MyViewHolder> {

    Context mContext;
    ArrayList<BookModel> bookCategories;
    MainActivity mainActivity;
    public BookCategoryAdapter(Context mContext, MainActivity mainActivity, ArrayList<BookModel> bookCategories){
        this.mContext = mContext;
        this.bookCategories = bookCategories;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public BookCategoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.book_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookCategoryAdapter.MyViewHolder holder, int position) {
        holder.categoryNameTV.setText(bookCategories.get(position).categoryName);

        if(bookCategories.get(position).bookNames.size()>0){
            holder.bookNameTV.setText(bookCategories.get(position).bookNames.toString());
        }else{
            holder.bookNameTV.setText("No Book added yet");
        }


        holder.categoryNameTV.setOnClickListener(view->{
            mainActivity.addBookNames(bookCategories.get(position));
        });
        holder.categoryDelIV.setOnClickListener(view->{
            mainActivity.deleteCategory(bookCategories.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return bookCategories.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTV,bookNameTV;
        ImageView categoryDelIV;


        MyViewHolder(View view) {
            super(view);

            categoryNameTV = view.findViewById(R.id.categoryNameTV);
            bookNameTV = view.findViewById(R.id.bookNameTV);
            categoryDelIV = view.findViewById(R.id.categoryDelIV);
        }
    }
}
