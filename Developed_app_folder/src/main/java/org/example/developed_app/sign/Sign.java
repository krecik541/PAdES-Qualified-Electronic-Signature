package org.example.developed_app.sign;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * The `Sign` class is responsible for signing PDF documents using an encrypted private key
 * and the SHA256withRSA algorithm.
 */
public class Sign {

    /**
     * Flag indicating whether the USB drive has been recognized.
     */
    private boolean isPendriveRecognized;

    /**
     * User PIN for decrypting the private key.
     */
    private String pin = "";

    /**
     * Path to the PDF document.
     */
    private String documentPath = "";

    /**
     * Path to the file containing the private key.
     */
    private String keyPath = "";

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes The byte array to convert.
     * @return Hexadecimal representation of the bytes.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Initializes the process of signing a PDF document.
     *
     * <p>Loads the private key from an encrypted file, decrypts it using the PIN,
     * calculates the hash of the PDF content, signs it, and saves the signature
     * in the PDF metadata.</p>
     *
     * @throws Exception If an error occurs during decryption, signing, or saving the document.
     */
    public void init() throws Exception {
        String base64 = Files.lines(Paths.get(keyPath))
                .filter(line -> !line.contains("BEGIN") && !line.contains("END"))
                .collect(Collectors.joining());

        byte[] encryptedBytes = Base64.getDecoder().decode(base64);

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(pin.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec aesKey = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);

        byte[] decryptedKey = cipher.doFinal(encryptedBytes);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(keySpec);

        Path originalPath = Paths.get(documentPath);
        String fileName = originalPath.getFileName().toString().replace(".pdf", "");
        Path signedPath = originalPath.resolveSibling(fileName + "_signed.pdf");

        PDDocument document = PDDocument.load(originalPath.toFile());

        PDFTextStripper stripper = new PDFTextStripper();
        String extractedText = stripper.getText(document);
        byte[] hash = sha.digest(extractedText.getBytes(StandardCharsets.UTF_8));

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(hash);
        byte[] digitalSignature = signature.sign();
        String signatureHex = bytesToHex(digitalSignature);

        document.getDocumentInformation().setCustomMetadataValue("Signature", signatureHex);

        document.save(signedPath.toFile());
        document.close();

        System.out.println("Podpisany PDF zapisano jako: " + signedPath.getFileName());
        System.out.println("Podpis (hex): " + signatureHex);
    }

    /**
     * Checks whether the USB drive has been recognized.
     *
     * @return `true` if recognized, `false` otherwise.
     */
    public boolean isPendriveRecognized() {
        return isPendriveRecognized;
    }

    /**
     * Sets the USB recognition status.
     *
     * @param b Flag indicating USB recognition.
     */
    public void setPendriveRecognized(boolean b) {
        this.isPendriveRecognized = b;
    }

    /**
     * Gets the path to the PDF document.
     *
     * @return Path to the document.
     */
    public String getDocumentPath() {
        return documentPath;
    }

    /**
     * Sets the path to the PDF document.
     *
     * @param documentPath Path to the document.
     */
    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    /**
     * Sets the user PIN for decrypting the private key.
     *
     * @param pin PIN as a string.
     */
    public void setPin(String pin) {
        this.pin = pin;
    }

    /**
     * Gets the path to the file containing the private key.
     *
     * @return Path to the private key file.
     */
    public String getKeyPath() {
        return keyPath;
    }

    /**
     * Sets the path to the file containing the private key.
     *
     * @param keyPath Path to the private key file.
     */
    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }
}
