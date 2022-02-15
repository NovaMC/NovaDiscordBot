package xyz.novaserver.discordbot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import xyz.novaserver.discordbot.NovaBot;
import xyz.novaserver.discordbot.services.roles.config.GuildInfo;
import xyz.novaserver.discordbot.services.roles.config.SubGuildRoles;
import xyz.novaserver.discordbot.services.roles.config.SyncedRoleSet;
import xyz.novaserver.discordbot.services.roles.listener.CommonRolesHandler;
import xyz.novaserver.discordbot.services.roles.listener.RoleSyncHandler;
import xyz.novaserver.discordbot.services.roles.listener.SubGuildRolesHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RolesService extends Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(RolesService.class);

    private final Map<Long, GuildInfo> GUILD_INFO_MAP = new HashMap<>();
    private final Set<SyncedRoleSet> SYNCED_ROLES = new HashSet<>();
    private SubGuildRoles SUBGUILD_ROLES;

    public RolesService(NovaBot novaBot) {
        super(novaBot);
    }

    @Override
    public void register() {
        ConfigurationNode config = getNovaBot().getConfig();
        boolean errorOccured = false;

        // Load guilds from config
        for (ConfigurationNode node : config.node("guilds").childrenMap().values()) {
            try {
                GUILD_INFO_MAP.put(node.node("guild-id").getLong(), new GuildInfo(node));
            } catch (SerializationException e) {
                LOGGER.error("Failed to load guilds from config!", e);
            }
        }
        // Load synced role sets
        for (ConfigurationNode node : config.node("synced-roles").childrenMap().values()) {
            try {
                SYNCED_ROLES.add(new SyncedRoleSet(node));
            } catch (SerializationException e) {
                LOGGER.error("Failed to load synced roles!", e);
            }
        }

        SUBGUILD_ROLES = new SubGuildRoles(config.node("subguild-roles"));

        // Register handlers if no errors occurred while loading
        if (!errorOccured) {
            getNovaBot().getJda().addEventListener(
                    new CommonRolesHandler(this),
                    new RoleSyncHandler(this),
                    new SubGuildRolesHandler(this));
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
}
