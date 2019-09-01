package me.mrCookieSlime.Slimefun.Objects.SlimefunItem.items;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.handlers.ItemConsumptionHandler;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;

public class DietCookie extends SlimefunItem {

	public DietCookie(Category category, ItemStack item, String id, RecipeType recipeType, ItemStack[] recipe) {
		super(category, item, id, recipeType, recipe);
	}
	
	@Override
	public void register(boolean slimefun) {
		addItemHandler(new ItemConsumptionHandler() {
			
			@Override
			public boolean onConsume(PlayerItemConsumeEvent e, Player p, ItemStack item) {
				if (SlimefunManager.isItemSimiliar(item, getItem(), true)) {
					p.sendMessage(ChatColor.YELLOW + "You feel so light...");
					p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);

					if (p.hasPotionEffect(PotionEffectType.LEVITATION)) p.removePotionEffect(PotionEffectType.LEVITATION);
					p.addPotionEffect(PotionEffectType.LEVITATION.createEffect(60, 1));
					
					return true;
				}
				return false;
			}
		});
		
		super.register(slimefun);
	}

}
