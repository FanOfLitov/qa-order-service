package com.example.qaorderservice.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateOrderRequest{
    @NotBlank
    private String customerName;

    @NotBlank
    private String product;

    @NotNull
    @Positive
    private Integer quantity;

    public CreateOrderRequest(){}

    public String getCustomerName(){
        return customerName;

    }

    public String getProduct(){
        return product;
    }
    public void setProduct(String product){
        this.product = product;
    }

    public Integer getQuantity(){
        return quantity;
    }
    public void setQuantity(Integer quantity){
        this.quantity = quantity;
    }
}