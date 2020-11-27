package cc.bytewithasix.smpfactions.cmd;

import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.lib.SkullCreator;
import cc.bytewithasix.smpfactions.obj.Faction;
import cc.bytewithasix.smpfactions.obj.Member;
import cc.bytewithasix.smpfactions.utils.UserUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class CommandInfo implements CommandExecutor {
    private Inventory inv;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(!p.hasPermission("smpfactions.info")) {
                MessageUtils.smpError(p, "No permission.");
                return true;
            }
            Member m = MysqlGetterSetter.instance.getMember(p.getUniqueId());

            if(args.length == 0) {
                if(m.getFactionId() == 0) {
                    MessageUtils.smpError(p, "You are not in a faction.");
                    return true;
                }
                Faction faction = MysqlGetterSetter.instance.getFaction(m.getFactionId());
                inv = Bukkit.createInventory(null, 27, "Faction | " + faction.getName());
                initializeItems(faction.getName(), faction.getGovernmentTypeString(), UserUtils.getName(faction.getOwnerUUID()), faction.getOwnerUUID());
                p.openInventory(inv);
            } else {
                if(p.hasPermission("smpfactions.info.other")) {
                    String name = StringUtils.join(args, ' ', 0, args.length);
                    Faction faction = MysqlGetterSetter.instance.getFaction(name);
                    if (faction != null) {
                        inv = Bukkit.createInventory(null, 27, "Faction | " + faction.getName());
                        initializeItems(faction.getName(), faction.getGovernmentTypeString(), UserUtils.getName(faction.getOwnerUUID()), faction.getOwnerUUID());
                        p.openInventory(inv);
                    } else {
                        MessageUtils.smpError(p, "This faction doesn't exist.");
                    }
                } else {
                    MessageUtils.smpError(p, "No permission to access another faction's information.");
                    return true;
                }
            }
        }
        return true;
    }

    public void initializeItems(String name, String governmentType, String ownerName, String ownerUUID) {
        inv.setItem(11, createGuiItem(Material.NAME_TAG, ChatColor.RED + "Name", ChatColor.GRAY + name));
        ItemStack govItem;
        switch(governmentType) {
            case "democracy":
                govItem = createGuiItem(Material.BOOK, ChatColor.GRAY + "Democracy");
                break;
            case "monarchy":
                govItem = createGuiItem(Material.GOLDEN_HELMET, ChatColor.GOLD + "Monarchy");
                break;
            case "dictatorship":
                govItem = createGuiItem(Material.IRON_SWORD, ChatColor.DARK_RED + "Dictatorship");
                break;
            default:
                govItem = createGuiItem(Material.STONE, ChatColor.DARK_GRAY + "-");
                break;
        }
        inv.setItem(12, govItem);

        inv.setItem(14, createSkullItem(ChatColor.RED + "Owner", ownerUUID, ChatColor.GRAY + ownerName));
        inv.setItem(15, createGuiItem(Material.PLAYER_HEAD, ChatColor.DARK_AQUA + "Members", ChatColor.GRAY + "List all members of the faction."));

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack is = inv.getItem(i);
            if (is == null || is.getType() == Material.AIR) {
                inv.setItem(i, createGuiItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.GRAY + "-"));
            }
        }
    }

    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    protected ItemStack createSkullItem(final String name, final String skullOwnerUUID, final String... lore) {
        final ItemStack skull = SkullCreator.itemFromUuid(UUID.fromString(skullOwnerUUID));
        final ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        skull.setItemMeta(meta);
        return skull;
    }
}
