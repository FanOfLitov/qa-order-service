package com.example.qaorderservice.order;

import jakarta.persistence.*;

@Entity
@Table(name="orders")
public class Order{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private String product;
    private Integer quantity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    public Order(){

    }

    public Order(String customerName, String product, Integer quantity){
        this.customerName = customerName;
        this.product = product;
        this.quantity = quantity;
        this.status = OrderStatus.NEW;
    }

    public Long getId(){
        return id;
    }

    public String getCustomerName(){
        return customerName;
    }

    public void setCustomerName(String customerName){
        this.customerName = customerName;
    }
    public String getProduct(){
        return product;
    }

    public void setProduct(String product){
        this.product = product;
    }

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity){
        this.quantity=quantity;
    }

    public OrderStatus getStatus(){
        return status;
    }
    public void setStatus(OrderStatus status){
        this.status = status;
    }


}