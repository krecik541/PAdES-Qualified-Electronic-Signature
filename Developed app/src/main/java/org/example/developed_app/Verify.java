package org.example.developed_app;

import java.io.BufferedReader;
import java.io.IOException;
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
import java.util.List;
import java.util.stream.Collectors;

public class Verify {
    private String documentPath;
    private String keyPath;

    public void init() throws Exception {
        Path pdfPath = Paths.get(documentPath);
        Path keyFilePath = Paths.get(keyPath);

        // 1. Wczytaj cały plik jako bajty
        byte[] allBytes = Files.readAllBytes(pdfPath);
        String fileContent = new String(allBytes, StandardCharsets.UTF_8);

        // 2. Znajdź podpis w pliku
        String beginMarker = "---BEGIN SIGNATURE---";
        String endMarker = "---END SIGNATURE---";

        int beginIndex = fileContent.indexOf(beginMarker);
        int endIndex = fileContent.indexOf(endMarker);

        if (beginIndex == -1 || endIndex == -1 || beginIndex >= endIndex) {
            System.out.println("❌ Nie znaleziono podpisu w pliku.");
            return;
        }

        // 3. Wyodrębnij podpis
        int base64Start = beginIndex + beginMarker.length();
        String base64Signature = fileContent.substring(base64Start, endIndex).replaceAll("\\s", "");
        byte[] digitalSignature = Base64.getDecoder().decode(base64Signature);

        String signatureSection = "\n---BEGIN SIGNATURE---\n"
                + Base64.getEncoder().encodeToString(digitalSignature)
                + "\n---END SIGNATURE---\n";
        System.out.println(signatureSection);

        // 4. Odtwórz oryginalny dokument bez podpisu
        byte[] unsignedPdfBytes = fileContent.substring(0, beginIndex).getBytes(StandardCharsets.UTF_8);

        // 5. Oblicz hash
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] calculatedHash = sha256.digest(unsignedPdfBytes);

        // 6. Wczytaj klucz publiczny
        String publicKeyPem = Files.lines(keyFilePath)
                .filter(line -> !line.contains("BEGIN") && !line.contains("END"))
                .collect(Collectors.joining());
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPem);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(keySpec);

        // 7. Zweryfikuj podpis
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
