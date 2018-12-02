package com.cx.sdk.api.dtos;

import java.util.Map;

/**
 * Created by ehuds on 2/23/2017.
 */
public class SessionDTO {
    private String sessionId;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiration;
    private boolean isScanner;
    private boolean isAllowedToChangeNotExploitable;
    private boolean isIsAllowedToModifyResultDetails;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean getIsScanner() {
        return isScanner;
    }

    public void setIsScanner(boolean scanner) {
        isScanner = scanner;
    }

    public boolean isAllowedToChangeNotExploitable() {
        return isAllowedToChangeNotExploitable;
    }

    public void setAllowedToChangeNotExploitable(boolean allowedToChangeNotExploitable) {
        isAllowedToChangeNotExploitable = allowedToChangeNotExploitable;
    }

    public boolean isIsAllowedToModifyResultDetails() {
        return isIsAllowedToModifyResultDetails;
    }

    public void setIsAllowedToModifyResultDetails(boolean isAllowedToModifyResultDetails) {
        isIsAllowedToModifyResultDetails = isAllowedToModifyResultDetails;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setAccessTokenExpiration(Long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }
}
