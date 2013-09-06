package io.narayana.txmsc.child;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * @author paul.robinson@redhat.com 05/09/2013
 */
public class SubordinateParticipant {

    private SubordinateTransaction subordinateTransaction;

    //todo, remove flag
    private SubordinateParticipant(Integer serverId, Uid subordinateUid, boolean fromDisk) {

        if (fromDisk) {
            this.subordinateTransaction = new SubordinateTransaction(subordinateUid);
        } else {
            this.subordinateTransaction = SubordinateTransactionImporter.getSubordinateTransaction(serverId, subordinateUid);
        }
    }

    public static SubordinateParticipant connect(Integer serverId, Uid subordinateUid) {
        return new SubordinateParticipant(serverId, subordinateUid, false);
    }

    public static SubordinateParticipant recoverThenConnect(Integer serverId, Uid subordinateUid) {
        return new SubordinateParticipant(serverId, subordinateUid, true);
    }

    public int prepare() {

        return subordinateTransaction.prepare();
    }

    public int commit() {

        return subordinateTransaction.commit();
    }

    public int rollback() {

        return subordinateTransaction.rollback();
    }
}
