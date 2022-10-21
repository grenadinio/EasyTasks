package me.grenadinio.easytasks;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        event.getPlayer().sendMessage("Привет");
        event.setJoinMessage("Privet, " + event.getPlayer().getName() +" or Привет");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.DIRT || event.getBlock().getType() == Material.GRASS) {
            AtomicInteger seconds = new AtomicInteger(5);

            Timer t = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    if(seconds.get() == 0){
                        t.cancel();
                        return;
                    }
                    event.getPlayer().sendMessage("Seconds: " + seconds.getAndDecrement());
                }
            };
            t.scheduleAtFixedRate(tt, 0L, TimeUnit.SECONDS.toMillis(1));

            Bukkit.getScheduler().scheduleSyncDelayedTask((Main.getPlugin()), () -> {
                
                Location player_loc = event.getPlayer().getLocation();
                Vector direction = event.getPlayer().getLocation().getDirection();
                Location block_loc = player_loc.add(direction.multiply(2));


                boolean block = block_loc.getBlock().getType() == Material.AIR;
                boolean block_under = block_loc.clone().add(0,-1,0).getBlock().getType() != Material.AIR;
                boolean block_above = block_loc.clone().add(0,1,0).getBlock().getType() == Material.AIR;

                if(block_under && block && block_above) {

                    Zombie z = (Zombie) event.getPlayer().getWorld().spawnEntity(block_loc, EntityType.ZOMBIE);
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

                    z.setCustomName(ChatColor.YELLOW + "Zombie");
                }
                else{
                    event.getPlayer().sendMessage("Зомби нет, ищи нового");
                }
            }, 100);
        }
    }
}
