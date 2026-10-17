package com.tamo.service;
import java.security.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.*;
public final class PasswordSecurity{
 private static final int ITERATIONS=600000;
 public static boolean encoded(String value){return value!=null&&value.matches("pbkdf2\\$[0-9]+\\$[A-Za-z0-9+/=]+\\$[A-Za-z0-9+/=]+");}
 public static String hash(String password){if(password==null||password.isBlank())throw new IllegalArgumentException("Password must not be empty.");byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);return "pbkdf2$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(derive(password,salt,ITERATIONS));}
 private static byte[] derive(String password,byte[] salt,int iterations){PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,iterations,256);try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}catch(GeneralSecurityException e){throw new IllegalStateException(e);}finally{spec.clearPassword();}}
 public static boolean verify(String password,String stored){if(password==null||stored==null)return false;if(!encoded(stored))return MessageDigest.isEqual(password.getBytes(java.nio.charset.StandardCharsets.UTF_8),stored.getBytes(java.nio.charset.StandardCharsets.UTF_8));try{String[] parts=stored.split("\\$");int iterations=Integer.parseInt(parts[1]);if(iterations<100000||iterations>2000000)return false;return MessageDigest.isEqual(Base64.getDecoder().decode(parts[3]),derive(password,Base64.getDecoder().decode(parts[2]),iterations));}catch(IllegalArgumentException e){return false;}}
}
