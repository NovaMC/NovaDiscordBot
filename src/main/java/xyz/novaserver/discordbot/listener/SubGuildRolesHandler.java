package xyz.novaserver.discordbot.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.novaserver.discordbot.NovaBot;
import xyz.novaserver.discordbot.config.GuildInfo;
import xyz.novaserver.discordbot.config.SubGuildRoles;

import java.util.List;

public class SubGuildRolesHandler extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubGuildRolesHandler.class);

    private final NovaBot novaBot;
    private final SubGuildRoles subGuildRoles;
    private GuildInfo mainGuildInfo;

    public SubGuildRolesHandler(NovaBot novaBot) {
        this.novaBot = novaBot;
        this.subGuildRoles = novaBot.getSubGuildRoles();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Guild eventGuild = event.getGuild();
        Member member = event.getMember();
        List<Guild> userGuilds = event.getUser().getMutualGuilds();

        if (mainGuildInfo == null) mainGuildInfo = novaBot.getGuildInfo(subGuildRoles.getMainGuild());

        // Check if member is joining main guild
        if (eventGuild.getIdLong() == mainGuildInfo.getGuildID()) {
            // Loop through guild -> role map
            subGuildRoles.getRoleMap().keySet().forEach(subGuildKey -> {
                // Check if we have guild info for this guild
                if (novaBot.hasGuildInfo(subGuildKey)) {
                    // Grab guild with it's id
                    Guild subGuild = event.getJDA().getGuildById(novaBot.getGuildInfo(subGuildKey).getGuildID());
                    // Give user mapped role if they are in this sub guild
                    if (userGuilds.contains(subGuild)) {
                        Role subGuildRole = eventGuild.getRoleById(subGuildRoles.getRoleMap().get(subGuildKey));
                        if (subGuildRole != null) {
                            eventGuild.addRoleToMember(member, subGuildRole).queue();
                            LOGGER.info("Added subserver role to member because they joined main server! M:" + member.getEffectiveName()
                                    + " R:" + subGuildRole.getName() + " S:" + (subGuild != null ? subGuild.getName() : "null"));
                        }
                    }
                }
            });
        } else {
            Guild mainGuild = event.getJDA().getGuildById(mainGuildInfo.getGuildID());

            // Check if user is in main guild
            if (mainGuild != null && userGuilds.contains(mainGuild)) {
                // Loop through guild -> role map
                subGuildRoles.getRoleMap().keySet().forEach(subGuildKey -> {
                    // Check if we have guild info for this guild
                    if (novaBot.hasGuildInfo(subGuildKey)) {
                        // Give user mapped role for joining sub guild
                        if (eventGuild.getIdLong() == novaBot.getGuildInfo(subGuildKey).getGuildID()) {
                            Role subGuildRole = mainGuild.getRoleById(subGuildRoles.getRoleMap().get(subGuildKey));
                            Member mainMember = mainGuild.getMember(event.getUser());
                            if (subGuildRole != null && mainMember != null) {
                                mainGuild.addRoleToMember(mainMember, subGuildRole).queue();
                                LOGGER.info("Added role to member because they joined subserver! M:" + mainMember.getEffectiveName()
                                        + " R:" + subGuildRole.getName() + " S:" + subGuildKey);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild eventGuild = event.getGuild();
        Member member = event.getMember();
        List<Guild> userGuilds = event.getUser().getMutualGuilds();

        if (mainGuildInfo == null) mainGuildInfo = novaBot.getGuildInfo(subGuildRoles.getMainGuild());

        Guild mainGuild = event.getJDA().getGuildById(mainGuildInfo.getGuildID());

        // Check if user is in main guild
        if (mainGuild != null && userGuilds.contains(mainGuild)) {
            // Loop through guild -> role map
            subGuildRoles.getRoleMap().keySet().forEach(subGuildKey -> {
                // Check if we have guild info for this guild
                if (novaBot.hasGuildInfo(subGuildKey)) {
                    // Removed user mapped role for leaving sub guild
                    if (eventGuild.getIdLong() == novaBot.getGuildInfo(subGuildKey).getGuildID()) {
                        Role subGuildRole = mainGuild.getRoleById(subGuildRoles.getRoleMap().get(subGuildKey));
                        Member mainMember = mainGuild.getMember(event.getUser());
                        if (subGuildRole != null && mainMember != null) {
                            mainGuild.removeRoleFromMember(mainMember, subGuildRole).queue();
                            LOGGER.info("Removed role to member because they left subserver! M:" + mainMember.getEffectiveName()
                                    + " R:" + subGuildRole.getName() + " S:" + subGuildKey);
                        }
                    }
                }
            });
        }
    }
}
