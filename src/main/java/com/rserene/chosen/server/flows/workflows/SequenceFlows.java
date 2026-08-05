package com.rserene.chosen.server.flows.workflows;

import java.util.Collections;
import java.util.List;
import lombok.Generated;

public class SequenceFlows<C> extends BaseFlows<C> {
   private final List<BaseFlows<C>> steps;

   public SequenceFlows(List<BaseFlows<C>> steps) {
      this.steps = Collections.unmodifiableList(steps);
   }

   @Override
   public Signal run(C context) {
      for (BaseFlows<C> step : this.steps) {
         Signal signal = step.run(context);
         if (signal != Signal.PASSED && signal == Signal.TERMINATED) {
            return Signal.TERMINATED;
         }
      }

      return Signal.PASSED;
   }

   @Generated
   public List<BaseFlows<C>> getSteps() {
      return this.steps;
   }
}
