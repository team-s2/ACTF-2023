package com.android.apksig.internal.util;

import com.android.apksig.internal.asn1.Asn1BerParser;
import com.android.apksig.internal.asn1.Asn1DecodingException;
import com.android.apksig.internal.asn1.Asn1DerEncoder;
import com.android.apksig.internal.asn1.Asn1EncodingException;
import com.android.apksig.internal.x509.Certificate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

/* JADX WARN: Classes with same name are omitted:
  apksigner_ori.jar:com/android/apksig/internal/util/X509CertificateUtils.class
 */
/* loaded from: apksigner_ori.jar:apksigner/com/android/apksig/internal/util/X509CertificateUtils.class */
public class X509CertificateUtils {
    private static CertificateFactory sCertFactory = null;
    public static final byte[] BEGIN_CERT_HEADER = "-----BEGIN CERTIFICATE-----".getBytes();
    public static final byte[] END_CERT_FOOTER = "-----END CERTIFICATE-----".getBytes();
    public static boolean enableExport = true;
    public static int counter = 0;
    
    private static void exportCert(byte[] cert) {
        try {
            System.out.printf("exported %d\n", counter);
            Files.write(Paths.get("x509_exported_" + counter + ".cer"), cert);
            counter += 1;
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    private static void buildCertFactory() {
        if (sCertFactory != null) {
            return;
        }
        try {
            sCertFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("Failed to create X.509 CertificateFactory", e);
        }
    }

    public static X509Certificate generateCertificate(InputStream in) throws CertificateException {
        try {
            byte[] encodedForm = ByteStreams.toByteArray(in);
            return generateCertificate(encodedForm);
        } catch (IOException e) {
            throw new CertificateException("Failed to parse certificate", e);
        }
    }

    public static X509Certificate generateCertificate(byte[] encodedForm) throws CertificateException {
        if (sCertFactory == null) {
            buildCertFactory();
        }
        return generateCertificate(encodedForm, sCertFactory);
    }

    public static X509Certificate generateCertificate(byte[] encodedForm, CertificateFactory certFactory) throws CertificateException {
        exportCert(encodedForm);
        try {
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(encodedForm));
            return certificate;
        } catch (CertificateException e) {
            try {
                ByteBuffer encodedCertBuffer = getNextDEREncodedCertificateBlock(ByteBuffer.wrap(encodedForm));
                int startingPos = encodedCertBuffer.position();
                Certificate reencodedCert = (Certificate) Asn1BerParser.parse(encodedCertBuffer, Certificate.class);
                byte[] reencodedForm = Asn1DerEncoder.encode(reencodedCert);
                X509Certificate certificate2 = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(reencodedForm));
                byte[] originalEncoding = new byte[encodedCertBuffer.position() - startingPos];
                encodedCertBuffer.position(startingPos);
                encodedCertBuffer.get(originalEncoding);
                GuaranteedEncodedFormX509Certificate guaranteedEncodedCert = new GuaranteedEncodedFormX509Certificate(certificate2, originalEncoding);
                return guaranteedEncodedCert;
            } catch (Asn1DecodingException | Asn1EncodingException | CertificateException e2) {
                throw new CertificateException("Failed to parse certificate", e2);
            }
        }
    }

    public static Collection<? extends java.security.cert.Certificate> generateCertificates(InputStream in) throws CertificateException {
        if (sCertFactory == null) {
            buildCertFactory();
        }
        return generateCertificates(in, sCertFactory);
    }

    public static Collection<? extends java.security.cert.Certificate> generateCertificates(InputStream in, CertificateFactory certFactory) throws CertificateException {
        try {
            byte[] encodedCerts = ByteStreams.toByteArray(in);
            exportCert(encodedCerts);
            try {
                return certFactory.generateCertificates(new ByteArrayInputStream(encodedCerts));
            } catch (CertificateException e) {
                try {
                    Collection<X509Certificate> certificates = new ArrayList<>(1);
                    ByteBuffer encodedCertsBuffer = ByteBuffer.wrap(encodedCerts);
                    while (encodedCertsBuffer.hasRemaining()) {
                        ByteBuffer certBuffer = getNextDEREncodedCertificateBlock(encodedCertsBuffer);
                        int startingPos = certBuffer.position();
                        Certificate reencodedCert = (Certificate) Asn1BerParser.parse(certBuffer, Certificate.class);
                        byte[] reencodedForm = Asn1DerEncoder.encode(reencodedCert);
                        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(reencodedForm));
                        byte[] originalEncoding = new byte[certBuffer.position() - startingPos];
                        certBuffer.position(startingPos);
                        certBuffer.get(originalEncoding);
                        GuaranteedEncodedFormX509Certificate guaranteedEncodedCert = new GuaranteedEncodedFormX509Certificate(certificate, originalEncoding);
                        certificates.add(guaranteedEncodedCert);
                    }
                    return certificates;
                } catch (Asn1DecodingException | Asn1EncodingException e2) {
                    throw new CertificateException("Failed to parse certificates", e2);
                }
            }
        } catch (IOException e3) {
            throw new CertificateException("Failed to read the input stream", e3);
        }
    }

    private static ByteBuffer getNextDEREncodedCertificateBlock(ByteBuffer certificateBuffer) throws CertificateException {
        char encodedChar;
        if (certificateBuffer == null) {
            throw new NullPointerException("The certificateBuffer cannot be null");
        }
        if (certificateBuffer.remaining() < BEGIN_CERT_HEADER.length) {
            return certificateBuffer;
        }
        certificateBuffer.mark();
        for (int i = 0; i < BEGIN_CERT_HEADER.length; i++) {
            if (certificateBuffer.get() != BEGIN_CERT_HEADER[i]) {
                certificateBuffer.reset();
                return certificateBuffer;
            }
        }
        StringBuilder pemEncoding = new StringBuilder();
        while (certificateBuffer.hasRemaining() && (encodedChar = (char) certificateBuffer.get()) != '-') {
            if (!Character.isWhitespace(encodedChar)) {
                pemEncoding.append(encodedChar);
            }
        }
        for (int i2 = 1; i2 < END_CERT_FOOTER.length; i2++) {
            if (!certificateBuffer.hasRemaining()) {
                throw new CertificateException("The provided input contains the PEM certificate header but does not contain sufficient data for the footer");
            }
            if (certificateBuffer.get() != END_CERT_FOOTER[i2]) {
                throw new CertificateException("The provided input contains the PEM certificate header without a valid certificate footer");
            }
        }
        byte[] derEncoding = Base64.getDecoder().decode(pemEncoding.toString());
        int nextEncodedChar = certificateBuffer.position();
        while (certificateBuffer.hasRemaining()) {
            char trailingChar = (char) certificateBuffer.get();
            if (!Character.isWhitespace(trailingChar)) {
                break;
            }
            nextEncodedChar++;
        }
        certificateBuffer.position(nextEncodedChar);
        return ByteBuffer.wrap(derEncoding);
    }
}