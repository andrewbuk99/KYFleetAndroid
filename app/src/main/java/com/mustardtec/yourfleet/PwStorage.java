package com.mustardtec.yourfleet;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PwStorage {
    public static int PW_HASH_ITERATION_COUNT = 2;
    private static MessageDigest md;

    public static String main(String pw) {
        //String pw = "teüöäßÖst1";
        String salt = "dIabP$xas2356G";

        //try {
        //    md = MessageDigest.getInstance("SHA-512");
        //} catch (NoSuchAlgorithmException e) {
        //    e.printStackTrace();
        //    throw new RuntimeException("No Such Algorithm");
        ///}
        String results2 = "";
        try {
            results2 = hashMac(pw, salt);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        //String result = PwStorage.hashPw(pw, salt);

        return results2;
        // result: 2SzT+ikuO9FBq7KJWulZy2uZYujLjFkSpcOwlfBhi6VvajJMr6gxuRo5WvilrMlcM/44u2q8Y1smUlidZQrLCQ==
    }


    private static String hashPw(String pw, String salt) {
        byte[] bSalt;
        byte[] bPw;

        try {
            bSalt = salt.getBytes("UTF-8");
            bPw = pw.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported Encoding", e);
        }

        byte[] digest = run(bPw, bSalt);
        // for (int i = 0; i < PW_HASH_ITERATION_COUNT - 1; i++) {
        //     digest = run(digest, bSalt);
        //}
        String aString = "";
        try {
            aString = new String(digest, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        String vtemp = Base64.encodeBytes(digest);
        String vtemp2 = convertToHex(digest);
        //string vtemp3 = Hex.encodeHex(digest);
        String s = "";
        try {
            s = new String(digest, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //return convertToHex(digest);
        //return Base64.encodeBytes(digest);
        return sb.toString();

    }
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
    private static byte[] run(byte[] input, byte[] salt) {
        md.update(input);
        //md.update(input);
        return md.digest(salt);
    }
    private static final String HASH_ALGORITHM = "HmacSHA512";

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }
    public static String hashMac(String text, String secretKey)
            throws SignatureException {

        try {
            Key sk = new SecretKeySpec(secretKey.getBytes(), HASH_ALGORITHM);
            Mac mac = Mac.getInstance(sk.getAlgorithm());
            mac.init(sk);
            final byte[] hmac = mac.doFinal(text.getBytes());
            return toHexString(hmac);
        } catch (NoSuchAlgorithmException e1) {
            // throw an exception or pick a different encryption method
            throw new SignatureException(
                    "error building signature, no such algorithm in device "
                            + HASH_ALGORITHM);
        } catch (InvalidKeyException e) {
            throw new SignatureException(
                    "error building signature, invalid key " + HASH_ALGORITHM);
        }
    }
}
