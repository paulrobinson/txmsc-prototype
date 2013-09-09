package io.narayana.txmsc.parent;

import com.arjuna.ats.arjuna.common.Uid;
import io.narayana.txmsc.ext.BasicActionRecoveryModule;

/**
 * This Recovery Module is responsible for driving recovery of Root Transactions and the enlisted Subordinate Transactions.
 *
 * It provides the same functionality as BasicActionRecoveryModule, but modified to use the required Transaction Type and
 * to invoke the appropriate code for replaying phase two.
 *
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
}
