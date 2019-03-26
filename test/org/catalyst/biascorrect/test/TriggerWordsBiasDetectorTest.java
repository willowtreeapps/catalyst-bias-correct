package org.catalyst.biascorrect.test;

import org.catalyst.biascorrect.algos.TriggerWordBiasDetector;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class TriggerWordsBiasDetectorTest {

    @Test
    public void testBiasDetector() {
        TriggerWordBiasDetector.init();
        TriggerWordBiasDetector detector = TriggerWordBiasDetector.getInstance();
        String originalMessage = "She is so emotional";
        Pair<Boolean, String> correctedMessage = detector.getCorrectedMessage(originalMessage);
        Assert.assertTrue(correctedMessage.getLeft());
        Assert.assertEquals(correctedMessage.getRight(), "She is so *passionate*");

        originalMessage = "Cold brew has been replenished";
        correctedMessage = detector.getCorrectedMessage(originalMessage);
        Assert.assertNull(correctedMessage);

        originalMessage = "Sheesh, I’ve hit a snag I won’t be able to join";
        correctedMessage = detector.getCorrectedMessage(originalMessage);
        Assert.assertNull(correctedMessage);

        originalMessage = "Emotional? You're right! That defines her.";
        correctedMessage = detector.getCorrectedMessage(originalMessage);
        Assert.assertTrue(correctedMessage.getLeft());
        Assert.assertEquals("*Passionate*? You're right! That defines her.", correctedMessage.getRight());

        originalMessage = "Jeez! She is such a nag!";
        correctedMessage = detector.getCorrectedMessage(originalMessage);
        Assert.assertTrue(correctedMessage.getLeft());
        Assert.assertEquals(correctedMessage.getRight(), "Jeez! She is such a *leader*!");

        originalMessage = "She'll do anything for a promotion. She's pushy!";
        correctedMessage = detector.getCorrectedMessage(originalMessage);
        Assert.assertTrue(correctedMessage.getLeft());
        Assert.assertEquals(correctedMessage.getRight(), "She'll do anything for a promotion. She's *persuasive*!");
    }
}
