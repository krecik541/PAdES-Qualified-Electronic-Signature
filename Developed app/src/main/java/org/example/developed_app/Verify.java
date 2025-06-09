package org.example.developed_app;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

public class Verify {
    private String documentPath;
    private String keyPath;

    public void init() throws Exception {
        Path pdfPath = Paths.get(documentPath);
        Path keyFilePath = Paths.get(keyPath);

        // 1. Wczytaj PDF i odczytaj podpis z metadanych
        PDDocument document = PDDocument.load(pdfPath.toFile());
        PDDocumentInformation info = document.getDocumentInformation();
        String signatureHex = info.getCustomMetadataValue("Signature");

        if (signatureHex == null || signatureHex.isEmpty()) {
            System.out.println("Nie znaleziono podpisu w metadanych PDF.");
            document.close(); // zamykamy wcześniej, bo dalej nie potrzebny
            return;
        }

        byte[] digitalSignature = hexToBytes(signatureHex);

        // 2. Oblicz hash tekstu PDF
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        PDFTextStripper stripper = new PDFTextStripper();
        String extractedText = stripper.getText(document);

        byte[] calculatedHash = sha256.digest(extractedText.getBytes(StandardCharsets.UTF_8));

        document.close();  // teraz zamykamy bezpiecznie

        // 3. Wczytaj klucz publiczny
        String publicKeyPem = Files.lines(keyFilePath)
                .filter(line -> !line.contains("BEGIN") && !line.contains("END"))
                .collect(Collectors.joining());
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPem);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(keySpec);

        // 4. Weryfikacja podpisu
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(calculatedHash);
        boolean isValid = signature.verify(digitalSignature);

        if (isValid) {
            System.out.println("✅ Podpis jest prawidłowy i zgodny z dokumentem.");
        } else {
            System.out.println("❌ Podpis jest nieprawidłowy lub dokument został zmodyfikowany.");
        }
    }


    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return result;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public String getKeyPath() {
        return keyPath;
    }
}
