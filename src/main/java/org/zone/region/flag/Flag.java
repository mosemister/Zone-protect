package org.zone.region.flag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.zone.region.Zone;
import org.zone.region.flag.meta.member.MembersFlag;
import org.zone.region.group.Group;
import org.zone.region.group.key.GroupKey;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * A modifier for a Zone. This is designed to hold data or modify the zone in one way or another
 */
public interface Flag {

    /**
     * If the flag can be enabled/disabled, then it should implement this to help other plugins to understand your flag
     */
    interface Enabled extends Flag {

        /**
         * Sets the flag enabled status
         *
         * @param enabled If the flag should be enabled or not. Setting this to null should make it the same as default
         */
        void setEnabled(@Nullable Boolean enabled);

        /**
         * Gets if the flag has been enabled
         *
         * @return If the flag has been enabled, {@link Optional#empty()} if default value should be used
         */
        Optional<Boolean> getEnabled();

        /**
         * Gets if the flag has been enabled
         *
         * @return If the flag has been enabled, if the value is null, the default is used instead
         */
        boolean isEnabled();

    }

    /**
     * If the flag has a group key override, then it should implement this to assist other plugins attempting to understand
     */
    interface GroupKeyed extends Flag {
        /**
         * Gets the required GroupKey
         *
         * @return The required GroupKey for overriding
         */
        @NotNull GroupKey getRequiredKey();

        /**
         * Checks if a player has permission to override this flag
         *
         * @param zone     the zone this flag is attached to
         * @param playerId The player ID
         * @return If the player has permission
         */
        default boolean hasPermission(@NotNull Zone zone, @NotNull UUID playerId) {
            return this.hasPermission(zone.getMembers(), playerId);
        }

        /**
         * Checks if a player has permission to override this flag
         *
         * @param flag     the MemberFlag this flag is part of
         * @param playerId the player ID
         * @return if the player has permission
         */
        default boolean hasPermission(@NotNull MembersFlag flag, @NotNull UUID playerId) {
            return this.hasPermission(flag.getGroup(playerId));
        }

        /**
         * Checks if a group has permission to override this flag
         *
         * @param group The group to compare
         * @return If the group has permission
         */
        default boolean hasPermission(@NotNull Group group) {
            return group.getAllKeys().contains(this.getRequiredKey());
        }
    }

    /**
     * Gets the attached FlagType to this flag
     *
     * @return The FlagType of this flag
     */
    @NotNull FlagType<?> getType();

    /**
     * serializes this flag to the provided ConfigurationNode
     *
     * @param node The node to serialize to
     * @param <T>  The class of this flag
     * @throws IOException if there is a issue saving
     */
    default <T extends Flag> void save(ConfigurationNode node) throws IOException {
        ((FlagType<T>) this.getType()).save(node, (T) this);
    }

}
