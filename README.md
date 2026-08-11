# RSLB (RSereneLoginBukkit)

正版（Mojang/Microsoft）与 LittleSkin 等多 Yggdrasil 认证服务登录插件。
支持 Paper 系核心（Paper / Folia）。

拦截 netty 登录管道，将原版会话验证替换为多服务认证：玩家使用哪个账号服务登录，
由数据库中的在线档案决定，一个服务器可同时承载正版玩家与 LittleSkin 玩家。

## 功能特性

- **多认证服务**：内置官方正版（`services/official.yml`）与 LittleSkin（`services/littleskin.yml`）服务，也可自行添加其他兼容 Yggdrasil 的 API。
- **无感知登录**：玩家照常用原版方式登录，插件在 netty 层接管 `hasJoined` 会话验证，无需客户端安装任何东西。
- **档案系统**：一个在线账号可创建多个游戏档案（`/rslb profile create`），登录时按数据库映射自动分配对应的游戏档案（详见下文"档案名分配与纠正"）。
- **档案名自动纠正**：登录时若档案名与他人冲突，自动追加数字（Steven → Steven1 → Steven2），不会让玩家卡在登录界面。
- **档案名自动回收**：在线账号在认证服务端（如正版）改名后，旧档案名自动释放给其他人注册（`auto-name-change`），防抢注。
- **服务白名单**：可按认证服务开启白名单（`whitelist: true`），仅允许指定账号进入，白名单外的账号在登录阶段即被拒绝。
- **指令 TAB 补全**：无权限的指令自动隐藏；参数位（在线玩家 / 档案）自动补全在线玩家名并支持前缀过滤，且补全同样受权限约束——无对应分支权限的参数不提供候选。
- **风险指令二次确认**：`/rslb confirm` 机制防止误操作。
- **数据持久化**：内置 H2 数据库，也可切换 MySQL（HikariCP 连接池）。
- **皮肤修复**：外置登录玩家的皮肤对正版客户端不可见时，可开启 `skinRestorer`——插件提取皮肤 URL/模型，
  通过 MineSkin v2 API 重新生成带正版签名的皮肤（支持 URL / 上传两种方式、PUBLIC / UNLISTED / PRIVATE 可见性、
  内置全局限速排队与自动重试），修复结果落库缓存，签名有效且域名白名单内的皮肤直接放行。
- **完整中文本地化**：全部消息集中在 `messages.yml`，支持 `&` 颜色码，可自由修改。
- **bStats 匿名统计**：可关闭（`settings.metrics-enabled`）。

## 环境要求

| 项目   | 要求                                                          |
|------|-------------------------------------------------------------|
| 服务端  | **26.2 的 Paper 系核心**：Paper 26.2 / Folia 26.2 及其分叉（如 Purpur） |
| Java | 运行时 Java 21+（构建需要 JDK 25）                                   |

> **`server.properties` 必配项**：
> - `online-mode=true`：插件在 netty 层接管会话验证，所有玩家必须经插件认证后才能进入游戏；
> - `enforce-secure-profile=false`：否则外置登录（如 LittleSkin）玩家会被服务端以"安全档案校验失败"强制踢出。

## 下载最新版（GitHub Actions 自动构建）

插件使用 GitHub Actions 在每次推送后自动构建，**最新构建产物在 Actions 中下载**：

1. 打开仓库页面 `https://github.com/Rain-Serenity/RSLB`；
2. 点击顶部 **Actions** 选项卡；
3. 左侧选择 **Auto Gradle Build** workflow；
4. 在最新一次**成功**（绿色 ✓）的 run 底部，找到 **Artifacts** 区域；
5. 点击 **RSLB Artifact** 下载 zip；
6. 解压得到 `RSLB-2.0-all.jar`，放入服务端 `plugins/` 文件夹。

> 提示：也可在代码仓库 Releases 页面查看是否有正式发布版。

## 安装与首次启动

1. 将 插件 放入 `plugins/`；
2. 启动服务器（或 `/reload confirm`）；
3. 插件自动生成配置与数据库文件：

