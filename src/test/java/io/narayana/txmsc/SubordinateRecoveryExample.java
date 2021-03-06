package io.narayana.txmsc;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import io.narayana.txmsc.child.SubordinateTransaction;
import io.narayana.txmsc.parent.NodeConfig;
import io.narayana.txmsc.parent.RootTransaction;
import io.narayana.txmsc.parent.SubordinateParticipantStub;
import io.narayana.txmsc.parent.SubordinateParticipantStubRecordTypeMap;
import io.narayana.txmsc.child.SubordinateTransactionImporter;

/**
 * An example showing recovery of a failed Root Transaction with a subordinate transaction.
 *
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

    /**
     * Run a Root transaction with a subordinate transaction. Simulate a failure during commit of the subordinate transaction.
     *
     * @throws Exception
     */
    private static void runTransaction() throws Exception {

        /*
            PARENT SIDE
         */
        RootTransaction rootTransaction = new RootTransaction();
        rootTransaction.begin();

        ConfigService parentConfigService = new ConfigService("ParentConfigService");
        rootTransaction.add(parentConfigService.getParticipant());

        //Make the config change
        parentConfigService.setNewValue("parent-config", "newParentConfigValue");


        /*
            CHILD SIDE (Propagated transaction context)
         */
        SubordinateTransaction subordinateTransaction = SubordinateTransactionImporter.createSubordinateTransaction(NodeConfig.SERVER_ID);
        subordinateTransaction.begin();

        ConfigService childsConfigService = new ConfigService("childConfigService", true);
        subordinateTransaction.add(childsConfigService.getParticipant());

        //Make the config change
        childsConfigService.setNewValue("child-config", "newChildConfigValue");

        //Get subordinate Uid and pass to parent.
        Uid subordinateUid = subordinateTransaction.get_uid();


        /*
            PARENT SIDE
         */
        SubordinateParticipantStub subordinateParticipantStub = new SubordinateParticipantStub(subordinateUid);
        rootTransaction.add(subordinateParticipantStub);

        try {
            rootTransaction.commit();
        } catch (Error e) {
            System.out.println("Server simulated a crash, as expected");
        }
    }

    /**
     * Run recovery and observer recovery of the Root Transaction and the Subordinate Transaction.
     *
     * @throws Exception
     */
    private static void recoverTransaction() throws Exception {

        /*
            PARENT SIDE
         */
        ConfigParticipantRecordTypeMap map = new ConfigParticipantRecordTypeMap();
        RecordTypeManager.manager().add(map);

        SubordinateParticipantStubRecordTypeMap subordinateParticipantStubRecordTypeMap = new SubordinateParticipantStubRecordTypeMap();
        RecordTypeManager.manager().add(subordinateParticipantStubRecordTypeMap);

        RecoveryManager recoveryManager = RecoverySetup.getAndConfigureRecoveryManager();
        recoveryManager.scan();

        //Print out the state after the transaction is recovered. As it should be committed, these values should be
        //set to those specified in the transaction.
        System.out.println("'child-config' value = " + ConfigService.getCommittedValue("child-config"));
        System.out.println("'parent-config' value = " + ConfigService.getCommittedValue("parent-config"));

        recoveryManager.terminate();
    }

}
