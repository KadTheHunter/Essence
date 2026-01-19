package net.lewmc.essence.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.lewmc.essence.Essence;
import net.lewmc.foundry.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Essence's Messaging Utility
 */
public class UtilMessage {

    private final CommandSender cs;
    private final Essence plugin;

    /**
     * Constructor for the MessageUtil class
     * @param cs CommandSender - the user who sent the command.
     * @param plugin Reference to the main Essence class.
     */
    public UtilMessage(Essence plugin, CommandSender cs) {
        this.plugin = plugin;
        this.cs = cs;
    }

    /**
     * Send a message to the user with additional data.
     * @param group String - The group the message belongs to in the language file.
     * @param msg String - The message taken from the language file.
     * @param replace String[] - Text that should be put in place of {{X}} in the message.
     * @since 1.5.3
     */
    public void send(String group, String msg, String[] replace) {
        this.sendTo(this.cs, group, msg, replace);
    }

    /**
     * Send a message to the user.
     * @param group String - The group the message belongs to in the language file.
     * @param msg String - The message taken from the language file.
     * @since 1.6.0
     */
    public void send(String group, String msg) {
        this.sendTo(this.cs, group, msg);
    }

    /**
     * Send a message to a user.
     * @param cs CommandSender - The player to send the message to.
     * @param group String - The group the message belongs to in the language file.
     * @param msg String - The message taken from the language file.
     * @since 1.6.0
     */
    public void sendTo(CommandSender cs, String group, String msg) {
        this.sendTo(cs, group, msg, new String[0]);
    }

    /**
     * Send a message to a user.
     * @param cs CommandSender - The player to send the message to.
     * @param group String - The group the message belongs to in the language file.
     * @param msg String - The message taken from the language file.
     * @param replace String[] - Text that should be put in place of tags in the message.
     * @since 1.6.0
     */
    public void sendTo(CommandSender cs, String group, String msg, String[] replace) {
        String raw = this.getMessage(msg, group);
        if (raw == null) {
            this.sendError(cs, group, msg);
            return;
        }

        List<TagResolver> resolvers = new ArrayList<>();
        for (int i = 0; i < replace.length; i++) {
            resolvers.add(Placeholder.parsed(String.valueOf(i + 1), replace[i]));
        }

        Component component = MiniMessage.miniMessage().deserialize(raw, TagResolver.resolver(resolvers));
        cs.sendMessage(new UtilPlaceholder(this.plugin, cs).replaceAll(component));
    }

    /**
     * Broadcasts a message to the server.
     * @param message String - The message to be sent.
     */
    public void broadcast(StringBuilder message) {
        Component content = MiniMessage.miniMessage().deserialize(message.toString());
        content = new UtilPlaceholder(this.plugin, Bukkit.getConsoleSender()).replaceAll(content);

        Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<gold>Broadcast > <yellow>").append(content));
    }

    /**
     * Handles errors whilst sending messages
     * @param cs    CommandSender - The command sender
     * @param group String - The message group (in the language file)
     * @param msg   Msg - The message (in the language file)
     */
    private void sendError(CommandSender cs, String group, String msg) {
        if (cs == this.cs) {
            cs.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>[Essence] <red>Unable to send message to player, see console for more information."));
        } else {
            cs.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>[Essence] <red>Unable to send message to player, see console for more information."));
            this.cs.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>[Essence] <red>Unable to send message to player, see console for more information."));
        }
        new Logger(this.plugin.foundryConfig).warn("Unable to send message '" + group + "." + msg + "' to player, could not find key in en-GB.yml");
    }

    /**
     * Send a message with clickable buttons to a player.
     * @param cs CommandSender - The player to send the message to.
     * @param group String - The group the message belongs to in the language file.
     * @param msg String - The message taken from the language file.
     * @param replace String[] - Text that should be put in place of {{X}} in the message.
     * @param acceptCommand String - The command to execute when the accept button is clicked.
     * @param denyCommand String - The command to execute when the deny button is clicked.
     * @since 1.11.0
     */
    public void sendToWithButtons(CommandSender cs, String group, String msg, String[] replace, String acceptCommand, String denyCommand) {
        if (!(cs instanceof Player player)) {
            this.sendTo(cs, group, msg, replace);
            return;
        }

        String rawMessage = this.getMessage(msg, group);

        if (rawMessage != null) {
            MiniMessage mm = MiniMessage.miniMessage();
            List<TagResolver> resolvers = new ArrayList<>();

            for (int i = 0; i < replace.length; i++) {
                resolvers.add(Placeholder.parsed(String.valueOf(i + 1), replace[i]));
            }

            String acceptText = this.getMessage("acceptbutton", group);
            String acceptHover = this.getMessage("accepthover", group);
            String denyText = this.getMessage("denybutton", group);
            String denyHover = this.getMessage("denyhover", group);

            resolvers.add(Placeholder.component("accept", mm.deserialize("<bold><green><click:run_command:'" + acceptCommand + "'><hover:show_text:'" + acceptHover + "'>[" + acceptText + "]</hover></click></green></bold>")));
            resolvers.add(Placeholder.component("deny", mm.deserialize("<bold><red><click:run_command:'" + denyCommand + "'><hover:show_text:'" + denyHover + "'>[" + denyText + "]</hover></click></red></bold>")));

            Component finalMessage = mm.deserialize(rawMessage + " <accept> <deny>", TagResolver.resolver(resolvers));
            player.sendMessage(finalMessage);
        } else {
            cs.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>[Essence] <red>Unable to send message to player, see console for more information."));
            new Logger(this.plugin.foundryConfig).warn("Unable to send message '" + group + "." + msg + "' to player, could not find key in en-GB.yml");
        }
    }

    /**
     * Retrieves the message from the language file.
     * @param code String - The code of the specific message to be retrieved.
     * @param group String - The group that the message is within.
     * @return String - The message from the language file.
     */
    private String getMessage(String code, String group) {
        if (this.plugin.messageStore.get(group) != null) {
            return this.plugin.messageStore.getString(group+"."+code);
        } else {
            return null;
        }
    }
}