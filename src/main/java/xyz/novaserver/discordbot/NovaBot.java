package xyz.novaserver.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import xyz.novaserver.discordbot.config.Config;
import xyz.novaserver.discordbot.config.GuildInfo;
import xyz.novaserver.discordbot.config.SubGuildRoles;
import xyz.novaserver.discordbot.config.SyncedRoleSet;
import xyz.novaserver.discordbot.listener.CommonRolesHandler;
import xyz.novaserver.discordbot.listener.RoleSyncHandler;
import xyz.novaserver.discordbot.listener.SubGuildRolesHandler;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NovaBot implements EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NovaBot.class);

    private final Map<Long, GuildInfo> GUILD_INFO_MAP = new HashMap<>();
    private final Set<SyncedRoleSet> SYNCED_ROLES = new HashSet<>();
    private final SubGuildRoles SUBGUILD_ROLES;

    private final ConfigurationNode CONFIG;
    private final ScheduledExecutorService threadPool;
    private JDA jda;

    public static void main(String[] args) {
        // Create and load configuration file
        try {
            ConfigurationNode config = Config.getConfig(NovaBot.class, new File(System.getProperty("user.dir"), "config.yml"), "config.yml").getRoot();
            new NovaBot(config);
        } catch (NullPointerException e) {
            exitWithError("Failed to load config! Make sure you have write permissions.", e);
        }
    }

    public NovaBot(ConfigurationNode config) {
        this.CONFIG = config;
        this.threadPool = Executors.newScheduledThreadPool(5);

        // Load guilds from config
        getConfig().node("guilds").childrenMap().values().forEach(node -> {
            try {
                GUILD_INFO_MAP.put(node.node("guild-id").getLong(), new GuildInfo(node));
            } catch (SerializationException e) {
                exitWithError("Failed to load guilds from config!", e);
            }
        });

        getConfig().node("synced-roles").childrenMap().values().forEach(node -> {
            try {
                SYNCED_ROLES.add(new SyncedRoleSet(node));
            } catch (SerializationException e) {
                LOGGER.error("Failed to load synced roles!", e);
            }
        });

        SUBGUILD_ROLES = new SubGuildRoles(getConfig().node("subguild-roles"));

        // Login and register bot
        try {
            jda = JDABuilder.createDefault(getConfig().node("bot-token").getString())
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.playing("Starting up..."))
                    .addEventListeners(this,
                            new CommonRolesHandler(this),
                            new RoleSyncHandler(this),
                            new SubGuildRolesHandler(this))
                    .build();
        } catch (LoginException e) {
            exitWithError("Failed to login to the bot. Did you change the bot token?", e);
        }
    }

    public boolean hasGuildInfo(Long guildID) {
        return GUILD_INFO_MAP.containsKey(guildID);
    }

    public GuildInfo getGuildInfo(Long guildID) {
        return GUILD_INFO_MAP.get(guildID);
    }

    public boolean hasGuildInfo(String guildKey) {
        for (GuildInfo guildInfo : GUILD_INFO_MAP.values()) {
            if (guildKey.equals(guildInfo.getGuildKey())) {
                return true;
            }
        }
        return false;
    }

    public GuildInfo getGuildInfo(String guildKey) {
        for (GuildInfo guildInfo : GUILD_INFO_MAP.values()) {
            if (guildKey.equals(guildInfo.getGuildKey())) {
                return guildInfo;
            }
        }
        return null;
    }

    public Set<SyncedRoleSet> getSyncedRoles() {
        return SYNCED_ROLES;
    }

    public SubGuildRoles getSubGuildRoles() {
        return SUBGUILD_ROLES;
    }

    public ConfigurationNode getConfig() {
        return CONFIG;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching("Nova Network"));

            threadPool.scheduleAtFixedRate(() -> {
                AtomicInteger totalCount = new AtomicInteger();
                Set<Long> users = new HashSet<>();

                jda.getGuilds().forEach(guild -> {
                    totalCount.addAndGet(guild.getMemberCount());
                    guild.getMembers().forEach(member -> users.add(member.getUser().getIdLong()));
                });

                jda.getPresence().setActivity(Activity.watching("Nova Network " + users.size() + "/" + totalCount));
            }, 5, 60 * 5, TimeUnit.SECONDS);
        }
    }

    public static void exitWithError(String s, Exception e) {
        LOGGER.error(s, e);
        System.exit(1);
    }
}











