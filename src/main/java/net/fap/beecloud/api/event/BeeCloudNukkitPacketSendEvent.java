package net.fap.beecloud.api.event;

import cn.nukkit.event.Event;
import cn.nukkit.network.protocol.DataPacket;

public class BeeCloudNukkitPacketSendEvent extends Event {

    private DataPacket packet;

    public BeeCloudNukkitPacketSendEvent(DataPacket packet)
    {
        this.packet = packet;
    }

    public DataPacket getPacket() {
        return packet;
    }

}
