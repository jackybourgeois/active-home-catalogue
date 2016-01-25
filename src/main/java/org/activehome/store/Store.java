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
import org.kevoree.annotation.Start;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.log.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
@ComponentType
public class Store extends Service {

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
        LinkedList<StoreItem> items = new LinkedList<>();
        HashMap<String, Object> response = sendGet("http://registry.kevoree.org/", null);
        if (response != null) {
            JsonObject root = JsonObject.readFrom((String) response.get("content"));
            for (JsonValue pack : root.get("packages").asArray()) {
                if (pack.asObject().get("name").asString().equals("org")) {
                    for (JsonValue orgPack : pack.asObject().get("packages").asArray()) {
                        if (orgPack.asObject().get("name").asString().equals("activehome")) {
                            items.addAll(searchPackage(orgPack.asObject()));
                        }
                    }
                }
            }
        }
        return items.toArray(new StoreItem[items.size()]);
    }

    private LinkedList<StoreItem> searchPackage(final JsonObject jsonPack) {
        LinkedList<StoreItem> items = new LinkedList<>();
        if (jsonPack.get("packages") != null) {
            for (JsonValue ahPack : jsonPack.get("packages").asArray()) {
                for (JsonValue td : ahPack.asObject().get("typeDefinitions").asArray()) {
                    String version = td.asObject().get("version").asString();
                    String name = td.asObject().get("name").asString();
                    String pack = "";
                    for (JsonValue md : td.asObject().get("metaData").asArray()) {
                        if (md.asObject().get("name").asString().equals("java.class")) {
                            pack = md.asObject().get("value").asString();
                        }
                    }
                    items.add(new StoreItem(name, version, pack));
                }
                items.addAll(searchPackage(ahPack.asObject()));
            }
        }
        return items;
    }

    public static HashMap<String, Object> sendGet(final String url,
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

            HashMap<String, Object> responseMap = new HashMap<>();

            int responseCode = con.getResponseCode();
            Map<String, List<String>> respHeaderMap = con.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : respHeaderMap.entrySet()) {
                if (entry.getKey() != null && entry.getKey().compareTo("Set-Cookie") == 0)
                    responseMap.put("Set-Cookie", entry.getValue());
            }

            responseMap.put("content", inputStreamToString(con.getInputStream()));

            return responseMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String inputStreamToString(InputStream is) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder response = new StringBuilder();

        try {
            while ((inputLine = in.readLine()) != null) response.append(inputLine);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return response.toString();
    }

}