```
plugins/RSLB/
├── config.yml          # 主配置
├── messages.yml        # 语言文件（可自定义消息）
├── services/           # 认证服务配置
│   ├── official.yml    # 正版服务
│   └── littleskin.yml  # LittleSkin 服务
├── RSLB.mv.db          # H2 数据库（默认后端）
└── bStats/             # bStats 匿名统计配置（全局）
```

> 若旧版本残留 `message.properties`，首次启动会自动删除并改用 `messages.yml`。
> 升级时若 `messages.yml` 缺少新键会自动补默认值；但**已存在的键不会被覆盖**，
> 如需恢复默认消息，请删除 `messages.yml` 后重启。

## 配置说明

### config.yml

```yaml
settings:
  debug: false                    # DEBUG 日志（登录拦截/认证请求详情）
  welcome-message: true           # 登录成功欢迎消息
  profile-key-verify: false       # 聊天会话公钥校验（默认关闭，丢弃会话更新包避免外置玩家被踢）
  name-allowed-regular: '^[0-9a-zA-Z_]{3,16}$'   # 档案名正则
  auto-correct-name: true         # 名字冲突时自动追加数字
  auto-name-change: true          # 档案改名后自动回收旧名
  confirm-command-valid-mills: 15000   # /rslb confirm 有效期
  link-accept-valid-mills: 30000       # /rslb link 接受请求有效期
  metrics-enabled: true           # bStats 匿名统计开关

database:
  backend: H2                     # H2 或 MYSQL
  host: 127.0.0.1                 # MYSQL 地址
  port: 3306                      # MYSQL 端口
  username: root                  # MYSQL 用户
  password: root                  # MYSQL 密码
  database: rslb                 # MYSQL 库名
  table-prefix: rslb              # 表前缀（勿随意更改，否则数据丢失）
  connect-url: ''                 # 自定义 JDBC URL（留空自动拼接）
```

### services/*.yml（认证服务）

每个服务文件定义一种认证来源，`id` 全局唯一（0-127），以下键均可选，省略时使用默认值：

```yaml
id: 0                          # 服务 ID（0-127，全局唯一）
name: '正版登录'                 # 服务显示名
initNameFormat: '{name}'       # 初始档案名格式（{name} 替换为登录名，空格转 _）
initUUID: DEFAULT              # 初始 UUID 策略：DEFAULT / OFFLINE / RANDOM
serviceType: OFFICIAL          # OFFICIAL / LITTLESKIN
skinRestorer:                  # 皮肤修复（默认关闭；外置登录皮肤对正版客户端不可见时开启）
  restorer: 'OFF'              # OFF / LOGIN（皮肤修复时机）
  method: URL                  # URL / UPLOAD（皮肤获取方式）
  timeout: 10000               # 皮肤请求超时（毫秒）
  retry: 2                     # 皮肤请求失败重试次数
  retryDelay: 5000             # 重试间隔（毫秒）
  mineskinApiKey: ''           # MineSkin API Key（留空为匿名额度）
  visibility: PUBLIC           # 生成的皮肤在图库的可见性：PUBLIC / UNLISTED / PRIVATE
  proxy:                       # 皮肤请求代理
    type: DIRECT               # DIRECT / HTTP / SOCKS
    hostname: '127.0.0.1'
    port: 1080
    username: ''               # 留空表示无需认证
    password: ''
whitelist: false               # 开启后仅白名单账号可登录（/rslb whitelist）
yggdrasilAuth:
  authProxy:                   # 会话验证请求代理（默认直连，如无法直连外置服可配置）
    type: DIRECT               # DIRECT / HTTP / SOCKS
    hostname: '127.0.0.1'
    port: 1080
    username: ''
    password: ''
  # official / littleSkin: 正版/外置服专用子块，见下方说明
  retry: 1                     # 验证失败重试次数
  retryDelay: 300              # 重试间隔（毫秒）
  timeout: 10000               # 验证请求超时（毫秒）
  trackIp: false               # 是否向会话服务器提交 IP
```

正版服务额外可自定义 `yggdrasilAuth.official.sessionServer`（默认为 Mojang 官方地址），
LittleSkin 服务可自定义 `yggdrasilAuth.littleSkin.apiRoot`。

