package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.dto.DashboardDto;
import fr.marstech.mtlinkspray.entity.DashboardEntity;
import fr.marstech.mtlinkspray.entity.HistoryItem;
import fr.marstech.mtlinkspray.repository.DashboardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardDto createDashboard(DashboardDto dashboardDto) {

        DashboardEntity inputEntity = new DashboardEntity(dashboardDto.getId(), LocalDateTime.now(), null, true, dashboardDto.getDescription(), Map.of(), new HistoryItem(), List.of(), dashboardDto.getName(), dashboardDto.getItems());
        DashboardEntity savedEntity = dashboardRepository.save(inputEntity);

        return new DashboardDto(savedEntity.getId(), savedEntity.getName(), savedEntity.getItems(), savedEntity.getDescription());
    }

    @Override
    public DashboardDto createDashboard(String dashboardName) {
        return createDashboard(new DashboardDto(UUID.randomUUID().toString(), dashboardName, List.of(), null));
    }

    @Override
    public DashboardDto getDashboard(String id) {
        DashboardEntity entity = dashboardRepository.findById(id).orElseThrow();
        return new DashboardDto(entity.getId(), entity.getName(), entity.getItems(), entity.getDescription());
    }

    @Override
    public DashboardDto updateDashboard(String id, DashboardDto dashboardDto) {
        return null;
    }
}
