package com.abtasty.flagship.utils;

import com.abtasty.flagship.BuildConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * FlagshipContext class define context constants keys used for targeting. Some might be automatically loaded during visitor creation.
 * Set FlagshipContext.autoLoading = false to disable automatic loading.
 */
public class FlagshipContext<T> {

    public static Boolean   autoLoading = true;

    private final String    name;
    private final String    key;
    private boolean         reserved = false;

    private FlagshipContext(String name, String key) {
        this.name = name;
        this.key = key;
    }

    private FlagshipContext(String name, String key, boolean reserved) {
        this.name = name;
        this.key = key;
        this.reserved = reserved;
    }

    public String key() {
        return this.key;
    }

    protected String name() {
        return this.name;
    }

//    public static boolean isOverriding(String key) {
//        return Stream.of(EContext.values()).anyMatch(context -> context.key.equals(key));
//    }

    public static boolean isReserved(String key) {
        return ALL.stream().anyMatch(context -> context.key.equals(key) && context.reserved);
    }

    public final boolean verify(T value) {
        try {
            return value != null && verifyValue(value);
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean verifyValue(T value) { return true; }

    public T load() { return null; }

    public enum DeviceType {
        @SuppressWarnings("unused") MOBILE("mobile"),
        @SuppressWarnings("unused") TABLET("tablet"),
        @SuppressWarnings("unused") PC("pc"),
        @SuppressWarnings("unused") SERVER("server"),
        @SuppressWarnings("unused") IOT("iot"),
        @SuppressWarnings("unused") OTHER("other");

        String value;

        DeviceType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Define the current device locale in the visitor context. (must be a iso3 code String)
     */
    public static final FlagshipContext<String> DEVICE_LOCALE = new FlagshipContext<String>("DEVICE_LOCALE", "sdk_deviceLanguage") {
        @Override
        public boolean verifyValue(String value) {
            String lang = new Locale(value).getISO3Language();
            return lang != null;
        }
    };

    /**
     * Define the current device type in the visitor context. Must be a DeviceType value.
     */
    public static final FlagshipContext<DeviceType> DEVICE_TYPE = new FlagshipContext<>("DEVICE_TYPE", "sdk_deviceType");

    /**
     * Define the current device model (Google Pixel 3) in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> DEVICE_MODEL = new FlagshipContext<>("DEVICE_MODEL", "sdk_deviceModel");

    /**
     * Define the current city location in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> LOCATION_CITY = new FlagshipContext<>("LOCATION_CITY", "sdk_city");

    /**
     * Define the current country location in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> LOCATION_REGION = new FlagshipContext<>("LOCATION_REGION", "sdk_region");

    /**
     * Define the current country location in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> LOCATION_COUNTRY = new FlagshipContext<>("LOCATION_COUNTRY", "sdk_country");

    /**
     * Define the current latitude location in the visitor context. Must be a Double.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<Double> LOCATION_LAT = new FlagshipContext<Double>("LOCATION_LAT", "sdk_lat");

    /**
     * Define the current longitude location in the visitor context. Must be a Double.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<Double> LOCATION_LONG = new FlagshipContext<>("LOCATION_LONG", "sdk_long");

    /**
     * Define the current longitude location in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> IP = new FlagshipContext<>("IP", "sdk_ip");

    /**
     * Define the current OS name in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> OS_NAME = new FlagshipContext<>("OS_NAME", "sdk_osName");

    /**
     * Define the current OS version name in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> OS_VERSION_NAME = new FlagshipContext<>("OS_VERSION_NAME", "sdk_osVersionName");

    /**
     * Define the current OS version code in the visitor context. Must be a Number >= 0.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<Number> OS_VERSION_CODE = new FlagshipContext<Number>("OS_VERSION_CODE", "sdk_osVersionCode") {

        @Override
        public boolean verifyValue(Number value) {
            return (value.doubleValue() >= 0);
        }
    };

    /**
     * Define the current carrier name in the visitor context.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> CARRIER_NAME = new FlagshipContext<String>("CARRIER_NAME", "sdk_carrierName");

    /**
     * Define the current connection type in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> INTERNET_CONNECTION = new FlagshipContext<String>("INTERNET_CONNECTION", "sdk_internetConnection");

    /**
     * Define the current app version in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> APP_VERSION_NAME = new FlagshipContext<String>("APP_VERSION_NAME", "sdk_versionName");

    /**
     * Define the current app version in the visitor context. Must be a Number >= 0.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<Number> APP_VERSION_CODE = new FlagshipContext<Number>("APP_VERSION_CODE", "sdk_versionCode") {

        @Override
        public boolean verifyValue(Number value) {
            return (value.doubleValue() > 0);
        }
    };

    /**
     * Define the current interface name or URL in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    public static final FlagshipContext<String> INTERFACE_NAME = new FlagshipContext<String>("INTERFACE_NAME", "sdk_interfaceName");

    /**
     * Define the flagship SDK in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    private static final FlagshipContext<String> FLAGSHIP_CLIENT = new FlagshipContext<String>("FLAGSHIP_CLIENT", "fs_client", true) {

        @Override
        public String load() {
            return "java";
        }
    };

    /**
     * Define the flagship SDK version  in the visitor context. Must be a String.
     */
    @SuppressWarnings("unused")
    private static final FlagshipContext<String> FLAGSHIP_VERSION = new FlagshipContext<String>("FLAGSHIP_VERSION", "fs_version", true) {

        @Override
        public String load() {
            return BuildConfig.flagship_version_name;
        }
    };

    public static final List<FlagshipContext<?>> ALL = Arrays.asList(DEVICE_LOCALE, DEVICE_TYPE, DEVICE_MODEL, LOCATION_CITY,
            LOCATION_REGION, LOCATION_COUNTRY, LOCATION_LAT, LOCATION_LONG, IP, OS_NAME, OS_VERSION_NAME,
            OS_VERSION_CODE, CARRIER_NAME, INTERNET_CONNECTION, APP_VERSION_NAME, APP_VERSION_CODE,
            INTERFACE_NAME, FLAGSHIP_CLIENT, FLAGSHIP_VERSION);
}