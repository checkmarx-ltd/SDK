package com.cx.sdk.api;

import com.cx.sdk.application.contracts.providers.*;
import com.cx.sdk.application.services.LoginService;
import com.cx.sdk.application.services.LoginServiceImpl;
import com.cx.sdk.application.services.ShortDescriptionService;
import com.cx.sdk.application.services.ShortDescriptionServiceImpl;
import com.cx.sdk.infrastructure.SDKConfigurationProviderFactory;
import com.cx.sdk.infrastructure.providers.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.modelmapper.ModelMapper;

/**
 * Created by victork on 28/02/2017.
 */
public class Bootstrapper extends AbstractModule {

    private static final ModelMapper modelMapper = new ModelMapper();
    private SdkConfiguration configuration;

    public Bootstrapper(SdkConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        registerApiDependencies();
        registerApplicationDependencies();
        registerInfrastructureDependencies();
    }

    @Provides
    SDKConfigurationProvider provideSDKConfigurationProvider() {
        return new SDKConfigurationProviderFactory().create(
                configuration.getCxServerUrl(),
                configuration.getOriginName(),
                configuration.getProxyParams());
    }

    private void registerApiDependencies() {
        bind(CxClient.class).to(CxClientImpl.class);
    }

    private void registerApplicationDependencies() {
        bind(LoginService.class).to(LoginServiceImpl.class);
        bind(ShortDescriptionService.class).to(ShortDescriptionServiceImpl.class);
    }

    private void registerInfrastructureDependencies() {
        bind(LoginProvider.class).to(LoginProviderImpl.class);
        bind(ShortDescriptionProvider.class).to(ShortDescriptionProviderImpl.class);
    }
}
