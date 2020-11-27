package cc.bytewithasix.smpfactions.events;

import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MemberListener implements Listener {

    @EventHandler
    public void ban(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (p.isBanned()) {
            MysqlGetterSetter.instance.deleteMember(p.getUniqueId());
        }
    }
}
