package io.github.thebusybiscuit.slimefun4.api.gps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.cscorelib2.chat.ChatColors;
import io.github.thebusybiscuit.cscorelib2.chat.ChatInput;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.cscorelib2.math.DoubleHandler;
import io.github.thebusybiscuit.slimefun4.api.events.WaypointCreateEvent;
import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun4.api.geo.ResourceManager;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.implementation.items.gps.GPSTransmitter;
import io.github.thebusybiscuit.slimefun4.implementation.items.gps.Teleporter;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.Slimefun;

/**
 * The {@link GPSNetwork} is a manager class for all {@link GPSTransmitter Transmitters} and waypoints.
 * There can only be one instance of this class per {@link Server}.
 * It is also responsible for teleportation and resource management.
 * 
 * @author TheBusyBiscuit
 * 
 * @see TeleportationManager
 * @see ResourceManager
 *
 */
public class GPSNetwork {

    private final int[] border = { 0, 1, 3, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53 };
    private final int[] inventory = { 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43 };

    private final Map<UUID, Set<Location>> transmitters = new HashMap<>();
    private final TeleportationManager teleportation = new TeleportationManager();
    private final ResourceManager resourceManager = new ResourceManager(SlimefunPlugin.instance);

    private final ItemStack deathpointIcon = SlimefunUtils.getCustomHead("1ae3855f952cd4a03c148a946e3f812a5955ad35cbcb52627ea4acd47d3081");
    private final ItemStack netherIcon = SlimefunUtils.getCustomHead("d83571ff589f1a59bb02b80800fc736116e27c3dcf9efebede8cf1fdde");
    private final ItemStack endIcon = SlimefunUtils.getCustomHead("c6cac59b2aae489aa0687b5d802b2555eb14a40bd62b21eb116fa569cdb756");
    private final ItemStack worldIcon = SlimefunUtils.getCustomHead("c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56");

    /**
     * This method updates the status of a {@link GPSTransmitter}.
     * 
     * @param l
     *            The {@link Location} of the {@link GPSTransmitter}
     * @param uuid
     *            The {@link UUID} who the {@link GPSTransmitter} belongs to
     * @param online
     *            Whether that {@link GPSTransmitter} is online
     */
    public void updateTransmitter(Location l, UUID uuid, boolean online) {
        Set<Location> set = transmitters.computeIfAbsent(uuid, id -> new HashSet<>());

        if (online) {
            set.add(l);
        }
        else {
            set.remove(l);
        }
    }

    /**
     * This method calculates the GPS complexity for the given {@link UUID}.
     * The complexity is determined by the Y level of each {@link GPSTransmitter}
     * multiplied by the multiplier of that transmitter.
     * 
     * @param uuid
     *            The {@link UUID} who to calculate it for
     * 
     * @return The network complexity for that {@link UUID}
     */
    public int getNetworkComplexity(UUID uuid) {
        if (!transmitters.containsKey(uuid)) {
            return 0;
        }

        int level = 0;
        for (Location l : transmitters.get(uuid)) {
            SlimefunItem item = BlockStorage.check(l);

            if (item instanceof GPSTransmitter) {
                level += ((GPSTransmitter) item).getMultiplier(l.getBlockY());
            }
        }

        return level;
    }

    /**
     * This method returns the amount of {@link GPSTransmitter Transmitters} for the
     * given {@link UUID}.
     * 
     * @param uuid
     *            The {@link UUID} who these transmitters belong to
     * 
     * @return The amount of transmitters
     */
    public int countTransmitters(UUID uuid) {
        if (!transmitters.containsKey(uuid)) {
            return 0;
        }
        else {
            return transmitters.get(uuid).size();
        }
    }

