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
import org.activehome.com.Request;
import org.activehome.service.Service;
import org.activehome.service.RequestHandler;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Param;
import org.kevoree.log.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
@ComponentType
public class Catalogue extends Service {

    private static String BASE_VCS_URL_RAW = "https://raw.githubusercontent.com/jackybourgeois";
    private static String BASE_VCS_URL = "https://github.com/jackybourgeois";

    @Param(defaultValue = "A catalog of Active Home components!")
    private String description;
    @Param(defaultValue = "/active-home-catalogue")
    private String src;

    @Override
    protected RequestHandler getRequestHandler(Request request) {
        return new CatalogueRequestHandler(request, this);
    }

    @Override
    public void modelUpdated() {
        if (isFirstModelUpdate()) {
            sendRequest(new Request(getFullId(), getNode() + ".http", getCurrentTime(),
                    "addHandler", new Object[]{"/catalogue", getFullId(), false}), null);
        }
        super.modelUpdated();
    }

    /**
     * Retrieve all existing Active Home Items from the Kevoree registry
     *
     * @return
     */
    public CatalogueItem[] getCatalogueItems() {
        String response = sendGet("http://registry.kevoree.org/", null);

        HashMap<String, CatalogueItem> items = new HashMap<>();
        JsonObject root = JsonObject.readFrom(response);
        for (JsonValue pack : root.get("packages").asArray()) {
            if (pack.asObject().get("name").asString().equals("org")) {
                for (JsonValue orgPack : pack.asObject().get("packages").asArray()) {
                    if (orgPack.asObject().get("name").asString().equals("activehome")) {
                        searchPackage(items, orgPack.asObject());
                    }
                }
            }
        }
        return items.values().toArray(new CatalogueItem[items.size()]);
    }

    /**
     * Search a Kevoree registry package to find Active Home items
     *
     * @param items existing items, will be fill with items found
     * @param json  the Kevoree registry package
     */
    private void searchPackage(final HashMap<String, CatalogueItem> items,
                               final JsonObject json) {
        if (json.get("packages") != null) {
            for (JsonValue packVal : json.get("packages").asArray()) {
                JsonObject jsonPack = packVal.asObject();
                for (JsonValue td : jsonPack.get("typeDefinitions").asArray()) {
                    String name = td.asObject().get("name").asString();
                    boolean isAbstract = Boolean.valueOf(td.asObject().get("abstract").asString());

                    if (isAbstract) {
                        if (!items.containsKey(name)) {
                            // search attributes
                            String description = "";
                            String src = "";
                            JsonArray dictionaryArray = td.asObject().get("dictionaryType").asArray();
                            if (dictionaryArray.size() > 0) {
                                JsonArray attrArray = dictionaryArray.get(0).asObject().get("attributes").asArray();
                                for (JsonValue attr : attrArray) {
                                    String attrName = attr.asObject().getString("name", "");
                                    String attrDefValue = attr.asObject().getString("defaultValue", "");
                                    switch (attrName) {
                                        case "description":
                                            description = attrDefValue;
                                            break;
                                        case "src":
                                            if (attrDefValue.startsWith("/")) {
                                                src = BASE_VCS_URL + attrDefValue;
                                            } else {
                                                src = attrDefValue;
                                            }
                                            break;
                                    }
                                }
                            }

                            // search package
                            String pack = "";
                            for (JsonValue md : td.asObject().get("metaData").asArray()) {
                                if (md.asObject().get("name").asString().equals("java.class")) {
                                    pack = md.asObject().get("value").asString();
                                }
                            }

                            items.put(name, new CatalogueItem(name, description, pack, src));
                        }

                        CatalogueItem item = items.get(name);

                        JsonArray superTypesArray = td.asObject().get("superTypes").asArray();
                        String superType = "";
                        for (JsonValue stValue : superTypesArray) {
                            String[] elems = stValue.asString().split("/");
                            if (elems[elems.length - 1].startsWith("typeDefinitions")) {
                                superType = elems[elems.length - 1].replace("typeDefinitions[name=", "");
                                item.getSuperTypes().add(superType.substring(0, superType.indexOf(",")));
                            }
                        }
                        item.getVersions().add(td.asObject().get("version").asString());
                    }
                    searchPackage(items, jsonPack);
                }
            }
        }
    }

    public static String sendGet(final String url,
                                 final List<String> cookieList) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");
            if (cookieList != null) {
                for (String cookie : cookieList) {
                    con.addRequestProperty("Cookie", cookie);
                }
            }

            int responseCode = con.getResponseCode();

            return inputStreamToString(con.getInputStream());

        } catch (IOException e) {
            Log.error(e.getMessage());
            return "";
        }
    }

    public static String inputStreamToString(InputStream is) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder response = new StringBuilder();

        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
        } catch (IOException e) {
            Log.error(e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                Log.error(e.getMessage());
            }
        }

        return response.toString();
    }

    public String getContentFrom(String src,
                                 String name) {
        if (src.startsWith("--")) {
            src = BASE_VCS_URL_RAW + src.replaceAll("--", "/").replaceAll("__", ".");
        }
        return sendGet(src + "/master/docs/" + name + ".md", null);
    }

}
