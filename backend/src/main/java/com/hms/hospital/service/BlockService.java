package com.hms.hospital.service;

import com.hms.hospital.dto.request.BlockCreateRequest;
import com.hms.hospital.entity.Block;

import java.util.List;

public interface BlockService {
    Block create(BlockCreateRequest request);
    List<Block> findAll();
    void delete(String id);
}
