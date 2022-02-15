package xyz.novaserver.discordbot.services.roles.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.novaserver.discordbot.services.RolesService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleSyncHandler extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleSyncHandler.class);

    private final RolesService rolesService;

    public RoleSyncHandler(RolesService rolesService) {
        this.rolesService = rolesService;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Guild eventGuild = event.getGuild();
        User user = event.getUser();
        List<Guild> userGuilds = new ArrayList<>(user.getMutualGuilds());
        Set<String> hasRoleKeys = new HashSet<>();

        userGuilds.remove(eventGuild);
        userGuilds.forEach(userGuild -> rolesService.getSyncedRoles().forEach(syncedRoleSet -> {
            syncedRoleSet.getRoles().forEach(otherRoleID -> {
                Member otherMember = userGuild.getMember(event.getUser());
                Role otherRole = event.getJDA().getRoleById(otherRoleID);
                if (otherMember != null && otherMember.getRoles().contains(otherRole)) {
                    hasRoleKeys.add(syncedRoleSet.getRoleKey());
                }
            });
        }));

        rolesService.getSyncedRoles().forEach(syncedRoleSet -> {
            if (hasRoleKeys.contains(syncedRoleSet.getRoleKey())) {
                syncedRoleSet.getRoles().forEach(roleID -> {
                    Role role = event.getJDA().getRoleById(roleID);
                    if (role != null && eventGuild.getRoles().contains(role)) {
                        eventGuild.addRoleToMember(event.getMember(), role).queue();
                        LOGGER.info("(Join) Synced member's role across server! M:" + event.getMember().getEffectiveName()
                                + " R:" + role.getName() + " S:" + eventGuild.getName());
                    }
                });
            }
        });
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        User user = event.getUser();
        List<Guild> userGuilds = user.getMutualGuilds();

        event.getRoles().forEach(eventRole -> rolesService.getSyncedRoles().forEach(syncedRoleSet -> {
            List<Long> syncedRoleList = new ArrayList<>(syncedRoleSet.getRoles());
            if (syncedRoleList.contains(eventRole.getIdLong())) {
                syncedRoleList.remove(eventRole.getIdLong());
                syncedRoleList.forEach(otherRoleID -> {
                    Role otherRole = event.getJDA().getRoleById(otherRoleID);
                    Guild otherGuild = otherRole != null ? otherRole.getGuild() : null;
                    Member otherMember = otherGuild != null ? otherGuild.getMember(user) : null;
                    if (otherRole != null && otherMember != null && userGuilds.contains(otherGuild)) {
                        otherGuild.addRoleToMember(otherMember, otherRole).queue();
                        LOGGER.info("(Add) Synced member's role across server! M:" + otherMember.getEffectiveName()
                                + " R:" + otherRole.getName() + " S:" + otherGuild.getName());
                    }
                });
            }
        }));
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        User user = event.getUser();
        List<Guild> userGuilds = user.getMutualGuilds();

        event.getRoles().forEach(eventRole -> rolesService.getSyncedRoles().forEach(syncedRoleSet -> {
            List<Long> syncedRoleList = new ArrayList<>(syncedRoleSet.getRoles());
            if (syncedRoleList.contains(eventRole.getIdLong())) {
                syncedRoleList.remove(eventRole.getIdLong());
                syncedRoleList.forEach(otherRoleID -> {
                    Role otherRole = event.getJDA().getRoleById(otherRoleID);
                    Guild otherGuild = otherRole != null ? otherRole.getGuild() : null;
                    Member otherMember = otherGuild != null ? otherGuild.getMember(user) : null;
                    if (otherRole != null && otherMember != null && userGuilds.contains(otherGuild)) {
                        otherGuild.removeRoleFromMember(otherMember, otherRole).queue();
                        LOGGER.info("(Remove) Synced member's role across server! M:" + otherMember.getEffectiveName()
                                + " R:" + otherRole.getName() + " S:" + otherGuild.getName());
                    }
                });
            }
        }));
    }
}
