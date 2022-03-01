package com.abtasty.flagship.visitor;

import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.cache.*;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Consent;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Flag;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipContext;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Visitor default method strategy
 */
class DefaultStrategy extends VisitorStrategy {

    public DefaultStrategy(VisitorDelegate visitor) {
        super(visitor);
    }

    @Override
    public void updateContext(HashMap<String, Object> context) {
        if (context != null) {
            for (HashMap.Entry<String, Object> e : context.entrySet()) {
                this.updateContext(e.getKey(), e.getValue());
            }
        }
        visitorDelegate.logVisitor(FlagshipLogManager.Tag.UPDATE_CONTEXT);
    }

    @Override
    public <T> void updateContext(String key, T value) {
        if (key == null)
            FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, LogManager.Level.ERROR, FlagshipConstants.Errors.CONTEXT_KEY_ERROR);
//        else if (((value instanceof String) || (value instanceof Number) || (value instanceof Boolean) || (value instanceof JSONObject) || (value instanceof JSONArray)))
        else if (!((value instanceof String) || (value instanceof Number) || (value instanceof Boolean)))
            FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.CONTEXT_VALUE_ERROR, key));
        else if (FlagshipContext.isReserved(key))
            FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.CONTEXT_RESERVED_KEY_ERROR, key));
        else
            visitorDelegate.getContext().put(key, value);
        visitorDelegate.logVisitor(FlagshipLogManager.Tag.UPDATE_CONTEXT);
    }

    @Override
    public <T> void updateContext(FlagshipContext<T> flagshipContext, T value) {
        if (flagshipContext.verify(value))
            visitorDelegate.getContext().put(flagshipContext.key(), value);
    }

    @Override
    public void clearContext() {
        visitorDelegate.getContext().clear();
        visitorDelegate.loadContext(null);
        visitorDelegate.logVisitor(FlagshipLogManager.Tag.UPDATE_CONTEXT);
    }

    @Override
    public void sendContextRequest() {
        TrackingManager trackingManager = visitorDelegate.getConfigManager().getTrackingManager();
        trackingManager.sendContextRequest(visitorDelegate.toDTO());
    }

    @Override
    public void sendConsentRequest() {
        TrackingManager trackingManager = visitorDelegate.getConfigManager().getTrackingManager();
        if (trackingManager != null)
            trackingManager.sendHit(visitorDelegate.toDTO(), new Consent(visitorDelegate.getConsent()));
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        TrackingManager trackingManager = visitorDelegate.getConfigManager().getTrackingManager();
        if (trackingManager != null && hit != null)
            trackingManager.sendHit(visitorDelegate.toDTO(), hit);
    }

    @Override
    public void authenticate(String visitorId) {
        if (visitorDelegate.getConfigManager().isDecisionMode(Flagship.DecisionMode.API)) {
            if (visitorDelegate.getAnonymousId() == null)
                visitorDelegate.setAnonymousId(visitorDelegate.getVisitorId());
            visitorDelegate.setVisitorId(visitorId);
        } else {
            FlagshipLogManager.log(FlagshipLogManager.Tag.AUTHENTICATE, LogManager.Level.ERROR,
                    String.format(FlagshipConstants.Errors.AUTHENTICATION_BUCKETING_ERROR, "authenticate"));
        }
        visitorDelegate.loadContext(null);
        visitorDelegate.logVisitor(FlagshipLogManager.Tag.AUTHENTICATE);
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public void unauthenticate() {
        if (visitorDelegate.getConfigManager().isDecisionMode(Flagship.DecisionMode.API)) {
            if (visitorDelegate.getAnonymousId() != null) {
                visitorDelegate.setVisitorId(visitorDelegate.getAnonymousId());
                visitorDelegate.setAnonymousId(null);
            }
        } else {
            FlagshipLogManager.log(FlagshipLogManager.Tag.UNAUTHENTICATE, LogManager.Level.ERROR,
                    String.format(FlagshipConstants.Errors.AUTHENTICATION_BUCKETING_ERROR, "unauthenticate"));
        }
        visitorDelegate.loadContext(null);
        visitorDelegate.logVisitor(FlagshipLogManager.Tag.UNAUTHENTICATE);
    }

    @Override
    public void setConsent(Boolean hasConsented) {
        visitorDelegate.setConsent(hasConsented);
        if (!hasConsented) {
            visitorDelegate.getStrategy().flushVisitorCache();
            visitorDelegate.getStrategy().flushHitCache();
        }
        sendConsentRequest();
    }

    @Override
    public Boolean hasConsented() {
        return visitorDelegate.getConsent();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    void loadContext(HashMap<String, Object> context) {
        if (context != null) {
            for (Map.Entry<String, Object> e : context.entrySet()) {
                this.updateContext(e.getKey(), e.getValue());
            }
        }
        if (FlagshipContext.autoLoading) {
            for (FlagshipContext flagshipContext : FlagshipContext.ALL) {
                this.updateContext(flagshipContext, flagshipContext.load(visitorDelegate));
            }
        }
    }

    @Override
    public void cacheVisitor() {
        VisitorDelegateDTO visitorDelegateDTO = visitorDelegate.toDTO();
        CacheManager cacheManager = flagshipConfig.getCacheManager();
        if (cacheManager != null) {
            CompletableFuture.supplyAsync(() -> {
                try {
                    IVisitorCacheImplementation visitorCacheImplementation = flagshipConfig.getCacheManager().getVisitorCacheImplementation();
                    if (visitorCacheImplementation != null)
                        visitorCacheImplementation.cacheVisitor(visitorDelegateDTO.getVisitorId(),  VisitorCacheHelper.visitorToCacheJSON(visitorDelegateDTO));
                } catch (Exception e) {
                    logCacheException(String.format(FlagshipConstants.Errors.CACHE_IMPL_ERROR, "cacheVisitor", visitorDelegateDTO.getVisitorId()), e);
                }
                return 0;
            });
        }
    }

    @Override
    public void lookupVisitorCache() {
        VisitorDelegateDTO visitorDelegateDTO = visitorDelegate.toDTO();
        CacheManager cacheManager = flagshipConfig.getCacheManager();
        if (cacheManager != null) {
            try {
                CompletableFuture.supplyAsync(() -> {
                    IVisitorCacheImplementation cacheImplementation = cacheManager.getVisitorCacheImplementation();
                    if (cacheImplementation != null) {
                        JSONObject json = cacheImplementation.lookupVisitor(visitorDelegateDTO.getVisitorId());
                        VisitorCacheHelper.applyCacheToVisitor(visitorDelegateDTO, (json != null) ? json : new JSONObject());
                    }
                    return 0;
                }).get(cacheManager.getVisitorCacheLookupTimeout(), cacheManager.getTimeoutUnit());
            } catch (Exception e) {
                if (e instanceof TimeoutException)
                    logCacheException(String.format(FlagshipConstants.Errors.CACHE_IMPL_TIMEOUT, "lookupVisitor", visitorDelegateDTO.getVisitorId()), e);
                else
                    logCacheException(String.format(FlagshipConstants.Errors.CACHE_IMPL_ERROR, "lookupVisitor", visitorDelegateDTO.getVisitorId()), e);
            }
        }
    }

    @Override
    public void flushVisitorCache() {
        VisitorDelegateDTO visitorDelegateDTO = visitorDelegate.toDTO();
        CacheManager cacheManager = flagshipConfig.getCacheManager();
        if (cacheManager != null) {
            try {
                IVisitorCacheImplementation visitorCacheImplementation = cacheManager.getVisitorCacheImplementation();
                if (visitorCacheImplementation != null)
                    visitorCacheImplementation.flushVisitor(visitorDelegateDTO.getVisitorId());
            } catch (Exception e) {
                logCacheException(String.format(FlagshipConstants.Errors.CACHE_IMPL_ERROR, "flushVisitor", visitorDelegateDTO.getVisitorId()), e);
            }
        }
    }

    @Override
    public void lookupHitCache() {
        VisitorDelegateDTO visitorDelegateDTO = visitorDelegate.toDTO();
        CacheManager cacheManager = flagshipConfig.getCacheManager();
        if (cacheManager != null) {
            try {
                CompletableFuture.supplyAsync(() -> {
                    IHitCacheImplementation hitCacheImplementation = cacheManager.getHitCacheImplementation();
                    if (hitCacheImplementation != null) {
                        JSONArray array = hitCacheImplementation.lookupHits(visitorDelegateDTO.getVisitorId());
                        JSONArray result = (array != null) ? array : new JSONArray();
                        HitCacheHelper.applyHitMigration(visitorDelegateDTO, result);
                    }
                    return 0;
                }).get(cacheManager.getVisitorCacheLookupTimeout(), cacheManager.getTimeoutUnit());
            } catch (Exception e) {
                if (e instanceof TimeoutException)
                    logCacheException(String.format(FlagshipConstants.Errors.CACHE_IMPL_TIMEOUT, "lookupHits", visitorDelegateDTO.getVisitorId()), e);
                else
                    logCacheException(String.format(FlagshipConstants.Errors.CACHE_IMPL_ERROR, "lookupHits", visitorDelegateDTO.getVisitorId()), e);
            }
        }
    }

    @Override
    public void cacheHit(String visitorId, JSONObject data) {
        CacheManager cacheManager = flagshipConfig.getCacheManager();
        if (cacheManager != null) {
            try {
                IHitCacheImplementation hitCacheImplementation = cacheManager.getHitCacheImplementation();
                if (hitCacheImplementation != null)
                    hitCacheImplementation.cacheHit(visitorId, data);
            } catch (Exception e) {
                logCacheException(String.format(FlagshipConstants.Errors.CACHE_IMPL_ERROR, "cacheHit", visitorId), e);
            }
        }
    }

    @Override
    public void flushHitCache() {
        VisitorDelegateDTO visitorDelegateDTO = visitorDelegate.toDTO();
        CacheManager cacheManager = flagshipConfig.getCacheManager();
        if (cacheManager != null) {
            CompletableFuture.supplyAsync(() -> {
                try {
                    IHitCacheImplementation hitCacheImplementation = cacheManager.getHitCacheImplementation();
                    if (hitCacheImplementation != null)
                        hitCacheImplementation.flushHits(visitorDelegateDTO.getVisitorId());
                } catch (Exception e) {
                    logCacheException(String.format(FlagshipConstants.Errors.CACHE_IMPL_ERROR, "flushHit", visitorDelegateDTO.getVisitorId()), e);
                }
                return 0;
            });
        }
    }

    @Override
    public CompletableFuture<Visitor> fetchFlags() {

        DecisionManager decisionManager = visitorDelegate.getConfigManager().getDecisionManager();
        return CompletableFuture.supplyAsync(() -> {
            try {
                VisitorDelegateDTO visitorDTO = visitorDelegate.toDTO();
                visitorDelegate.updateModifications(decisionManager.getCampaignsModifications(visitorDTO));
                visitorDelegate.logVisitor(FlagshipLogManager.Tag.FETCHING);
                visitorDelegate.getStrategy().cacheVisitor();
            } catch (Exception e) {
                FlagshipLogManager.exception(e);
            }
            return visitorDelegate.getOriginalVisitor();
        }, HttpManager.getInstance().getThreadPoolExecutor()).whenCompleteAsync((instance, error) -> visitorDelegate.logVisitor(FlagshipLogManager.Tag.FETCHING));
    }

    @Override
    public <T> Flag<T> getFlag(String key, T defaultValue) {
        return new Flag<T>(visitorDelegate, key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    private <T> Modification getModification(String key, T defaultValue) throws FlagshipConstants.Exceptions.FlagNotFoundException, FlagshipConstants.Exceptions.FlagTypeException, FlagshipConstants.Exceptions.FlagException {
        HashMap<String, Modification> modifications = new HashMap<String, Modification>(visitorDelegate.getModifications());
        try {
            Modification modification = modifications.get(key);
            if (modification != null) {
                T castValue = (T) ((modification.isReference() && modification.getValue() == null) ? defaultValue : modification.getValue());
                if (defaultValue == null || castValue == null || castValue.getClass().equals(defaultValue.getClass()))
                    return modification;
                else
                    throw new FlagshipConstants.Exceptions.FlagTypeException();
            } else
                throw new FlagshipConstants.Exceptions.FlagNotFoundException();
        } catch (Exception e) {
            if (e instanceof FlagshipConstants.Exceptions.FlagTypeException || e instanceof FlagshipConstants.Exceptions.FlagNotFoundException)
                throw e;
            else
                throw new  FlagshipConstants.Exceptions.FlagException();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    synchronized public <T> T getFlagValue(String key, T defaultValue) {
        try {
            Modification modification = getModification(key, defaultValue);
            return (modification.getValue() != null) ? (T) modification.getValue() : (T) defaultValue;
        } catch (Exception e) {
           logFlagError(FlagshipLogManager.Tag.FLAG_VALUE, e, String.format( FlagshipConstants.Errors.FLAG_VALUE_ERROR, key));
        }
        return (T) defaultValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    synchronized public <T> Modification getFlagMetadata(String key, T defaultValue) {
        try {
            return getModification(key, defaultValue);
        } catch (Exception e) {
            logFlagError(FlagshipLogManager.Tag.FLAG_METADATA, e, String.format( FlagshipConstants.Errors.FLAG_METADATA_ERROR, key));
        }
        return null;
    }

    @Override
    synchronized public <T> void exposeFlag(String key, T defaultValue) {
        try {
            Modification modification = getModification(key, defaultValue);
            if (!visitorDelegate.getActivatedVariations().contains(modification.getVariationId()))
                visitorDelegate.getActivatedVariations().add(modification.getVariationId());
            sendHit(new Activate(modification));
        } catch (Exception e) {
            logFlagError(FlagshipLogManager.Tag.FLAG_USER_EXPOSED, e, String.format( FlagshipConstants.Errors.FLAG_USER_EXPOSITION_ERROR, key));
        }
    }
}