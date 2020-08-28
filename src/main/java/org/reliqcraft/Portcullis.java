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

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 *
 * @author pepijn
 */
public class Portcullis {
    public Portcullis(String worldName, int x, int z, int y, int width, int height, BlockFace direction, Material type) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
        this.width = width;
        this.height = height;
        this.y = y;
        this.direction = direction;
        this.type = type;
    }

    public String getWorldName() {
        return worldName;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public Material getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Portcullis other = (Portcullis) obj;
        if ((this.worldName == null) ? (other.worldName != null) : !this.worldName.equals(other.worldName)) {
            return false;
        }
        if (this.x != other.x) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.width != other.width) {
            return false;
        }
        if (this.height != other.height) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.worldName != null ? this.worldName.hashCode() : 0);
        hash = 29 * hash + this.x;
        hash = 29 * hash + this.z;
        hash = 29 * hash + this.y;
        hash = 29 * hash + this.width;
        hash = 29 * hash + this.height;
        hash = 29 * hash + (this.direction != null ? this.direction.hashCode() : 0);
        return hash;
    }

    private final String worldName;
    private final int x, z, width, height;
    private final Material type;
    private int y;
    private final BlockFace direction;
}