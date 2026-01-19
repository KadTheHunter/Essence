package net.lewmc.essence.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.lewmc.essence.Essence;
import net.lewmc.essence.core.UtilMessage;
import net.lewmc.essence.core.UtilPlaceholder;
import net.lewmc.essence.core.UtilPlayer;
import net.lewmc.foundry.command.FoundryCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /reply command.
 */
public class CommandReply extends FoundryCommand {
    private final Essence plugin;

    /**
     * Constructor for the ReplyCommand class.
     * @param plugin References to the main plugin class.
     */
    public CommandReply(Essence plugin) {
        this.plugin = plugin;
    }

    /**
     * The permission required to run the command.
     * @return String - The permission string.
     */
    @Override
    protected String requiredPermission() {
        return "essence.chat.reply";
    }

    /**
     * /reply command handler.
     * @param cs Information about who sent the command - player or console.
     * @param command Information about what command was sent.
     * @param s Command label - not used here.
     * @param args The command's arguments.
     * @return boolean true/false - was the command accepted and processed or not?
     */
    @Override
    protected boolean onRun(CommandSender cs, Command command, String s, String[] args) {
        UtilMessage message = new UtilMessage(this.plugin, cs);

        if (args.length > 0) {
            if (this.plugin.msgHistory.containsKey(cs)) {
                CommandSender p = this.plugin.msgHistory.get(cs);

                boolean canSend = true;
                if (cs instanceof Player sender && p instanceof Player target) {
                    canSend = !new UtilPlayer(this.plugin).playerIsIgnoring(target.getUniqueId(), sender.getUniqueId());
                }

                if (canSend) {
                    String rawMsg = String.join(" ", args);

                    Component msgComponent = (boolean) this.plugin.config.get("chat.allow-message-formatting")
                            ? MiniMessage.miniMessage().deserialize(rawMsg)
                            : MiniMessage.miniMessage().deserialize(rawMsg, TagResolver.empty());

                    msgComponent = new UtilPlaceholder(this.plugin, cs).replaceAll(msgComponent);

                    String processedMsg = PlainTextComponentSerializer.plainText().serialize(msgComponent);

                    String[] repl = new String[]{cs.getName(), p.getName(), processedMsg};
                    message.send("msg", "send", repl);
                    message.sendTo(p, "msg", "send", repl);

                    this.plugin.msgHistory.put(p, cs);
                } else {
                    message.send("ignore", "cantmessage", new String[]{p.getName()});
                }
            } else {
                message.send("reply", "none");
            }
            return true;
        }

        message.send("reply", "usage");
        return true;
    }
}