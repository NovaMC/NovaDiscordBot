package xyz.novaserver.discordbot.services.music.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import xyz.novaserver.discordbot.services.MusicService;

import java.util.Collections;

public class PlayCommand extends SlashCommand {
    private final MusicService service;

    public PlayCommand(MusicService service) {
        this.service = service;
        this.name = "play";
        this.help = "Plays a song or playlist from a link";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "url", "The link to play").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping option = event.getOption("url");
        String url = option == null ? null : option.getAsString();

        event.reply("Playing " + url).queue();
    }
}
