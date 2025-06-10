package org.example.developed_app.verify;

import javafx.scene.text.Text;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * Klasa Verify umożliwia weryfikację integralności dokumentu PDF
 * poprzez porównanie podpisu cyfrowego przechowywanego w metadanych
 * z hashem zawartości dokumentu.
 */
public class Verify {

    private String documentPath;
    private String keyPath;

    /**
     * Inicjuje proces weryfikacji podpisu dokumentu PDF.
     *
     * @param correctLabel Tekst wyświetlany, gdy dokument jest poprawny.
     * @param changedLabel Tekst wyświetlany, gdy dokument został zmodyfikowany.
     * @throws Exception w przypadku błędu odczytu pliku lub weryfikacji podpisu.
     */
    public void init(Text correctLabel, Text changedLabel) throws Exception {
        byte[] digitalSignature;
        byte[] calculatedHash;

        try (PDDocument document = PDDocument.load(Paths.get(documentPath).toFile())) {
            digitalSignature = extractSignatureFromMetadata(document);
            if (digitalSignature == null) {
                System.out.println("Nie znaleziono podpisu w metadanych PDF.");
                return;
            }

            String extractedText = new PDFTextStripper().getText(document);
            calculatedHash = computeSHA256(extractedText.getBytes(StandardCharsets.UTF_8));
        }

        PublicKey publicKey = loadPublicKey();
        boolean isValid = verifySignature(calculatedHash, digitalSignature, publicKey);

        if (isValid) {
            System.out.println("Poprawne");
            correctLabel.setVisible(true);
            changedLabel.setVisible(false);
        } else {
            System.out.println("Niepoprawne");
            correctLabel.setVisible(false);
            changedLabel.setVisible(true);
        }
    }

    /**
     * Pobiera podpis cyfrowy z metadanych dokumentu PDF.
     *
     * @param document dokument PDF.
     * @return tablica bajtów podpisu lub {@code null}, jeśli nie znaleziono.
     */
    private byte[] extractSignatureFromMetadata(PDDocument document) {
        PDDocumentInformation info = document.getDocumentInformation();
        String signatureHex = info.getCustomMetadataValue("Signature");

        if (signatureHex == null || signatureHex.isEmpty()) {
            return null;
        }

        return hexToBytes(signatureHex);
    }

    /**
     * Oblicza hash SHA-256 na podstawie danych wejściowych.
     *
     * @param data dane do skrócenia.
     * @return tablica bajtów zawierająca hash SHA-256.
     * @throws NoSuchAlgorithmException jeśli algorytm nie jest obsługiwany.
     */
    private byte[] computeSHA256(byte[] data) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    /**
     * Ładuje klucz publiczny z pliku PEM.
     *
     * @return obiekt PublicKey.
     * @throws Exception w przypadku błędu odczytu lub parsowania klucza.
     */
    private PublicKey loadPublicKey() throws Exception {
        String keyBase64 = Files.lines(Paths.get(keyPath))
                .filter(line -> !line.contains("BEGIN") && !line.contains("END"))
                .collect(Collectors.joining());

        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    /**
     * Weryfikuje podpis cyfrowy na podstawie hasha i klucza publicznego.
     *
     * @param hash           obliczony hash danych.
     * @param signatureBytes podpis do weryfikacji.
     * @param publicKey      klucz publiczny.
     * @return {@code true}, jeśli podpis jest poprawny; w przeciwnym razie {@code false}.
     * @throws Exception w przypadku błędu weryfikacji.
     */
    private boolean verifySignature(byte[] hash, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(hash);
        return signature.verify(signatureBytes);
    }

    /**
     * Konwertuje ciąg szesnastkowy na tablicę bajtów.
     *
     * @param hex ciąg szesnastkowy.
     * @return tablica bajtów.
     */
    private byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] result = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return result;
    }

    /**
     * Zwraca ścieżkę do dokumentu PDF.
     *
     * @return ścieżka do dokumentu.
     */
    public String getDocumentPath() {
        return documentPath;
    }

    /**
     * Ustawia ścieżkę do dokumentu PDF.
     *
     * @param documentPath ścieżka do dokumentu.
     */
    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    /**
     * Zwraca ścieżkę do pliku z kluczem publicznym.
     *
     * @return ścieżka do pliku klucza.
     */
    public String getKeyPath() {
        return keyPath;
    }

    /**
     * Ustawia ścieżkę do pliku z kluczem publicznym.
     *
     * @param keyPath ścieżka do pliku klucza.
     */
    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }
}
