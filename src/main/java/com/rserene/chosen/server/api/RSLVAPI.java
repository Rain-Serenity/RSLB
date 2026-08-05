package com.rserene.chosen.server.api;

import java.util.Collection;
import java.util.UUID;
import com.rserene.chosen.server.api.data.RSLVPlayerData;
import com.rserene.chosen.server.api.service.IService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface RSLVAPI {
   @NotNull
   Collection<? extends IService> getServices();

   @Nullable
   RSLVPlayerData getPlayerData(@NotNull UUID var1);
}
