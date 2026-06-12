package com.example.projecttwo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    public interface InventoryListener {
        void onItemClicked(InventoryItem item);
        void onDeleteClicked(InventoryItem item);
    }

    private final Context context;
    private final List<InventoryItem> items;
    private final InventoryListener listener;

    public InventoryAdapter(Context context, List<InventoryItem> items, InventoryListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory_card, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = items.get(position);

        holder.textItemName.setText(item.name);
        holder.textItemQty.setText("Qty: " + item.quantity);

        String status;
        if (item.quantity <= 0) status = "Status: Out of stock";
        else if (item.quantity <= 10) status = "Status: Low stock";
        else status = "Status: In stock";

        holder.textItemStatus.setText(status);

        holder.itemView.setOnClickListener(v -> listener.onItemClicked(item));
        holder.buttonDeleteItem.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {

        TextView textItemName, textItemQty, textItemStatus;
        Button buttonDeleteItem;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            textItemQty = itemView.findViewById(R.id.textItemQty);
            textItemStatus = itemView.findViewById(R.id.textItemStatus);
            buttonDeleteItem = itemView.findViewById(R.id.buttonDeleteItem);
        }
    }
}

