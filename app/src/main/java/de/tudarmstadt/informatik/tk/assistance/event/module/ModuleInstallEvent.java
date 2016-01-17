package de.tudarmstadt.informatik.tk.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 19.07.2015
 */
public class ModuleInstallEvent {

    private String modulePackageName;

    public ModuleInstallEvent(String modulePackageName) {
        this.modulePackageName = modulePackageName;
    }

    public String getModulePackageName() {
        return modulePackageName;
    }
}