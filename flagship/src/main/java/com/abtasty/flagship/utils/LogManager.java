package com.abtasty.flagship.utils;

/**
 * Class to extends in order to provide a custom Log manager.
 */
public abstract class LogManager {

    /**
     * This enum class defines Flagship log levels that can be used to control SDK outputs.
     * <br><br>
     * The levels in ascending order are : NONE(0), EXCEPTIONS(1), ERROR(2), WARNING(3), DEBUG(4), INFO(5), ALL(6).
     * <br><br>
     * <ul>
     * <li>NONE = 0: Logging will be disabled.</li>
     * <li>EXCEPTIONS = 1: Only caught exception will be logged.</li>
     * <li>ERROR = 2: Only errors and above will be logged.</li>
     * <li>WARNING = 3: Only warnings and above will be logged.</li>
     * <li>DEBUG = 4: Only debug logs and above will be logged.</li>
     * <li>INFO = 5: Only info logs and above will be logged.</li>
     * <li>ALL = 6: All logs will be logged.</li>
     * </ul>
     *
     */
    public enum Level {

        /**
         * NONE = 0: Logging will be disabled.
         */
        NONE(0),
        /**
         * EXCEPTIONS = 1: Only caught exception will be logged.
         */
        EXCEPTIONS(1),
        /**
         * ERROR = 2: Only errors and above will be logged.
         */
        ERROR(2),
        /**
         * WARNING = 3: Only warnings and above will be logged.
         */
        WARNING(3),
        /**
         * DEBUG = 4: Only debug logs and above will be logged.
         */
        DEBUG(4),
        /**
         * INFO = 5: Only info logs and above will be logged.
         */
        INFO(5),
        /**
         * ALL = 6: All logs will be logged.
         */
        ALL(6);

        int level;

        Level(int level) {
            this.level = level;
        }

        public final boolean isAllowed(Level newLevel) {
            return (newLevel.level < this.level) || (newLevel.level == this.level);
        }
    }

    protected LogManager.Level  level;

    public LogManager() {
        this.level = Level.ALL;
    }

    public LogManager(LogManager.Level level) {
        this.level = (level == null) ? Level.NONE : level;
    }

    /**
     * Set the log mode for filtering logs.
     * @param level log level.
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    protected final void newLog(Level level, String tag, String message) {
        if (this.level.isAllowed(level))
            onLog(level, tag, message);
    }

    /**
     * Called when the SDK produce a log.
     * @param level log level.
     * @param tag location where the log come from.
     * @param message log message.
     */
    public abstract void onLog(Level level, String tag, String message);

    /**
     * Called when the SDK has caught an Exception.
     * @param e exception.
     */
    public void onException(Exception e) { }
}
