package me.grenadinio.easytasks;

import javafx.scene.paint.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        event.getPlayer().sendMessage("Привет");
        event.setJoinMessage("Privet, " + event.getPlayer().getName() +"or Привет");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.DIRT || event.getBlock().getType() == Material.GRASS){
            Location l = event.getBlock().getLocation();
            Sheep s = (Sheep) event.getBlock().getWorld().spawnEntity(l.add(0,1,0), EntityType.SHEEP);

            s.setColor(DyeColor.PURPLE);
        }
    }
}
