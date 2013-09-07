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
     * Static handle on the recovery manager.
     */
    protected static RecoveryManager recoveryManager;

    /**
     * Configure the Recovery Manager and start it.
     *
     */
    public static void startRecovery() {

        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryBackoffPeriod(1);

        List<RecoveryModule> recoveryModules = new ArrayList<RecoveryModule>();
        recoveryModules.add(new RootTransactionRecoveryModule());
        recoveryModules.add(new SubordinateTransactionOrphanDetector());
        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryModules(recoveryModules);

        recoveryManager = RecoveryManager.manager();
    }

    /**
     * Stop the recovery manager.
     *
     */
    public static void stopRecovery() {

        recoveryManager.terminate();
    }

    /**
     * Run a recovery scan on-demand.
     *
     */
    public static void runRecoveryScan() {

        recoveryManager.scan();
    }
}