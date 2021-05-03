package net.fap.beecloud.api;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import net.fap.beecloud.api.synapse.SimpleCommand;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class BeeCloudAPI extends PluginBase implements Listener {

    public static String ENCODING_UTF8 = "UTF-8";
    public static String ENCODING_GBK = "GBK";
    public static String ENCODING_GB2312 = "GB2312";

    private Synapse synapse;
    private static BeeCloudAPI beeCloudAPI;

    public static Config config;

    private static String password;
    private boolean login = false;

    private ArrayList<String> commandList = new ArrayList<>();

    public static BeeCloudAPI getInstance()
    {
        return beeCloudAPI;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable()
    {
        config = new Config(this.getDataFolder()+"/synapse.yml",2);
        beeCloudAPI = this;
        this.getLogger().info("BeeCloudAPI running!");
        if (!config.exists("server-port"))
        {
            config.set("server-port",8888);
            config.set("server-motd","synapse-server1");
            config.set("synapse-password","123456789");
            config.set("isLobbyServer","true");
            config.set("transferOnShutdown","true");
            config.set("synapse-chat",true);
            config.save();
        }
        this.getServer().getPluginManager().registerEvents(this,this);
        synapse = new Synapse(config.getInt("server-port"));
        password = config.getString("synapse-password");
        //实时更新服务器状态 ServerUpdatePacket
        this.getServer().getScheduler().scheduleRepeatingTask(new Task() {
            @Override
            public void onRun(int i) {
               synapse.send("ServerUpdatePacket");
               int tick = 0;
               if (!login)
               {
                  if (tick<=20)
                  {
                      String connectPacket = "ConnectPacket:"+password+":"+config.getString("server-motd")+":"+getServer().getPort()+":"+config.getString("isLobbyServer")+":"+
                              config.getString("transferOnShutdown");
                      synapse.send(connectPacket);
                      tick++;
                  }else System.exit(0);
               }
            }
        }, 1);

    }

    @EventHandler
    public void handleCommand(PlayerCommandPreprocessEvent event)
    {
        if (commandList.contains(event.getMessage().replace("/","").split("\\s+")[0]))
        {
            Player player = event.getPlayer();
            String pk = "CommandPacket:"+player.getName()+":"+event.getMessage();
            synapse.send(pk);
        }
    }

    @EventHandler
    public void handleJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        String pk = "LoginPacket:"+event.getPlayer().getName()+":"+player.getAddress()+":"+player.getUniqueId()+":"+player.getClientId()+":"+config.getString("server-motd");
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
        if (packet.contains("ConnectPacket"))
        {
            String pk2[] = packet.split("\\:");
            if (pk2[1].equals("SUCCESS")) {
                this.login=true;
                getLogger().info("Connect BeeCloud successfully!");
            }
            else if (pk2[1].equals("FAIlED")) login = false;
        }
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
            login = false;
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
        if (packet.contains("TransferPacket"))
        {
           try{
               String[] pk2 = packet.split("\\:");
               Player player = this.getServer().getPlayer(pk2[2]);
               InetAddress address = InetAddress.getLocalHost();
               String serverIp = address.getHostAddress();
               player.transfer(new InetSocketAddress(serverIp,Integer.valueOf(pk2[3])));
           }catch (Exception e)
           {
               e.printStackTrace();
           }
        }
        if (packet.contains("CommandRegisterPacket"))
        {
            String[] pk2 = packet.split("\\:");
            String commandStr = pk2[1];
            String commandUsage = pk2[2];
            if (!commandList.contains(commandStr))
            {
                this.getServer().getCommandMap().register(commandStr,new SimpleCommand(this,commandStr,commandUsage));
                commandList.add(commandStr);
            }
        }
    }

}
