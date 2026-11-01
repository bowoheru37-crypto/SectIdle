package com.sect.idle.fusion;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NizamFusionEngineTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testGodotNodeHierarchy() {
        NizamNode root = new NizamNode(1, "Root");
        NizamNode player = new NizamNode(2, "Player");
        NizamNode sprite = new NizamNode(3, "Sprite");

        root.addChild(player);
        player.addChild(sprite);

        Assert.assertEquals(1, root.getChildren().size());
        Assert.assertEquals(player, root.getChildren().get(0));
        Assert.assertEquals(root, player.getParent());
        Assert.assertEquals(1, player.getChildren().size());
        Assert.assertEquals(player, sprite.getParent());
    }

    @Test
    public void testFighterMakerFrameDataAndHitbox() {
        NizamMoveData punch = new NizamMoveData("Punch", 5, 8, 12, 15f);
        Assert.assertEquals(25, punch.getTotalFrames());

        // Startup frames (0-4): Hitbox should NOT be active
        Assert.assertFalse(punch.isHitboxActive(0));
        Assert.assertFalse(punch.isHitboxActive(4));

        // Active frames (5-12): Hitbox should be active
        Assert.assertTrue(punch.isHitboxActive(5));
        Assert.assertTrue(punch.isHitboxActive(8));
        Assert.assertTrue(punch.isHitboxActive(12));

        // Recovery frames (13-24): Hitbox should NOT be active
        Assert.assertFalse(punch.isHitboxActive(13));
        Assert.assertFalse(punch.isHitboxActive(24));
    }

    @Test
    public void testHitboxOverlapCollision() {
        NizamHitbox h1 = new NizamHitbox(10, 10, 50, 50, true);
        NizamHitbox h2 = new NizamHitbox(30, 30, 50, 50, false);
        NizamHitbox h3 = new NizamHitbox(100, 100, 50, 50, false);

        Assert.assertTrue(h1.overlaps(h2));
        Assert.assertFalse(h1.overlaps(h3));
    }

    @Test
    public void testBinaryMoveCodecSerialization() {
        NizamMoveData original = new NizamMoveData("Keris_Tebas", 6, 7, 10, 25f);
        original.activeHitbox.x = 120;
        original.activeHitbox.y = 85;
        original.activeHitbox.width = 75;
        original.activeHitbox.height = 40;

        byte[] encoded = NizamBinaryCodec.encodeMove(original);
        Assert.assertNotNull(encoded);
        Assert.assertEquals(32, encoded.length);

        NizamMoveData decoded = NizamBinaryCodec.decodeMove(encoded);
        Assert.assertEquals(6, decoded.startupFrames);
        Assert.assertEquals(7, decoded.activeFrames);
        Assert.assertEquals(10, decoded.recoveryFrames);
        Assert.assertEquals(25, (int) decoded.damage);
        Assert.assertEquals(120, (int) decoded.activeHitbox.x);
        Assert.assertEquals(85, (int) decoded.activeHitbox.y);
        Assert.assertEquals(75, (int) decoded.activeHitbox.width);
        Assert.assertEquals(40, (int) decoded.activeHitbox.height);
    }

    @Test
    public void testFusionStudioViewInteraction() {
        NizamFusionStudioView view = new NizamFusionStudioView(context);
        Assert.assertFalse(view.isPlaying());

        view.setPlaying(true);
        Assert.assertTrue(view.isPlaying());

        view.setCurrentFrame(10);
        Assert.assertEquals(10, view.getCurrentFrame());
    }
}
