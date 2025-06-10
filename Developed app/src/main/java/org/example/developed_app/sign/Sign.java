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
 * Klasa odpowiedzialna za podpisywanie dokumentów PDF
 * przy użyciu zaszyfrowanego klucza prywatnego i algorytmu SHA256withRSA.
 */
public class Sign {

    /**
     * Flaga wskazująca, czy pendrive został rozpoznany.
     */
    private boolean isPendriveRecognized;

    /**
     * PIN użytkownika do odszyfrowania klucza.
     */
    private String pin = "";

    /**
     * Ścieżka do dokumentu PDF.
     */
    private String documentPath = "";

    /**
     * Ścieżka do pliku z kluczem prywatnym.
     */
    private String keyPath = "";

    /**
     * Konwertuje tablicę bajtów na postać szesnastkową (hex).
     *
     * @param bytes Tablica bajtów do konwersji.
     * @return Reprezentacja szesnastkowa bajtów.
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
     * Inicjalizuje proces podpisywania dokumentu PDF.
     *
     * <p>Ładuje klucz prywatny z zaszyfrowanego pliku, odszyfrowuje go za pomocą PIN-u,
     * oblicza hash zawartości PDF, podpisuje go, a następnie zapisuje podpis
     * w metadanych dokumentu PDF.</p>
     *
     * @throws Exception w przypadku błędu podczas odszyfrowywania, podpisywania lub zapisu dokumentu.
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
     * Sprawdza, czy pendrive został rozpoznany.
     *
     * @return {@code true} jeśli tak, {@code false} w przeciwnym razie.
     */
    public boolean isPendriveRecognized() {
        return isPendriveRecognized;
    }

    /**
     * Ustawia status rozpoznania pendrive'a.
     *
     * @param b Flaga wskazująca rozpoznanie pendrive'a.
     */
    public void setPendriveRecognized(boolean b) {
        this.isPendriveRecognized = b;
    }

    /**
     * Zwraca ścieżkę do dokumentu PDF.
     *
     * @return Ścieżka dokumentu.
     */
    public String getDocumentPath() {
        return documentPath;
    }

    /**
     * Ustawia ścieżkę do dokumentu PDF.
     *
     * @param documentPath Ścieżka dokumentu.
     */
    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    /**
     * Ustawia PIN użytkownika, który służy do odszyfrowania klucza prywatnego.
     *
     * @param pin PIN jako ciąg znaków.
     */
    public void setPin(String pin) {
        this.pin = pin;
    }

    /**
     * Zwraca ścieżkę do pliku z kluczem prywatnym.
     *
     * @return Ścieżka do klucza.
     */
    public String getKeyPath() {
        return keyPath;
    }

    /**
     * Ustawia ścieżkę do pliku z kluczem prywatnym.
     *
     * @param keyPath Ścieżka do klucza.
     */
    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }
}
