package org.fakereplace.integration.wildfly.autoupdate;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Stuart Douglas
 */
public class WebUpdateHandlerWrapper implements HandlerWrapper {

    public static final int UPDATE_INTERVAL = 1000;

    public static final HandlerWrapper INSTANCE = new WebUpdateHandlerWrapper();

    @Override
    public HttpHandler wrap(HttpHandler httpHandler) {
        return new WebUpdateHandler(httpHandler);
    }

    public static class WebUpdateHandler implements HttpHandler {
        private final HttpHandler httpHandler;
        private final AtomicLong nextUpdate = new AtomicLong();

        public WebUpdateHandler(HttpHandler httpHandler) {
            this.httpHandler = httpHandler;
        }

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            long next = nextUpdate.get();
            if (System.currentTimeMillis() > next) {
                if (nextUpdate.compareAndSet(next, System.currentTimeMillis() + UPDATE_INTERVAL)) {
                    WildflyAutoUpdate.runUpdate(Thread.currentThread().getContextClassLoader());
                }
            }


            httpHandler.handleRequest(httpServerExchange);
        }
    }
}
