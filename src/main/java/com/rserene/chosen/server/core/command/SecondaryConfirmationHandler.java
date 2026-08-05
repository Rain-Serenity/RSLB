package com.rserene.chosen.server.core.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;

public class SecondaryConfirmationHandler {
   private final Map<IPlayer, SecondaryConfirmationHandler.ConfirmEntry> concurrentHashMap = new ConcurrentHashMap<>();
   private final AtomicReference<SecondaryConfirmationHandler.ConfirmEntry> consoleConfirm = new AtomicReference<>();

   public void submit(ISender sender, SecondaryConfirmationHandler.CallbackConfirmCommand callbackConfirmCommand, String desc, String consequences) {
      if (sender.isPlayer()) {
         this.concurrentHashMap.put(sender.getAsPlayer(), new SecondaryConfirmationHandler.ConfirmEntry(callbackConfirmCommand));
      } else {
         if (!sender.isConsole()) {
            sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_unidentified"));
            return;
         }

         this.consoleConfirm.set(new SecondaryConfirmationHandler.ConfirmEntry(callbackConfirmCommand));
      }

      sender.sendMessagePL(
         CommandHandler.getCore()
            .getLanguageHandler()
            .getMessage("command_message_confirm_warning", new Pair("desc", desc), new Pair("consequences", consequences))
      );
   }

   public void confirm(ISender sender) throws Exception {
      this.concurrentHashMap.values().removeIf(SecondaryConfirmationHandler.ConfirmEntry::isInvalid);
      this.consoleConfirm.updateAndGet(confirmEntry -> {
         if (confirmEntry == null) {
            return null;
         } else {
            return confirmEntry.isInvalid() ? null : confirmEntry;
         }
      });
      if (sender.isPlayer()) {
         SecondaryConfirmationHandler.ConfirmEntry entry = this.concurrentHashMap.remove(sender.getAsPlayer());
         if (entry == null) {
            sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_not_found"));
            return;
         }

         entry.confirm();
      } else if (sender.isConsole()) {
         SecondaryConfirmationHandler.ConfirmEntry entry = this.consoleConfirm.getAndSet(null);
         if (entry == null) {
            sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_not_found"));
            return;
         }

         entry.confirm();
      } else {
         sender.sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_confirm_unidentified"));
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
