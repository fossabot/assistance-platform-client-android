package de.tudarmstadt.informatik.tk.assistance.model.item;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.01.2016
 */
public class ModuleRunningSensorTypeItem {

    private int type;

    private String title;

    private boolean allowed;

    private int requiredByModules;

    public ModuleRunningSensorTypeItem(int type, String title, boolean allowed, int requiredByModules) {
        this.type = type;
        this.title = title;
        this.allowed = allowed;
        this.requiredByModules = requiredByModules;
    }

    public int getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isAllowed() {
        return this.allowed;
    }

    public int getRequiredByModules() {
        return this.requiredByModules;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public void setRequiredByModules(int requiredByModules) {
        this.requiredByModules = requiredByModules;
    }

    @Override
    public String toString() {
        return "ModuleRunningSensorTypeItem{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", allowed=" + allowed +
                ", requiredByModules=" + requiredByModules +
                '}';
    }
}