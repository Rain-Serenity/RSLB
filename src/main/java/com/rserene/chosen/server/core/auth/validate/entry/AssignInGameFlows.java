package com.rserene.chosen.server.core.auth.validate.entry;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.UUID;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.auth.validate.ValidateContext;
import com.rserene.chosen.server.core.main.RSLBCore;
import com.rserene.chosen.server.flows.workflows.BaseFlows;
import com.rserene.chosen.server.flows.workflows.Signal;

public class AssignInGameFlows extends BaseFlows<ValidateContext> {
   private final RSLBCore core;

   public AssignInGameFlows(RSLBCore core) {
      this.core = core;
   }

   public Signal run(ValidateContext validateContext) {
      try {
         UUID inGameUUID = this.core
            .getSqlManager()
            .getUserDataTable()
            .getInGameUUID(
               validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
               validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId()
            );
         String loginName = validateContext.getBaseServiceAuthenticationResult().getResponse().getName();
         if (inGameUUID == null) {
            inGameUUID = validateContext.getBaseServiceAuthenticationResult()
               .getServiceConfig()
               .getInitUUID()
               .generateUUID(validateContext.getBaseServiceAuthenticationResult().getResponse().getId(), loginName);
            synchronized (AssignInGameFlows.class) {
               while (this.core.getSqlManager().getInGameProfileTable().dataExists(inGameUUID)) {
                  LoggerProvider.getLogger().warn(String.format("UUID %s has been used and will take a random value.", inGameUUID.toString()));
                  inGameUUID = UUID.randomUUID();
               }

               this.core
                  .getSqlManager()
                  .getUserDataTable()
                  .setInGameUUID(
                     validateContext.getBaseServiceAuthenticationResult().getResponse().getId(),
                     validateContext.getBaseServiceAuthenticationResult().getServiceConfig().getId(),
                     inGameUUID
                  );
            }
         }

         if (this.core.getPluginConfig().isAutoNameChange() && validateContext.isOnlineNameUpdated()) {
            String username = this.core.getSqlManager().getInGameProfileTable().getUsername(inGameUUID);
            if (!ValueUtil.isEmpty(username)) {
               this.core.getSqlManager().getInGameProfileTable().eraseUsername(username);
            }
         }

         boolean exist = this.core.getSqlManager().getInGameProfileTable().dataExists(inGameUUID);
         if (exist) {
            String username = this.core.getSqlManager().getInGameProfileTable().getUsername(inGameUUID);
            if (!ValueUtil.isEmpty(username)) {
               validateContext.getInGameProfile().setId(inGameUUID);
               validateContext.getInGameProfile().setName(username);
               return Signal.PASSED;
            }
         }

         String fixName = validateContext.getBaseServiceAuthenticationResult().getServiceConfig().generateName(loginName);
         if (fixName.isEmpty()) {
            fixName = "1";
         }

         String initFixName = fixName;
         if (this.core.getPluginConfig().isNameCorrect()) {
            boolean modified;
            UUID ownerUUID;
            for (modified = false;
               (ownerUUID = this.core.getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(fixName)) != null && !ownerUUID.equals(inGameUUID);
               modified = true
            ) {
               fixName = this.incrementString(fixName);
            }

            if (modified) {
               UUID finalInGameUUID = inGameUUID;
               String finalFixName = fixName;
               LoggerProvider.getLogger().warn(String.format("The name %s is occupied, change it to %s.", initFixName, fixName));
               this.core
                  .getPlugin()
                  .getRunServer()
                  .getScheduler()
                  .runTaskAsync(
                     () -> {
                        IPlayer player = this.core.getPlugin().getRunServer().getPlayerManager().getPlayer(finalInGameUUID);
                        player.sendMessagePL(
                           this.core
                              .getLanguageHandler()
                              .getMessage("name_correct_info", new Pair("old_name", initFixName), new Pair("new_name", finalFixName))
                        );
                     },
                     2000L
                  );
            }
         }

         if (exist) {
            try {
               this.core.getSqlManager().getInGameProfileTable().updateUsername(inGameUUID, fixName);
               validateContext.getInGameProfile().setId(inGameUUID);
               validateContext.getInGameProfile().setName(fixName);
               return Signal.PASSED;
            } catch (SQLIntegrityConstraintViolationException e) {
               validateContext.setDisallowMessage(
                  this.core
                     .getLanguageHandler()
                     .getMessage("auth_validate_failed_username_repeated", new Pair("name", validateContext.getInGameProfile().getName()))
               );
               return Signal.TERMINATED;
            }
         } else {
            try {
               this.core.getSqlManager().getInGameProfileTable().insertNewData(inGameUUID, fixName);
               validateContext.getInGameProfile().setId(inGameUUID);
               validateContext.getInGameProfile().setName(fixName);
               return Signal.PASSED;
            } catch (SQLIntegrityConstraintViolationException e) {
               validateContext.setDisallowMessage(
                  this.core
                     .getLanguageHandler()
                     .getMessage("auth_validate_failed_username_repeated", new Pair("name", validateContext.getInGameProfile().getName()))
               );
               return Signal.TERMINATED;
            }
         }
      } catch (Exception e) {
         // BaseFlows 不声明受检异常，反编译出的 Throwable 透传无法编译；这里保持失败向上冒泡。
         throw new RuntimeException(e);
      }
   }

   private String incrementString(String source) {
      if (source.isEmpty()) {
         return "1";
      } else {
         char c = source.charAt(source.length() - 1);
         if (Character.isDigit(c)) {
            int i = Character.getNumericValue(c);
            return i == 9 ? this.incrementString(source.substring(0, source.length() - 1)) + "0" : source.substring(0, source.length() - 1) + (i + 1);
         } else {
            return source + "1";
         }
      }
   }
}
