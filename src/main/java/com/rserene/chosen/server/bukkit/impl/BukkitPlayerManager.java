package com.rserene.chosen.server.bukkit.impl;

import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.IPlayerManager;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitPlayerManager implements IPlayerManager {

    @Override
    public Set<IPlayer> getPlayers(String name) {
        return Bukkit.getOnlinePlayers().stream()
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .map(BukkitPlayer::new)
            .collect(Collectors.toSet());
    }

    @Override
    public IPlayer getPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? new BukkitPlayer(player) : null;
    }

    @Override
    public Set<IPlayer> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
            .map(BukkitPlayer::new)
            .collect(Collectors.toSet());
    }
}
