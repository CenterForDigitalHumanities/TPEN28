package edu.slu.tpen.servlet.util;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import textdisplay.Folio;

public class EncryptUtil {

        static char[] HEX_CHARS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        private String iv = Folio.getRbTok("IV"); 
        private IvParameterSpec ivspec;
        private SecretKeySpec keyspec;
        private Cipher cipher;
        private String SecretKey = Folio.getRbTok("KEY");

        public EncryptUtil()
        {
            ivspec = new IvParameterSpec(iv.getBytes());

            keyspec = new SecretKeySpec(SecretKey.getBytes(), "AES");

            try {
                    cipher = Cipher.getInstance("AES/CBC/NoPadding");
            } catch (NoSuchAlgorithmException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
        }

        public byte[] encrypt(String text) throws Exception
        {
            if(text == null || text.length() == 0)
                    throw new Exception("Empty string");

            byte[] encrypted = null;

            try {
                    cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

                    encrypted = cipher.doFinal(padString(text).getBytes());
            } catch (Exception e)
            {                       
                    throw new Exception("[encrypt] " + e.getMessage());
            }

            return encrypted;
        }

        public byte[] decrypt(String code) throws Exception
        {
            if(code == null || code.length() == 0)
                    throw new Exception("Empty string");

            byte[] decrypted = null;

                    cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

                    decrypted = cipher.doFinal(hexToBytes(code));
                    //Remove trailing zeroes
                    if( decrypted.length > 0)
                    {
                        int trim = 0;
                        for( int i = decrypted.length - 1; i >= 0; i-- ) if( decrypted[i] == 0 ) trim++;

                        if( trim > 0 )
                        {
                            byte[] newArray = new byte[decrypted.length - trim];
                            System.arraycopy(decrypted, 0, newArray, 0, decrypted.length - trim);
                            decrypted = newArray;
                        }
                    }
            return decrypted;
        }      


        public static String bytesToHex(byte[] buf)
        {
            char[] chars = new char[2 * buf.length];
            for (int i = 0; i < buf.length; ++i)
            {
                chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
                chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
            }
            return new String(chars);
        }


        public static byte[] hexToBytes(String str) {
            if (str==null) {
                    return null;
            } else if (str.length() < 2) {
                    return null;
            } else {
                    int len = str.length() / 2;
                    byte[] buffer = new byte[len];
                    for (int i=0; i<len; i++) {
                            buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
                    }
                    return buffer;
            }
        }



        private static String padString(String source)
        {
            char paddingChar = 0;
            int size = 16;
            int x = source.length() % size;
            int padLength = size - x;

            for (int i = 0; i < padLength; i++)
            {
                    source += paddingChar;
            }

            return source;
        }
        
        
        
        
        /**
         * Test run
         * @param args
         * @throws UnsupportedEncodingException 
         */
        public static void main(String[] args) throws UnsupportedEncodingException{
            String sampleQueryStr1 = "45dd983c89cb66e1746568b9643788bb63f8d0210ebf0543c5d0a30c85a0d32bceaba341f15e34edf6fdfc7ce1febb88419fc98d4415762872efea47e849f32e59fe318b5688e9a7f5be66364642bc2d81fb1ecb836f75f67c50f25adcf33e6d234a59d153088d5a764fa1abbc041474060c7b59d1099fd39a7e00a2119acb75";
//            sampleQueryStr1 = new String(Base64.decodeBase64(sampleQueryStr1));
            System.out.println(sampleQueryStr1);
            EncryptUtil mcrypt = new EncryptUtil();
            
            /* Encrypt */

            String encrypted;
            try {
//                byte[] be = mcrypt.encrypt("id=dp_sunny_client&email=sunnywd.lee%40utoronto.ca&role=1&redirect_uri=https%3A%2F%2Fsunnyl.library.utoronto.ca%2Fhome");
//                for(int i = 0;i < be.length; i++){
//                    System.out.print(be[i]);
//                }
//                encrypted = MCrypt.bytesToHex( mcrypt.encrypt("id=dp_sunny_client&email=sunnywd.lee%40utoronto.ca&role=1&redirect_uri=https%3A%2F%2Fsunnyl.library.utoronto.ca%2Fhome") );
                /* Decrypt */
//                System.out.println("");
                String decrypted = URLDecoder.decode(new String( mcrypt.decrypt( sampleQueryStr1 ) ), "UTF-8");
                System.out.println(decrypted);
            } catch (Exception ex) {
                Logger.getLogger(EncryptUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
}