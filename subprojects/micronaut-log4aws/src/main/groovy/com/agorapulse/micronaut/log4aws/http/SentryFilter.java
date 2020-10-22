/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.micronaut.log4aws.http;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.Flowable;
import io.sentry.IHub;
import org.reactivestreams.Publisher;

@Filter("/**")
public class SentryFilter implements HttpServerFilter {

    private final IHub hub;

    public SentryFilter(IHub hub) {
        this.hub = hub;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Flowable
            .just(request)
            .doOnNext(r -> {
                hub.pushScope();
                hub.configureScope(s -> {
                    s.addEventProcessor(new MicronautHttpRequestEventProcessor(r));
                });
            })
            .switchMap(chain::proceed)
            .doOnNext(res -> {
                hub.popScope();
            });
    }

}