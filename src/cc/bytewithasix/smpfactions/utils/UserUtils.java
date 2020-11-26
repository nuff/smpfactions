package cc.bytewithasix.smpfactions.utils;

import cc.bytewithasix.smpfactions.Main;
import cc.bytewithasix.smpfactions.obj.Member;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class UserUtils {

    public static String getName(String uuid) {
        String url = "https://api.mojang.com/user/profiles/"+uuid.replace("-", "")+"/names";
        try {
            @SuppressWarnings("deprecation")
            String nameJson = IOUtils.toString(new URL(url));
            JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
            String playerSlot = nameValue.get(nameValue.size()-1).toString();
            JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);
            return nameObject.get("name").toString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return "<Error>";
    }

    public static Map<Player, Member> getAllOnlineMembers() {
        Map<Player, Member> temp = new HashMap<Player, Member>();
        for(Player p : Main.getPlugin(Main.class).getServer().getOnlinePlayers()) {
            Member m = MysqlGetterSetter.instance.getMember(p.getUniqueId());
            temp.put(p, m);
        }
        return temp;
    }

    public static int countOnlineFactionMembers(int factionId) {
        Map<Player, Member> onlineMembers = getAllOnlineMembers();
        int onlines = 0;
        for(Entry<Player, Member> p: onlineMembers.entrySet()) {
            if(p.getValue().getFactionId() == factionId) {
                onlines++;
            }
        }
        return onlines;
    }
}
