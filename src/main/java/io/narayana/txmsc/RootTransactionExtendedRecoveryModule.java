package io.narayana.txmsc;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;

/**
 * @author paul.robinson@redhat.com 01/09/2013
 */
public class RootTransactionExtendedRecoveryModule extends AtomicActionRecoveryModule {

    private String _transactionType = new RootTransaction().type() ;

    public String get_transactionType() {

        return _transactionType;
    }

    public void set_transactionType(String _transactionType) {

        this._transactionType = _transactionType;
    }

    @Override
    protected void replayPhase2(Uid recoverUid, int theStatus) {
        RecoverRootTransaction rcvRootTransaction =
           new RecoverRootTransaction( recoverUid, theStatus ) ;

        rcvRootTransaction.replayPhase2() ;
    }
}
