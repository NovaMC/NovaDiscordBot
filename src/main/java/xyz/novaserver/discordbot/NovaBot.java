package xyz.novaserver.discordbot;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.novaserver.discordbot.util.Config;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

public class NovaBot {
    public static final Logger LOGGER = LoggerFactory.getLogger(NovaBot.class);
    public static final Config CONFIG = new Config(NovaBot.class, new File(System.getProperty("user.dir"), "config.yml"), "config.yml");

    private static ScheduledExecutorService generalThreadPool;
    private static JDA jda;

    public static void main(String[] args) {

    }
}
