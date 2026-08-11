package com.rserene.chosen.server.command.bukkit;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.command.CommandAPI;
import com.rserene.chosen.server.main.RSLBCoreAPI;
import com.rserene.chosen.server.util.MessageUtil;
import com.rserene.chosen.server.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

/**
 * Bukkit 侧命令入口：把 /rslb 命令按静态树分派给核心 CommandAPI，
 * 同时负责 TAB 补全与命令级权限检查（权限节点来自树的 perms 定义）。
 */
public final class CommandHandler implements CommandExecutor, TabCompleter {
    private static final Node ROOT = buildTree();
    private final RSLB plugin;

    public CommandHandler(RSLB plugin) {
        this.plugin = plugin;
    }

    public void register() {
        PluginCommand command = this.plugin.getCommand("rslb");
        if (command == null) {
            this.plugin.getLogger().severe("Failed to bind /rslb command: command not registered in plugin.yml");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            this.executeCore(sender, "rslb help");
            return true;
        }
        String first = args[0].toLowerCase(Locale.ROOT);
        Node node = ROOT.words.get(first);
        if (node == null) {
            this.sendWrongCommand(sender);
            return true;
        }
        List<List<String>> permissionSets = new ArrayList<>();
        if (!this.validatePath(args, permissionSets)) {
            this.sendWrongCommand(sender);
            return true;
        }
        for (List<String> perms : permissionSets) {
            if (!hasPermission(sender, perms)) {
                MessageUtil.sendLegacy(sender, this.message("command_message_no_permission"));
                return true;
            }
        }
        this.executeCore(sender, "rslb " + this.canonicalPath(args));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("rslb")) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        if (args.length == 0) {
            return List.of();
        }
        if (!sender.hasPermission("rslb.tab.complete")) {
            return List.of();
        }
        return this.suggest(args, sender);
    }

    private List<String> suggest(String[] args, CommandSender sender) {
        String partial = args[args.length - 1].toLowerCase(Locale.ROOT);
        Node node = ROOT;
        for (int i = 0; i < args.length - 1; i++) {
            if (node.paramChild != null) {
                node = node.paramChild;
                continue;
            }
            Node child = node.words.get(args[i].toLowerCase(Locale.ROOT));
            if (child == null) {
                return List.of();
            }
            node = child;
        }
        if (node.paramChild != null || node.display == null) {
            return this.paramSuggestions(sender, args);
        }
        List<String> suggestions = new ArrayList<>();
        for (Node child : node.words.values()) {
            if (!hasPermission(sender, child.perms)) {
                continue;
            }
            String display = child.display;
            if (!display.toLowerCase(Locale.ROOT).startsWith(partial)) {
                continue;
            }
            if (display.equalsIgnoreCase(partial)) {
                if (child.paramChild != null || child.words.isEmpty()) {
                    continue;
                }
                for (Node sub : child.words.values()) {
                    if (hasPermission(sender, sub.perms)) {
                        suggestions.add(display + " " + sub.display);
                    }
                }
            } else {
                suggestions.add(display);
            }
        }
        return suggestions;
    }

    private List<String> paramSuggestions(CommandSender sender, String[] args) {
        RSLBCoreAPI api = this.plugin.getCoreAPI();
        CommandAPI commandAPI = api == null ? null : api.getCommandHandler();
        if (commandAPI == null) {
            return List.of();
        }
        return commandAPI.tabComplete(sender, "rslb " + String.join(" ", args));
    }

    private boolean validatePath(String[] args, List<List<String>> permissionSets) {
        return match(ROOT, 0, args, permissionSets);
    }

    private boolean match(Node node, int index, String[] args, List<List<String>> permissionSets) {
        if (index >= args.length) {
            if (!node.terminable) {
                return false;
            }
            if (!node.perms.isEmpty()) {
                permissionSets.add(node.perms);
            }
            if (!node.selfPerms.isEmpty()) {
                permissionSets.add(node.selfPerms);
            }
            return true;
        }
        if (node.paramChild != null) {
            if (index == args.length - 1) {
                Node paramChild = node.paramChild;
                if (!paramChild.terminable) {
                    return false;
                }
                if (!paramChild.selfPerms.isEmpty()) {
                    permissionSets.add(paramChild.selfPerms);
                }
                return true;
            }
            return match(node.paramChild, index + 1, args, permissionSets);
        }
        Node child = node.words.get(args[index].toLowerCase(Locale.ROOT));
        if (child == null) {
            return false;
        }
        if (!child.perms.isEmpty()) {
            permissionSets.add(child.perms);
        }
        return match(child, index + 1, args, permissionSets);
    }

    private String canonicalPath(String[] args) {
        StringBuilder sb = new StringBuilder();
        Node node = ROOT;
        for (String arg : args) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            if (node.paramChild != null) {
                sb.append(arg);
                node = node.paramChild;
            } else {
                Node child = node.words.get(arg.toLowerCase(Locale.ROOT));
                if (child == null) {
                    sb.append(arg);
                } else {
                    sb.append(child.display);
                    node = child;
                }
            }
        }
        return sb.toString().trim();
    }

    private static boolean hasPermission(CommandSender sender, List<String> perms) {
        if (perms.isEmpty()) {
            return true;
        }
        for (String perm : perms) {
            if (sender.hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

    private void sendWrongCommand(CommandSender sender) {
        MessageUtil.sendLegacy(sender, this.message("command_message_error_header"));
        this.executeCore(sender, "rslb help");
    }

    private void executeCore(CommandSender sender, String command) {
        RSLBCoreAPI api = this.plugin.getCoreAPI();
        CommandAPI commandAPI = api == null ? null : api.getCommandHandler();
        if (commandAPI == null) {
            MessageUtil.sendLegacy(sender, this.message("command_error"));
            return;
        }
        commandAPI.execute(sender, command);
    }

    private String message(String key) {
        return this.plugin.getCoreAPI().getLanguageHandler().getMessage(key, new Pair[0]);
    }

    private static Node buildTree() {
        Node root = new Node(null);
        root.word("reload", "rslb.reload").end();
        root.word("eraseUsername", "rslb.erase.username").param().end();
        root.word("eraseAllUsernames", "rslb.erase.all").end();
        root.word("confirm", "rslb.confirm").end();
        root.word("list", "rslb.list").end();
        root.word("help", "rslb.base").end();
        Node link = root.word("link", "rslb.link.to", "rslb.link.accept", "rslb.link.code");
        link.word("to", "rslb.link.to").param().end();
        link.word("accept", "rslb.link.accept").param().end();
        link.word("code", "rslb.link.code").param().param().end();
        Node whitelist = root.word(
            "whitelist",
            "rslb.whitelist.add",
            "rslb.whitelist.remove",
            "rslb.whitelist.specific.add",
            "rslb.whitelist.specific.remove",
            "rslb.whitelist.list",
            "rslb.whitelist.list.verbose"
        );
        whitelist.word("add", "rslb.whitelist.add").param().end();
        whitelist.word("remove", "rslb.whitelist.remove").param().end();
        Node specific = whitelist.word("specific", "rslb.whitelist.specific.add", "rslb.whitelist.specific.remove");
        specific.word("add", "rslb.whitelist.specific.add").param().end();
        specific.word("remove", "rslb.whitelist.specific.remove").param().end();
        Node whitelistList = whitelist.word("list", "rslb.whitelist.list");
        whitelistList.end();
        whitelistList.word("verbose", "rslb.whitelist.list.verbose").end();
        Node profile = root.word(
            "profile",
            "rslb.profile.create",
            "rslb.profile.set.oneself",
            "rslb.profile.set.other",
            "rslb.profile.remove"
        );
        Node create = profile.word("create", "rslb.profile.create");
        create.param("rslb.profile.create").end();
        create.param("rslb.profile.create").param("rslb.profile.create").end();
        Node set = profile.word("set", "rslb.profile.set.oneself", "rslb.profile.set.other");
        set.param("rslb.profile.set.oneself").end();
        set.param("rslb.profile.set.oneself").param("rslb.profile.set.other").end();
        profile.word("remove", "rslb.profile.remove").param("rslb.profile.remove").end();
        Node rename = root.word("rename", "rslb.rename.oneself", "rslb.rename.other");
        rename.param("rslb.rename.oneself").end();
        rename.param("rslb.rename.oneself").param("rslb.rename.other").end();
        Node find = root.word("find", "rslb.find.profile", "rslb.find.online");
        find.word("profile", "rslb.find.profile").param().end();
        find.word("online", "rslb.find.online").param().end();
        Node info = root.word("info", "rslb.info.oneself", "rslb.info.other");
        info.end();
        info.param("rslb.info.other").end();
        return root;
    }

    /**
     * 静态命令树节点：words 为子命令表（TreeMap，保证 TAB 补全有序），
     * paramChild 为参数占位节点，perms/selfPerms 为权限节点。
     */
    private static final class Node {
        private final Map<String, Node> words;
        private final String display;
        private final List<String> perms;
        private List<String> selfPerms;
        private Node paramChild;
        private boolean terminable;

        private Node(String display) {
            this(display, List.of());
        }

        private Node(String display, List<String> perms) {
            this.words = new TreeMap<>();
            this.selfPerms = List.of();
            this.display = display;
            this.perms = perms;
        }

        private Node word(String word) {
            return this.words.computeIfAbsent(word.toLowerCase(Locale.ROOT), Node::new);
        }

        private Node word(String word, String... perms) {
            return this.words.computeIfAbsent(word.toLowerCase(Locale.ROOT), w -> new Node(w, List.of(perms)));
        }

        private Node param() {
            return this.param(List.of());
        }

        private Node param(String... selfPerms) {
            return this.param(List.of(selfPerms));
        }

        private Node param(List<String> selfPerms) {
            if (this.paramChild == null) {
                this.paramChild = new Node(null);
            }
            this.paramChild.selfPerms = selfPerms;
            return this.paramChild;
        }

        private Node end() {
            this.terminable = true;
            return this;
        }
    }
}
