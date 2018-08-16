package uk.gov.ida.restclient;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import uk.gov.ida.configuration.JerseyClientWithRetryBackoffConfiguration;

import javax.inject.Provider;
import javax.ws.rs.client.Client;
import java.net.ProxySelector;

public class ClientProvider implements Provider<Client> {

    private final Client client;

    public ClientProvider(
            Environment environment,
            JerseyClientConfiguration jerseyClientConfiguration,
            boolean enableRetryTimeOutConnections,
            String clientName) {

        JerseyClientBuilder jerseyClientBuilder = new JerseyClientBuilder(environment)
                .using(jerseyClientConfiguration)
                .using(getHttpRequestRetryHandler(jerseyClientConfiguration, enableRetryTimeOutConnections))
                .using(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));

        client = jerseyClientBuilder.build(clientName);
    }

    private HttpRequestRetryHandler getHttpRequestRetryHandler(JerseyClientConfiguration jerseyClientConfiguration, boolean enableRetryTimeOutConnections) {
        HttpRequestRetryHandler retryHandler;
        if (enableRetryTimeOutConnections) {
            if (jerseyClientConfiguration instanceof JerseyClientWithRetryBackoffConfiguration) {
                JerseyClientWithRetryBackoffConfiguration clientConfig = (JerseyClientWithRetryBackoffConfiguration) jerseyClientConfiguration;
                if (clientConfig.getRetryExceptionNames() == null) {
                    retryHandler = new TimeoutRequestRetryWithBackoffHandler(
                            jerseyClientConfiguration.getRetries(),
                            clientConfig.getRetryBackoffPeriod()
                    );
                } else {
                    retryHandler = new TimeoutRequestRetryWithBackoffHandler(
                            jerseyClientConfiguration.getRetries(),
                            clientConfig.getRetryBackoffPeriod(),
                            clientConfig.getRetryExceptionNames()
                    );
                }
            } else {
                retryHandler = new TimeoutRequestRetryHandler(jerseyClientConfiguration.getRetries());
            }
        } else {
            retryHandler = new StandardHttpRequestRetryHandler(0, false);
        }
        return retryHandler;
    }

    @Override
    public Client get() {
        return client;
    }
}
