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
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class ConfigParticipant extends AbstractRecord {

    private String name;

    private boolean failCommit = false;

    private static Map<String, String> persistedValues = new HashMap<String, String>();

    private String key;
    private String newValue;

    public ConfigParticipant() {

        this.name = "recovery";
    }

    public ConfigParticipant(String name) {

        this.name = name;
    }

    public ConfigParticipant(String name, boolean failCommit) {

        this.name = name;
        this.failCommit = failCommit;
    }

    public void setNewValue(String key, String newValue) {

        this.key = key;
        this.newValue = newValue;
    }

    public static String getPersistedValue(String key) {

        return persistedValues.get(key);
    }

    public static void reset() {

        persistedValues = new HashMap<String, String>();
    }


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

    private void fail() {

        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String methodName = ste[2].getMethodName();
        System.out.println("ConfigParticipant:" + name + ":CRASH:" + methodName);
        throw new Error("Intentional Error");
    }


    public boolean doSave() {

        return true;
    }

    /*
     * Nothing too interesting below
     */


    @Override
    public Object value() {

        log();
        // return a HeuristicInformation if we had one.
        return null;
    }

    @Override
    public void setValue(Object o) {

        log();
        //todo: is this linked to the "value" method?
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

        log();

    }

    @Override
    public void alter(AbstractRecord a) {

        log();

    }

    @Override
    public boolean shouldAdd(AbstractRecord a) {

        log();

        return true;
    }

    @Override
    public boolean shouldAlter(AbstractRecord a) {

        log();

        return false;
    }

    @Override
    public boolean shouldMerge(AbstractRecord a) {

        log();

        return false;
    }

    @Override
    public boolean shouldReplace(AbstractRecord a) {

        log();

        return false;
    }

}