    public void openTransmitterControlPanel(Player p) {
        ChestMenu menu = new ChestMenu(ChatColor.BLUE + SlimefunPlugin.getLocal().getMessage(p, "machines.GPS_CONTROL_PANEL.title"));

        for (int slot : border) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        menu.addItem(2, new CustomItem(SlimefunItems.GPS_TRANSMITTER, im -> {
            im.setDisplayName(ChatColor.GRAY + SlimefunPlugin.getLocal().getMessage(p, "machines.GPS_CONTROL_PANEL.transmitters"));
            im.setLore(null);
        }));

        menu.addMenuClickHandler(2, ChestMenuUtils.getEmptyClickHandler());

        int complexity = getNetworkComplexity(p.getUniqueId());
        menu.addItem(4, new CustomItem(SlimefunItems.GPS_CONTROL_PANEL, "&7Network Info", "", "&8\u21E8 &7Status: " + (complexity > 0 ? "&2&lONLINE" : "&4&lOFFLINE"), "&8\u21E8 &7Complexity: &r" + complexity));
        menu.addMenuClickHandler(4, ChestMenuUtils.getEmptyClickHandler());

        menu.addItem(6, new CustomItem(worldIcon, "&7" + SlimefunPlugin.getLocal().getMessage(p, "machines.GPS_CONTROL_PANEL.waypoints"), "", ChatColor.GRAY + "\u21E8 " + SlimefunPlugin.getLocal().getMessage(p, "guide.tooltips.open-category")));
        menu.addMenuClickHandler(6, (pl, slot, item, action) -> {
            openWaypointControlPanel(pl);
            return false;
        });

        int index = 0;
        for (Location l : getTransmitters(p.getUniqueId())) {
            if (index >= inventory.length) break;

            SlimefunItem sfi = BlockStorage.check(l);
            if (sfi instanceof GPSTransmitter) {
                int slot = inventory[index];

                menu.addItem(slot, new CustomItem(SlimefunItems.GPS_TRANSMITTER, "&bGPS Transmitter", "&8\u21E8 &7World: &r" + l.getWorld().getName(), "&8\u21E8 &7X: &r" + l.getX(), "&8\u21E8 &7Y: &r" + l.getY(), "&8\u21E8 &7Z: &r" + l.getZ(), "", "&8\u21E8 &7Signal Strength: &r" + ((GPSTransmitter) sfi).getMultiplier(l.getBlockY()), "&8\u21E8 &7Ping: &r" + DoubleHandler.fixDouble(1000D / l.getY()) + "ms"));
                menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());

                index++;
            }
        }

