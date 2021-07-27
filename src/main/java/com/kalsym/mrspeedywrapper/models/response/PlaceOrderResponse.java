package com.kalsym.mrspeedywrapper.models.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaceOrderResponse extends Response{

    private String TrackNo;
    private String BarCode;
    private String CashCollection;
    private String OrderId;

}
