package com.hms.hospital.dto.response;

import com.hms.hospital.entity.Undergoes;

/** An Undergoes record enriched with the procedure's name and cost, as the doctor dashboard needs. */
public record OperationView(
        String id, Long patient, Integer procedures, Integer stay, String dateUndergoes,
        Integer physician, Integer assistingNurse, String procedureName, Double cost) {

    public static OperationView from(Undergoes u, String procedureName, Double cost) {
        return new OperationView(u.getId(), u.getPatient(), u.getProcedures(), u.getStay(),
                u.getDateUndergoes(), u.getPhysician(), u.getAssistingNurse(), procedureName, cost);
    }
}
