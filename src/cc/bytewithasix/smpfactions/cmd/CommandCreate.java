package cc.bytewithasix.smpfactions.cmd;

import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCreate implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(!p.hasPermission("smpfactions.create")) {
                MessageUtils.smpError(p, "No permission.");
                return true;
            }

            if(args.length < 2) {
                MessageUtils.smpError(p, "You have not provided enough arguments. Correct usage: /faccreate <democracy/monarchy> <name>");
                return true;
            }
            int govType;

            if(args[0].equalsIgnoreCase("democracy")) {
                govType = 0;
            } else if(args[0].equalsIgnoreCase("monarchy")) {
                govType = 1;
            } else {
                MessageUtils.smpError(p, "Please select a proper government type. Correct usage: /faccreate <democracy/monarchy> <name>");
                return true;
            }
            String name = StringUtils.join(args, ' ', 1, args.length);
            if(name.length() > 30) {
                MessageUtils.smpError(p, "Please choose a name with less than 31 characters.");
                return true;
            }

            if(MysqlGetterSetter.instance.factionExists(name)) {
                MessageUtils.smpError(p, "A faction with this name already exists.");
                return true;
            }

            int factionId = MysqlGetterSetter.instance.getMemberFactionId(p.getUniqueId());


            switch(factionId) {
                case 0:
                    //Create Faction
                    try {
                        int id = MysqlGetterSetter.instance.getNextFactionsAutoIncrement();
                        MysqlGetterSetter.instance.createFaction(name, govType, p.getUniqueId());
                        MysqlGetterSetter.instance.memberUpdateFaction(p.getUniqueId(), id, true);
                    } catch(Exception e) {
                        System.out.println(e);
                        MessageUtils.smpError(p, "An error occured, please try again later.");
                        return true;
                    }
                    MessageUtils.smpSuccess(p, "You have successfully created the faction " + ChatColor.DARK_GREEN + name + ChatColor.GREEN + " with the government type " + ChatColor.DARK_GREEN + args[0] + ChatColor.GREEN + ".");
                    break;
                default:
                    MessageUtils.smpError(p, "You are already in a faction.");
                    return true;
            }
        }
        return true;
    }
}
