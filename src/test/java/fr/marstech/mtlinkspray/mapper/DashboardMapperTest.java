package fr.marstech.mtlinkspray.mapper;

import fr.marstech.mtlinkspray.dto.DashboardDto;
import fr.marstech.mtlinkspray.entity.DashboardEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DashboardMapperTest {

    @Autowired
    DashboardMapper dashboardMapper;

    @Test
    void toDto() {
        // Given
        DashboardEntity entity = DashboardEntity.builder()
                .name("Test Dashboard")
                .description("Test Description")
                .items(List.of())
                .build();

        // When
        DashboardDto dto = dashboardMapper.toDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals(entity.getName(), dto.getName());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getItems(), dto.getItems());
    }

    @Test
    void toEntity() {
        // Given
        DashboardDto dto = DashboardDto.builder()
                .name("Test Dashboard")
                .description("Test Description")
                .items(List.of())
                .build();
        // When
        DashboardEntity entity = dashboardMapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getItems(), entity.getItems());
    }
}