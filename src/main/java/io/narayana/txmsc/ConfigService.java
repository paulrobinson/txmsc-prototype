/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana.txmsc;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple service representing the server's configuration service. It allows a configuration value to be changed
 * within the scope of a transaction. This functionality is simulated and the implementation should not be considered
 * as the recommended way to implement a transactional resource.
 *
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class ConfigService extends AbstractRecord {

    /**
     * A name to identify this instance of the service in the logging.
     */
    private String name;

    /**
     * Whether to throw an java.lang.Error in the commit method.
     */
    private boolean failCommit = false;

    /**
     * The state managed by the resource. Due to the simplistic approach taken, this state is shared between all instances of the resource.
     */
    private static Map<String, String> persistedValues = new HashMap<String, String>();

    /**
     * The shadow state of the resource.
     */
    private String key;
    private String newValue;

    /**
     * no-args constructor, used during recovery time.
     */
    public ConfigService() {

        this.name = "recovery";
    }

    /**
     * Create a named instance of the service.
     *
     * @param name the name to identify this instance during logging.
     */
    public ConfigService(String name) {

        this.name = name;
    }

    /**
     * Create a named instance of the service and specify if it should fail during commit.
     *
     * @param name the name to identify this instance during logging.
     * @param failCommit if true, will throw a java.lang.error during commit. This simulates a failure and is useful for testing recovery.
     */
    public ConfigService(String name, boolean failCommit) {

        this.name = name;
        this.failCommit = failCommit;
    }

    /**
     * Business method, to propose a new configuration value. It is not 'persisted' until the transaction commits.
     * @param key The key to update
     * @param newValue the value to update the key to.
     */
    public void setNewValue(String key, String newValue) {

        this.key = key;
        this.newValue = newValue;
    }

    /**
     * Get the committed value for the specified key.
     *
     * @param key the key to get the value for.
     * @return The committed value associated with the key.
     */
    public static String getCommittedValue(String key) {

        return persistedValues.get(key);
    }

    /**
     * Remove all state. This happens immediately and is intended to be used outside of a transaction.
     * This method is useful for resetting the state between tests.
     */
    public static void reset() {

        persistedValues = new HashMap<String, String>();
    }

    /**
     * Serialise the state of the record, ready for it being written to the object-store.
     *
     * @param os the OutputObjectStream to write the state to.
     * @param i
     * @return boolean representing success/failure.
     */
    @Override
    public boolean save_state(OutputObjectState os, int i) {

        log();

        if (!super.save_state(os, i)) {
            return false;
        }

        try {
            os.packString(key);
            os.packString(newValue);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Called when restoring the state of the record from the object-store during recovery.
     *
     * @param os the InputObjectState to read the state from.
     * @param i
     * @return boolean representing success/failure.
     */
    @Override
    public boolean restore_state(InputObjectState os, int i) {

        log();

        if (!super.restore_state(os, i)) {
            return false;
        }

        try {
            key = os.unpackString();
            newValue = os.unpackString();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public int topLevelCommit() {

        if (failCommit) {
            fail();
        }

        log("newValue:" + key + "=" + newValue);
        persistedValues.put(key, newValue);

        return TwoPhaseOutcome.FINISH_OK;
    }


    @Override
    public int typeIs() {

        log();

        return RecordType.USER_DEF_FIRST1;
    }


    private void log(String... additionalMsg) {

        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String methodName = ste[2].getMethodName();

        String message = "ConfigParticipant:" + name + ":" + methodName;

        if (additionalMsg.length > 0) {
            message += ":" + additionalMsg[0];
        }

        System.out.println(message);
    }

    /**
     * Simulates a failure.
     */
    private void fail() {

        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String methodName = ste[2].getMethodName();
        System.out.println("ConfigParticipant:" + name + ":CRASH:" + methodName);
        throw new Error("Intentional Error");
    }

    /**
     * Enables logging for this record.
     * @return
     */
    public boolean doSave() {

        return true;
    }

    /**
     * Simulates the returning of a participant to enlist in the transaction. Just returns 'this' as the ConfigService
     * class also implements the participant logic.
     * @return
     */
    public AbstractRecord getParticipant() {
        return this;
    }

    /*
     * Nothing too interesting below
     */


    @Override
    public Object value() {
        return null;
    }

    @Override
    public void setValue(Object o) {

    }

    @Override
    public int nestedAbort() {

        log();
        return TwoPhaseOutcome.FINISH_OK;
    }

    @Override
    public int nestedCommit() {

        log();
        return TwoPhaseOutcome.FINISH_OK;
    }

    @Override
    public int nestedPrepare() {

        log();
        return TwoPhaseOutcome.FINISH_OK;
    }

    @Override
    public int topLevelAbort() {

        log();
        return TwoPhaseOutcome.FINISH_OK;
    }

    @Override
    public int topLevelPrepare() {

        log();
        return TwoPhaseOutcome.PREPARE_OK;
    }

    @Override
    public void merge(AbstractRecord a) {
    }

    @Override
    public void alter(AbstractRecord a) {
    }

    @Override
    public boolean shouldAdd(AbstractRecord a) {
        return true;
    }

    @Override
    public boolean shouldAlter(AbstractRecord a) {
        return false;
    }

    @Override
    public boolean shouldMerge(AbstractRecord a) {
        return false;
    }

    @Override
    public boolean shouldReplace(AbstractRecord a) {
        return false;
    }

}
