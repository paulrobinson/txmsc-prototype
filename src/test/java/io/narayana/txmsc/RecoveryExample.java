package io.narayana.txmsc;

import com.arjuna.ats.arjuna.common.Uid;
import io.narayana.txmsc.transport.ProxyBasicRecord;

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

        ba1.commit();
    }

    private static void recoverTransaction() throws Exception {
        RecoverySetup.startRecovery();
        RecoverySetup.runRecoveryScan();

        Thread.sleep(10000);
    }

}
