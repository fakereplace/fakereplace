package org.fakereplace.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Stuart Douglas
 */
public class MD5 {
    public static String md5(byte[] data) {

        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");

            m.reset();
            m.update(data);
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
