package uk.gov.ida.common.shared.security;

import io.prometheus.client.Gauge;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * <p>A class to register prometheus metrics for expiry dates of certificates in truststores.</p>
 * <p>The metrics will be in the form:</p>
 * <pre>
 * verify_trust_store_certificate_expiry_date{truststore="$name",subject="$subject_dn",serial="$serial"}
 * </pre>
 */
public class TrustStoreMetrics {
    private Gauge expiryDateGauge;

    /**
     * Create a new TrustStoreMetrics.  This will automatically register metrics with the default Prometheus
     * CollectorRegistry.
     */
    public TrustStoreMetrics() {
        expiryDateGauge = Gauge.build("verify_trust_store_certificate_expiry_date", "Expiry date (in unix time milliseconds) of a certificate in a Java truststore")
                .labelNames("truststore", "subject", "serial")
                .register();
    }

    /**
     * Captures metrics for the certificates in a truststore.
     * @param name
     *   A friendly name for the truststore. This will be set as the <code>truststore</code> label on the metric
     * @param trustStore
     *   The truststore containing certificates to output metrics for
     */
    public void registerTrustStore(String name, KeyStore trustStore) {
        try {
            Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
                expiryDateGauge.labels(name, certificate.getSubjectDN().getName(), certificate.getSerialNumber().toString(10))
                        .set(certificate.getNotAfter().getTime());
            }
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
