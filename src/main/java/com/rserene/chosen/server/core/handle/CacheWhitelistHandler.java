package com.rserene.chosen.server.core.handle;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;

public class CacheWhitelistHandler {
   private final Set<String> cachedWhitelist = Collections.newSetFromMap(new ConcurrentHashMap<>());

   @Generated
   public Set<String> getCachedWhitelist() {
      return this.cachedWhitelist;
   }
}
