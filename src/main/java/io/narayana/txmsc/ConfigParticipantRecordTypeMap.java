package io.narayana.txmsc;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;

/**
 * The mapping used by the recovery manager to associate the RecordType with the Record class.
 *
 * @author paul.robinson@redhat.com 01/09/2013
 */
public class ConfigParticipantRecordTypeMap implements RecordTypeMap {

    @Override
    public Class<? extends AbstractRecord> getRecordClass() {

        return ConfigService.class;
    }

    @Override
    public int getType() {

        //See RecordType. Essentially, I've placed this record towards the start. However, I don't think the order matters for our usage.
        //I think it is important that a different RecordType is used in the SubordinateParticipantStubRecordTypeMap, so as to avoid collisions.
        return RecordType.USER_DEF_FIRST1;
    }
}
