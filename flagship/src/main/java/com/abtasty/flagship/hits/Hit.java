package com.abtasty.flagship.hits;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.utils.FlagshipConstants;
import org.json.JSONObject;

public abstract class Hit<T> {

    enum Type { SCREENVIEW, PAGEVIEW, TRANSACTION, ITEM, EVENT, ACTIVATION, BATCH, CONSENT }

    private         Type        type;
    protected final JSONObject  data = new JSONObject();

    public Hit(Type type) {

        this.type = type;
        this.data.put(FlagshipConstants.HitKeyMap.CLIENT_ID, Flagship.getConfig().getEnvId());
        if (!(this instanceof Activate)) {
            this.data.put(FlagshipConstants.HitKeyMap.TYPE, type.toString());
            this.data.put(FlagshipConstants.HitKeyMap.DATA_SOURCE, FlagshipConstants.HitKeyMap.APP);
        }
    }

    public JSONObject getData() {
        return data;
    }

    public Type getType() {
        return type;
    }

    public abstract boolean checkData();

    @SuppressWarnings("unchecked")
    public T withIp(String ip) {
        if (ip != null)
            this.data.put(FlagshipConstants.HitKeyMap.IP, ip);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withResolution(int width, int height) {
        if (width > 0 && height > 0)
            this.data.put(FlagshipConstants.HitKeyMap.DEVICE_RESOLUTION, String.format("%dx%d", width, height));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSessionNumber(int number) {
        if (number > 0)
            this.data.put(FlagshipConstants.HitKeyMap.SESSION_NUMBER, number);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLocale(String locale) {
        if (locale != null)
            this.data.put(FlagshipConstants.HitKeyMap.DEVICE_LOCALE, locale);
        return (T) this;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("data", data);
        return  json.toString(2);
    }
}
