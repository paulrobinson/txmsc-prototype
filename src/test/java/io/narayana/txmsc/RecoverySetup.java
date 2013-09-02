package io.narayana.txmsc;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.io.File;

/**
 * @author paul.robinson@redhat.com 30/08/2013
 */
public class RecoverySetup {

    protected static RecoveryManager recoveryManager;

    public static void startRecovery() {

        RecoveryManager.delayRecoveryManagerThread();
        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryBackoffPeriod(1);
        recoveryManager = RecoveryManager.manager();
    }

    public static void stopRecovery() {

        recoveryManager.terminate();
    }

    public static void runRecoveryScan() {

        recoveryManager.scan();
    }

    public static void clearLog() {

        File objectStoreDir = new File(BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir() + "/ShadowNoFileLockStore/defaultStore/StateManager/RootTransaction");
        for (File record : objectStoreDir.listFiles()) {
            boolean result = record.delete();
            if (!result) {
                throw new RuntimeException("Unable to delete file: " + record.getAbsolutePath());
            }
        }
    }
}