/*
 * Copyright 2016 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.servlet.mvc;

import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.servlet.filter.ContentNegotiationResolver;
import com.stormpath.sdk.servlet.http.MediaType;
import com.stormpath.sdk.servlet.http.UnresolvedMediaTypeException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.stormpath.sdk.servlet.mvc.View.STORMPATH_JSON_VIEW_NAME;

/**
 * @since 1.0.0
 */
public class DefaultViewResolver implements ViewResolver {

    private final ViewResolver delegateViewResolver;

    protected final List<MediaType> producesMediaTypes;

    private final View jsonView;

    public DefaultViewResolver(ViewResolver delegate, View jsonView, List<MediaType> producesMediaTypes) {
        Assert.notNull(delegate, "Delegate ViewResolver cannot be null.");
        Assert.notNull(jsonView, "JSON View cannot be null.");
        this.delegateViewResolver = delegate;
        this.jsonView = jsonView;
        this.producesMediaTypes = producesMediaTypes;
    }

    @Override
    public View getView(ViewModel model, HttpServletRequest request) {
        try {
            MediaType mediaType = ContentNegotiationResolver.INSTANCE.getContentType(request, null, producesMediaTypes);
            //Check for explicit stormpathJsonView in case of /me it should always render JSON
            if (mediaType.equals(MediaType.APPLICATION_JSON) || STORMPATH_JSON_VIEW_NAME.equals(model.getViewName())) {
                return jsonView;
            }
            if (mediaType.equals(MediaType.TEXT_HTML)) {
                return delegateViewResolver.getView(model, request);
            }
        } catch (UnresolvedMediaTypeException e) {
            //No MediaType could be resolved for this request based on the produces setting. Let's return null
        }

        return null;
    }
}
