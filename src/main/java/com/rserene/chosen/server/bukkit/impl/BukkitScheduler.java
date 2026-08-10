package com.rserene.chosen.server.bukkit.impl;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.plugin.BaseScheduler;
import org.bukkit.Bukkit;

public class BukkitScheduler extends BaseScheduler {
    @Override
    public void runTask(Runnable run, long delay) {
        // GlobalRegionScheduler 在 Paper 26.2 与 Folia 上均为全局调度器，
        // 替代 Bukkit.getScheduler()（Folia 上不存在）。
        if (delay > 0) {
            Bukkit.getGlobalRegionScheduler().runDelayed(RSLB.getInstance(), task -> run.run(), delay / 50L);
        } else {
            Bukkit.getGlobalRegionScheduler().run(RSLB.getInstance(), task -> run.run());
        }
    }
}
