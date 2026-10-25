import java.time.LocalDateTime;

/*
 * Logging System
 *
 * 1. Logger - exposes info(), debug(), warn(), error().
 * 2. LogManager - manages log level and sends log messages.
 * 3. LogAppender - interface for output destinations.
 * 4. ConsoleAppender - prints logs to console.
 * 5. LogLevel - defines severity levels.
 *
 * Flow:
 * Logger -> LogManager -> LogAppender -> Console
 *
 * Design pattern: Strategy - different appenders can be plugged in.
 */

public class Main {
    public static void main(String[] args) {
        LogManager manager = new LogManager(
            LogLevel.DEBUG, new ConsoleAppender()
        );

        Logger logger = new Logger(manager);

        logger.debug("Application started");
        logger.info("User logged in");
        logger.warn("Disk space is low");
        logger.error("Payment failed");
    }
}

class Logger {
    private LogManager manager;

    public Logger(LogManager manager) {
        this.manager = manager;
    }

    public void debug(String message) {
        manager.log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        manager.log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        manager.log(LogLevel.WARN, message);
    }

    public void error(String message) {
        manager.log(LogLevel.ERROR, message);
    }
}

class LogManager {
    private LogLevel minimumLevel;
    private LogAppender appender;

    public LogManager(LogLevel minimumLevel, LogAppender appender) {
        this.minimumLevel = minimumLevel;
        this.appender = appender;
    }

    public void log(LogLevel level, String message) {
        if (level.ordinal() < minimumLevel.ordinal()) {
            return;
        }

        String formattedMessage = String.format(
            "%s [%s] %s",
            LocalDateTime.now(), level, message
        );

        appender.append(formattedMessage);
    }
}

interface LogAppender {
    void append(String message);
}

class ConsoleAppender implements LogAppender {
    @Override
    public void append(String message) {
        System.out.println(message);
    }
}

enum LogLevel {
    DEBUG, INFO, WARN, ERROR
}