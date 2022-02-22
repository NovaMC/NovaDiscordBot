package xyz.novaserver.discordbot.services;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.novaserver.discordbot.NovaBot;
import xyz.novaserver.discordbot.services.music.AudioHandler;
import xyz.novaserver.discordbot.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MusicService extends Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(MusicService.class);

    private final List<SlashCommand> commands = new ArrayList<>();
    private final AudioPlayerManager playerManager;
    private AudioPlayer player;
    private AudioHandler audioHandler;

    public MusicService(NovaBot novaBot) {
        super(novaBot);
        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.MEDIUM);
    }

    @Override
    public void register() {
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer();
        audioHandler = new AudioHandler(player);
        player.addListener(audioHandler);

        try {
            Set<Class<? extends SlashCommand>> subTypes = ReflectionUtil.getTypes("xyz.novaserver.discordbot.services.music.commands", SlashCommand.class);
            for (Class<? extends SlashCommand> clazz : subTypes) {
                commands.add(clazz.getDeclaredConstructor(MusicService.class).newInstance(this));
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LOGGER.error("Failed to load music commands!", e);
        }
    }
}
