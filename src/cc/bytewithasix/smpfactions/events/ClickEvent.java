package cc.bytewithasix.smpfactions.events;

import cc.bytewithasix.smpfactions.obj.Faction;
import cc.bytewithasix.smpfactions.obj.Member;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.utils.UserUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ClickEvent implements Listener {
    class TempMember {
        private String name;
        private boolean leader;
        public TempMember(String name, boolean leader) {
            this.name = name;
            this.leader = leader;
        }
        public String getName() { return name; }
        public boolean isLeader() { return leader; }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(final InventoryClickEvent e) {
        if(!(e.getView().getTitle().indexOf("Faction") != -1 ? true: false)) return;
        e.setCancelled(true);
        final ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        final Player p = (Player) e.getWhoClicked();
        if(e.getRawSlot() == 15) {
            String factionName = e.getView().getTitle().substring(10);
            Faction faction = MysqlGetterSetter.instance.getFaction(factionName);
            if(faction != null) {
                ArrayList<Member> members = MysqlGetterSetter.instance.getFactionMembers(faction.getId());
                ArrayList<TempMember> tempMembers = new ArrayList<TempMember>();
                if(members != null) {
                    for(Member m: members) {
                        tempMembers.add(new TempMember(UserUtils.getName(m.getUUID()), m.isLeader()));
                    }
                }


                p.sendMessage(ChatColor.GRAY + "===============");
                p.sendMessage(ChatColor.RED + "Members of " + ChatColor.GOLD + factionName);
                p.sendMessage(ChatColor.GRAY + "===============");
                for(TempMember m : tempMembers) {
                    if(m.isLeader()) {
                        p.sendMessage(ChatColor.YELLOW + m.getName() + ChatColor.DARK_GRAY + " | " + ChatColor.RED + "Leader");
                    }
                }
                for(TempMember m : tempMembers) {
                    if(!m.isLeader()) {
                        p.sendMessage(ChatColor.GRAY + m.getName() + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "Member");
                    }
                }
                p.sendMessage(ChatColor.GRAY + "===============");
                p.sendMessage(ChatColor.RED + "Total Members: " + ChatColor.GOLD + members.size());
                p.sendMessage(ChatColor.GRAY + "===============");
            } else {
                e.getView().close();
            }
        }
    }
}
