package com.abtasty.flagship.main;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.visitor.Visitor;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import java.util.HashMap;

/**
 * Flagship main singleton.
 */
public class Flagship {

    private static volatile Flagship    instance = null;

    private final ConfigManager         configManager   = new ConfigManager();
    private Visitor                     singleVisitorInstance = null;
    private Status                      status          = Status.NOT_INITIALIZED;

    /**
     * Flagship running Mode
     */
    public enum DecisionMode {
        API,
        BUCKETING,
    }

    /**
     * Status listener to implement in order to get a call back when the SDK status has changed.
     */
    public interface StatusListener {
        void onStatusChanged(Status newStatus);
    }

    /**
     * Flagship Status enum
     */
    public enum Status {
        /**
         * Flagship SDK has not been started or initialized successfully.
         */
        NOT_INITIALIZED(0x0),
        /**
         * Flagship SDK is starting.
         */
        STARTING(0x1),
        /**
         * Flagship SDK has been started successfully but is still polling campaigns.
         */
        POLLING(0x10),
        /**
         * Flagship SDK is ready but is running in Panic mode: All features are disabled except the one which refresh this status.
         */
        PANIC(0x20),
        /**
         * Flagship SDK is ready to use.
         */
        READY(0x100);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public boolean lessThan(Status status) {
            return this.value < status.value;
        }

        public boolean greaterThan(Status status) {
            return this.value > status.value;
        }
    }

    /**
     * Main singleton
     * @return Flagship instance
     */
    public static Flagship instance() {
        if (instance == null) {
            synchronized (Flagship.class) {
                if (instance == null)
                    instance = new Flagship();
            }
        }
        return instance;
    }

    /**
     * Start the flagship SDK, with the default configuration.
     *
     * @param envId  : Environment id provided by Flagship.
     * @param apiKey : Secure api key provided by Flagship.
     */
    public static void start(String envId, String apiKey) {
        start(envId, apiKey, null);
    }

    /**
     * Start the flagship SDK, with a custom configuration implementation.
     *
     * @param envId  : Environment id provided by Flagship.
     * @param apiKey : Secure api key provided by Flagship.
     * @param config : SDK configuration. @see FlagshipConfig
     */
    public static void start(String envId, String apiKey, FlagshipConfig<?> config) {
        instance()._start(envId, apiKey, config);
    }

    /**
     * Return a Visitor Builder class.
     *
     * @param visitorId : Unique visitor identifier.
     * @param instanceType : How Flagship SDK should handle the newly created visitor instance when built. (Default is NEW_INSTANCE)
     * @return Visitor.Builder
     */
    public static Visitor.Builder newVisitor(String visitorId, Visitor.Instance instanceType) {
        return new Visitor.Builder(instanceType, instance().configManager, visitorId);
    }

    /**
     * Return a Visitor Builder class. When built this will return a new instance of Visitor.
     *
     * @param visitorId : Unique visitor identifier.
     * @return Visitor.Builder
     */
    public static Visitor.Builder newVisitor(String visitorId) {
        return newVisitor(visitorId, Visitor.Instance.NEW_INSTANCE);
    }

    public static void setSingleVisitorInstance(Visitor visitor) {
        instance().singleVisitorInstance = visitor;
    }


    /**
     * This method will return any previous created visitor instance initialized with the SINGLE_INSTANCE parameter or null.
     */
    public static Visitor getVisitor() {
        return instance().singleVisitorInstance;
    }


    /**
     * Return the current used configuration.
     * @return FlagshipConfig
     */
    public static FlagshipConfig<?> getConfig() {
        return instance().configManager.getFlagshipConfig();
    }

    /**
     * Return the current SDK status.
     * @return Status
     */
    public static Status getStatus() {
        return instance().status;
    }

    /**
     * Private instance implementations.
     */
    private void _start(String envId, String apiKey, FlagshipConfig<?> config) {
        updateStatus(Status.STARTING);
        this.configManager.reset();
        this.configManager.init(envId, apiKey, config);
        this.configManager.getDecisionManager().setStatusListener(this::updateStatus);
        if (!this.configManager.isSet()) {
            this.updateStatus(Status.NOT_INITIALIZED);
            FlagshipLogManager.log(FlagshipLogManager.Tag.INITIALIZATION, LogManager.Level.ERROR, FlagshipConstants.Errors.INITIALIZATION_PARAM_ERROR);
        }
    }

    private void updateStatus(Status status) {
        if (this.status != status) {
            this.status = status;
            FlagshipLogManager.Tag tag = FlagshipLogManager.Tag.GLOBAL;
            LogManager.Level level = LogManager.Level.INFO;
            String message = (this.status == Status.READY) ?
                    String.format(FlagshipConstants.Info.READY, BuildConfig.flagship_version_name) :
                    String.format(FlagshipConstants.Info.STATUS_CHANGED, status);
            FlagshipLogManager.log(tag, level, message);
            StatusListener customerStatusListener = this.configManager.getFlagshipConfig().getStatusListener();
            if (customerStatusListener != null)
                customerStatusListener.onStatusChanged(status);
        }
    }
}
