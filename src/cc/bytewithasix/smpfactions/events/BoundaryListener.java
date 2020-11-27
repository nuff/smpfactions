package cc.bytewithasix.smpfactions.events;

import cc.bytewithasix.smpfactions.Main;
import cc.bytewithasix.smpfactions.obj.*;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;

public class BoundaryListener implements Listener {

    //Prevent breaking blocks
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if(!p.isOp() && positionCheck(e.getBlock().getLocation(), p, ChatColor.GRAY + "You can't break blocks here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Prevent placing blocks
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if(!p.isOp() && positionCheck(e.getBlock().getLocation(), p, ChatColor.GRAY + "You can't place blocks here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Safezone (No-PVP)
    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        if(!p.isOp() && positionCheck(entity.getLocation(), p, ChatColor.GRAY + "You can't attack here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Area enter and leave messages
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) return;

        Member member = MysqlGetterSetter.instance.getMember(p.getUniqueId());

        if(member != null) {
            for(Boundaries b: Main.instance.getFactionBoundaries()) {
                Faction f = MysqlGetterSetter.instance.getFaction(b.getFactionId());
                Location lastLoc = e.getFrom();
                Location newLoc = e.getTo();

                if(positionInArea(lastLoc.getBlockX(), lastLoc.getBlockZ(), b.getCoordsOne(), b.getCoordsTwo()) &&
                        !positionInArea(newLoc.getBlockX(), newLoc.getBlockZ(), b.getCoordsOne(), b.getCoordsTwo())) {
                    p.sendMessage(String.format(ChatColor.GRAY + "Leaving '%s' (Wilderness)..", f.getName()));
                }

                if(!positionInArea(lastLoc.getBlockX(), lastLoc.getBlockZ(), b.getCoordsOne(), b.getCoordsTwo()) &&
                        positionInArea(newLoc.getBlockX(), newLoc.getBlockZ(), b.getCoordsOne(), b.getCoordsTwo())) {
                    p.sendMessage(String.format(ChatColor.GRAY + "Entering '%s' (Safezone)..", f.getName()));
                }
            }
        }
    }

    //Prevent damaging vehicles
    @EventHandler(priority = EventPriority.LOW)
    public void vehicleDamageEvent(VehicleDamageEvent e) {
        if (e.getVehicle() instanceof Boat) return;
        if (!(e.getAttacker() instanceof Player)) return;

        Player p = (Player) e.getAttacker();
        if(!p.isOp() && positionCheck(e.getVehicle().getLocation(), p, ChatColor.GRAY + "You can't damage vehicles here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Prevent entering beds
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerBedEnter(final PlayerBedEnterEvent e) {
        Player p = e.getPlayer();
        if(!p.isOp() && positionCheck(e.getBed().getLocation(), p, ChatColor.GRAY + "You can't do that here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Prevent breakage of hanging entities
    @EventHandler(priority = EventPriority.LOW)
    public void onBreakHanging(final HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player)) return;
        Player p = (Player) e.getRemover();
        if(!p.isOp() && positionCheck(e.getEntity().getLocation(), p, ChatColor.GRAY + "You can't do that here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Prevent emptying buckets
    @EventHandler(priority = EventPriority.LOW)
    public void onBucketEmpty(final PlayerBucketEmptyEvent e) {
        Player p = e.getPlayer();
        if(!p.isOp() && positionCheck(e.getBlock().getLocation(), p, ChatColor.GRAY + "You can't do that here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Prevent filling buckets
    @EventHandler(priority = EventPriority.LOW)
    public void onBucketFill(final PlayerBucketFillEvent e) {
        Player p = e.getPlayer();
        if(!p.isOp() && positionCheck(e.getBlock().getLocation(), p, ChatColor.GRAY + "You can't do that here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Protect sheep
    @EventHandler(priority = EventPriority.LOW)
    public void onShear(final PlayerShearEntityEvent e) {
        Player p = e.getPlayer();
        if(!p.isOp() && positionCheck(e.getEntity().getLocation(), p, ChatColor.GRAY + "You can't do that here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Stop lava flow or water into a district
    @EventHandler(priority = EventPriority.LOW)
    public void onFlow(final BlockFromToEvent e) {
        Block lastBlock = e.getBlock();
        Block toBlock = e.getToBlock();

        //Only check lateral movement
        if (lastBlock.getLocation().getBlockX() == toBlock.getLocation().getBlockX()
                && e.getBlock().getLocation().getBlockZ() == e.getToBlock().getLocation().getBlockZ()) return;

        //Ignore flows within flow
        if (lastBlock.getType().equals(toBlock.getType())) return;

        for(Boundaries b: Main.instance.getFactionBoundaries()) {
            if (!positionInArea(lastBlock.getLocation().getBlockX(), lastBlock.getLocation().getBlockZ(), b.getCoordsOne(), b.getCoordsTwo()) &&
                    positionInArea(toBlock.getLocation().getBlockX(), toBlock.getLocation().getBlockZ(), b.getCoordsOne(), b.getCoordsTwo())) {
                e.setCancelled(true);
            }
        }
    }

    //Prevent block click interaction
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if(!p.isOp() && positionCheck(e.getClickedBlock().getLocation(), p, ChatColor.GRAY + "You can't do that here. This area is claimed by %s.")) e.setCancelled(true);
    }

    //Prevent entity right-click interaction
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityInteract(final PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if(!p.isOp() && positionCheck(e.getRightClicked().getLocation(), p, ChatColor.GRAY + "You can't do that here. This area is claimed by %s.")) e.setCancelled(true);
    }

    public boolean positionCheck(Location elementLocation, Player p, String message) {
        Member m = MysqlGetterSetter.instance.getMember(p.getUniqueId());

        if(m != null) {
            for(Boundaries b: Main.instance.getFactionBoundaries()) {
                Faction f = MysqlGetterSetter.instance.getFaction(b.getFactionId());
                War w = MysqlGetterSetter.instance.getWarByFaction(b.getFactionId());

                if(b.getFactionId() != m.getFactionId()) { //Member is not part of boundary faction
                    if (w == null) { //No war under the boundaries factions's ID
                        if (positionInArea(elementLocation.getBlockX(), elementLocation.getBlockZ(), b.getCoordsOne(), b.getCoordsTwo())) { //Block is within boundaries
                            p.sendMessage(String.format(message, f.getName()));
                            return true;
                        }
                    } else { //War under the boundaries factions's ID
                        if (    m.getFactionId() != w.getAttackerId() &&
                                m.getFactionId() != w.getDefenderId()
                        ) {
                            p.sendMessage(String.format(message, f.getName()));
                            return true;
                        }
                    }
                }
            }
        } else {
            p.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "We couldn't find you in our member database. This is most likely a bug and you won't be able to attack until it's fixed. Please try reconnecting or contacting an admin.");
            return true;
        }
        return false;
    }

    public boolean positionInArea(int x, int y, Coords2D posOne, Coords2D posTwo) {
        int x1, x2, y1, y2;

        if(posOne.getX() <= posTwo.getX()) {
            x1 = posOne.getX();
            x2 = posTwo.getX();
        } else {
            x1 = posTwo.getX();
            x2 = posOne.getX();
        }
        if(posOne.getZ() <= posTwo.getZ()) {
            y2 = posTwo.getZ();
            y1 = posOne.getZ();
        } else {
            y2 = posOne.getZ();
            y1 = posTwo.getZ();
        }

        if(x >= x1 && x <= x2 && y >= y1 && y <= y2) return true;
        return false;
    }
}
