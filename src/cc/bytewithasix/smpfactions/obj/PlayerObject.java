package cc.bytewithasix.smpfactions.obj;

import java.util.UUID;

public class PlayerObject {
    private String name;
    private UUID uuid;

    public PlayerObject(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }
}
