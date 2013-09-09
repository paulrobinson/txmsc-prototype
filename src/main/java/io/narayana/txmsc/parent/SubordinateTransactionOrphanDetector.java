package io.narayana.txmsc.parent;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import io.narayana.txmsc.child.OrphanCleanup;

/**
 * A Recovery Module that's sole purpose is to detect orphans and have them rolled-back.
 *
 * This Recovery Module must run after RootTransactionRecoveryModule, to ensure that recovery of non-orphans is complete
 * first.
 *
 * @author paul.robinson@redhat.com 06/09/2013
 */
public class SubordinateTransactionOrphanDetector implements RecoveryModule {

    /**
     * Does nothing.
     *
     */
    @Override
    public void periodicWorkFirstPass() {
    }

    /**
     *  Recovery of Root Transactions is now complete, so assume all un-recovered transactions for this server are orphans.
     *
     *  This method makes a remote call to the child to clean-up (rollback) all remaining subordinate transactions in the
     *  object-store and asociated with this server id.
     *
     *  todo: this should not run if the Root Transaction recovery failed due to a transient issue. Maybe the two Recovery
     *  Managers should be merged and renamed to something more generic
     */
    @Override
    public void periodicWorkSecondPass() {

        OrphanCleanup orphanCleanup = OrphanCleanup.connect();
        orphanCleanup.doCleanup(NodeConfig.SERVER_ID);
    }
}
