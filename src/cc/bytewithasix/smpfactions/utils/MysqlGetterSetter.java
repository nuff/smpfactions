package cc.bytewithasix.smpfactions.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import cc.bytewithasix.smpfactions.Main;
import cc.bytewithasix.smpfactions.obj.*;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MysqlGetterSetter implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!MysqlGetterSetter.instance.createMember(player.getUniqueId(), player)) {
            player.kickPlayer(ChatColor.RED + "[SMPFactions] An error occured while entering you into our database. Please try reconnecting or contact an administrator!");
        }

        for(OfflinePlayer p: Main.instance.getOfflinePlayerKicks()) {
            if(p.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.GOLD + "[SMPFactions] " + ChatColor.DARK_RED + "You were kicked from your faction.");
                Main.instance.getOfflinePlayerKicks().remove(p);
            }
        }
    }

    Main plugin = Main.getPlugin(Main.class);

    public static MysqlGetterSetter instance;
    public MysqlGetterSetter() { this.instance = this; }

    public boolean memberExists(final UUID uuid) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM `member` WHERE UUID=?");
            statement.setString(1, uuid.toString());

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createMember(final UUID uuid, Player player) {
        try {
            if (!memberExists(uuid)) {
                PreparedStatement insert = plugin.getConnection()
                        .prepareStatement("INSERT INTO `member` (UUID,factionId,leader) VALUES (?,?,?)");
                insert.setString(1, uuid.toString());
                insert.setInt(2, 0);
                insert.setInt(3, 0);
                insert.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteMember(final UUID uuid) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("DELETE FROM `member` WHERE UUID=?");
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Member getMember(final UUID uuid) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM `member` WHERE UUID=?");
            statement.setString(1, uuid.toString());

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                Member member = new Member(uuid.toString(), results.getInt("factionId"), results.getInt("leader"));
                return member;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean memberUpdateFaction(UUID uuid, int factionId, boolean leader) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("UPDATE `member` SET factionId=?,leader=? WHERE UUID=?");
            statement.setInt(1, factionId);
            if(leader) {
                statement.setInt(2, 1);
            } else {
                statement.setInt(2, 0);
            }
            statement.setString(3, uuid.toString());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getMemberFactionId(final UUID uuid) {
        Member m = this.getMember(uuid);
        if(m == null) {
            return m.getFactionId();
        } else {
            return 0;
        }
    }

    public boolean factionExists(int id) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM `factions` WHERE id=?");
            statement.setInt(1, id);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean factionExists(String name) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM `factions` WHERE name=?");
            statement.setString(1, name);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createFaction(String name, int govType, UUID ownerUUID) {
        try {
            if (!factionExists(name)) {
                PreparedStatement insert = plugin.getConnection()
                        .prepareStatement("INSERT INTO factions(id, name, governmentType, ownerUUID) VALUES (NULL, ?, ?, ?)");
                insert.setString(1, name);
                insert.setInt(2, govType);
                insert.setString(3, ownerUUID.toString());
                insert.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteFaction(int id) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("DELETE FROM `factions` WHERE id=?");
            statement.setInt(1, id);
            statement.executeUpdate();
            deleteBoundaries(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Faction getFaction(int id) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM `factions` WHERE id=?");
            statement.setInt(1, id);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                Faction faction = new Faction(id, results.getString("name"), results.getInt("governmentType"), results.getString("ownerUUID"));
                return faction;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Faction getFaction(String name) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM `factions` WHERE name=?");
            statement.setString(1, name);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                Faction faction = new Faction(results.getInt("id"), name, results.getInt("governmentType"), results.getString("ownerUUID"));
                return faction;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateFactionGoverment(int id, int govType) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("UPDATE factions SET governmentType=? WHERE id=?");
            statement.setInt(1, govType);
            statement.setInt(2, id);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateFactionOwner(int id, UUID ownerUUID) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("UPDATE factions SET ownerUUID=? WHERE id=?");
            statement.setString(1, ownerUUID.toString());
            statement.setInt(2, id);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Member> getFactionMembers(int id) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM member WHERE factionId = ?");
            statement.setInt(1, id);

            ResultSet results = statement.executeQuery();
            ArrayList<Member> members = new ArrayList<Member>();
            while(results.next()) {
                members.add(new Member(results.getString("UUID"), results.getInt("factionId"), results.getInt("leader")));
            }
            return members;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getNextFactionsAutoIncrement() {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareCall("SELECT `AUTO_INCREMENT` FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'factions'");
            ResultSet results = statement.executeQuery();
            if(results.next()) {
                return results.getInt("AUTO_INCREMENT");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean boundariesExist(int factionId) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM `boundaries` WHERE factionId=?");
            statement.setInt(1, factionId);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addBoundaries(int factionId, Coords2D coordsOne, Coords2D coordsTwo) {
        try {
            if(!boundariesExist(factionId)) {
                PreparedStatement insert = plugin.getConnection()
                        .prepareStatement("INSERT INTO `boundaries`(factionId, coordsFirst, coordsSecond) VALUES (?,?,?)");
                insert.setInt(1, factionId);
                if(coordsOne != null) {
                    insert.setString(2, coordsOne.getX() + "|" + coordsOne.getZ());
                } else {
                    insert.setString(2, null);
                }
                if(coordsTwo != null) {
                    insert.setString(3, coordsTwo.getX() + "|" + coordsTwo.getZ());
                } else {
                    insert.setString(3, null);
                }
                insert.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBoundaries(int factionId) {
        try {
            if(boundariesExist(factionId)) {
                PreparedStatement statement = plugin.getConnection()
                        .prepareStatement("DELETE FROM `boundaries` WHERE factionId=?");
                statement.setInt(1, factionId);
                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boundaries getBoundaries(int factionId) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM `boundaries` WHERE factionId=?");
            statement.setInt(1, factionId);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                Coords2D one = null, two = null;
                if(results.getString("coordsFirst") != null) {
                    String[] first = results.getString("coordsFirst").split("\\|");
                    one = new Coords2D(Integer.parseInt(first[0]), Integer.parseInt(first[1]));
                }
                if(results.getString("coordsSecond") != null) {
                    String[] second = results.getString("coordsSecond").split("\\|");
                    two = new Coords2D(Integer.parseInt(second[0]), Integer.parseInt(second[1]));
                }
                Boundaries boundaries = new Boundaries(factionId, one, two);
                return boundaries;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateBoundariesFirst(int factionId, Coords2D coordsOne) {
        try {
            PreparedStatement update = plugin.getConnection()
                    .prepareStatement("UPDATE boundaries SET coordsFirst=? WHERE factionId=?");
            update.setString(1, coordsOne.getX() + "|" + coordsOne.getZ());
            update.setInt(2, factionId);
            update.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateBoundariesSecond(int factionId, Coords2D coordsTwo) {
        try {
            PreparedStatement update = plugin.getConnection()
                    .prepareStatement("UPDATE boundaries SET coordsSecond=? WHERE factionId=?");
            update.setString(1, coordsTwo.getX() + "|" + coordsTwo.getZ());
            update.setInt(2, factionId);
            update.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateBoundaries(int factionId, Coords2D coordsOne, Coords2D coordsTwo) {
        try {
            PreparedStatement update = plugin.getConnection()
                    .prepareStatement("UPDATE boundaries SET coordsFirst=?, coordsSecond=? WHERE factionId=?");
            update.setString(1, coordsOne.getX() + "|" + coordsOne.getZ());
            update.setString(2, coordsTwo.getX() + "|" + coordsTwo.getZ());
            update.setInt(3, factionId);
            update.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Boundaries> getAllBoundaries() {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM boundaries WHERE 1");
            ResultSet results = statement.executeQuery();
            ArrayList<Boundaries> b = new ArrayList<Boundaries>();
            while(results.next()) {
                Coords2D one = null, two = null;
                if(results.getString("coordsFirst") != null) {
                    String[] first = results.getString("coordsFirst").split("\\|");
                    one = new Coords2D(Integer.parseInt(first[0]), Integer.parseInt(first[1]));
                }
                if(results.getString("coordsSecond") != null) {
                    String[] second = results.getString("coordsSecond").split("\\|");
                    two = new Coords2D(Integer.parseInt(second[0]), Integer.parseInt(second[1]));
                }
                if(one != null && two != null) {
                    Boundaries temp = new Boundaries(results.getInt("factionId"), one, two);
                    b.add(temp);
                }
            }
            return b;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean warRunningById(int id) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM wars WHERE id=?");
            statement.setInt(1, id);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                if(results.getInt("finished") == 0) {
                    return true;
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean warRunningByDefender(int defenderId) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM wars WHERE defenderId=?");
            statement.setInt(1, defenderId);
            ResultSet results = statement.executeQuery();
            while(results.next()) {
                if(results.getInt("finished") == 0) {
                    return true;
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean warRunningByAttacker(int attackerId) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM wars WHERE attackerId=?");
            statement.setInt(1, attackerId);
            ResultSet results = statement.executeQuery();
            while(results.next()) {
                if(results.getInt("finished") == 0) {
                    return true;
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public War getWarByFaction(int factionId) {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM wars WHERE attackerId=? OR defenderId=? AND finished = 0");
            statement.setInt(1, factionId);
            ResultSet results = statement.executeQuery();
            while(results.next()) {
                War war = new War(results.getInt("id"),
                        results.getInt("attackerId"),
                        results.getInt("defenderId"),
                        results.getInt("attackerDeaths"),
                        results.getInt("defenderDeaths"),
                        results.getInt("finished"),
                        results.getInt("grace"),
                        results.getInt("graceMinutes"));
                return war;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createWar(int attackerId, int defenderId) {
        try {
            if(!warRunningByDefender(defenderId)) {
                PreparedStatement statement = plugin.getConnection()
                        .prepareStatement("INSERT INTO wars(id, attackerId, defenderId, attackerDeaths, defenderDeaths, finished, grace, graceMinutes) VALUES (NULL, ?, ?, 0, 0, 0, 1, ?)");
                statement.setInt(1, attackerId);
                statement.setInt(2, defenderId);
                statement.setInt(3, Main.instance.getConfig().getInt("wars.graceDurationInMinutes"));
                statement.executeQuery();
                return true;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateWarDeaths(int id, int attackerDeaths, int defenderDeaths) {
        try{
            PreparedStatement update = plugin.getConnection()
                    .prepareStatement("UPDATE wars SET attackerDeaths=?,defenderDeaths=? WHERE id=?");
            update.setInt(1, attackerDeaths);
            update.setInt(2, defenderDeaths);
            update.setInt(3, id);
            update.executeUpdate();
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateWarGrace(int id, int grace, int graceMinutes) {
        try{
            PreparedStatement update = plugin.getConnection()
                    .prepareStatement("UPDATE wars SET grace=?,graceMinutes=? WHERE id=?");
            update.setInt(1, grace);
            update.setInt(2, graceMinutes);
            update.setInt(3, id);
            update.executeUpdate();
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean endWar(int id) {
        try {
            PreparedStatement update = plugin.getConnection()
                    .prepareStatement("UPDATE wars SET finished=1 WHERE id=?");
            update.setInt(1, id);
            update.executeUpdate();
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<War> getAllWars() {
        try {
            PreparedStatement statement = plugin.getConnection()
                    .prepareStatement("SELECT * FROM wars WHERE 1");
            ResultSet results = statement.executeQuery();
            ArrayList<War> w = new ArrayList<War>();
            while(results.next()) {
                if(results.getInt("finished") != 1) {
                    w.add(new War(results.getInt("id"),
                            results.getInt("attackerId"),
                            results.getInt("defenderId"),
                            results.getInt("attackerDeaths"),
                            results.getInt("defenderDeaths"),
                            results.getInt("finished"),
                            results.getInt("grace"),
                            results.getInt("graceMinutes")));
                }
            }
            return w;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}