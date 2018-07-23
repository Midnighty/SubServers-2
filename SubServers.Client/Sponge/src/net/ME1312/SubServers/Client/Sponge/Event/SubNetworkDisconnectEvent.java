package net.ME1312.SubServers.Client.Sponge.Event;

import net.ME1312.SubServers.Client.Sponge.Library.SubEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * SubData Network Disconnect Event
 */
public class SubNetworkDisconnectEvent extends AbstractEvent implements SubEvent {

    /**
     * Gets the cause of this Event
     *
     * @return An empty cause list
     */
    @Override
    public Cause getCause() {
        return Cause.builder().build();
    }
}