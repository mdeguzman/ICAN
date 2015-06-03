/*
 * DAWNProcessToCERNER.java
 *
 * Created on 19 December 2005, 11:45
 *
 */

package com.dataagility.ICAN.BHLibDAWN;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * DAWNProcessToCERNER provides methods to process a message from DAWN to CERNER
 * structure.
 * @author Norman Soh
 */
public class DAWNProcessToCERNER extends ProcessSegmentsToUFD {
    /**
     * Class wide HL7Message object
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";

    /**
     * Creates a new instance of DAWNProcessToCERNER
     * @param pHL7Message HL7 Message string
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public DAWNProcessToCERNER(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";    // DAWNProcessToCERNER Release Version Number
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
            aOutHL7Message.append(processMSHToCERNER());
            aOutHL7Message.append(processPIDToCERNER());
            aOutHL7Message.append(processPV1ToCERNER());
            aOutHL7Message.append(processOrderObservations_ToCERNER());
        }
        return aOutHL7Message.getMessage();
    }
    //--------------------------------------------------------------------------
    public HL7Segment processMSHToCERNER() throws ICANException {
        HL7Segment aOutMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PID));
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
        String aPID3IDValue = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
        if (aPID3IDValue.startsWith("ALF")) {
            mFacility = "ALF";
        } else if (aPID3IDValue.startsWith("CGMC")) {
            mFacility = "CGMC";
        } else if (aPID3IDValue.startsWith("SDMH")) {
            mFacility = "SDMH";
        } else if (aPID3IDValue.startsWith("EXTN")) {
            mFacility = "EXTN";
        }
        aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-".concat(mFacility));
        aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, mFacility);
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "CERNERPM");
        aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, mFacility);
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aOriginatingSys.concat(aMessageType).concat(aOutMSHSegment.getVerDateTime(mVersion)));
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        mFacility = aOutMSHSegment.getField(HL7_23.MSH_4_sending_facility);
        return aOutMSHSegment;
    }

    public HL7Segment processPIDToCERNER() throws ICANException {
        HL7Segment aOutPIDSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PID));
        //Get patient UR
        String aURNumber = "";
        int aPatientIDCount = aOutPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPatientIDCount; i++) {
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).matches("UNITNO")) {
                //GET UR number
                aURNumber = aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                if (mFacility.matches("ALF")) {
                    aURNumber = aURNumber.substring(3, aURNumber.length());
                } else if (mFacility.matches("CGMC") || mFacility.matches("SDMH") || mFacility.matches("EXTN")) {
                    aURNumber = aURNumber.substring(4, aURNumber.length());
                }
            }
        }
        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, "");
        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aURNumber);
        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, mFacility);
        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "PI");
        return aOutPIDSegment;
    }

    public HL7Segment processPV1ToCERNER() throws ICANException {
        HL7Segment aOutPV1Segment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PV1));
        aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, "");
        aOutPV1Segment.set(HL7_23.PV1_9_consulting_doctor, "");
        String aPatientClass = aOutPV1Segment.get(HL7_23.PV1_2_patient_class);
        aOutPV1Segment.set(HL7_23.PV1_18_patient_type, aPatientClass);
        String aVisitNumber = aOutPV1Segment.get(HL7_23.PV1_19_visit_number);
        if (aVisitNumber.startsWith("I")) {
            aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, mFacility.concat("-CSC"));
        } else {
            aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, mFacility.concat("-CERNER"));
        }
        return aOutPV1Segment;
    }

    /**
     * Translates order observation group to UFD structure.
     * @return Order observation group
     */
    public HL7Group processOrderObservations_ToCERNER() {
        HL7Group aOutOrderObservationsGroup = new HL7Group("");
        HL7Group aReqDetsGroup = new HL7Group("");
        HL7Group aReqDetsGroupTemp = new HL7Group("");
        String aFillerOrderID = "";
        int aOBXCount = 1;

        int aGroupCount = mInHL7Message.countGroups(HL7_23.Group_Orders);

        for (int aCount = 1; aCount <= aGroupCount; aCount++) {

            aOBXCount = 1;

            aReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, aCount));

            for (int aSegCount = 1; aSegCount <= aReqDetsGroup.countSegments(); aSegCount++) {

                HL7Segment aInSegment = new HL7Segment(aReqDetsGroup.getSegment(aSegCount));
                if (aInSegment.getSegmentID().equalsIgnoreCase("ORC")) {
                    aFillerOrderID = aInSegment.get(HL7_23.ORC_3_filler_order_num);
                    aInSegment.set(HL7_23.ORC_3_filler_order_num, "");
                    aInSegment.set(HL7_23.ORC_5_order_status, "");
                } else if (aInSegment.getSegmentID().equalsIgnoreCase("OBR")) {
                    aInSegment.set(HL7_23.OBR_3_Filler_Order_Number, aFillerOrderID);
                    aInSegment.set(HL7_23.OBR_21_Fillers_Field_2, "DAWN".concat(aInSegment.get(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID)));
                    aInSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "LA");
                } else if (aInSegment.getSegmentID().equalsIgnoreCase("OBX")) {

                    if (aInSegment.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code).matches("Instruction")) {
                        aInSegment.set(HL7_23.OBX_1_set_ID, String.valueOf(aOBXCount));
                        aOBXCount++;
                        aInSegment.set(HL7_23.OBX_2_value_type, "ST");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, "Instruction");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text, "Instruction");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme, "Dosing Instruction");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID, "DawnAC");

                    } else if (aInSegment.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code).matches("NextTest")) {
                        aInSegment.set(HL7_23.OBX_1_set_ID, String.valueOf(aOBXCount));
                        aOBXCount++;
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, "NextTest");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text, "NextTest");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme, "Next Test Date");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID, "DawnAC");
                    } else if (aInSegment.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code).matches("Comment")) {
                        aInSegment.set(HL7_23.OBX_1_set_ID, String.valueOf(aOBXCount));
                        aOBXCount++;
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, "Comment");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text, "Comment");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme, "Test Comment");
                        aInSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID, "DawnAC");
                    } else {
                        aInSegment.setSegment("");
                    }
                }
                aReqDetsGroupTemp.append(aInSegment);
            }
            aOutOrderObservationsGroup.append(aReqDetsGroupTemp);
            aReqDetsGroupTemp = new HL7Group("");
        }
        return aOutOrderObservationsGroup;
    }
}
