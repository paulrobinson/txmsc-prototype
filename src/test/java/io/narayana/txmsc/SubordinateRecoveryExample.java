package io.narayana.txmsc;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import io.narayana.txmsc.child.SubordinateTransaction;
import io.narayana.txmsc.parent.RootTransaction;
import io.narayana.txmsc.parent.SubordinateParticipantStub;
import io.narayana.txmsc.parent.SubordinateParticipantStubRecordTypeMap;
import io.narayana.txmsc.child.SubordinateTransactionImporter;

/**
 * @author paul.robinson@redhat.com 08/08/2013
 */
public class SubordinateRecoveryExample {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            runTransaction();
        } else if (args[0].equals("--recover")) {
            recoverTransaction();
        } else {
            System.err.println("Unexpected arg: " + args[0]);
        }
    }

    private static void runTransaction() throws Exception {

        /*
            PARENT SIDE
         */
        Integer parentServerId = 1;

        RootTransaction rootTransaction = new RootTransaction();
        rootTransaction.begin();

        ConfigParticipant rootsConfigService = new ConfigParticipant("ParentConfigService");
        rootTransaction.add(rootsConfigService);

        //Make the config change
        rootsConfigService.setNewValue("parent-config", "newParentConfigValue");


        /*
            CHILD SIDE
         */
        SubordinateTransaction subordinateTransaction = SubordinateTransactionImporter.getSubordinateTransaction(parentServerId, null);
        subordinateTransaction.begin();
        //Get subordinate Uid and pass to parent.
        Uid subordinateUid = subordinateTransaction.get_uid();

        /*
            PARENT SIDE
         */
        SubordinateParticipantStub subordinateParticipantStub = new SubordinateParticipantStub(parentServerId, subordinateUid);
        rootTransaction.add(subordinateParticipantStub);


        /*
            CHILD SIDE
         */
        ConfigParticipant childsConfigService = new ConfigParticipant("childConfigService", true);
        subordinateTransaction.add(childsConfigService);

        //Make the config change
        childsConfigService.setNewValue("child-config", "newChildConfigValue");


        /*
            PARENT SIDE
         */
        try {
            rootTransaction.commit();
        } catch (Error e) {
            System.out.println("Server simulated a crashed, as expected");
        }
    }

    private static void recoverTransaction() throws Exception {

        /*
            PARENT SIDE
         */
        ConfigParticipantRecordTypeMap map = new ConfigParticipantRecordTypeMap();
        RecordTypeManager.manager().add(map);

        SubordinateParticipantStubRecordTypeMap subordinateParticipantStubRecordTypeMap = new SubordinateParticipantStubRecordTypeMap();
        RecordTypeManager.manager().add(subordinateParticipantStubRecordTypeMap);

        RecoverySetup.startRecovery();
        RecoverySetup.runRecoveryScan();

        Thread.sleep(5000);

        System.out.println(ConfigParticipant.getPersistedValue("child-config"));
        System.out.println(ConfigParticipant.getPersistedValue("parent-config"));

        RecoverySetup.stopRecovery();
    }

}
