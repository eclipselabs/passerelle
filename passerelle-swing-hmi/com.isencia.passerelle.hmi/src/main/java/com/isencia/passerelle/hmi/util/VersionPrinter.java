/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.util;


/**
 * <p>
 * Prints the version of the project on stdout.
 * </p>
 * 
 * @author erwin.de.ley@isencia.be
 */
public class VersionPrinter {
    public static final String VERSION_MAJOR = "8";
    public static final String VERSION_MINOR = "5";
    public static final String VERSION_ITERATION = "0";
    public static final String PROJECT_NAME = "Passerelle HMI";
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public static void main(String[] args) {
        System.out.println(getProjectVersionInfo());
    }
    
    public static String getProjectVersionInfo() {
    	return (PROJECT_NAME+" version: " + VERSION_MAJOR
                + "." + VERSION_MINOR + "."
                + VERSION_ITERATION);
    }
}
