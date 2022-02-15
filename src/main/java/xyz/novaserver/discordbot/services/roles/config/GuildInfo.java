package xyz.novaserver.discordbot.services.roles.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class GuildInfo {
    private final String guildKey;
    private final long guildID;
    private final List<CommonRoleSet> commonRoleSets = new ArrayList<>();

    public GuildInfo(ConfigurationNode configNode) throws SerializationException {
        guildKey = (String) configNode.key();
        guildID = configNode.node("guild-id").getLong();
        for (ConfigurationNode commonNode : configNode.node("common-roles").childrenMap().values()) {
            String key = (String) commonNode.key();
            commonRoleSets.add(new CommonRoleSet(key,
                    commonNode.node("roles-to-give").getList(Long.class),
                    commonNode.node("role-list").getList(Long.class)
            ));
        }
    }

    public String getGuildKey() {
        return guildKey;
    }

    public long getGuildID() {
        return guildID;
    }

    public List<CommonRoleSet> getCommonRoleSets() {
        return commonRoleSets;
    }

    public static class CommonRoleSet {
        private final String setName;
        private final List<Long> rolesToGive = new ArrayList<>();
        private final List<Long> roleList = new ArrayList<>();

        private CommonRoleSet(String setName, List<Long> rolesToGive, List<Long> roleList) {
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
