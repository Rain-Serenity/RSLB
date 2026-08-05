package com.rserene.chosen.server.api;

import java.util.Map;

public interface MapperConfigAPI {
   Map<Integer, Integer> getPacketMapping();

   void save();

   void reload();
}
