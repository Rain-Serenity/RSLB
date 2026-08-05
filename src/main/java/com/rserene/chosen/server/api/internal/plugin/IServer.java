package com.rserene.chosen.server.api.internal.plugin;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IServer {
   BaseScheduler getScheduler();

   IPlayerManager getPlayerManager();

   boolean isOnlineMode();

   boolean isForwarded();

   String getName();

   String getVersion();

   void shutdown();

   ISender getConsoleSender();

   boolean pluginHasEnabled(String var1);
}
