package cc.bytewithasix.smpfactions.obj;

public class War {
    private int id;
    private int attackerId;
    private int defenderId;
    private int attackerDeaths;
    private int defenderDeaths;
    private int finished;
    private int grace;
    private int graceMinutes;

    public War(int id, int attackerId, int defenderId, int attackerDeaths, int defenderDeaths, int finished, int grace, int graceMinutes) {
        this.id = id;
        this.attackerId = attackerId;
        this.defenderId = defenderId;
        this.attackerDeaths = attackerDeaths;
        this.defenderDeaths = defenderDeaths;
        this.finished = finished;
        this.grace = grace;
        this.graceMinutes = graceMinutes;
    }

    public boolean isGrace() {
        if(grace == 1) return true;
        return false;
    }

    //Getters
    public int getId() {
        return id;
    }

    public int getAttackerId() {
        return attackerId;
    }

    public int getDefenderId() {
        return defenderId;
    }

    public int getAttackerDeaths() {
        return attackerDeaths;
    }

    public int getDefenderDeaths() {
        return defenderDeaths;
    }

    public int getFinished() {
        return finished;
    }

    public int getGrace() {
        return grace;
    }

    public int getGraceMinutes() {
        return graceMinutes;
    }
}
