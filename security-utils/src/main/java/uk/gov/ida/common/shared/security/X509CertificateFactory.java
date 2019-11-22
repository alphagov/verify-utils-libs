package uk.gov.ida.common.shared.security;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

public class X509CertificateFactory {

    @Inject
    public X509CertificateFactory() {}

    public X509Certificate createCertificate(String partialCert) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            String fullCert;
            if (partialCert.contains("-----BEGIN CERTIFICATE-----")) {
                fullCert = partialCert;
            } else {
                fullCert = MessageFormat.format(
                        "-----BEGIN CERTIFICATE-----\n{0}\n-----END CERTIFICATE-----",
                        partialCert.trim()
                );
            }

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fullCert.getBytes(StandardCharsets.UTF_8));
            return (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
