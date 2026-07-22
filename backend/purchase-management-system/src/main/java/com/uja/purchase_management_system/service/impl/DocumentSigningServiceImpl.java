package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.entity.PurchaseOrder;
import com.uja.purchase_management_system.entity.OrderItem;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@Service
public class DocumentSigningServiceImpl {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Value("${signing.keystore.path}")
    private String keystorePath;

    @Value("${signing.keystore.password}")
    private String keystorePassword;

    private final ResourcePatternResolver resourceLoader;

    public DocumentSigningServiceImpl(ResourcePatternResolver resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Generates the initial PDF for an order and signs it with the requester's certificate.
     * This is the FIRST signature (the lecturer).
     */
    public byte[] generateAndSignByRequester(PurchaseOrder order, String signerAlias, String signerName) throws Exception {
        byte[] unsignedPdf = buildOrderPdf(order);
        return signPdf(unsignedPdf, signerAlias, signerName, "Purchase request submitted by lecturer");
    }

    /**
     * Adds a SECOND signature (the authorizer) on top of an already-signed PDF.
     * The existing signature is preserved; PAdES supports multiple sequential signatures.
     */
    public byte[] addAuthorizerSignature(byte[] alreadySignedPdf, String signerAlias, String signerName) throws Exception {
        return signPdf(alreadySignedPdf, signerAlias, signerName, "Purchase order authorized");
    }

    /**
     * Fallback: build + sign with the given alias in one shot (used if no stored PDF exists yet).
     */
    public byte[] generateSignedOrderPdf(PurchaseOrder order, String signerAlias, String signerName) throws Exception {
        byte[] unsignedPdf = buildOrderPdf(order);
        return signPdf(unsignedPdf, signerAlias, signerName, "Purchase order document");
    }

    private byte[] buildOrderPdf(PurchaseOrder order) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 50;
                float y = page.getMediaBox().getHeight() - margin;

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText("Universidad de Jaen - Purchase Order");
                cs.endText();
                y -= 30;

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                y = writeLine(cs, margin, y, "Order number (Pedido): " + safe(order.getOrderNumber()));
                y = writeLine(cs, margin, y, "File number (Expediente): " + safe(order.getFileNumber()));
                y = writeLine(cs, margin, y, "Requested by: " + order.getRequestedBy().getFullName()
                        + (order.getRequesterPhone() != null ? "  Tel: " + order.getRequesterPhone() : ""));
                y = writeLine(cs, margin, y, "Expenditure unit: " + order.getExpenditureUnit().getName()
                        + "  Code: " + order.getExpenditureUnit().getCode());
                y = writeLine(cs, margin, y, "Budget line: " + safe(order.getBudgetLineCode()));
                y = writeLine(cs, margin, y, "Period: " + safe(order.getPeriod()));
                y = writeLine(cs, margin, y, "Status: " + order.getStatus());
                y -= 15;

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                y = writeLine(cs, margin, y, "Line items");
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);

                for (OrderItem item : order.getItems()) {
                    String line = String.format("%-28s qty:%-3d price:%-8.2f VAT:%-3s%% supplier:%-18s total:%.2f",
                            truncate(item.getProductName(), 28), item.getQuantity(), item.getUnitPrice(),
                            item.getVatRate().stripTrailingZeros().toPlainString(),
                            truncate(item.getSupplier() != null ? item.getSupplier() : "-", 18),
                            item.getLineTotal());
                    y = writeLine(cs, margin, y, line);
                }

                y -= 10;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                y = writeLine(cs, margin, y, "TOTAL: " + order.getTotalAmount() + " EUR");

                y -= 20;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                y = writeLine(cs, margin, y, "Delivery: " + safe(order.getDeliveryBuilding()) + " / " + safe(order.getDeliveryRoom())
                        + "  Contact: " + safe(order.getDeliveryContactPerson()) + "  Tel: " + safe(order.getDeliveryPhone()));

                y -= 30;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
                writeLine(cs, margin, y, "Document generated: "
                        + java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        + ". Digital signatures are embedded in this file and can be verified in a PDF reader.");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private float writeLine(PDPageContentStream cs, float x, float y, String text) throws Exception {
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - 14;
    }

    private String truncate(String s, int max) { return s != null && s.length() > max ? s.substring(0, max) : (s != null ? s : ""); }
    private String safe(String s) { return s != null ? s : "-"; }

    private byte[] signPdf(byte[] pdfToSign, String alias, String signerName, String reason) throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        Resource resource = resourceLoader.getResource(keystorePath);
        try (InputStream is = resource.getInputStream()) {
            keystore.load(is, keystorePassword.toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, keystorePassword.toCharArray());
        if (privateKey == null) {
            throw new IllegalStateException("No signing certificate found for alias: " + alias);
        }
        Certificate[] certChain = keystore.getCertificateChain(alias);
        X509Certificate certificate = (X509Certificate) certChain[0];

        try (PDDocument doc = Loader.loadPDF(pdfToSign)) {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(signerName);
            signature.setReason(reason);
            signature.setLocation("Jaen, Spain");
            signature.setSignDate(Calendar.getInstance());

            SignatureInterface signatureInterface = content -> {
                try {
                    CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
                    ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
                    generator.addSignerInfoGenerator(
                            new JcaSignerInfoGeneratorBuilder(
                                    new JcaDigestCalculatorProviderBuilder().build())
                                    .build(sha256Signer, certificate));
                    generator.addCertificates(new JcaCertStore(java.util.List.of(certificate)));
                    CMSProcessableInputStream cmsData = new CMSProcessableInputStream(content);
                    CMSSignedData signedData = generator.generate(cmsData, false);
                    return signedData.getEncoded();
                } catch (Exception e) {
                    throw new RuntimeException("PDF signing failed", e);
                }
            };

            ByteArrayOutputStream signedOutput = new ByteArrayOutputStream();
            SignatureOptions options = new SignatureOptions();
            options.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
            doc.addSignature(signature, signatureInterface, options);
            doc.saveIncremental(signedOutput);
            return signedOutput.toByteArray();
        }
    }

    private static class CMSProcessableInputStream implements org.bouncycastle.cms.CMSTypedData {
        private final InputStream in;
        private final ASN1ObjectIdentifier contentType = org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.data;

        CMSProcessableInputStream(InputStream in) { this.in = in; }

        @Override public ASN1ObjectIdentifier getContentType() { return contentType; }
        @Override public Object getContent() { return in; }
        @Override public void write(java.io.OutputStream out) throws java.io.IOException {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        }
    }
}