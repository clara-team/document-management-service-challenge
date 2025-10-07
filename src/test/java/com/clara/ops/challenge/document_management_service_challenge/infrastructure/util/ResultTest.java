package com.clara.ops.challenge.document_management_service_challenge.infrastructure.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class ResultTest {

  /**
   * Test for of() method when supplier executes successfully. It should return a Success type
   * Result.
   */
  @Test
  void testOf_Success() {
    // Arrange
    Supplier<String> successfulSupplier = () -> "Test Success";

    // Act
    Result<String, Exception> result = Result.of(successfulSupplier);

    // Assert
    assertTrue(result.isSuccess(), "Result should be a Success type.");
    assertEquals(
        "Test Success", result.getValue(), "Success value should match the supplier output.");
  }

  /**
   * Test for of() method when supplier throws an exception. It should return a Failure type Result.
   */
  @Test
  void testOf_Failure() {
    // Arrange
    Supplier<String> failingSupplier =
        () -> {
          throw new RuntimeException("Test Failure");
        };

    // Act
    Result<String, Exception> result = Result.of(failingSupplier);

    // Assert
    assertTrue(result.isFailure(), "Result should be a Failure type.");
    Exception error = result.getError();
    assertNotNull(error, "Error in Failure result should not be null.");
    assertEquals(
        RuntimeException.class,
        error.getClass(),
        "Error type should match the thrown exception type.");
    assertEquals(
        "Test Failure",
        error.getMessage(),
        "Error message should match the thrown exception message.");
  }
}
