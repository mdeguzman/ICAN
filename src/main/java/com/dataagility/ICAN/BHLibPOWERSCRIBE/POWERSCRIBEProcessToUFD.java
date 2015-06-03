/*
 * POWERSCRIBEProcessToUFD.java
 *
 * Created on 12 December 2007, 11:45
 *
 */

package com.dataagility.ICAN.BHLibPOWERSCRIBE;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * POWERSCRIBEProcessToUFD provides methods to process a message from DDS to UFD
 * structure.  These methods are DDS specific.
 * @author Ray Fillingham and Norman Soh
 */
public class POWERSCRIBEProcessToUFD extends ProcessSegmentsToUFD {
    /**
     * Class wide HL7Message object
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";
    public String mSReg4 = "";
    /**
     * Creates a new instance of POWERSCRIBEProcessToUFD
     * @param pHL7Message HL7 Message string
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public POWERSCRIBEProcessToUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";    // POWERSCRIBEProcessToUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------
    /**
     * processMessage will convert / process the entire POWERSCRIBE message into a
     * UFD HL7 message structure
     * @return Returns a UFD HL7 message
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public String processMessage() throws ICANException {

        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));

        if (mInHL7Message.isEvent("R01")) {
            aOutHL7Message.append(processMSHToUFD());
            aOutHL7Message.append(processPIDToUFD());
            aOutHL7Message.append(processPV1ToUFD());
            aOutHL7Message.append(processOrderObservations_ToUFD());
        }
        if (aOutHL7Message.getMessage().length() > 0) {
            aOutHL7Message.append(setupZBX("MESSAGE", "SOURCE_ID", aInMSHSegment.get(HL7_23.MSH_10_message_control_ID)));
        }
        return aOutHL7Message.getMessage();
    }
    //--------------------------------------------------------------------------
    public HL7Segment processMSHToUFD() throws ICANException {
        HL7Segment aOutMSHSegment = new HL7Segment(k.NULL);
        aOutMSHSegment.setSegment(mInHL7Message.getSegment(HL7_23.MSH));
        aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "POWERSCRIBE");
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aOutMSHSegment.getVerDateTime(mVersion));
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        return aOutMSHSegment;
    }

    /**
     * Translates order observation group to UFD structure.
     * @return Order observation group
     */
    public HL7Group processOrderObservations_ToUFD() {
        HL7Group aOutOrderObservationsGroup = new HL7Group("");
        HL7Group aReqDetsGroup = new HL7Group("");
        HL7Group aReqDetsGroupTemp = new HL7Group("");
        int aGroupCount;
        aGroupCount = mInHL7Message.countGroups(HL7_23.Group_Orders);

        for (int aCount = 1; aCount <= aGroupCount; aCount++) {
            aReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, aCount));
            for (int aSegCount = 1; aSegCount <= aReqDetsGroup.countSegments(); aSegCount++) {
                HL7Segment aInSegment = new HL7Segment(aReqDetsGroup.getSegment(aSegCount));
                aReqDetsGroupTemp.append(aInSegment);
            }
            aOutOrderObservationsGroup.append(aReqDetsGroupTemp);
            aReqDetsGroupTemp = new HL7Group("");
        }
        return aOutOrderObservationsGroup;
    }
}
