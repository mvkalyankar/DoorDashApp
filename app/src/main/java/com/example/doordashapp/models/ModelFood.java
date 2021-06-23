package com.example.doordashapp.models;

public class ModelFood {
    private String foodId, foodTitle, foodDescription, foodQuantity, foodIcon, originalPrice, discountPrice, discountAvailable, discountNote, timestamp, uid;

    public ModelFood() {
    }

    public ModelFood(String foodId, String foodTitle, String foodDescription, String foodQuantity, String foodIcon, String originalPrice,
                     String discountPrice, String discountAvailable, String discountNote, String timestamp, String uid) {
        this.foodId = foodId;
        this.foodTitle = foodTitle;
        this.foodDescription = foodDescription;
        this.foodQuantity = foodQuantity;
        this.foodIcon = foodIcon;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.discountAvailable = discountAvailable;
        this.discountNote = discountNote;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodTitle() {
        return foodTitle;
    }

    public void setFoodTitle(String foodTitle) {
        this.foodTitle = foodTitle;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public String getFoodQuantity() {
        return foodQuantity;
    }

    public void setFoodQuantity(String foodQuantity) {
        this.foodQuantity = foodQuantity;
    }

    public String getFoodIcon() {
        return foodIcon;
    }

    public void setFoodIcon(String foodIcon) {
        this.foodIcon = foodIcon;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(String discountPrice) {
        this.discountPrice = discountPrice;
    }

    public String getDiscountAvailable() {
        return discountAvailable;
    }

    public void setDiscountAvailable(String discountAvailable) {
        this.discountAvailable = discountAvailable;
    }

    public String getDiscountNote() {
        return discountNote;
    }

    public void setDiscountNote(String discountNote) {
        this.discountNote = discountNote;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
