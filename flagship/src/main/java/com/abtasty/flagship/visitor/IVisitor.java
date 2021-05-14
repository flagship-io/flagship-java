package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipContext;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for public visitor methods.
 */
public interface IVisitor {

    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     * <p>
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context keys must be String, and values types must be one of the following : Number, Boolean, String.
     *
     * @param context: HashMap of keys, values.
     */
    void updateContext(HashMap<String, Object> context);

    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     * <p>
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context key must be String, and value type must be one of the following : Number, Boolean, String.
     *
     * @param key:  context key.
     * @param value context value.
     */
    <T> void updateContext(String key, T value);

    /**
     * Update the visitor context values, matching the given predefined key, used for targeting.
     * <p>
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context key must be String, and value type must be one of the following : Number, Boolean, String.
     *
     * @param flagshipContext:  Predefined context key.
     * @param value context value.
     */
    <T> void updateContext(FlagshipContext<T> flagshipContext, T value);

    /**
     * Clear all the visitor context values used for targeting.
     */
    public void clearContext();

    /**
     * This function will call the decision api and update all the campaigns modifications from the server according to the visitor context.
     *
     * @return a CompletableFuture for this synchronization
     */
    CompletableFuture<Visitor> synchronizeModifications();

    /**
     * Retrieve a modification value by its key. If no modification match the given key, default value will be returned.
     *
     * @param key          key associated to the modification.
     * @param defaultValue default value to return.
     * @return modification value or default value.
     */
    <T> T getModification(String key, T defaultValue);

    /**
     * Retrieve a modification value by its key. If no modification match the given key or if the stored value type and default value type do not match, default value will be returned.
     *
     * @param key          key associated to the modification.
     * @param defaultValue default value to return.
     * @param activate     Set this parameter to true to automatically report on our server that the
     *                     current visitor has seen this modification. It is possible to call activateModification() later.
     * @return modification value or default value.
     */
    <T> T getModification(String key, T defaultValue, boolean activate);

    /**
     * Get the campaign modification information value matching the given key.
     *
     * @param key key which identify the modification.
     * @return JSONObject containing the modification information.
     */
    JSONObject getModificationInfo(String key);

    /**
     * Report this user has seen this modification.
     *
     * @param key key which identify the modification to activate.
     */
    void activateModification(String key);

    /**
     * Send a Hit to Flagship servers for reporting.
     *
     * @param hit hit to track.
     */
    <T> void sendHit(Hit<T> hit);

}
