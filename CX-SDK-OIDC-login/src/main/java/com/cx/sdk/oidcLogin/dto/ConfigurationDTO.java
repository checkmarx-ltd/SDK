package com.cx.sdk.oidcLogin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ConfigurationDTO {

	@JsonProperty("mandatoryCommentOnChangeResultState")
    private String mandatoryCommentOnChangeResultState;

    @JsonProperty("mandatoryCommentOnChangeResultStateToNE")
    private String mandatoryCommentOnChangeResultStateToNE;

    @JsonProperty("mandatoryCommentOnChangeResultStateToPNE")
    private String mandatoryCommentOnChangeResultStateToPNE;

    // Getter and setter for mandatoryCommentOnChangeResultState
    public String getMandatoryCommentOnChangeResultState() {
        return mandatoryCommentOnChangeResultState;
    }

    public void setMandatoryCommentOnChangeResultState(String mandatoryCommentOnChangeResultState) {
        this.mandatoryCommentOnChangeResultState = mandatoryCommentOnChangeResultState;
    }

    // Getter and setter for mandatoryCommentOnChangeResultStateToNE
    public String getMandatoryCommentOnChangeResultStateToNE() {
        return mandatoryCommentOnChangeResultStateToNE;
    }

    public void setMandatoryCommentOnChangeResultStateToNE(String mandatoryCommentOnChangeResultStateToNE) {
        this.mandatoryCommentOnChangeResultStateToNE = mandatoryCommentOnChangeResultStateToNE;
    }

    // Getter and setter for mandatoryCommentOnChangeResultStateToPNE
    public String getMandatoryCommentOnChangeResultStateToPNE() {
        return mandatoryCommentOnChangeResultStateToPNE;
    }

    public void setMandatoryCommentOnChangeResultStateToPNE(String mandatoryCommentOnChangeResultStateToPNE) {
        this.mandatoryCommentOnChangeResultStateToPNE = mandatoryCommentOnChangeResultStateToPNE;
    }

}