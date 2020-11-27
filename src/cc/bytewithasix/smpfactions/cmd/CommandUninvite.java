package cc.bytewithasix.smpfactions.cmd;

import cc.bytewithasix.smpfactions.Main;
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

import java.util.UUID;

public class CommandUninvite implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            UUID pUUID = p.getUniqueId();

            if(!p.hasPermission("smpfactions.uninvite")) {
                MessageUtils.smpError(p, "No permission.");
                return true;
            }

            if(args.length < 1) {
                MessageUtils.smpError(p, "You have not provided enough arguments. Correct usage: /facuninvite <username>");
                return true;
            }

            Player invPlayer = Bukkit.getPlayer(args[0]);
            if(invPlayer != null && invPlayer.equals(p)) {
                MessageUtils.smpError(p, "You can't uninvite yourself.");
                return true;
            }

            Member member = MysqlGetterSetter.instance.getMember(pUUID);
            Faction faction = null;
            if(member.getFactionId() != 0) {
                faction = MysqlGetterSetter.instance.getFaction(member.getFactionId());
                if(faction == null) {
                    MessageUtils.smpError(p, "The faction you are uninviting someone to doesn't exist. This is most likely a bug, please contact an administrator.");
                    return true;
                }
            } else {
                MessageUtils.smpError(p, "You are not in a faction.");
                return true;
            }

            if(!member.isLeader()) {
                MessageUtils.smpError(p, "You are not the leader of your faction.");
                return true;
            }

            if(invPlayer != null && invPlayer.isOnline()) {
                if(Main.instance.getFactionInv().containsKey(invPlayer)) {
                    Main.instance.getFactionInv().remove(invPlayer);
                    MessageUtils.smpSuccess(p, String.format("You have uninvited %s%s from your faction.", ChatColor.DARK_GREEN + invPlayer.getDisplayName(), ChatColor.GREEN));
                    MessageUtils.smpImportant(invPlayer, String.format("You have been uninvited from %s%s.", ChatColor.DARK_AQUA + faction.getName(), ChatColor.AQUA));
                    return true;
                } else {
                    MessageUtils.smpError(p, "This player is not invited.");
                }
            } else {
                MessageUtils.smpError(p, "The player " + args[0] + " is not online.");
                return true;
            }
        }

        return true;
    }
}
