package com.rserene.chosen.server.api.internal.plugin;

import java.io.File;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IPlugin {
   File getDataFolder();

   File getTempFolder();

   IServer getRunServer();
}
