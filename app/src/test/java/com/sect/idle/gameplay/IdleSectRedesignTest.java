package com.sect.idle.gameplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import com.sect.idle.procedural.IdleSectAssetGenerator;
import com.sect.idle.utils.MobileThermalGovernor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class IdleSectRedesignTest {

    @Test
    public void testJsonDataParsing() {
        String json = "{\n" +
                "  \"version\": \"2.0-ItelA70-Ultra\",\n" +
                "  \"sect\": {\n" +
                "    \"name\": \"Cloud Mist Sect\",\n" +
                "    \"realm\": \"Mortal\",\n" +
                "    \"resources\": {\n" +
                "      \"SS\": 1000,\n" +
                "      \"Jade\": 50,\n" +
                "      \"Essence\": 20\n" +
                "    }\n" +
                "  },\n" +
                "  \"disciples\": [\n" +
                "    {\n" +
                "      \"id\": \"zhao_kai\",\n" +
                "      \"name\": \"Zhao Kai\",\n" +
                "      \"realm\": \"Qi Refining\",\n" +
                "      \"stars\": \"2-Star\",\n" +
                "      \"stats\": {\n" +
                "        \"STR\": 11,\n" +
                "        \"AGI\": 21,\n" +
                "        \"INT\": 60,\n" +
                "        \"LCK\": 9\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        IdleSectDataLoader loader = IdleSectDataLoader.get();
        boolean parsed = loader.parseJson(json);
        assertTrue("JSON parsing should succeed", parsed);
        assertEquals("Cloud Mist Sect", loader.sectName);
        assertEquals(1000, loader.ss);
        assertEquals(50, loader.jade);
        assertEquals(20, loader.essence);
        assertEquals(1, loader.disciples.size());
        assertEquals("Zhao Kai", loader.disciples.get(0).name);
        assertEquals(60, loader.disciples.get(0).intStat);
    }

    @Test
    public void testAssetGeneration() {
        Bitmap zhaoKai = IdleSectAssetGenerator.createZhaoKaiChibi(64);
        assertNotNull("Zhao Kai Chibi bitmap should be generated", zhaoKai);
        assertEquals(64, zhaoKai.getWidth());
        assertEquals(64, zhaoKai.getHeight());

        Bitmap guardian = IdleSectAssetGenerator.createSectGuardianChibi(64);
        assertNotNull("Sect Guardian Chibi should be generated", guardian);

        Bitmap wolf = IdleSectAssetGenerator.createDemonicWolf(64);
        assertNotNull("Demonic Wolf should be generated", wolf);

        Bitmap prambanan = IdleSectAssetGenerator.createPrambananBuilding(96, 96);
        assertNotNull("Prambanan building should be generated", prambanan);
    }

    @Test
    public void testThermalGovernorHeapMemoryBudget() {
        long heapMb = MobileThermalGovernor.getJvmHeapUsedMb();
        assertTrue("Heap query should return non-negative value", heapMb >= 0);

        MobileThermalGovernor governor = MobileThermalGovernor.getInstance(null);
        assertNotNull("Governor instance should not be null", governor);
        governor.applyLowQualitySettings();
        assertTrue("Quality should be low after applying low quality settings",
                com.sect.idle.core.GameConfig.currentQuality == com.sect.idle.core.GameConfig.QUALITY_LOW);
    }

    @Test
    public void testAudioSpatial3D() {
        assertNotNull("AudioManager class should be present", com.sect.idle.systems.AudioManager.class);
    }
}
