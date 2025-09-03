package org.woftnw.battlewinner.gameplay.item;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.woftnw.battlewinner.Battlewinner;

import java.util.regex.Pattern;

/**
 * This class manages functionalities related to the grappling hook.
 */
public class GrapplingHookManager implements Listener {

    @EventHandler
    public void onPlayerFish(@NotNull PlayerFishEvent event) {

        ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack offHandItem = event.getPlayer().getInventory().getItemInOffHand();
        // Make sure the player is holding a grappling hook
        if (isNotGrapplingHook(mainHandItem) && isNotGrapplingHook(offHandItem)) return;

        if (event.getState().equals(PlayerFishEvent.State.FISHING)) {
            Battlewinner.getInstance().getLogger().info("Player launched a grappling hook!");
            FishHook hook = event.getHook();

            BukkitRunnable hookTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!hook.isValid()) this.cancel();
                    Vector velocity = hook.getVelocity();
                    Battlewinner.getInstance().getLogger().info("Velocity: " + velocity.getX() + ", " + velocity.getY() + ", " + velocity.getZ());
                    if (velocity.getX() == 0.0 && velocity.getZ() == 0.0) {
                        // setGravity(false) doesn't work
                        // setNoPhysics(true) doesn't work
                        hook.setVelocity(new Vector(0.0, 0.0298, 0.0));
                    }
                }
            };

            hookTask.runTaskTimer(Battlewinner.getInstance(), 0, 0);
        }

        if (event.getState().equals(PlayerFishEvent.State.REEL_IN) || event.getState().equals(PlayerFishEvent.State.IN_GROUND)) {

            FishHook hook = event.getHook();

            boolean isTarget = false;
            boolean isUniversal = !(isNotUniversalGrapple(offHandItem) && isNotUniversalGrapple(mainHandItem));

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {

                        Location clone = hook.getLocation().clone();
                        Vector vector = new Vector(x, y, z);
                        vector.multiply(0.5);
                        clone.add(vector);
                        if (clone.getBlock().getType() == Material.TARGET || (isUniversal && !clone.getBlock().isPassable())) {
                            isTarget = true;
                            break;
                        }
                    }
                }
            }

            if (!isTarget) return;

            Location hookLocation = hook.getLocation();
            Location playerLocation = event.getPlayer().getLocation();
            double distance = hookLocation.distance(playerLocation);

            Vector velocity = hookLocation.toVector().add(playerLocation.toVector().multiply(-1)).normalize().multiply(distance / 4);
            event.getPlayer().setVelocity(velocity);
        }
    }

    private boolean isNotGrapplingHook(@NotNull ItemStack item) {
        if (item.getItemMeta() == null) return true;
        String componentString = item.getItemMeta().getAsComponentString();
        Pattern pattern = Pattern.compile("\\[.*minecraft:custom_data=\\{.*grappling_hook:.*\\{.*}}.*]");
        return (!pattern.matcher(componentString).find());
    }

    private boolean isNotUniversalGrapple(@NotNull ItemStack item) {
        if (item.getItemMeta() == null) return true;
        String componentString = item.getItemMeta().getAsComponentString();
        Pattern pattern = Pattern.compile("\\[.*minecraft:custom_data=\\{.*grappling_hook:.*\\{.*any_blocks:1b.*}.*}.*]");
        return (!pattern.matcher(componentString).find());
    }

}
