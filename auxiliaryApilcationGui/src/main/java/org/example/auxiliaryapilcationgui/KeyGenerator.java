package org.example.auxiliaryapilcationgui;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static java.lang.Integer.min;

public class KeyGenerator {
    private final String pin;
    private final String pathToSave;

    public KeyGenerator(String pin, String pathToSave) {
        this.pin = pin;
        this.pathToSave = pathToSave;
    }

    public void init() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);

        KeyPair keyPair = keyGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        System.out.println("\nKlucz prywatny (Base64):");
        System.out.println(Base64.getEncoder().encodeToString(privateKey.getEncoded()));

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(pin.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedPrivateKey = cipher.doFinal(privateKey.getEncoded());

        saveToFile(Base64.getEncoder().encodeToString(publicKey.getEncoded()), true);
        saveToFile(Base64.getEncoder().encodeToString(encryptedPrivateKey), false);
    }

    private void saveToFile(String s, boolean pub) {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(pathToSave + (pub ? "\\public.pem" : "\\private.enc"))));
            writer.printf("-----BEGIN %s KEY-----\n", pub ? "PUBLIC" : "PRIVATE");
            for (int i = 0; i < s.length(); i += 64) {
                writer.println(s.substring(i, min(i + 64, s.length())));
            }
            writer.printf("-----END %s KEY-----\n", pub ? "PUBLIC" : "PRIVATE");
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
