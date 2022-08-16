package com.lighty.atlantisland;

import com.lighty.atlantisland.commands.LandCommand;
import com.lighty.atlantisland.utils.Methods;
import com.mongodb.client.MongoDatabase;
import com.nftworlds.wallet.api.WalletAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public final class AtlantisLandPlugin extends JavaPlugin {

    @Getter private static AtlantisLandPlugin atlantisLandPlugin;
    @Getter private static WalletAPI walletAPI;
    @Getter private static HashMap<Player, ArrayList<Integer>> playerLands;
    @Getter private static MongoDatabase database = MongoUtils.loadDatabase("atlantis-land");

    @Override
    public void onEnable() {
        // Plugin startup logic
        atlantisLandPlugin = this;
        atlantisLandPlugin.saveDefaultConfig();
        walletAPI = new WalletAPI();
        playerLands = new HashMap<>();

        registerCommands(new String[] { "land" }, new LandCommand() );
        getServer().getPluginManager().registerEvents(new Events(), this);
        Methods.refreshOwnerships();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                getLogger().info(Methods.chatColor("&a[ATLANTIS-LAND] &eRefreshing all land ownerships..."));
                Methods.refreshOwnerships();
                getLogger().info(Methods.chatColor("&a[ATLANTIS-LAND] &eLand ownerships refreshed successfully!"));
            }
        }, 60 * 20L, 60 * 20L); //we multiply by 20 to make it do seconds

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerCommands(String[] cmds, CommandExecutor cmdExecutor)
    {
        for (String cmd : cmds)
        {
            getCommand(cmd).setExecutor(cmdExecutor);
        }
    }

}
