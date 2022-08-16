package com.lighty.atlantisland.commands;

import com.lighty.atlantisland.AtlantisLandPlugin;
import com.lighty.atlantisland.IslandGenerator;
import com.lighty.atlantisland.utils.Methods;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LandCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        String line1 = " ";
        String line2 = Methods.chatColor("&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r &r&6&lATLANTIS PARCEL PASS &e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r&e&l&m &r");

        if (args.length == 0) {
            String line3 = Methods.chatColor("&cPlease enter a sub-command!");
            String line4 = Methods.chatColor("&7- &e/land visit [token id]: &7&oTeleports you to the given NFT land!");
            String line5 = Methods.chatColor("&7- &e/land generate [token id]: &7&oGenerates your NFT land if you have one and it is not generated.");
            String line6 = Methods.chatColor("&7- &e/land lobby: &7&oTeleports you to the lands lobby.");
            String line7 = " ";
            player.sendMessage(line1, line2, line3, line4, line5, line6, line7);
            return true;
        }

        if (args.length >= 1) {
            // Some arguments were provided
            if (args[0].equalsIgnoreCase("visit")) {
                if (args.length >= 2) {
                    int islandID = Integer.parseInt(args[1]);
                    if(islandID < 1 || islandID > 5000) {
                        String line3 = Methods.chatColor("&cToken ID must be between 1 and 5000!");
                        String line4 = " ";
                        player.sendMessage(line1, line2, line3, line4);
                        return true;
                    }

                    if(!Methods.isIslandAlreadyCreated(islandID)) {
                        String line3 = Methods.chatColor("&cThis land is not generated or claimed yet! If you have this land, use &l/land generate&r&c!");
                        String line4 = " ";
                        player.sendMessage(line1, line2, line3, line4);
                        return true;
                    }

                    boolean landOwnership = Methods.isLandOwner(islandID, player);
                    String ownerWallet = Methods.getLandOwner(islandID);

                    player.teleport(Methods.getIslandLocation(islandID));
                    if(landOwnership) {
                        player.sendTitle(Methods.chatColor("&6&lLand #" + islandID), Methods.chatColor("&a&oWelcome to your private island!"), 10, 50, 20);
                        String line3 = Methods.chatColor("&aSuccesfully teleported to land #" + islandID + "!");
                        String line4 = " ";
                        player.sendMessage(line1, line2, line3, line4);
                    } else {
                        player.sendTitle(Methods.chatColor("&6&lLand #" + islandID), Methods.chatColor("&aOwner: &7" + ownerWallet), 10, 50, 20);
                        String line3 = Methods.chatColor("&aSuccesfully teleported to land #" + islandID + "!");
                        String line4 = " ";
                        player.sendMessage(line1, line2, line3, line4);
                    }
                } else {
                    String line3 = Methods.chatColor("&cPlease enter a token ID!");
                    String line4 = " ";
                    player.sendMessage(line1, line2, line3, line4);
                    return true;
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("generate")) {
                if (args.length >= 2) {
                    int islandID = Integer.parseInt(args[1]);
                    if(islandID < 1 || islandID > 5000) {
                        String line3 = Methods.chatColor("&cToken ID must be between 1 and 5000!");
                        String line4 = " ";
                        player.sendMessage(line1, line2, line3, line4);
                        return true;
                    }

                    if(Methods.isIslandAlreadyCreated(islandID)) {
                        String line3 = Methods.chatColor("&cThis land is already generated!");
                        String line4 = " ";
                        player.sendMessage(line1, line2, line3, line4);
                        return true;
                    }

                    boolean landOwnership = Methods.isLandOwner(islandID, player);

                    if(landOwnership) {
                        player.sendMessage(Methods.chatColor("&a&oStarting to generate your private island..."));
                        IslandGenerator.generate(islandID);
                        player.sendMessage(Methods.chatColor("&aYour private island is generated! Use &l/land visit " + islandID + "&r&a to visit your island now!"));
                        return true;
                    } else {
                        String line3 = Methods.chatColor("&cYou don't own that NFT land!");
                        String line4 = " ";
                        player.sendMessage(line1, line2, line3, line4);
                        return true;
                    }

                } else {
                    String line3 = Methods.chatColor("&cPlease enter a token ID!");
                    String line4 = " ";
                    player.sendMessage(line1, line2, line3, line4);
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("lobby")) {
                Location lobby = Methods.stringToLocation(AtlantisLandPlugin.getAtlantisLandPlugin().getConfig().getString("settings.island-lobby"));
                player.teleport(lobby);
                player.sendMessage(Methods.chatColor("&aTeleported to the lobby!"));
                return true;
            }
        }
        return true;
    }
}
