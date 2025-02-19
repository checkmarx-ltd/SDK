package com.cx.sdk.api;

import com.cx.sdk.application.contracts.providers.*;
import com.cx.sdk.application.services.LoginService;
import com.cx.sdk.application.services.LoginServiceImpl;
<<<<<<< Updated upstream
import com.cx.sdk.domain.CredentialsInputValidator;
import com.cx.sdk.domain.enums.LoginType;
import com.cx.sdk.domain.validators.CredentialsInputValidatorImpl;
=======
import com.cx.sdk.application.services.ShortDescriptionService;
import com.cx.sdk.application.services.ShortDescriptionServiceImpl;
>>>>>>> Stashed changes
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
        registerDomainDependencies();
        registerInfrastructureDependencies();
    }

    @Provides
    SDKConfigurationProvider provideSDKConfigurationProvider() {
        return new SDKConfigurationProviderFactory().create(
                configuration.getCxServerUrl(),
                configuration.getOriginName(),
                modelMapper.map(configuration.getLoginType(), LoginType.class),
                configuration.getUsername(),
                configuration.getPassword(),
                configuration.useKerberosAuthentication());
    }

    private void registerApiDependencies() {
        bind(CxClient.class).to(CxClientImpl.class);
    }

    private void registerApplicationDependencies() {
        bind(LoginService.class).to(LoginServiceImpl.class);
        bind(ShortDescriptionService.class).to(ShortDescriptionServiceImpl.class);
    }

    private void registerDomainDependencies() {
        bind(CredentialsInputValidator.class).to(CredentialsInputValidatorImpl.class);
    }

    private void registerInfrastructureDependencies() {
        bind(LoginProvider.class).to(LoginProviderImpl.class);
<<<<<<< Updated upstream
        bind(ConfigurationProvider.class).to(ConfigurationProviderImpl.class);
        bind(TeamProvider.class).to(TeamProviderImpl.class);
        bind(PresetProvider.class).to(PresetProviderImpl.class);
        bind(ProjectProvider.class).to(ProjectProviderImpl.class);
=======
        bind(ShortDescriptionProvider.class).to(ShortDescriptionProviderImpl.class);
>>>>>>> Stashed changes
    }
}
