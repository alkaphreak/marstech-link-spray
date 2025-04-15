package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.dto.DashboardDto;
import fr.marstech.mtlinkspray.entity.DashboardEntity;
import fr.marstech.mtlinkspray.repository.DashboardRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardDto createDashboard(DashboardDto dashboardDto) {

        DashboardEntity dashboardEntity = DashboardEntity.builder().name(dashboardDto.getName()).description(dashboardDto.getDescription()).items(dashboardDto.getItems()).build();

        DashboardEntity saved = dashboardRepository.save(dashboardEntity);

        return DashboardDto.builder().id(saved.getId()).name(saved.getName()).description(saved.getDescription()).items(saved.getItems()).build();
    }

    @Override
    public DashboardDto createDashboard(String dashboardName) {
        DashboardEntity dashboardEntity = DashboardEntity.builder().name(dashboardName).build();
        DashboardEntity saved = dashboardRepository.save(dashboardEntity);
        return DashboardDto.builder().id(saved.getId()).name(saved.getName()).build();
    }

    @Override
    public DashboardDto getDashboard(String id) {
        DashboardEntity dashboardEntity = dashboardRepository.findById(id).orElse(null);
        return dashboardEntity != null ? DashboardDto.builder().id(dashboardEntity.getId()).name(dashboardEntity.getName()).description(dashboardEntity.getDescription()).items(dashboardEntity.getItems()).build() : null;
    }
}
