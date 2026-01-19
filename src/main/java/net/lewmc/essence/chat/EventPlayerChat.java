package net.lewmc.essence.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.lewmc.essence.Essence;
import net.lewmc.essence.core.UtilPlaceholder;
import net.lewmc.essence.core.UtilPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * PlayerChatEvent fires when a player sends a message in chat.
 */
public class EventPlayerChat implements Listener {
    private final Essence plugin;

    /**
     * Constructs the class.
     * @param plugin Reference to the main Essence class.
     */
    public EventPlayerChat(Essence plugin) {
        this.plugin = plugin;
    }

    /**
     * Fires when a player sends a message in chat.
     * @param event The AsyncChatEvent event.
     */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (!(boolean) this.plugin.config.get("chat.manage-chat")) return;

        Player player = event.getPlayer();
        MiniMessage mm = MiniMessage.miniMessage();
        UtilPlayer up = new UtilPlayer(this.plugin);

        event.viewers().removeIf(audience -> {
            if (audience instanceof Player recipient) {
                return up.playerIsIgnoring(recipient.getUniqueId(), player.getUniqueId());
            }
            return false;
        });

        String rawMsg = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        Component messageContent = (boolean) this.plugin.config.get("chat.allow-message-formatting")
                ? mm.deserialize(rawMsg)
                : Component.text(rawMsg);

        String format = this.plugin.config.get("chat.name-format") + " <user_message>";

        final Component finalChatLine = new UtilPlaceholder(this.plugin, player).replaceAll(
                mm.deserialize(format, Placeholder.component("user_message", messageContent))
        );

        event.renderer((source, sourceDisplayName, message, viewer) -> finalChatLine);
    }}
