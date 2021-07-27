package com.kalsym.mrspeedywrapper.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Package {

    private String ware_code;
    private String description;
    private Float items_count;
    private String item_payment_amount;

}
