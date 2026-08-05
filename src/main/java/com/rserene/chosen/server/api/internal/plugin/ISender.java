package com.rserene.chosen.server.api.internal.plugin;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ISender {
   boolean isPlayer();

   boolean isConsole();

   boolean hasPermission(String var1);

   void sendMessagePL(String var1);

   String getName();

   IPlayer getAsPlayer();
}
