/*
 * RAPIDCOMMProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 */

package com.dataagility.ICAN.BHLibRAPIDCOMM;
import com.dataagility.ICAN.BHLibClasses.*;

/**
 *
 * @author norman soh
 */
public class RAPIDCOMMProcessFromUFD extends ProcessSegmentsFromUFD {

    public String mEnvironment = "";
    /**
     * Creates a new instance of RAPIDCOMMProcessFromUFD
     */
    public RAPIDCOMMProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {
        String aHL7MessageArray[] = {k.NULL, k.NULL, k.NULL};
        String aSegment;
        HL7Group aGroup;
        HL7Message aInMess = new HL7Message(mHL7Message);
        HL7Message aOutMess = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = new HL7Segment(aInMess.getSegment(HL7_23.MSH));

        if (aInMess.isEvent("A01, A02, A03, A04, A08, A11, A12, A13, A17, A21, A22, A28, A31, A34")) {
            aOutMess = aInMess;
            aOutMess.setSegment(processMSHFromUFD("RAPIDCOMM").getSegment());
            aHL7MessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aHL7MessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aHL7MessageArray[2] = aOutMess.getMessage();

        }
        return aHL7MessageArray;
    }
}
