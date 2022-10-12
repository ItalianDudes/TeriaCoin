package com.italiandudes.teriacoin.common;

import java.io.Serializable;

public final class Balance implements Serializable {

    //Attributes
    private double balance;

    //Constructors
    public Balance(){
        balance = 0;
    }
    public Balance(double balance){
        this.balance = balance;
    }

    //Methods
    public double getBalance(){
        return balance;
    }
    public void setBalance(double balance){
        if(balance>=0){
            this.balance = balance;
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Balance)) return false;

        Balance balance1 = (Balance) o;

        return Double.compare(balance1.getBalance(), getBalance()) == 0;
    }
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(getBalance());
        return (int) (temp ^ (temp >>> 32));
    }
    @Override
    public String toString(){
        return balance+"TC";
    }

}
