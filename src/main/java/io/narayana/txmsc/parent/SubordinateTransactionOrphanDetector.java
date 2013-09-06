package io.narayana.txmsc.parent;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import io.narayana.txmsc.child.OrphanCleanup;

/**
 * @author paul.robinson@redhat.com 06/09/2013
 */
public class SubordinateTransactionOrphanDetector implements RecoveryModule {

    @Override
    public void periodicWorkFirstPass() {
        //do nothing
    }

    @Override
    public void periodicWorkSecondPass() {

        //Recovery is now complete, so assume all un-recovered transactions for this server are orphans.
        OrphanCleanup.doCleanup(NodeConfig.SERVER_ID);
    }
}
