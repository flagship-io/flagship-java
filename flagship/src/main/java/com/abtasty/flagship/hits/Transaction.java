package com.abtasty.flagship.hits;

import com.abtasty.flagship.utils.FlagshipConstants;

import java.util.Currency;
import java.util.Optional;

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

    /**
     * Total revenue associated with the transaction. This value should include any shipping or tax costs. (optional)
     *
     * @param revenue total revenue
     */
    public Transaction withTotalRevenue(float revenue) {
        this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_REVENUE, revenue);
        return this;
    }

    /**
     * Specifies the total shipping cost of the transaction. (optional)
     *
     * @param shipping total of the shipping costs
     */
    public Transaction withShippingCosts(float shipping) {
        this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_SHIPPING, shipping);
        return this;
    }

    /**
     * Specifies the shipping method. (optional)
     *
     * @param shippingMethod shipping method used for the transaction
     */
    public Transaction withShippingMethod(String shippingMethod) {
        if (shippingMethod != null)
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_SHIPPING_METHOD, shippingMethod);
        return this;
    }

    /**
     * Specifies the total taxes of the transaction. (optional)
     *
     * @param taxes total taxes
     *
     */
    public Transaction withTaxes(float taxes) {
        this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_TAX, taxes);
        return this;
    }

    /**
     * Specifies the currency used for all transaction currency values. Value should be a valid ISO 4217 currency code. (optional)
     *
     * @param currency currency used for the transaction
     */
    public Transaction withCurrency(String currency) {
        if (currency != null)
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_CURRENCY, currency);
        return this;
    }

    /**
     * Specifies the payment method for the transaction (optional)
     *
     * @param paymentMethod method used for the payment
     */
    public Transaction withPaymentMethod(String paymentMethod) {
        if (paymentMethod != null)
            this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_PAYMENT_METHOD, paymentMethod);
        return this;
    }

    /**
     * Specifies the number of items for the transaction (optional)
     *
     * @param itemCount number of item
     */
    public Transaction withItemCount(int itemCount) {
        this.data.put(FlagshipConstants.HitKeyMap.TRANSACTION_ITEM_COUNT, itemCount);
        return this;
    }

    /**
     * Specifies the coupon code used by the customer for the transaction (optional)
     *
     * @param coupon coupon code
     */
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
            String currency = this.data.getString(FlagshipConstants.HitKeyMap.TRANSACTION_CURRENCY);
            Optional<Currency> result  = Currency.getAvailableCurrencies().stream().filter(c -> c.getCurrencyCode().equals(currency)).findFirst();
            if (!result.isPresent())
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
