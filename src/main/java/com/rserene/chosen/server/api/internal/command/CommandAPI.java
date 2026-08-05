package com.rserene.chosen.server.api.internal.command;

import java.util.List;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface CommandAPI {
   void execute(ISender var1, String[] var2);

   void execute(ISender var1, String var2);

   List<String> tabComplete(ISender var1, String[] var2);

   List<String> tabComplete(ISender var1, String var2);
}
