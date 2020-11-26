package cc.bytewithasix.smpfactions.obj;

import java.util.ArrayList;

public class Faction {

    private int id;
    private String name;
    private int governmentType; //0: democracy, 1: monarchy, 2: dictatorship
    private String ownerUUID;

    public Faction(int id, String name, int governmentType, String ownerUUID) {
        this.id = id;
        this.name = name;
        this.governmentType = governmentType;
        this.ownerUUID = ownerUUID;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGovernmentType() {
        return governmentType;
    }

    public String getGovernmentTypeString() {
        switch(governmentType) {
            case 0:
                return "democracy";
            case 1:
                return "monarchy";
            case 2:
                return "dictatorship";
        }
        return null;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }
}
