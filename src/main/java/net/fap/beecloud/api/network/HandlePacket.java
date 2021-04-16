package net.fap.beecloud.api.network;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;

public class HandlePacket {

    public static void handlePacket(String pk)
    {
        if (pk.indexOf("MovePlayerPacket")==1)
        {
            MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
            movePlayerPacket.putString(pk);
        }
    }

}
