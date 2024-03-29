package com.kalsym.mrspeedywrapper.services;

import com.google.gson.Gson;
import com.kalsym.mrspeedywrapper.models.Person;
import com.kalsym.mrspeedywrapper.models.Point;
import com.kalsym.mrspeedywrapper.models.request.QuotationRequest;
import com.kalsym.mrspeedywrapper.models.response.Response;
import com.kalsym.mrspeedywrapper.repositories.DeliveryRepository;
import com.kalsym.mrspeedywrapper.repositories.DropoffDetailsRepository;
import com.kalsym.mrspeedywrapper.repositories.PickupDetailsRepository;
import com.kalsym.mrspeedywrapper.repositories.TrackingInfoRepository;
import com.kalsym.parentwrapper.models.*;
import com.kalsym.parentwrapper.models.enums.Status;
import com.kalsym.parentwrapper.models.mqtt.StatusMessage;
import com.kalsym.parentwrapper.mqtt.MqttPublisher;
import com.kalsym.parentwrapper.services.DeliveryService;
import com.kalsym.parentwrapper.utils.LogUtil;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.remoting.httpinvoker.HttpInvokerClientInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SpeedyService extends DeliveryService {
    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DropoffDetailsRepository dropoffDetailsRepository;

    @Autowired
    private PickupDetailsRepository pickupDetailsRepository;

    @Autowired
    private TrackingInfoRepository trackingInfoRepository;

    @Value("${mrspeedy.base.url}")
    private String BASE_URL;

    @Value("${mrspeedy.endpoint.place.order}")
    private String ENDPOINT_PLACE_ORDER;

    @Value("${mrspeedy.endpoint.quotation}")
    private String ENDPOINT_QUOTATION;

    @Value("${mrspeedy.key}")
    private String MR_SPEEDY_KEY;

    @Value("${status.being_delivered}")
    private String STATUS_BEING_DELIVERED;

    @Value("${status.delivered_to_customer}")
    private String STATUS_DELIVERED_TO_CUSTOMER;

    @Value("${mqtt.url}")
    private String MQTT_URL;

    @Value("${mqtt.clientId}")
    private String MQTT_CLIENT_ID;

    @Value("${mqtt.message.template}")
    private String MQTT_MESSAGE_TEMPLATE;


    @Override
    public Delivery addQuotation(HttpServletRequest httpServletRequest, Delivery delivery) {

        String logprefix = httpServletRequest.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();

        LogUtil.info(logprefix, location, "ENTERED METHOD addQuotation", "");


        RestTemplate restTemplate = new RestTemplate();
        QuotationRequest orderDetails = new QuotationRequest();

        if(delivery.getVehicleType().ordinal() == 1){
            orderDetails.setVehicle_type_id(8);
        }
        else{
            orderDetails.setVehicle_type_id(7);
        }
        orderDetails.setTotal_weight_kg(delivery.getWeight());

        ArrayList<Point> points = new ArrayList<>();

        Point p1 = new Point();
        p1.setAddress(delivery.getPickupDetails().getAddress());
        p1.setContact_person(new Person(delivery.getPickupDetails().getContactPhone(), delivery.getDropoffDetails().getContactName()));
        points.add(p1);
        Point p2 = new Point();
        p2.setAddress(delivery.getDropoffDetails().getAddress());
        p2.setContact_person(new Person(delivery.getDropoffDetails().getContactPhone(), delivery.getDropoffDetails().getContactName()));
        points.add(p2);

        orderDetails.setPoints(points);
        JSONObject requestBody = new JSONObject(orderDetails);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-DV-Auth-Token", MR_SPEEDY_KEY);
        HttpEntity<String> request =new HttpEntity<>(requestBody.toString(), headers);

        LogUtil.info(logprefix, location, "METHOD POST, URL : ",BASE_URL+ENDPOINT_QUOTATION);

        LogUtil.info(logprefix, location, "REQUEST BODY FOR "+BASE_URL+ENDPOINT_QUOTATION, new JSONObject(request.getBody()).toString());

        Response response= restTemplate.exchange(BASE_URL+ENDPOINT_QUOTATION, HttpMethod.POST, request, Response.class).getBody();

//        JSONObject responseJson = new JSONObject();
//        Response response = new Gson().fromJson(responseJson.toString(), Response.class);

        LogUtil.info(logprefix, location, "RESPONSE FROM : "+BASE_URL+ENDPOINT_QUOTATION, new JSONObject(response).toString());

        delivery.setCharges(Double.parseDouble(response.getOrder().getDelivery_fee_amount()));
        delivery.setStatus(Status.QUOTED);

        PickupDetails pickupDetails = delivery.getPickupDetails();
        DropoffDetails dropoffDetails = delivery.getDropoffDetails();
        TrackingInfo trackingInfo = delivery.getTrackingInfo();

        pickupDetails.setLatitude(Double.parseDouble(response.getOrder().getPoints().get(0).getLatitude()));
        pickupDetails.setLongitude(Double.parseDouble(response.getOrder().getPoints().get(0).getLongitude()));

        dropoffDetails.setLatitude(Double.parseDouble(response.getOrder().getPoints().get(1).getLatitude()));
        dropoffDetails.setLongitude(Double.parseDouble(response.getOrder().getPoints().get(1).getLongitude()));

        //to avoid foreign key conflict on first time save
        delivery.setObjectsNull();

        try{
            delivery = deliveryRepository.save(delivery);

            pickupDetails.setDeliveryId(delivery.getId());
            dropoffDetails.setDeliveryId(delivery.getId());
            trackingInfo.setDeliveryId(delivery.getId());

            pickupDetailsRepository.save(pickupDetails);
            dropoffDetailsRepository.save(dropoffDetails);
            trackingInfoRepository.save(trackingInfo);

            delivery.setDropoffDetails(dropoffDetails);
            delivery.setPickupDetails(pickupDetails);
            delivery.setTrackingInfo(trackingInfo);

            LogUtil.info(logprefix, location, "DELIVERY QUOTATION DETAILS : ", new JSONObject(delivery).toString());

            LogUtil.info(logprefix, location, "EXITING METHOD addQuotation","");


        }catch (Exception e)
        {
            LogUtil.error(logprefix,location, "ERROR SAVING DELIVERY TO DATABASE", "", e);
            delivery = null;
        }finally {
            return delivery;
        }
    }

    @Override
    public Delivery placeDeliveryOrder(HttpServletRequest servletRequest, String quotationId) {

        String logprefix = servletRequest.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();

        LogUtil.info(logprefix, location, "ENTERED METHOD placeDeliveryOrder", "");

        Optional<Delivery> optDelivery = deliveryRepository.findById(quotationId);

        if(!optDelivery.isPresent()){
            return null;
        }

        Delivery delivery = optDelivery.get();
        Optional<TrackingInfo> optTrackingInfo = trackingInfoRepository.findById(quotationId);
        if(!optTrackingInfo.isPresent()){
            return null;
        }
        TrackingInfo trackingInfo = optTrackingInfo.get();

        if(delivery.getStatus().equals(Status.BEING_DELIVERED)){
            LogUtil.warn(logprefix, location, "ORDER ALREADY PLACED", "");
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "ORDER ALREADY PLACED !");
        }

        QuotationRequest request = new QuotationRequest();
        request.setMatter("documents");

        ArrayList<Point> points = new ArrayList<>();
        Point p1 = new Point();
        p1.setAddress(delivery.getPickupDetails().getAddress());
        p1.setContact_person(new Person(delivery.getPickupDetails().getContactPhone(), delivery.getDropoffDetails().getContactName()));
        points.add(p1);
        Point p2 = new Point();
        p2.setAddress(delivery.getDropoffDetails().getAddress());
        p2.setContact_person(new Person(delivery.getDropoffDetails().getContactPhone(), delivery.getDropoffDetails().getContactName()));
        points.add(p2);

        request.setPoints(points);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-DV-Auth-Token", MR_SPEEDY_KEY);
        HttpEntity<QuotationRequest> req =new HttpEntity<>(request, headers);

        LogUtil.info(logprefix, location, "METHOD POST, URL : ",BASE_URL+ENDPOINT_PLACE_ORDER);

        LogUtil.info(logprefix, location, "REQUEST BODY FOR "+BASE_URL+ENDPOINT_PLACE_ORDER, new JSONObject(req.getBody()).toString());

//        LogUtil.info(logprefix, location, "MrSpeedy Request body: ", new JSONObject(req.getBody()).toString());


        Response response = restTemplate.exchange(BASE_URL+ENDPOINT_PLACE_ORDER, HttpMethod.POST, req, Response.class).getBody();

        LogUtil.info(logprefix, location, "RESPONSE FROM : "+BASE_URL+ENDPOINT_PLACE_ORDER, new JSONObject(response).toString());

        trackingInfo.setTrackingNo(response.getOrder().getPoints().get(0).getTracking_url());

        delivery.setDpRefId(response.getOrder().getOrder_id());

        delivery.setStatus(Status.BEING_DELIVERED);

        Delivery placedDeliveryOrder;

        try{
            trackingInfoRepository.save(trackingInfo);
            placedDeliveryOrder = deliveryRepository.save(delivery);
        }catch (Exception e){
            LogUtil.error(logprefix, location, "ERROR SAVING DELIVERY TO DATABASE", "", e);
            placedDeliveryOrder = null;
            throw new HttpClientErrorException(HttpStatus.resolve(503),"Error Saving to Database");
        }

        try {
            MqttPublisher publisher = new MqttPublisher(MQTT_URL,MQTT_CLIENT_ID);
            StatusMessage statusMessage = new StatusMessage(Status.BEING_DELIVERED);
            JSONObject message = new JSONObject(statusMessage);
            String topic = MQTT_MESSAGE_TEMPLATE.replace("_", delivery.getSfRefId());
            publisher.sendMessage(message, topic);

            //"orders/"+delivery.getSfRefId()+"/status-update"

            LogUtil.info(logprefix, location, "Mqtt Topic : "+topic+" Mqtt Message : "+message,"" );

        } catch (MqttException e) {
            LogUtil.error(logprefix, location, "MQTT PUBLISHER COULD NOT CONNECT TO BROKER", " ",e);
        }

        return placedDeliveryOrder;
    }

    public Delivery updateOnCallback(String dpRefId, String status){

        String id = deliveryRepository.findByOrderId(dpRefId);

        Optional<Delivery> optDelivery = deliveryRepository.findById(id);

        Delivery delivery;

        if(optDelivery.isPresent()){
            delivery = optDelivery.get();
        }
        else{
            delivery = null;
        }

        List<String> beingDeliveredStatusList = Arrays.asList(STATUS_BEING_DELIVERED.split(","));
        List<String> deliveredToCustomer = Arrays.asList(STATUS_DELIVERED_TO_CUSTOMER.split(","));
        if(beingDeliveredStatusList.contains(status)){
            delivery.setStatus(Status.BEING_DELIVERED);
        }

        else if(deliveredToCustomer.contains(status)){
            delivery.setStatus(Status.DELIVERED_TO_CUSTOMER);
        }

        return deliveryRepository.save(delivery);
    }

    public Status getPreviousStatus(String dpRefId){
        String id = deliveryRepository.findByOrderId(dpRefId).toString();
        Optional<Delivery> optionalDelivery = deliveryRepository.findById(id);
        Delivery delivery;
        if(optionalDelivery.isPresent()){
            delivery = optionalDelivery.get();
        }
        else{
            delivery = null;
        }

        return delivery.getStatus();
    }

    public void publishStatus(Delivery delivery, String logprefix){

        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        StatusMessage statusMessage = new StatusMessage(delivery.getStatus());
        JSONObject message = new JSONObject(statusMessage);
        String topic = MQTT_MESSAGE_TEMPLATE.replace("_", delivery.getSfRefId());

        try {
            MqttPublisher publisher = new MqttPublisher(MQTT_URL,MQTT_CLIENT_ID);
            publisher.sendMessage(message, topic);
            publisher.getPublisher().close();
        } catch (MqttException e) {
            LogUtil.error(logprefix, location, "MQTT PUBLISHER COULD NOT CONNECT TO BROKER", " ",e);
        }finally {
            LogUtil.info(logprefix, location, "Mqtt Topic : "+topic+" Mqtt Message : "+message, "");
        }
    }
}
