package com.abtasty.flagship.main;

import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import java.util.concurrent.TimeUnit;

/**
 * Flagship SDK configuration class to provide at initialization.
 */
public abstract class FlagshipConfig<T> {

    private String                              envId               = null;
    private String                              apiKey              = null;
    private Flagship.DecisionMode               decisionMode        = Flagship.DecisionMode.API;
    private int                                 timeout             = 2000;
    private LogManager.Level                    logLevel            = LogManager.Level.ALL;
    private LogManager                          logManager          = new FlagshipLogManager(logLevel);
    private long                                pollingTime         = 60;
    private TimeUnit                            pollingUnit         = TimeUnit.SECONDS;
    private Flagship.StatusListener             statusListener      = null;


    /**
     * Create a new empty FlagshipConfig configuration.
     */
    public FlagshipConfig() { }

    /**
     * Create a new FlagshipConfig configuration.
     *
     * @param envId : Environment id provided by Flagship.
     * @param apiKey : Secure api key provided by Flagship.
     */
    protected FlagshipConfig(String envId, String apiKey) {
        this.envId = envId;
        this.apiKey = apiKey;
    }

    /**
     * Specify the environment id provided by Flagship, to use.
     * @param envId environment id.
     */
    protected void withEnvId(String envId) {
        this.envId = envId;
    }

    /**
     * Specify the secure api key provided by Flagship, to use.
     * @param apiKey secure api key.
     */
    protected void withApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Specify the SDK running mode.
     * @param mode mode.
     * @return FlagshipConfig
     */
    @SuppressWarnings("unchecked")
    protected T withDecisionMode(Flagship.DecisionMode mode) {
        if (mode != null)
            this.decisionMode = mode;
        return (T) this;
    }


    /**
     * Specify a custom implementation of LogManager in order to receive logs from the SDK.
     * @param logManager custom implementation of LogManager.
     * @return FlagshipConfig
     */
    @SuppressWarnings("unchecked")
    public T withLogManager(LogManager logManager) {
        this.logManager = logManager;
        return (T) this;
    }

    /**
     * Specify a log level to filter SDK logs.
     * @param level level of log priority.
     * @return FlagshipConfig
     */
    @SuppressWarnings("unchecked")
    public T withLogLevel(LogManager.Level level) {
        if (level != null) {
            this.logLevel = level;
            this.logManager.setLevel(this.logLevel);
        }
        return (T) this;
    }

    /**
     * Specify timeout for api request.
     * @param timeout milliseconds for connect and read timeouts. Default is 2000.
     * @return FlagshipConfig
     */
    @SuppressWarnings("unchecked")
    public T withTimeout(int timeout) {
        if (timeout > 0)
            this.timeout = timeout;
        return (T) this;
    }

    /**
     * Define time interval between two bucketing updates. Default is 60 seconds. MICROSECONDS and NANOSECONDS Unit are ignored.
     * @param time time value.
     * @param timeUnit time unit.
     * @return FlagshipConfig
     */
    @SuppressWarnings("unchecked")
    protected T withBucketingPollingIntervals(long time, TimeUnit timeUnit) {
        if (time >= 0 && timeUnit != null && timeUnit != TimeUnit.MICROSECONDS && timeUnit != TimeUnit.NANOSECONDS) {
            this.pollingTime = time;
            this.pollingUnit = timeUnit;
        }
        return (T) this;
    }

    /**
     * Define a new listener in order to get callback when the SDK status has changed.
     * @param listener new listener.
     * @return FlagshipConfig
     */
    @SuppressWarnings("unchecked")
    public T withStatusListener(Flagship.StatusListener listener) {
        if (listener != null)
            statusListener = listener;
        return (T) this;
    }

    public Flagship.StatusListener getStatusListener() {
        return statusListener;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getEnvId() {
        return envId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Flagship.DecisionMode getDecisionMode() {
        return decisionMode;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public long getPollingTime() {
        return pollingTime;
    }

    public TimeUnit getPollingUnit() {
        return pollingUnit;
    }

    @Override
    public String toString() {
        return "FlagshipConfig{" +
                "envId='" + envId + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", mode=" + decisionMode +
                ", logManager=" + logManager +
                '}';
    }

    protected boolean isSet() {
        return (envId != null && apiKey != null);
    }

    public static class Bucketing extends FlagshipConfig<Bucketing> {
        public Bucketing() {
            super();
            super.withDecisionMode(Flagship.DecisionMode.BUCKETING);
        }

        public Bucketing(String envId, String apiKey) {
            super(envId, apiKey);
            super.withDecisionMode(Flagship.DecisionMode.BUCKETING);
        }

        public Bucketing withPollingIntervals(long time, TimeUnit timeUnit) {
            return super.withBucketingPollingIntervals(time, timeUnit);
        }
    }

    public static class DecisionApi extends FlagshipConfig<DecisionApi> {
        public DecisionApi() {
            super();
            super.withDecisionMode(Flagship.DecisionMode.API);
        }

        public DecisionApi(String envId, String apiKey) {
            super(envId, apiKey);
            super.withDecisionMode(Flagship.DecisionMode.API);
        }
    }
}
