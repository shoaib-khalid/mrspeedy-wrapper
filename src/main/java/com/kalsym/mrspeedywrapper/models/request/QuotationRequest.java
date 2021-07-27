package com.kalsym.mrspeedywrapper.models.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kalsym.mrspeedywrapper.models.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuotationRequest {

    private String type;
    private String matter;
    private Integer vehicle_type_id;
    private Double total_weight_kg;
    private String insured_amount;
    private Boolean is_client_notification_enabled;
    private Boolean is_contact_person_notification_enabled;
    private Boolean is_route_optimizer_enabled;
    private Integer loaders_count;
    private String backpayment_details;
    private Boolean is_motobox_required;
    private String payment_method;
    private Integer bank_card_id;
    private ArrayList<Point> points;

    public QuotationRequest() {
        type = null;
        matter = null;
        vehicle_type_id = null;
        total_weight_kg = null;
        insured_amount = null;
        is_client_notification_enabled = false;
        is_contact_person_notification_enabled = false;
        is_route_optimizer_enabled = false;
        loaders_count = null;
        backpayment_details = null;
        is_motobox_required = null;
        payment_method = null;
        bank_card_id = null;
        points = new ArrayList<>();
    }
}
