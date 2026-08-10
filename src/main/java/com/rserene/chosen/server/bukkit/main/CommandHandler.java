package com.rserene.chosen.server.bukkit.main;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.command.CommandAPI;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.bukkit.impl.BukkitSender;
import java.util.ArrayList;
import java.util.Arrays;
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
 * RSLB 指令模块（仿 RSIslandManager /rim 的 Bukkit CommandExecutor + TabCompleter 形态）。
 *
 * 指令树与核心 brigadier 命令树一一对应（见 README「指令用法」）：
 *  - 字面量词节点（reload / whitelist / link …）按小写键匹配；
 *  - 参数位（param）按位置匹配：消耗任意一个单词作为参数值，之后可继续衔接下一个参数位
 *    （如 rename &lt;新名&gt; [&lt;档案&gt;]、profile set &lt;档案&gt; [&lt;在线玩家&gt;]、link code &lt;玩家&gt; &lt;验证码&gt;），
 *    参数位自身可终结 = 该分支有 executes（oneself 分支），衔接下一个参数 = other 分支。
 *
 * 执行链路：
 *  - 裸 /rslb 与 /rslb help 直接展示帮助；
 *  - 子命令先做静态校验（未知 / 缺参 / 无权限 → 顶部错误行 + 指令帮助），
 *    校验通过后原样委托核心命令树执行（rslb + 规范化大小写的参数）；
 *  - 所有提示文案均来自 message.yml（& 颜色，控制台与玩家端一致渲染）。
 *
 * 补全链路（纯静态，与核心命令树结构一致）：
 *  - 第一层给出全部子命令；
 *  - 已完整输入的子命令会补出它的下一层词（如 whitelist → add/list/remove/specific，
 *    选择后为 /rslb whitelist add，不会把已输入的命令覆盖掉）；
 *  - 部分输入按前缀过滤；参数位置无静态候选（返回空）；无权限的候选会隐藏。
 */
public class CommandHandler implements CommandExecutor, TabCompleter {

    /** 指令树节点：word 键统一小写存储（大小写宽容匹配），display 为委托核心时的规范写法 */
    private static final class Node {
        private final Map<String, Node> words = new TreeMap<>();
        private final String display;
        private final List<String> perms;
        private Node paramChild;
        private boolean terminable;

        private Node(String display) {
            this(display, List.of());
        }

        private Node(String display, List<String> perms) {
            this.display = display;
            this.perms = perms;
        }

        private Node word(String display) {
            return words.computeIfAbsent(display.toLowerCase(Locale.ROOT), k -> new Node(display));
        }

        private Node word(String display, String... perms) {
            return words.computeIfAbsent(display.toLowerCase(Locale.ROOT), k -> new Node(display, List.of(perms)));
        }

        private Node param() {
            if (paramChild == null) {
                paramChild = new Node(null);
            }
            return paramChild;
        }

        private Node end() {
            this.terminable = true;
            return this;
        }
    }

    /** 指令树：与核心 brigadier 命令树一一对应（字面量词 / 参数位） */
    private static final Node ROOT = new Node(null);

    static {
        ROOT.word("reload", "rslb.reload").end();
        ROOT.word("eraseUsername", "rslb.erase.username").param().end();
        ROOT.word("eraseAllUsernames", "rslb.erase.all").end();
        ROOT.word("confirm", "rslb.confirm").end();
        ROOT.word("list", "rslb.list").end();
        ROOT.word("help", "rslb.base").end();

        Node link = ROOT.word("link");
        link.word("to", "rslb.link.to").param().end();
        link.word("accept", "rslb.link.accept").param().end();
        link.word("code", "rslb.link.code").param().param().end();

        Node whitelist = ROOT.word("whitelist");
        whitelist.word("add", "rslb.whitelist.add").param().end();
        whitelist.word("remove", "rslb.whitelist.remove").param().end();
        Node specific = whitelist.word("specific");
        specific.word("add", "rslb.whitelist.specific.add").param().end();
        specific.word("remove", "rslb.whitelist.specific.remove").param().end();
        Node wlList = whitelist.word("list", "rslb.whitelist.list");
        wlList.end();
        wlList.word("verbose", "rslb.whitelist.list.verbose").end();

        Node profile = ROOT.word("profile");
        Node create = profile.word("create", "rslb.profile.create");
        create.param().end();
        create.param().param().end();
        Node set = profile.word("set");
        set.param().end();
        set.param().param().end();
        profile.word("remove", "rslb.profile.remove").param().end();

        Node rename = ROOT.word("rename");
        rename.param().end();
        rename.param().param().end();

        Node find = ROOT.word("find");
        find.word("profile", "rslb.find.profile").param().end();
        find.word("online", "rslb.find.online").param().end();

        Node info = ROOT.word("info");
        info.end();
        info.param().end();
    }

    private final RSLB plugin;

    public CommandHandler(RSLB plugin) {
        this.plugin = plugin;
    }

    /**
     * 绑定 /rslb 指令（plugin.yml 注册）到本处理器。
     * 必须在 onEnable（核心已加载）后调用。
     */
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
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        ISender sender = new BukkitSender(commandSender);
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            this.executeCore(sender, "rslb help");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        Node target = ROOT.words.get(sub);
        if (target == null) {
            this.sendWrongCommand(sender);
            return true;
        }
        if (!hasPermission(sender, target.perms)) {
            sender.sendMessagePL(this.message("command_message_no_permission"));
            return true;
        }
        if (!validPath(args)) {
            this.sendWrongCommand(sender);
            return true;
        }

