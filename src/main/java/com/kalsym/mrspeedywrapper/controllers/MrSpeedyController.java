package com.kalsym.mrspeedywrapper.controllers;

import com.kalsym.mrspeedywrapper.services.SpeedyService;
import com.kalsym.parentwrapper.controllers.ParentWrapperController;
import com.kalsym.parentwrapper.models.Delivery;
import com.kalsym.parentwrapper.models.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/")
public class MrSpeedyController {

    @Autowired
    private SpeedyService deliveryService;

    ParentWrapperController parentController = new ParentWrapperController();

    @PostMapping(path = {"/quotation/generate"} , name = "get-quotation")
    public ResponseEntity<HttpResponse> getPrice(HttpServletRequest request,
                                                 @Valid @RequestBody Delivery delivery){
        HttpResponse response = parentController.getPrice(request, delivery, deliveryService);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {"/quotation/{quotationId}/order"}, name = "place-delivery-order")
    public ResponseEntity<HttpResponse> placeDeliveryOrder (HttpServletRequest request,
                                                            @PathVariable String quotationId) {
        HttpResponse response = parentController.placeDeliveryOrder(request, quotationId, deliveryService);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

}
