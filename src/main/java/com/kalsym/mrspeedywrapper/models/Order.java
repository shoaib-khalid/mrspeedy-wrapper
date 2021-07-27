package com.kalsym.mrspeedywrapper.models;


import com.kalsym.mrspeedywrapper.models.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
public class Order {

//    private String type;
//    private String matter;
//    private Integer vehicle_type_id;
//    private Integer total_weight_kg;
//    private String insured_amount;
//    private Boolean is_client_notification_enabled;
//    private Boolean is_contact_person_notifcation_enabled;
//    private Boolean is_route_optimizer_enabled;
//    private Integer loaders_count;
//    private Boolean is_motobox_required;
//    private ArrayList<Point> points;
//    private String payment_amount;
//    private String delivery_fee_amount;
//    private String weight_fee_amount;
//    private String insurance_amount;
//    private String insurance_fee_amount;
//    private String loading_fee_amount;
//    private String money_transfer_fee_amount;
//    private String suburban_delivery_fee_amount;
//    private String overnight_fee_amount;
//    private String discount_amount;
//    private String cod_fee_amount;
//    private String backpayment_details;


    private String type;
    private String order_id;
    private String order_name;
    private int vehicle_type_id;
    private Date created_datetime;
    private Date finish_datetime;
    private String status;
    private Object status_description;
    private String matter;
    private int total_weight_kg;
    private boolean is_client_notification_enabled;
    private boolean is_contact_person_notification_enabled;
    private int loaders_count;
    private String backpayment_details;
    private ArrayList<Point> points;
    private String payment_amount;
    private String delivery_fee_amount;
    private String weight_fee_amount;
    private String insurance_amount;
    private String insurance_fee_amount;
    private String loading_fee_amount;
    private String money_transfer_fee_amount;
    private String suburban_delivery_fee_amount;
    private String overnight_fee_amount;
    private String discount_amount;
    private String backpayment_amount;
    private String cod_fee_amount;
    private String backpayment_photo_url;
    private String itinerary_document_url;
    private String waybill_document_url;
    private String courier;
    private boolean is_motobox_required;
    private String payment_method;
    private String bank_card_id;


}
