package de.tudarmstadt.informatik.tk.android.assistance.event;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 19.07.2015
 */
public class ModuleShowMoreInfoEvent {

    private String moduleId;

    public ModuleShowMoreInfoEvent(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

}
