package com.rserene.chosen.server.core.auth.validate.entry;

import com.rserene.chosen.server.core.auth.validate.ValidateContext;
import com.rserene.chosen.server.core.database.table.UserDataTableV3;
import com.rserene.chosen.server.core.main.RSLBCore;
import com.rserene.chosen.server.flows.workflows.BaseFlows;
import com.rserene.chosen.server.flows.workflows.Signal;

public class InitialLoginDataFlows extends BaseFlows<ValidateContext> {
   private final RSLBCore core;

   public InitialLoginDataFlows(RSLBCore core) {
      this.core = core;
   }

   public Signal run(ValidateContext validateContext) {
      try {
         UserDataTableV3 dataTable = this.core.getSqlManager().getUserDataTable();
         if (!dataTable.dataExists(
            validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
            validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId()
         )) {
            dataTable.insertNewData(
               validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
               validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId(),
               validateContext.getBaseServiceAuthenticationResult().getResponse().getName(),
               null
            );
         } else {
            String currentName = dataTable.getOnlineName(
               validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
               validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId()
            );
            if (!validateContext.getBaseServiceAuthenticationResult().getResponse().getName().equals(currentName)) {
               dataTable.setOnlineName(
                  validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                  validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId(),
                  validateContext.getBaseServiceAuthenticationResult().getResponse().getName()
               );
               validateContext.setOnlineNameUpdated(true);
            }
         }

         return Signal.PASSED;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }
}
