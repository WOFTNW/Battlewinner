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

        final ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
        final ItemStack offHandItem = event.getPlayer().getInventory().getItemInOffHand();
        // Make sure the player is holding a grappling hook
        if (isNotGrapplingHook(mainHandItem) && isNotGrapplingHook(offHandItem)) return;

        final FishHook hook = event.getHook();

        // Check if the player has cast the fishing rod
        if (event.getState().equals(PlayerFishEvent.State.FISHING)) {

            // This runs every tick and ensures the hook does not fall if it hits a block.
            BukkitRunnable hookTask = new BukkitRunnable() {
                @Override
                public void run() {
                    // Cancel this task if the hook doesn't exist anymore
                    if (!hook.isValid()) this.cancel();
                    final Vector velocity = hook.getVelocity();

                    // The hook velocity X and Z become zero when it hits a block.
                    if (velocity.getX() == 0.0 && velocity.getZ() == 0.0) {
                        // setGravity(false) doesn't work
                        // setNoPhysics(true) doesn't work
                        // 0.0298 is the magic number that keeps it in the air.
                        // Found through trial and error.
                        hook.setVelocity(new Vector(0.0, 0.0298, 0.0));
                    }
                }
            };

            // Run the task we just created every tick.
            hookTask.runTaskTimer(Battlewinner.getInstance(), 0, 0);
        }

        // Check if the player has reeled back the fishing rod
        if (event.getState().equals(PlayerFishEvent.State.REEL_IN) || event.getState().equals(PlayerFishEvent.State.IN_GROUND)) {

            boolean isTarget = false;
            final boolean isUniversal = !(isNotUniversalGrapple(offHandItem) && isNotUniversalGrapple(mainHandItem));

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

            final Location hookLocation = hook.getLocation();
            final Location playerLocation = event.getPlayer().getLocation();
            final double distance = hookLocation.distance(playerLocation);

            // Take the hook location as a vector and subtract the player location as a vector to get the vector from the player to the hook.
            Vector playerToHookVector = hookLocation.toVector().add(playerLocation.toVector().multiply(-1));

            // Velocity power is calculated as "log(distance + 1)"
            final Vector velocity = playerToHookVector.normalize().multiply(Math.log(distance + 1));
            // Velocity is in meters (blocks) per tick (1/20 second).
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
