package io.narayana.txmsc.child;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

/**
 * This is currently un-tested, but it gives an idea as to how we think orphan detection would work.
 *
 * @author paul.robinson@redhat.com 06/09/2013
 */
public class OrphanCleanup {

    public static void doCleanup(Integer serverId) {

        try {
            RecoveryStore recoveryStore = StoreManager.getRecoveryStore();

            //Get all Uids of transactions of this type, from the log
            InputObjectState allUids = new InputObjectState();
            boolean found = recoveryStore.allObjUids(new SubordinateTransaction().type(), allUids);

            //If there where none, return.
            if (!found) {
                //nothing to do
                return;
            }

            /*
                Iterate over all the uids and rollback the associated SubordinateTransaction if it was
                initiated by the specified server.
             */
            boolean moreUids = true;
            while (moreUids) {

                try {

                    Uid orphanUid = UidHelper.unpackFrom(allUids);
                    if (orphanUid.equals(Uid.nullUid())) {
                        moreUids = false;
                    } else {
                        //todo: should check first if it's for this serverId, before reloading the entire transaction
                        SubordinateTransaction subordinateTransaction = new SubordinateTransaction(orphanUid);

                        //check if it was initiated by the specified server and if so, rollback.
                        if (subordinateTransaction.getServerId().equals(serverId)) {
                            subordinateTransaction.rollback();
                        }
                    }

                } catch (Exception ex) {
                    moreUids = false;
                }
            }

        } catch (ObjectStoreException e) {
            throw new RuntimeException("Error querying ObjectStore", e);
        }
    }
}
