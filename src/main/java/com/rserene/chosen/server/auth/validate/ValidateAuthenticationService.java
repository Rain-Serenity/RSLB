package com.rserene.chosen.server.auth.validate;

import java.util.Arrays;
import com.rserene.chosen.server.auth.service.BaseServiceAuthenticationResult;
import com.rserene.chosen.server.auth.validate.entry.AssignInGameFlows;
import com.rserene.chosen.server.auth.validate.entry.InitialLoginDataFlows;
import com.rserene.chosen.server.auth.validate.entry.NameAllowedRegularCheckFlows;
import com.rserene.chosen.server.auth.validate.entry.WhitelistCheckFlows;
import com.rserene.chosen.server.main.RSLBCore;
import com.rserene.chosen.server.flows.workflows.SequenceFlows;
import com.rserene.chosen.server.flows.workflows.Signal;

public class ValidateAuthenticationService {
   private final RSLBCore core;
   private final SequenceFlows<ValidateContext> sequenceFlows;

   public ValidateAuthenticationService(RSLBCore core) {
      this.core = core;
      this.sequenceFlows = new SequenceFlows(
         Arrays.asList(new InitialLoginDataFlows(core), new NameAllowedRegularCheckFlows(core), new WhitelistCheckFlows(core), new AssignInGameFlows(core))
      );
   }

   public ValidateAuthenticationResult checkIn(BaseServiceAuthenticationResult baseServiceAuthenticationResult) {
      ValidateContext context = new ValidateContext(baseServiceAuthenticationResult);
      Signal run = this.sequenceFlows.run(context);
      if (run == Signal.PASSED) {
         if (context.isNeedWait()) {
            try {
               Thread.sleep(500L);
            } catch (InterruptedException e) {
               this.core.logDebug(e);
            }
         }

         return ValidateAuthenticationResult.ofAllowed(context.getInGameProfile());
      } else {
         return ValidateAuthenticationResult.ofDisallowed(context.getDisallowMessage());
      }
   }
}
