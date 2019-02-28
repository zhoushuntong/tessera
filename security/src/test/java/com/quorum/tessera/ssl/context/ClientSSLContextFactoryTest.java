package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;
import com.quorum.tessera.config.util.EnvironmentVariables;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.trust.TrustOnFirstUseManager;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientSSLContextFactoryTest {

    private EnvironmentVariableProvider envVarProvider;

    @Before
    public void setUp() {
        envVarProvider = EnvironmentVariableProviderFactory.load().create();
        when(envVarProvider.getEnv(EnvironmentVariables.clientKeyStorePwd)).thenReturn(null);
        when(envVarProvider.getEnv(EnvironmentVariables.clientTrustStorePwd)).thenReturn(null);
    }

    @Test
    public void createFromConfig() throws Exception {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
        when(config.getClientTrustMode()).thenReturn(SslTrustMode.CA);
        when(config.getClientKeyStore()).thenReturn(keyStore);
        when(config.getClientKeyStorePassword()).thenReturn("password");
        when(config.getClientTrustStore()).thenReturn(trustStore);
        when(config.getClientTrustStorePassword()).thenReturn("password");
        when(config.getKnownServersFile()).thenReturn(knownServers);

        SSLContext result = ClientSSLContextFactory.create().from("localhost",config);

        assertThat(result).isNotNull();

    }

    @Test
    public void createFromConfigWithDefaultKnownServers() throws URISyntaxException {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());

        when(config.getClientTrustMode()).thenReturn(SslTrustMode.TOFU);
        when(config.getClientKeyStore()).thenReturn(keyStore);
        when(config.getClientKeyStorePassword()).thenReturn("password");
        when(config.getKnownServersFile()).thenReturn(null);

        SSLContext result = ClientSSLContextFactory.create().from("localhost",config);

        assertThat(result).isNotNull()
            .extracting("contextSpi").isNotNull()
            .extracting("trustManager").isNotNull()
            .extracting("tm").isNotNull()
            .hasAtLeastOneElementOfType(TrustOnFirstUseManager.class)
            .extracting("knownHostsFile").asList().first().isEqualTo(Paths.get("knownServers"));
    }

    @Test
    public void getClientKeyStorePasswordOnlyConfigSetReturnsConfigValue() {
        ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getClientKeyStorePassword()).thenReturn(password);

        when(envVarProvider.getEnv(EnvironmentVariables.clientKeyStorePwd)).thenReturn(null);

        String result = factory.getClientKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getClientKeyStorePasswordOnlyEnvSetReturnsEnvValue() {
        ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getClientKeyStorePassword()).thenReturn(null);

        when(envVarProvider.getEnv(EnvironmentVariables.clientKeyStorePwd)).thenReturn(password);

        String result = factory.getClientKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getClientKeyStorePasswordEnvAndConfigSetReturnsEnvValue() {
        ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

        String configPassword = "config";
        String envPassword = "env";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getClientKeyStorePassword()).thenReturn(configPassword);

        when(envVarProvider.getEnv(EnvironmentVariables.clientKeyStorePwd)).thenReturn(envPassword);

        String result = factory.getClientKeyStorePassword(sslConfig);

        assertThat(result).isEqualTo(envPassword);
    }

    @Test
    public void getClientTrustStorePasswordOnlyConfigSetReturnsConfigValue() {
        ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getClientTrustStorePassword()).thenReturn(password);

        when(envVarProvider.getEnv(EnvironmentVariables.clientTrustStorePwd)).thenReturn(null);

        String result = factory.getClientTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getClientTrustStorePasswordOnlyEnvSetReturnsEnvValue() {
        ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

        String password = "password";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getClientTrustStorePassword()).thenReturn(null);

        when(envVarProvider.getEnv(EnvironmentVariables.clientTrustStorePwd)).thenReturn(password);

        String result = factory.getClientTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(password);
    }

    @Test
    public void getClientTrustStorePasswordEnvAndConfigSetReturnsEnvValue() {
        ClientSSLContextFactoryImpl factory = new ClientSSLContextFactoryImpl();

        String configPassword = "config";
        String envPassword = "env";

        SslConfig sslConfig = mock(SslConfig.class);
        when(sslConfig.getClientTrustStorePassword()).thenReturn(configPassword);

        when(envVarProvider.getEnv(EnvironmentVariables.clientTrustStorePwd)).thenReturn(envPassword);

        String result = factory.getClientTrustStorePassword(sslConfig);

        assertThat(result).isEqualTo(envPassword);
    }

    @Test(expected = TesseraSecurityException.class)
    public void securityExceptionsAreThrownAsTesseraException() throws Exception {
        SslConfig config = mock(SslConfig.class);

        Path keyStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path trustStore = Paths.get(getClass().getResource("/trust.jks").toURI());
        Path knownServers = Paths.get(getClass().getResource("/known-servers").toURI());
        when(config.getClientTrustMode()).thenReturn(SslTrustMode.CA);
        when(config.getClientKeyStore()).thenReturn(keyStore);
        when(config.getClientKeyStorePassword()).thenReturn("bogus");
        when(config.getClientTrustStore()).thenReturn(trustStore);
        when(config.getClientTrustStorePassword()).thenReturn("password");
        when(config.getKnownServersFile()).thenReturn(knownServers);

        ClientSSLContextFactory.create().from("localhost",config);

    }
}
