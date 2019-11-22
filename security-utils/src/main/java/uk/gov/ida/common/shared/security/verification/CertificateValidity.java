package uk.gov.ida.common.shared.security.verification;

import java.security.cert.CertPathValidatorException;
import java.util.Optional;

public class CertificateValidity {
    private final CertPathValidatorException exception;

    public static CertificateValidity valid() {
        return new CertificateValidity(null);
    }

    public static CertificateValidity invalid(CertPathValidatorException e) {
        return new CertificateValidity(e);
    }

    private CertificateValidity(CertPathValidatorException exception) {
        this.exception = exception;
    }

    public boolean isValid() {
        return exception == null;
    }

    public Optional<CertPathValidatorException> getException() {
        return Optional.ofNullable(exception);
    }

}
