package com.abtasty.flagship.cache;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * This interface specifies the methods to implement in order to cache visitors.<br/>
 *
 * Visitor cache is used for :
 * - Retrieve campaign flags while offline.
 * - Prevent re-allocation in bucketing mode.
 * - Specific features.
 */
public interface IVisitorCacheImplementation {

    /**
     * This method is called when the SDK need to upsert (insert or update) current visitor information into the database.
     *
     * @param visitorId unique visitor identifier whom information need to be cached.
     * @param data visitor information to store in your database.
     */
    void cacheVisitor(String visitorId, JSONObject data) throws InterruptedException;

    /**
     * This method is called when the SDK need to load visitor information from the database.
     *
     * @param visitorId unique visitor identifier whom information need to be loaded from the database.
     */
    JSONObject lookupVisitor(String visitorId);

    /**
     * This method is called when visitor information should be cleared from the database.
     *
     * @param visitorId unique visitor identifier whom cached information need to be cleared from the database.
     */
    void flushVisitor(String visitorId);

}
