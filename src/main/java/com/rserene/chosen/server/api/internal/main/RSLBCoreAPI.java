package com.rserene.chosen.server.api.internal.main;

import com.rserene.chosen.server.api.internal.auth.AuthAPI;
import com.rserene.chosen.server.api.internal.command.CommandAPI;
import com.rserene.chosen.server.api.internal.handle.HandlerAPI;
import com.rserene.chosen.server.api.internal.language.LanguageAPI;
import com.rserene.chosen.server.api.internal.plugin.IPlugin;
import com.rserene.chosen.server.api.internal.skinrestorer.SkinRestorerAPI;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface RSLBCoreAPI {
   void load() throws Exception;

   void close() throws Exception;

   CommandAPI getCommandHandler();

   LanguageAPI getLanguageHandler();

   AuthAPI getAuthHandler();

   SkinRestorerAPI getSkinRestorerHandler();

   HandlerAPI getPlayerHandler();

   IPlugin getPlugin();
}
