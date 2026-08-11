package com.rserene.chosen.server.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SecondaryConfirmationHandler {
   private final Map<Player, SecondaryConfirmationHandler.ConfirmEntry> concurrentHashMap = new ConcurrentHashMap<>();
   private final AtomicReference<SecondaryConfirmationHandler.ConfirmEntry> consoleConfirm = new AtomicReference<>();

   public void submit(CommandSender sender, SecondaryConfirmationHandler.CallbackConfirmCommand callbackConfirmCommand, String desc, String consequences) {
      if (sender instanceof Player player) {
         this.concurrentHashMap.put(player, new SecondaryConfirmationHandler.ConfirmEntry(callbackConfirmCommand));
      } else {
         this.consoleConfirm.set(new SecondaryConfirmationHandler.ConfirmEntry(callbackConfirmCommand));
      }

      MessageUtil.sendLegacy(
         sender,
         CommandHandler.getCore()
            .getLanguageHandler()
            .getMessage("command_message_confirm_warning", new Pair("desc", desc), new Pair("consequences", consequences))
      );
   }

   public void confirm(CommandSender sender) throws Exception {
      this.concurrentHashMap.values().removeIf(SecondaryConfirmationHandler.ConfirmEntry::isInvalid);
      this.consoleConfirm.updateAndGet(confirmEntry -> {
         if (confirmEntry == null) {
            return null;
         } else {
            return confirmEntry.isInvalid() ? null : confirmEntry;
         }
      });
      if (sender instanceof Player player) {
         SecondaryConfirmationHandler.ConfirmEntry entry = this.concurrentHashMap.remove(player);
         if (entry == null) {
            MessageUtil.sendLegacy(sender, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_not_found"));
            return;
         }

         entry.confirm();
      } else {
         SecondaryConfirmationHandler.ConfirmEntry entry = this.consoleConfirm.getAndSet(null);
         if (entry == null) {
            MessageUtil.sendLegacy(sender, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_not_found"));
            return;
         }

         entry.confirm();
      }
   }

   public interface CallbackConfirmCommand {
      void confirm() throws Exception;
   }

   private static class ConfirmEntry {
      private final long subTime = System.currentTimeMillis();
      private final SecondaryConfirmationHandler.CallbackConfirmCommand callbackConfirmCommand;

      private ConfirmEntry(SecondaryConfirmationHandler.CallbackConfirmCommand callbackConfirmCommand) {
         this.callbackConfirmCommand = callbackConfirmCommand;
      }

      private boolean isInvalid() {
         return this.subTime + CommandHandler.getCore().getPluginConfig().getConfirmCommandValidTimeMills() < System.currentTimeMillis();
      }

      public void confirm() throws Exception {
         this.callbackConfirmCommand.confirm();
      }
   }
}