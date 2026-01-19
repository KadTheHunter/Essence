package net.lewmc.essence.core;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.lewmc.essence.Essence;
import net.lewmc.essence.team.UtilTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * The tag utility replaces tags with preconfigured text (placeholders)
 */
public class UtilPlaceholder {
    private final Essence plugin;
    private final CommandSender cs;

    private static final String MC_VERSION = Bukkit.getBukkitVersion().split("-")[0];
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor
     * @param plugin Reference to the main Essence class.
     */
    public UtilPlaceholder(Essence plugin, CommandSender cs) {
        this.plugin = plugin;
        this.cs = cs;
    }

    /**
     * Replaces tags with preconfigured text. This is used to search for and replace multiple placeholders at a time.
     * @param text Component - Text to search and replace.
     * @return Component - Resulting String
     */
    public Component replaceAll(Component text) {
        if (this.plugin.integrations.PAPIEnabled) {
            return this.invokePAPI(this.cs instanceof Player p ? p : null, text);
        }

        String[] keys = {"version", "minecraft_version", "time", "date", "datetime", "player", "username", "team", "team_name", "team_leader", "team_prefix", "combined_prefix", "player_prefix", "player_suffix", "balance"};
        for (String key : keys) {
            String placeholder = "%essence_" + key + "%";
            text = text.replaceText(b -> b.matchLiteral(placeholder).replacement(this.replaceSingle(key)));
        }
        return text;
    }

    /**
     * Converts placeholders into strings. This is used to convert a single placeholder at a time.
     * @param placeholder String - The placeholder to convert (without the braces)
     * @return String - The string the placeholder becomes.
     */
    public String replaceSingle(String placeholder) {
        return switch (placeholder.toLowerCase()) {
            case "version" -> this.plugin.getDescription().getVersion();
            case "minecraft_version" -> MC_VERSION;
            case "time" -> LocalTime.now().format(TIME_FMT);
            case "date" -> LocalDate.now().format(DATE_FMT);
            case "datetime" -> LocalDateTime.now().format(DT_FMT);
            case "username" -> cs.getName();
            case "player" -> (boolean) this.plugin.config.get("chat.manage-chat") ? new UtilPlayer(this.plugin).getDisplayname(this.cs) : cs.getName();
            case "team_name", "team_leader", "team_prefix" -> {
                UtilTeam tu = new UtilTeam(this.plugin, new UtilMessage(this.plugin, this.cs));
                if (placeholder.equals("team_prefix")) yield tu.getTeamPrefix(this.cs);
                yield (this.cs instanceof Player p)
                        ? (placeholder.equals("team_name") ? Objects.requireNonNullElse(tu.getPlayerTeam(p.getUniqueId()), "No team") : Objects.requireNonNullElse(tu.getTeamLeader(tu.getPlayerTeam(p.getUniqueId())), "No leader"))
                        : (placeholder.equals("team_name") ? "No team" : "No leader");
            }
            case "player_prefix" -> new UtilPlayer(this.plugin).getPlayerPrefix(this.cs);
            case "player_suffix" -> new UtilPlayer(this.plugin).getPlayerSuffix(this.cs);
            case "combined_prefix" -> new UtilPlayer(this.plugin).getPlayerPrefix(cs) + new UtilTeam(this.plugin, new UtilMessage(this.plugin, this.cs)).getTeamPrefix(this.cs);
            case "balance" -> {
                String symbol = this.plugin.config.get("economy.symbol").toString();
                yield (this.cs instanceof Player p) ? symbol + new UtilPlayer(this.plugin).getPlayer(p.getUniqueId(), UtilPlayer.KEYS.ECONOMY_BALANCE) : symbol + "Infinity";
            }
            default -> placeholder;
        };
    }

    /**
     * Invokes PlaceholderAPI.
     * @param player Player - The player who invoked PAPI.
     * @param text Component - The text to translate placeholders for.
     * @return Component - The translated text.
     */
    public Component invokePAPI(Player player, Component text) {
        return MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, PlainTextComponentSerializer.plainText().serialize(text)));
    }
}
