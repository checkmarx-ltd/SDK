package com.cx.sdk.api;

import com.cx.sdk.api.dtos.EngineConfigurationDTO;
import com.cx.sdk.api.dtos.PresetDTO;
import com.cx.sdk.api.dtos.SessionDTO;
import com.cx.sdk.api.dtos.TeamDTO;
import com.cx.sdk.application.contracts.exceptions.NotAuthorizedException;
import com.cx.sdk.application.contracts.providers.*;
import com.cx.sdk.application.services.LoginService;
import com.cx.sdk.application.services.ShortDescriptionService;
import com.cx.sdk.domain.Session;
import com.cx.sdk.domain.entities.EngineConfiguration;
import com.cx.sdk.domain.entities.Preset;
import com.cx.sdk.domain.entities.Team;
import com.cx.sdk.domain.enums.LoginType;
import com.cx.sdk.domain.exceptions.SdkException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.modelmapper.ModelMapper;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ehuds on 2/22/2017.
 */
public class CxClientImpl implements CxClient {
    private static final ModelMapper modelMapper = new ModelMapper();
    public static final String LOGIN_CONF_PATH = "resources/login.conf";

    private final LoginService loginService;
    private final SDKConfigurationProvider sdkConfigurationProvider;
    private final ConfigurationProvider configurationProvider;
    private final PresetProvider presetProvider;
    private final TeamProvider teamProvider;
    private final ProjectProvider projectProvider;

    private static Session singletonSession;
    private final ShortDescriptionService shortDescriptionService;

    @Inject
    private CxClientImpl(LoginService loginService,
<<<<<<< Updated upstream
                         SDKConfigurationProvider sdkConfigurationProvider,
                         ConfigurationProvider configurationProvider,
                         PresetProvider presetProvider,
                         TeamProvider teamProvider,
                         ProjectProvider projectProvider) {
        this.loginService = loginService;
        this.sdkConfigurationProvider = sdkConfigurationProvider;
        this.configurationProvider = configurationProvider;
        this.presetProvider = presetProvider;
        this.teamProvider = teamProvider;
        this.projectProvider = projectProvider;
=======
                         SDKConfigurationProvider sdkConfigurationProvider,ShortDescriptionService shortDescriptionServic) {
        this.loginService = loginService;
        this.sdkConfigurationProvider = sdkConfigurationProvider;
        this.shortDescriptionService = shortDescriptionServic;
>>>>>>> Stashed changes
    }

    public static CxClient createNewInstance(SdkConfiguration configuration) {
        if (configuration.getLoginType() == null)
            throw new IllegalArgumentException("Please provide a LoginType");

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
        LoginType loginType = sdkConfigurationProvider.getLoginType();

        switch (loginType) {
            case CREDENTIALS:
                singletonSession = loginService.login();
                break;
            case SAML:
                singletonSession = loginService.samlLogin();
                break;
            case SSO:
                singletonSession = loginService.ssoLogin();
                break;
            default:
                String errorMessage = String.format("login does not support the following login type: '%s'", loginType);
                throw new SdkException(errorMessage);
        }

        return modelMapper.map(singletonSession, SessionDTO.class);
    }

    @Override
    public List<EngineConfigurationDTO> getEngineConfigurations() throws CxClientException {

        try {
            if (singletonSession == null) {
                login();
            }
            return getEngineConfigurationDTOs();

        }
        catch(NotAuthorizedException sessionExpiredException) {
            login();
            return getEngineConfigurationDTOs();

        }
    }

    private List<EngineConfigurationDTO> getEngineConfigurationDTOs() throws CxClientException {
        try {
            List<EngineConfiguration> engineConfigurations = configurationProvider.getEngineConfigurations(singletonSession);
            List<EngineConfigurationDTO> dtos = new ArrayList(engineConfigurations.size());

            for (EngineConfiguration configuration: engineConfigurations ) {
                dtos.add(new EngineConfigurationDTO(configuration.getId(), configuration.getName()));
            }
            return dtos;
        } catch (SdkException sdk) {
            throw new CxClientException(sdk.getMessage());
        }
    }

    @Override
    public List<PresetDTO> getPresets() throws CxClientException {

        try {
            if (singletonSession == null) {
                login();
            }
            return getPresetDTOs();
        }
        catch(NotAuthorizedException sessionExpiredException) {
            login();
            return getPresetDTOs();
        }
    }

    private List<PresetDTO> getPresetDTOs() throws CxClientException {
        try {
            List<Preset> presets = presetProvider.getPresets(singletonSession);
            List<PresetDTO> dtos = new ArrayList(presets.size());

            for (Preset preset: presets ) {
                dtos.add(new PresetDTO(preset.getId(), preset.getName()));
            }

            return dtos;
        } catch (SdkException sdk) {
            throw new CxClientException(sdk.getMessage());
        }
    }


    @Override
    public List<TeamDTO> getTeams() throws CxClientException {

        try {
            if (singletonSession == null) {
                login();
            }
            return getTeamDTOs();
        }
        catch(NotAuthorizedException sessionExpiredException) {
            login();
            return getTeamDTOs();
        }
    }

    private List<TeamDTO> getTeamDTOs() throws CxClientException {
        try {

            List<Team> teams = teamProvider.getTeams(singletonSession);
            List<TeamDTO> dtos = new ArrayList(teams.size());

            for (Team team: teams ) {
                dtos.add(new TeamDTO(team.getId(), team.getName()));
            }

            return dtos;

        }
        catch(SdkException sdk) {
            throw new CxClientException(sdk.getMessage());
        }
    }

    @Override
    public Boolean validateProjectName(String projectName, String teamId) throws CxClientException {

        try {
            if (singletonSession == null) {
                login();
            }
            return isValid(projectName, teamId);
        }
        catch(NotAuthorizedException sessionExpiredException) {
            login();
            return isValid(projectName, teamId);
        }
    }

    private Boolean isValid(String projectName, String teamId) throws CxClientException {
        try {
            Boolean isValid = projectProvider.isValidProjectName(singletonSession, projectName, teamId);
            return isValid;
        }
        catch(SdkException sdk) {
            throw new CxClientException(sdk.getMessage());
        }
    }
    
    @Override
    public String fetchShortDescription(String accessToken , long scanId, long pathId  ) {
    	String shortDescription = "Click on the vulnerable file to view.";
    	shortDescription = shortDescriptionService.fetchShortDescription(accessToken, scanId, pathId);
    	return shortDescription;
    }


//    public class AuthorizedActionInvoker<T> {
//        public T invoke(Supplier<T> function) throws SdkException {
//            try {
//                if (singletonSession == null) {
//                    login();
//                }
//                return function.get();
//            }
//            catch(NotAuthorizedException sessionExpiredException) {
//                return loginAndHandleFunction(function);
//            }
//        }
//
//        private T loginAndHandleFunction(Supplier<T> function) throws SdkException
//        {
//            login();
//            return function.get();
//        }
//    }
}
