package de.tudarmstadt.informatik.tk.android.assistance.controller;

import android.content.Context;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.DaoProvider;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class CommonControllerImpl implements CommonController {

    protected final DaoProvider daoProvider;

    public CommonControllerImpl(Context context) {
        this.daoProvider = DaoProvider.getInstance(context);
    }

    @Override
    public List<DbModule> getAllActiveModules(Long userId) {
        return daoProvider.getModuleDao().getAllActive(userId);
    }

    @Override
    public DbUser getUserByToken(String userToken) {
        return daoProvider
                .getUserDao()
                .getByToken(userToken);
    }

    @Override
    public DbUser getUserByEmail(String userEmail) {
        return daoProvider
                .getUserDao()
                .getByEmail(userEmail);
    }
}
