package org.woftnw.battlewinner;

import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.woftnw.battlewinner.gameplay.item.GrapplingHookManager;

public final class Battlewinner extends JavaPlugin {

    private static Battlewinner instance;

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
}
