package com.sect.idle.ui;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.sect.idle.systems.NizamAnalyticsBus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NizamWizardEngineViewTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NizamAnalyticsBus.clearQueue();
    }

    @Test
    public void testWizardInitializationAndDefaultSteps() {
        NizamWizardEngineView view = new NizamWizardEngineView(context);
        Assert.assertEquals(NizamWizardEngineView.STEP_PROJECT_SETUP, view.getCurrentStep());

        view.setCurrentStep(NizamWizardEngineView.STEP_CHARACTER_PREVIEW);
        Assert.assertEquals(NizamWizardEngineView.STEP_CHARACTER_PREVIEW, view.getCurrentStep());

        view.setCurrentStep(NizamWizardEngineView.STEP_PLATFORM_SELECTION);
        Assert.assertEquals(NizamWizardEngineView.STEP_PLATFORM_SELECTION, view.getCurrentStep());

        view.setCurrentStep(NizamWizardEngineView.STEP_BUILD_EXPORT);
        Assert.assertEquals(NizamWizardEngineView.STEP_BUILD_EXPORT, view.getCurrentStep());
    }

    @Test
    public void testBuildAllTriggerAndListener() {
        NizamWizardEngineView view = new NizamWizardEngineView(context);
        final boolean[] triggered = new boolean[] { false };

        view.setOnBuildAllListener(new NizamWizardEngineView.OnBuildAllListener() {
            @Override
            public void onBuildTriggered(String[] targetPlatforms) {
                triggered[0] = true;
                Assert.assertEquals(6, targetPlatforms.length);
            }
        });

        Assert.assertFalse(view.isBuildSuccessful());
        view.triggerBuildAll();
        Assert.assertTrue(view.isBuildSuccessful());
        Assert.assertTrue(triggered[0]);
        Assert.assertTrue(view.getBuildStatusMessage().contains("Succeeded"));
    }

    @Test
    public void testAnalyticsQueueCappingAntiOOM() {
        NizamAnalyticsBus.clearQueue();
        for (int i = 0; i < 100; i++) {
            NizamAnalyticsBus.sendEvent("stress_test_event_" + i, 60, "Benchmarking memory");
        }

        // Capped at 50 to avoid any memory leak / OOM on Itel A70
        Assert.assertEquals(50, NizamAnalyticsBus.getPendingCount());
    }
}
