package com.kalsym.mrspeedywrapper.models.response;

import com.kalsym.mrspeedywrapper.models.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Response {
    private Boolean is_successful;
    private Order order;
}
