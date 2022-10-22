package me.grenadinio.easytasks;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Plugin plugin;

    public static Plugin getPlugin() {
        return plugin;
    }

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
