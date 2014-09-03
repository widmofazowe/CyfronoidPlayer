package eu.cyfronoid.audio.player.dsp;

public class FFT {
    private float[] xre;
    private float[] xim;
    private float[] mag;

    private float[] fftSin;
    private float[] fftCos;
    private int[] fftBr;

    private int sampleSize, halfSampleSize, nu;

    /**
     * @param The amount of the sample provided to the "calculate" method to use during
     *        FFT calculations.
     */
    public FFT(int sampleSize) {
        this.sampleSize  = sampleSize;
        halfSampleSize = sampleSize >> 1;

        xre = new float[sampleSize];
        xim = new float[sampleSize];
        mag = new float[halfSampleSize];

        nu = (int)(Math.log(sampleSize)/Math.log(2));

        prepareFFTTables();
    }

    /**
     * @param  samples The sample to compute FFT values on.
     * @return         The results of the calculation, normalized between 0.0 and 1.0.
     */
    public float[] calculate(float[] samples) {
        int n2 = halfSampleSize;

        int wAps = samples.length/sampleSize;

        // -- FIXME: This affects the calculation accuracy, because
        //           is compresses the digital signal. Looks nice on
        //           the spectrum analyser, as it chops off most of
        //           sound we cannot hear anyway.
        for(int a = 0, b = 0; a < samples.length; a += wAps, b++) {
            xre[b] = samples[a];
            xim[b] = 0.0f;
        }

        float tr, ti, c, s;
        int k, kn2, x = 0;

        for(int l = 1; l <= nu; l++) {

            k = 0;

            while(k < sampleSize) {

                for(int i = 1; i <= n2; i++) {

                    // -- Tabled sin/cos
                    c = fftCos[x];
                    s = fftSin[x];

                    kn2 = k + n2;

                    tr = xre[kn2] * c + xim[kn2] * s;
                    ti = xim[kn2] * c - xre[kn2] * s;

                    xre[kn2] = xre[k] - tr;
                    xim[n2] = xim[k] - ti;
                    xre[k] += tr;
                    xim[k] += ti;

                    k++; x++;

                }

                k += n2;

            }

            n2 >>= 1;

        }

        int r;

        // -- Reorder output.
        for(k = 0; k < sampleSize; k++) {

            // -- Use tabled BR values.
            r = fftBr[k];

            if (r > k) {

                tr = xre[k];
                ti = xim[k];

                xre[k] = xre[r];
                xim[k] = xim[r];
                xre[r] = tr;
                xim[r] = ti;

            }

        }

        // -- Calculate magnitude.
        mag[0] = (float)(Math.sqrt(xre[0] * xre[0] + xim[0] * xim[0]))/sampleSize;

        for(int i = 1; i < halfSampleSize; i++) {
            mag[i]= 2 * (float)(Math.sqrt(xre[i] * xre[i] + xim[i] * xim[i]))/sampleSize;
        }

        return mag;

    }

    private void prepareFFTTables() {
        int n2 = halfSampleSize;
        int nu1 = nu - 1;

        // -- Allocate FFT SIN/COS tables.
        fftSin = new float[nu * n2];
        fftCos = new float[nu * n2];

        float p, arg;
        int k = 0, x = 0;

        // -- Prepare SIN/COS tables.
        for(int l = 1; l <= nu; l++) {

            while(k < sampleSize) {

                for(int i = 1; i <= n2; i++) {

                    p = bitrev(k >> nu1, nu);

                    arg = 2 * (float)Math.PI * p/sampleSize;

                    fftSin[x] = (float)Math.sin(arg);
                    fftCos[x] = (float)Math.cos(arg);

                    k++;
                    x++;

                }

                k += n2;

            }

            k = 0;

            nu1--;
            n2 >>= 1;

        }

        // -- Prepare bitrev table.
        fftBr = new int[sampleSize];

        for(k = 0; k < sampleSize; k++) {
            fftBr[k] = bitrev(k, nu);
        }
    }

    private int bitrev(int j, int nu) {
        int j1 = j;
        int j2;
        int k = 0;

        for( int i = 1; i <= nu; i++ ) {
            j2 = j1 >> 1;
            k  = (k << 1) + j1 - (j2 << 1);
            j1 = j2;
        }

        return k;
    }


}

