package com.abtasty.flagship.utils;

import java.util.Random;

public class MurmurHash {

    public static int getAllocationFromMurmur(String variationGroupId, String visitorId) {
        if (variationGroupId != null && visitorId != null) {
            long hash = murmurHash3_x86_32(variationGroupId + visitorId);
            return (int) (hash % 100);
        } else {
            return new Random().nextInt(100);
        }
    }

    public static long murmurHash3_x86_32(String source) {

        int c1 = -0x3361d2af;
        int c2 = 0x1b873593;

        int h1 = 0;
        int pos = 0;
        int end = source.length();
        int k1 = 0;
        int k2 = 0;
        int shift = 0;
        int bits = 0;
        int nBytes = 0;

        while (pos < end) {
            int charCode = source.charAt(pos++);
            if (charCode < 0x80) {
                k2 = charCode;
                bits = 8;
            } else if (charCode < 0x800) {
                k2 = (0xC0 | (charCode >> 6) | (0x80 | (charCode & 0x3F) << 8));
                bits = 16;
            } else if (charCode < 0xD800 || charCode > 0xDFFF || pos >= end) {
                k2 = (0xE0 | (charCode >> 12) | (0x80 | (charCode >> 6 & 0x3F) << 8) | (0x80 | (charCode & 0x3F) << 16));
                bits = 24;
            } else {
                int utf32 = source.charAt(pos++);
                utf32 = (charCode - 0xD7C0 << 10) + (utf32 & 0x3FF);
                k2 = (0xff & (0xF0 | (utf32 >> 18)) | (0x80 | (utf32 >> 12 & 0x3F) << 8) | (0x80 | (utf32 >> 6 & 0x3F) << 16) | (0x80 | (utf32 & 0x3F) << 24));
                bits = 32;
            }

            k1 = (k1 | (k2 << shift));
            shift += bits;
            if (shift >= 32) {
                k1 *= c1;
                k1 = (k1 << 15) | (k1 >>> 17);
                k1 *= c2;
                h1 = h1 ^ k1;
                h1 = (h1 << 13) | (h1 >>> 19);
                h1 = (h1 * 5) + (-0x19ab949c);

                shift -= 32;

                if (shift != 0) {
                    k1 = k2 >>> (bits - shift);
                } else {
                    k1 = 0;
                }
                nBytes += 4;
            }
        }

        if (shift > 0) {
            nBytes += (shift >> 3);
            k1 *= c1;
            k1 = (k1 << 15) | (k1 >>> 17);
            k1 *= c2;
            h1 = h1 ^ k1;
        }

        h1 = h1 ^ nBytes;
        h1 = h1 ^ (h1 >>> 16);
        h1 *= -0x7a143595;
        h1 = h1 ^ (h1 >>> 13);
        h1 *= -0x3d4d51cb;
        h1 = h1 ^ (h1 >>> 16);

        return ((long) h1 & 0xFFFFFFFFL);

    }
}
