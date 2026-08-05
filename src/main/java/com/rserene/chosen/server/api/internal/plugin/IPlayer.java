package com.rserene.chosen.server.api.internal.plugin;

import java.net.SocketAddress;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IPlayer extends ISender {
   void kickPlayer(String var1);

   UUID getUniqueId();

   SocketAddress getAddress();

   boolean isOnline();
}
