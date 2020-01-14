package com.stakater.nordmart.catalog.messaging;

import com.stakater.nordmart.catalog.domain.Product;

public interface MessageSender {

    void sendCreate(Product product);

    void sendUpdate(Product product);

    void sendDelete(String itemId);
}
