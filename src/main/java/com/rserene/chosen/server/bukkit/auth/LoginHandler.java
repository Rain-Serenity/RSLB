package com.rserene.chosen.server.bukkit.auth;

import com.google.common.primitives.Ints;
import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.auth.AuthResult;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.core.auth.LoginAuthResult;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.network.Connection;
import net.minecraft.network.HandlerNames;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Crypt;
import org.bukkit.Bukkit;

/**
 * Pure-NMS multi-Yggdrasil login interception for Luminol 26.2 (Folia).
 *
 * The ChannelInitializer of every ServerBootstrapAcceptor is wrapped so that
 * each accepted connection gets an interceptor installed inside initChannel,
 * before the client's first packet is ever processed. ServerboundHelloPacket
 * is consumed and replaced with a native ClientboundHelloPacket that carries
 * {@code shouldAuthenticate = true}, forcing 26.2 clients to call joinServer
 * with their session token. ServerboundKeyPacket is consumed, the shared
 * secret is decrypted with the server keypair, and the resulting serverId is
 * verified against every configured Yggdrasil service through RSLV's
 * AuthHandler (hasJoined). Only on ALLOW is the vanilla login state machine
 * resumed by setting {@code authenticatedProfile} + {@code state = VERIFYING}
 * on the login listener, which makes the vanilla tick() drive compression,
 * duplicate checks and LoginFinished. Unauthenticated players never reach the
 * game - they are disconnected during the login phase.
 */
public final class LoginHandler {
    private static final String HANDLER_NAME = "rslb_login_handler";
    private static final String ACCEPTOR_CLASS = "io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor";
    private static final AtomicInteger AUTH_THREAD_ID = new AtomicInteger();

    private final RSLB plugin;
    private final MinecraftServer server;
    private final Map<Connection, LoginSession> sessions = new ConcurrentHashMap<>();
    private final Set<Object> wrappedAcceptors = ConcurrentHashMap.newKeySet();
    private final Random random = new Random();

    private Field channelField;
    private Field authenticatedProfileField;
    private Field stateField;

    private volatile io.papermc.paper.threadedregions.scheduler.ScheduledTask tickTask;

    public LoginHandler(RSLB plugin) {
        this.plugin = plugin;
        this.server = getMinecraftServer();
        try {
            this.channelField = Connection.class.getDeclaredField("channel");
            this.channelField.setAccessible(true);
            this.authenticatedProfileField = ServerLoginPacketListenerImpl.class.getDeclaredField("authenticatedProfile");
            this.authenticatedProfileField.setAccessible(true);
            this.stateField = ServerLoginPacketListenerImpl.class.getDeclaredField("state");
            this.stateField.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve NMS fields for login interception", e);
        }
    }

