/*
 * PorteCoulissante - a Bukkit plugin for creating working portcullises
 * Copyright 2010, 2012, 2014  Pepijn Schmitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.reliqcraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;

/**
 *
 * @author pepijn
 */
public class PortcullisPlugin extends JavaPlugin {
    @Override
    public void onDisable() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("[PorteCoulissante] Plugin disabled");
        }
    }

    @Override
    public void onEnable() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "[PorteCoulissante] PortcullisPlugin.onEnable() (thread: " + Thread.currentThread() + ")", new Throwable());
        }
        
        File configFile = new File(getDataFolder(), "config.yml");
        if (! configFile.exists()) {
            getDataFolder().mkdirs();
            try {
                InputStream in = PortcullisPlugin.class.getResourceAsStream("/default.yml");
                try {
                    FileOutputStream out = new FileOutputStream(configFile);
                    try {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    } finally {
                        out.close();
                    }
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("I/O error creating default config.yml file", e);
            }
        }
        
        FileConfiguration config = getConfig();
        entityMovingEnabled = config.getBoolean("entityMoving");
        hoistingDelay = config.getInt("hoistingDelay");
        droppingDelay = config.getInt("droppingDelay");
        portcullisMaterials = new HashSet<Material>();
        portcullisMaterials.addAll(config.getStringList("portcullisMaterials").stream().map((x) -> Material.valueOf(x)).collect(Collectors.toSet()));
        allowFloating = config.getBoolean("allowFloating");
        powerBlocks = new HashSet<Material>();
        powerBlocks.addAll(config.getStringList("powerBlocks").stream().map((x) -> Material.valueOf(x)).collect(Collectors.toSet()));
        additionalWallMaterials = new HashSet<Material>();
        additionalWallMaterials.addAll(config.getStringList("additionalWallMaterials").stream().map((x) -> Material.valueOf(x)).collect(Collectors.toSet()));
        startSoundURL = config.getString("startSoundURL");
        upSoundURL = config.getString("upSoundURL");
        downSoundURL = config.getString("downSoundURL");
        soundEffectDistance = config.getInt("soundEffectDistance");
        soundEffectVolume = config.getInt("soundEffectVolume");
        allPowerBlocksAllowed = powerBlocks.isEmpty();
        List<String> warnings = new ArrayList<String>();
        if (hoistingDelay != DEFAULT_HOISTING_DELAY) {
            warnings.add("hoisting speed " + hoistingDelay);
        }
        if (droppingDelay != DEFAULT_DROPPING_DELAY) {
            warnings.add("dropping speed " + droppingDelay);
        }
        if (! portcullisMaterials.equals(DEFAULT_PORTCULLIS_MATERIALS)) {
            warnings.add("portcullis materials " + portcullisMaterials);
        }
        if (allowFloating != DEFAULT_ALLOW_FLOATING) {
            warnings.add("floating not allowed");
        }
        if (! allPowerBlocksAllowed) {
            warnings.add("power blocks allowed " + powerBlocks);
        }
        if (! additionalWallMaterials.isEmpty()) {
            warnings.add("additional wall materials " + additionalWallMaterials);
        }
        if (! warnings.isEmpty()) {
            StringBuilder sb = new StringBuilder("[PorteCoulissante] Non-standard configuration items loaded from config file: ");
            boolean first = true;
            for (String warning: warnings) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(warning);
            }
            logger.info(sb.toString());
        }
        
        String debugLoggingPropertyValue = config.getString("debugLogging");
        if (debugLoggingPropertyValue != null) {
            if (debugLoggingPropertyValue.equalsIgnoreCase("extra")) {
                logger.setLevel(Level.FINEST);
                logger.info("[PorteCoulissante] Extra debug logging enabled (see log file)");
            } else if (! debugLoggingPropertyValue.equalsIgnoreCase("false")) {
                logger.setLevel(Level.FINE);
                logger.info("[PorteCoulissante] Debug logging enabled (see log file)");
            }
        }

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PortcullisBlockListener(this), this);
        logger.info("[PorteCoulissante] Plugin version " + getDescription().getVersion() + " by Captain_Chaos enabled");
    }

    @Getter
    private boolean entityMovingEnabled;
    @Getter
    private int hoistingDelay, droppingDelay, soundEffectDistance, soundEffectVolume;
    @Getter
    private Set<Material> portcullisMaterials, powerBlocks, additionalWallMaterials;
    @Getter
    private boolean allowFloating, allPowerBlocksAllowed;
    @Getter
    private String startSoundURL, upSoundURL, downSoundURL;

    static final Logger logger = Logger.getLogger("Minecraft.org.reliqcraft");
    
    private static final int DEFAULT_HOISTING_DELAY = 40, DEFAULT_DROPPING_DELAY = 10;
    private static final Set<Material> DEFAULT_PORTCULLIS_MATERIALS = new HashSet<Material>(Arrays.asList(
        Material.ACACIA_FENCE, Material.IRON_BARS, Material.NETHER_BRICK_FENCE, Material.BIRCH_FENCE, Material.CRIMSON_FENCE, 
        Material.DARK_OAK_FENCE, Material.JUNGLE_FENCE, Material.OAK_FENCE, Material.SPRUCE_FENCE, Material.WARPED_FENCE
    ));
    private static final boolean DEFAULT_ALLOW_FLOATING = true;
    private static final int BUFFER_SIZE = 32768;
}