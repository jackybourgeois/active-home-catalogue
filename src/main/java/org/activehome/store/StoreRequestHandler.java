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
import com.eclipsesource.json.JsonValue;
import org.activehome.com.Request;
import org.activehome.com.error.*;
import org.activehome.com.error.Error;
import org.activehome.service.RequestHandler;
import org.activehome.tools.file.FileHelper;
import org.activehome.tools.file.TypeMime;
import org.activehome.context.data.UserInfo;

/**
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
public class StoreRequestHandler implements RequestHandler {

    private final Store service;
    private final Request request;

    public StoreRequestHandler(Request request, Store service) {
        this.service = service;
        this.request = request;
    }


    public Object html() {
        JsonObject wrap = new JsonObject();
        wrap.add("name", "store-view");
        wrap.add("url", service.getId() + "/store-view_backup.html");
        wrap.add("title", "Active Home Store");
        wrap.add("description", "Active Home Store");

        JsonObject json = new JsonObject();
        json.add("wrap", wrap);
        return json;
    }

    public JsonValue file(String str) {
        String content = FileHelper.fileToString(str, getClass().getClassLoader());
        if (str.endsWith(".html")) {
            content = content.replaceAll("\\$\\{id\\}", service.getId());
            content = content.replaceAll("\\$\\{node\\}", service.getNode());
        }
        JsonObject json = new JsonObject();
        json.add("content", content);
        json.add("mime", TypeMime.valueOf(str.substring(str.lastIndexOf(".") + 1, str.length())).getDesc());
        return json;
    }

    public Object library() {
        Object userInfo = request.getEnviElem().get("userInfo");
        if (userInfo!=null && userInfo instanceof UserInfo) {
            JsonObject json = new JsonObject();
            json.add("library", service.getLibrary((UserInfo) userInfo));
            return json;
        }
        return new Error(ErrorType.PERMISSION_DENIED,"User info missing.");
    }

    public StoreItem[] getStoreItems() {
        return service.getStoreItems();
    }

}
