package io.narayana.txmsc;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;

/**
 * @author paul.robinson@redhat.com 30/08/2013
 */
public class SubordinateTransactionRecoveryModule implements RecoveryModule {

    @Override
    public void periodicWorkFirstPass() {

        System.out.println("periodicWorkFirstPass");
    }

    @Override
    public void periodicWorkSecondPass() {

        System.out.println("periodicWorkSecondPass");
    }
}
