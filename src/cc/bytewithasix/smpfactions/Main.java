package cc.bytewithasix.smpfactions;

import cc.bytewithasix.smpfactions.cmd.*;
import cc.bytewithasix.smpfactions.events.*;
import cc.bytewithasix.smpfactions.obj.War;
import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.obj.Boundaries;
import cc.bytewithasix.smpfactions.obj.Faction;
import javafx.scene.control.MultipleSelectionModel;
import net.minecraft.server.v1_16_R2.ChatComponentText;
import net.minecraft.server.v1_16_R2.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends JavaPlugin {

    public static Main instance;
    private HashMap<Player, Faction> factionInv = new HashMap<Player, Faction>();
    private ArrayList<OfflinePlayer> offlinePlayerKicks = new ArrayList<OfflinePlayer>();
    private ArrayList<Boundaries> factionBoundaries = new ArrayList<Boundaries>();
    private ArrayList<War> ongoingWars = new ArrayList<War>();
    //SQL
    private Connection connection;
    private String host, database, username, password;
    private int port;


    public Main() {
        this.instance = this;
    }

    @Override
    public void onEnable() {
        loadConfig();

        //SQL
        mysqlSetup();

        //Events
        this.getServer().getPluginManager().registerEvents(new MysqlGetterSetter(), this);
        this.getServer().getPluginManager().registerEvents(new ClickListener(), this);
        this.getServer().getPluginManager().registerEvents(new BoundaryListener(), this);
        this.getServer().getPluginManager().registerEvents(new ExplodeListener(), this);
        this.getServer().getPluginManager().registerEvents(new WarListener(), this);
        this.getServer().getPluginManager().registerEvents(new FactionListener(), this);
        this.getServer().getPluginManager().registerEvents(new MemberListener(), this);

        //Commands
        this.getCommand("faccreate").setExecutor(new CommandCreate());
        this.getCommand("facdelete").setExecutor(new CommandDelete());
        this.getCommand("facinfo").setExecutor(new CommandInfo());
        this.getCommand("facinvite").setExecutor(new CommandInvite());
        this.getCommand("facuninvite").setExecutor(new CommandUninvite());
        this.getCommand("fackick").setExecutor(new CommandKick());
        this.getCommand("factransfer").setExecutor(new CommandTransfer());
        this.getCommand("facboundaries").setExecutor(new CommandBoundaries());
        this.getCommand("facwar").setExecutor(new CommandWar());

        //Fill factionBoundaries array for the first time
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> updateFactionBoundaries());

        //Fill ongoingWars array for the first time
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> updateOngoingWars());

        //Continue grace periods in case server crashed, got restarted e.G.
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            ArrayList<War> allRunningWars = MysqlGetterSetter.instance.getAllWars();
            for(War w: allRunningWars) {
                if(w.getGrace() == 1) {
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("SMPFactions"), () -> {
                        try {
                            if (w.getGraceMinutes() > 1) {
                                MysqlGetterSetter.instance.updateWarGrace(w.getId(), 1, w.getGraceMinutes() - 1);
                            } else {
                                MysqlGetterSetter.instance.updateWarGrace(w.getId(), 0, w.getGraceMinutes() - 1);
                                Faction attacker = MysqlGetterSetter.instance.getFaction(w.getAttackerId());
                                Faction defender = MysqlGetterSetter.instance.getFaction(w.getDefenderId());
                                MessageUtils.smpBroadcast(String.format("The grace period of the war between %s and %s had ended.", attacker.getName(), defender.getName()));
                            }
                        } catch(Exception e) {}
                    }, 1L , (long) 60 * 20);
                }
            }
        });

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean headerEnabled = false;
                boolean footerEnabled = false;
                String headerString = "";
                String footerString = "";

                if(getConfig().getBoolean("showServerName")) {
                    headerEnabled = true;
                    headerString += getConfig().getString("tablist.serverName");
                }

                if(getConfig().getBoolean("showOnlinePlayers")) {
                    headerEnabled = true;
                    headerString += "\n";
                    headerString += "§aOnline: §f" + Bukkit.getOnlinePlayers().size();
                    if(getConfig().getBoolean("showIngameTime")) headerString += "§7| ";
                }

                if(getConfig().getBoolean("showIngameTime")) {
                    headerEnabled = true;
                    int hours = (int) Bukkit.getWorld("world").getTime() / 1000;
                    int minutes = Math.round(Bukkit.getWorld("world").getTime() % (1000 / 60));
                    String time = (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes;
                    headerString += "§eIngame time: §f" + time;
                }

                if(getConfig().getBoolean("showOngoingWars")) {
                    footerString += "\n§bOngoing wars§f:\n";
                    if(ongoingWars.isEmpty()) {
                        footerString += "§eNone!";
                    } else {
                        for(War w: ongoingWars) {
                            Faction att = MysqlGetterSetter.instance.getFaction(w.getAttackerId());
                            Faction def = MysqlGetterSetter.instance.getFaction(w.getDefenderId());
                            footerString += String.format("§c%s §fvs. §a%s §f- §c%s§f:§a%s\n", att.getName(), def.getName(), w.getDefenderDeaths(), w.getAttackerDeaths());
                        }
                    }
                }
                if(headerEnabled) headerString += "\n";

                Object header = new ChatComponentText(headerString);
                Object footer = new ChatComponentText(footerString);
                try {
                    Field a = packet.getClass().getDeclaredField("a");
                    a.setAccessible(true);
                    Field b = packet.getClass().getDeclaredField("b");
                    b.setAccessible(true);

                    a.set(packet, header);
                    b.set(packet, footer);

                    if(Bukkit.getOnlinePlayers().size() == 0) return;
                    for(Player p: Bukkit.getOnlinePlayers()) {
                        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    public HashMap<Player, Faction> getFactionInv() {
        return factionInv;
    }
    public ArrayList<OfflinePlayer> getOfflinePlayerKicks() {
        return offlinePlayerKicks;
    }
    public ArrayList<Boundaries> getFactionBoundaries() { return factionBoundaries; }

    public void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void mysqlSetup() {
        host = getConfig().getString("database.host");
        port = getConfig().getInt("database.port");
        database = getConfig().getString("database.database");
        username = getConfig().getString("database.user");
        password = getConfig().getString("database.password");

        try {
            synchronized (this) {
                if (getConnection() != null && !getConnection().isClosed()) return;
                Class.forName("com.mysql.jdbc.Driver");
                setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateFactionBoundaries() {
        factionBoundaries.clear();
        ArrayList<Boundaries> b = MysqlGetterSetter.instance.getAllBoundaries();
        if(b != null) {
            for(Boundaries i: b) {
                factionBoundaries.add(i);
            }
        }
    }

    public void updateOngoingWars() {
        ongoingWars.clear();
        ArrayList<War> w = MysqlGetterSetter.instance.getAllWars();
        if(w != null) {
            for(War i: w) {
                ongoingWars.add(i);
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}