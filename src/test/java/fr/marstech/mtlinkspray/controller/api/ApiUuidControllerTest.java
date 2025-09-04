package fr.marstech.mtlinkspray.controller.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ApiUuidControllerTest {
  ApiUuidController apiUuidController = new ApiUuidController();

  @Test
  void testGetUuid() {
    // Basic test: two UUIDs should be different and valid
    String uuid1 = apiUuidController.getUuid();
    String uuid2 = apiUuidController.getUuid();
    Assertions.assertNotNull(uuid1);
    Assertions.assertNotNull(uuid2);
    Assertions.assertNotEquals(uuid1, uuid2);
    Assertions.assertEquals(UUID.fromString(uuid1).toString(), uuid1);
    Assertions.assertEquals(UUID.fromString(uuid2).toString(), uuid2);
  }

  @Test
  void testUuidFormat() {
    String uuid = apiUuidController.getUuid();
    Pattern uuidPattern =
        Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    Assertions.assertTrue(uuidPattern.matcher(uuid).matches(), "UUID format is incorrect");
    Assertions.assertEquals(36, uuid.length(), "UUID length should be 36");
  }

  @Test
  void testMultipleUuidsAreUnique_en() {
    int count = 100;
    Set<String> uuids =
        IntStream.range(0, count)
            .mapToObj(i -> apiUuidController.getUuid())
            .collect(Collectors.toSet());
    Assertions.assertEquals(count, uuids.size(), "All UUIDs should be unique");
  }
}
