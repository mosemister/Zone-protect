package org.zone;

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.zone.ai.HumanAIListener;
import org.zone.annotations.Typed;
import org.zone.commands.structure.ZoneCommands;
import org.zone.config.ZoneConfig;
import org.zone.event.listener.PlayerListener;
import org.zone.keys.ZoneKeys;
import org.zone.memory.MemoryHolder;
import org.zone.region.Zone;
import org.zone.region.ZoneManager;
import org.zone.region.flag.Flag;
import org.zone.region.flag.FlagManager;
import org.zone.region.flag.FlagType;
import org.zone.region.flag.entity.monster.block.explode.creeper.CreeperGriefListener;
import org.zone.region.flag.entity.monster.block.explode.enderdragon.EnderDragonGriefListener;
import org.zone.region.flag.entity.monster.block.explode.wither.WitherGriefListener;
import org.zone.region.flag.entity.monster.block.hatch.EnderMiteGriefListener;
import org.zone.region.flag.entity.monster.block.ignite.SkeletonGriefListener;
import org.zone.region.flag.entity.monster.block.knock.ZombieGriefListener;
import org.zone.region.flag.entity.monster.block.take.EnderManGriefListener;
import org.zone.region.flag.entity.monster.move.MonsterPreventionListener;
import org.zone.region.flag.entity.nonliving.block.farmland.FarmTramplingListener;
import org.zone.region.flag.entity.nonliving.block.tnt.TnTDefuseListener;
import org.zone.region.flag.entity.player.damage.attack.EntityDamagePlayerListener;
import org.zone.region.flag.entity.player.damage.fall.PlayerFallDamageListener;
import org.zone.region.flag.entity.player.interact.block.destroy.BlockBreakListener;
import org.zone.region.flag.entity.player.interact.block.place.BlockPlaceListener;
import org.zone.region.flag.entity.player.interact.door.DoorInteractListener;
import org.zone.region.flag.entity.player.interact.itemframe.ItemFrameInteractionListener;
import org.zone.region.flag.entity.player.move.greetings.GreetingsFlagListener;
import org.zone.region.flag.entity.player.move.leaving.LeavingFlagListener;
import org.zone.region.flag.entity.player.move.preventing.PreventPlayersListener;
import org.zone.region.group.key.GroupKeyManager;
import org.zone.utils.Messages;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private ZoneConfig config;
    private MemoryHolder memoryHolder;
    private static ZonePlugin zonePlugin;

    @SuppressWarnings("SpongeInjection")
    @Inject
    public ZonePlugin(final PluginContainer plugin, final Logger logger) {
        zonePlugin = this;
        this.plugin = plugin;
        this.logger = logger;
    }

    public @NotNull ZoneConfig getConfig() {
        return this.config;
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
        this.config = new ZoneConfig(new File("config/zone/config.conf"));
    }

    private void registerListeners() {
        EventManager eventManager = Sponge.eventManager();
        eventManager.registerListeners(this.plugin, new PlayerListener());
        eventManager.registerListeners(this.plugin, new MonsterPreventionListener());
        eventManager.registerListeners(this.plugin, new DoorInteractListener());
        eventManager.registerListeners(this.plugin, new BlockBreakListener());
        eventManager.registerListeners(this.plugin, new BlockPlaceListener());
        eventManager.registerListeners(this.plugin, new GreetingsFlagListener());
        eventManager.registerListeners(this.plugin, new PreventPlayersListener());
        eventManager.registerListeners(this.plugin, new LeavingFlagListener());
        eventManager.registerListeners(this.plugin, new ItemFrameInteractionListener());
        eventManager.registerListeners(this.plugin, new EntityDamagePlayerListener());
        eventManager.registerListeners(this.plugin, new PlayerFallDamageListener());
        eventManager.registerListeners(this.plugin, new TnTDefuseListener());
        eventManager.registerListeners(this.plugin, new FarmTramplingListener());
        eventManager.registerListeners(this.plugin, new CreeperGriefListener());
        eventManager.registerListeners(this.plugin, new EnderManGriefListener());
        eventManager.registerListeners(this.plugin, new ZombieGriefListener());
        eventManager.registerListeners(this.plugin, new SkeletonGriefListener());
        eventManager.registerListeners(this.plugin, new EnderDragonGriefListener());
        eventManager.registerListeners(this.plugin, new WitherGriefListener());
        eventManager.registerListeners(this.plugin, new EnderMiteGriefListener());
        eventManager.registerListeners(this.plugin, new HumanAIListener());
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        this.registerListeners();
    }

    @Listener
    public void onServerStarted(final StartedEngineEvent<Server> event) {
        this.config.loadDefaults();


        FlagManager manager = this.getFlagManager();
        Iterable<? extends FlagType.SerializableType<?>> types = (Iterable<? extends FlagType.SerializableType<?>>) manager
                .getRegistered(FlagType.SerializableType.class)
                .collect(Collectors.toSet());
        for (FlagType.SerializableType<?> type : types) {
            if (type instanceof FlagType.TaggedFlagType) {
                continue;
            }
            Optional<?> opDefault = manager.getDefaultFlags().loadDefault(type);
            if (opDefault.isPresent()) {
                continue;
            }
            Optional<? extends Flag.Serializable> opFlag = type.createCopyOfDefaultFlag();
            if (opFlag.isEmpty()) {
                continue;
            }
            try {
                manager.getDefaultFlags().setDefault(opFlag.get());
            } catch (IOException e) {
                this
                        .getLogger()
                        .error("Could not set the defaults for '" +
                                type.getId() +
                                "' " +
                                "despite a copy of default found. Is saving done " +
                                "correctly?");
                e.printStackTrace();
            }
        }
        try {
            manager.getDefaultFlags().save();
        } catch (ConfigurateException e) {
            this.getLogger().error("Could not save defaults file");
            e.printStackTrace();
        }

        Sponge.systemSubject().sendMessage(Messages.getLoadingZonesStart());
        File zonesFolder = new File("config/zone/zones/");
        Sponge.systemSubject().sendMessage(Messages.getZonesLoadingFrom(zonesFolder.getPath()));

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

    @Listener
    public void onRegisterData(RegisterDataEvent event) {
        event.register(DataRegistration.of(ZoneKeys.HUMAN_AI_ATTACHED_ZONE_ID, Human.class));
    }

    @Listener
    public void onReload(RefreshGameEvent event) {
        Optional<Audience> cSender = event.cause().first(Audience.class);
        try {
            this.config.getLoader().load();
            cSender.ifPresent(audience -> audience.sendMessage(Messages.getZoneConfigReloadedInfo()));
            this.zoneManager.zonesReload();
            cSender.ifPresent(audience -> audience.sendMessage(Messages.getZonesReloadedInfo()));
        } catch (ConfigurateException ce) {
            cSender.ifPresent(audience -> audience.sendMessage(Messages.getZoneConfigReloadFail()));
            ce.printStackTrace();
            this.logger.error("Event terminated!");
        }
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

    public <T extends Identifiable> Stream<T> getVanillaTypes(Class<T> type) {
        Typed typedAnnotation = type.getAnnotation(Typed.class);
        if (typedAnnotation == null) {
            throw new IllegalArgumentException("identifiable has no vanilla types");
        }
        Class<?> typesClass = typedAnnotation.typesClass();
        if (Enum.class.isAssignableFrom(typesClass)) {
            //noinspection rawtypes
            Class<? extends Enum> enumTypesClass = (Class<? extends Enum<?>>) typesClass;
            return EnumSet.allOf(enumTypesClass).stream();
        }
        return Arrays
                .stream(typesClass.getDeclaredFields())
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .filter(field -> type.isAssignableFrom(field.getType()))
                .map(field -> {
                    try {
                        return (T) field.get(null);
                    } catch (IllegalAccessException e) {
                        //noinspection ReturnOfNull
                        return null;
                    }
                })
                .filter(Objects::nonNull);

    }
}
