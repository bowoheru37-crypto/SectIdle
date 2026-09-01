package com.sect.idle.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ExceptionManager - Centralized, Thread-Safe & Non-Blocking Exception & Crash Management System.
 *
 * Key Capabilities:
 * - Asynchronous, non-blocking exception logging queue avoiding game thread stutter.
 * - Automatic stack trace capture, sanitization, and deduplication/throttling for high-frequency loops.
 * - Contextual system telemetry (Heap memory, Android OS version, Device model, Thread states).
 * - Circular breadcrumbs tracker for forensic root-cause analysis before crashes.
 * - Global UncaughtExceptionHandler integration for clean recovery and persistent crash dump storage.
 * - Safe procedural wrappers (SafeRunnable / SafeCallable) to safely sandbox dangerous code blocks.
 *
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class ExceptionManager {

    private static final String TAG = "ExceptionManager";
    private static final String PREF_NAME = "IdleSect_CrashDumps";
    private static final String KEY_CRASH_HISTORY = "crash_dumps_history";
    private static final String KEY_LATEST_CRASH = "latest_crash_dump";

    // Severity Levels
    public static final int LEVEL_DEBUG = 0;
    public static final int LEVEL_INFO = 1;
    public static final int LEVEL_WARN = 2;
    public static final int LEVEL_ERROR = 3;
    public static final int LEVEL_CRITICAL = 4;
    public static final int LEVEL_FATAL = 5;

    // Buffer and Pool Limits
    private static final int MAX_BREADCRUMBS = 32;
    private static final int MAX_CRASH_HISTORY = 20;
    private static final int MAX_DEDUPLICATED_EXCEPTIONS = 64;
    private static final long THROTTLE_WINDOW_MS = 3000L; // Throttle identical error spam for 3 seconds

    // Singleton Instance
    private static volatile ExceptionManager instance;
    private static final Object INIT_LOCK = new Object();

    // Context & Storage
    private Context appContext;
    private SharedPreferences prefs;

    // Asynchronous Queue & Background Worker
    private final ConcurrentLinkedQueue<ErrorRecord> errorQueue;
    private final AtomicBoolean isWorkerRunning;
    private Thread workerThread;
    private final Object queueSignal;

    // Breadcrumbs Circular Buffer (Thread-safe)
    private final String[] breadcrumbBuffer;
    private final long[] breadcrumbTimes;
    private int breadcrumbHead = 0;
    private int breadcrumbCount = 0;
    private final Object breadcrumbLock;

    // Exception Deduplication Cache (Stack signature -> Last log timestamp)
    private final ConcurrentHashMap<String, ErrorThrottleInfo> throttleMap;

    // Chained Default Uncaught Exception Handler
    private Thread.UncaughtExceptionHandler defaultHandler;
    private boolean isGlobalHandlerInstalled = false;

    // Registered Error Callbacks
    private final ArrayList<ErrorListener> errorListeners;
    private final Object listenerLock;

    // Re-entrancy guard to avoid recursive crash-logging loops
    private static final ThreadLocal<Boolean> IS_LOGGING_REENTRANT = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    /**
     * Data class holding captured exception details.
     */
    public static final class ErrorRecord {
        public final long timestamp;
        public final int level;
        public final String tag;
        public final String message;
        public final String exceptionClass;
        public final String stackTrace;
        public final String threadName;
        public final long threadId;
        public final String memorySnapshot;
        public final int occurrenceCount;

        public ErrorRecord(long timestamp, int level, String tag, String message,
                           String exceptionClass, String stackTrace, String threadName,
                           long threadId, String memorySnapshot, int occurrenceCount) {
            this.timestamp = timestamp;
            this.level = level;
            this.tag = tag != null ? tag : "UNKNOWN";
            this.message = message != null ? message : "";
            this.exceptionClass = exceptionClass != null ? exceptionClass : "";
            this.stackTrace = stackTrace != null ? stackTrace : "";
            this.threadName = threadName != null ? threadName : "unknown-thread";
            this.threadId = threadId;
            this.memorySnapshot = memorySnapshot != null ? memorySnapshot : "";
            this.occurrenceCount = occurrenceCount;
        }

        public String getFormattedSummary() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            StringBuilder sb = new StringBuilder(256);
            sb.append("[").append(sdf.format(new Date(timestamp))).append("] ");
            sb.append(getLevelString(level)).append(" [").append(tag).append("] ");
            sb.append("(Thread: ").append(threadName).append(" #").append(threadId).append(") ");
            sb.append(message);
            if (!exceptionClass.isEmpty()) {
                sb.append(" -> ").append(exceptionClass);
            }
            if (occurrenceCount > 1) {
                sb.append(" (Repeated ").append(occurrenceCount).append("x)");
            }
            return sb.toString();
        }
    }

    private static final class ErrorThrottleInfo {
        long lastLoggedTime;
        int suppressedCount;

        ErrorThrottleInfo(long time) {
            this.lastLoggedTime = time;
            this.suppressedCount = 0;
        }
    }

    /**
     * Listener interface for UI notifications or analytics telemetry.
     */
    public interface ErrorListener {
        void onErrorCaptured(ErrorRecord record);
    }

    /**
     * Functional interface for safe code execution (No return).
     */
    public interface SafeRunnable {
        void run() throws Throwable;
    }

    /**
     * Functional interface for safe code execution with return value.
     */
    public interface SafeSupplier<T> {
        T get() throws Throwable;
    }

    private ExceptionManager(Context ctx) {
        if (ctx != null) {
            this.appContext = ctx.getApplicationContext();
            this.prefs = this.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
        this.errorQueue = new ConcurrentLinkedQueue<ErrorRecord>();
        this.isWorkerRunning = new AtomicBoolean(true);
        this.queueSignal = new Object();

        this.breadcrumbBuffer = new String[MAX_BREADCRUMBS];
        this.breadcrumbTimes = new long[MAX_BREADCRUMBS];
        this.breadcrumbLock = new Object();

        this.throttleMap = new ConcurrentHashMap<String, ErrorThrottleInfo>();
        this.errorListeners = new ArrayList<ErrorListener>();
        this.listenerLock = new Object();

        startBackgroundWorker();
        addBreadcrumb("System", "ExceptionManager initialized successfully.");
    }

    /**
     * Initializes the singleton with context.
     */
    public static ExceptionManager init(Context context) {
        if (instance == null) {
            synchronized (INIT_LOCK) {
                if (instance == null) {
                    instance = new ExceptionManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Gets the global singleton instance.
     */
    public static ExceptionManager get() {
        if (instance == null) {
            synchronized (INIT_LOCK) {
                if (instance == null) {
                    instance = new ExceptionManager(null);
                }
            }
        }
        return instance;
    }

    /**
     * Installs global uncaught exception hook on current and all future threads.
     */
    public synchronized void installGlobalHandler(Context ctx) {
        if (isGlobalHandlerInstalled) return;

        if (this.appContext == null && ctx != null) {
            this.appContext = ctx.getApplicationContext();
            this.prefs = this.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        final Thread.UncaughtExceptionHandler existingHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.defaultHandler = existingHandler;

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                handleUncaughtException(t, e, existingHandler);
            }
        });

        isGlobalHandlerInstalled = true;
        addBreadcrumb("Security", "Global UncaughtExceptionHandler hooked.");
        Log.i(TAG, "Global UncaughtExceptionHandler successfully registered.");
    }

    private void handleUncaughtException(Thread t, Throwable e, Thread.UncaughtExceptionHandler original) {
        try {
            // Immediately capture and write crash dump synchronously since app is terminating
            String crashReport = generateFullCrashDump(t, e);
            saveCrashDumpSynchronously(crashReport);
            Log.e(TAG, "FATAL CRASH DETECTED:\n" + crashReport);
        } catch (Throwable inner) {
            Log.e(TAG, "Failed to persist crash dump: " + inner.getMessage());
        } finally {
            if (original != null) {
                original.uncaughtException(t, e);
            }
        }
    }

    /**
     * Non-blocking report method for exceptions.
     */
    public void report(Throwable t) {
        reportException(t, "GENERAL", null, LEVEL_ERROR);
    }

    /**
     * Non-blocking report method with custom tag.
     */
    public void report(Throwable t, String tag) {
        reportException(t, tag, null, LEVEL_ERROR);
    }

    /**
     * Non-blocking report method with custom tag and message.
     */
    public void report(Throwable t, String tag, String message) {
        reportException(t, tag, message, LEVEL_ERROR);
    }

    /**
     * Reports critical errors.
     */
    public void reportCritical(Throwable t, String tag, String message) {
        reportException(t, tag, message, LEVEL_CRITICAL);
    }

    /**
     * Reports fatal errors.
     */
    public void reportFatal(Throwable t, String tag, String message) {
        reportException(t, tag, message, LEVEL_FATAL);
    }

    /**
     * Reports informational warning / non-throwable error message.
     */
    public void reportWarning(String tag, String message) {
        reportException(null, tag, message, LEVEL_WARN);
    }

    /**
     * Core non-blocking method to capture, sanitize, and queue error records.
     */
    public void reportException(Throwable t, String tag, String customMessage, int level) {
        // Prevent recursive exception logging
        if (Boolean.TRUE.equals(IS_LOGGING_REENTRANT.get())) {
            return;
        }

        try {
            IS_LOGGING_REENTRANT.set(Boolean.TRUE);

            long now = System.currentTimeMillis();
            Thread currentThread = Thread.currentThread();

            String exClass = (t != null) ? t.getClass().getName() : "None";
            String exMsg = (t != null && t.getMessage() != null) ? t.getMessage() : "";
            String mainMsg = (customMessage != null && !customMessage.isEmpty())
                    ? customMessage
                    : (!exMsg.isEmpty() ? exMsg : "Unexpected Error State");

            String stackTrace = (t != null) ? extractStackTrace(t) : "";

            // Deduplication and Rate-limiting based on tag + exception class + stack frame signature
            String errorKey = tag + ":" + exClass + ":" + getFirstStackFrame(t);
            ErrorThrottleInfo throttle = throttleMap.get(errorKey);
            int count = 1;

            if (throttle != null) {
                if (now - throttle.lastLoggedTime < THROTTLE_WINDOW_MS) {
                    throttle.suppressedCount++;
                    return; // Throttled
                } else {
                    count += throttle.suppressedCount;
                    throttle.lastLoggedTime = now;
                    throttle.suppressedCount = 0;
                }
            } else {
                if (throttleMap.size() < MAX_DEDUPLICATED_EXCEPTIONS) {
                    throttleMap.put(errorKey, new ErrorThrottleInfo(now));
                }
            }

            String memInfo = getMemoryUsageString();

            ErrorRecord record = new ErrorRecord(
                    now, level, tag, mainMsg, exClass, stackTrace,
                    currentThread.getName(), currentThread.getId(), memInfo, count
            );

            // Logcat output based on level
            logcatOutput(record);

            // Push to non-blocking queue for disk write and notification
            errorQueue.offer(record);
            synchronized (queueSignal) {
                queueSignal.notify();
            }

        } catch (Throwable fatal) {
            // Absolute fallback: direct logcat, never throw back
            Log.e(TAG, "ExceptionManager internal failure: " + fatal.getMessage());
        } finally {
            IS_LOGGING_REENTRANT.set(Boolean.FALSE);
        }
    }

    private void logcatOutput(ErrorRecord record) {
        String formatted = record.getFormattedSummary();
        switch (record.level) {
            case LEVEL_DEBUG:
                Log.d(record.tag, formatted);
                break;
            case LEVEL_INFO:
                Log.i(record.tag, formatted);
                break;
            case LEVEL_WARN:
                Log.w(record.tag, formatted);
                break;
            case LEVEL_CRITICAL:
            case LEVEL_FATAL:
                Log.e(record.tag, "!!! " + formatted);
                if (!record.stackTrace.isEmpty()) {
                    Log.e(record.tag, record.stackTrace);
                }
                break;
            case LEVEL_ERROR:
            default:
                Log.e(record.tag, formatted);
                if (!record.stackTrace.isEmpty()) {
                    Log.e(record.tag, record.stackTrace);
                }
                break;
        }
    }

    /**
     * Executes a runnable block safely. Traps any throwable, reports it, and prevents app crash.
     */
    public static void safeRun(SafeRunnable runnable, String tag, String errorDesc) {
        if (runnable == null) return;
        try {
            runnable.run();
        } catch (Throwable t) {
            get().reportException(t, tag, errorDesc, LEVEL_ERROR);
        }
    }

    /**
     * Executes a supplier safely. Returns fallbackValue if an exception occurs.
     */
    public static <T> T safeCall(SafeSupplier<T> supplier, T fallbackValue, String tag, String errorDesc) {
        if (supplier == null) return fallbackValue;
        try {
            return supplier.get();
        } catch (Throwable t) {
            get().reportException(t, tag, errorDesc, LEVEL_ERROR);
            return fallbackValue;
        }
    }

    /**
     * Records a diagnostic breadcrumb event into circular memory buffer.
     */
    public void addBreadcrumb(String tag, String message) {
        if (message == null) return;
        long now = System.currentTimeMillis();
        synchronized (breadcrumbLock) {
            breadcrumbBuffer[breadcrumbHead] = "[" + (tag != null ? tag : "APP") + "] " + message;
            breadcrumbTimes[breadcrumbHead] = now;
            breadcrumbHead = (breadcrumbHead + 1) % MAX_BREADCRUMBS;
            if (breadcrumbCount < MAX_BREADCRUMBS) {
                breadcrumbCount++;
            }
        }
    }

    /**
     * Retrieves all recorded breadcrumbs in chronological order.
     */
    public ArrayList<String> getBreadcrumbs() {
        ArrayList<String> list = new ArrayList<String>(breadcrumbCount);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
        synchronized (breadcrumbLock) {
            int start = (breadcrumbHead - breadcrumbCount + MAX_BREADCRUMBS) % MAX_BREADCRUMBS;
            for (int i = 0; i < breadcrumbCount; i++) {
                int idx = (start + i) % MAX_BREADCRUMBS;
                String timeStr = sdf.format(new Date(breadcrumbTimes[idx]));
                list.add(timeStr + " -> " + breadcrumbBuffer[idx]);
            }
        }
        return list;
    }

    /**
     * Registers an error listener for telemetry / UI alerts.
     */
    public void addListener(ErrorListener listener) {
        if (listener == null) return;
        synchronized (listenerLock) {
            if (!errorListeners.contains(listener)) {
                errorListeners.add(listener);
            }
        }
    }

    /**
     * Unregisters an error listener.
     */
    public void removeListener(ErrorListener listener) {
        if (listener == null) return;
        synchronized (listenerLock) {
            errorListeners.remove(listener);
        }
    }

    /**
     * Background worker thread initialization.
     */
    private void startBackgroundWorker() {
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                processBackgroundQueue();
            }
        }, "ExceptionManager-AsyncWriter");
        workerThread.setDaemon(true);
        workerThread.setPriority(Thread.MIN_PRIORITY);
        workerThread.start();
    }

    private void processBackgroundQueue() {
        while (isWorkerRunning.get()) {
            ErrorRecord record = errorQueue.poll();
            if (record != null) {
                handleQueuedRecord(record);
            } else {
                synchronized (queueSignal) {
                    try {
                        queueSignal.wait(5000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private void handleQueuedRecord(ErrorRecord record) {
        // 1. Notify error listeners safely
        synchronized (listenerLock) {
            for (int i = 0; i < errorListeners.size(); i++) {
                try {
                    errorListeners.get(i).onErrorCaptured(record);
                } catch (Throwable ignored) {}
            }
        }

        // 2. Persist critical & fatal errors to SharedPreferences
        if (record.level >= LEVEL_ERROR && prefs != null) {
            try {
                String dump = formatErrorRecordToDump(record);
                saveCrashDumpSynchronously(dump);
            } catch (Throwable ignored) {}
        }
    }

    private synchronized void saveCrashDumpSynchronously(String dump) {
        if (prefs == null || dump == null || dump.isEmpty()) return;
        try {
            String history = prefs.getString(KEY_CRASH_HISTORY, "");
            StringBuilder sb = new StringBuilder(dump.length() + history.length() + 64);
            sb.append(dump).append("\n===SPLIT===\n").append(history);

            // Trim old history entries
            String fullStr = sb.toString();
            String[] entries = fullStr.split("===SPLIT===");
            if (entries.length > MAX_CRASH_HISTORY) {
                sb.setLength(0);
                for (int i = 0; i < MAX_CRASH_HISTORY; i++) {
                    sb.append(entries[i].trim()).append("\n===SPLIT===\n");
                }
            }

            prefs.edit()
                    .putString(KEY_LATEST_CRASH, dump)
                    .putString(KEY_CRASH_HISTORY, sb.toString())
                    .apply();
        } catch (Throwable ignored) {}
    }

    /**
     * Generates a complete comprehensive diagnostic crash dump string.
     */
    public String generateFullCrashDump(Thread t, Throwable ex) {
        StringBuilder sb = new StringBuilder(2048);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z", Locale.US);
        sb.append("====================================================\n");
        sb.append("         SECT IDLE CULTIVATION CRASH DUMP           \n");
        sb.append("====================================================\n");
        sb.append("Timestamp:     ").append(sdf.format(new Date())).append("\n");
        sb.append("Thread:        ").append(t != null ? t.getName() : "unknown").append(" (ID: ").append(t != null ? t.getId() : -1).append(")\n");
        sb.append("Device Model:  ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        sb.append("Android OS:    API ").append(Build.VERSION.SDK_INT).append(" (").append(Build.VERSION.RELEASE).append(")\n");
        sb.append("Memory Status: ").append(getMemoryUsageString()).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("EXCEPTION DETAILS:\n");
        if (ex != null) {
            sb.append("Class:   ").append(ex.getClass().getName()).append("\n");
            sb.append("Message: ").append(ex.getMessage()).append("\n");
            sb.append("Stack Trace:\n").append(extractStackTrace(ex)).append("\n");
        } else {
            sb.append("No throwable attached.\n");
        }
        sb.append("----------------------------------------------------\n");
        sb.append("RECENT BREADCRUMBS:\n");
        ArrayList<String> breadcrumbs = getBreadcrumbs();
        if (breadcrumbs.isEmpty()) {
            sb.append("  (No breadcrumbs recorded)\n");
        } else {
            for (String b : breadcrumbs) {
                sb.append("  * ").append(b).append("\n");
            }
        }
        sb.append("====================================================\n");
        return sb.toString();
    }

    private String formatErrorRecordToDump(ErrorRecord record) {
        StringBuilder sb = new StringBuilder(1024);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        sb.append("[").append(sdf.format(new Date(record.timestamp))).append("] ");
        sb.append(getLevelString(record.level)).append(" Tag: ").append(record.tag).append("\n");
        sb.append("Thread: ").append(record.threadName).append(" (ID: ").append(record.threadId).append(")\n");
        sb.append("Message: ").append(record.message).append("\n");
        if (!record.exceptionClass.isEmpty()) {
            sb.append("Exception: ").append(record.exceptionClass).append("\n");
        }
        sb.append("Memory: ").append(record.memorySnapshot).append("\n");
        if (!record.stackTrace.isEmpty()) {
            sb.append("StackTrace:\n").append(record.stackTrace).append("\n");
        }
        return sb.toString();
    }

    public String getLatestCrashDump() {
        if (prefs == null) return "No persistent storage attached.";
        return prefs.getString(KEY_LATEST_CRASH, "No crash recorded.");
    }

    public ArrayList<String> getCrashHistory() {
        ArrayList<String> list = new ArrayList<String>();
        if (prefs == null) return list;
        String raw = prefs.getString(KEY_CRASH_HISTORY, "");
        if (raw.isEmpty()) return list;

        String[] split = raw.split("===SPLIT===");
        for (String item : split) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    public void clearCrashHistory() {
        if (prefs != null) {
            prefs.edit().remove(KEY_LATEST_CRASH).remove(KEY_CRASH_HISTORY).apply();
        }
        addBreadcrumb("System", "Crash history cleared.");
    }

    private static String extractStackTrace(Throwable t) {
        if (t == null) return "";
        try {
            StringWriter sw = new StringWriter(512);
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        } catch (Throwable e) {
            return "Failed to stringify stacktrace: " + e.getMessage();
        }
    }

    private static String getFirstStackFrame(Throwable t) {
        if (t == null) return "no_trace";
        StackTraceElement[] elements = t.getStackTrace();
        if (elements != null && elements.length > 0) {
            return elements[0].getClassName() + "." + elements[0].getMethodName() + ":" + elements[0].getLineNumber();
        }
        return "empty_trace";
    }

    public static String getMemoryUsageString() {
        try {
            Runtime rt = Runtime.getRuntime();
            long max = rt.maxMemory() / (1024 * 1024);
            long total = rt.totalMemory() / (1024 * 1024);
            long free = rt.freeMemory() / (1024 * 1024);
            long used = total - free;
            return "Used: " + used + "MB / Total: " + total + "MB (Max: " + max + "MB)";
        } catch (Throwable t) {
            return "Memory info unavailable";
        }
    }

    public static String getLevelString(int level) {
        switch (level) {
            case LEVEL_DEBUG: return "DEBUG";
            case LEVEL_INFO: return "INFO";
            case LEVEL_WARN: return "WARN";
            case LEVEL_ERROR: return "ERROR";
            case LEVEL_CRITICAL: return "CRITICAL";
            case LEVEL_FATAL: return "FATAL";
            default: return "UNKNOWN";
        }
    }

    /**
     * Clean shutdown of worker thread if app process terminates.
     */
    public void shutdown() {
        isWorkerRunning.set(false);
        synchronized (queueSignal) {
            queueSignal.notifyAll();
        }
    }
}
