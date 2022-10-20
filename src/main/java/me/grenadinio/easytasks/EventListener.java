package me.grenadinio.easytasks;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        event.getPlayer().sendMessage("Привет");
        event.setJoinMessage("Privet, " + event.getPlayer().getName() +" or Привет");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.DIRT || event.getBlock().getType() == Material.GRASS){
            Location l = event.getBlock().getLocation();
            Zombie z = (Zombie) event.getBlock().getWorld().spawnEntity(l.add(0,1,0), EntityType.ZOMBIE);
            EntityEquipment equipment = z.getEquipment();

            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            ItemStack pants = new ItemStack(Material.LEATHER_LEGGINGS);
            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);

            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) helmet.getItemMeta();

            leatherArmorMeta.setColor(Color.YELLOW);

            helmet.setItemMeta(leatherArmorMeta);
            chestplate.setItemMeta(leatherArmorMeta);
            pants.setItemMeta(leatherArmorMeta);
            boots.setItemMeta(leatherArmorMeta);

            equipment.setHelmet(helmet);
            equipment.setChestplate(chestplate);
            equipment.setLeggings(pants);
            equipment.setBoots(boots);
        }
    }
}
