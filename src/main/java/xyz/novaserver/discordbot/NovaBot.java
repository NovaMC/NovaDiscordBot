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
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import xyz.novaserver.discordbot.services.Service;
import xyz.novaserver.discordbot.util.Config;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
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

    private final ConfigurationNode CONFIG;
    private final ScheduledExecutorService threadPool;
    private final JDA jda;

    private final Map<String, Service> serviceMap = new HashMap<>();

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

        // Login and register bot
        JDA tempJda;
        try {
            tempJda = JDABuilder.createDefault(getConfig().node("bot-token").getString())
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.playing("Starting up..."))
                    .addEventListeners(this)
                    .build();
        } catch (LoginException e) {
            tempJda = null;
            exitWithError("Failed to login to the bot. Did you change the bot token?", e);
        }
        this.jda = tempJda;

        // Load services
        try {
            Reflections reflections = new Reflections("xyz.novaserver.discordbot.services");
            Set<Class<? extends Service>> subTypes = reflections.getSubTypesOf(Service.class);
            for (Class<? extends Service> clazz : subTypes) {
                if (clazz.getSimpleName().equals("Service")) continue;
                String serviceName = clazz.getSimpleName().toLowerCase().replace("service", "");
                if (getConfig().node("enable-features", serviceName).getBoolean()) {
                    serviceMap.put(serviceName, clazz.getDeclaredConstructor(NovaBot.class).newInstance(this));
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            exitWithError("Failed to load services!", e);
        }

        // Register services
        serviceMap.values().forEach(Service::register);
        LOGGER.info("Registered " + serviceMap.size() + " service(s)!");
    }

    public ScheduledExecutorService getThreadPool() {
        return threadPool;
    }

    public JDA getJda() {
        return jda;
    }

    public ConfigurationNode getConfig() {
        return CONFIG;
    }

    public Service getService(String name) {
        return serviceMap.get(name);
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
            }, 5, 60 * 20, TimeUnit.SECONDS);
        }
    }

    private static void exitWithError(String s, Exception e) {
        LOGGER.error(s, e);
        System.exit(1);
    }
}











