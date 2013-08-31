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

package io.narayana.txmsc.transport;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import io.narayana.txmsc.SubordinateTransaction;

/**
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class ProxyBasicRecord extends AbstractRecord {

    private String name;
    private SubordinateTransaction subordinateTransaction;


    public ProxyBasicRecord(String name, SubordinateTransaction subordinateTransaction) {

        this.name = name;
        this.subordinateTransaction = subordinateTransaction;

    }

    @Override
    public int typeIs() {

        log();

        //todo: what should this be?
        return RecordType.USER_DEF_FIRST9;
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
        return subordinateTransaction.rollback();
    }

    @Override
    public int topLevelCommit() {

        log();

        return subordinateTransaction.commit();
    }

    @Override
    public int topLevelPrepare() {

        log();

        return subordinateTransaction.prepare();
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

    private void log() {

        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String methodName = ste[2].getMethodName();
        System.out.println("DummyBasicRecord:" + name + ":" + methodName);
    }
}