## 指令用法

所有指令通过 `/rslb` 调用，输入 `/rslb help` 查看有权限使用的指令列表。
除 `/rslb help`、`/rslb confirm`、`/rslb link` 外，其余指令**默认仅 OP 可用**；
TAB 补全会自动隐藏当前玩家没有权限的指令，指令名后接参数的位置会补全**在线玩家名**
（如 `/rslb link to`、`/rslb info`、`/rslb rename <新名> <档案>` 的档案位、`/rslb profile set <档案>` 等）。
纯文本参数位（如新档案名、验证码）不提供候选，直接手输即可。
参数补全与指令隐藏走同一套权限判定：例如没有 `rslb.rename.other` 权限时，
`/rslb rename` 的档案位不会给出任何候选。

### 指令表

| 指令                                                  | 所需权限                                                             | 默认  | 说明                                             |
|-----------------------------------------------------|------------------------------------------------------------------|-----|------------------------------------------------|
| `/rslb confirm`                                     | `rslb.confirm`                                                   | 所有人 | 确认待执行的风险指令                                     |
| `/rslb eraseAllUsernames`                           | `rslb.erase.all`                                                 | op  | 回收所有档案名（需确认）                                   |
| `/rslb eraseUsername <名字>`                          | `rslb.erase.username`                                            | op  | 回收指定档案名（需确认）                                   |
| `/rslb find online <在线账号>`                          | `rslb.find.online`                                               | op  | 检索在线账号                                         |
| `/rslb find profile <档案名/UUID>`                     | `rslb.find.profile`                                              | op  | 检索游戏档案                                         |
| `/rslb help`                                        | `rslb.base`                                                      | 所有人 | 帮助列表（只列有权限的指令）                                 |
| `/rslb info`                                        | `rslb.info.oneself`                                              | op  | 查询自己的登录档案信息                                    |
| `/rslb info <在线玩家>`                                 | `rslb.info.other`                                                | op  | 查询指定在线玩家的档案信息                                  |
| `/rslb link accept`                                 | `rslb.link.accept`                                               | 所有人 | 接受迁移请求                                         |
| `/rslb link code <验证码>`                             | `rslb.link.code`                                                 | 所有人 | 用验证码确认迁移                                       |
| `/rslb link to <在线玩家>`                              | `rslb.link.to`                                                   | 所有人 | 请求迁移账号（如正版→LittleSkin）                         |
| `/rslb list`                                        | `rslb.list`                                                      | op  | 按服务分组列出在线玩家                                    |
| `/rslb profile create <用户名> [uuid]`                 | `rslb.profile.create`                                            | op  | 创建档案                                           |
| `/rslb profile remove <档案>`                         | `rslb.profile.remove`                                            | op  | 删除档案                                           |
| `/rslb profile set <档案>`                            | `rslb.profile.set.oneself`                                       | op  | 切换自己的档案                                        |
| `/rslb profile set <档案> <在线玩家>`                     | `rslb.profile.set.other`                                         | op  | 切换他人档案                                         |
| `/rslb reload`                                      | `rslb.reload`                                                    | op  | 重载配置与语言文件                                      |
| `/rslb rename <新名>`                                 | `rslb.rename.oneself`                                            | op  | 修改自己的档案名                                       |
| `/rslb rename <新名> <档案>`                            | `rslb.rename.other`                                              | op  | 修改指定档案的名字                                      |
| `/rslb whitelist add <档案名>`                         | `rslb.whitelist.add`                                             | op  | 添加白名单                                          |
| `/rslb whitelist list [-verbose]`                   | `rslb.whitelist.list`                                            | op  | 列出白名单（verbose 需 `rslb.whitelist.list.verbose`） |
| `/rslb whitelist remove <档案名>`                      | `rslb.whitelist.remove`                                          | op  | 移除白名单（在线玩家会被踢出）                                |
| `/rslb whitelist specific add/remove <服务ID> <在线账号>` | `rslb.whitelist.specific.add` / `rslb.whitelist.specific.remove` | op  | 按服务操作白名单                                       |

