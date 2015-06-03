/*
 * DDSProcessToUFD.java
 *
 * Created on 19 December 2005, 11:45
 *
 */

package com.dataagility.ICAN.BHLibDDS;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * DDSProcessToUFD provides methods to process a message from DDS to UFD
 * structure.  These methods are DDS specific.
 * @author Ray Fillingham and Norman Soh
 */
public class DDSProcessToUFD extends ProcessSegmentsToUFD {
    /**
     * Class wide HL7Message object
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";
    public String mSReg4 = "";
    /**
     * Creates a new instance of DDSProcessToUFD
     * @param pHL7Message HL7 Message string
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public DDSProcessToUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";    // DDSProcessToUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------
    /**
     * processMessage will convert / process the entire DDS message into a
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
            aOutHL7Message.append(processPD1NTEs_ToUFD());
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
        aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "DDS");
        String aMessageType = aOutMSHSegment.get(HL7_23.MSH_9_2_trigger_event);
        String aOriginatingSys = aOutMSHSegment.get(HL7_23.MSH_3_sending_application);
        if (aOriginatingSys.indexOf("CERNER") >= 0) {
            aOriginatingSys = "C";
        } else if (aOriginatingSys.indexOf("FIRSTNET") >= 0) {
            aOriginatingSys = "C";
        } else if(aOriginatingSys.indexOf("CARENET") >= 0) {
            aOriginatingSys = "C";
        } else if (aOriginatingSys.indexOf("CSC") >= 0) {
            aOriginatingSys = "H";
        } else {
            aOriginatingSys = aOriginatingSys.substring(0, 1);
        }
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aOriginatingSys.concat(aMessageType).concat(aOutMSHSegment.getVerDateTime(mVersion)));
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        mFacility = aOutMSHSegment.getField(HL7_23.MSH_4_sending_facility);
        return aOutMSHSegment;
    }
    public HL7Group processPD1NTEs_ToUFD() {
        HL7Group aOutPD1NTEGroup = new HL7Group("");
        int aGroupCount;
        int aCount = 1;
        String aGroupID[] = HL7_23.Group_PD1Notes;
        aGroupCount = mInHL7Message.countGroups(aGroupID);

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            aOutPD1NTEGroup.append(mInHL7Message.getGroup(aGroupID, aCount));
        }

        return aOutPD1NTEGroup;
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
        int aCount = 1;
        String aGroupID[] = HL7_23.Group_Orders;
        aGroupCount = mInHL7Message.countGroups(aGroupID);

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            aReqDetsGroup = new HL7Group(mInHL7Message.getGroup(aGroupID, aCount));
            for (int aSegCount = 1; aSegCount <= aReqDetsGroup.countSegments(); aSegCount++) {
                HL7Segment aInSegment = new HL7Segment(aReqDetsGroup.getSegment(aSegCount));
                if (aInSegment.getSegmentID().equalsIgnoreCase("ORC") && mEnvironment.indexOf("TEST") >= 0) {
                    String aPlacerOrderNum = aInSegment.get(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
                    if (aPlacerOrderNum.length() > 0) {
                        mSReg4 = aPlacerOrderNum.substring(0, 2) + "0" + aPlacerOrderNum.substring(3, 10);
                    }
                } else if (aInSegment.getSegmentID().equalsIgnoreCase("OBR") && mEnvironment.indexOf("TEST") >= 0) {
                    aInSegment.set(HL7_23.OBR_21_Fillers_Field_2, "00" + mSReg4);
                    aInSegment.set(HL7_23.OBR_32_Principal_Result_Interpreter, "R375RAD2");
                    aInSegment.set(HL7_23.OBR_35_transcriptionist, "R375KMH");
                } else if (aInSegment.getSegmentID().equalsIgnoreCase("OBR")) {
                    String aInterpNameField = aInSegment.get(HL7_23.OBR_35_transcriptionist, "CN_1", 1);
                    aInSegment.set(HL7_23.OBR_35_transcriptionist, aInterpNameField, 1);
                }
                aReqDetsGroupTemp.append(aInSegment);
            }
            aOutOrderObservationsGroup.append(aReqDetsGroupTemp);
            aReqDetsGroupTemp = new HL7Group("");
        }
        return aOutOrderObservationsGroup;
    }
}
