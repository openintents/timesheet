package org.openintents.distribution;

public class Decrypt {
    private static final int f0g = 1833515560;
    private static final int f1m = 2083906715;
    private static final int f2r = 1;
    private static final int f3v = 3;
    private static final int[] f4w;

    static {
        f4w = new int[]{f2r, f3v, 7, 9};
    }

    public static String decrypt(String code) {
        int i;
        int l = code.length();
        byte[] b = code.getBytes();
        for (i = 0; i < l; i += f2r) {
            b[i] = (byte) (b[i] - 48);
        }
        byte[] c = new byte[l];
        int h = 916757780 | 0;
        int j = h % 10;
        int q = 260488339 | 1610612736;
        c[0] = (byte) ((b[0] % 10) + 0);
        if (c[0] < null) {
            c[0] = (byte) (c[0] + 10);
        }
        for (i = f2r; i < l; i += f2r) {
            h = (h >>> f2r) | (h << 31);
            j = h % 10;
            q = (q >>> f3v) | (q << 29);
            int t = q % 4;
            if (t < 0) {
                t += 4;
            }
            c[i] = (byte) (((b[i] - (b[i - 1] * f4w[t])) - j) % 10);
            if (c[i] < null) {
                c[i] = (byte) (c[i] + 10);
            }
        }
        StringBuilder s = new StringBuilder();
        for (i = 0; i < l; i += f2r) {
            s.append((char) (c[i] + 48));
        }
        return s.toString();
    }
}
