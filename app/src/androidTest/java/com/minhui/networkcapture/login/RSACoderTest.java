package com.minhui.networkcapture.login;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class RSACoderTest {

    @Before
    public void setUp() throws Exception {


    }

    @Test
    public void decryptBASE64() {
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