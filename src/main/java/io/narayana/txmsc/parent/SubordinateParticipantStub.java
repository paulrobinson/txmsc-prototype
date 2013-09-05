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

package io.narayana.txmsc.parent;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import io.narayana.txmsc.child.SubordinateParticipant;
import io.narayana.txmsc.child.SubordinateTransaction;

import java.io.IOException;

/**
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class SubordinateParticipantStub extends AbstractRecord {

    private Integer serverId;
    private Uid subordinateUid;

    private SubordinateParticipant subordinateParticipant;

    public SubordinateParticipantStub() {
    }

    public SubordinateParticipantStub(Integer serverId, Uid subordinateUid) {

        this.serverId = serverId;
        this.subordinateUid = subordinateUid;
        this.subordinateParticipant = SubordinateParticipant.lookup(serverId, subordinateUid);
    }

    @Override
    public int typeIs() {

        log();

        //todo: Document
        return RecordType.USER_DEF_FIRST0;
    }

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
    public boolean doSave() {

        return true;
    }


    @Override
    public boolean save_state(OutputObjectState os, int i) {

        log();

        if (!super.save_state(os, i)) {
            return false;
        }

        try {
            os.packInt(serverId);
            UidHelper.packInto(subordinateUid, os);
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
            serverId = os.unpackInt();
            subordinateUid = UidHelper.unpackFrom(os);

            //Lookup the remote participant, ensuring it restores from the recovery log
            subordinateParticipant = SubordinateParticipant.lookupDuringRecovery(serverId, subordinateUid);
        } catch (IOException e) {
            return false;
        }
        return true;
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
        //Simulates a remote call
        return subordinateParticipant.rollback();
    }

    @Override
    public int topLevelCommit() {

        log();
        //Simulates a remote call
        return subordinateParticipant.commit();
    }

    @Override
    public int topLevelPrepare() {

        log();
        //Simulates a remote call
        return subordinateParticipant.prepare();
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

    private void log() {

        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String methodName = ste[2].getMethodName();
        System.out.println("SubordinateParticipantStub:" + methodName);
    }
}
