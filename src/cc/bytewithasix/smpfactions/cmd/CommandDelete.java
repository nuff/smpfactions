package cc.bytewithasix.smpfactions.cmd;

import cc.bytewithasix.smpfactions.Main;
import cc.bytewithasix.smpfactions.obj.War;
import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.obj.Faction;
import cc.bytewithasix.smpfactions.obj.Member;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class CommandDelete implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(!p.hasPermission("smpfactions.delete")) {
                MessageUtils.smpError(p, "No permission.");
                return true;
            }

            if(args.length < 1) {
                MessageUtils.smpError(p, "You have not provided enough arguments. Correct usage: /facdelete <name>");
                return true;
            }

            Faction faction;
            if(!p.hasPermission("smpfactions.admin")) {
                Member m = MysqlGetterSetter.instance.getMember(p.getUniqueId());
                if(m.getFactionId() == 0) {
                    MessageUtils.smpError(p, "You are not in a faction.");
                    return true;
                }
                if(!m.isLeader()) {
                    MessageUtils.smpError(p, "You are not the leader of this faction.");
                    return true;
                }
                faction = MysqlGetterSetter.instance.getFaction(m.getFactionId());

                if(MysqlGetterSetter.instance.warRunningByAttacker(faction.getId()) || MysqlGetterSetter.instance.warRunningByDefender(faction.getId())) {
                    MessageUtils.smpError(p, "Can't delete your faction during a war.");
                    return true;
                }
            } else {
                faction = MysqlGetterSetter.instance.getFaction(args[0]);
                if(faction == null) {
                    MessageUtils.smpError(p, "This faction doesn't exist.");
                    return true;
                }
            }


            String name = StringUtils.join(args, ' ', 0, args.length);
            ArrayList<Member> members = MysqlGetterSetter.instance.getFactionMembers(faction.getId());
            for(Member m: members) {
                MysqlGetterSetter.instance.memberUpdateFaction(UUID.fromString(m.getUUID()), 0, false);
            }
            War war = MysqlGetterSetter.instance.getWarByFaction(faction.getId());
            if(war != null) {
                MysqlGetterSetter.instance.endWar(war.getId());
            }
            MysqlGetterSetter.instance.deleteFaction(faction.getId());
            MysqlGetterSetter.instance.deleteBoundaries(faction.getId());
            Main.instance.updateFactionBoundaries();
            MessageUtils.smpSuccess(p, String.format("You have successfully deleted the faction %s%s.", ChatColor.DARK_GREEN + name, ChatColor.GREEN));
        }
        return true;
    }
}
