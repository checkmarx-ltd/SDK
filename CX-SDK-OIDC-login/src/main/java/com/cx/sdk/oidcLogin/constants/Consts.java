package com.cx.sdk.oidcLogin.constants;

public class Consts {

    public static final String CLIENT_ID_KEY = "client_id";
    public static final String CLIENT_VALUE = "ide_client";
    public static final String SCOPE_KEY = "scope";
    public static final String CODE_KEY = "code";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String REDIRECT_URI_KEY = "redirect_uri";
    public static final String RESPONSE_TYPE_VALUE = "code";
    public static final String SCOPE_VALUE = "offline_access openid sast_api sast-permissions access_control_api";
    public static final String SCOPE_VALUE2 = "sast_api openid sast-permissions access-control-permissions access_control_api management_and_orchestration_api";
    public static final String APPLICATION_NAME = "CxRestAPI";
    public static final String SAST_PREFIX = "/" + APPLICATION_NAME + "/auth";
    public static final String AUTHORIZATION_ENDPOINT = SAST_PREFIX + "/identity/connect/authorize";
    public static final String END_SESSION_ENDPOINT = SAST_PREFIX + "/identity/connect/endsession";
    public static final String VERSION_END_POINT = "/" + APPLICATION_NAME + "/system/version";
    public static final String LOGOUT_ENDPOINT = SAST_PREFIX + "/identity/logout";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String USER_INFO_ENDPOINT = SAST_PREFIX + "/identity/connect/userinfo";
    public static final String SAVE_SAST_SCAN = "save-sast-scan";
    public static final String MANAGE_RESULTS_COMMENT = "manage-result-comment";
    public static final String MANAGE_RESULTS_EXPLOITABILITY = "set-result-state-notexploitable";
    
    public static final String RESPONSE_TYPE_KEY = "response_type";

    public static final String LOGOUT_REDIRECT = "post_logout_redirect_uri";
    public static final String TOKEN_HINT = "id_token_hint";
    public static final String LOGOUT_ID = "logoutId";
    public static final String EXTENDED_CONFIGURATIONS_ENDPOINT = "/" + APPLICATION_NAME + "/configurationsExtended";
    public static final String PORTAL= "portal";
    public static final String NONE ="None";
    
    public static final String MANDATORY_COMMENTS_ON_CHANGE_RESULT_STATE ="MandatoryCommentOnChangeResultState";
    public static final String MANDATORY_COMMENTS_ON_CHANGE_RESULT_STATE_TO_NE ="MandatoryCommentOnChangeResultStateToNE";
    public static final String MANDATORY_COMMENTS_ON_CHANGE_RESULT_STATE_TO_PNE ="MandatoryCommentOnChangeResultStateToPNE";
}