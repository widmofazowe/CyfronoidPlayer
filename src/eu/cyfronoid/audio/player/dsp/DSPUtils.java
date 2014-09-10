package eu.cyfronoid.audio.player.dsp;

public class DSPUtils {

    public static float[] stereoMerge(float[] pLeft, float[] pRight) {
        for (int a = 0; a < pLeft.length; a++) {
            pLeft[a] = (pLeft[a] + pRight[a]) / 2.0f;
        }
        return pLeft;
    }
}
