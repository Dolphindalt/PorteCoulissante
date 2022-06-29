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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import static org.bukkit.block.BlockFace.*;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 *
 * @author pepijn
 */
public class PortcullisBlockListener implements Listener {
    public PortcullisBlockListener(final PortcullisPlugin plugin) {
        this.plugin = plugin;
        wallMaterials.addAll(plugin.getAdditionalWallMaterials());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockRedstoneChange(final BlockRedstoneEvent event) {
        try {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "[PorteCoulissante] PortcullisBlockListener.onBlockRedstoneChange() (thread: "
                        + Thread.currentThread() + ")", new Throwable());
            }
            final Block block = event.getBlock();
            final Location location = block.getLocation();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("[PorteCoulissante] Redstone event on block @ " + location.getBlockX() + ", "
                        + location.getBlockY() + ", " + location.getBlockZ() + ", type: " + block.getType() + "; "
                        + event.getOldCurrent() + " -> " + event.getNewCurrent());
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("[PorteCoulissante] Type according to World.getBlockAt(): "
                            + block.getWorld().getBlockAt(location).getType());
                    logger.finest("[PorteCoulissante] Type according to World.getBlockAt(...).getBlockData().getMateral(): "
                            + block.getWorld().getBlockAt(location).getBlockData().getMaterial().toString());
                }
            }
            if (!((event.getOldCurrent() == 0) || (event.getNewCurrent() == 0))) {
                // Not a power on or off event
                return;
            }
            if (!CONDUCTIVE.contains(block.getBlockData().getMaterial())) {
                logger.fine("[PorteCoulissante] Block @ " + location.getBlockX() + ", " + location.getBlockY() + ", "
                        + location.getBlockZ() + ", type: " + block.getType() + " not conductive; ignoring");
                return;
            }
            final boolean powerOn = event.getOldCurrent() == 0;
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("[PorteCoulissante] Block powered " + (powerOn ? "on" : "off"));
            }
            for (final BlockFace direction : CARDINAL_DIRECTIONS) {
                Portcullis portCullis = findPortcullisInDirection(block, direction);
                if (portCullis != null) {
                    portCullis = normalisePortcullis(portCullis);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("[PorteCoulissante] Portcullis found! (x: " + portCullis.getX() + ", z: "
                                + portCullis.getZ() + ", y: " + portCullis.getY() + ", width: " + portCullis.getWidth()
                                + ", height: " + portCullis.getHeight() + ", direction: " + portCullis.getDirection()
                                + ")");
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("[PorteCoulissante] According to Bukkit cache:");
                            final World world = block.getWorld();
                            for (int y = portCullis.getY() + portCullis.getHeight() + 4; y >= portCullis.getY()
                                    - 5; y--) {
                                final StringBuilder sb = new StringBuilder("[PorteCoulissante] ");
                                sb.append(y);
                                for (int i = -5; i <= portCullis.getWidth() + 4; i++) {
                                    sb.append('|');
                                    sb.append(world
                                            .getBlockAt(portCullis.getX() + i * portCullis.getDirection().getModX(), y,
                                                    portCullis.getZ() + i * portCullis.getDirection().getModZ())
                                            .getType().name().substring(0, 2));
                                }
                                logger.finest(sb.toString());
                            }
                            logger.finest("[PorteCoulissante] According to Minecraft:");
                            for (int y = portCullis.getY() + portCullis.getHeight() + 4; y >= portCullis.getY()
                                    - 5; y--) {
                                final StringBuilder sb = new StringBuilder("[PorteCoulissante] ");
                                sb.append(y);
                                for (int i = -5; i <= portCullis.getWidth() + 4; i++) {
                                    sb.append('|');
                                    sb.append(world.getBlockAt(
                                        portCullis.getX() + i * portCullis.getDirection().getModX(), y,
                                        portCullis.getZ() + i * portCullis.getDirection().getModZ())
                                        .getBlockData().getMaterial().toString());
                                }
                                logger.finest(sb.toString());
                            }
                        }
                    }
                    if (powerOn) {
                        hoistPortcullis(portCullis);
                    } else {
                        dropPortcullis(portCullis);
                    }
                }
            }
        } catch (final Throwable t) {
            logger.log(Level.SEVERE, "[PorteCoulissante] Exception thrown while handling redstone event!", t);
        }
    }

    private Portcullis findPortcullisInDirection(final Block block, final BlockFace direction) {
        final Block powerBlock = block.getRelative(direction);
        final Material powerBlockType = powerBlock.getBlockData().getMaterial();
        if (isPotentialPowerBlock(powerBlockType)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("[PorteCoulissante] Potential power block found (type: " + powerBlockType + ")");
            }
            final Block firstPortcullisBlock = powerBlock.getRelative(direction);
            if (isPotentialPortcullisBlock(firstPortcullisBlock)) {
                final Material portcullisType = firstPortcullisBlock.getBlockData().getMaterial();
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("[PorteCoulissante] Potential portcullis block found (type: " + portcullisType + ")");
                }
                if (portcullisType == powerBlockType) {
                    // The portcullis can't be made of the same blocks as its frame
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("[PorteCoulissante] Potential portcullis block is same type as wall; aborting");
                    }
                    return null;
                }
                Block lastPortCullisBlock = firstPortcullisBlock.getRelative(direction);
                if (isPortcullisBlock(portcullisType,lastPortCullisBlock)) {
                    int width = 2;
                    Block nextBlock = lastPortCullisBlock.getRelative(direction);
                    while (isPortcullisBlock(portcullisType, nextBlock)) {
                        width++;
                        lastPortCullisBlock = nextBlock;
                        nextBlock = lastPortCullisBlock.getRelative(direction);
                    }
                    // At least two fences found in a row. Now search up and down
                    int highestY = firstPortcullisBlock.getLocation().getBlockY();
                    Block nextBlockUp = firstPortcullisBlock.getRelative(UP);
                    while (isPortcullisBlock(portcullisType, nextBlockUp)) {
                        highestY++;
                        nextBlockUp = nextBlockUp.getRelative(UP);
                    }
                    int lowestY = firstPortcullisBlock.getLocation().getBlockY();
                    Block nextBlockDown = firstPortcullisBlock.getRelative(DOWN);
                    while (isPortcullisBlock(portcullisType, nextBlockDown)) {
                        lowestY--;
                        nextBlockDown = nextBlockDown.getRelative(DOWN);
                    }
                    final int height = highestY - lowestY + 1;
                    if (height >= 2) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("[PorteCoulissante] Found potential portcullis of width " + width
                                    + " and height " + height);
                        }
                        final int x = firstPortcullisBlock.getX();
                        final int y = lowestY;
                        final int z = firstPortcullisBlock.getZ();
                        final World world = firstPortcullisBlock.getWorld();
                        // Check the integrity of the portcullis
                        for (int i = -1; i <= width; i++) {
                            for (int dy = -1; dy <= height; dy++) {
                                if ((((i == -1) || (i == width)) && (dy != -1) && (dy != height))
                                        || (((dy == -1) || (dy == height)) && (i != -1) && (i != width))) {
                                    // This is one of the blocks to the sides or above or below of the portcullis
                                    final Block frameBlock = world.getBlockAt(x + i * direction.getModX(), y + dy,
                                            z + i * direction.getModZ());
                                    if (isPortcullisBlock(portcullisType, frameBlock)) {
                                        if (logger.isLoggable(Level.FINE)) {
                                            logger.fine(
                                                    "[PorteCoulissante] Block of same type as potential portcullis found in frame; aborting");
                                        }
                                        return null;
                                    }
                                } else if ((i >= 0) && (i < width) && (dy >= 0) && (dy < height)) {
                                    // This is a portcullis block
                                    final Block portcullisBlock = world.getBlockAt(x + i * direction.getModX(),
                                            y + dy, z + i * direction.getModZ());
                                    if (!isPortcullisBlock(portcullisType, portcullisBlock)) {
                                        if (logger.isLoggable(Level.FINE)) {
                                            logger.fine("[PorteCoulissante] Block of wrong type ("
                                                    + portcullisBlock.getBlockData().getMaterial()
                                                    + ") found inside potential portcullis; aborting");
                                        }
                                        return null;
                                    }
                                }
                            }
                        }
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("[PorteCoulissante] Portcullis found! Location: " + x + ", " + y + ", " + z
                                    + ", width: " + width + ", height: " + height + ", direction: " + direction
                                    + ", type: " + portcullisType);
                        }
                        return new Portcullis(world.getName(), x, z, y, width, height, direction, portcullisType);
                    }
                }
            }
        }
        return null;
    }

    private boolean isPotentialPowerBlock(final Material wallType) {
        return plugin.isAllPowerBlocksAllowed() ? wallMaterials.contains(wallType)
                : plugin.getPowerBlocks().contains(wallType);
    }

    private boolean isPotentialPortcullisBlock(final Block block) {
        return plugin.getPortcullisMaterials().contains(block.getBlockData().getMaterial());
    }

    private boolean isPortcullisBlock(final Material portcullisType, final Block block) {
        return (block.getBlockData().getMaterial() == portcullisType);
    }

    private Portcullis normalisePortcullis(final Portcullis portcullis) {
        if (portcullis.getDirection() == WEST) {
            return new Portcullis(portcullis.getWorldName(), portcullis.getX() - portcullis.getWidth() + 1,
                    portcullis.getZ(), portcullis.getY(), portcullis.getWidth(), portcullis.getHeight(), EAST,
                    portcullis.getType());
        } else if (portcullis.getDirection() == NORTH) {
            return new Portcullis(portcullis.getWorldName(), portcullis.getX(),
                    portcullis.getZ() - portcullis.getWidth() + 1, portcullis.getY(), portcullis.getWidth(),
                    portcullis.getHeight(), SOUTH, portcullis.getType());
        } else {
            return portcullis;
        }
    }

    private void hoistPortcullis(final Portcullis portcullis) {
        // Check whether the portcullis is already known
        for (final PortcullisMover mover : portcullisMovers) {
            if (mover.getPortcullis().equals(portcullis)) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("[PorteCoulissante] Reusing existing portcullis mover");
                }
                // Set the portcullis, because the one cached by the portcullis
                // mover may be made from a different material
                mover.setPortcullis(portcullis);
                mover.hoist();
                return;
            }
        }
        // It isn't
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("[PorteCoulissante] Creating new portcullis mover");
        }
        final PortcullisMover mover = new PortcullisMover(plugin, portcullis, wallMaterials);
        portcullisMovers.add(mover);
        mover.hoist();
    }

    private void dropPortcullis(final Portcullis portcullis) {
        // Check whether the portcullis is already known
        for (final PortcullisMover mover : portcullisMovers) {
            if (mover.getPortcullis().equals(portcullis)) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("[PorteCoulissante] Reusing existing portcullis mover");
                }
                // Set the portcullis, because the one cached by the portcullis
                // mover may be made from a different material
                mover.setPortcullis(portcullis);
                mover.drop();
                return;
            }
        }
        // It isn't
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("[PorteCoulissante] Creating new portcullis mover");
        }
        final PortcullisMover mover = new PortcullisMover(plugin, portcullis, wallMaterials);
        portcullisMovers.add(mover);
        mover.drop();
    }

    private final PortcullisPlugin plugin;
    private final Set<PortcullisMover> portcullisMovers = new HashSet<PortcullisMover>();
    private final Set<Material> wallMaterials = new HashSet<Material>(Arrays.asList(Material.values()));
    
    private static final BlockFace[] CARDINAL_DIRECTIONS = {NORTH, EAST, SOUTH, WEST};
    private static final Set<Material> CONDUCTIVE = new HashSet<Material>(Arrays.asList(
        Material.REDSTONE_WIRE, Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH, Material.REPEATER,
        Material.STONE_BUTTON, Material.LEVER, Material.STONE_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
        Material.TRIPWIRE_HOOK, Material.ACACIA_BUTTON, Material.BIRCH_BUTTON, Material.CRIMSON_BUTTON, Material.DARK_OAK_BUTTON,
        Material.JUNGLE_BUTTON, Material.OAK_BUTTON, Material.POLISHED_BLACKSTONE_BUTTON, Material.SPRUCE_BUTTON, Material.WARPED_BUTTON,
        Material.COMPARATOR, Material.REDSTONE_BLOCK, Material.HEAVY_WEIGHTED_PRESSURE_PLATE
    ));
    private static final Logger logger = PortcullisPlugin.logger;
}