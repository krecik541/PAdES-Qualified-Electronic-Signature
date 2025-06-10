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
 * The `Verify` class enables verification of the integrity of a PDF document
 * by comparing the digital signature stored in its metadata with the hash of its content.
 */
public class Verify {

    /**
     * Path to the PDF document to be verified.
     */
    private String documentPath;

    /**
     * Path to the file containing the public key.
     */
    private String keyPath;

    /**
     * Initializes the process of verifying the signature of a PDF document.
     *
     * @param correctLabel Text displayed when the document is valid.
     * @param changedLabel Text displayed when the document has been modified.
     * @throws Exception If an error occurs during file reading or signature verification.
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
     * Extracts the digital signature from the metadata of a PDF document.
     *
     * @param document The PDF document.
     * @return A byte array containing the signature, or `null` if not found.
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
     * Computes the SHA-256 hash of the input data.
     *
     * @param data The data to hash.
     * @return A byte array containing the SHA-256 hash.
     * @throws NoSuchAlgorithmException If the algorithm is not supported.
     */
    private byte[] computeSHA256(byte[] data) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    /**
     * Loads the public key from a PEM file.
     *
     * @return The `PublicKey` object.
     * @throws Exception If an error occurs during file reading or key parsing.
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
     * Verifies the digital signature using the hash and public key.
     *
     * @param hash The computed hash of the data.
     * @param signatureBytes The signature to verify.
     * @param publicKey The public key.
     * @return `true` if the signature is valid; otherwise, `false`.
     * @throws Exception If an error occurs during verification.
     */
    private boolean verifySignature(byte[] hash, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(hash);
        return signature.verify(signatureBytes);
    }

    /**
     * Converts a hexadecimal string to a byte array.
     *
     * @param hex The hexadecimal string.
     * @return A byte array.
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
     * Gets the path to the PDF document.
     *
     * @return The path to the document.
     */
    public String getDocumentPath() {
        return documentPath;
    }

    /**
     * Sets the path to the PDF document.
     *
     * @param documentPath The path to the document.
     */
    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    /**
     * Gets the path to the file containing the public key.
     *
     * @return The path to the key file.
     */
    public String getKeyPath() {
        return keyPath;
    }

    /**
     * Sets the path to the file containing the public key.
     *
     * @param keyPath The path to the key file.
     */
    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }
}
