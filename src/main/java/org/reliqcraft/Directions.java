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

import org.bukkit.block.BlockFace;

/**
 * Special directional constants class which provides compatibility with older
 * versions of Bukkit in which the BlockFace directions were incorrect due to
 * historical reasons.
 *
 * @author pepijn
 */
public final class Directions {
    private Directions() {
        // Prevent instantiation
    }
    
    /**
     * This method is necessary to support Bukkit versions prior to 1.4.5, which
     * had a different mapping from cardinal direction to axes.
     * 
     * <strong>Does not work for diagonal directions!</strong>
     */
    public static BlockFace actual(BlockFace direction) {
        if (legacy) {
            switch (direction) {
                case WEST:
                    return BlockFace.NORTH;
                case NORTH:
                    return BlockFace.EAST;
                case EAST:
                    return BlockFace.SOUTH;
                case SOUTH:
                    return BlockFace.WEST;
                default:
                    return direction;
            }
        } else {
            return direction;
        }
    }
    
    public static final BlockFace ACTUAL_NORTH;
    public static final BlockFace ACTUAL_EAST;
    public static final BlockFace ACTUAL_SOUTH;
    public static final BlockFace ACTUAL_WEST;
    
    private static final boolean legacy = BlockFace.NORTH.getModX() == -1;
    
    static {
        if (legacy) {
            ACTUAL_NORTH = BlockFace.EAST;
            ACTUAL_EAST = BlockFace.SOUTH;
            ACTUAL_SOUTH = BlockFace.WEST;
            ACTUAL_WEST = BlockFace.NORTH;
        } else {
            ACTUAL_NORTH = BlockFace.NORTH;
            ACTUAL_EAST = BlockFace.EAST;
            ACTUAL_SOUTH = BlockFace.SOUTH;
            ACTUAL_WEST = BlockFace.WEST;
        }
    }
}
