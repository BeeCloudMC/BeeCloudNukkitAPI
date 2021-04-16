package net.fap.beecloud.api.network.mcpe;

import cn.nukkit.Player;

public class PlayerJoinPacket {

    public String player;

    public PlayerJoinPacket(Player player)
    {
        this.player = player.getName();
    }


}
