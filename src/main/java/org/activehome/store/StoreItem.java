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
    private String description;
    private String img;

    public StoreItem(final String name,
                     final String version,
                     final String pack,
                     final String description,
                     final String img) {
        this.name = name;
        this.version = version;
        this.pack = pack;
        this.description = description;
        this.img = img;
    }

    public StoreItem(final JsonObject json) {
        this.name = json.getString("name", "");
        this.version = json.getString("version","");
        this.pack = json.getString("pack","");
        this.description = json.getString("description", "");
        this.img = json.getString("img", "");
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
        json.add("description", description);
        json.add("img", img);
        return json;
    }

    public String toString() {
        return toJson().toString();
    }
}
