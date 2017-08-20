package com.sogouime.hackathon4.vknowa.util;

/**
 * Created by zhusong on 2017-08-20.
 */
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.sogouime.hackathon4.vknowa.util.Base64Utils;

public class CryptUtils {

    public static String HMACSHA256 = "HmacSHA256";

    public static String EncodeString(String SecretKey, String Message)
    {
        try {
            Mac sha256_HMAC = Mac.getInstance(HMACSHA256);
            SecretKeySpec secret_key = new SecretKeySpec(SecretKey.getBytes(), HMACSHA256);
            sha256_HMAC.init(secret_key);

            String hash = Base64Utils.encode(sha256_HMAC.doFinal(Message.getBytes()));
            return  hash;
        } catch (Exception e) {
            System.out.println("Error");
        }

        return null;
    }
}
