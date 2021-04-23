package net.fap.beecloud.api;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

public class BeeCloudAPI extends PluginBase implements Listener {

    private Synapse synapse;
    private static BeeCloudAPI beeCloudAPI;

    public static Config config;

    public static BeeCloudAPI getInstance()
    {
        return beeCloudAPI;
    }

    @Override
    public void onEnable()
    {
        beeCloudAPI = this;
        this.getLogger().info("BeeCloudAPI running!");
        config = new Config(this.getDataFolder()+"/synapse.yml",2);
        if (!config.exists("server-port"))
        {
            config.set("server-port",8888);
            config.set("server-motd","synapse-server1");
            config.set("synapse-chat",true);
            config.save();
        }
        synapse = new Synapse(config.getInt("server-port"));
        this.getServer().getPluginManager().registerEvents(this,this);

        //实时更新服务器状态 ServerUpdatePacket
        this.getServer().getScheduler().scheduleRepeatingTask(new Task() {
            @Override
            public void onRun(int i) {
               synapse.send("ServerUpdatePacket");
            }
        }, 1);

    }

    @EventHandler
    public void handleJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        String pk = "LoginPacket:"+event.getPlayer().getName()+":"+player.getAddress()+":"+player.getUniqueId()+":"+player.getClientId();
        synapse.send(pk);
    }

    @EventHandler
    public void handleQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        String pk = "QuitPacket:"+player.getName();
        synapse.send(pk);
    }

    @EventHandler
    public void handleChat(PlayerChatEvent event)
    {
        if (config.getBoolean("synapse-chat"))
        {
            String message = event.getMessage();
            String player = event.getPlayer().getName();
            String customPacket = "ServerChatPacket:"+config.getString("server-motd")+":"+player+":"+message;
            synapse.send(customPacket);
        }
    }

    public void handlePacket(String packet)
    {
        if (packet.contains("ServerUpdatePacket"))
        {
            String[] pk2 = packet.split("\\:");
            int number = Integer.parseInt(pk2[1]);
            getServer().getQueryInformation().setPlayerCount(number);
        }
        if (packet.contains("ServerChatPacket"))
        {

        }
    }

}
