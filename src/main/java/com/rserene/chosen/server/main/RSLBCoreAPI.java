package com.rserene.chosen.server.main;

import com.rserene.chosen.server.auth.AuthAPI;
import com.rserene.chosen.server.command.CommandAPI;
import com.rserene.chosen.server.player.HandlerAPI;
import com.rserene.chosen.server.language.LanguageAPI;
import com.rserene.chosen.server.skinrestorer.SkinRestorerAPI;
import org.bukkit.plugin.Plugin;
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

   Plugin getPlugin();
}
