package cc.bytewithasix.smpfactions.cmd;

import cc.bytewithasix.smpfactions.Main;
import cc.bytewithasix.smpfactions.obj.Faction;
import cc.bytewithasix.smpfactions.obj.Member;
import cc.bytewithasix.smpfactions.obj.War;
import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.utils.UserUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandWar implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            Member m = MysqlGetterSetter.instance.getMember(p.getUniqueId());

            if(!p.hasPermission("smpfactions.war")) {
                MessageUtils.smpError(p, "No permission.");
                return true;
            }

            if(args.length < 1) {
                if(!p.hasPermission("smfactions.admin")) {
                    MessageUtils.smpError(p, "You have not provided enough arguments. Correct usage: /facwar declare <factionName>");
                } else {
                    MessageUtils.smpAdminError(p, "You have not provided enough arguments. Correct usage: /facwar <declare [factionName]/enable/disable>");
                }
            }

            switch(args[0]) {
                case "declare":
                    if(m.getFactionId() == 0) {
                        MessageUtils.smpError(p, "You are not in a faction.");
                        return true;
                    }

                    if(!Main.instance.getConfig().getBoolean("wars.enabled")) {
                        MessageUtils.smpError(p, "Wars are disabled.");
                        return true;
                    }

                    if(!m.isLeader()) {
                        MessageUtils.smpError(p, "You are not your faction's leader.");
                        return true;
                    }

                    Faction attacker = MysqlGetterSetter.instance.getFaction(m.getFactionId());
                    if(MysqlGetterSetter.instance.getFactionMembers(attacker.getId()).size() < Main.instance.getConfig().getInt("membersOnlineToStartWar")) {
                        MessageUtils.smpError(p, "You don't have enough members to start a war.");
                        return true;
                    }

                    if(args.length < 2) {
                        MessageUtils.smpError(p, "You have not provided enough arguments. Correct usage: /facwar declare <factionName>");
                        return true;
                    }

                    Faction defender = MysqlGetterSetter.instance.getFaction(args[1]);
                    if(defender == null) {
                        MessageUtils.smpError(p, "That faction doesn't exist.");
                        return true;
                    }

                    if(MysqlGetterSetter.instance.getFactionMembers(defender.getId()).size() < Main.instance.getConfig().getInt("membersOnlineToStartWar")) {
                        MessageUtils.smpError(p, "This faction doesn't have enough members for a war.");
                        return true;
                    }

                    if(UserUtils.countOnlineFactionMembers(defender.getId()) < Main.instance.getConfig().getInt("membersOnlineToStartWar")) {
                        MessageUtils.smpError(p, String.format("This faction doesn't have enough members online a war (>%s).", Main.instance.getConfig().getInt("membersOnlineToStartWar")));
                        return true;
                    }

                    if(MysqlGetterSetter.instance.warRunningByAttacker(attacker.getId())) {
                        MessageUtils.smpError(p, "Your faction already has a war running.");
                        return true;
                    }

                    if(MysqlGetterSetter.instance.warRunningByDefender(defender.getId())) {
                        MessageUtils.smpError(p, String.format("The defender %s already has a war running.", defender.getName()));
                        return true;
                    }

                    MysqlGetterSetter.instance.createWar(attacker.getId(), defender.getId());
                    MessageUtils.smpBroadcast(String.format("%s has declared war on %s.", attacker.getName(), defender.getName()));

                    War w = MysqlGetterSetter.instance.getWarByFaction(attacker.getId());
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("SMPFactions"), () -> {
                        try {
                            if (w.getGraceMinutes() > 1) {
                                MysqlGetterSetter.instance.updateWarGrace(w.getId(), 1, w.getGraceMinutes() - 1);
                            } else {
                                MysqlGetterSetter.instance.updateWarGrace(w.getId(), 0, w.getGraceMinutes() - 1);
                                MessageUtils.smpBroadcast(String.format("The grace period of the war between %s and %s had ended.", attacker.getName(), defender.getName()));
                            }
                        } catch(Exception e) {}
                    }, 1L , (long) 60 * 20);
                    break;
                case "enable":
                    if(p.hasPermission("smfactions.admin")) {
                        if (!Main.instance.getConfig().getBoolean("wars.enabled")) {
                            Main.instance.getConfig().set("wars.enabled", true);
                        } else {
                            MessageUtils.smpAdminError(p, "Wars are already enabled.");
                            return true;
                        }
                    } else {
                        MessageUtils.smpError(p, "No permission.");
                    }
                    break;
                case "disable":
                    if(p.hasPermission("smfactions.admin")) {
                        if (Main.instance.getConfig().getBoolean("wars.enabled")) {
                            Main.instance.getConfig().set("wars.enabled", false);
                        } else {
                            MessageUtils.smpAdminError(p, "Wars are already disabled.");
                            return true;
                        }
                    } else {
                        MessageUtils.smpError(p, "No permission.");
                    }
                    break;
            }
        }
        return false;
    }
}
