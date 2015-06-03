/*
 * MEDIBILLProcessFromUFD.java
 *
 * Created on 11 October 2005, 17:04
 *
 */
package BHLibMEDIBILL;

import BHLibClasses.*;

/**
 * MEDIBILLProcessFromUFD
 *
 * @author Ray Fillingham and Norman Soh
 */
public class MEDIBILLProcessFromUFD extends ProcessSegmentsFromUFD {
    /**
     * Constant class
     */
    public BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public String aAssignBillNo = "";
    boolean aProcess = true;
    /**
     * Class wide HL7Message object
     */
    public HL7Message aInHL7Message;
    //--------------------------------------------------------------------------
    /**
     * This constructor creates a new instance of MEDIBILLProcessFromUFD passing a HL7 UFD
     * message structure
     *
     * @param pHL7Message HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public MEDIBILLProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";    // MEDIBILLProcessFromUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------
    /**
     * This method contains the methods required to build a HL7 message
     * @return HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public String[] processMessage() throws ICANException {

        String aMEDIBILLMessageArray[] = {k.NULL, k.NULL, k.NULL};
        HL7Message aOutHL7Message = new HL7Message();
        aInHL7Message = new HL7Message(mHL7Message);

        if (aInHL7Message.isEvent("S12, S13, S14, S15, S26")) {

            aOutHL7Message.setMessage(mHL7Message);
            HL7Segment aMSHSegment = new HL7Segment(aOutHL7Message.getSegment("MSH"));

            aMEDIBILLMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aMEDIBILLMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aMEDIBILLMessageArray[2] = aOutHL7Message.getMessage();
        }
        return aMEDIBILLMessageArray;
    }

    public HL7Segment checkMSHTime(HL7Segment pSegment) {
        String aDateTime = pSegment.get(HL7_23.MSH_7_message_date_time);
        if (aDateTime.endsWith("0000")) {
            aDateTime = aDateTime.substring(0, 8) + "0001";
            pSegment.set(HL7_23.MSH_7_message_date_time, aDateTime);
        }
        return pSegment;
    }

    public HL7Segment checkEVNTime(HL7Segment pSegment) {
        String aDateTime = pSegment.get(HL7_23.EVN_2_date_time_of_event);
        if (aDateTime.endsWith("000000")) {
            aDateTime = aDateTime.substring(0, 8) + "000100";
            pSegment.set(HL7_23.EVN_2_date_time_of_event, aDateTime);
        }
        return pSegment;
    }
}
