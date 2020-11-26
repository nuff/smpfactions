package cc.bytewithasix.smpfactions.cmd;

import cc.bytewithasix.smpfactions.Main;
import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.obj.Boundaries;
import cc.bytewithasix.smpfactions.obj.Coords2D;
import cc.bytewithasix.smpfactions.obj.Member;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBoundaries implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            Member member = MysqlGetterSetter.instance.getMember(p.getUniqueId());

            if(!p.hasPermission("smpfactions.boundaries")) {
                MessageUtils.smpError(p, "No permission.");
                return true;
            }

            if(member.getFactionId() == 0) {
                MessageUtils.smpError(p, "You are not in a faction.");
                return true;
            }

            if(!member.isLeader()) {
                MessageUtils.smpError(p, "You are not your faction's leader.");
                return true;
            }

            if(MysqlGetterSetter.instance.getFactionMembers(member.getFactionId()).size() < 3) {
                MessageUtils.smpError(p, "Your faction has not enough members (less than 3) to be able to set boundaries.");
                return true;
            }

            if(args.length < 1) {
                MessageUtils.smpError(p, "You have not provided enough arguments. Correct usage: /facboundaries <setFirst/setSecond>");
                return true;
            }


            int chunkAmount = MysqlGetterSetter.instance.getFactionMembers(member.getFactionId()).size() * Main.getPlugin(Main.class).getConfig().getInt("factions.perMemberChunks");
            int blockAmount = 256 * chunkAmount;

            switch(args[0]) {
                case "setFirst":
                    int x1 = p.getLocation().getBlockX();
                    int z1 = p.getLocation().getBlockZ();

                    Boundaries boundaries = MysqlGetterSetter.instance.getBoundaries(member.getFactionId());
                    if(boundaries == null) {
                        MysqlGetterSetter.instance.addBoundaries(member.getFactionId(), new Coords2D(x1, z1), null);
                        MessageUtils.smpSuccess(p, "You have set the first XZ coordinate set of your faction's boundaries to X " + ChatColor.DARK_GREEN + String.valueOf(x1) + ChatColor.GREEN + " and Z " + ChatColor.DARK_GREEN + String.valueOf(z1) + ChatColor.GREEN + ".");
                    } else if(boundaries.getCoordsTwo() == null) {
                        MysqlGetterSetter.instance.updateBoundariesFirst(member.getFactionId(), new Coords2D(x1, z1));
                        MessageUtils.smpSuccess(p, "You have set the first XZ coordinate set of your faction's boundaries to X " + ChatColor.DARK_GREEN + String.valueOf(x1) + ChatColor.GREEN + " and Z " + ChatColor.DARK_GREEN + String.valueOf(z1) + ChatColor.GREEN + ".");
                    } else {
                        Coords2D two = boundaries.getCoordsTwo();
                        int blocksInNewRange = Math.abs((two.getX() - x1) * (two.getZ() - z1));
                        double rangeChunks = Math.round((blocksInNewRange/256) * 100.0) / 100.0;
                        if(blocksInNewRange <= blockAmount) {
                            MysqlGetterSetter.instance.updateBoundariesFirst(member.getFactionId(), new Coords2D(x1, z1));
                            MessageUtils.smpSuccess(p, "You have set the first XZ coordinate set of your faction's boundaries to X " + ChatColor.DARK_GREEN + String.valueOf(x1) + ChatColor.GREEN + " and Z " + ChatColor.DARK_GREEN + String.valueOf(z1) + ChatColor.GREEN + ". You have " + ChatColor.DARK_GREEN + String.valueOf(blockAmount - blocksInNewRange) + ChatColor.GREEN + " blocks left.");
                        } else {
                            MessageUtils.smpError(p, "You are trying to claim " + ChatColor.DARK_RED + String.valueOf(rangeChunks) + ChatColor.RED + " chunks while your faction only has " + ChatColor.DARK_RED + String.valueOf(chunkAmount) + ChatColor.RED + " chunks available.");
                            return true;
                        }
                    }
                    Main.instance.updateFactionBoundaries();
                    break;
                case "setSecond":
                    int x2 = p.getLocation().getBlockX();
                    int z2 = p.getLocation().getBlockZ();

                    Boundaries boundaries2 = MysqlGetterSetter.instance.getBoundaries(member.getFactionId());
                    if(boundaries2 == null) {
                        MysqlGetterSetter.instance.addBoundaries(member.getFactionId(), null, new Coords2D(x2, z2));
                        MessageUtils.smpSuccess(p, "You have set the second XZ coordinate set of your faction's boundaries to X " + ChatColor.DARK_GREEN + String.valueOf(x2) + ChatColor.GREEN + " and Z " + ChatColor.DARK_GREEN + String.valueOf(z2) + ChatColor.GREEN + ".");
                    } else if(boundaries2.getCoordsTwo() == null) {
                        MysqlGetterSetter.instance.updateBoundariesSecond(member.getFactionId(), new Coords2D(x2, z2));
                        MessageUtils.smpSuccess(p, "You have set the second XZ coordinate set of your faction's boundaries to X " + ChatColor.DARK_GREEN + String.valueOf(x2) + ChatColor.GREEN + " and Z " + ChatColor.DARK_GREEN + String.valueOf(z2) + ChatColor.GREEN + ".");
                    } else {
                        Coords2D one = boundaries2.getCoordsOne();
                        int blocksInNewRange = Math.abs((x2 - one.getX()) * (z2 - one.getZ()));
                        double rangeChunks = Math.round((blocksInNewRange/256) * 100.0) / 100.0;
                        if(blocksInNewRange <= blockAmount) {
                            MysqlGetterSetter.instance.updateBoundariesSecond(member.getFactionId(), new Coords2D(x2, z2));
                            MessageUtils.smpSuccess(p, "You have set the second XZ coordinate set of your faction's boundaries to X " + ChatColor.DARK_GREEN + String.valueOf(x2) + ChatColor.GREEN + " and Z " + ChatColor.DARK_GREEN + String.valueOf(z2) + ChatColor.GREEN + ". You have " + ChatColor.DARK_GREEN + String.valueOf(blockAmount - blocksInNewRange) + ChatColor.GREEN + " blocks left.");
                        } else {
                            MessageUtils.smpError(p, "You are trying to claim " + ChatColor.DARK_RED + String.valueOf(rangeChunks) + ChatColor.RED + " chunks while your faction only has " + ChatColor.DARK_RED + String.valueOf(chunkAmount) + ChatColor.RED + " chunks available.");
                            return true;
                        }
                    }
                    Main.instance.updateFactionBoundaries();
                    break;
                case "delete":
                    MysqlGetterSetter.instance.deleteBoundaries(member.getFactionId());
                    MessageUtils.smpSuccess(p, "You have deleted the boundaries of your faction.");
                    Main.instance.updateFactionBoundaries();
                    break;
                case "info":
                    Boundaries b = MysqlGetterSetter.instance.getBoundaries(member.getFactionId());
                    if(b != null) {
                        String firstCoords = "", secondCoords = "";
                        if(b.getCoordsOne() != null) {
                            firstCoords = String.format("X %s | Z %s", b.getCoordsOne().getX(), b.getCoordsOne().getZ());
                        } else {
                            firstCoords = "Not set";
                        }

                        if(b.getCoordsTwo() != null) {
                            secondCoords = String.format("X %s | Z %s", b.getCoordsTwo().getX(), b.getCoordsTwo().getZ());
                        } else {
                            secondCoords = "Not set";
                        }

                        p.sendMessage(ChatColor.GRAY + "===============");
                        p.sendMessage(String.format("%sFirst%s: %s", ChatColor.GOLD, ChatColor.WHITE, firstCoords));
                        p.sendMessage(String.format("%sSecond%s: %s", ChatColor.GOLD, ChatColor.WHITE, secondCoords));
                        p.sendMessage(ChatColor.GRAY + "===============");
                    } else {
                        MessageUtils.smpError(p, "Your faction has no boundaries yet.");
                    }
                    break;
                default: MessageUtils.smpError(p, "You have not provided incorrect arguments. Correct usage: /facboundaries <setFirst/setSecond/delete/info>");
            }
        }
        return true;
    }
}