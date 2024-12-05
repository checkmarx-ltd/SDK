package com.cx.sdk.oidcLogin.restClient.entities;

public class Configurations {

    private boolean mandatoryCommentOnChangeResultState = true;
    private boolean mandatoryCommentOnChangeResultStateToNE = true;
    private boolean mandatoryCommentOnChangeResultStateToPNE = true;

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