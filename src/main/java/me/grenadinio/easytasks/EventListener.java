package me.grenadinio.easytasks;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EventListener implements Listener {
    private static final NamespacedKey zombie_key = new NamespacedKey(Main.getPlugin(), "zombie_key");
    private static final DecimalFormat damage_format = new DecimalFormat("#.##");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Привет");
        event.setJoinMessage("Privet, " + event.getPlayer().getName() + " or Привет");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.DIRT || event.getBlock().getType() == Material.GRASS) {
            AtomicInteger seconds = new AtomicInteger(5);

            Timer t = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    if (seconds.get() == 0) {
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

                Location loc_zombie_left = block_loc.clone().add(-1, 0, 0);
                Location loc_zombie_right = block_loc.clone().add(1, 0, 0);


                //Leather armour
                ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
                ItemStack pants = new ItemStack(Material.LEATHER_LEGGINGS);
                ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);

                //Yellow colored leather armour
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) helmet.getItemMeta();
                if (leatherArmorMeta != null) {
                    leatherArmorMeta.setColor(Color.YELLOW);
                }
                helmet.setItemMeta(leatherArmorMeta);
                chestplate.setItemMeta(leatherArmorMeta);
                pants.setItemMeta(leatherArmorMeta);
                boots.setItemMeta(leatherArmorMeta);

                //Check location for left zombie
                if (loc_zombie_left.clone().add(0, -1, 0).getBlock().getType() != Material.AIR
                        && loc_zombie_left.getBlock().getType() == Material.AIR
                        && loc_zombie_left.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {

                    Zombie z = (Zombie) event.getPlayer().getWorld().spawnEntity(loc_zombie_left, EntityType.ZOMBIE);
                    EntityEquipment equipment = z.getEquipment();

                    if (equipment != null) {
                        equipment.setHelmet(helmet);
                        equipment.setChestplate(chestplate);
                        equipment.setLeggings(pants);
                        equipment.setBoots(boots);
                    }

                    z.setCustomName(ChatColor.YELLOW + "Zombie");

                    z.getPersistentDataContainer().set(zombie_key, PersistentDataType.STRING, "left_zombie");
                } else {
                    event.getPlayer().sendMessage("Слева не получилось");
                }

                //Check location for right zombie
                if (loc_zombie_right.clone().add(0, -1, 0).getBlock().getType() != Material.AIR
                        && loc_zombie_right.getBlock().getType() == Material.AIR
                        && loc_zombie_right.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {

                    Zombie z = (Zombie) event.getPlayer().getWorld().spawnEntity(loc_zombie_right, EntityType.ZOMBIE);
                    EntityEquipment equipment = z.getEquipment();

                    if (equipment != null) {
                        equipment.setHelmet(helmet);
                        equipment.setChestplate(chestplate);
                        equipment.setLeggings(pants);
                        equipment.setBoots(boots);
                    }
                    z.setCustomName(ChatColor.YELLOW + "Zombie");

                    z.getPersistentDataContainer().set(zombie_key, PersistentDataType.STRING, "right_zombie");
                } else {
                    event.getPlayer().sendMessage("Справа не получилось");
                }
            }, 100);
        }
    }

    @EventHandler
    public void onHurt(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (Objects.equals(event.getEntity().getPersistentDataContainer().get(zombie_key, PersistentDataType.STRING), "left_zombie")) {
            double damage = event.getFinalDamage();
            String rounded_damage = damage_format.format(damage);
            LivingEntity entity = (LivingEntity) event.getEntity();
            String entiry_health = damage_format.format(entity.getHealth() - damage);
            event.getDamager().sendMessage("Нанесено " + rounded_damage + " урона. У моба осталось " + entiry_health + " хп.");
        }
        if (Objects.equals(event.getEntity().getPersistentDataContainer().get(zombie_key, PersistentDataType.STRING), "right_zombie")) {
            Player player = (Player) event.getDamager();
            List<PotionEffect> list = new ArrayList<>();
            list.add(PotionEffectType.LEVITATION.createEffect(10 * 20, 1));
            list.add(PotionEffectType.SLOW.createEffect(10 * 20, 1));
            list.add(PotionEffectType.BLINDNESS.createEffect(10 * 20, 1));
            Collections.shuffle(list);
            player.addPotionEffect(list.get(0));
        }
    }
}
