package com.kalsym.mrspeedywrapper.controllers;

import com.kalsym.mrspeedywrapper.services.SpeedyService;
import com.kalsym.parentwrapper.controllers.DeliveryController;
import com.kalsym.parentwrapper.models.Delivery;
import com.kalsym.parentwrapper.models.HttpResponse;
import com.kalsym.parentwrapper.models.enums.Status;
import com.kalsym.parentwrapper.models.mqtt.StatusMessage;
import com.kalsym.parentwrapper.mqtt.MqttPublisher;
import com.kalsym.parentwrapper.utils.LogUtil;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class MrSpeedyController {

    @Autowired
    private SpeedyService deliveryService;

    DeliveryController parentController = new DeliveryController();

    @PostMapping(path = {"/quotation/generate"} , name = "get-quotation")
    public ResponseEntity<HttpResponse> getPrice(HttpServletRequest request,
                                                 @Valid @RequestBody Delivery delivery){

        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();

        LogUtil.info(logprefix, location, "Quotation Request body: ", new JSONObject(delivery).toString());

        HttpResponse response = parentController.getPrice(request, delivery, deliveryService);

//        LogUtil.warn(logprefix, location, "Quotation Response: ", new JSONObject(response).toString());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {"/quotation/{quotationId}/order"}, name = "place-delivery-order")
    public ResponseEntity<HttpResponse> placeDeliveryOrder (HttpServletRequest request,
                                                            @PathVariable String quotationId) {

        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();

        LogUtil.info(logprefix, location, "PLACE ORDER Request body: ", quotationId);

        HttpResponse response = parentController.placeDeliveryOrder(request, quotationId, deliveryService);

        LogUtil.info(logprefix,location, "PLACE ORDER RESPONSE ", "" + new JSONObject(response));

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @PostMapping(path = {"/callback"}, name = "mrspeedy-callback")
    public ResponseEntity<HttpResponse> callback(HttpServletRequest request,
                                                  @Valid @RequestBody Object requestBody){

        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();

        LogUtil.info(logprefix, location, "ENTERED callback", "");


        HttpResponse response = new HttpResponse();
        response.setSuccessStatus(com.kalsym.parentwrapper.models.enums.HttpStatus.OK);
        response.setData(requestBody);
        JSONObject objectJson = new JSONObject(requestBody);

        LogUtil.info(logprefix, location, "Data from : "+request.getRemoteAddr(),objectJson.toString());


        String status = objectJson.getJSONObject("delivery").get("status").toString();
        String dpRefId = objectJson.getJSONObject("delivery").get("order_id").toString();

        Status previousStatus = deliveryService.getPreviousStatus(dpRefId);
        Delivery delivery = deliveryService.updateOnCallback(dpRefId, status);

        if(!previousStatus.equals(delivery.getStatus())){
            deliveryService.publishStatus(delivery, logprefix);
        }


        LogUtil.info(logprefix, location, "Callback Response body: ", new JSONObject(requestBody).toString());

        LogUtil.info(logprefix, location, "EXITING callback", "");

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

}
