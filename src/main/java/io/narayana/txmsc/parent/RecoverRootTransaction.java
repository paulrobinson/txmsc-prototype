package io.narayana.txmsc.parent;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionExpiryScanner;

/**
 * This class is used at recovery-time to provide an operation for replaying the second phase of the protocol.
 *
 * @author paul.robinson@redhat.com 01/09/2013
 */
public class RecoverRootTransaction extends RootTransaction {

    //he Status of the transaction according to the log.
    private int status;

    // Flag to indicate that this transaction has been re-activated successfully.
    private boolean activatedResult = false;

    /**
     * Re-create an existing Root Transaction, re-loading it from the object store.
     *
     * @param uid    the Uid of the transaction being re-created
     * @param status the Status of the transaction according to the log.
     */
    public RecoverRootTransaction(Uid uid, int status) {

        super(uid);
        this.status = status;
        activatedResult = activate();
    }

    /**
     * Replays phase 2 of the commit protocol, using the status to decide whether to commit or rollback.
     */
    public void replayPhase2() {

        if (activatedResult) {
            if ((status == ActionStatus.PREPARED) ||
                    (status == ActionStatus.COMMITTING) ||
                    (status == ActionStatus.COMMITTED) ||
                    (status == ActionStatus.H_COMMIT) ||
                    (status == ActionStatus.H_MIXED) ||
                    (status == ActionStatus.H_HAZARD)) {
                super.phase2Commit(true);
            } else if ((status == ActionStatus.ABORTED) ||
                    (status == ActionStatus.H_ROLLBACK) ||
                    (status == ActionStatus.ABORTING) ||
                    (status == ActionStatus.ABORT_ONLY)) {
                super.phase2Abort(true);
            } else {
                //todo: unexpected status, take apropriate action.
            }

        } else { //Failure to activate so move the log. Unlikely to get better automatically!

            AtomicActionExpiryScanner scanner = new AtomicActionExpiryScanner();
            try {
                scanner.moveEntry(get_uid());
            } catch (final Exception ex) {
                tsLogger.i18NLogger.warn_recovery_RecoverAtomicAction_5(get_uid());
            }
        }
    }

}