package org.zone;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.zone.commands.structure.ZoneCommands;
import org.zone.misc.Messages;
import org.zone.event.listener.PlayerListener;
import org.zone.memory.MemoryHolder;
import org.zone.region.Zone;
import org.zone.region.ZoneManager;
import org.zone.region.flag.FlagManager;
import org.zone.region.flag.interact.block.destroy.BlockBreakListener;
import org.zone.region.flag.interact.door.DoorInteractListener;
import org.zone.region.flag.interact.itemframe.ItemFrameInteractionListener;
import org.zone.region.flag.move.monster.MonsterPreventionListener;
import org.zone.region.flag.move.player.preventing.PreventPlayersListener;
import org.zone.region.flag.move.player.greetings.GreetingsFlagListener;
import org.zone.region.flag.move.player.leaving.LeavingFlagListener;
import org.zone.region.group.key.GroupKeyManager;

import java.io.File;

/**
 * The zone plugin's boot and main class, use {@link ZonePlugin#getZonesPlugin()} to gain an
 * instance of this class
 */
@Plugin("zones")
public class ZonePlugin {

    private final PluginContainer plugin;
    private final Logger logger;
    private FlagManager flagManager;
    private ZoneManager zoneManager;
    private GroupKeyManager groupKeyManager;
    private MemoryHolder memoryHolder;
    private static ZonePlugin zonePlugin;

    @SuppressWarnings("SpongeInjection")
    @Inject
    public ZonePlugin(final PluginContainer plugin, final Logger logger) {
        zonePlugin = this;
        this.plugin = plugin;
        this.logger = logger;
    }

    /**
     * Gets the flag manager
     *
     * @return The instance of the flag manager
     */
    public @NotNull FlagManager getFlagManager() {
        return this.flagManager;
    }

    /**
     * Gets the zone manager
     *
     * @return The instance of the zone manager
     */
    public @NotNull ZoneManager getZoneManager() {
        return this.zoneManager;
    }

    /**
     * Gets the Memory holder
     *
     * @return the instance of the memory holder
     */
    public @NotNull MemoryHolder getMemoryHolder() {
        return this.memoryHolder;
    }

    /**
     * Gets the Group key manager
     *
     * @return The instance of the group key manager
     */
    public @NotNull GroupKeyManager getGroupKeyManager() {
        return this.groupKeyManager;
    }

    /**
     * Gets the logger for this plugin (Oh no! log4j!)
     *
     * @return The logger for this plugin
     */
    public @NotNull Logger getLogger() {
        return this.logger;
    }

    @Listener
    public void onConstructor(ConstructPluginEvent event) {
        this.flagManager = new FlagManager();
        this.zoneManager = new ZoneManager();
        this.groupKeyManager = new GroupKeyManager();
        this.memoryHolder = new MemoryHolder();
    }

    private void registerListeners() {
        Sponge.eventManager().registerListeners(this.plugin, new PlayerListener());
        Sponge.eventManager().registerListeners(this.plugin, new MonsterPreventionListener());
        Sponge.eventManager().registerListeners(this.plugin, new DoorInteractListener());
        Sponge.eventManager().registerListeners(this.plugin, new BlockBreakListener());
        Sponge.eventManager().registerListeners(this.plugin, new GreetingsFlagListener());
        Sponge.eventManager().registerListeners(this.plugin, new PreventPlayersListener());
        Sponge.eventManager().registerListeners(this.plugin, new LeavingFlagListener());
        Sponge.eventManager().registerListeners(this.plugin, new ItemFrameInteractionListener());
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        this.registerListeners();
    }

    @Listener
    public void onServerStarted(final StartedEngineEvent<Server> event) {
        Sponge
                .systemSubject()
                .sendMessage(Messages.getLoadingZonesStart());
        File zonesFolder = new File("config/zone/zones/");
        Sponge
                .systemSubject()
                .sendMessage(Messages.getZonesLoadingfrom(zonesFolder.getPath()));

        for (PluginContainer container : Sponge.pluginManager().plugins()) {
            File keyFolder = new File(zonesFolder, container.metadata().id());
            File[] keyFiles = keyFolder.listFiles();
            if (keyFiles == null) {
                continue;
            }
            for (File file : keyFiles) {
                try {
                    Zone zone = this.zoneManager.load(file);
                    this.zoneManager.register(zone);
                } catch (ConfigurateException e) {
                    Sponge
                            .systemSubject()
                            .sendMessage(Messages.getZonesLoadingFail(file.getPath()));
                    e.printStackTrace();
                }
            }
        }
        Sponge
                .systemSubject()
                .sendMessage(Messages.getZonesLoaded(this.getZoneManager().getZones()));
    }

    @Listener
    public void onRegisterCommands(@SuppressWarnings("BoundedWildcard") final RegisterCommandEvent<Command.Raw> event) {
        event.register(this.plugin,
                       ZoneCommands.createCommand(),
                       "zone",
                       "region",
                       "claim",
                       "protect");
    }

    /**
     * Gets the PluginContainer for this plugin
     *
     * @return The plugin container for this plugin
     */
    public @NotNull PluginContainer getPluginContainer() {
        return this.plugin;
    }

    /**
     * Gets the instance of this class
     *
     * @return The instance of this class
     */
    public static @NotNull ZonePlugin getZonesPlugin() {
        return zonePlugin;
    }
}
