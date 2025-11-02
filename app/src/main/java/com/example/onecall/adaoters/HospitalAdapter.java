package com.example.onecall.adaoters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.onecall.R;
import com.example.onecall.models.HospitalData;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {
    private List<HospitalData> hospitalList;

    private OnHospitalClickListener listener;

    public interface OnHospitalClickListener {
        void onHospitalClick(HospitalData hospital);
    }

    public HospitalAdapter(List<HospitalData> hospitalList, OnHospitalClickListener listener) {
        this.hospitalList = hospitalList;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hospital_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HospitalData hospital = hospitalList.get(position);
        holder.name.setText(hospital.name);
        holder.address.setText(hospital.address);
        holder.itemView.setOnClickListener(v -> listener.onHospitalClick(hospital));
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.hospital_name);
            address = itemView.findViewById(R.id.hospital_address);
        }
    }
}
