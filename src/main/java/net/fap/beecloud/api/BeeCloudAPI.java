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

import java.net.URLEncoder;

public class BeeCloudAPI extends PluginBase implements Listener {

    public static String ENCODING_UTF8 = "UTF-8";
    public static String ENCODING_GBK = "GBK";
    public static String ENCODING_GB2312 = "GB2312";

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
            event.setCancelled();
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
            if (config.getBoolean("synapse-chat"))
            {
                try{
                    String[] pk2 = packet.split("\\:");
                    String message = pk2[1];
                    String message2 = new String(message.getBytes(ENCODING_UTF8), ENCODING_UTF8);
                    Server.getInstance().broadcastMessage(message2);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        if (packet.contains("DisconnectPacket"))
        {
            for (Player player : Server.getInstance().getOnlinePlayers().values())
                player.kick("§cSynapse Server Closed!");
        }
        if (packet.contains("KickPlayerPacket"))
        {
            String[] pk2 = packet.split("\\:");
            String player = pk2[1];
            String reason = pk2[2];
            if (reason==null)this.getServer().getPlayer(player).kick(); else this.getServer().getPlayer(player).kick(reason);
        }
        if (packet.contains("TextMessagePacket"))
        {
            String[] pk2 = packet.split("\\:");
            String player = pk2[1];
            String message = pk2[2];
            this.getServer().getPlayer(player).sendMessage(message);
        }
        if (packet.contains("TextTitlePacket"))
        {
            String[] pk2 = packet.split("\\:");
            String player = pk2[1];
            if (pk2.length==7)
            {
                this.getServer().getPlayer(player).sendTitle(pk2[2],pk2[3],Integer.valueOf(pk2[4]),Integer.valueOf(pk2[5]),Integer.valueOf(pk2[6]));
            }else{
                this.getServer().getPlayer(player).sendTitle(pk2[2],pk2[3]);
            }
        }
        if (packet.contains("TextTipPacket"))
        {
            String[] pk2 = packet.split("\\:");
            this.getServer().getPlayer(pk2[1]).sendTip(pk2[2]);
        }
    }

}
