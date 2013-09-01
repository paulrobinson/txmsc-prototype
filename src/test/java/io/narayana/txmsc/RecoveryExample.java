package io.narayana.txmsc;

import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;

/**
 * @author paul.robinson@redhat.com 08/08/2013
 */
public class RecoveryExample {

    public static void main(String[] args) throws Exception  {

        if (args.length == 0) {
            runTransaction();
        }
        else if (args[0].equals("--recover")) {
            recoverTransaction();
        }
        else if (args[0].equals("--both")) {
            runTransaction();
            recoverTransaction();
        }
        else {
            System.err.println("Unexpected arg: "+ args[0]);
        }
    }

    private static void runTransaction() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        DummyBasicRecord dummyBasicRecord1 = new DummyBasicRecord("1");
        DummyBasicRecord dummyBasicRecord2 = new DummyBasicRecord("2", true);
        ba1.add(dummyBasicRecord1);
        ba1.add(dummyBasicRecord2);

        dummyBasicRecord1.setNewValue("1", "newVal1");
        dummyBasicRecord2.setNewValue("2", "newVal2");

        try {
            ba1.commit();
        } catch (Error e) {
            System.out.println("Server crashed, as expected");
        }
    }

    private static void recoverTransaction() throws Exception {

        DummyBasicRecordTypeMap map = new DummyBasicRecordTypeMap();
        RecordTypeManager.manager().add(map);

        RecoverySetup.startRecovery();
        RecoverySetup.runRecoveryScan();

        Thread.sleep(5000);

        System.out.println(DummyBasicRecord.getPersistedValue("1"));
        System.out.println(DummyBasicRecord.getPersistedValue("2"));
    }

}
