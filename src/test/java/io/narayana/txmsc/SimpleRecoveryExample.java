package io.narayana.txmsc;

import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import io.narayana.txmsc.parent.RootTransaction;

/**
 * @author paul.robinson@redhat.com 08/08/2013
 */
public class SimpleRecoveryExample {

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

        ConfigParticipant configParticipant1 = new ConfigParticipant("1");
        ConfigParticipant configParticipant2 = new ConfigParticipant("2", true);
        ba1.add(configParticipant1);
        ba1.add(configParticipant2);

        configParticipant1.setNewValue("1", "newVal1");
        configParticipant2.setNewValue("2", "newVal2");

        try {
            ba1.commit();
        } catch (Error e) {
            System.out.println("Server simulated a crashed, as expected");
        }
    }

    private static void recoverTransaction() throws Exception {

        ConfigParticipantRecordTypeMap map = new ConfigParticipantRecordTypeMap();
        RecordTypeManager.manager().add(map);

        RecoverySetup.startRecovery();
        RecoverySetup.runRecoveryScan();

        Thread.sleep(5000);

        System.out.println(ConfigParticipant.getPersistedValue("1"));
        System.out.println(ConfigParticipant.getPersistedValue("2"));

        RecoverySetup.stopRecovery();
    }

}
