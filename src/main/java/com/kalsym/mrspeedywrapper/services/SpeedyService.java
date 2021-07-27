package com.kalsym.mrspeedywrapper.services;

import com.google.gson.Gson;
import com.kalsym.mrspeedywrapper.models.Order;
import com.kalsym.mrspeedywrapper.models.Person;
import com.kalsym.mrspeedywrapper.models.Point;
import com.kalsym.mrspeedywrapper.models.request.QuotationRequest;
import com.kalsym.mrspeedywrapper.models.response.QuotationResponse;
import com.kalsym.mrspeedywrapper.models.response.Response;
import com.kalsym.mrspeedywrapper.repositories.DeliveryRepository;
import com.kalsym.mrspeedywrapper.repositories.DropoffDetailsRepository;
import com.kalsym.mrspeedywrapper.repositories.PickupDetailsRepository;
import com.kalsym.mrspeedywrapper.repositories.TrackingInfoRepository;
import com.kalsym.parentwrapper.models.Delivery;
import com.kalsym.parentwrapper.models.DropoffDetails;
import com.kalsym.parentwrapper.models.PickupDetails;
import com.kalsym.parentwrapper.models.TrackingInfo;
import com.kalsym.parentwrapper.models.enums.Status;
import com.kalsym.parentwrapper.models.mqtt.StatusMessage;
import com.kalsym.parentwrapper.mqtt.MqttPublisher;
import com.kalsym.parentwrapper.services.ParentService;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class SpeedyService extends ParentService {
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

    @Override
    public Delivery addQuotation(Delivery delivery) {
        RestTemplate restTemplate = new RestTemplate();
        QuotationRequest orderDetails = new QuotationRequest();
//        orderDetails.setMatter("documents");
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

        System.out.println(requestBody);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-DV-Auth-Token", MR_SPEEDY_KEY);
        HttpEntity<String> req =new HttpEntity<>(requestBody.toString(), headers);

        System.out.println(requestBody.toString());
        JSONObject responseJson = new JSONObject(restTemplate.exchange(BASE_URL+ENDPOINT_QUOTATION, HttpMethod.POST, req, String.class).getBody());
        Response response = new Gson().fromJson(responseJson.toString(), Response.class);

        delivery.setCharges(Double.parseDouble(response.getOrder().getDelivery_fee_amount()));
        delivery.setStatus(Status.QUOTED);

        System.out.println(new JSONObject(response).toString());


        PickupDetails pickupDetails = delivery.getPickupDetails();
        DropoffDetails dropoffDetails = delivery.getDropoffDetails();
        TrackingInfo trackingInfo = delivery.getTrackingInfo();

        pickupDetails.setLatitude(Double.parseDouble(response.getOrder().getPoints().get(0).getLatitude()));
        pickupDetails.setLongitude(Double.parseDouble(response.getOrder().getPoints().get(0).getLongitude()));

        dropoffDetails.setLatitude(Double.parseDouble(response.getOrder().getPoints().get(1).getLatitude()));
        dropoffDetails.setLongitude(Double.parseDouble(response.getOrder().getPoints().get(1).getLongitude()));



        delivery.setObjectsNull();

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

        return delivery;
    }

    @Override
    public Delivery placeDeliveryOrder(String quotationId) {
//        String dpRefId = "12316892173";
//        String trackingNo = "8934262374";

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

        System.out.println(new JSONObject(request).toString());


        JSONObject responseJson = new JSONObject(restTemplate.exchange(BASE_URL+ENDPOINT_PLACE_ORDER, HttpMethod.POST, req, String.class).getBody());
        Response response = new Gson().fromJson(responseJson.toString(), Response.class);

        trackingInfo.setTrackingNo(response.getOrder().getPoints().get(0).getTracking_url());

        System.out.println(new JSONObject(response).toString());

        delivery.setDpRefId(response.getOrder().getOrder_id());

        delivery.setStatus(Status.BEING_DELIVERED_TO_CUSTOMER);

        trackingInfoRepository.save(trackingInfo);
        Delivery placedDeliveryOrder = deliveryRepository.save(delivery);

        try {
            MqttPublisher publisher = new MqttPublisher("192.168.0.201:3040","publisher");
            StatusMessage statusMessage = new StatusMessage(Status.BEING_DELIVERED_TO_CUSTOMER);
            JSONObject message = new JSONObject(statusMessage);

            publisher.sendMessage(message, "orders/"+delivery.getSfRefId()+"/status-update");
        } catch (MqttException e) {
            System.out.println("Error Creating publisher");
        }

        return placedDeliveryOrder;
    }
}
