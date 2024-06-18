package eu.dissco.core.datacitepublisher.controller;

import static eu.dissco.core.datacitepublisher.TestUtils.givenRecoveryEvent;
import static org.assertj.core.api.Assertions.assertThat;

import eu.dissco.core.datacitepublisher.service.RecoveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RecoveryControllerTest {

  @Mock
  private RecoveryService recoveryService;

  private RecoveryController recoveryController;

  @BeforeEach
  void init(){
    recoveryController = new RecoveryController(recoveryService);
  }

  @Test
  void testRecoverPids() throws Exception {
    // Given

    // When
    var result = recoveryController.recoverPids(givenRecoveryEvent());

    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

}
