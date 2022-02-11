package xyz.novaserver.discordbot.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;

public class GuildInfo {
    private final String guildKey;
    private final long guildID;
    private final Map<String, CommonRoles> commonRolesMap = new HashMap<>();

    public GuildInfo(ConfigurationNode configNode) throws SerializationException {
        guildKey = (String) configNode.key();
        guildID = configNode.node("guild-id").getLong();
        for (ConfigurationNode commonNode : configNode.node("common-roles").childrenMap().values()) {
            String key = (String) commonNode.key();
            commonRolesMap.put(key, new CommonRoles(key,
                    commonNode.node("roles-to-give").getList(Long.class),
                    commonNode.node("role-list").getList(Long.class)
            ));
        }
    }

    public long getGuildID() {
        return guildID;
    }

    public Map<String, CommonRoles> getCommonRoles() {
        return commonRolesMap;
    }

    public static class CommonRoles {
        private final String setName;
        private final List<Long> rolesToGive = new ArrayList<>();
        private final List<Long> roleList = new ArrayList<>();

        private CommonRoles(String setName, List<Long> rolesToGive, List<Long> roleList) {
            this.setName = setName;
            this.rolesToGive.addAll(rolesToGive);
            this.roleList.addAll(roleList);
        }

        public String getSetName() {
            return setName;
        }

        public List<Long> getRolesToGive() {
            return rolesToGive;
        }

        public List<Long> getRoleList() {
            return roleList;
        }
    }
}
