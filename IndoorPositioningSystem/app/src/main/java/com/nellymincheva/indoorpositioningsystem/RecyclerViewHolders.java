package com.nellymincheva.indoorpositioningsystem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;
public class RecyclerViewHolders extends RecyclerView.ViewHolder{
    public TextView venue;
    Context context;
    private List<Venue> venuesObject;
    public RecyclerViewHolders(final View itemView, final List<Venue> taskObject, Context c) {
        super(itemView);
        context = c;
        this.venuesObject = taskObject;
        venue = (TextView)itemView.findViewById(R.id.venue_name);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String venueId = taskObject.get(getAdapterPosition()).venueId;
                Intent intent = new Intent(context, VenueActivity.class);
                Bundle b = new Bundle();
                b.putString("venueId", venueId);
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
    }
}