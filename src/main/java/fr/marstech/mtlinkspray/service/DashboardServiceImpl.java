package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.dto.DashboardDto;
import fr.marstech.mtlinkspray.mapper.DashboardMapper;
import fr.marstech.mtlinkspray.repository.DashboardRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    final DashboardMapper dashboardMapper;

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository, DashboardMapper dashboardMapper) {
        this.dashboardRepository = dashboardRepository;
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public DashboardDto createDashboard(DashboardDto dashboardDto) {
        return dashboardMapper.toDto(dashboardRepository.save(dashboardMapper.toEntity(dashboardDto)));
    }

    @Override
    public DashboardDto createDashboard(String dashboardName) {
        return createDashboard(DashboardDto.builder().name(dashboardName).build());
    }

    @Override
    public DashboardDto getDashboard(String id) {
        return dashboardMapper.toDto(dashboardRepository.findById(id).orElseThrow());
    }

    @Override
    public DashboardDto updateDashboard(String id, DashboardDto dashboardDto) {
        return null;
    }
}
