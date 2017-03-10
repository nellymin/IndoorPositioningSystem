package com.nellymincheva.indoorpositioningsystem;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolders> {
    private List<Venue> venues;
    protected Context context;
    public RecyclerViewAdapter(Context context, List<Venue> venues) {
        this.venues = venues;
        this.context = context;
    }
    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerViewHolders viewHolder = null;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.venues_list, parent, false);
        viewHolder = new RecyclerViewHolders(layoutView, venues, context);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        holder.venue.setText(venues.get(position).name);
    }
    @Override
    public int getItemCount() {
        return this.venues.size();
    }
}