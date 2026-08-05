package com.rserene.chosen.server.flows.workflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Generated;
import com.rserene.chosen.server.flows.ProcessingFailedException;

public class ParallelFlows<C> extends BaseFlows<C> {
   private final List<BaseFlows<C>> steps;

   public ParallelFlows(List<BaseFlows<C>> steps) {
      this.steps = Collections.unmodifiableList(steps);
   }

   @Override
   public Signal run(C context) {
      AtomicBoolean terminate = new AtomicBoolean(false);
      CountDownLatch latch = new CountDownLatch(1);
      List<BaseFlows<C>> currentTasks = Collections.synchronizedList(new ArrayList<>());
      boolean flag = false;

      for (BaseFlows<C> step : this.steps) {
         flag = true;
         currentTasks.add(step);
         BaseFlows.getExecutorService().execute(() -> {
            try {
               Signal signal = step.run(context);
               if (signal == Signal.TERMINATED) {
                  terminate.set(true);
                  latch.countDown();
                  return;
               }
            } finally {
               currentTasks.remove(step);
               if (currentTasks.isEmpty()) {
                  latch.countDown();
               }
            }
         });
      }

      if (flag) {
         try {
            latch.await();
         } catch (InterruptedException e) {
            throw new ProcessingFailedException(e);
         }
      }

      return terminate.get() ? Signal.TERMINATED : Signal.PASSED;
   }

   @Generated
   public List<BaseFlows<C>> getSteps() {
      return this.steps;
   }
}
