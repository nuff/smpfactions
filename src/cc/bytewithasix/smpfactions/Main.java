package cc.bytewithasix.smpfactions;

import cc.bytewithasix.smpfactions.cmd.*;
import cc.bytewithasix.smpfactions.events.*;
import cc.bytewithasix.smpfactions.obj.War;
import cc.bytewithasix.smpfactions.utils.MessageUtils;
import cc.bytewithasix.smpfactions.utils.MysqlGetterSetter;
import cc.bytewithasix.smpfactions.obj.Boundaries;
import cc.bytewithasix.smpfactions.obj.Faction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sun.awt.Win32GraphicsConfig;

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
        this.getServer().getPluginManager().registerEvents(new ClickEvent(), this);
        this.getServer().getPluginManager().registerEvents(new BoundaryEvents(), this);
        this.getServer().getPluginManager().registerEvents(new ExplodeEvent(), this);
        this.getServer().getPluginManager().registerEvents(new WarEvents(), this);
        this.getServer().getPluginManager().registerEvents(new FactionEvents(), this);

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

        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> updateFactionBoundaries());

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

    public Connection getConnection() {
        return connection;
    }
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
