package uk.gov.ida.truststore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientTrustStoreConfiguration {

    @Valid
    @NotNull
    protected String path;

    @Valid
    @NotNull
    @Size(min = 1)
    protected String password;

    @Valid
    protected boolean enabled = true;

    protected ClientTrustStoreConfiguration() {
    }

    public ClientTrustStoreConfiguration(String path, String password) {
        this.path = path;
        this.password = password;
    }

    public ClientTrustStoreConfiguration(String path, String password, boolean enabled) {
        this.path = path;
        this.password = password;
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
