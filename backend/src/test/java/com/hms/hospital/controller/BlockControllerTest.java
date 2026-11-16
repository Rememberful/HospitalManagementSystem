package com.hms.hospital.controller;

import com.hms.hospital.entity.Block;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.BlockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(BlockController.class)
class BlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlockService blockService;

    @Test
    void createReturns201() throws Exception {
        when(blockService.create(any())).thenReturn(new Block("1-1", 1, 1));

        mockMvc.perform(post("/api/block")
                        .contentType("application/json")
                        .content("{\"blockFloor\":1,\"blockCode\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void listReturnsAllBlocks() throws Exception {
        when(blockService.findAll()).thenReturn(List.of(new Block("1-1", 1, 1)));

        mockMvc.perform(get("/api/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1-1"));
    }

    @Test
    void deleteReturns200() throws Exception {
        mockMvc.perform(delete("/api/block/1-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted successfully"));
    }

    @Test
    void deleteMissingReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Block")).when(blockService).delete("9-9");

        mockMvc.perform(delete("/api/block/9-9"))
                .andExpect(status().isNotFound());
    }
}