        this.executeCore(sender, "rslb " + canonicalPath(args));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // 兼容不同端点的参数形态（某些路径会多带一个命令名）
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

    /** 计算当前位置的补全候选（区间语义：替换光标前的最后一个词） */
    private List<String> suggest(String[] args, CommandSender commandSender) {
        String last = args[args.length - 1].toLowerCase(Locale.ROOT);

        // 定位最后一个已知词节点：沿 args[0..n-2] 逐层匹配（参数位自动吞值并下移一层）
        Node node = ROOT;
        for (int i = 0; i < args.length - 1; i++) {
            if (node.paramChild != null) {
                node = node.paramChild;
                continue;
            }
            Node next = node.words.get(args[i].toLowerCase(Locale.ROOT));
            if (next == null) {
                return List.of();
            }
            node = next;
        }

        // 当前位置是参数值：委托核心建议器（在线玩家 / 档案名 / 服务 ID …），
        // 与核心 brigadier 树的参数槽一一对应，并天然按权限过滤
        if (node.paramChild != null) {
            return this.paramSuggestions(commandSender, args);
        }

        List<String> candidates = new ArrayList<>();
        for (Node candidate : node.words.values()) {
            if (!hasPermission(commandSender, candidate.perms)) {
                continue;
            }
            String name = candidate.display;
            if (!name.toLowerCase(Locale.ROOT).startsWith(last)) {
                continue;
            }
            if (name.equalsIgnoreCase(last)) {
                // 已完整输入该候选：补出它的下一层词（候选词本身作为前缀，
                // 客户端替换区间只覆盖光标前的最后一个词，避免覆盖前面已输入的部分）；
                // 参数位 / 无下一层的候选保持输入（不再补全）
                if (candidate.paramChild != null || candidate.words.isEmpty()) {
                    continue;
                }
                for (Node next : candidate.words.values()) {
                    if (hasPermission(commandSender, next.perms)) {
                        candidates.add(name + " " + next.display);
                    }
                }
            } else {
                // 部分输入：普通前缀过滤（替换当前词）
                candidates.add(name);
            }
        }
        return candidates;
    }

    /** 参数值位置：完整路径拼接 rslb 前缀后委托核心命令树建议器 */
    private List<String> paramSuggestions(CommandSender commandSender, String[] args) {
        CommandAPI commandHandler = this.plugin.getCoreAPI().getCommandHandler();
        if (commandHandler == null) {
            return List.of();
        }
        ISender sender = new BukkitSender(commandSender);
        // 核心 dispatcher 根结点为 "rslb"；末尾追加空格把当前词视作待补全的参数槽
        return commandHandler.tabComplete(sender, "rslb " + String.join(" ", args) + " ");
    }

    /** 指令路径是否合法完整：词按小写匹配、参数位按位置消费任意值，要求落点分支可执行 */
    private boolean validPath(String[] args) {
        return match(ROOT, 0, args);
    }

    private boolean match(Node node, int i, String[] args) {
        if (i >= args.length) {
            return node.terminable;
        }
        if (node.paramChild != null) {
            // 参数位：消耗 args[i] 作为参数值
            if (i == args.length - 1) {
                return node.paramChild.terminable;
            }
            return match(node.paramChild, i + 1, args);
        }
        Node next = node.words.get(args[i].toLowerCase(Locale.ROOT));
        if (next == null) {
            return false;
        }
        return match(next, i + 1, args);
    }

    /** 把输入的指令路径规范化为核心命令树接受的大小写（参数保持原样） */
    private String canonicalPath(String[] args) {
        StringBuilder canonical = new StringBuilder();
        Node node = ROOT;
        for (String token : args) {
            if (canonical.length() > 0) {
                canonical.append(' ');
            }
            if (node.paramChild != null) {
                // 参数值：原样保留
                canonical.append(token);
                node = node.paramChild;
                continue;
            }
            Node next = node.words.get(token.toLowerCase(Locale.ROOT));
            if (next == null) {
                canonical.append(token);
            } else {
                canonical.append(next.display);
                node = next;
            }
        }
        return canonical.toString().trim();
    }

    /** 权限组内任一权限满足即放行 */
    private static boolean hasPermission(ISender sender, List<String> permissions) {
        if (permissions.isEmpty()) {
            return true;
        }
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPermission(CommandSender sender, List<String> permissions) {
        if (permissions.isEmpty()) {
            return true;
        }
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /** 顶部错误行 + 完整指令帮助 */
    private void sendWrongCommand(ISender sender) {
        sender.sendMessagePL(this.message("command_message_error_header"));
        this.executeCore(sender, "rslb help");
    }

    /** 委托核心命令树执行（异步），所有业务文案均来自 message.yml */
    private void executeCore(ISender sender, String input) {
        CommandAPI commandHandler = this.plugin.getCoreAPI().getCommandHandler();
        if (commandHandler == null) {
            sender.sendMessagePL(this.message("command_error"));
            return;
        }
        commandHandler.execute(sender, input);
    }

    private String message(String node) {
        return this.plugin.getCoreAPI().getLanguageHandler().getMessage(node);
    }
}