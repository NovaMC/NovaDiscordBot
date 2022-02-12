package xyz.novaserver.discordbot.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.novaserver.discordbot.NovaBot;
import xyz.novaserver.discordbot.config.GuildInfo;

public class CommonRolesHandler extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRolesHandler.class);

    private final NovaBot novaBot;

    public CommonRolesHandler(NovaBot novaBot) {
        this.novaBot = novaBot;
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        Guild guild = event.getGuild();

        // Return if guild info doesn't exist
        if (!novaBot.hasGuildInfo(guild.getIdLong())) {
            return;
        }
        GuildInfo guildInfo = novaBot.getGuildInfo(guild.getIdLong());

        event.getRoles().forEach(eventRole -> {
            // Loop through common role groups
            guildInfo.getCommonRoleSets().forEach(roleSet -> {
                // Check if role given from event is in the role list of the set
                if (roleSet.getRoleList().contains(eventRole.getIdLong())) {
                    // Give member each role from the roles to give
                    roleSet.getRolesToGive().forEach(givenRoleID -> {
                        Role givenRole = guild.getRoleById(givenRoleID);
                        if (givenRole != null) {
                            guild.addRoleToMember(event.getMember(), givenRole).queue();
                            LOGGER.info("Added role to member because of common role! M:" + event.getMember().getEffectiveName()
                                    + " R:" + givenRole.getName());
                        }
                    });
                }
            });
        });
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        // Return if guild info doesn't exist
        if (!novaBot.hasGuildInfo(guild.getIdLong())) {
            return;
        }
        GuildInfo guildInfo = novaBot.getGuildInfo(guild.getIdLong());

        event.getRoles().forEach(eventRole -> {
            long eventRoleID = eventRole.getIdLong();
            // Loop through common role groups
            guildInfo.getCommonRoleSets().forEach(roleSet -> {
                // Check if role being removed is part of roles to give
                if (roleSet.getRolesToGive().contains(eventRoleID)) {
                    // Loop through role list
                    roleSet.getRoleList().forEach(roleListID -> {
                        // If member has a role from the list give them back the removed role
                        Role roleFromList = guild.getRoleById(roleListID);
                        if (roleFromList != null && member.getRoles().contains(roleFromList)) {
                            guild.addRoleToMember(member, eventRole).queue();
                            LOGGER.info("Added role to member because of removed common role! M:" + event.getMember().getEffectiveName()
                                    + " R:" + eventRole.getName() + "LR:" + roleFromList.getName());
                        }
                    });
                }
                // Check if role given from event is in the role list of the set
                if (roleSet.getRoleList().contains(eventRoleID)) {
                    // Give member each role from the roles to give
                    roleSet.getRolesToGive().forEach(givenRoleID -> {
                        Role givenRole = guild.getRoleById(givenRoleID);
                        if (givenRole != null) {
                            guild.removeRoleFromMember(member, givenRole).queue();
                            LOGGER.info("Removed role to member because of common role! M:" + event.getMember().getEffectiveName()
                                    + " R:" + givenRole.getName());
                        }
                    });
                }
            });
        });
    }
}
