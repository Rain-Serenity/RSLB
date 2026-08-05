package com.rserene.chosen.server.api.internal.injector;

import java.util.Map;
import com.rserene.chosen.server.api.internal.main.RSLVCoreAPI;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface Injector {
   void inject(RSLVCoreAPI var1) throws Throwable;

   void registerChatSession(Map<Integer, Integer> var1);
}
