package io.narayana.txmsc.parent;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import io.narayana.txmsc.parent.SubordinateParticipantStub;

/**
 * The mapping used by the recovery manager to associate the RecordType with the Record class.
 *
 * @author paul.robinson@redhat.com 01/09/2013
 */
public class SubordinateParticipantStubRecordTypeMap implements RecordTypeMap {

    @Override
    public Class<? extends AbstractRecord> getRecordClass() {

        return SubordinateParticipantStub.class;
    }

    @Override
    public int getType() {

        //See RecordType. Essentially, I've placed this record at the start. However, I don;t think the order matters for our usage.
        return RecordType.USER_DEF_FIRST0;
    }
}