### 权限列表

| 权限                               | 对应指令                              | 默认  |
|----------------------------------|-----------------------------------|-----|
| `rslb.base`                      | `/rslb help`（基础入口）                | 所有人 |
| `rslb.tab.complete`              | TAB 补全总开关                         | 所有人 |
| `rslb.confirm`                   | `/rslb confirm`                   | 所有人 |
| `rslb.link.to`                   | `/rslb link to`                   | 所有人 |
| `rslb.link.accept`               | `/rslb link accept`               | 所有人 |
| `rslb.link.code`                 | `/rslb link code`                 | 所有人 |
| `rslb.reload`                    | `/rslb reload`                    | op  |
| `rslb.erase.username`            | `/rslb eraseUsername`             | op  |
| `rslb.erase.all`                 | `/rslb eraseAllUsernames`         | op  |
| `rslb.list`                      | `/rslb list`                      | op  |
| `rslb.whitelist.add`             | `/rslb whitelist add`             | op  |
| `rslb.whitelist.remove`          | `/rslb whitelist remove`          | op  |
| `rslb.whitelist.specific.add`    | `/rslb whitelist specific add`    | op  |
| `rslb.whitelist.specific.remove` | `/rslb whitelist specific remove` | op  |
| `rslb.whitelist.list`            | `/rslb whitelist list`            | op  |
| `rslb.whitelist.list.verbose`    | `/rslb whitelist list -verbose`   | op  |
| `rslb.rename.oneself`            | `/rslb rename <新名>`               | op  |
| `rslb.rename.other`              | `/rslb rename <新名> <档案>`          | op  |
| `rslb.info.oneself`              | `/rslb info`                      | op  |
| `rslb.info.other`                | `/rslb info <在线玩家>`               | op  |
| `rslb.profile.create`            | `/rslb profile create`            | op  |
| `rslb.profile.set.oneself`       | `/rslb profile set <档案>`          | op  |
| `rslb.profile.set.other`         | `/rslb profile set <档案> <在线玩家>`   | op  |
| `rslb.profile.remove`            | `/rslb profile remove`            | op  |
| `rslb.find.online`               | `/rslb find online`               | op  |
| `rslb.find.profile`              | `/rslb find profile`              | op  |

### 白名单使用示例

让"仅 LittleSkin 白名单账号可进入"：

1. `services/littleskin.yml` 中设置 `whitelist: true`；
2. `/rslb whitelist add 玩家名` 或 `/rslb whitelist specific add 1 玩家名`（1 = LittleSkin 服务 id）。

未在白名单的账号登录时会被拒绝并提示。

### 档案名分配与纠正（登录时自动发生）

玩家每次通过认证后，插件会执行"档案分配"流程（`AssignInGameFlows`），决定他这次进服用什么游戏内名字：

**首次登录（分配档案）**

1. 用账号的在线 UUID + 服务 id 查询数据库映射，没有记录则视为新账号；
2. 按该服务配置的 `initUUID` 策略生成初始档案 UUID；若与其他档案冲突（UUID 碰撞）则自动换成随机 UUID；
3. 按服务配置的 `initNameFormat` 生成初始档案名（默认 `{name}` = 账号名，如正版名为 `Steven` 则档案名也是 `Steven`）；
4. 写入数据库：`userdata`（在线账号 ↔ 游戏档案映射）+ `ingameprofile`（档案 UUID ↔ 档案名）。

**档案名已被占用（自动纠正）**

- 若目标档案名已被**其他**档案占用（忽略大小写），自动递增改名：`Steven` → `Steven1` → `Steven2` ……
- 玩家进服 2 秒后会收到提示："您的名字已被占用，已改为 xxx"；
- 若最终的名字仍与他人冲突（数据库唯一约束），登录被拒绝并提示，不会出现两个同名玩家。

**正版账号改名（自动回收旧名）**

- 正版玩家在 Mojang 改名后（如 `Steven` → `Steve`），登录时检测到名字已更新，
  自动把旧档案名 `Steven` 从档案表中**释放**，其他人即可注册/纠正到该名字；
