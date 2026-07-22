package com.clara.ops.challenge.document_management_service_challenge.infrastructure.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckSum {

  private static final String CHECKSUM_ALGORITHM = "SHA-256";

  public static Result<String, Exception> generateCheckSum(InputStream input, String fileName) {
    try {
      MessageDigest digest = MessageDigest.getInstance(CHECKSUM_ALGORITHM);
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = input.read(buffer)) != -1) {
        digest.update(buffer, 0, bytesRead);
      }
      return Result.success(HexFormat.of().formatHex(digest.digest()));

    } catch (IOException | NoSuchAlgorithmException exception) {
      log.error("Failed to generate checksum for file '{}'", fileName, exception);
      return Result.failure(exception);
    }
  }
}
