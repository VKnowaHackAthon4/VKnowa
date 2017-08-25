package com.sogouime.hackathon4.vknowa.ui;

import java.net.*;
import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.*;
import java.lang.Math;

public class LexerDemo {
    public static String hmacSha256(String value, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes());
            return new String(Base64.getEncoder().encode(rawHmac));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Map<String, List<String>> sortByKey(Map<String, List<String>> unsortMap) {
        Map<String, List<String>> result = unsortMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return result;
    }

    public static Map<String, List<String>> parseQuery(String query) throws UnsupportedEncodingException {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
        String[] pairs = query.split("&");
        for (String pair: pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!result.containsKey(key)) {
                result.put(key, new LinkedList<String>());
            }
            String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            result.get(key).add(value);
        }
        return result;
    }

    public static String joinQuery(Map<String, List<String>> queryMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : queryMap.keySet()) {
            List<String> values = queryMap.get(key);
            try {
                for (String value: values) {
                    stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                    stringBuilder.append("=");
                    stringBuilder.append((value != null ? URLEncoder.encode(value, "UTF-8") : ""));
                    stringBuilder.append("&");
                } 
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
         }
         return stringBuilder.toString().replaceAll("&$", "");
    }
    
    public static String sign(String ak, String sk, String url, String method) throws Exception {
        URL aUrl = new URL(url);
        String hst = aUrl.getHost();
        String uri = aUrl.getPath();
        Map<String, List<String>> query = Collections.emptyMap();
        try {
            query = parseQuery(aUrl.getQuery());            
        } catch (Exception e) {
            throw e;
        }
        Map<String, List<String>> squery = sortByKey(query);
        String arg = joinQuery(squery);

        String now = Integer.toString(Math.toIntExact(System.currentTimeMillis() / 1000L));
        String pre = "sac-auth-v1/" + ak + "/" + now + "/3600";
        String calc = pre + "\n" + method + "\n" + hst + "\n" + uri + "\n" + arg;

        return pre + "/" + hmacSha256(calc, sk);
    }

    public static void TestMain() {
        String url  = "http://api.ai.sogou.com/nlp/lexer";
        String ak = "TtoAnu03oexDkwADdU3rhwH5";
        String sk = "Y-JFvKBw8KYQ1dHZcDoyA_nh1p4Iv4qm";
        try {
            String text = URLEncoder.encode("我把钥匙放在右边抽屉的柜子里了", "UTF-8");
            url = String.format("%s?text=%s", url, text);

            String sign = sign(ak, sk, url, "GET");
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // add request header
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.setRequestProperty("Authorization", sign);

            // Send get request
            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            // print response result
            System.out.println(response.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        }   
    }
}
