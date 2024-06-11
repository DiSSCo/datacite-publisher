package eu.dissco.core.datacitepublisher.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.core.datacitepublisher.domain.RecoveryEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.HandleResolutionException;
import eu.dissco.core.datacitepublisher.service.RecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/datacite-recovery")
@RequiredArgsConstructor
@Slf4j
public class RecoveryController {

  private final RecoveryService recoveryService;

  @PostMapping("")
  public ResponseEntity<Void> recoverPids(@RequestBody RecoveryEvent event)
      throws HandleResolutionException, DataCiteApiException, JsonProcessingException {
    recoveryService.recoverDataciteDois(event);
    return ResponseEntity.ok(null);
  }


}
