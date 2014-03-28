package com.muje.parcel;

/**
 * Created by yeang-shing.then on 3/17/14.
 */
public enum Status {
    /**
     * Submit parcel at courier counter. Exist one result found, represent as red color.
     */
    SENT,
    /**
     * Parcel start on delivering. Represent color is orange.
     */
    WIP,
    /**
     * Finally parcel successfully delivered and received by recipient. Represent as Green color.
     */
    DELIVERED,
    /**
     * Invalid consignment number or not exist in courier database.
     */
    INVALID,
}

