package com.example.travelmantics;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends  RecyclerView.Adapter<DealAdapter.DealViewHolder>{
    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;
    private ImageView imageDeal;

    public DealAdapter(){

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference  = FirebaseUtil.mDatabaseReference;
        deals = FirebaseUtil.mDeals;

        mChildListener  = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal td =  dataSnapshot.getValue(TravelDeal.class);
                td.setId(dataSnapshot.getKey());
                Log.d("Deal",td.getTitle());
                deals.add(td);
                notifyItemChanged(deals.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildListener);
    }
    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.rv_row,parent,false);
        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
          TravelDeal deal = deals.get(position);
          holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class  DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView trvTitle;
        TextView trvDescription;
        TextView trvPrice;

        public DealViewHolder(@NonNull View itemView){
            super(itemView);
            trvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            trvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            trvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            imageDeal = (ImageView) itemView.findViewById(R.id.imageDeal);
            itemView.setOnClickListener(this);

        }

        public void bind(TravelDeal deal){
            trvTitle.setText(deal.getTitle());
            trvDescription.setText(deal.getDescription());
            trvPrice.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TravelDeal selectedDeal = deals.get(position);
        Intent intent =  new Intent(view.getContext(), AddDealActivity.class);
        intent.putExtra("Deal", selectedDeal);
        view.getContext().startActivity(intent);
        }
    }

    public void showImage(String url){
        if(url != null && url.isEmpty() == false){
            Picasso.with(imageDeal.getContext())
                    .load(url)
                    .resize(175,175)
                    .centerCrop()
                    .into(imageDeal);
        }
    }
}
