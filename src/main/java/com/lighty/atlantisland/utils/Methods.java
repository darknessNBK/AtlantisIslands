package com.lighty.atlantisland.utils;

import com.lighty.atlantisland.AtlantisLandPlugin;
import com.lighty.atlantisland.MongoUtils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.nftworlds.wallet.objects.Wallet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.task.RegionAdder;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.web3j.protocol.Network;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class Methods {
    public static Location stringToLocation(String string2){
        String[] string = string2.split(",");
        World world   = Bukkit.getWorld(string[0]);
        Double x      = Double.parseDouble(string[1]);
        Double y      = Double.parseDouble(string[2]);
        Double z      = Double.parseDouble(string[3]);
        return new Location(world, x, y, z);
    }

    public static String locationToString(Location loc){
        String world = loc.getWorld().getName();
        String x     = String.valueOf(loc.getX());
        String y     = String.valueOf(loc.getY());
        String z     = String.valueOf(loc.getZ());
        return world + "," + x + "," + y + "," + z;
    }

    public static void pasteSchematic(Location location, String direction, Integer id){
        Clipboard clipboard = null;
        File file = new File(direction);
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
            assert clipboard != null;

            BlockVector3 pointX = BukkitAdapter.asBlockVector(location.clone().add(80, 255, 80));
            BlockVector3 pointZ = BukkitAdapter.asBlockVector(location.clone().add(-80, -255, -80));

            ProtectedRegion rg = new ProtectedCuboidRegion("land_" + id, pointX, pointZ);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
            if(regions == null) return;
            RegionAdder ra = new RegionAdder(regions, rg);
            try {
                ra.call();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BukkitAdapter.asBlockVector(location))
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }

    public static boolean isIslandAlreadyCreated(Integer id) {
        MongoCollection<Document> lands = MongoUtils.getCollection(AtlantisLandPlugin.getDatabase(), "lands");

        if(lands.find(Filters.eq("land", id)).first() != null) return true;
        else return false;
    }

    public static int getIslandFromLocation(Location location) {
        int islandID = -1;
        ArrayList<String> regions = (ArrayList<String>) WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld())).getApplicableRegionsIDs(BukkitAdapter.asBlockVector(location));
        for(String region : regions) {
            if(region.contains("land_")) {
                String[] land = region.split("land_");
                islandID = Integer.parseInt(land[1]);
            }
        }
        return islandID;
    }

    public static Location getIslandLocation(Integer id) {
        MongoCollection<Document> lands = MongoUtils.getCollection(AtlantisLandPlugin.getDatabase(), "lands");
        Document land = lands.find(Filters.eq("land", id)).first();
        Location loc = stringToLocation(land.getString("location"));
        return loc;
    }

    public static String chatColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @SneakyThrows
    public static String getLandOwner(Integer id) {
        String landContract = AtlantisLandPlugin.getAtlantisLandPlugin().getConfig().getString("settings.land-contract");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://deep-index.moralis.io/api/v2/nft/" + landContract + "/" + id + "/owners?chain=0x1&format=decimal&limit=1")
                .get()
                .addHeader("X-API-KEY", "k4TJtpaAoL3RPFR2XLCAbRw9mmZPMdzCxs9xAgCFrLdeQA7GLacGKURFkTPAzsJt")
                .build();

        JSONObject result = new JSONObject(client.newCall(request).execute().body().string());

        if(result.getInt("total") == 0) {
            return "Unclaimed";
        }

        JSONArray tokenArray = result.getJSONArray("result");
        JSONObject tokenInfo = tokenArray.getJSONObject(0);

        String ownerWallet = tokenInfo.getString("owner_of");
        return ownerWallet;
    }

    public static boolean isLandOwner(Integer id, Player player) {
        String landOwner = getLandOwner(id);
        String playerWallet = AtlantisLandPlugin.getWalletAPI().getPrimaryWallet(player).getAddress();

        return landOwner.equalsIgnoreCase(playerWallet);
    }

    @SneakyThrows
    public static ArrayList<Integer> getOwnedLands(Player player) {
        String landContract = AtlantisLandPlugin.getAtlantisLandPlugin().getConfig().getString("settings.land-contract");
        String playerWallet = AtlantisLandPlugin.getWalletAPI().getPrimaryWallet(player).getAddress();
        ArrayList<Integer> lands = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://deep-index.moralis.io/api/v2/" + playerWallet + "/nft/" + landContract + "?chain=0x1&format=decimal")
                .get()
                .addHeader("X-API-KEY", "k4TJtpaAoL3RPFR2XLCAbRw9mmZPMdzCxs9xAgCFrLdeQA7GLacGKURFkTPAzsJt")
                .build();

        JSONObject result = new JSONObject(client.newCall(request).execute().body().string());
        if(result.getJSONArray("result") != null) {
            JSONArray tokenArray = result.getJSONArray("result");
            for (int i=0; i < tokenArray.length(); i++) {
                JSONObject token = tokenArray.getJSONObject(i);
                String tokenIDString = token.getString("token_id");
                int tokenID = Integer.valueOf(tokenIDString);
                if(tokenID > 0 && tokenID < 5001) {
                    lands.add(tokenID);
                }
            }
        }
        return lands;
    }

    public static void refreshOwnerships() {
        AtlantisLandPlugin.getPlayerLands().clear();
        for(Player player : Bukkit.getOnlinePlayers()) {
            AtlantisLandPlugin.getPlayerLands().put(player, getOwnedLands(player));
        }
    }
}
