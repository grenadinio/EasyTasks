package me.grenadinio.easytasks;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Plugin plugin;

    public final NamespacedKey ZOMBIE_KEY = new NamespacedKey(this, "zombie_key");

    @Override
    public void onEnable() {
        plugin = this;
        EventListener eventListener = new EventListener(this);
        getServer().getPluginManager().registerEvents(eventListener, this);
    }

    @Override
    public void onDisable() {

    }
}
