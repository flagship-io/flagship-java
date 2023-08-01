package com.abtasty.flagship.cache;

import java.util.concurrent.TimeUnit;

/**
 * Cache Manager to implement for features that request cache functionalities.
 *
 * Have a look on IVisitorCacheImplementation interface in order to cache Visitor's data.
 * Have a look on IHitCacheImplementation  in order to cache hits that failed to be sent.
 *
 */
public abstract class CacheManager {

    protected TimeUnit                      timeoutUnit = TimeUnit.MILLISECONDS;
    protected Long                          visitorCacheLookupTimeout = 200L;
    protected Long                          hitCacheLookupTimeout = 200L;
    protected IVisitorCacheImplementation   visitorCacheImplementation = null;
    protected IHitCacheImplementation       hitCacheImplementation = null;

    public Long getVisitorCacheLookupTimeout() {
        return visitorCacheLookupTimeout;
    }

    public Long getHitCacheLookupTimeout() {
        return hitCacheLookupTimeout;
    }

    public IVisitorCacheImplementation getVisitorCacheImplementation() {
        return visitorCacheImplementation;
    }

    public IHitCacheImplementation getHitCacheImplementation() {
        return hitCacheImplementation;
    }

    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    public static class NoCache extends CacheManager {};

    /**
     * Builder class that helps to implement a custom cache manager.
     */
    public static class Builder {

        private CacheManager cacheManager = new CacheManager() {};

        /**
         * Define the visitor cache lookup timeout.
         */
        public Builder withVisitorCacheLookupTimeout(Long timeout) {
            cacheManager.visitorCacheLookupTimeout = timeout;
            return this;
        }

        /**
         * Define the visitor hit cache lookup timeout.
         */
        public Builder withHitCacheLookupTimeout(Long timeout) {
            cacheManager.hitCacheLookupTimeout = timeout;
            return this;
        }

        /**
         * Define a custom visitor cache implementation.
         */
        public Builder withVisitorCacheImplementation(IVisitorCacheImplementation implementation) {
            cacheManager.visitorCacheImplementation = implementation;
            return this;
        }

        /**
         * Define a custom hit cache implementation.
         */
        public Builder withHitCacheImplementation(IHitCacheImplementation implementation) {
            cacheManager.hitCacheImplementation = implementation;
            return this;
        }

        /**
         * Build an instance of CacheManager
         */
        public CacheManager build() {
            return cacheManager;
        }
    }

}
