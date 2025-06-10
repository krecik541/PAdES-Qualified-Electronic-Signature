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

/**
 * The KeyGenerator class is responsible for generating RSA key pairs and saving them securely.
 * It encrypts the private key using a PIN and saves both the public and private keys to files.
 */
public class KeyGenerator {

    /**
     * The PIN used for encrypting the private key.
     */
    private final String pin;

    /**
     * The path where the keys will be saved.
     */
    private final String pathToSave;


    /**
     * Konstruktor klasy KeyGenerator.
     *
     * @param pin        Hasło (PIN) używane do szyfrowania klucza prywatnego.
     * @param pathToSave Ścieżka do katalogu, w którym klucze będą zapisywane.
     */
    public KeyGenerator(String pin, String pathToSave) {
        this.pin = pin;
        this.pathToSave = pathToSave;
    }

    /**
     * Initializes the key generation process. Generates RSA key pairs, encrypts the private key,
     * and saves both keys to files.
     *
     * @throws Exception If an error occurs during key generation or encryption.
     */
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

    /**
     * Saves the given key string to a file.
     *
     * @param s   The key string to save.
     * @param pub Indicates whether the key is public or private.
     */
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
