package net.lewmc.essence.stats;

import net.lewmc.essence.Essence;
import net.lewmc.essence.core.UtilMessage;
import net.lewmc.essence.core.UtilPermission;
import net.lewmc.foundry.command.FoundryCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandKill extends FoundryCommand {

    private final Essence plugin;

    /**
     * Constructor for the KillCommand class.
     * @param plugin References to the main plugin class.
     */
    public CommandKill(Essence plugin) {
        this.plugin = plugin;
    }

    /**
     * The permission required to run the command.
     * @return String - The permission string.
     */
    @Override
    protected String requiredPermission() {
        return "essence.stats.kill";
    }

    /**
     * @param cs        Information about who sent the command - player or console.
     * @param command   Information about what command was sent.
     * @param s         Command label - not used here.
     * @param args      The command's arguments.
     * @return boolean true/false - was the command accepted and processed or not?
     */
    @Override
    protected boolean onRun(CommandSender cs, Command command, String s, String[] args) {
        UtilMessage message = new UtilMessage(this.plugin, cs);

        if (args.length > 0) {
            return this.killOther(new UtilPermission(this.plugin, cs), cs, message, args);
        } else {
            if (!(cs instanceof Player)) {
                message.send("kill","usage");
                return true;
            } else {
                return this.killSelf((Player) cs, message);
            }
        }
    }

    /**
     * Kills the command sender.
     * @param p Player - The user to kill.
     * @param msg MessageUtil - The messaging system.
     * @return boolean - If the operation was successful
     */
    private boolean killSelf(Player p, UtilMessage msg) {
        p.setHealth(0);
        msg.send("kill", "beenkilled");
        return true;
    }

    /**
     * Kills another user.
     * @param perms PermisionHandler - The permission system.
     * @param cs CommandSender - The user to kill.
     * @param msg MessageUtil - The messaging system.
     * @param args String[] - List of command arguments.
     * @return boolean - If the operation was successful
     */
    private boolean killOther(UtilPermission perms, CommandSender cs, UtilMessage msg, String[] args) {
        if (perms.has("essence.stats.kill.other")) {
            String pName = args[0];
            Player p = Bukkit.getPlayer(pName);
            if (p != null) {
                msg.send("kill", "killed", new String[] { p.getName() });
                if (!(cs instanceof Player)) {
                    msg.sendTo(p, "kill", "serverkilled");
                } else {
                    msg.sendTo(p, "kill", "killedby", new String[] { cs.getName() });
                }
                p.setHealth(0);
            } else {
                msg.send("generic", "playernotfound");
            }
            return true;
        } else {
            return perms.not();
        }
    }
}
