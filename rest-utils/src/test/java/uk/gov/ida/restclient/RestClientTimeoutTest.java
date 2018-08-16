package uk.gov.ida.restclient;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.dropwizard.util.Duration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.NoHttpResponseException;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.ida.configuration.JerseyClientWithRetryBackoffConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RestClientTimeoutTest {

    @ClassRule
    public static final DropwizardAppRule testAppRule = new DropwizardAppRule(TestApplication.class);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void socketTimeoutRetryWithBackoffTests_thirdCallSucceeds() {
        final String firstCallState = "Call 1 Complete";
        final String secondCallState = "Call 2 Complete";
        final String thirdCallState = "Call 3 Complete";
        final String scenarioName = "socket timeout scenario scenario";
        final String resourcePath = "/delayed-resource";


        stubFor(get(urlEqualTo(resourcePath)).inScenario(scenarioName)
            .withHeader("Accept", equalTo("application/json"))
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse()
                .withFixedDelay(2000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(format("{ \"message\": \"%s\" }", firstCallState)))
            .willSetStateTo(firstCallState)
        );

        stubFor(get(urlEqualTo(resourcePath)).inScenario(scenarioName)
            .withHeader("Accept", equalTo("application/json"))
            .whenScenarioStateIs(firstCallState)
            .willReturn(aResponse()
                .withFixedDelay(2000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(format("{ \"message\": \"%s\" }", secondCallState)))
            .willSetStateTo(secondCallState)
        );

        stubFor(get(urlEqualTo(resourcePath)).inScenario(scenarioName)
            .withHeader("Accept", equalTo("application/json"))
            .whenScenarioStateIs(secondCallState)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(format("{ \"message\": \"%s\" }", thirdCallState)))
            .willSetStateTo(thirdCallState)
        );

        JerseyClientWithRetryBackoffConfiguration clientConfig = new JerseyClientWithRetryBackoffConfiguration();
        clientConfig.setRetries(3);
        clientConfig.setRetryBackoffPeriod(Duration.milliseconds(1000));
        clientConfig.setTimeout(Duration.milliseconds(1000));
        clientConfig.setConnectionTimeout(Duration.milliseconds(1000));

        ClientProvider provider = new ClientProvider(
            testAppRule.getEnvironment(),
            clientConfig,
            true,
            "socketTimeoutRetryWithBackoffTests_thirdCallSucceedsClient"
        );
        Client client = provider.get();

        WebTarget target = client.target(format("http://localhost:%d%s", wireMockRule.port(), resourcePath));
        final Invocation.Builder request = target.request();
        long start = System.currentTimeMillis();

        final Response response = request.accept(MediaType.APPLICATION_JSON).get();

        int status = response.getStatus();
        long end = System.currentTimeMillis();

        assertEquals(200, status);
        Scenario scenario = this.getScenario(scenarioName);

        assertEquals(thirdCallState, scenario.getState());

        assertTrue((end - start) >= getTotalBackoffPeriod(2, clientConfig.getRetryBackoffPeriod()));
    }

    @Test
    public void multipleErrorRetryWithBackoffTests_forthCallSucceeds() {
        final String firstCallState = "Call 1 Complete";
        final String secondCallState = "Call 2 Complete";
        final String thirdCallState = "Call 3 Complete";
        final String forthCallState = "Call 4 Complete";
        final String scenarioName = "multiple error scenario";
        final String resourcePath = "/multiple-error-resource";


        stubFor(get(urlEqualTo(resourcePath)).inScenario(scenarioName)
            .withHeader("Accept", equalTo("application/json"))
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse()
                .withFixedDelay(2000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(format("{ \"message\": \"%s\" }", firstCallState)))
            .willSetStateTo(firstCallState)
        );

        stubFor(get(urlEqualTo(resourcePath)).inScenario(scenarioName)
            .withHeader("Accept", equalTo("application/json"))
            .whenScenarioStateIs(firstCallState)
            .willReturn(aResponse()
                .withFault(Fault.CONNECTION_RESET_BY_PEER))
            .willSetStateTo(secondCallState)
        );

        stubFor(get(urlEqualTo(resourcePath)).inScenario(scenarioName)
            .withHeader("Accept", equalTo("application/json"))
            .whenScenarioStateIs(secondCallState)
            .willReturn(aResponse()
                .withFault(Fault.EMPTY_RESPONSE)
            )
            .willSetStateTo(thirdCallState)
        );

        stubFor(get(urlEqualTo(resourcePath)).inScenario(scenarioName)
            .withHeader("Accept", equalTo("application/json"))
            .whenScenarioStateIs(thirdCallState)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(format("{ \"message\": \"%s\" }", forthCallState)))
            .willSetStateTo(forthCallState)
        );

        JerseyClientWithRetryBackoffConfiguration clientConfig = new JerseyClientWithRetryBackoffConfiguration();
        clientConfig.setRetries(3);
        clientConfig.setRetryBackoffPeriod(Duration.milliseconds(1000));
        clientConfig.setTimeout(Duration.milliseconds(1000));
        clientConfig.setConnectionTimeout(Duration.milliseconds(1000));
        clientConfig.setRetryExceptionNames(Arrays.asList(new String[]{
            NoHttpResponseException.class.getName(),
            SocketTimeoutException.class.getName(),
            SocketException.class.getName()
        }));

        ClientProvider provider = new ClientProvider(
            testAppRule.getEnvironment(),
            clientConfig,
            true,
            "multipleErrorRetryWithBackoffTests_forthCallSucceedssClient"
        );
        Client client = provider.get();

        WebTarget target = client.target(format("http://localhost:%d%s", wireMockRule.port(), resourcePath));
        final Invocation.Builder request = target.request();
        long start = System.currentTimeMillis();

        final Response response = request.accept(MediaType.APPLICATION_JSON).get();

        int status = response.getStatus();
        long end = System.currentTimeMillis();
        Scenario scenario = this.getScenario(scenarioName);

        assertEquals(200, status);
        assertEquals(forthCallState, scenario.getState());
        assertTrue((end - start) >= getTotalBackoffPeriod(3, clientConfig.getRetryBackoffPeriod()));
    }


    @Test
    public void emptyResponseRetryWithBackoffTests_secondCallSucceeds() {
        final String firstCallState = "Call 1 Complete";
        final String secondCallState = "Call 2 Complete";
        final String scenarioName = "empty response scenario";
        final String resourcePath = "/empty-resource";

        stubFor(get(urlEqualTo(resourcePath)).inScenario(scenarioName)
            .withHeader("Accept", equalTo("application/json"))
            .whenScenarioStateIs(STARTED)
            .willSetStateTo(firstCallState)
            .willReturn(aResponse()
                .withFault(Fault.EMPTY_RESPONSE)
            )
        );

        stubFor(get(urlEqualTo(resourcePath)).inScenario(scenarioName)
            .withHeader("Accept", equalTo("application/json"))
            .whenScenarioStateIs(firstCallState)
            .willSetStateTo(secondCallState)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(format("{ \"message\": \"%s\" }", secondCallState))
            )
        );

        JerseyClientWithRetryBackoffConfiguration clientConfig = new JerseyClientWithRetryBackoffConfiguration();
        clientConfig.setRetries(3);
        clientConfig.setRetryBackoffPeriod(Duration.milliseconds(2000));
        clientConfig.setTimeout(Duration.milliseconds(1000));
        clientConfig.setConnectionTimeout(Duration.milliseconds(1000));
        clientConfig.setRetryExceptionNames(Arrays.asList(new String[]{
                NoHttpResponseException.class.getName()
        }));

       ClientProvider provider = new ClientProvider(
            testAppRule.getEnvironment(),
            clientConfig,
            true,
            "emptyResponseRetryWithBackoffTests_secondCallSucceedsClient"
        );
        Client client = provider.get();

        WebTarget target = client.target(format("http://localhost:%d%s", wireMockRule.port(), resourcePath));

        final Invocation.Builder request = target.request();
        long start = System.currentTimeMillis();

        final Response response = request.accept(MediaType.APPLICATION_JSON).get();

        int status = response.getStatus();
        long end = System.currentTimeMillis();

        assertEquals(200, status);
        assertEquals(secondCallState, getAllScenarios().get(0).getState());
        assertTrue((end - start) >= clientConfig.getRetryBackoffPeriod().toMilliseconds());
    }

    private long getTotalBackoffPeriod(int retries, Duration duration){
        return ((retries * (retries + 1)) / 2) * duration.toMilliseconds();
    }

    private Scenario getScenario(String scenarioName) {
        for( Scenario s: getAllScenarios()) {
            if (s.getName().equals(scenarioName)) return s;
        }
        throw new RuntimeException(format("Scenario not found: {0} ", scenarioName));
    }
}
