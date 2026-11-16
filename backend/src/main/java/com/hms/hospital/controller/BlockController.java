package com.hms.hospital.controller;

import com.hms.hospital.dto.request.BlockCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Block;
import com.hms.hospital.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/block")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody BlockCreateRequest request) {
        blockService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping
    public List<Block> list() {
        return blockService.findAll();
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable String id) {
        blockService.delete(id);
        return new MessageResponse("Deleted successfully");
    }
}
