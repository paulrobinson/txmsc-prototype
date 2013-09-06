package io.narayana.txmsc.child;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * This is the child-side participant that is driven remotely by the parent transaction. It wraps the Subordinate Transaction
 * instance that runs on the child.
 *
 * @author paul.robinson@redhat.com 05/09/2013
 */
public class SubordinateParticipant {

    private SubordinateTransaction subordinateTransaction;

    private SubordinateParticipant(Uid subordinateUid) {

        this.subordinateTransaction = new SubordinateTransaction(subordinateUid);
    }

    private SubordinateParticipant(Uid subordinateUid, Integer serverId) {

        this.subordinateTransaction = SubordinateTransactionImporter.getSubordinateTransaction(serverId, subordinateUid);
    }

    /**
     * This simulates the remote connection to this participant. This is a Factory method that calls the appropriate constructor
     * to ensure that the previously created Subordinate Transaction is used. It should be used during normal (i.e.
     * non-recovery) operation of the protocol.
     *
     * @param serverId The serverId of the parent of the transaction.
     * @param subordinateUid The uid of a previously created Subordinate transaction to use in this instance.
     * @return
     */
    public static SubordinateParticipant connect(Integer serverId, Uid subordinateUid) {

        return new SubordinateParticipant(subordinateUid, serverId);
    }

    /**
     * This simulates the remote connection to this participant. This is a Factory method that calls the appropriate constructor
     * to ensure that a previously serialised-to-log, Subordinate Transaction is used. It should be used during recovery.
     *
     * @param subordinateUid The uid of a previously created Subordinate transaction (now present in the log) to use in this instance.
     * @return
     */
    public static SubordinateParticipant recoverThenConnect(Uid subordinateUid) {

        return new SubordinateParticipant(subordinateUid);
    }

    /**
     * Delegate through to the prepare method of the SubordinateTransaction
     *
     * @return the status of the prepare operation.
     */
    public int prepare() {

        return subordinateTransaction.prepare();
    }

    /**
     * Delegate through to the commit method of the SubordinateTransaction
     *
     * @return the status of the commit operation.
     */
    public int commit() {

        return subordinateTransaction.commit();
    }

    /**
     * Delegate through to the rollback method of the SubordinateTransaction
     *
     * @return the status of the rollback operation.
     */
    public int rollback() {

        return subordinateTransaction.rollback();
    }
}
