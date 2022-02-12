package xyz.novaserver.discordbot.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class SyncedRoleSet {
    private final String roleKey;
    private final List<Long> roles;

    public SyncedRoleSet(ConfigurationNode configNode) throws SerializationException {
        roleKey = (String) configNode.key();
        roles = configNode.getList(Long.class);
    }

    public String getRoleKey() {
        return roleKey;
    }

    public List<Long> getRoles() {
        return roles;
    }
}
