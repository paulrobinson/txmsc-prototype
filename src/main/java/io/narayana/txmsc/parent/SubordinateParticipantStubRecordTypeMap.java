package io.narayana.txmsc.parent;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import io.narayana.txmsc.parent.SubordinateParticipantStub;

/**
 * @author paul.robinson@redhat.com 01/09/2013
 */
public class SubordinateParticipantStubRecordTypeMap implements RecordTypeMap {

    @Override
    public Class<? extends AbstractRecord> getRecordClass() {

        return SubordinateParticipantStub.class;
    }

    @Override
    public int getType() {

        //todo: add comment...
        return RecordType.USER_DEF_FIRST0;
    }
}
