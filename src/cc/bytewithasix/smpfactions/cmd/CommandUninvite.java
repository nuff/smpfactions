package cc.bytewithasix.smpfactions.cmd;

import cc.bytewithasix.smpfactions.Main;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.obj.Faction;
import cc.bytewithasix.smpfactions.obj.Member;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandUninvite implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            UUID pUUID = p.getUniqueId();

            if(!p.hasPermission("smpfactions.uninvite")) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "No permission!");
                return true;
            }

            if(args.length < 1) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You have not provided enough arguments. Correct usage: /facuninvite <username>");
                return true;
            }

            Player invPlayer = Bukkit.getPlayer(args[0]);
            if(invPlayer != null && invPlayer.equals(p)) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You can't uninvite yourself.");
                return true;
            }

            Member member = MysqlGetterSetter.instance.getMember(pUUID);
            Faction faction = null;
            if(member.getFactionId() != 0) {
                faction = MysqlGetterSetter.instance.getFaction(member.getFactionId());
                if(faction == null) {
                    sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "The faction you are uninviting someone to doesn't exist. This is most likely a bug, please contact an administrator.");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You are not in a faction.");
                return true;
            }

            if(!member.isLeader()) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You are not the leader of your faction.");
                return true;
            }

            if(invPlayer != null && invPlayer.isOnline()) {
                if(Main.instance.getFactionInv().containsKey(invPlayer)) {
                    Main.instance.getFactionInv().remove(invPlayer);
                    sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.GREEN + "You have uninvited " + ChatColor.DARK_GREEN + invPlayer.getDisplayName() + ChatColor.GREEN + " from your faction.");
                    invPlayer.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.AQUA + "You have been uninvited from " + ChatColor.DARK_AQUA + faction.getName() + ChatColor.AQUA + ".");
                    return true;
                } else {
                    p.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "This player is not invited.");
                }
            } else {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "The player " + args[0] + " is not online.");
                return true;
            }
        }

        return true;
    }
}
