package net.fap.beecloud.api;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.plugin.PluginBase;

public class BeeCloudAPI extends PluginBase implements Listener {

    private Synapse synapse;

    @Override
    public void onEnable()
    {
        this.getLogger().info("BeeCloudAPI running!");
        synapse = new Synapse(8888);
        this.getServer().getPluginManager().registerEvents(this,this);
    }

    @EventHandler
    public void handlePacket(DataPacketReceiveEvent event)
    {
        DataPacket packet = event.getPacket();
        Player player = event.getPlayer();
        if (packet instanceof LoginPacket)
        {
            String pk = "LoginPacket:"+event.getPlayer().getName()+":"+player.getAddress()+":"+player.getUniqueId()+":"+((LoginPacket) packet).clientId;
            synapse.send(pk);
        }else synapse.send(packet);
    }

    @EventHandler
    public void handleEvent(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        String pk = "QuitPacket"+player.getName();
        synapse.send(pk);
    }

}
