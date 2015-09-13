package de.tudarmstadt.informatik.tk.android.assistance.model.api.module;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Wladimir Schmidt on 30.06.2015.
 */
public class AvailableModuleResponse {

    @SerializedName("id")
    @Expose
    private String modulePackage;

    @SerializedName("name")
    @Expose
    private String title;

    @SerializedName("logoUrl")
    @Expose
    private String logo;

    @SerializedName("description_short")
    @Expose
    private String descriptionShort;

    @SerializedName("description_long")
    @Expose
    private String descriptionFull;

    @SerializedName("copyright")
    @Expose
    private String copyright;

    @SerializedName("requiredCapabilities")
    @Expose
    private List<ModuleCapabilityResponse> sensorsRequired;

    @SerializedName("optionalCapabilites")
    @Expose
    private List<ModuleCapabilityResponse> sensorsOptional;

    @SerializedName("supportEmail")
    @Expose
    private String supportEmail;

    public AvailableModuleResponse() {
    }

    public AvailableModuleResponse(String modulePackage, String title, String logo, String descriptionShort, String descriptionFull, String copyright, List<ModuleCapabilityResponse> sensorsRequired, List<ModuleCapabilityResponse> sensorsOptional, String supportEmail) {
        this.modulePackage = modulePackage;
        this.title = title;
        this.logo = logo;
        this.descriptionShort = descriptionShort;
        this.descriptionFull = descriptionFull;
        this.copyright = copyright;
        this.sensorsRequired = sensorsRequired;
        this.sensorsOptional = sensorsOptional;
        this.supportEmail = supportEmail;
    }

    public String getModulePackage() {
        return modulePackage;
    }

    public void setModulePackage(String modulePackage) {
        this.modulePackage = modulePackage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getDescriptionShort() {
        return descriptionShort;
    }

    public void setDescriptionShort(String descriptionShort) {
        this.descriptionShort = descriptionShort;
    }

    public String getDescriptionFull() {
        return descriptionFull;
    }

    public void setDescriptionFull(String descriptionFull) {
        this.descriptionFull = descriptionFull;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public List<ModuleCapabilityResponse> getSensorsRequired() {
        return sensorsRequired;
    }

    public void setSensorsRequired(List<ModuleCapabilityResponse> sensorsRequired) {
        this.sensorsRequired = sensorsRequired;
    }

    public List<ModuleCapabilityResponse> getSensorsOptional() {
        return sensorsOptional;
    }

    public void setSensorsOptional(List<ModuleCapabilityResponse> sensorsOptional) {
        this.sensorsOptional = sensorsOptional;
    }

    public String getSupportEmail() {
        return this.supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    @Override
    public String toString() {
        return "AvailableModuleResponse{" +
                "modulePackage='" + modulePackage + '\'' +
                ", title='" + title + '\'' +
                ", logo='" + logo + '\'' +
                ", descriptionShort='" + descriptionShort + '\'' +
                ", descriptionFull='" + descriptionFull + '\'' +
                ", copyright='" + copyright + '\'' +
                ", sensorsRequired=" + sensorsRequired +
                ", sensorsOptional=" + sensorsOptional +
                ", supportEmail='" + supportEmail + '\'' +
                '}';
    }
}
