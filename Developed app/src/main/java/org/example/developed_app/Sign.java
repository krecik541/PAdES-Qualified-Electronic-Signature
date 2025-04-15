package org.example.developed_app;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;


public class Sign {
    private boolean isPendriveRecognized;
    private String pin = "";
    private String documentPath = "";
    private String keyPath = "";

    public void init() throws Exception {
        String base64 = Files.lines(Paths.get(keyPath))
                .filter(line -> !line.contains("BEGIN") && !line.contains("END"))
                .collect(Collectors.joining());

        byte[] encryptedBytes = Base64.getDecoder().decode(base64);

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(pin.getBytes("UTF-8"));
        SecretKeySpec aesKey = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);

        byte[] decryptedKey = cipher.doFinal(encryptedBytes);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(keySpec);

//        System.out.println("\nKlucz prywatny (Base64):");
//        System.out.println(Base64.getEncoder().encodeToString(kf.generatePrivate(keySpec).getEncoded()));

        Path originalPath = Paths.get(documentPath);
        String fileName = originalPath.getFileName().toString().replace(".pdf", "");
        Path signedPath = originalPath.resolveSibling(fileName + "_signed.pdf");

        PDDocument document = PDDocument.load(originalPath.toFile());

        PDPage lastPage = document.getPage(document.getNumberOfPages() - 1);

        byte[] originalPdfBytes = Files.readAllBytes(originalPath);
        byte[] hash = sha.digest(originalPdfBytes);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, lastPage, PDPageContentStream.AppendMode.APPEND, true)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.newLineAtOffset(50, 50);
            String displayText = "Signed (PIN *****) on " + LocalDate.now();
            contentStream.showText(displayText);
            contentStream.newLineAtOffset(0, -14); // nowa linia
            contentStream.showText("Document hash (SHA-256):");
            contentStream.newLineAtOffset(0, -14);
            contentStream.showText(hashBase64.substring(0, Math.min(hashBase64.length(), 64)));
            if (hashBase64.length() > 64) {
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText(hashBase64.substring(64));
            }
            contentStream.endText();
        }

        document.save(signedPath.toFile());
        document.close();

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(hash);
        byte[] digitalSignature = signature.sign();

        String signatureSection = "\n---BEGIN SIGNATURE---\n"
                + Base64.getEncoder().encodeToString(digitalSignature)
                + "\n---END SIGNATURE---\n";

        Files.write(signedPath, signatureSection.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

        System.out.println(signatureSection);

        System.out.println("✅ Podpisany PDF zapisano jako: " + signedPath.getFileName());
        System.out.println("🔒 Hash (Base64): " + hashBase64);
    }

    public boolean isPendriveRecognized() {
        return isPendriveRecognized;
    }

    public void setPendriveRecognized(boolean b) {
        this.isPendriveRecognized = b;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }
}
