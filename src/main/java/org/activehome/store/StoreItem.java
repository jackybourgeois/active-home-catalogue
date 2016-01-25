package org.activehome.store;

/*
 * #%L
 * Active Home :: Store
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2016 org.activehome
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.eclipsesource.json.JsonObject;

/**
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
public class StoreItem {

    private String name;
    private String version;
    private String pack;

    public StoreItem(final String name,
                     final String version,
                     final String pack) {
        this.name = name;
        this.version = version;
        this.pack = pack;
    }

    public StoreItem(final JsonObject json) {
        this.name = json.get("name").asString();
        this.version = json.get("version").asString();
        this.pack = json.get("pack").asString();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getPack() {
        return pack;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.add("type", StoreItem.class.getName());
        json.add("name", name);
        json.add("version", version);
        json.add("pack", pack);
        return json;
    }

    public String toString() {
        return toJson().toString();
    }
}
