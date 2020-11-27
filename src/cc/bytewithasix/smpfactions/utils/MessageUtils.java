package cc.bytewithasix.smpfactions.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtils {

    public static void smpError(Player p, String s) {
        p.sendMessage(String.format("%s[SMPFactions] %s%s", ChatColor.GOLD, ChatColor.RED, s));
    }

    public static void smpSuccess(Player p, String s) {
        p.sendMessage(String.format("%s[SMPFactions] %s%s", ChatColor.GOLD, ChatColor.GREEN, s));
    }

    public static void smpAdminError(Player p, String s) {
        p.sendMessage(String.format("%s[SMPFactions Admin] %s%s", ChatColor.GOLD, ChatColor.RED, s));
    }

    public static void smpAdminSuccess(Player p, String s) {
        p.sendMessage(String.format("%s[SMPFactions Admin] %s%s", ChatColor.GOLD, ChatColor.GREEN, s));
    }

    public static void smpBroadcast(String s) {
        Bukkit.broadcastMessage(String.format("%s[SMPFactions News] %s%s", ChatColor.GOLD, ChatColor.DARK_PURPLE, s));
    }
}
