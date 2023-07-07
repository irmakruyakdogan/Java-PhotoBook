package com.yirmak.photobook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yirmak.photobook.databinding.RecyclerNewBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtH> {

    ArrayList<art> artArrayList;

    public ArtAdapter(ArrayList<art> artArrayList) {
        this.artArrayList = artArrayList;
    }

    @NonNull
    @Override
    public ArtH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerNewBinding recyclerNewBinding = RecyclerNewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ArtH(recyclerNewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtH holder, int position) {
        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),PhotoActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("artId",artArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }

    public class ArtH extends RecyclerView.ViewHolder {

        private RecyclerNewBinding binding;

        public ArtH(RecyclerNewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
