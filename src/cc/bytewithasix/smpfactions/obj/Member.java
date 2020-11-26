package cc.bytewithasix.smpfactions.obj;

import cc.bytewithasix.smpfactions.utils.UserUtils;

public class Member {

    private String uuid;
    private int factionId; //0: null
    private int leader; //1: leader, 0: member

    public Member(String uuid, int faction, int leader) {
        this.uuid = uuid;
        this.factionId = faction;
        this.leader = leader;
    }

    public String getUsername() { return UserUtils.getName(uuid); }
    public String getUUID() {
        return uuid;
    }
    public int getFactionId() {
        return factionId;
    }
    public int getLeader() {
        return leader;
    }
    public boolean isLeader() {
        if(leader == 0) {
            return false;
        } else {
            return true;
        }
    }
    public boolean sameFactionAs(Member m) {
        if(m != null && this.getFactionId() == m.getFactionId()) {
            return true;
        }
        return false;
    }
    public boolean sameFactionAs(Faction f) {
        if(f != null && this.getFactionId() == f.getId()) {
            return true;
        }
        return false;
    }
}
