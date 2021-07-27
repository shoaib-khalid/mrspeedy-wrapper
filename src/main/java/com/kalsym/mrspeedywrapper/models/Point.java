package com.kalsym.mrspeedywrapper.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Point {
//    private String address;
//    private Person contact_person;
//    private String client_order_id;
//    private String latitude;
//    private String longitude;
//    private Date required_start_datetime;
//    private Date required_finish_datetime;
//    private String taking_amount;
//    private String buyout_amount;
//    private String note;
//    private Boolean is_order_payment_here;
//    private String building_number;
//    private String entrance_number;
//    private String intercom_code;
//    private String floor_number;
//    private String apartment_number;
//    private String invisble_mile_navigation_instructions;
//    private Boolean is_cod_cash_voucher_required;
//    private Integer delivery_id;
//    private ArrayList<Package> packages;
    private String point_type;
    private String point_id;
    private String delivery_id;
    private String client_order_id;
    private String address;
    private String latitude;
    private String longitude;
    private Date required_start_datetime;
    private Date required_finish_datetime;
    private Date arrival_start_datetime;
    private Date arrival_finish_datetime;
    private Date estimated_arrival_datetime;
    private Date courier_visit_datetime;
    private Person contact_person;
    private String taking_amount;
    private String buyout_amount;
    private String note;
    private ArrayList<Package> packages;
    private Boolean is_cod_cash_voucher_required;
    private String place_photo_url;
    private String sign_photo_url;
    private String tracking_url;
    private String checkin;
    private Boolean is_order_payment_here;
    private String building_number;
    private String entrance_number;
    private String intercom_code;
    private String floor_number;
    private String apartment_number;
    private String invisible_mile_navigation_instructions;

}
