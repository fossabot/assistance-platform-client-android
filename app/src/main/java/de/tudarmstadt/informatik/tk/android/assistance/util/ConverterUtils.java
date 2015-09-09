package de.tudarmstadt.informatik.tk.android.assistance.util;

import java.util.Date;
import java.util.Locale;

import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.AvailableModuleResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ModuleCapabilityResponse;
import de.tudarmstadt.informatik.tk.android.kraken.db.Module;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleCapability;
import de.tudarmstadt.informatik.tk.android.kraken.utils.DateUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 08.09.2015
 */
public class ConverterUtils {

    private ConverterUtils() {
    }

    /**
     * Converts from db module object to available module response object
     *
     * @param dbModule
     * @return
     */
    public static final AvailableModuleResponse convertModule(Module dbModule) {

        AvailableModuleResponse availableModuleResponse = new AvailableModuleResponse();

        availableModuleResponse.setTitle(dbModule.getTitle());
        availableModuleResponse.setLogo(dbModule.getLogo_url());
        availableModuleResponse.setCopyright(dbModule.getCopyright());
        availableModuleResponse.setDescriptionShort(dbModule.getDescription_short());
        availableModuleResponse.setDescriptionFull(dbModule.getDescription_full());
        availableModuleResponse.setModulePackage(dbModule.getPackage_name());
        availableModuleResponse.setSupportEmail(dbModule.getSupport_email());

        return availableModuleResponse;
    }

    /**
     * Converts from available module response object to db module object
     *
     * @param availableModuleResponse
     * @return
     */
    public static final Module convertModule(AvailableModuleResponse availableModuleResponse) {

        Module dbModule = new Module();

        dbModule.setTitle(availableModuleResponse.getTitle() == null ? "" : availableModuleResponse.getTitle());
        dbModule.setLogo_url(availableModuleResponse.getLogo() == null ? "" : availableModuleResponse.getLogo());
        dbModule.setCopyright(availableModuleResponse.getCopyright() == null ? "" : availableModuleResponse.getCopyright());
        dbModule.setDescription_short(availableModuleResponse.getDescriptionShort() == null ? "" : availableModuleResponse.getDescriptionShort());
        dbModule.setDescription_full(availableModuleResponse.getDescriptionFull() == null ? "" : availableModuleResponse.getDescriptionFull());
        dbModule.setPackage_name(availableModuleResponse.getModulePackage() == null ? "" : availableModuleResponse.getModulePackage());
        dbModule.setSupport_email(availableModuleResponse.getSupportEmail() == null ? "" : availableModuleResponse.getSupportEmail());
        dbModule.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        return dbModule;
    }

    /**
     * Converts from db module capability object to module capability response object
     *
     * @param moduleCapability
     * @return
     */
    public static final ModuleCapabilityResponse convertModuleCapability(ModuleCapability moduleCapability) {

        ModuleCapabilityResponse moduleCapabilityResponse = new ModuleCapabilityResponse();

        moduleCapabilityResponse.setType(moduleCapability.getType());
        moduleCapabilityResponse.setFrequency(moduleCapability.getFrequency());

        return moduleCapabilityResponse;
    }

    /**
     * Converts from module capability response object to db module capability object
     *
     * @param moduleCapabilityResponse
     * @return
     */
    public static final ModuleCapability convertModuleCapability(ModuleCapabilityResponse moduleCapabilityResponse) {

        ModuleCapability moduleCapability = new ModuleCapability();

        moduleCapability.setType(moduleCapabilityResponse.getType());
        moduleCapability.setFrequency(moduleCapabilityResponse.getFrequency());
        moduleCapability.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        return moduleCapability;
    }
}
