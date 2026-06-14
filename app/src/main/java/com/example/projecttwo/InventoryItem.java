package com.example.projecttwo;

public class InventoryItem {
    public long id;
    public String name;
    public int quantity;
    public String category;
    public int minimumStock;
    public String lastUpdated;

    public InventoryItem(long id, String name, int quantity, String category, int minimumStock, String lastUpdated) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.minimumStock = minimumStock;
        this.lastUpdated = lastUpdated;
    }
}
