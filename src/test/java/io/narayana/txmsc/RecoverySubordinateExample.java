package io.narayana.txmsc;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import io.narayana.txmsc.transport.ProxyBasicRecord;

/**
 * @author paul.robinson@redhat.com 08/08/2013
 */
public class RecoverySubordinateExample {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            runTransaction();
        } else if (args[0].equals("--recover")) {
            recoverTransaction();
        } else if (args[0].equals("--both")) {
            runTransaction();
            recoverTransaction();
        } else {
            System.err.println("Unexpected arg: " + args[0]);
        }
    }

    private static void runTransaction() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        DummyBasicRecord dummyBasicRecord1 = new DummyBasicRecord("1");
        DummyBasicRecord dummyBasicRecord2 = new DummyBasicRecord("2");
        ba1.add(dummyBasicRecord1);
        ba1.add(dummyBasicRecord2);

        dummyBasicRecord1.setNewValue("1", "newVal1");
        dummyBasicRecord2.setNewValue("2", "newVal2");

        Integer serverId = 1;
        Uid rootTransactionUid = new Uid();
        SubordinateTransaction subordinateTransaction = SubordinateTransactionImporter.getInstance().getSubordinateTransaction(serverId, rootTransactionUid);
        subordinateTransaction.begin();
        ProxyBasicRecord proxyBasicRecord = new ProxyBasicRecord("proxy", serverId, subordinateTransaction);

        ba1.add(proxyBasicRecord);

        DummyBasicRecord dummySubRecord1 = new DummyBasicRecord("sub-1");
        DummyBasicRecord dummySubRecord2 = new DummyBasicRecord("sub-2", true);
        subordinateTransaction.add(dummySubRecord1);
        subordinateTransaction.add(dummySubRecord2);

        dummySubRecord1.setNewValue("sub-1", "sub-newVal1");
        dummySubRecord2.setNewValue("sub-2", "sub-newVal2");

        ba1.commit();

        try {
            ba1.commit();
        } catch (Error e) {
            System.out.println("Server simulated a crashed, as expected");
        }
    }

    private static void recoverTransaction() throws Exception {

        DummyBasicRecordTypeMap map = new DummyBasicRecordTypeMap();
        RecordTypeManager.manager().add(map);

        ProxyBasicRecordTypeMap proxyBasicRecordTypeMap = new ProxyBasicRecordTypeMap();
        RecordTypeManager.manager().add(proxyBasicRecordTypeMap);

        RecoverySetup.startRecovery();
        RecoverySetup.runRecoveryScan();

        Thread.sleep(5000);

        System.out.println(DummyBasicRecord.getPersistedValue("1"));
        System.out.println(DummyBasicRecord.getPersistedValue("2"));
        System.out.println(DummyBasicRecord.getPersistedValue("sub-1"));
        System.out.println(DummyBasicRecord.getPersistedValue("sub-2"));
    }

}
