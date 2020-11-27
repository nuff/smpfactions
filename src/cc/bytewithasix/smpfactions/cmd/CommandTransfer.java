package cc.bytewithasix.smpfactions.cmd;

import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.obj.Faction;
import cc.bytewithasix.smpfactions.obj.Member;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTransfer implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(!p.hasPermission("smpfactions.transfer")) {
                MessageUtils.smpError(p, "No permission.");
                return true;
            }

            if(args.length < 1) {
                MessageUtils.smpError(p, "You have not provided enough arguments. Correct usage: /facinvite <username>");
                return true;
            }

            Player transferPlayer = Bukkit.getPlayer(args[0]);
            if(transferPlayer != null && transferPlayer.equals(p)) {
                MessageUtils.smpError(p, "You can't transfer to yourself.");
                return true;
            }

            Member member = MysqlGetterSetter.instance.getMember(p.getUniqueId());
            Faction faction = MysqlGetterSetter.instance.getFaction(member.getFactionId());
            if(faction == null) {
                MessageUtils.smpError(p, "You are not in a faction.");
                return true;
            }

            if(!member.isLeader()) {
                MessageUtils.smpError(p, "You are not the leader of your faction.");
                return true;
            }

            Member transferMember = MysqlGetterSetter.instance.getMember(transferPlayer.getUniqueId());
            if(transferMember.sameFactionAs(member)) {
                MysqlGetterSetter.instance.memberUpdateFaction(p.getUniqueId(), member.getFactionId(), false);
                MysqlGetterSetter.instance.memberUpdateFaction(transferPlayer.getUniqueId(), member.getFactionId(), true);
                MysqlGetterSetter.instance.updateFactionOwner(faction.getId(), transferPlayer.getUniqueId());
                MessageUtils.smpSuccess(p, "The leadership of your faction has successfully been transferred.");
                MessageUtils.smpSuccess(transferPlayer, String.format("You are now the leader of %s%s.", ChatColor.DARK_GREEN + faction.getName(), ChatColor.GREEN));
                return true;
            } else {
                MessageUtils.smpError(p, "This player is not a member of your faction.");
                return true;
            }
        }
        return true;
    }
}
