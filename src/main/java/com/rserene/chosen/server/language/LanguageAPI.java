package com.rserene.chosen.server.language;

import com.rserene.chosen.server.util.Pair;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface LanguageAPI {
   String getMessage(String var1, Pair<?, ?>... var2);
}
