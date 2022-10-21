package me.grenadinio.easytasks;

import org.bson.Document;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

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

        Location location = event.getBlock().getLocation();
        UUID uuid = event.getPlayer().getUniqueId();
        Material material = event.getBlock().getType();

        Document result = MongoConnect.execute((collection -> collection.find(and(
                eq("uuid", uuid.toString()),
                eq("x", location.getBlockX()),
                eq("y", location.getBlockY()),
                eq("z", location.getBlockZ())
        )).first()));

        if (result == null) {
            event.setCancelled(true);
            return;
        }

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

        event.setDropItems(false);
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            event.getBlock().getWorld().dropItemNaturally(location, addItemMeta(new ItemStack(material, 1)));
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
            String entiry_health = damage_format.format(Math.max(entity.getHealth() - damage, 0));
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

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (Objects.equals(event.getEntity().getPersistentDataContainer().get(zombie_key, PersistentDataType.STRING), "left_zombie")
                || Objects.equals(event.getEntity().getPersistentDataContainer().get(zombie_key, PersistentDataType.STRING), "right_zombie")) {
            event.getDrops().clear();

            event.getDrops().add(addItemMeta(new ItemStack(Material.DIRT, 1)));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Location location = event.getBlock().getLocation();
        String material = event.getBlock().getType().toString();


        ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = Objects.requireNonNull(stack.getItemMeta());
        PersistentDataContainer container = meta.getPersistentDataContainer();

        boolean hasMarker = Objects.equals(container.get(zombie_key, PersistentDataType.STRING), "dropped_item");

        if (!hasMarker) {
            event.setCancelled(true);
            return;
        }

        MongoConnect.execute((collection) -> {
            collection.insertOne(new Document()
                    .append("uuid", uuid.toString())
                    .append("type", material)
                    .append("x", location.getBlockX())
                    .append("y", location.getBlockY())
                    .append("z", location.getBlockZ()));
            return null;
        });
    }


    private ItemStack addItemMeta(ItemStack stack) {
        ItemMeta meta = Objects.requireNonNull(stack.getItemMeta());
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(zombie_key, PersistentDataType.STRING, "dropped_item");
        stack.setItemMeta(meta);

        return stack;
    }

}
