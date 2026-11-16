package com.hms.hospital.service;

import com.hms.hospital.dto.response.TrainedInWithProcedureView;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.entity.TrainedIn;

import java.util.List;

public interface TrainedInService {
    TrainedIn create(Integer physician, Integer treatment, String certificationDate, String certificationExpires);
    List<TrainedIn> findAll();
    List<TrainedInWithProcedureView> treatmentsForPhysician(Integer physicianId);
    List<Physician> physiciansForProcedure(Integer procedureId);
}
