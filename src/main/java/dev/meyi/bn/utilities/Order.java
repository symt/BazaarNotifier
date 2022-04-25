package dev.meyi.bn.utilities;


public class Order {
    public String product;
    public int startAmount;
    private int amountRemaining;
    public double pricePerUnit;
    public String priceString;
    public boolean outdatedOrder = false;
    public boolean matchedOrder = false;
    public boolean goodOrder = true;
    public double orderValue;
    public String type;
    public String currentNotification;

    public Order(String product,int startAmount,double pricePerUnit,String priceString,String type){
        this.product = product;
        this.startAmount = startAmount;
        amountRemaining = startAmount;
        this.pricePerUnit = pricePerUnit;
        this.priceString = priceString;
        this.type = type;
        orderValue = startAmount * pricePerUnit;
    }

    public void setAmountRemaining(int amountRemaining){
        this.amountRemaining = amountRemaining;
        orderValue = amountRemaining * pricePerUnit;
    }
    public int getAmountRemaining(){
        return  amountRemaining;
    }
}