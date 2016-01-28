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


import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.activehome.com.Request;
import org.activehome.com.RequestCallback;
import org.activehome.com.error.Error;
import org.activehome.com.error.ErrorType;
import org.activehome.service.Service;
import org.activehome.service.RequestHandler;
import org.activehome.tools.Util;
import org.activehome.context.data.UserInfo;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Param;
import org.kevoree.annotation.Start;
import org.kevoree.api.handler.UUIDModel;
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
public class Store extends Service {

    private static String BASE_VCS_URL = "https://raw.githubusercontent.com/jackybourgeois";

    @Param(defaultValue = "A catalog of component!")
    private String description;
    @Param(defaultValue = "/activehome-store/master/docs/store.png")
    private String img;
    @Param(defaultValue = "/activehome-store/master/docs/store.md")
    private String doc;
    @Param(defaultValue = "/activehome-store/master/docs/demo.kevs")
    private String demoScript;

    @Override
    protected RequestHandler getRequestHandler(Request request) {
        return new StoreRequestHandler(request, this);
    }

    public JsonArray getLibrary(UserInfo userInfo) {
        StringBuilder groups = new StringBuilder();
        for (int i = 0; i < userInfo.getGroups().length; i++) {
            groups.append("?");
            if (i < userInfo.getGroups().length - 1) groups.append(",");
        }
        JsonArray widgets = new JsonArray();

        JsonObject widget = new JsonObject();
//                widget.add("id",result.getString("id"));
//                widget.add("title",result.getString("title"));
//                widget.add("desc",result.getString("desc"));
//
//                JsonArray imports = new JsonArray();
//                for (String imp : result.getString("import").split(",")) imports.add(imp);
//                widget.add("import",imports);
//
//                widget.add("img",result.getString("img"));
//                widget.add("defaultWidth", result.getDouble("defaultWidth"));
//                widget.add("defaultHeight", result.getDouble("defaultHeight"));
//                widget.add("defaultAttributes", JsonObject.readFrom(result.getString("defaultAttributes")));
//                widgets.add(widget);

        return widgets;
    }

    @Override
    public void modelUpdated() {
        if (isFirstModelUpdate()) {
            sendRequest(new Request(getFullId(), getNode() + ".http", getCurrentTime(),
                    "addHandler", new Object[]{"/store", getFullId(), false}), null);
        }
        super.modelUpdated();
    }

    public StoreItem[] getStoreItems() {
        String response = sendGet("http://registry.kevoree.org/", null);

        HashMap<String, StoreItem> items = new HashMap<>();
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
        return items.values().toArray(new StoreItem[items.size()]);
    }

    private void searchPackage(final HashMap<String, StoreItem> items,
                               final JsonObject json) {
        if (json.get("packages") != null) {
            for (JsonValue packVal : json.get("packages").asArray()) {
                JsonObject jsonPack = packVal.asObject();
                if (accept(jsonPack.getString("name", ""))) {
                    for (JsonValue td : jsonPack.get("typeDefinitions").asArray()) {
                        String name = td.asObject().get("name").asString();
                        String version = td.asObject().get("version").asString();
                        boolean isAbstract = Boolean.valueOf(td.asObject().get("abstract").asString());

                        JsonArray superTypesArray = td.asObject().get("superTypes").asArray();
                        String superType = "";
                        for (JsonValue stValue : superTypesArray) {
                            String[] elems = stValue.asString().split("/");
                            if (elems[elems.length - 1].startsWith("typeDefinitions")) {
                                superType = elems[elems.length - 1].replace("typeDefinitions[name=", "");
                                superType = superType.substring(0, superType.indexOf(","));
                            }
                        }

                        String description = "";
                        String img = "";
                        String doc = "";
                        String demoScript = "";
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
                                    case "img":
                                        if (attrDefValue.startsWith("/")) {
                                            img = BASE_VCS_URL + attrDefValue;
                                        } else {
                                            img = attrDefValue;
                                        }
                                        break;
                                    case "doc":
                                        if (attrDefValue.startsWith("/")) {
                                            doc = attrDefValue;
                                        } else {
                                            doc = attrDefValue;
                                        }
                                        break;
                                    case "demoScript":
                                        if (attrDefValue.startsWith("/")) {
                                            demoScript = attrDefValue;
                                        } else {
                                            demoScript = attrDefValue;
                                        }
                                        break;
                                }
                            }
                        }

                        String pack = "";
                        for (JsonValue md : td.asObject().get("metaData").asArray()) {
                            if (md.asObject().get("name").asString().equals("java.class")) {
                                pack = md.asObject().get("value").asString();
                            }
                        }
                        if (!name.contains("Test") && !isAbstract) {
                            if (!items.containsKey(name) || items.get(name).hasOlderVersionThan(version)) {
                                items.put(name, new StoreItem(name, version, pack, description,
                                        img, doc, demoScript, superType));
                            }
                        }
                    }
                    searchPackage(items, jsonPack);
                }
            }
        }
    }

    public boolean accept(String name) {
        return !(name.equals("service") || name.equals("api"));
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

    public String getContentFrom(String url) {
        if (url.startsWith("/")) {
            url = BASE_VCS_URL + url;
        }
        return sendGet(url, null);
    }

}
