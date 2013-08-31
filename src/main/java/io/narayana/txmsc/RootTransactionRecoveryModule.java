package io.narayana.txmsc;

import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;

/**
 * @author paul.robinson@redhat.com 30/08/2013
 */
public class RootTransactionRecoveryModule extends AtomicActionRecoveryModule {

    public RootTransactionRecoveryModule() {

        super(new RootTransaction().type());
    }
}
