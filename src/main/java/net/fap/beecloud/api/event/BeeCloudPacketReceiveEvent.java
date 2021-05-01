package net.fap.beecloud.api.event;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

public class BeeCloudPacketReceiveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public String packetString;

    public BeeCloudPacketReceiveEvent(String packet)
    {
        this.packetString = packet;
    }

    public String getPacketString() {
        return packetString;
    }

    public String[] toArray()
    {
        String[] pk = getPacketString().split("\\:");
        return pk;
    }

}
