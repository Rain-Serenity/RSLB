package com.rserene.chosen.server.bukkit.impl;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.plugin.BaseScheduler;

public class BukkitScheduler extends BaseScheduler {
    @Override
    public void runTask(Runnable run, long delay) {
        if (delay > 0) {
            RSLB.getInstance().getServer().getScheduler().runTaskLater(RSLB.getInstance(), run, delay / 50L);
        } else {
            RSLB.getInstance().getServer().getScheduler().runTask(RSLB.getInstance(), run);
        }
    }
}
