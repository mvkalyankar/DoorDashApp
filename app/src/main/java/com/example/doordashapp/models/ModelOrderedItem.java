package com.example.doordashapp.models;

public class ModelOrderedItem {

    private String fId, name, cost, price, quantity;

    public ModelOrderedItem() {
    }

    public ModelOrderedItem(String fId, String name, String cost, String price, String quantity) {
        this.fId = fId;
        this.name = name;
        this.cost = cost;
        this.price = price;
        this.quantity = quantity;
    }

    public String getfId() {
        return fId;
    }

    public void setfId(String fId) {
        this.fId = fId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
