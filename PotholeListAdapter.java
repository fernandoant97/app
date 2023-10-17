package com.example.roomcoord;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

class PotholeViewHolder extends RecyclerView.ViewHolder {
    private final TextView latitudeTextView;
    private final TextView longitudeTextView;
    private final TextView addressTextView;

    private PotholeViewHolder(View itemView) {
        super(itemView);
        latitudeTextView = itemView.findViewById(R.id.latitudeTextView);
        longitudeTextView = itemView.findViewById(R.id.longitudeTextView);
        addressTextView = itemView.findViewById(R.id.addressTextView);
    }

    public void bind(double latitude, double longitude, String address) {
        latitudeTextView.setText("Latitudine: " + latitude);
        longitudeTextView.setText("Longitudine: " + longitude);
        addressTextView.setText("Indirizzo: " + address);
    }

    static PotholeViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pothole_recyclerview_item, parent, false);
        return new PotholeViewHolder(view);
    }
}
    public class PotholeListAdapter extends ListAdapter<Pothole, PotholeViewHolder> {

        public PotholeListAdapter(@NonNull DiffUtil.ItemCallback<Pothole> diffCallback) {
            super(diffCallback);
        }

        @Override
        public PotholeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return PotholeViewHolder.create(parent);
        }

        @Override
        public void onBindViewHolder(PotholeViewHolder holder, int position) {
            Pothole current = getItem(position);
            holder.bind(current.getLatitude(), current.getLongitude(), current.getAddress());
        }

        static class PotholeDiff extends DiffUtil.ItemCallback<Pothole> {

            @Override
            public boolean areItemsTheSame(@NonNull Pothole oldItem, @NonNull Pothole newItem) {
                return oldItem.getPotholeId() == newItem.getPotholeId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Pothole oldItem, @NonNull Pothole newItem) {
                return oldItem.getLatitude() == newItem.getLatitude() &&
                        oldItem.getLongitude() == newItem.getLongitude() &&
                        oldItem.getAddress().equals(newItem.getAddress());
            }
        }
    }

