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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author pepijn
 */
@EqualsAndHashCode
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

    @Getter
    private final String worldName;
    @Getter
    private final int x, z, width, height;
    @Getter
    private final Material type;
    @Getter
    @Setter
    private int y;
    @Getter
    private final BlockFace direction;
}