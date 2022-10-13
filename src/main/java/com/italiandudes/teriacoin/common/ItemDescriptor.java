package com.italiandudes.teriacoin.common;

import java.util.Objects;

public final class ItemDescriptor {

    //Attributes
    private final String itemID;
    private final String itemName;
    private final double valueTC;

    //Constructors
    public ItemDescriptor(String itemID, String itemName, double valueTC){
        this.itemID = itemID;
        this.itemName = itemName;
        this.valueTC = valueTC;
    }

    //Methods
    public String getItemID(){
        return itemID;
    }
    public String getItemName(){
        return itemName;
    }
    public double getValueTC(){
        return valueTC;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDescriptor that = (ItemDescriptor) o;
        return Double.compare(that.getValueTC(), getValueTC()) == 0 && getItemID().equals(that.getItemID()) && getItemName().equals(that.getItemName());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getItemID(), getItemName(), getValueTC());
    }
    @Override
    public String toString() {
        return itemName+"["+itemID+"]: "+valueTC+"TC";
    }
}
