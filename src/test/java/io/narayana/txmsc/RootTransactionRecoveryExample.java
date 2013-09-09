package io.narayana.txmsc;

import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import io.narayana.txmsc.parent.RootTransaction;

/**
 * An example showing recovery of a failed Root Transaction.
 *
 * @author paul.robinson@redhat.com 08/08/2013
 */
public class RootTransactionRecoveryExample {

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
     * Run the transaction and simulate a crash during commit.
     *
     * @throws Exception
     */
    private static void runTransaction() throws Exception {

        RootTransaction ba1 = new RootTransaction();
        ba1.begin();

        ConfigService configService1 = new ConfigService("1");
        ConfigService configService2 = new ConfigService("2", true);
        ba1.add(configService1.getParticipant());
        ba1.add(configService2.getParticipant());

        configService1.setNewValue("1", "newVal1");
        configService2.setNewValue("2", "newVal2");

        try {
            // Will fail when the ConfigService2 commits
            ba1.commit();
            System.err.println("Was able to commit, but should have failed");
        } catch (Error e) {
            System.out.println("Server simulated a crash, as expected");
        }
    }

    /**
     * Run the recovery manager and observe recovery of the transaction.
     *
     * @throws Exception
     */
    private static void recoverTransaction() throws Exception {

        ConfigParticipantRecordTypeMap map = new ConfigParticipantRecordTypeMap();
        RecordTypeManager.manager().add(map);

        RecoveryManager recoveryManager = RecoverySetup.getAndConfigureRecoveryManager();
        recoveryManager.scan();

        System.out.println("'child-config' value = " + ConfigService.getCommittedValue("child-config"));
        System.out.println("'parent-config' value = " + ConfigService.getCommittedValue("parent-config"));

        recoveryManager.terminate();
    }

}
