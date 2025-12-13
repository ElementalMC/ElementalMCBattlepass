// ============================================================================
// FILE: BattlepassCommand.java
// LOCATION: src/main/java/com/elemental/battlepass/commands/
// ============================================================================
package com.elemental.battlepass.commands;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BattlepassCommand implements CommandExecutor {
    private final ElementalMCBattlepassTracker plugin;

    public BattlepassCommand(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "season":
                return handleSeason(sender, args);
            case "tier":
                return handleTier(sender, args);
            case "reload":
                return handleReload(sender);
            case "stats":
                return handleStats(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleSeason(CommandSender sender, String[] args) {
        if (!sender.hasPermission("battlepass.admin")) {
            sender.sendMessage(Component.text("No permission!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /battlepass season <create|end> [name]").color(NamedTextColor.RED));
            return true;
        }

        if (args[1].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /battlepass season create <name>").color(NamedTextColor.RED));
                return true;
            }

            String seasonName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            int seasonId = plugin.getSeasonManager().createSeason(seasonName);
            
            if (seasonId > 0) {
                sender.sendMessage(Component.text("Created season: " + seasonName + " (ID: " + seasonId + ")").color(NamedTextColor.GREEN));
                plugin.getQuestManager().loadQuestsForActiveSeason();
            } else {
                sender.sendMessage(Component.text("Failed to create season!").color(NamedTextColor.RED));
            }
            return true;
        }

        if (args[1].equalsIgnoreCase("end")) {
            int seasonId = plugin.getSeasonManager().getActiveSeasonId();
            if (seasonId == -1) {
                sender.sendMessage(Component.text("No active season!").color(NamedTextColor.RED));
                return true;
            }

            if (plugin.getSeasonManager().endSeason(seasonId)) {
                sender.sendMessage(Component.text("Ended season ID: " + seasonId).color(NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Failed to end season!").color(NamedTextColor.RED));
            }
            return true;
        }

        return false;
    }

    private boolean handleTier(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        int seasonId = plugin.getSeasonManager().getActiveSeasonId();
        
        if (seasonId == -1) {
            sender.sendMessage(Component.text("No active season!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            String tier = plugin.getSeasonManager().getPlayerTier(player.getUniqueId().toString(), seasonId);
            sender.sendMessage(Component.text("Your tier: ").color(NamedTextColor.GOLD).append(Component.text(tier).color(NamedTextColor.YELLOW)));
            return true;
        }

        if (!sender.hasPermission("battlepass.admin")) {
            sender.sendMessage(Component.text("No permission!").color(NamedTextColor.RED));
            return true;
        }

        String tier = args[1].toLowerCase();
        if (!tier.equals("free") && !tier.equals("premium") && !tier.equals("collector")) {
            sender.sendMessage(Component.text("Invalid tier! Use: free, premium, or collector").color(NamedTextColor.RED));
            return true;
        }

        plugin.getSeasonManager().setPlayerTier(player.getUniqueId().toString(), seasonId, tier);
        sender.sendMessage(Component.text("Tier set to: " + tier).color(NamedTextColor.GREEN));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("battlepass.admin")) {
            sender.sendMessage(Component.text("No permission!").color(NamedTextColor.RED));
            return true;
        }

        plugin.reloadConfig();
        plugin.getQuestManager().loadQuestsForActiveSeason();
        sender.sendMessage(Component.text("Configuration reloaded!").color(NamedTextColor.GREEN));
        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        sender.sendMessage(Component.text("=== Your Battlepass Stats ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Session Time: " + 
            plugin.getTimerManager().getSessionTime(player.getUniqueId()) + "s").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Kill Streak: " + 
            plugin.getTimerManager().getKillStreak(player.getUniqueId())).color(NamedTextColor.YELLOW));
        
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Battlepass Commands ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/battlepass season create <name> ").color(NamedTextColor.YELLOW).append(Component.text("- Create new season").color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("/battlepass season end ").color(NamedTextColor.YELLOW).append(Component.text("- End active season").color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("/battlepass tier [tier] ").color(NamedTextColor.YELLOW).append(Component.text("- View/set tier").color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("/battlepass reload ").color(NamedTextColor.YELLOW).append(Component.text("- Reload config").color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("/battlepass stats ").color(NamedTextColor.YELLOW).append(Component.text("- View stats").color(NamedTextColor.DARK_GRAY)));
    }
}