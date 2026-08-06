package com.rserene.chosen.server.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.argument.OnlinePlayerArgumentType;
import com.rserene.chosen.server.core.command.argument.StringArgumentType;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;

public class MLinkCommand {
   private final CommandHandler handler;
   private final Map<GameProfile, MLinkCommand.Entry> gameProfileEntryMap = new ConcurrentHashMap<>();

   public MLinkCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literal) {
      return (LiteralArgumentBuilder<ISender>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literal
            .requires(sender ->
               sender.hasPermission("command.RSLB.link.to")
               || sender.hasPermission("command.RSLB.link.accept")
               || sender.hasPermission("command.RSLB.link.code"))
            .then(
               ((LiteralArgumentBuilder)this.handler.literal("to").requires(sender -> sender.hasPermission("command.RSLB.link.to")))
                  .then(this.handler.argument("player", OnlinePlayerArgumentType.players()).executes(this::executeLinkTo))
            ))
            .then(
               ((LiteralArgumentBuilder)this.handler.literal("accept").requires(iSender -> iSender.hasPermission("command.RSLB.link.accept")))
                  .then(this.handler.argument("name", StringArgumentType.string()).executes(this::executeLinkAccept))
            ))
         .then(
            ((LiteralArgumentBuilder)this.handler.literal("code").requires(iSender -> iSender.hasPermission("command.RSLB.link.code")))
               .then(
                  this.handler
                     .argument("player", OnlinePlayerArgumentType.players())
                     .then(this.handler.argument("code", StringArgumentType.string()).executes(this::executeLinkCode))
               )
         );
   }

   private int executeLinkCode(CommandContext<ISender> context) {
      try {
         GameProfile self = (GameProfile)this.handler.requireDataCacheArgumentSelf(context).getValue1();
         IPlayer target = OnlinePlayerArgumentType.getPlayer(context, "player");
         String code = StringArgumentType.getString(context, "code");
         this.gameProfileEntryMap.values().removeIf(e -> e.timeMills < System.currentTimeMillis() - 30000L);
         MLinkCommand.Entry entry = this.gameProfileEntryMap.get(self);
         if (entry == null || !entry.receiverUserInGameUUID.equals(target.getUniqueId()) || entry.code == null) {
            ((ISender)context.getSource()).sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_code_invalid"));
            return 0;
         } else if (!entry.code.equals(code)) {
            ((ISender)context.getSource()).sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_code_invalid_code"));
            return 0;
         } else {
            this.gameProfileEntryMap.remove(self);
            CommandHandler.getCore()
               .getSqlManager()
               .getUserDataTable()
               .setInGameUUID(
                  ((GameProfile)entry.requesterOnlineProfile.getValue1()).getId(),
                  (Integer)entry.requesterOnlineProfile.getValue2(),
                  entry.receiverUserInGameUUID
               );
            ((ISender)context.getSource())
               .sendMessagePL(
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage("command_message_code_succeed", new Pair("redirect_name", target.getName()), new Pair("redirect_uuid", target.getUniqueId()))
               );
            ((ISender)context.getSource())
               .getAsPlayer()
               .kickPlayer(
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage(
                        "command_message_code_kickmessage", new Pair("redirect_name", target.getName()), new Pair("redirect_uuid", target.getUniqueId())
                     )
               );
            return 0;
         }
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeLinkAccept(CommandContext<ISender> context) throws CommandSyntaxException {
      this.handler.requireDataCacheArgumentSelf(context);
      String string = StringArgumentType.getString(context, "name");
      this.gameProfileEntryMap
         .values()
         .removeIf(e -> e.timeMills < System.currentTimeMillis() - CommandHandler.getCore().getPluginConfig().getLinkAcceptValidTimeMills());
      Optional<Map.Entry<GameProfile, MLinkCommand.Entry>> entry = this.gameProfileEntryMap
         .entrySet()
         .stream()
         .filter(e -> e.getKey().getName().equalsIgnoreCase(string))
         .filter(e -> e.getValue().receiverUserInGameUUID.equals(((ISender)context.getSource()).getAsPlayer().getUniqueId()))
         .filter(e -> e.getValue().code == null)
         .findFirst();
      if (entry.isEmpty()) {
         ((ISender)context.getSource()).sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_accept_invalid"));
         return 0;
      }

      Map.Entry<GameProfile, MLinkCommand.Entry> profileEntryEntry = entry.get();
      BaseServiceConfig bsc = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(profileEntryEntry.getValue().requesterOnlineProfile.getValue2());
      String targetServiceName;
      if (bsc == null) {
         targetServiceName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_info_unidentified_name");
      } else {
         targetServiceName = bsc.getName();
      }

      this.handler
         .getSecondaryConfirmationHandler()
         .submit(
            (ISender)context.getSource(),
            () -> {
               profileEntryEntry.getValue().code = ValueUtil.generateLinkCode();
               ((ISender)context.getSource())
                  .sendMessagePL(
                     CommandHandler.getCore()
                        .getLanguageHandler()
                        .getMessage(
                           "command_message_accept",
                           new Pair("code", profileEntryEntry.getValue().code),
                           new Pair("profile_name", ((ISender)context.getSource()).getAsPlayer().getName())
                        )
                  );
            },
            CommandHandler.getCore()
               .getLanguageHandler()
               .getMessage(
                  "command_message_accept_desc",
                  new Pair("target_service_name", targetServiceName),
                  new Pair("target_service_id", (Integer)profileEntryEntry.getValue().requesterOnlineProfile.getValue2()),
                  new Pair("target_online_name", profileEntryEntry.getKey().getName()),
                  new Pair("target_online_uuid", profileEntryEntry.getKey().getId()),
                  new Pair("profile_name", ((ISender)context.getSource()).getAsPlayer().getName()),
                  new Pair("profile_uuid", ((ISender)context.getSource()).getAsPlayer().getUniqueId())
               ),
            CommandHandler.getCore()
               .getLanguageHandler()
               .getMessage(
                  "command_message_accept_cq",
                  new Pair("target_service_name", targetServiceName),
                  new Pair("target_service_id", (Integer)profileEntryEntry.getValue().requesterOnlineProfile.getValue2()),
                  new Pair("target_online_name", profileEntryEntry.getKey().getName()),
                  new Pair("target_online_uuid", profileEntryEntry.getKey().getId()),
                  new Pair("profile_name", ((ISender)context.getSource()).getAsPlayer().getName()),
                  new Pair("profile_uuid", ((ISender)context.getSource()).getAsPlayer().getUniqueId())
               )
         );
      return 0;
   }

   private int executeLinkTo(CommandContext<ISender> context) throws CommandSyntaxException {
      Pair<GameProfile, Integer> self = this.handler.requireDataCacheArgumentSelf(context);
      IPlayer target = OnlinePlayerArgumentType.getPlayer(context, "player");
      this.handler.requirePlayerAndNoSelf(context, target);
      this.handler.requireDataCacheArgumentOther(target);
      this.handler
         .getSecondaryConfirmationHandler()
         .submit(
            (ISender)context.getSource(),
            () -> {
               this.gameProfileEntryMap.put((GameProfile)self.getValue1(), new MLinkCommand.Entry(self, target.getUniqueId()));
               ((ISender)context.getSource())
                  .sendMessagePL(
                     CommandHandler.getCore()
                        .getLanguageHandler()
                        .getMessage("command_message_link", new Pair("self_online_name", ((GameProfile)self.getValue1()).getName()))
                  );
            },
            CommandHandler.getCore()
               .getLanguageHandler()
               .getMessage("command_message_link_to_desc", new Pair("redirect_name", target.getName()), new Pair("redirect_uuid", target.getUniqueId())),
            CommandHandler.getCore()
               .getLanguageHandler()
               .getMessage("command_message_link_to_cq", new Pair("redirect_name", target.getName()), new Pair("redirect_uuid", target.getUniqueId()))
         );
      return 0;
   }

   public static class Entry {
      private final long timeMills = System.currentTimeMillis();
      private final Pair<GameProfile, Integer> requesterOnlineProfile;
      private final UUID receiverUserInGameUUID;
      private String code;

      public Entry(Pair<GameProfile, Integer> requesterOnlineProfile, UUID receiverUserInGameUUID) {
         this.requesterOnlineProfile = requesterOnlineProfile;
         this.receiverUserInGameUUID = receiverUserInGameUUID;
      }
   }
}
