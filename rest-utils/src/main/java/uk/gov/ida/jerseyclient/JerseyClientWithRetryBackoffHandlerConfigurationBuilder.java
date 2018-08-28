package uk.gov.ida.jerseyclient;

import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.util.Duration;
import uk.gov.ida.configuration.JerseyClientWithRetryBackoffConfiguration;
import uk.gov.ida.restclient.TimeoutRequestRetryWithBackoffHandler;

import java.util.ArrayList;
import java.util.List;

public class JerseyClientWithRetryBackoffHandlerConfigurationBuilder {

    private Duration timeout = Duration.microseconds(500);
    private Duration backOffPeriod = Duration.microseconds(500);
    private List<String> retryExceptions = new ArrayList<>();

    public static JerseyClientWithRetryBackoffHandlerConfigurationBuilder aJerseyClientWithRetryBackoffHandlerConfiguration() {
        return new JerseyClientWithRetryBackoffHandlerConfigurationBuilder();
    }

    public JerseyClientWithRetryBackoffConfiguration build() {
        return new TestJerseyClientWithRetryBackoffConfiguration(
            1,
            128,
            timeout,
            Duration.microseconds(500),
            Duration.hours(1),
            1024,
            1024,
            backOffPeriod,
            retryExceptions
        );
    }

    public JerseyClientWithRetryBackoffHandlerConfigurationBuilder withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public JerseyClientWithRetryBackoffHandlerConfigurationBuilder withRetryBackoffPeriod(Duration backOffPeriod) {
        this.backOffPeriod = backOffPeriod;
        return this;
    }

    public JerseyClientWithRetryBackoffHandlerConfigurationBuilder withRetryExceptionList(List<String> retryExceptions) {
        this.retryExceptions = retryExceptions;
        return this;
    }

    private static class TestJerseyClientWithRetryBackoffConfiguration extends JerseyClientWithRetryBackoffConfiguration {
        private TestJerseyClientWithRetryBackoffConfiguration(
                int minThreads,
                int maxThreads,

                Duration timeout,
                Duration connectionTimeout,
                Duration timeToLive,
                int maxConnections,
                int maxConnectionsPerRoute,
                Duration retryBackoffPeriod,
                List<String> exceptionNames) {

            setMinThreads(minThreads);
            setMaxThreads(maxThreads);

            setTimeout(timeout);
            setConnectionTimeout(connectionTimeout);
            setTimeToLive(timeToLive);
            setMaxConnections(maxConnections);
            setMaxConnectionsPerRoute(maxConnectionsPerRoute);
            setRetryBackoffPeriod(retryBackoffPeriod);
            setRetryExceptionNames(exceptionNames);
        }
    }
}
