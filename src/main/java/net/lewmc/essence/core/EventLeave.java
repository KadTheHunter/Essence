package net.lewmc.essence.core;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.lewmc.essence.Essence;
import net.lewmc.essence.teleportation.UtilLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * LeaveEvent class.
 */
public class EventLeave implements Listener {
    private final Essence plugin;

    /**
     * Constructor for the LeaveEvent class.
     * @param plugin Essence - Reference to the main Essence class.
     */
    public EventLeave(Essence plugin) {
        this.plugin = plugin;
    }

    /**
     * Event handler for when a player dies.
     * @param event PlayerQuitEvent - Server thrown event.
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        UtilLocation locationUtil = new UtilLocation(this.plugin);
        locationUtil.UpdateLastLocation(p);

        UtilPlaceholder tag = new UtilPlaceholder(this.plugin, p);
        if (this.plugin.config.get("chat.broadcasts.leave") instanceof String) {
            event.quitMessage(tag.replaceAll(MiniMessage.miniMessage().deserialize((String) this.plugin.config.get("chat.broadcasts.leave"))));
        }

        UtilPlayer up = new UtilPlayer(this.plugin);
        if (!up.savePlayer(p.getUniqueId())) {
            this.plugin.log.severe("Unable to save player data.");
            this.plugin.log.warn("It wasn't possible to save "+p.getName()+"'s player data.");
            this.plugin.log.warn("The player data may be stale/outdated.");
        }
        up.unloadPlayer(p.getUniqueId());

        plugin.msgHistory.remove(p);
        plugin.msgHistory.values().removeIf(value -> value.equals(p));
    }
}
