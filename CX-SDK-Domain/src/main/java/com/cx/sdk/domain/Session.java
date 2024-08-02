package com.cx.sdk.domain;

/**
 * Created by ehuds on 2/23/2017.
 */
public class Session {
    private String sessionId;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiration;
    private boolean isScanner;

    private boolean isAllowedToChangeNotExploitable;
    private boolean isIsAllowedToModifyResultDetails;
    
    private String cxVersion;
    private boolean mandatoryCommentOnChangeResultState = false;
    private boolean mandatoryCommentOnChangeResultStateToNE = false;
    private boolean mandatoryCommentOnChangeResultStateToPNE = false;

    public Session (){
    }

    public Session(String sessionId, String accessToken, String refreshToken, Long accessTokenExpiration,
                   boolean isScanner, boolean isAllowedToChangeNotExploitable,
                   boolean isIsAllowedToModifyResultDetails) {
        this.sessionId = sessionId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiration = accessTokenExpiration;
        this.isScanner = isScanner;
        this.isAllowedToChangeNotExploitable = isAllowedToChangeNotExploitable;
        this.isIsAllowedToModifyResultDetails = isIsAllowedToModifyResultDetails;
    }
    public String getSessionId() {
        return sessionId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean getIsScanner() { return isScanner; }
    public boolean getIsAllowedToChangeNotExploitable() { return isAllowedToChangeNotExploitable; }    
    public boolean getIsAllowedToModifyResultDetails() { return isIsAllowedToModifyResultDetails; }
    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
    
    public String getCxVersion() {
    	return cxVersion;
    }
    
    public void setCxVersion(String cxVersion) {
    	this.cxVersion = cxVersion;
    }
    
    public boolean isMandatoryCommentOnChangeResultState() {
        return mandatoryCommentOnChangeResultState;
    }
    
    public void setMandatoryCommentOnChangeResultState(boolean mandatoryCommentOnChangeResultState) {
    	this.mandatoryCommentOnChangeResultState = mandatoryCommentOnChangeResultState;
    }

    public boolean isMandatoryCommentOnChangeResultStateToNE() {
        return mandatoryCommentOnChangeResultStateToNE;
    }
    
    public void setMandatoryCommentOnChangeResultStateToNE(boolean mandatoryCommentOnChangeResultStateToNE) {
    	this.mandatoryCommentOnChangeResultStateToNE = mandatoryCommentOnChangeResultStateToNE;
    }

    public boolean isMandatoryCommentOnChangeResultStateToPNE() {
        return mandatoryCommentOnChangeResultStateToPNE;
    }
    
    public void setMandatoryCommentOnChangeResultStateToPNE(boolean mandatoryCommentOnChangeResultStateToPNE) {
    	this.mandatoryCommentOnChangeResultStateToPNE = mandatoryCommentOnChangeResultStateToPNE;
    }
}
