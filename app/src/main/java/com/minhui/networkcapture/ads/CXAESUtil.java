package com.minhui.networkcapture.ads;

import com.minhui.vpn.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CXAESUtil {

    private final static String HEX = "0123456789ABCDEF";
    private static final int keyLenght = 16;
    private static final String defaultV = "0";

    /**
     * 加密
     *
     * @param key 密钥
     * @param src 加密文本
     * @return
     * @throws Exception
     */
    public static String encrypt(String key, String src) throws Exception {
        // /src = Base64.encodeToString(src.getBytes(), Base64.DEFAULT);
        byte[] rawKey = toMakekey(key, keyLenght, defaultV).getBytes();// key.getBytes();
        byte[] result = encrypt(rawKey, src.getBytes("utf-8"));
        // result = Base64.encode(result, Base64.DEFAULT);
        return toHex(result);
    }

    /**
     * 加密
     *
     * @param key 密钥
     * @param src 加密文本
     * @return
     * @throws Exception
     */
    public static String encrypt2Java(String key, String src) throws Exception {
        // /src = Base64.encodeToString(src.getBytes(), Base64.DEFAULT);
        byte[] rawKey = toMakekey(key, keyLenght, defaultV).getBytes();// key.getBytes();
        byte[] result = encrypt2Java(rawKey, src.getBytes("utf-8"));
        // result = Base64.encode(result, Base64.DEFAULT);
        return toHex(result);
    }

    /**
     * 解密
     *
     * @param key       密钥
     * @param encrypted 待揭秘文本
     * @return
     * @throws Exception
     */
    public static String decrypt(String key, String encrypted) throws Exception {
        byte[] rawKey = toMakekey(key, keyLenght, defaultV).getBytes();// key.getBytes();
        byte[] enc = toByte(encrypted);
        // enc = Base64.decode(enc, Base64.DEFAULT);
        byte[] result = decrypt(rawKey, enc);
        // /result = Base64.decode(result, Base64.DEFAULT);
        return new String(result, "utf-8");
    }

    /**
     * 密钥key ,默认补的数字，补全16位数，以保证安全补全至少16位长度,android和ios对接通过
     *
     * @param str
     * @param strLength
     * @param val
     * @return
     */
    private static String toMakekey(String str, int strLength, String val) {

        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(str).append(val);
                str = buffer.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /**
     * 真正的加密过程
     * 1.通过密钥得到一个密钥专用的对象SecretKeySpec
     * 2.Cipher 加密算法，加密模式和填充方式三部分或指定加密算 (可以只用写算法然后用默认的其他方式)Cipher.getInstance("AES");
     *
     * @param key
     * @param src
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] key, byte[] src) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        byte[] encrypted = cipher.doFinal(src);
        return encrypted;
    }

    /**
     * 真正的加密过程
     *
     * @param key
     * @param src
     * @return
     * @throws Exception
     */
    private static byte[] encrypt2Java(byte[] key, byte[] src) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        byte[] encrypted = cipher.doFinal(src);
        return encrypted;
    }

    /**
     * 真正的解密过程
     *
     * @param key
     * @param encrypted
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] key, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }


    /**
     * 把16进制转化为字节数组
     *
     * @param hexString
     * @return
     */
    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }


    /**
     * 二进制转字符,转成了16进制
     * 0123456789abcdefg
     *
     * @param buf
     * @return
     */
    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

    /**
     * 初始化 AES Cipher
     *
     * @param sKey
     * @param cipherMode
     * @return
     */
    public static Cipher initAESCipher(String sKey, int cipherMode) {
        // 创建Key gen
        // KeyGenerator keyGenerator = null;
        Cipher cipher = null;
        try {
            /*
             * keyGenerator = KeyGenerator.getInstance("AES");
             * keyGenerator.init(128, new SecureRandom(sKey.getBytes()));
             * SecretKey secretKey = keyGenerator.generateKey(); byte[]
             * codeFormat = secretKey.getEncoded(); SecretKeySpec key = new
             * SecretKeySpec(codeFormat, "AES"); cipher =
             * Cipher.getInstance("AES"); //初始化 cipher.init(cipherMode, key);
             */
            byte[] rawKey = toMakekey(sKey, keyLenght, defaultV).getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (InvalidKeyException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (Exception e){

        }
        return cipher;
    }


    public static File encryptFile(String source, String dir, String toFile, String sKey) {
        // 新建临时加密文件
        File encryptFile = null;
        OutputStream outputStream = null;
        try {
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            encryptFile = new File(dirFile , toFile);
            outputStream = new FileOutputStream(encryptFile);
            Cipher cipher = initAESCipher(sKey, Cipher.ENCRYPT_MODE);
            // 以加密流写入文件
            byte[] bytes = cipher.doFinal(source.getBytes());
            outputStream.write(bytes);
            outputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (Exception e) {

        } finally {
            Utils.close(outputStream);
        }
        return encryptFile;
    }

    /**
     * AES方式解密文件
     *
     * @param sourceFile
     * @return
     */
    public static String  decryptFile(File sourceFile, String sKey) {
        InputStream inputStream = null;
        StringBuilder builder = new StringBuilder();
        try {
            Cipher cipher = initAESCipher(sKey, Cipher.DECRYPT_MODE);
            inputStream = new FileInputStream(sourceFile);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = inputStream.read(buffer)) >= 0) {
                byte[] bytes = cipher.doFinal(buffer, 0, r);
                builder.append(new String(bytes,0,bytes.length,"utf-8"));
            }

        } catch (IOException e) {
            e.printStackTrace();  // To change body of catch statement use File |
            // Settings | File Templates.
        } catch (Exception e) {

        } finally {
            Utils.close(inputStream);

        }
        return builder.toString();
    }


}