- 旧名释放后玩家继续用新名 `Steve` 登录，档案 UUID 不变，皮肤、数据不受影响。

**已有档案（保持不变）**

- 老玩家登录时直接沿用数据库中的档案 UUID 与当前档案名，不会重复分配或重置。

**有什么用？**

- 正版/LittleSkin 玩家同服共存且互不抢名（两边 UUID 空间不同）；
- 玩家在认证服务端改名后无需管理员干预，服务器内名字自动跟随，且旧名不会被抢注者占用（先回收后释放）；
- 名字冲突时玩家仍能正常进服，而不是被卡在登录界面。

### 二次确认示例

```text
/rslb eraseUsername 某玩家名
→ 插件提示该指令风险，并给出确认提示
/rslb confirm        # 15 秒内执行才生效
```

## 构建

需要 **JDK 25**。

```bash
# Windows
.\gradlew.bat shadowJar

# Linux / macOS
chmod +x ./gradlew && ./gradlew shadowJar
```

产物：`build/libs/RSLB-2.0-all.jar`（含全部依赖的 fat jar）。

> 注意：`com.mojang:authlib` 与 `io.netty:netty-all` 必须与服务器自带版本一致，
> 已在 `build.gradle.kts` 中锁定，**请勿升级**，否则登录拦截可能崩溃。

## 实现原理

```
玩家登录请求
   ↓
netty 管道（LoginHandler 注入）
   ↓
从登录包提取账号 UUID → 查数据库匹配认证服务（official / littleskin）
   ↓
调用对应 Yggdrasil API（hasJoined，带 serverId 签名）
   ├─ 正版    → https://sessionserver.mojang.com
   └─ LittleSkin → https://littleskin.cn/api/yggdrasil
   ↓
验证流程（ValidateFlows 链）
   ├─ WhitelistCheckFlows  服务白名单检查
   ├─ NameAllowedRegularCheckFlows 档案名正则
   ├─ AssignInGameFlows    分配/纠正档案（含旧名回收、防抢注）
   └─ SkinRestorer         皮肤恢复（可选）
   ↓
通过 → 移交 vanilla 完成登录
拒绝 → 带原因踢出
```

- **登录拦截**：`LoginHandler` 包装服务端 netty acceptor 与管道，在 vanilla 会话验证之前截获登录流程，使所有玩家必须经插件认证。拦截器初始化失败不影响插件加载，但所有登录会退回原版流程。
- **认证服务**：`services/*.yml` 定义服务类型与 API 地址，`PluginConfig` 读取并校验 id 唯一性与服务重复。
- **数据库**：默认 H2 单文件库（`rslb.mv.db`），支持 MySQL。表结构含用户数据（`userdata`）、游戏档案（`ingameprofile`）、皮肤缓存等，表名带 `table-prefix` 前缀。
- **指令系统**：核心使用 brigadier 命令树（权限过滤、参数解析、TAB 补全），Bukkit 层桥接 `/rslb`——
  指令名的补全与输入校验由 Bukkit 层的静态指令树完成，参数位置委托核心建议器补全在线玩家名。
- **语言系统**：`messages.yml` 键值 + `{name}` 占位符替换，缺失键自动回填默认值。
- **统计**：bStats 匿名上报，开关 `settings.metrics-enabled`。

## 常见问题

- **TAB 补全不显示指令**：无权限的指令会被隐藏；若完全无补全，检查 `rslb.tab.complete` 权限（默认所有人）。
- **踢出消息显示 `&` 原文**：请确认使用最新构建。
- **升级后配置不生效**：旧 `config.yml` 结构变更后不会被自动覆盖，请对照模板手动更新或删除重建。
- **登录全部失败**：检查 `services/` 下至少存在一个有效服务，且服务 `id` 不重复。

## 许可
本插件基于 [MultiLogin](https://GitHub.com/CaaMoe/MultiLogin) 二次开发，因此继承上游 [GPL-3.0](LICENSE) 开源协议。  
本插件皮肤修复功能部分参照 [SkinsRestorer](https://GitHub.com/SkinsRestorer/SkinsRestorer/) 。
