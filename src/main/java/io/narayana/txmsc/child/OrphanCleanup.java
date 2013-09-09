package io.narayana.txmsc.child;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import java.util.Collection;

/**
 * This is currently un-tested, but it gives an idea as to how we think orphan detection would work.
 *
 * @author paul.robinson@redhat.com 06/09/2013
 */
public class OrphanCleanup {

    private OrphanCleanup() {

    }

    public static OrphanCleanup connect() {

        return new OrphanCleanup();
    }

    /**
     * todo: do the in-mem ones first, then peak for all the tx with the required serverId and do them
     * <p/>
     * Orphan detection is driven by the parent after it has recovered all the transactions it knows about.
     * After which, this method is invoked, to request that the child rollback all the transactions in the log initiated
     * by the requesting server, and of the type 'SubordinateTransaction'.
     *
     * @param serverId The id of the server that initiated the request.
     */
    public void doCleanup(Integer serverId) {

        doCleanupOfInMemoryTransactions(serverId);
        doCleanupOfCrashedTransactions(serverId);
    }

    private void doCleanupOfInMemoryTransactions(Integer serverId) {

        Collection<SubordinateTransaction> subordinateTransactions = SubordinateTransactionImporter.getSubordinateTransactions(serverId);
        if (subordinateTransactions == null) {
            return;
        }

        for (SubordinateTransaction subordinateTransaction : subordinateTransactions) {
            subordinateTransaction.rollback();
        }
    }

    private void doCleanupOfCrashedTransactions(Integer serverId) {

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


                        InputObjectState objectState = recoveryStore.read_committed(orphanUid, new SubordinateTransaction().type());
                        Integer unpackedServerId = objectState.unpackInt();

                        //If Transaction was initiated by the specified server, then restore and rollback the transaction.
                        if (unpackedServerId.equals(serverId)) {

                            SubordinateTransaction subordinateTransaction = new SubordinateTransaction(orphanUid);
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
