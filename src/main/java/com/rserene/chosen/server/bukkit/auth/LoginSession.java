package com.rserene.chosen.server.bukkit.auth;

/**
 * 单个登录连接在握手期间的会话状态。
 *
 * 记录客户端上报的用户名、来源 IP 以及本次握手的加密挑战值
 * （challenge），供后续 ServerboundKeyPacket 校验客户端是否持有了
 * 对应的私钥，并据此推导出用于 hasJoined 校验的 serverId。
 */
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
