package xyz.novaserver.discordbot.services.roles.config;

import org.spongepowered.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.Map;

public class SubGuildRoles {
    private final long mainGuildID;
    private final Map<String, Long> roleMap = new HashMap<>();

    public SubGuildRoles(ConfigurationNode configNode) {
        mainGuildID = configNode.node("main-guild").getLong();
        configNode.node("role-map").childrenMap().values().forEach(node -> {
            roleMap.put((String) node.key(), node.getLong());
        });
    }

    public long getMainGuild() {
        return mainGuildID;
    }

    public Map<String, Long> getRoleMap() {
        return roleMap;
    }
}
