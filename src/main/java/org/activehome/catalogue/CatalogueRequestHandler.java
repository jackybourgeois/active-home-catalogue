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


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.activehome.com.Request;
import org.activehome.com.RequestCallback;
import org.activehome.service.RequestHandler;
import org.activehome.tools.file.FileHelper;
import org.activehome.tools.file.TypeMime;

/**
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
public class CatalogueRequestHandler implements RequestHandler {

    private final Catalogue service;
    private final Request request;

    public CatalogueRequestHandler(Request request, Catalogue service) {
        this.service = service;
        this.request = request;
    }


    public void html(final RequestCallback callback) {
        JsonObject wrap = new JsonObject();
        wrap.add("name", "catalogue-view");
        wrap.add("url", service.getId() + "/catalogue-view.html");
        wrap.add("title", "Active Home Catalogue");
        wrap.add("description", "Active Home Catalogue");

        JsonObject json = new JsonObject();
        json.add("wrap", wrap);
        callback.success(json);
    }

    public void docs(final String src,
                    final String name,
                    final RequestCallback callback) {
        callback.success(service.getContentFrom(src, name));
    }

    public void pushDemo(final String src,
                         final String name,
                         final RequestCallback callback) {
        service.pushDemo(src, name, callback);
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

    public CatalogueItem[] getCatalogueItems() {
        return service.getCatalogueItems();
    }

}
