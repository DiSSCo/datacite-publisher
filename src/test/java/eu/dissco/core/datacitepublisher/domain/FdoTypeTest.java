package eu.dissco.core.datacitepublisher.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;

class FdoTypeTest {

  @Test
  void testDsFromString(){
    // When
    var result = FdoType.fromString("https://hdl.handle.net/21.T11148/894b1e6cad57e921764e");

    // Then
    assertThat(result).isEqualTo(FdoType.DIGITAL_SPECIMEN);
  }

  @Test
  void testMoFromString(){
    // When
    var result = FdoType.fromString("https://hdl.handle.net/21.T11148/bbad8c4e101e8af01115");

    // Then
    assertThat(result).isEqualTo(FdoType.MEDIA_OBJECT);
  }

  @Test
  void testBadTypeFromString(){
    // Then
    assertThrows(IllegalStateException.class, () -> FdoType.fromString("Bad type"));
  }

}
