package cc.bytewithasix.smpfactions.cmd;

import cc.bytewithasix.smpfactions.Main;
import cc.bytewithasix.smpfactions.obj.War;
import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.obj.Faction;
import cc.bytewithasix.smpfactions.obj.Member;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandKick implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(!p.hasPermission("smpfactions.kick")) {
                sender.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.RED + "No permission.");
                return true;
            }

            if(args.length < 1) {
                MessageUtils.smpError(p, "You have not provided enough arguments. Correct usage: /fackick <UUID>");
                return true;
            }

            Member m = MysqlGetterSetter.instance.getMember(p.getUniqueId());
            if(m.getFactionId() == 0) {
                MessageUtils.smpError(p, "You are not in a faction.");
                return true;
            }

            if(!m.isLeader()) {
                MessageUtils.smpError(p, "You are not the leader of your faction.");
                return true;
            }

            Faction f = MysqlGetterSetter.instance.getFaction(m.getFactionId());
            if(f == null) {
                MessageUtils.smpError(p, "Your faction doesn't exist. This might be a bug.");
                return true;
            }

            War w = MysqlGetterSetter.instance.getWarByFaction(f.getId());
            if(w != null) {
                MessageUtils.smpError(p, "You can't kick someone during a war.");
                return true;
            }

            UUID kickPlayerUUID;
            try {
                kickPlayerUUID = UUID.fromString(args[0]);
            } catch(Exception e) {
                MessageUtils.smpError(p, "The argument you provided is not an UUID.");
                return true;
            }

            OfflinePlayer kickPlayer = Bukkit.getOfflinePlayer(kickPlayerUUID);

            if(kickPlayer.equals(p)) {
                MessageUtils.smpError(p, "You can't kick yourself.");
                return true;
            }

            if(kickPlayer != null) {
                Member kickMember = MysqlGetterSetter.instance.getMember(kickPlayer.getUniqueId());
                if(kickMember.sameFactionAs(f)) {
                    MysqlGetterSetter.instance.memberUpdateFaction(kickPlayer.getUniqueId(), 0, false);
                    MessageUtils.smpSuccess(p, "You have kicked the player with the UUID " + ChatColor.RED + kickPlayer.getUniqueId() + ChatColor.GREEN + " from your faction.");
                    if(kickPlayer.isOnline()) {
                        MessageUtils.smpError(kickPlayer.getPlayer(), "You were kicked from your faction.");
                    } else {
                        Main.instance.getOfflinePlayerKicks().add(kickPlayer);
                    }
                } else {
                    MessageUtils.smpError(p, "This player is not in your faction.");
                }
            } else {
                MessageUtils.smpError(p, "The player with the UUID " + args[0] + " doesn't exist.");
            }
            return true;
        }
        return true;
    }
}
