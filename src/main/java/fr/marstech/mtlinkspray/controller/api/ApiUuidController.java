package fr.marstech.mtlinkspray.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ApiUuidController {

  @GetMapping("/uuid")
  public String getUuid() {
    return UUID.randomUUID().toString();
  }
}
