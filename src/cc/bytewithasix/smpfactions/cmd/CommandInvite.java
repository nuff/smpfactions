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

public class CommandInvite implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            if(args.length < 1) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You have not provided enough arguments. Correct usage: /facinvite <username>");
                return true;
            }

            Player p = (Player) sender;
            UUID pUUID = p.getUniqueId();

            if(args[0].equalsIgnoreCase("accept")) {
                if (Main.instance.getFactionInv().containsKey(p)) {
                    Faction fac = Main.instance.getFactionInv().get(p);
                    if (fac != null) {
                        Member m = MysqlGetterSetter.instance.getMember(pUUID);
                        if(MysqlGetterSetter.instance.getFactionMembers(m.getFactionId()).size() < Main.instance.getConfig().getInt("factions.maxFactionSize")) {
                            MysqlGetterSetter.instance.memberUpdateFaction(pUUID, fac.getId(), false);
                            Bukkit.broadcastMessage(ChatColor.GOLD + "[SMPFactions News] " + ChatColor.DARK_PURPLE + p.getDisplayName() + ChatColor.AQUA + " has joined the faction " + ChatColor.DARK_PURPLE + fac.getName());
                            Main.instance.getFactionInv().remove(p);
                            sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.GREEN + "You have successfully joined the faction " + ChatColor.DARK_GREEN + fac.getName() + ChatColor.GREEN + ".");
                        } else {
                            Main.instance.getFactionInv().remove(p);
                            sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "The faction you were invited to is already full.");
                        }
                        return true;
                    }
                    return true;
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You have not been invited to a faction.");
                    return true;
                }
            }

            if(args[0].equalsIgnoreCase("deny")) {
                if(Main.instance.getFactionInv().containsKey(p)) {
                    Faction fac = Main.instance.getFactionInv().get(p);
                    if(fac != null) {
                        Main.instance.getFactionInv().remove(p);
                        sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.DARK_RED + "You have declined to join the faction " + ChatColor.RED + fac.getName() + ChatColor.DARK_RED + ".");
                        return true;
                    }
                    return true;
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You have not been invited to a faction.");
                    return true;
                }
            }

            if(!p.hasPermission("smpfactions.invite")) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "No permission.");
                return true;
            }

            Player invPlayer = Bukkit.getPlayer(args[0]);
            if(invPlayer != null && invPlayer.equals(p)) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "You can't invite yourself.");
                return true;
            }

            Member member = MysqlGetterSetter.instance.getMember(pUUID);
            Faction faction;
            if(member.getFactionId() != 0) {
                faction = MysqlGetterSetter.instance.getFaction(member.getFactionId());
                if(faction == null) {
                    sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "The faction you are inviting someone to doesn't exist. This is most likely a bug, please contact an administrator.");
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

            if(!(MysqlGetterSetter.instance.getFactionMembers(member.getFactionId()).size() < Main.instance.getConfig().getInt("factions.maxFactionSize"))) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "Your faction is full (" + Main.getPlugin(Main.class).getConfig().getInt("factions.maxFactionSize") + " members).");
                return true;
            }

            if(invPlayer != null && invPlayer.isOnline()) {
                Member invMember = MysqlGetterSetter.instance.getMember(invPlayer.getUniqueId());
                if(invMember.getFactionId() == 0) {
                    if (!Main.instance.getFactionInv().containsKey(invPlayer)) {
                        Main.instance.getFactionInv().put(invPlayer, faction);
                        sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.GREEN + "You have invited " + ChatColor.DARK_GREEN + invPlayer.getDisplayName() + ChatColor.GREEN + " to your faction.");
                        invPlayer.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.AQUA + "You have been invited to the faction " + ChatColor.DARK_AQUA + faction.getName() + ChatColor.AQUA + " by " + ChatColor.DARK_AQUA + p.getName() + ChatColor.AQUA + ".");
                        return true;
                    } else {
                        p.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "This player already has a pending invite.");
                    }
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "This player is already in a faction!");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "The player " + args[0] + " is not online.");
                return true;
            }
        }
        return true;
    }
}
