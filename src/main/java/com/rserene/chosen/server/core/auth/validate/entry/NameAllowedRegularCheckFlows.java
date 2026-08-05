package com.rserene.chosen.server.core.auth.validate.entry;

import java.util.regex.Pattern;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.auth.validate.ValidateContext;
import com.rserene.chosen.server.core.main.RSLVCore;
import com.rserene.chosen.server.flows.workflows.BaseFlows;
import com.rserene.chosen.server.flows.workflows.Signal;

public class NameAllowedRegularCheckFlows extends BaseFlows<ValidateContext> {
   private final RSLVCore core;

   public NameAllowedRegularCheckFlows(RSLVCore core) {
      this.core = core;
   }

   public Signal run(ValidateContext validateContext) {
      String nameAllowedRegular = this.core.getPluginConfig().getNameAllowedRegular();
      if (ValueUtil.isEmpty(nameAllowedRegular)) {
         return Signal.PASSED;
      } else if (!Pattern.matches(nameAllowedRegular, validateContext.getBaseServiceAuthenticationResult().getResponse().getName())) {
         validateContext.setDisallowMessage(
            this.core
               .getLanguageHandler()
               .getMessage(
                  "auth_validate_failed_username_mismatch",
                  new Pair("name", validateContext.getBaseServiceAuthenticationResult().getResponse().getName()),
                  new Pair("regular", nameAllowedRegular)
               )
         );
         return Signal.TERMINATED;
      } else {
         return Signal.PASSED;
      }
   }
}
