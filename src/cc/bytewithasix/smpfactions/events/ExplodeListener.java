package cc.bytewithasix.smpfactions.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplodeListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.blockList().clear();
    }
}
