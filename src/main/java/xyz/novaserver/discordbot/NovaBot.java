package xyz.novaserver.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import xyz.novaserver.discordbot.util.Config;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

public class NovaBot {
    public static final Logger LOGGER = LoggerFactory.getLogger(NovaBot.class);
    public static ConfigurationNode CONFIG;

    private static ScheduledExecutorService generalThreadPool;
    private static JDA jda;

    public static void main(String[] args) {
        try {
            CONFIG = Config.getConfig(NovaBot.class, new File(System.getProperty("user.dir"), "config.yml"), "config.yml").getRoot();
            jda = JDABuilder.createDefault(CONFIG.node("bot-token").getString()).build();
        } catch (NullPointerException e) {
            LOGGER.error("Failed to load config! Make sure you have write permissions.", e);
            System.exit(1);
        } catch (LoginException e) {
            LOGGER.error("Failed to login to the bot. Did you change the bot token?", e);
            System.exit(1);
        }



    }
}











