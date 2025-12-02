package eu.dissco.core.datacitepublisher.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.dissco.core.datacitepublisher.Profiles;
import eu.dissco.core.datacitepublisher.domain.RecoveryEvent;
import eu.dissco.core.datacitepublisher.exceptions.DataCiteApiException;
import eu.dissco.core.datacitepublisher.exceptions.DoiResolutionException;
import eu.dissco.core.datacitepublisher.exceptions.InvalidRequestException;
import eu.dissco.core.datacitepublisher.service.RecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/datacite-recovery")
@RequiredArgsConstructor
@Slf4j
@Profile(Profiles.WEB)
public class RecoveryController {

  private final RecoveryService recoveryService;

  @PostMapping("")
  public ResponseEntity<Void> recoverPids(@RequestBody RecoveryEvent event)
      throws DoiResolutionException, DataCiteApiException, JsonProcessingException, InvalidRequestException {
    recoveryService.recoverDataciteDois(event);
    return ResponseEntity.ok(null);
  }


}