    public void start() {
        this.wrapAcceptors();
        this.tickTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> injectAll(), 1L, 1L);
        LoggerProvider.getLogger().info("Login interceptor enabled - all logins will be verified against configured Yggdrasil services");
    }

    public void stop() {
        if (this.tickTask != null) {
            this.tickTask.cancel();
        }
    }

    private static MinecraftServer getMinecraftServer() {
        try {
            Object craftServer = Bukkit.getServer();
            Method getServer = craftServer.getClass().getMethod("getServer");
            return (MinecraftServer) getServer.invoke(craftServer);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to access MinecraftServer", e);
        }
    }

    private void injectAll() {
        wrapAcceptors();
        try {
            List<Connection> connections = this.server.getConnection().getConnections();
            for (Connection connection : connections) {
                if (connection.isMemoryConnection()) {
                    continue;
                }
                Channel channel = getChannel(connection);
                if (channel == null) {
                    continue;
                }
                if (channel.pipeline().get(HANDLER_NAME) != null) {
                    continue;
                }
                channel.eventLoop().execute(() -> inject(channel));
            }
        } catch (Exception e) {
            LoggerProvider.getLogger().debug("Login interceptor scan failed: " + e.getMessage());
        }
    }

    /**
     * Wraps the ChannelInitializer of every server bootstrap acceptor so that
     * each newly accepted connection gets the login interceptor installed
     * inside initChannel(), i.e. before the first packet of the client is ever
     * processed. A plain per-tick scan loses the race against local clients
     * whose handshake + LoginStart arrive within milliseconds of connect.
     */
    private void wrapAcceptors() {
        try {
            for (ChannelFuture future : getServerChannels()) {
                Channel serverChannel = future.channel();
                if (serverChannel == null) {
                    continue;
                }
                for (Map.Entry<String, ChannelHandler> entry : serverChannel.pipeline()) {
                    Class<?> acceptorType = findAcceptorType(entry.getValue().getClass());
                    if (acceptorType == null) {
                        continue;
                    }
                    ChannelHandler acceptor = entry.getValue();
                    if (this.wrappedAcceptors.contains(acceptor)) {
                        continue;
                    }
                    try {
                        Field childHandlerField = acceptorType.getDeclaredField("childHandler");
                        childHandlerField.setAccessible(true);
                        ChannelHandler original = (ChannelHandler) childHandlerField.get(acceptor);
                        if (original == null) {
                            continue;
                        }
                        ChannelInitializer<Channel> wrapper = new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel ch) {
                                ch.pipeline().addLast(original);
                                inject(ch);
                            }
                        };
                        childHandlerField.set(acceptor, wrapper);
                        this.wrappedAcceptors.add(acceptor);
                        LoggerProvider.getLogger().debug("Wrapped server bootstrap acceptor - login interception installed before first packet");
                    } catch (Exception e) {
                        LoggerProvider.getLogger().debug("Failed to wrap server bootstrap acceptor: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LoggerProvider.getLogger().debug("Acceptor wrap scan failed: " + e.getMessage());
        }
    }

    private List<ChannelFuture> getServerChannels() {
        try {
            Field channelsField = net.minecraft.server.network.ServerConnectionListener.class.getDeclaredField("channels");
            channelsField.setAccessible(true);
            return (List<ChannelFuture>) channelsField.get(this.server.getConnection());
        } catch (Exception e) {
            return List.of();
        }
    }

    private static Class<?> findAcceptorType(Class<?> clazz) {
        while (clazz != null && clazz != Object.class) {
            if (clazz.getName().equals(ACCEPTOR_CLASS)) {
                return clazz;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private void inject(Channel channel) {
        try {
            ChannelHandler packetHandler = channel.pipeline().get(HandlerNames.PACKET_HANDLER);
            if (!(packetHandler instanceof Connection connection)) {
                return;
            }
            if (connection.isMemoryConnection()) {
                return;
            }
            if (channel.pipeline().get(HANDLER_NAME) != null) {
                return;
            }
            channel.pipeline().addBefore(HandlerNames.PACKET_HANDLER, HANDLER_NAME, new Interceptor(connection));
            LoggerProvider.getLogger().debug("Injected login interceptor for " + channel.remoteAddress());
        } catch (Exception e) {
            LoggerProvider.getLogger().debug("Failed to inject login interceptor: " + e.getMessage());
        }
    }

    private Channel getChannel(Connection connection) {
        try {
            return (Channel) this.channelField.get(connection);
        } catch (Exception e) {
            return null;
        }
    }

    private final class Interceptor extends ChannelInboundHandlerAdapter {
        private final Connection connection;

        Interceptor(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ServerboundHelloPacket packet) {
                handleHello(packet);
                return;
            }
            if (msg instanceof ServerboundKeyPacket packet) {
                handleKey(packet);
                return;
            }
            ctx.fireChannelRead(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            sessions.remove(this.connection);
            ctx.fireChannelInactive();
        }

        private void handleHello(ServerboundHelloPacket packet) {
            if (sessions.containsKey(this.connection)) {
                return;
            }
            String username = packet.name();
            byte[] challenge = Ints.toByteArray(random.nextInt());
            LoginSession session = new LoginSession(username, getIp(), challenge);
            sessions.put(this.connection, session);
            byte[] publicKey = server.getKeyPair().getPublic().getEncoded();
            this.connection.send(new ClientboundHelloPacket("", publicKey, challenge, true));
            LoggerProvider.getLogger().debug(
                "Intercepted login start from " + username + " [" + session.getIp() + "], sent encrypted auth request (shouldAuthenticate=true)"
            );
        }

        private void handleKey(ServerboundKeyPacket packet) {
            LoginSession session = sessions.remove(this.connection);
            if (session == null) {
                LoggerProvider.getLogger().warn("Received key packet without a login session");
                return;
            }
            try {
                PrivateKey privateKey = server.getKeyPair().getPrivate();
                if (!packet.isChallengeValid(session.getChallenge(), privateKey)) {
                    LoggerProvider.getLogger().warn("Challenge verification failed for " + session.getUsername());
                    kick(session, "验证失败，请重新连接");
                    return;
                }
                SecretKey secretKey = packet.getSecretKey(privateKey);
                String serverId = new BigInteger(Crypt.digestData("", server.getKeyPair().getPublic(), secretKey)).toString(16);
                this.connection.setEncryptionKey(secretKey);
                LoggerProvider.getLogger().debug("Encryption enabled for " + session.getUsername() + ", serverId=" + serverId);
                authAsync(session, serverId);
            } catch (Exception e) {
                LoggerProvider.getLogger().error("Failed to process key packet for " + session.getUsername(), e);
                kick(session, "认证过程发生错误，请重试");
            }
        }

        private void authAsync(LoginSession session, String serverId) {
            new Thread(() -> {
                try {
                    LoginAuthResult authResult = (LoginAuthResult) plugin.getRSLVCoreAPI().getAuthHandler().auth(session.getUsername(), serverId, session.getIp());
                    if (authResult.getResult() == AuthResult.Result.ALLOW) {
                        com.rserene.chosen.server.api.profile.GameProfile profile = authResult.getResponse();
                        com.mojang.authlib.GameProfile mojangProfile = toMojangProfile(profile);
                        LoggerProvider.getLogger().debug(
                            "Authenticated " + session.getUsername() + " -> " + mojangProfile.id() + " via RSLB"
                        );
                        Bukkit.getGlobalRegionScheduler().run(plugin, task -> completeLogin(mojangProfile));
                    } else {
                        LoggerProvider.getLogger().info("Auth rejected for " + session.getUsername() + ": " + authResult.getKickMessage());
                        kick(session, authResult.getKickMessage());
                    }
                } catch (Exception e) {
                    LoggerProvider.getLogger().error("Auth error for " + session.getUsername(), e);
                    kick(session, "认证过程发生错误，请重试");
                }
            }, "RSLB Auth #" + AUTH_THREAD_ID.incrementAndGet()).start();
        }

        private void completeLogin(com.mojang.authlib.GameProfile profile) {
            Object listener = this.connection.getPacketListener();
            if (!(listener instanceof ServerLoginPacketListenerImpl loginListener)) {
                LoggerProvider.getLogger().warn("Packet listener is not a login listener: " + listener);
                return;
            }
            try {
                authenticatedProfileField.set(loginListener, profile);
                stateField.set(loginListener, Enum.valueOf((Class<Enum>) stateField.getType(), "VERIFYING"));
                LoggerProvider.getLogger().debug(
                    "Login for " + profile.name() + " [" + profile.id() + "] handed to vanilla login state machine"
                );
            } catch (Exception e) {
                LoggerProvider.getLogger().error("Failed to set login listener state for " + profile.name(), e);
            }
        }

        private void kick(LoginSession session, String message) {
            try {
                Component reason = Component.literal(message);
                this.connection.send(new ClientboundLoginDisconnectPacket(reason));
                this.connection.disconnect(reason);
            } catch (Exception e) {
                LoggerProvider.getLogger().error("Failed to kick " + session.getUsername(), e);
            }
        }

        private String getIp() {
            if (this.connection.getRemoteAddress() instanceof InetSocketAddress inet) {
                return inet.getAddress().getHostAddress();
            }
            return "";
        }
    }

    private static com.mojang.authlib.GameProfile toMojangProfile(com.rserene.chosen.server.api.profile.GameProfile profile) {
        com.google.common.collect.ImmutableMultimap.Builder<String, com.mojang.authlib.properties.Property> builder =
            com.google.common.collect.ImmutableMultimap.builder();
        Map<String, com.rserene.chosen.server.api.profile.Property> source = profile.getPropertyMap();
        if (source != null) {
            for (Map.Entry<String, com.rserene.chosen.server.api.profile.Property> entry : source.entrySet()) {
                com.rserene.chosen.server.api.profile.Property p = entry.getValue();
                if (p == null) {
                    continue;
                }
                if (p.getSignature() != null && !p.getSignature().isEmpty()) {
                    builder.put(entry.getKey(), new com.mojang.authlib.properties.Property(p.getName(), p.getValue(), p.getSignature()));
                } else {
                    builder.put(entry.getKey(), new com.mojang.authlib.properties.Property(p.getName(), p.getValue()));
                }
            }
        }
        return new com.mojang.authlib.GameProfile(profile.getId(), profile.getName(), new com.mojang.authlib.properties.PropertyMap(builder.build()));
    }
}
