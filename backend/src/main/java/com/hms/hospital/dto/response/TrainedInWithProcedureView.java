package com.hms.hospital.dto.response;

import com.hms.hospital.entity.MedicalProcedure;
import com.hms.hospital.entity.TrainedIn;

/** A TrainedIn certification enriched with the full procedure it certifies the physician for. */
public record TrainedInWithProcedureView(
        String id, Integer physician, Integer treatment,
        String certificationDate, String certificationExpires, MedicalProcedure procedure) {

    public static TrainedInWithProcedureView from(TrainedIn t, MedicalProcedure procedure) {
        return new TrainedInWithProcedureView(t.getId(), t.getPhysician(), t.getTreatment(),
                t.getCertificationDate(), t.getCertificationExpires(), procedure);
    }
}
