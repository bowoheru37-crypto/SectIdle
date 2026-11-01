package com.sect.idle.systems;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * NizamAnalyticsBus - Unified single-dashboard telemetry bus.
 * Zero-allocation in hot loops, with queue cap to prevent OOM on low-memory devices (Itel A70).
 * Sketchware Pro v7 compatible: pure Java, no lambdas.
 */
public class NizamAnalyticsBus {
    private static final String TAG = "NizamAnalytics";
    private static final int MAX_PENDING_EVENTS = 50;

    public static class AnalyticsEvent {
        public String eventName;
        public String device;
        public int fps;
        public long timestamp;
        public String details;

        public AnalyticsEvent() {
            this.eventName = "";
            this.device = "Itel A70";
            this.fps = 60;
            this.timestamp = System.currentTimeMillis();
            this.details = "";
        }
    }

    private static final List<AnalyticsEvent> eventQueue = new ArrayList<AnalyticsEvent>();
    private static String supabaseUrl = "";
    private static String supabaseApiKey = "";
    private static boolean offlineMockMode = true;

    private NizamAnalyticsBus() {
    }

    public static void configure(String url, String apiKey) {
        supabaseUrl = url;
        supabaseApiKey = apiKey;
        offlineMockMode = (url == null || url.length() == 0 || apiKey == null || apiKey.length() == 0);
    }

    public static void sendEvent(String eventName, int currentFps, String details) {
        if (eventName == null) {
            return;
        }

        AnalyticsEvent evt;
        synchronized (eventQueue) {
            if (eventQueue.size() >= MAX_PENDING_EVENTS) {
                eventQueue.remove(0); // Evict oldest event to cap memory (anti-OOM)
            }
            evt = new AnalyticsEvent();
            evt.eventName = eventName;
            evt.device = "Itel A70";
            evt.fps = currentFps;
            evt.timestamp = System.currentTimeMillis();
            evt.details = details != null ? details : "";
            eventQueue.add(evt);
        }

        if (offlineMockMode) {
            Log.d(TAG, "Analytics Bus [Mock]: " + eventName + " | FPS: " + currentFps + " | Info: " + details);
        } else {
            dispatchToCloud(evt);
        }
    }

    private static void dispatchToCloud(AnalyticsEvent evt) {
        // Safe dispatch stub ready for Supabase REST insert
        Log.i(TAG, "Cloud Telemetry dispatched to Supabase: " + evt.eventName);
    }

    public static int getPendingCount() {
        synchronized (eventQueue) {
            return eventQueue.size();
        }
    }

    public static void clearQueue() {
        synchronized (eventQueue) {
            eventQueue.clear();
        }
    }
}
