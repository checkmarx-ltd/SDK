package com.cx.sdk.oidcLogin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ConfigurationDTO {

	@JsonProperty("mandatoryCommentOnChangeResultState")
    private boolean mandatoryCommentOnChangeResultState;

    @JsonProperty("mandatoryCommentOnChangeResultStateToNE")
    private boolean mandatoryCommentOnChangeResultStateToNE;

    @JsonProperty("mandatoryCommentOnChangeResultStateToPNE")
    private boolean mandatoryCommentOnChangeResultStateToPNE;

    // Getter and setter for mandatoryCommentOnChangeResultState
    public boolean isMandatoryCommentOnChangeResultState() {
        return mandatoryCommentOnChangeResultState;
    }

    public void setMandatoryCommentOnChangeResultState(boolean mandatoryCommentOnChangeResultState) {
        this.mandatoryCommentOnChangeResultState = mandatoryCommentOnChangeResultState;
    }

    // Getter and setter for mandatoryCommentOnChangeResultStateToNE
    public boolean isMandatoryCommentOnChangeResultStateToNE() {
        return mandatoryCommentOnChangeResultStateToNE;
    }

    public void setMandatoryCommentOnChangeResultStateToNE(boolean mandatoryCommentOnChangeResultStateToNE) {
        this.mandatoryCommentOnChangeResultStateToNE = mandatoryCommentOnChangeResultStateToNE;
    }

    // Getter and setter for mandatoryCommentOnChangeResultStateToPNE
    public boolean isMandatoryCommentOnChangeResultStateToPNE() {
        return mandatoryCommentOnChangeResultStateToPNE;
    }

    public void setMandatoryCommentOnChangeResultStateToPNE(boolean mandatoryCommentOnChangeResultStateToPNE) {
        this.mandatoryCommentOnChangeResultStateToPNE = mandatoryCommentOnChangeResultStateToPNE;
    }

}