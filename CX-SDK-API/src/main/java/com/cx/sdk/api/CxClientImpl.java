package com.cx.sdk.api;

import com.cx.sdk.api.dtos.SessionDTO;
import com.cx.sdk.application.contracts.providers.SDKConfigurationProvider;
import com.cx.sdk.application.services.LoginService;
import com.cx.sdk.domain.Session;
import com.cx.sdk.domain.exceptions.SdkException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import java.io.File;


/**
 * Created by ehuds on 2/22/2017.
 */
public class CxClientImpl implements CxClient {
    private static final ModelMapper modelMapper = new ModelMapper();
    public static final String LOGIN_CONF_PATH = "resources/login.conf";

    private final LoginService loginService;
    private final SDKConfigurationProvider sdkConfigurationProvider;
    private static Session singletonSession;

    @Inject
    private CxClientImpl(LoginService loginService,
                         SDKConfigurationProvider sdkConfigurationProvider) {
        this.loginService = loginService;
        this.sdkConfigurationProvider = sdkConfigurationProvider;
    }

    public static CxClient createNewInstance(SdkConfiguration configuration) {
        if (configuration.getCxServerUrl() == null) {
            throw new IllegalArgumentException("Please provide the CxServerURL");
        }

        initKerberosParameters(configuration);

        singletonSession = null;
        Injector injector = Guice.createInjector(new Bootstrapper(configuration));
        return injector.getInstance(CxClient.class);
    }

    private static void initKerberosParameters(SdkConfiguration configuration) {
        if (configuration.useKerberosAuthentication()) {
            System.setProperty("java.security.auth.login.config", new File(LOGIN_CONF_PATH).getAbsolutePath());
            System.setProperty("com.sun.security.auth.module.Krb5LoginModule", "true");
        }
    }

    @Override
    public SessionDTO login() throws SdkException {
        singletonSession = loginService.login();
        return modelMapper.map(singletonSession, SessionDTO.class);
    }

    @Override
    public boolean isTokenExpired(Long expirationTime) {
        return loginService.isTokenExpired(expirationTime);
    }

    @Override
    public SessionDTO getAccessTokenFromRefreshToken(String refreshToken) {
        singletonSession = loginService.getAccessTokenFromRefreshToken(refreshToken);
        return modelMapper.map(singletonSession, SessionDTO.class);
    }

    @Override
    public boolean isCxWebServiceAvailable() {
        return loginService.isCxWebServiceAvailable();
    }

    public void logout() {
        loginService.logout();
    }

}