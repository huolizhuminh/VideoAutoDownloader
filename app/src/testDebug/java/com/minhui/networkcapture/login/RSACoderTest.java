package com.minhui.networkcapture.login;

import junit.framework.TestCase;

import java.util.Map;

public class RSACoderTest extends TestCase {

    public void testInitKey() {
        try {
            Map<String, Object> keyPair = RSACoder.initKey();
            String publicKey = RSACoder.getPublicKey(keyPair);
            String privateKey = RSACoder.getPrivateKey(keyPair);
            byte[] bytes = RSACoder.encryptByPrivateKey("zhuminh".getBytes(), privateKey);
            String afterEncrypt = RSACoder.encryptBASE64(bytes);
            byte[] bytesAfterBase64 = RSACoder.decryptBASE64(afterEncrypt);
            byte[] afterDecrpt = RSACoder.decryptByPublicKey(bytesAfterBase64, publicKey);

            String s = new String(afterDecrpt);
            System.out.println("result = "+s);

        }catch (Exception e){
            System.out.println("failed error = "+e.getMessage());
        }

    }
}