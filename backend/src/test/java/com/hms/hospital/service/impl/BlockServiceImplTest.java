package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.BlockCreateRequest;
import com.hms.hospital.entity.Block;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.BlockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {

    @Mock
    private BlockRepository blockRepository;

    @InjectMocks
    private BlockServiceImpl blockService;

    @Test
    @DisplayName("create builds the composite id from BlockFloor and BlockCode")
    void createsBlock() {
        when(blockRepository.existsById("1-1")).thenReturn(false);
        when(blockRepository.save(any(Block.class))).thenAnswer(inv -> inv.getArgument(0));

        Block result = blockService.create(new BlockCreateRequest(1, 1));

        assertThat(result.getId()).isEqualTo("1-1");
    }

    @Test
    @DisplayName("create rejects a duplicate block")
    void rejectsDuplicateBlock() {
        when(blockRepository.existsById("1-1")).thenReturn(true);

        assertThatThrownBy(() -> blockService.create(new BlockCreateRequest(1, 1)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("delete removes the block when it exists")
    void deletesBlock() {
        when(blockRepository.existsById("1-1")).thenReturn(true);

        blockService.delete("1-1");

        verify(blockRepository).deleteById("1-1");
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException instead of silently no-oping")
    void deleteMissingBlock() {
        when(blockRepository.existsById("9-9")).thenReturn(false);

        assertThatThrownBy(() -> blockService.delete("9-9"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(blockRepository, never()).deleteById(anyString());
    }
}
