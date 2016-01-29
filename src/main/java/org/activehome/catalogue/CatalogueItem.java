package org.activehome.catalogue;

/*
 * #%L
 * Active Home :: Catalogue
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2016 Active Home Project
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


import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
public class CatalogueItem {

    private String name;
    private String description;
    private Set<String> versions;
    private String pack;
    private Set<String> superTypes;
    private String src;

    public CatalogueItem(final String name,
                         final String description,
                         final String pack,
                         final String src) {
        this.name = name;
        this.description = description;
        this.pack = pack;
        this.src = src;
    }

    public CatalogueItem(final JsonObject json) {
        name = json.getString("name", "");
        description = json.getString("description", "");
        pack = json.getString("pack","");
        src = json.getString("src", "");
        versions = new HashSet<>();
        for (JsonValue jVal : json.get("versions").asArray()) {
            versions.add(jVal.asString());
        }
        superTypes = new HashSet<>();
        for (JsonValue jVal : json.get("superTypes").asArray()) {
            superTypes.add(jVal.asString());
        }
    }

    public String getName() {
        return name;
    }

    public Set<String> getVersions() {
        return versions;
    }

    public Set<String> getSuperTypes() {
        return superTypes;
    }

    public String getPack() {
        return pack;
    }

    public String getDescription() {
        return description;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.add("type", CatalogueItem.class.getName());
        json.add("name", name);
        json.add("description", description);
        json.add("pack", pack);
        json.add("src", src);
        JsonArray jVers = new JsonArray();
        for (String vers : versions) {
            jVers.add(vers);
        }
        json.add("versions", jVers);
        JsonArray jSuperType = new JsonArray();
        for (String st : superTypes) {
            jSuperType.add(st);
        }
        json.add("superTypes", jSuperType);
        return json;
    }

    public String toString() {
        return toJson().toString();
    }

}
