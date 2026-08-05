package com.rserene.chosen.server.api.internal.plugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class BaseScheduler {
   private final AtomicInteger asyncThreadId = new AtomicInteger(0);
   private final ScheduledExecutorService asyncExecutor = Executors.newScheduledThreadPool(
      5, r -> new Thread(r, "RSLV Async #" + this.asyncThreadId.incrementAndGet())
   );

   public void runTaskAsync(Runnable runnable) {
      this.asyncExecutor.execute(runnable);
   }

   public void runTaskAsync(Runnable runnable, long delay) {
      this.asyncExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
   }

   public void runTaskAsyncTimer(Runnable run, long delay, long period) {
      this.asyncExecutor.scheduleAtFixedRate(run, delay, period, TimeUnit.MILLISECONDS);
   }

   public synchronized void shutdown() {
      if (!this.asyncExecutor.isShutdown()) {
         this.asyncExecutor.shutdown();
      }
   }

   public abstract void runTask(Runnable var1, long var2);

   public void runTask(Runnable run) {
      this.runTask(run, 0L);
   }
}
