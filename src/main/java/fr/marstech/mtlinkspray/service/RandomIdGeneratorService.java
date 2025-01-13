package fr.marstech.mtlinkspray.service;

import org.jetbrains.annotations.NotNull;

public interface RandomIdGeneratorService {

    String getGeneratedFreeId();

    @NotNull String getGeneratedFreeIdwithoutCache();

    String getGeneratedFreeIdWithCache();

    String generateRandomId();
}
