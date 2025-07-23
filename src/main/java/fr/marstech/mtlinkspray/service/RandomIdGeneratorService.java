package fr.marstech.mtlinkspray.service;

import org.jetbrains.annotations.NotNull;

public interface RandomIdGeneratorService {

    String getGeneratedFreeId();

    @NotNull String getGeneratedFreeIdWithoutCache();

    String getGeneratedFreeIdWithCache();

    String generateRandomId();
}
