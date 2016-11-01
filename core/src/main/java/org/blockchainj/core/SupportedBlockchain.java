package org.blockchainj.core;

import java.security.InvalidParameterException;

/**
 * Created by rodrigo on 10/31/16.
 * Enum that lists the supported blockchains on the API.
 * Currently, only Bitcoin and Internet of People
 */
public enum SupportedBlockchain {
    BITCOIN ("BTC"),
    INTERNET_OF_PEOPLE ("IoP");

    // local variables
    private final String name;

    // constructor
    private SupportedBlockchain(String name){
        this.name = name;
    }

    // gets blockchain by name.
    public SupportedBlockchain getByName(String name){
        if (name.compareTo("BTC") == 0)
                return BITCOIN;
        else if (name.compareTo("IoP") == 0)
                return INTERNET_OF_PEOPLE;
        else
                throw new InvalidParameterException("The specified blockchain " + name + " does not exists.");
    }

    // get the name of the blockchain
    public String getName(){
        return this.name;
    }

}
