package cc.bytewithasix.smpfactions.events;

import cc.bytewithasix.smpfactions.obj.Faction;
import cc.bytewithasix.smpfactions.obj.Member;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class FactionEvents implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        Member m = MysqlGetterSetter.instance.getMember(p.getUniqueId());
        if(m != null) {
            if(m.getFactionId() != 0) {
                String dN = p.getDisplayName();
                Faction f = MysqlGetterSetter.instance.getFaction(m.getFactionId());
                p.setDisplayName(String.format("%s[%s] %s%s", ChatColor.AQUA, f.getName(), ChatColor.GRAY, dN));
            } else {
                String dN = p.getDisplayName();
                p.setDisplayName(ChatColor.GRAY + dN);
            }
        }
    }
}
