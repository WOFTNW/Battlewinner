package org.woftnw.battlewinner.worldguard.handler;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.woftnw.battlewinner.Battlewinner;

import java.util.Objects;
import java.util.regex.Pattern;

public class RespiratorFlagHandler extends Handler {
	public static final Factory FACTORY = new Factory();
	private final StateFlag FLAG = Battlewinner.getInstance().getWorldGuardAccessor().getFlag("respirator").getFlag();

	public static class Factory extends Handler.Factory<RespiratorFlagHandler> {
		@Override
		public RespiratorFlagHandler create(Session session) {
			// create an instance of a handler for the particular session
			// if you need to pass certain variables based on, for example, the player
			// whose session this is, do it here
			return new RespiratorFlagHandler(session);
		}
	}
	// construct with your desired flag to track changes
	public RespiratorFlagHandler(Session session) {
		super(session);
	}
	// ... override handler methods here

	private long lastStageChange = 0;
	private AsphyxiaStage stage = AsphyxiaStage.NONE;

	@Override
	public void tick(@NotNull LocalPlayer player, ApplicableRegionSet set) {
		if (player.getHealth() <= 0) {
			return;
		}

		if (!Objects.equals(set.queryValue(player, FLAG), StateFlag.State.ALLOW)) {
			return;
		}

		long now = System.currentTimeMillis();

		if (getSession().isInvincible(player) || (player.getGameMode() != GameModes.SURVIVAL && player.getGameMode() != GameModes.ADVENTURE)) {
			// don't damage invincible players
			return;
		}

		Player bukkitPlayer = BukkitAdapter.adapt(player);
		final PotionEffect slowness1 = new PotionEffect(PotionEffectType.SLOWNESS, 20, 0, true, false);
		final PotionEffect slowness2 = new PotionEffect(PotionEffectType.SLOWNESS, 20, 1, true, false);
		final PotionEffect darkness = new PotionEffect(PotionEffectType.DARKNESS, 20, 0, true, false);
		final PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 20, 0, true, false);
		final PotionEffect poison1 = new PotionEffect(PotionEffectType.POISON, 20, 0, true, false);
		final PotionEffect poison2 = new PotionEffect(PotionEffectType.POISON, 20, 1, true, false);
		final PotionEffect nausea = new PotionEffect(PotionEffectType.NAUSEA, 20, 0, true, false);
		final PotionEffect wither2 = new PotionEffect(PotionEffectType.WITHER, 20, 1, true, false);

		if (stage == AsphyxiaStage.NONE) {

		} else if (stage == AsphyxiaStage.MILD) {
			bukkitPlayer.addPotionEffect(slowness1);
		} else if (stage == AsphyxiaStage.MODERATE) {
			bukkitPlayer.addPotionEffect(slowness1);
			bukkitPlayer.addPotionEffect(darkness);
		} else if (stage == AsphyxiaStage.HEAVY) {
			bukkitPlayer.addPotionEffect(slowness2);
			bukkitPlayer.addPotionEffect(darkness);
			bukkitPlayer.addPotionEffect(poison1);
		} else if (stage == AsphyxiaStage.EXTREME) {
			bukkitPlayer.addPotionEffect(slowness2);
			bukkitPlayer.addPotionEffect(blindness);
			bukkitPlayer.addPotionEffect(poison2);
		} else if (stage == AsphyxiaStage.LETHAL) {
			bukkitPlayer.addPotionEffect(slowness2);
			bukkitPlayer.addPotionEffect(blindness);
			bukkitPlayer.addPotionEffect(nausea);
			bukkitPlayer.addPotionEffect(wither2);
		}

		// 5 minutes in milliseconds
		long stageTime = 5 * 60 * 1000;
		if (now - lastStageChange > stageTime) {

			if (stage == AsphyxiaStage.NONE) {
				stage = AsphyxiaStage.MILD;
			} else if (stage == AsphyxiaStage.MILD) {
				stage = AsphyxiaStage.MODERATE;
			} else if (stage == AsphyxiaStage.MODERATE) {
				stage = AsphyxiaStage.HEAVY;
			} else if (stage == AsphyxiaStage.HEAVY) {
				stage = AsphyxiaStage.EXTREME;
			} else if (stage == AsphyxiaStage.EXTREME) {
				stage = AsphyxiaStage.LETHAL;
			} else {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + player.getName() + " only vengeance:asphyxia");
			}

			lastStageChange = now;
		}
	}

	/**
	 * Check whether a player is currently wearing a respirator
	 * @return true if the player is wearing a respirator, false otherwise
	 */
	public static boolean isPlayerWearingRespirator(@NotNull Player player) {
		// Get the item in the helmet slot
		ItemStack helmet = player.getInventory().getHelmet();
		// If it is null or has no item meta, it is not a respirator
		if (helmet == null || helmet.getItemMeta() == null) return false;
		// Get component string
		String componentString = helmet.getItemMeta().getAsComponentString();
		// We need to check for the custom_data component with the respirator key
		Pattern pattern = Pattern.compile("\\[.*minecraft:custom_data=\\{.*respirator:.*\\{.*}}.*]");
		// True if exists, false otherwise
		return (pattern.matcher(componentString).find());
	}

	enum AsphyxiaStage {
		NONE, // nothing
		MILD, // slowness
		MODERATE, // slowness & darkness
		HEAVY, // slowness 2, darkness, poison
		EXTREME, // slowness 2, blindness, poison 2
		LETHAL // slowness 2, blindness, nausea, wither 2
	}
}

