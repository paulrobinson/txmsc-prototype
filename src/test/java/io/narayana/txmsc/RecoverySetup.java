package io.narayana.txmsc;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import io.narayana.txmsc.parent.RootTransactionRecoveryModule;
import io.narayana.txmsc.parent.SubordinateTransactionOrphanDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Sets up the recovery manager
 *
 * @author paul.robinson@redhat.com 30/08/2013
 */
public class RecoverySetup {

    /**
     * Configure the Recovery Manager, start it then return it.
     *
     */
    public static RecoveryManager getAndConfigureRecoveryManager() {

        List<RecoveryModule> recoveryModules = new ArrayList<RecoveryModule>();
        recoveryModules.add(new RootTransactionRecoveryModule());
        recoveryModules.add(new SubordinateTransactionOrphanDetector());
        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryModules(recoveryModules);

        return RecoveryManager.manager();
    }

}