package com.kalsym.mrspeedywrapper.models.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DeliveryStatus {
    BEING_DELIVERED("active", "courier_assigned", "courier_departed", "parcel_picked_up", "courier_arrived"),
    BEING_PREPAIRED(),
    DELIVERED_TO_CUSTOMER("finished"),
    CANCELED_BY_CUSTOMER("canceled"),
    PAYMENT_CONFIRMED,
    READY_FOR_DELIVERY(),
    RECEIVED_AT_STORE,
    REFUNDED,
    REJECTED_BY_STORE,
    REQUESTING_DELIVERY_FAILED,
    AWAITING_PICKUP("planned"),
    FAILED("failed", "deleted");



    static final private Map<String, DeliveryStatus> ALIAS_MAP = new HashMap<String, DeliveryStatus>();

    static {
        for (DeliveryStatus deliveryStatus : DeliveryStatus.values()) {

            ALIAS_MAP.put(deliveryStatus.name(), deliveryStatus);
            deliveryStatus.aliases.forEach(alias -> {
                ALIAS_MAP.put(alias, deliveryStatus);
            });
        }
    }

    static public boolean has(String value) {

        return ALIAS_MAP.containsKey(value);
    }

    static public DeliveryStatus fromString(String value) {
        if (value == null) {
            throw new NullPointerException("alias null");
        }
        DeliveryStatus deliveryStatus = ALIAS_MAP.get(value);
        if (deliveryStatus == null) {
            throw new IllegalArgumentException("Not an alias: " + value);
        }
        return deliveryStatus;
    }

    private List<String> aliases;

    private DeliveryStatus(String... aliases) {
        this.aliases = Arrays.asList(aliases);
    }
}
