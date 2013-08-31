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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

/**
 * @author paul.robinson@redhat.com 07/08/2013
 */
public class SubordinateTransaction extends BasicAction {

    Uid parentTransactionUid;

    public SubordinateTransaction(Uid parentTransactionUid) {

        this.parentTransactionUid = parentTransactionUid;
    }

    public int begin() {

        return super.Begin(null);
    }

    @Override
    protected synchronized int End(boolean reportHeuristics) {

        return super.End(reportHeuristics);
    }

    public String type() {

        return "/StateManager/SubordinateTransaction";
    }

    public int prepare() {

        int status = super.status();

        // Could this have been aborted by the TransactionReaper?
        if (status == ActionStatus.ABORTED) {
            return TwoPhaseOutcome.PREPARE_NOTOK;
        }

        if (!(status == ActionStatus.ABORT_ONLY || status == ActionStatus.ABORTING)) {
            return super.prepare(true);
        } else {
            super.phase2Abort(true);
            return TwoPhaseOutcome.PREPARE_NOTOK;
        }
    }


    public int commit() {

        super.phase2Commit(true);

        int toReturn;

        switch (super.getHeuristicDecision()) {
            case TwoPhaseOutcome.PREPARE_OK:
            case TwoPhaseOutcome.FINISH_OK:
                toReturn = super.status();
                break;
            case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
                toReturn = ActionStatus.H_ROLLBACK;
                break;
            case TwoPhaseOutcome.HEURISTIC_COMMIT:
                toReturn = ActionStatus.H_COMMIT;
                break;
            case TwoPhaseOutcome.HEURISTIC_MIXED:
                toReturn = ActionStatus.H_MIXED;
                break;
            case TwoPhaseOutcome.HEURISTIC_HAZARD:
            default:
                toReturn = ActionStatus.H_HAZARD;
                break;
        }

        //todo remove from reaper here if required.

        return toReturn;
    }

    public int rollback() {

        super.phase2Abort(true);

        int toReturn;

        switch (super.getHeuristicDecision()) {
            case TwoPhaseOutcome.PREPARE_OK:
            case TwoPhaseOutcome.FINISH_OK:
                toReturn = super.status();
                break;
            case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
                toReturn = ActionStatus.H_ROLLBACK;
                break;
            case TwoPhaseOutcome.HEURISTIC_COMMIT:
                toReturn = ActionStatus.H_COMMIT;
                break;
            case TwoPhaseOutcome.HEURISTIC_MIXED:
                toReturn = ActionStatus.H_MIXED;
                break;
            case TwoPhaseOutcome.HEURISTIC_HAZARD:
            default:
                toReturn = ActionStatus.H_HAZARD;
                break;
        }

        //todo remove from reaper here if required.

        return toReturn;
    }

}
