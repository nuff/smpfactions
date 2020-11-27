package cc.bytewithasix.smpfactions.cmd;

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
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "No permission.");
                return true;
            }

            if(args.length < 1) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You have not provided enough arguments. Correct usage: /facinvite <username>");
                return true;
            }

            Player transferPlayer = Bukkit.getPlayer(args[0]);
            if(transferPlayer != null && transferPlayer.equals(p)) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You can't transfer to yourself.");
                return true;
            }

            Member member = MysqlGetterSetter.instance.getMember(p.getUniqueId());
            Faction faction = MysqlGetterSetter.instance.getFaction(member.getFactionId());
            if(faction == null) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You are not in a faction.");
                return true;
            }

            if(!member.isLeader()) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You are not the leader of your faction.");
                return true;
            }

            Member transferMember = MysqlGetterSetter.instance.getMember(transferPlayer.getUniqueId());
            if(transferMember.sameFactionAs(member)) {
                MysqlGetterSetter.instance.memberUpdateFaction(p.getUniqueId(), member.getFactionId(), false);
                MysqlGetterSetter.instance.memberUpdateFaction(transferPlayer.getUniqueId(), member.getFactionId(), true);
                MysqlGetterSetter.instance.updateFactionOwner(faction.getId(), transferPlayer.getUniqueId());
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.DARK_RED + "The leadership of your faction has successfully been transferred.");
                transferPlayer.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.GREEN + "You are now the leader of " + ChatColor.DARK_GREEN + faction.getName() + ChatColor.GREEN + ".");
                return true;
            } else {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "This player is not a member of your faction.");
                return true;
            }
        }
        return true;
    }
}
