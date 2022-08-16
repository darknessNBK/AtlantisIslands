package com.lighty.atlantisland;

import com.lighty.atlantisland.utils.Methods;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

public class IslandGenerator {

    public static void generate(Integer id) {
        if(Methods.isIslandAlreadyCreated(id)) {
            Bukkit.getServer().getLogger().info(Methods.chatColor("&a[ATLANTIS-LAND] &eAn island tried to be generated but it is already generated! (Land: " + id + ")"));
            return;
        }

        Bukkit.getServer().getLogger().info(Methods.chatColor("&a[ATLANTIS-LAND] &eStarted to generate island... (Land: " + id + ")"));
        Location islandWorldLoc = Methods.stringToLocation(Objects.requireNonNull(AtlantisLandPlugin.getAtlantisLandPlugin().getConfig().getString("settings.island-world")));
        MongoCollection<Document> lands = MongoUtils.getCollection(AtlantisLandPlugin.getDatabase(), "lands");
        int lastIsland = Math.toIntExact(lands.countDocuments() + 1);

        int toAddX = lastIsland * AtlantisLandPlugin.getAtlantisLandPlugin().getConfig().getInt("settings.island-gap");
        Location islandLoc = islandWorldLoc.clone().add(toAddX, 0, 0);

        Methods.pasteSchematic(islandLoc, AtlantisLandPlugin.getAtlantisLandPlugin().getDataFolder() + "/island.schem", id);

        Bukkit.getServer().getLogger().info(Methods.chatColor("&a[ATLANTIS-LAND] &eIsland schematic pasted successfully, saving it to the database... (Land: " + id + ")"));

        Document land = new Document();
        land.append("land", id);
        land.append("location", Methods.locationToString(islandLoc));

        lands.insertOne(land);
        Bukkit.getServer().getLogger().info(Methods.chatColor("&a[ATLANTIS-LAND] &eIsland created and saved to database successfully! (Land: " + id + ")"));
    }
}
