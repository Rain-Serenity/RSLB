package com.rserene.chosen.server.auth.validate.entry;

import java.util.Locale;
import com.rserene.chosen.server.auth.validate.ValidateContext;
import com.rserene.chosen.server.main.RSLBCore;
import com.rserene.chosen.server.flows.workflows.BaseFlows;
import com.rserene.chosen.server.flows.workflows.Signal;

public class WhitelistCheckFlows extends BaseFlows<ValidateContext> {
   private final RSLBCore core;

   public WhitelistCheckFlows(RSLBCore core) {
      this.core = core;
   }

   public Signal run(ValidateContext validateContext) {
      try {
         boolean removed = this.core
            .getCacheWhitelistHandler()
            .getCachedWhitelist()
            .remove(validateContext.getBaseServiceAuthenticationResult().getResponse().getName().toLowerCase(Locale.ROOT));
         if (removed) {
            this.core
               .getSqlManager()
               .getUserDataTable()
               .setWhitelist(
                  validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                  validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId(),
                  true
               );
         }

         if (!validateContext.getBaseServiceAuthenticationResult().getServiceConfig().isWhitelist()) {
            return Signal.PASSED;
         }

         if (this.core
            .getSqlManager()
            .getUserDataTable()
            .hasWhitelist(
               validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
               validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId()
            )) {
            return Signal.PASSED;
         }

         validateContext.setDisallowMessage(this.core.getLanguageHandler().getMessage("auth_validate_failed_no_whitelist"));
         return Signal.TERMINATED;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }
}
