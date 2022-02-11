package xyz.novaserver.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import xyz.novaserver.discordbot.config.GuildInfo;
import xyz.novaserver.discordbot.listener.CommonRoleHandler;
import xyz.novaserver.discordbot.listener.RoleSyncHandler;
import xyz.novaserver.discordbot.config.Config;
import xyz.novaserver.discordbot.listener.SubServerRolesHandler;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class NovaBot {
    public static final Logger LOGGER = LoggerFactory.getLogger(NovaBot.class);
    public static ConfigurationNode CONFIG;

    public static final Map<Long, GuildInfo> GUILD_INFO_MAP = new HashMap<>();

    private static ScheduledExecutorService generalThreadPool;
    private static JDA jda;

    public static void main(String[] args) {
        generalThreadPool = Executors.newScheduledThreadPool(3);

        // Create and load configuration file
        try {
            CONFIG = Config.getConfig(NovaBot.class, new File(System.getProperty("user.dir"), "config.yml"), "config.yml").getRoot();
        } catch (NullPointerException e) {
            exitWithError("Failed to load config! Make sure you have write permissions.", e);
        }

        // Login and register bot
        try {
            jda = JDABuilder.createDefault(CONFIG.node("bot-token").getString())
                    .setActivity(Activity.watching("Nova Network"))
                    .addEventListeners(
                        new RoleSyncHandler(),
                        new SubServerRolesHandler(),
                        new CommonRoleHandler())
                    .build();
        } catch (LoginException e) {
            exitWithError("Failed to login to the bot. Did you change the bot token?", e);
        }

        // Load guilds from config
        CONFIG.node("guilds").childrenMap().values().forEach(node -> {
            long guildID = node.node("guild-id").getLong();
            try {
                GuildInfo guildInfo = new GuildInfo(node);
                GUILD_INFO_MAP.put(guildID, guildInfo);
            } catch (SerializationException e) {
                exitWithError("Failed to load guilds from config!", e);
            }
        });
    }

    public static void exitWithError(String s, Exception e) {
        LOGGER.error(s, e);
        System.exit(1);
    }
}











