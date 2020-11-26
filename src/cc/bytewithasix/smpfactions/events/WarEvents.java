package cc.bytewithasix.smpfactions.events;

import cc.bytewithasix.smpfactions.obj.Faction;
import cc.bytewithasix.smpfactions.obj.Member;
import cc.bytewithasix.smpfactions.obj.War;
import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class WarEvents implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity().getPlayer();
        Member m = MysqlGetterSetter.instance.getMember(p.getUniqueId());
        int memberFaction = m.getFactionId();

        if(m != null) {
            if(memberFaction == 0) return;
            Faction f = MysqlGetterSetter.instance.getFaction(memberFaction);
            for(War w: MysqlGetterSetter.instance.getAllWars()) {
                if (w.getGrace() == 0) {
                    Faction attacker = MysqlGetterSetter.instance.getFaction(w.getAttackerId());
                    Faction defender = MysqlGetterSetter.instance.getFaction(w.getDefenderId());
                    if(memberFaction == w.getAttackerId()) {
                        MysqlGetterSetter.instance.updateWarDeaths(w.getId(), w.getAttackerDeaths() + 1, w.getDefenderDeaths());
                        if(w.getAttackerDeaths() == 3 * MysqlGetterSetter.instance.getFactionMembers(w.getAttackerId()).size()) {
                            //End war
                            MysqlGetterSetter.instance.endWar(w.getId());
                            MessageUtils.smpBroadcast(String.format("%s won the war against %s.", defender.getName(), attacker.getName()));
                        }
                    } else if(memberFaction == w.getDefenderId()) {
                        MysqlGetterSetter.instance.updateWarDeaths(w.getId(), w.getAttackerDeaths(), w.getDefenderDeaths() + 1);
                        if(w.getDefenderDeaths() == 3 * MysqlGetterSetter.instance.getFactionMembers(w.getAttackerId()).size()) {
                            //End war
                            MysqlGetterSetter.instance.endWar(w.getId());
                            MessageUtils.smpBroadcast(String.format("%s won the war against %s.", attacker.getName(), defender.getName()));
                        }
                    }
                }
            }
        }
    }
}
