package com.rserene.chosen.server.bukkit.auth;

public class LoginSession {
    private final String username;
    private final String ip;
    private final byte[] challenge;

    public LoginSession(String username, String ip, byte[] challenge) {
        this.username = username;
        this.ip = ip;
        this.challenge = challenge;
    }

    public String getUsername() {
        return username;
    }

    public String getIp() {
        return ip;
    }

    public byte[] getChallenge() {
        return challenge;
    }
}
