package com.rserene.chosen.server.core.auth.service.yggdrasil;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;
import com.rserene.chosen.server.core.configuration.service.yggdrasil.BaseYggdrasilServiceConfig;
import com.rserene.chosen.server.core.main.RSLVCore;
import com.rserene.chosen.server.flows.workflows.EntrustFlows;
import com.rserene.chosen.server.flows.workflows.Signal;

public class YggdrasilAuthenticationService {
   private final RSLVCore core;

   public YggdrasilAuthenticationService(RSLVCore core) {
      this.core = core;
   }

   public YggdrasilAuthenticationResult hasJoined(String username, String serverId, String ip) throws SQLException {
      Set<Integer> ids = this.core
         .getPluginConfig()
         .getServiceIdMap()
         .entrySet()
         .stream()
         .filter(e -> e.getValue() instanceof BaseYggdrasilServiceConfig)
         .map(Entry::getKey)
         .collect(Collectors.toSet());
      if (ids.size() == 0) {
         return YggdrasilAuthenticationResult.ofNoService();
      }

      Set<Integer> primaries = new HashSet<>();
      if (ids.size() == 1) {
         primaries.add(ids.iterator().next());
      } else {
         UUID inGameUUID = this.core.getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
         if (inGameUUID != null) {
            primaries.addAll(this.core.getSqlManager().getUserDataTable().getOnlineServiceIds(inGameUUID));
         }
      }

      Set<Integer> secondaries = ids.stream().filter(i -> !primaries.contains(i)).collect(Collectors.toSet());
      LoggerProvider.getLogger()
         .debug(
            String.format(
               "%s's hasJoined verification order: [%s], [%s]", username, ValueUtil.join(", ", ", ", primaries), ValueUtil.join(", ", ", ", secondaries)
            )
         );
      boolean serverBreakdown = false;
      if (primaries.size() != 0) {
         YggdrasilAuthenticationResult result = this.hasJoined0(username, serverId, ip, primaries);
         if (result.getReason() == YggdrasilAuthenticationResult.Reason.ALLOWED) {
            return result;
         }

         if (result.getReason() == YggdrasilAuthenticationResult.Reason.SERVER_BREAKDOWN) {
            serverBreakdown = true;
         }
      }

      if (secondaries.size() != 0) {
         YggdrasilAuthenticationResult result = this.hasJoined0(username, serverId, ip, secondaries);
         if (result.getReason() == YggdrasilAuthenticationResult.Reason.ALLOWED) {
            return result;
         }

         if (result.getReason() == YggdrasilAuthenticationResult.Reason.SERVER_BREAKDOWN) {
            serverBreakdown = true;
         }
      }

      return serverBreakdown ? YggdrasilAuthenticationResult.ofServerBreakdown() : YggdrasilAuthenticationResult.ofValidationFailed();
   }

   private YggdrasilAuthenticationResult hasJoined0(String username, String serverId, String ip, Set<Integer> ids) {
      Set<BaseYggdrasilServiceConfig> serviceConfigs = new HashSet<>();

      for (Integer id : ids) {
         BaseServiceConfig config = this.core.getPluginConfig().getServiceIdMap().get(id);
         if (config instanceof BaseYggdrasilServiceConfig) {
            serviceConfigs.add((BaseYggdrasilServiceConfig)config);
         }
      }

      EntrustFlows<HasJoinedContext> flows = new EntrustFlows(
         serviceConfigs.stream().map(i -> new YggdrasilAuthenticationFlows(this.core, username, serverId, ip, i)).collect(Collectors.toList())
      );
      HasJoinedContext context = new HasJoinedContext(username, serverId, ip);
      Signal run = flows.run(context);
      if (run == Signal.PASSED) {
         return YggdrasilAuthenticationResult.ofAllowed(
            (GameProfile)context.getResponse().get().getValue1(), (BaseYggdrasilServiceConfig)context.getResponse().get().getValue2()
         );
      }

      if (context.getServiceUnavailable().size() == 0) {
         return YggdrasilAuthenticationResult.ofValidationFailed();
      }

      for (Entry<BaseYggdrasilServiceConfig, Throwable> entry : context.getServiceUnavailable().entrySet()) {
         LoggerProvider.getLogger()
            .debug("An exception occurred during authentication of the yggdrasil service whose ID is " + entry.getKey().getId(), entry.getValue());
      }

      return YggdrasilAuthenticationResult.ofServerBreakdown();
   }
}
