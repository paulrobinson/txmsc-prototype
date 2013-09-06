package io.narayana.txmsc.parent;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.recovery.BasicActionRecoveryModule;

/**
 * @author paul.robinson@redhat.com 01/09/2013
 */
public class RootTransactionRecoveryModule extends BasicActionRecoveryModule {

    public String getTransactionType() {

        return new RootTransaction().type();
    }

    @Override
    protected void replayPhase2(Uid recoverUid, int theStatus) {

        RecoverRootTransaction rcvRootTransaction = new RecoverRootTransaction(recoverUid, theStatus);
        rcvRootTransaction.replayPhase2();
    }

    //do orphan detection of subordinates here
}
