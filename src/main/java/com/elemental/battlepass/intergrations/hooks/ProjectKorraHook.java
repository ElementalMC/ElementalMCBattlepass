// ============================================================================
// FILE: ProjectKorraHook.java
// LOCATION: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ProjectKorraHook implements Listener {
    private final ElementalBattlepassTracker plugin;

    public ProjectKorraHook(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbilityUse(PlayerCooldownChangeEvent event) {
        if (event.isCooldownStarting()) {
            Player player = event.getPlayer();
            String abilityName = event.getAbility().getName();
            String element = event.getAbility().getElement().getName();
            
            plugin.getStatTracker().trackBendingUse(player, element, abilityName);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BENDING_USE", element, 1);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BENDING_ABILITY", abilityName, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbilityDamage(AbilityDamageEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            double damage = event.getDamage();
            String abilityName = event.getAbility().getName();
            
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BENDING_DAMAGE", abilityName, (int) damage);
        }
    }
}
