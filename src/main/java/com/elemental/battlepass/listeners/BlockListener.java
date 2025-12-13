// ============================================================================
// FILE: BlockListener.java
// LOCATION: src/main/java/com/elemental/battlepass/listeners/
// ============================================================================
package com.elemental.battlepass.listeners;

import com.elemental.battlepass.ElementalBattlepassTracker;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    private final ElementalBattlepassTracker plugin;

    public BlockListener(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockType = block.getType().name();

        plugin.getStatTracker().trackBlockBreak(player, blockType);
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BLOCK_BREAK", blockType, 1);
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BLOCK_BREAK", null, 1);

        if (isCrop(block.getType())) {
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "HARVEST_CROP", blockType, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String blockType = event.getBlock().getType().name();

        plugin.getStatTracker().trackBlockPlace(player, blockType);
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BLOCK_PLACE", blockType, 1);
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BLOCK_PLACE", null, 1);
    }

    private boolean isCrop(Material material) {
        return material == Material.WHEAT || material == Material.CARROTS || 
               material == Material.POTATOES || material == Material.BEETROOTS ||
               material == Material.NETHER_WART || material == Material.SWEET_BERRY_BUSH;
    }
}