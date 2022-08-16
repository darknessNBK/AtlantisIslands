package com.lighty.atlantisland;

import com.lighty.atlantisland.utils.Methods;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Objects;


public class Events implements Listener {
    @EventHandler
    public void blockListener(BlockBreakEvent e) {
        int islandID = Methods.getIslandFromLocation(e.getPlayer().getLocation());
        if(islandID == -1) return;
        ArrayList<Integer> playerLands = AtlantisLandPlugin.getPlayerLands().get(e.getPlayer());
        if(playerLands == null) return;
        boolean perm = playerLands.contains(islandID);
        if(!perm) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Methods.chatColor("&c&lHey!&r &7You can't break a block on a island you don't own!"));
        }
        else {
            if(e.getBlock().getLocation().getBlockY() < 104) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Methods.chatColor("&c&lHey!&r &7You can't break the ground!"));
            }
        }
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        int islandID = Methods.getIslandFromLocation(e.getPlayer().getLocation());
        if(islandID == -1) return;
        ArrayList<Integer> playerLands = AtlantisLandPlugin.getPlayerLands().get(e.getPlayer());
        if(playerLands == null) return;
        boolean perm = playerLands.contains(islandID);
        if(!perm) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Methods.chatColor("&c&lHey!&r &7You can't place a block on a island you don't own!"));
        }
        else {
            if(e.getBlock().getLocation().getBlockY() < 104) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Methods.chatColor("&c&lHey!&r &7You can't place a block on the ground level!"));
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent e) {
        if(e.getAction() != Action.LEFT_CLICK_AIR || e.getAction() != Action.RIGHT_CLICK_AIR) {
            if(!e.getAction().isRightClick()) return;
            int islandID = Methods.getIslandFromLocation(e.getPlayer().getLocation());
            if(islandID == -1) return;
            ArrayList<Integer> playerLands = AtlantisLandPlugin.getPlayerLands().get(e.getPlayer());
            if(playerLands == null) return;
            boolean perm = playerLands.contains(islandID);
            if(!perm) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Methods.chatColor("&c&lHey!&r &7You can't interact with something on a island you don't own!"));
            }
        }
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {
        int islandID = Methods.getIslandFromLocation(e.getPlayer().getLocation());
        if(islandID == -1) return;

        Location to = e.getTo();
        Location from = e.getFrom();

        ArrayList<String> regionsFrom = (ArrayList<String>) Objects.requireNonNull(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(from.getWorld()))).getApplicableRegionsIDs(BukkitAdapter.asBlockVector(from));
        ArrayList<String> regionsTo = (ArrayList<String>) Objects.requireNonNull(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(to.getWorld()))).getApplicableRegionsIDs(BukkitAdapter.asBlockVector(to));

        if(regionsFrom.contains("land_" + islandID) && !regionsTo.contains("land_" + islandID)) {
            e.getPlayer().sendMessage(Methods.chatColor("&c&lHey!&r &7You can't leave the island! Use &l/land visit&r&7 or &l/land lobby&r&7 instead!"));
            e.setCancelled(true);
        }
        else if(regionsFrom.contains("land_" + islandID) && e.getTo().getBlockY() < 87) {
            e.getPlayer().sendMessage(Methods.chatColor("&c&lHey!&r &7You can't leave the island! Use &l/land visit&r&7 or &l/land lobby&r&7 instead!"));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Location lobby = Methods.stringToLocation(AtlantisLandPlugin.getAtlantisLandPlugin().getConfig().getString("settings.island-lobby"));
        e.getPlayer().teleport(lobby);
        Methods.refreshOwnerships();
    }
}
