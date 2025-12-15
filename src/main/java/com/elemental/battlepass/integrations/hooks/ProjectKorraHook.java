// ============================================================================
// FILE: ProjectKorraHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

public class ProjectKorraHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public ProjectKorraHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            // Check if ProjectKorra is loaded
            Class<?> cooldownEventClass = Class.forName("com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent");
            Class<?> damageEventClass = Class.forName("com.projectkorra.projectkorra.event.AbilityDamageEntityEvent");
            
            // Register using EventExecutor to handle events dynamically
            plugin.getServer().getPluginManager().registerEvent(
                cooldownEventClass,
                this,
                org.bukkit.event.EventPriority.MONITOR,
                (listener, event) -> onAbilityUse(event),
                plugin,
                true
            );
            
            plugin.getServer().getPluginManager().registerEvent(
                damageEventClass,
                this,
                org.bukkit.event.EventPriority.MONITOR,
                (listener, event) -> onAbilityDamage(event),
                plugin,
                true
            );
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("ProjectKorra not found, skipping integration");
        }
    }

    public void onAbilityUse(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
            java.lang.reflect.Method isCooldownStarting = eventClass.getMethod("isCooldownStarting");
            if (!(Boolean) isCooldownStarting.invoke(event)) return;
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            
            java.lang.reflect.Method getAbility = eventClass.getMethod("getAbility");
            Object ability = getAbility.invoke(event);
            
            java.lang.reflect.Method getName = ability.getClass().getMethod("getName");
            String abilityName = (String) getName.invoke(ability);
            
            java.lang.reflect.Method getElement = ability.getClass().getMethod("getElement");
            Object element = getElement.invoke(ability);
            java.lang.reflect.Method getElementName = element.getClass().getMethod("getName");
            String elementName = (String) getElementName.invoke(element);
            
            plugin.getStatTracker().trackBendingUse(player, elementName, abilityName);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BENDING_USE", elementName, 1);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BENDING_ABILITY", abilityName, 1);
        } catch (Exception e) {
            // Silently fail
        }
    }

    public void onAbilityDamage(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
            java.lang.reflect.Method getDamager = eventClass.getMethod("getDamager");
            Object damager = getDamager.invoke(event);
            
            if (!(damager instanceof Player)) return;
            Player player = (Player) damager;
            
            java.lang.reflect.Method getDamage = eventClass.getMethod("getDamage");
            double damage = (Double) getDamage.invoke(event);
            
            java.lang.reflect.Method getAbility = eventClass.getMethod("getAbility");
            Object ability = getAbility.invoke(event);
            java.lang.reflect.Method getName = ability.getClass().getMethod("getName");
            String abilityName = (String) getName.invoke(ability);
            
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "BENDING_DAMAGE", abilityName, (int) damage);
        } catch (Exception e) {
            // Silently fail
        }
    }
}
