package fr.marstech.mtlinkspray;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MtLinkSprayApplicationTests {

    @Test
    void contextLoads() {
        assert true;
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        assertEquals(map.get("badkey"), map.getOrDefault("badkey", null));
    }
}
