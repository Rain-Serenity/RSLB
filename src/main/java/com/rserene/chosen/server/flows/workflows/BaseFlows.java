package com.rserene.chosen.server.flows.workflows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;

public abstract class BaseFlows<CONTEXT> {
   private static final AtomicInteger asyncThreadId = new AtomicInteger(0);
   private static final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
      Thread thread = new Thread(r, "RSLB Flows #" + asyncThreadId.incrementAndGet());
      thread.setDaemon(true);
      return thread;
   });

   public static synchronized void close() {
      if (!executorService.isShutdown()) {
         executorService.shutdown();
      }
   }

   public abstract Signal run(CONTEXT var1);

   @Generated
   protected static ExecutorService getExecutorService() {
      return executorService;
   }
}
