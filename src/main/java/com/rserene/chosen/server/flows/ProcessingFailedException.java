package com.rserene.chosen.server.flows;

public class ProcessingFailedException extends RuntimeException {
   public ProcessingFailedException(String message) {
      super(message);
   }

   public ProcessingFailedException(String message, Throwable cause) {
      super(message, cause);
   }

   public ProcessingFailedException(Throwable cause) {
      super(cause);
   }
}
