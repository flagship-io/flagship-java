package com.abtasty.flagship.hits;

import com.abtasty.flagship.utils.FlagshipConstants;

public class Item extends Hit<Item> {


    /**
     * Hit to send an item associated to a transaction. Items must be sent after the corresponding transaction.
     *
     * @param transactionId id of the transaction to link
     * @param productName product name.
     * @param productSku specifies the item code or SKU.
     *
     */
    public Item(String transactionId, String productName, String productSku) {
        super(Type.ITEM);
        if (transactionId != null && productName != null && productSku != null) {
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_ID, transactionId);
            this.data.put(FlagshipConstants.HitKeyMap.ITEM_NAME, productName);
            this.data.put(FlagshipConstants.HitKeyMap.ITEM_CODE, productSku);
        }
    }

    public Item withItemPrice(float price) {
        this.data.put(FlagshipConstants.HitKeyMap.ITEM_PRICE, price);
        return this;
    }

    public Item withItemQuantity(int quantity) {
        this.data.put(FlagshipConstants.HitKeyMap.ITEM_QUANTITY, quantity);
        return this;
    }

    public Item withItemCategory(String category) {
        if (category != null)
            this.data.put(FlagshipConstants.HitKeyMap.ITEM_CATEGORY, category);
        return this;
    }

    @Override
    public boolean checkData() {
        try {
            this.data.getString(FlagshipConstants.HitKeyMap.TRANSACTION_ID);
            this.data.getString(FlagshipConstants.HitKeyMap.ITEM_NAME);
            this.data.getString(FlagshipConstants.HitKeyMap.ITEM_CODE);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
