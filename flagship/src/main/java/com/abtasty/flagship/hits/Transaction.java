package com.abtasty.flagship.hits;

import com.abtasty.flagship.utils.FlagshipConstants;

public class Transaction extends Hit<Transaction> {


    /**
     * Hit to send when a user complete a transaction.
     *
     *  @param transactionId transaction unique identifier.
     *  @param affiliation affiliation name.
     *
     */
    public Transaction(String transactionId, String affiliation) {
        super(Type.TRANSACTION);
        if (transactionId != null && affiliation != null) {
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_ID, transactionId);
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_AFFILIATION, affiliation);
        }
    }

    public Transaction withTotalRevenue(float revenue) {
        this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_REVENUE, revenue);
        return this;
    }

    public Transaction withShippingCosts(float shipping) {
        this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_SHIPPING, shipping);
        return this;
    }

    public Transaction withShippingMethod(String shippingMethod) {
        if (shippingMethod != null)
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_SHIPPING_METHOD, shippingMethod);
        return this;
    }

    public Transaction withTaxes(float taxes) {
        this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_TAX, taxes);
        return this;
    }

    public Transaction withCurrency(String currency) {
        if (currency != null)
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_CURRENCY, currency);
        return this;
    }

    public Transaction withPaymentMethod(String paymentMethod) {
        if (paymentMethod != null)
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_PAYMENT_METHOD, paymentMethod);
        return this;
    }

    public Transaction withItemCount(int itemCount) {
        this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_ITEM_COUNT, itemCount);
        return this;
    }

    public Transaction withCouponCode(String coupon) {
        if (coupon != null)
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_COUPON, coupon);
        return this;
    }


    @Override
    public boolean checkData() {
        try {
            this.data.getString(FlagshipConstants.HitKeyMap.TRANSACTION_ID);
            this.data.getString(FlagshipConstants.HitKeyMap.TRANSACTION_AFFILIATION);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
