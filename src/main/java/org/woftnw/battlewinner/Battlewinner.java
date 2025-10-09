package org.woftnw.battlewinner;

import org.bukkit.plugin.java.JavaPlugin;
import org.woftnw.battlewinner.gameplay.item.GrapplingHookManager;
import org.woftnw.battlewinner.worldguard.WorldGuardAccessor;

public final class Battlewinner extends JavaPlugin {

    private static Battlewinner instance;
    private final WorldGuardAccessor worldGuardAccessor = new WorldGuardAccessor();

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new GrapplingHookManager(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Battlewinner getInstance() {
        return instance;
    }

    public WorldGuardAccessor getWorldGuardAccessor() {
        return worldGuardAccessor;
    }
}
