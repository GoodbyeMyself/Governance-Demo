package com.governance.platform.shared.web;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClientRequestHeaderProvider {

    public Optional<ClientRequestHeaders> getHeaders() {
        return ClientRequestContextHolder.getOptional();
    }

    public String getRequestId() {
        return getHeaders().map(ClientRequestHeaders::requestId).orElse(null);
    }

    public String getRequestTime() {
        return getHeaders().map(ClientRequestHeaders::requestTime).orElse(null);
    }

    public String getClientApp() {
        return getHeaders().map(ClientRequestHeaders::clientApp).orElse(null);
    }

    public String getTenantId() {
        return getHeaders().map(ClientRequestHeaders::tenantId).orElse(null);
    }

    public String getAuthorization() {
        return getHeaders().map(ClientRequestHeaders::authorization).orElse(null);
    }
}
