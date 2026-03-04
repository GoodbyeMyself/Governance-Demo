package com.governance.platform.shared.web;

import java.util.Optional;

public final class ClientRequestContextHolder {

    private static final ThreadLocal<ClientRequestHeaders> CONTEXT = new ThreadLocal<>();

    private ClientRequestContextHolder() {
    }

    public static void set(ClientRequestHeaders headers) {
        CONTEXT.set(headers);
    }

    public static ClientRequestHeaders get() {
        return CONTEXT.get();
    }

    public static Optional<ClientRequestHeaders> getOptional() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