        menu.open(p);
    }

    /**
     * This returns an icon for the given waypoint.
     * The icon is dependent on the {@link Environment} of the waypoint's {@link World}.
     * However if the name of this waypoint indicates that this is actually a deathmarker
     * then a different texture will be used.
     * 
     * Otherwise it will return a globe, a nether or end sphere according to the {@link Environment}.
     * 
     * @param name
     *            The name of a waypoint
     * @param environment
     *            The {@link Environment} of the waypoint's {@link World}
     * 
     * @return An icon for this waypoint
     */
    public ItemStack getIcon(String name, Environment environment) {
        if (name.startsWith("player:death ")) {
            return deathpointIcon;
        }
        else if (environment == Environment.NETHER) {
            return netherIcon;
        }
        else if (environment == Environment.THE_END) {
            return endIcon;
        }
        else {
            return worldIcon;
        }
    }

    public void openWaypointControlPanel(Player p) {
        PlayerProfile.get(p, profile -> {
            ChestMenu menu = new ChestMenu(ChatColor.BLUE + SlimefunPlugin.getLocal().getMessage(p, "machines.GPS_CONTROL_PANEL.title"));

            for (int slot : border) {
                menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }

            menu.addItem(2, new CustomItem(SlimefunItems.GPS_TRANSMITTER, "&7" + SlimefunPlugin.getLocal().getMessage(p, "machines.GPS_CONTROL_PANEL.transmitters"), "", ChatColor.GRAY + "\u21E8 " + SlimefunPlugin.getLocal().getMessage(p, "guide.tooltips.open-category")));
            menu.addMenuClickHandler(2, (pl, slot, item, action) -> {
                openTransmitterControlPanel(pl);
                return false;
            });

            int complexity = getNetworkComplexity(p.getUniqueId());
            menu.addItem(4, new CustomItem(SlimefunItems.GPS_CONTROL_PANEL, "&7Network Info", "", "&8\u21E8 &7Status: " + (complexity > 0 ? "&2&lONLINE" : "&4&lOFFLINE"), "&8\u21E8 &7Complexity: &r" + complexity));
            menu.addMenuClickHandler(4, ChestMenuUtils.getEmptyClickHandler());

            menu.addItem(6, new CustomItem(worldIcon, "&7" + SlimefunPlugin.getLocal().getMessage(p, "machines.GPS_CONTROL_PANEL.waypoints")));
            menu.addMenuClickHandler(6, ChestMenuUtils.getEmptyClickHandler());

            int index = 0;
            for (Waypoint waypoint : profile.getWaypoints()) {
                if (index >= inventory.length) break;
                int slot = inventory[index];

                Location l = waypoint.getLocation();
                menu.addItem(slot, new CustomItem(waypoint.getIcon(), waypoint.getName().replace("player:death ", ""), "&8\u21E8 &7World: &r" + l.getWorld().getName(), "&8\u21E8 &7X: &r" + l.getX(), "&8\u21E8 &7Y: &r" + l.getY(), "&8\u21E8 &7Z: &r" + l.getZ(), "", "&8\u21E8 &cClick to delete"));
                menu.addMenuClickHandler(slot, (pl, slotn, item, action) -> {
                    profile.removeWaypoint(waypoint);
                    pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);

                    openWaypointControlPanel(pl);
                    return false;
                });

                index++;
            }

            menu.open(p);
        });
    }

    /**
     * This method will prompt the given {@link Player} to enter a name for a waypoint.
     * After entering the name, it will be added to his waypoint list.
     * 
     * @param p
     *            The {@link Player} who should get a new waypoint
     * @param l
     *            The {@link Location} of the new waypoint
     */
    public void createWaypoint(Player p, Location l) {
        PlayerProfile.get(p, profile -> {
            if ((profile.getWaypoints().size() + 2) > inventory.length) {
                SlimefunPlugin.getLocal().sendMessage(p, "gps.waypoint.max", true);
                return;
            }

            SlimefunPlugin.getLocal().sendMessage(p, "gps.waypoint.new", true);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 1F);

            ChatInput.waitForPlayer(SlimefunPlugin.instance, p, message -> addWaypoint(p, message, l));
        });
    }

    /**
     * This method adds a new waypoint with the given name and {@link Location} for that {@link Player}.
     * 
     * @param p
     *            The {@link Player} to get the new waypoint
     * @param name
     *            The name of this waypoint
     * @param l
     *            The {@link Location} of this waypoint
     */
    public void addWaypoint(Player p, String name, Location l) {
        PlayerProfile.get(p, profile -> {
            if ((profile.getWaypoints().size() + 2) > inventory.length) {
                SlimefunPlugin.getLocal().sendMessage(p, "gps.waypoint.max", true);
                return;
            }

            Slimefun.runSync(() -> {
                WaypointCreateEvent event = new WaypointCreateEvent(p, name, l);
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    String id = ChatColor.stripColor(ChatColors.color(event.getName())).toUpperCase(Locale.ROOT).replace(' ', '_');
                    profile.addWaypoint(new Waypoint(profile, id, event.getLocation(), event.getName()));

                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F);
                    SlimefunPlugin.getLocal().sendMessage(p, "gps.waypoint.added", true);
                }
            });
        });
    }

    /**
     * This method returns a {@link Set} of {@link Location Locations} for all {@link GPSTransmitter Transmitters}
     * owned by the given {@link UUID}.
     * 
     * @param uuid
     *            The {@link UUID} owning those transmitters
     * 
     * @return A {@link Set} with all {@link Location Locations} of transmitters for this {@link UUID}
     */
    public Set<Location> getTransmitters(UUID uuid) {
        return transmitters.getOrDefault(uuid, new HashSet<>());
    }

    /**
     * This returns the {@link TeleportationManager} for this {@link GPSNetwork}.
     * It is responsible for all actions that relate to the {@link Teleporter}.
     * 
     * @return The {@link TeleportationManager} for this {@link GPSNetwork}
     */
    public TeleportationManager getTeleportationManager() {
        return teleportation;
    }

    /**
     * This returns the {@link ResourceManager} for this {@link GPSNetwork}.
     * Use this to access {@link GEOResource GEOResources}.
     * 
     * @return The {@link ResourceManager} for this {@link GPSNetwork}
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

}
