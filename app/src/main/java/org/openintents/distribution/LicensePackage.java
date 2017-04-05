package org.openintents.distribution;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

public class LicensePackage {
    public static boolean checkLicense(Context context) {
        try {
            Signature[] signatures = context.getPackageManager().getPackageInfo(convert("yga.sysggqbrfqu.fzifysuzt.vxwdrbs.kdzxwq", 5), 64).signatures;
            if (signatures == null || signatures.length <= 0 || !signatures[0].toCharsString().substring(30, 80).equals(convert("8004r6hm374o81268d8389ox576700468559373851152v6900", 7))) {
                return false;
            }
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private static String convert(String text, int number) {
        char[] cs = text.toCharArray();
        StringBuffer sb = new StringBuffer(text.length());
        int i = number;
        int j = number;
        for (char c : cs) {
            char c2;
            i += number;
            while (i > 25) {
                i -= 26;
            }
            while (i < 0) {
                i += 26;
            }
            if (c2 >= 'a' && c2 <= 'z') {
                c2 = (char) (c2 - i);
                while (c2 < 'a') {
                    c2 = (char) (c2 + 26);
                }
                while (c2 > 'z') {
                    c2 = (char) (c2 - 26);
                }
            }
            j += number;
            while (j > 9) {
                j -= 10;
            }
            while (j < 0) {
                j += 10;
            }
            if (c2 >= '0' && c2 <= '9') {
                c2 = (char) (c2 - j);
                while (c2 < '0') {
                    c2 = (char) (c2 + 10);
                }
                while (c2 > '9') {
                    c2 = (char) (c2 - 10);
                }
            }
            sb.append(c2);
        }
        return sb.toString();
    }
}
