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
    private final Main plugin;

    public EventListener(Main plugin) {
        this.plugin = plugin;
    }

    private static final NamespacedKey ZOMBIE_KEY = new NamespacedKey(Main.getPlugin(), "zombie_key");
    private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#.##");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Привет");
        event.setJoinMessage("Privet, " + event.getPlayer().getName() + " or Привет");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Location location = event.getBlock().getLocation();
        UUID uuid = event.getPlayer().getUniqueId();

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

            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {

                Location playerLoc = event.getPlayer().getLocation();
                Vector direction = event.getPlayer().getLocation().getDirection();
                Location blockLoc = playerLoc.add(direction.multiply(2));

                Location locZombieLeft = blockLoc.clone().add(-1, 0, 0);
                Location locZombieRight = blockLoc.clone().add(1, 0, 0);


                //Leather yellow colored armour
                ItemStack boots = addLeatherArmourColor(Material.LEATHER_BOOTS, Color.YELLOW);
                ItemStack pants = addLeatherArmourColor(Material.LEATHER_LEGGINGS, Color.YELLOW);
                ItemStack chestplate = addLeatherArmourColor(Material.LEATHER_CHESTPLATE, Color.YELLOW);
                ItemStack helmet = addLeatherArmourColor(Material.LEATHER_HELMET, Color.RED);

                //Check location for left zombie
                if (locZombieLeft.clone().add(0, -1, 0).getBlock().getType() != Material.AIR
                        && locZombieLeft.getBlock().getType() == Material.AIR
                        && locZombieLeft.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {

                    Zombie z = (Zombie) event.getPlayer().getWorld().spawnEntity(locZombieLeft, EntityType.ZOMBIE);
                    EntityEquipment equipment = z.getEquipment();

                    if (equipment != null) {
                        equipment.setHelmet(helmet);
                        equipment.setChestplate(chestplate);
                        equipment.setLeggings(pants);
                        equipment.setBoots(boots);
                    }

                    z.setCustomName(ChatColor.YELLOW + "Zombie");

                    z.getPersistentDataContainer().set(ZOMBIE_KEY, PersistentDataType.STRING, "left_zombie");
                } else {
                    event.getPlayer().sendMessage("Слева не получилось");
                }

                //Check location for right zombie
                if (locZombieRight.clone().add(0, -1, 0).getBlock().getType() != Material.AIR
                        && locZombieRight.getBlock().getType() == Material.AIR
                        && locZombieRight.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {

                    Zombie z = (Zombie) event.getPlayer().getWorld().spawnEntity(locZombieRight, EntityType.ZOMBIE);
                    EntityEquipment equipment = z.getEquipment();

                    if (equipment != null) {
                        equipment.setHelmet(helmet);
                        equipment.setChestplate(chestplate);
                        equipment.setLeggings(pants);
                        equipment.setBoots(boots);
                    }
                    z.setCustomName(ChatColor.YELLOW + "Zombie");

                    z.getPersistentDataContainer().set(ZOMBIE_KEY, PersistentDataType.STRING, "right_zombie");
                } else {
                    event.getPlayer().sendMessage("Справа не получилось");
                }
            }, 100);
        }

        event.setDropItems(false);
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            event.getBlock().getWorld().dropItemNaturally(location, addItemMeta(new ItemStack(Material.MYCELIUM, 1)));
        }
    }

    @EventHandler
    public void onHurt(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (Objects.equals(event.getEntity().getPersistentDataContainer().get(ZOMBIE_KEY, PersistentDataType.STRING), "left_zombie")) {
            double damage = event.getFinalDamage();
            String roundedDamage = DAMAGE_FORMAT.format(damage);
            LivingEntity entity = (LivingEntity) event.getEntity();
            String entityHealth = DAMAGE_FORMAT.format(Math.max(entity.getHealth() - damage, 0));
            event.getDamager().sendMessage(String.format("Нанесено %s урона. У моба осталось %s хп.", roundedDamage, entityHealth));
        }
        if (Objects.equals(event.getEntity().getPersistentDataContainer().get(ZOMBIE_KEY, PersistentDataType.STRING), "right_zombie")) {
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
        if (Objects.equals(event.getEntity().getPersistentDataContainer().get(ZOMBIE_KEY, PersistentDataType.STRING), "left_zombie")
                || Objects.equals(event.getEntity().getPersistentDataContainer().get(ZOMBIE_KEY, PersistentDataType.STRING), "right_zombie")) {
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

        boolean hasMarker = Objects.equals(container.get(ZOMBIE_KEY, PersistentDataType.STRING), "dropped_item");

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
        container.set(ZOMBIE_KEY, PersistentDataType.STRING, "dropped_item");
        stack.setItemMeta(meta);

        return stack;
    }

    private ItemStack addLeatherArmourColor(Material material, Color color) {
        ItemStack itemStack = new ItemStack(material);
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
        if (leatherArmorMeta != null) leatherArmorMeta.setColor(color);
        itemStack.setItemMeta(leatherArmorMeta);

        return itemStack;
    }

}
