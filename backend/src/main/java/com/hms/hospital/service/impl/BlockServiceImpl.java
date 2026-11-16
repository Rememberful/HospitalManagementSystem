package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.BlockCreateRequest;
import com.hms.hospital.entity.Block;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.BlockRepository;
import com.hms.hospital.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;

    @Override
    public Block create(BlockCreateRequest request) {
        String id = request.blockFloor() + "-" + request.blockCode();
        if (blockRepository.existsById(id)) {
            throw new BadRequestException("This block already exists");
        }
        return blockRepository.save(new Block(id, request.blockFloor(), request.blockCode()));
    }

    @Override
    public List<Block> findAll() {
        return blockRepository.findAll();
    }

    @Override
    public void delete(String id) {
        if (!blockRepository.existsById(id)) {
            throw new ResourceNotFoundException("Block");
        }
        blockRepository.deleteById(id);
    }
}
