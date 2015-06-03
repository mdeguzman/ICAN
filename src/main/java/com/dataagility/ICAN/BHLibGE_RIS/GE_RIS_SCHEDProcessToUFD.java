/*
 * GE_RIS_SCHEDProcessToUFD.java
 *
 * Created on 10 June 2008, 14:34
 *
 */

package com.dataagility.ICAN.BHLibGE_RIS;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * GE_RIS_SCHEDProcessToUFD provides methods to process a message from GE_RIS Scheduling to UFD
 * structure.  These methods are PARIS specific.
 * @author Ray Fillingham and Norman Soh
 */
public class GE_RIS_SCHEDProcessToUFD extends ProcessSegmentsToUFD {
    /**
     * Class wide HL7Message object
     */
    public HL7Message mInHL7Message;
    /**
     * Class wide variable to hold the Hospital prefix
     */
    public String mHospitalPrefix = "";
    /**
     * Class wide variable to hold the Client number
     */
    public int mClientNumber = 0;
    /**
     * Class wide variable to hold a SREG4 variable
     */
    public String mSReg4 = "";
    /**
     * Creates a new instance of GE_RISProcessToUFD
     * @param pHL7Message HL7 Message string
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public GE_RIS_SCHEDProcessToUFD(String pHL7Message) throws ICANException {
        super(pHL7Message);
        mVersion = "a";    // GE_RISProcessToUFD Release Version Number
        mHL7Message = pHL7Message;
    }
    //--------------------------------------------------------------------------
    /**
     * processMessage will convert / process the entire GE_RIS message into a
     * UFD HL7 message structure
     * @return Returns a UFD HL7 message
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public String processMessage() throws ICANException {

        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));

        if (mInHL7Message.isEvent("S12, S13, S14, S15, S26")) {
            HL7Segment aMSHSegment = processGERISMSHToUFD();
            HL7Segment aSCHSegment = processGERISSCHToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Segment aAILSegment = processGERISAILToUFD();

            aPIDSegment = processGERISPIDToUFD(aPIDSegment);
            aPV1Segment = processGERISPV1ToUFD(aPV1Segment);

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aSCHSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aAILSegment);
        }
        if (aOutHL7Message.getMessage().length() > 0) {
            aOutHL7Message.append(setupZBX("MESSAGE", "SOURCE_ID", aInMSHSegment.get(HL7_23.MSH_10_message_control_ID)));
        }
        return aOutHL7Message.getMessage();
    }
    //--------------------------------------------------------------------------
    /**
     * Process MSH segment to UFD based on PARIS requirements
     * @return MSH segment
     */
    public HL7Segment processGERISMSHToUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        String aPID3AssAuth = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority);
        aOutMSHSegment.linkTo(aInMSHSegment);

        //check sending facility from GE_RIS
        String aSendingFac = aInMSHSegment.get(HL7_23.MSH_4_sending_facility);
        if (aSendingFac.matches("ALF")) {
            //Alfred
            mHospitalPrefix = "A";
            mSReg4 = "";
        } else if (aSendingFac.matches("SDMH")) {
            //Sandringham
            mHospitalPrefix = "S";
            mSReg4 = "";
        } else {
            //Other Campus
            mHospitalPrefix = "O";
            mSReg4 = "";
        }

        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        if (mHospitalPrefix.equalsIgnoreCase("A")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "GERIS-ALF");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "ALF");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "ALF");
        } else if(mHospitalPrefix.equalsIgnoreCase("S")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "GERIS-SDMH");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "SDMH");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "SDMH");
        } else if(mHospitalPrefix.equalsIgnoreCase("O")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "GERIS-OTH");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "OTH");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "OTH");
        }

        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "ICAN");
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        aOutMSHSegment.copy(HL7_23.MSH_9_message_type);
        String aMessageType = aOutMSHSegment.get(HL7_23.MSH_9_2_trigger_event);
        String aOriginatingSys = aOutMSHSegment.get(HL7_23.MSH_3_sending_application);
        if (aOriginatingSys.indexOf("CERNER") >= 0) {
            aOriginatingSys = "C";
        } else if (aOriginatingSys.indexOf("CSC") >= 0) {
            aOriginatingSys = "H";
        } else {
            aOriginatingSys = aOriginatingSys.substring(0, 1);
        }
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aOriginatingSys.concat(aMessageType).concat(aOutMSHSegment.getVerDateTime(mVersion)));
        //aOutMSHSegment.copy(HL7_23.MSH_10_message_control_ID);
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");

        return aOutMSHSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process PID segment to UFD based on PARIS requirements
     * @param pPIDSegment PID segment
     * @return PID segment
     */
    public HL7Segment processGERISPIDToUFD(HL7Segment pPIDSegment) {
        HL7Segment aOutPIDSegment = new HL7Segment("");
        aOutPIDSegment = pPIDSegment;
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");
        String aURNoTemp = aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number);
        if (aURNoTemp.endsWith("-SDMH")) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal,HL7_23.CX_ID_number, aURNoTemp.substring(0, aURNoTemp.length()-5));
        } else {
            aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal,HL7_23.CX_ID_number);
        }
        if (mHospitalPrefix.equalsIgnoreCase("A")) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "ALF", 1);
            aOutPIDSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "ALF");
        } else if (mHospitalPrefix.equalsIgnoreCase("S")) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "SDMH", 1);
            aOutPIDSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "SDMH");
        } else if (mHospitalPrefix.equalsIgnoreCase("O")) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "OTH", 1);
            aOutPIDSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "OTH");
        }

        return aOutPIDSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process PV1 segment to UFD based on PARIS requirements
     * @param pPV1Segment PV1 segment
     * @return PV1 segment
     */
    public HL7Segment processGERISPV1ToUFD(HL7Segment pPV1Segment) {
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        HL7Segment aOutPV1Segment = pPV1Segment;
        String aPatientClass = pPV1Segment.get(HL7_23.PV1_2_patient_class);

        //Remove S prefix from visit number
        String aSDMHVisitNum = aOutPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
        if (aSDMHVisitNum.startsWith("S")) {
            aSDMHVisitNum = aSDMHVisitNum.substring(1, aSDMHVisitNum.length());
            aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aSDMHVisitNum);
        }

        //Swap encounter W to PO
        if (aPatientClass.equalsIgnoreCase("W")) {
            aOutPV1Segment.set(HL7_23.PV1_2_patient_class, "PO");
            aOutPV1Segment.set(HL7_23.PV1_18_patient_type, "PO");
            aPatientClass = "PO";
        }

        if (mHospitalPrefix.equalsIgnoreCase("O")) {
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "OTH");
            if (aPatientClass.equalsIgnoreCase("I")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "OTH-CSC");
            } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "OTH-CERNER");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "OTH-TRIAL");
            }
        }  else if (mHospitalPrefix.equalsIgnoreCase("A")) {
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "ALF");
            if (aPatientClass.equalsIgnoreCase("I")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CSC");
            } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E") || aPatientClass.equalsIgnoreCase("PO")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CERNER");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-TRIAL");
            }
        }  else if (mHospitalPrefix.equalsIgnoreCase("S")) {
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "SDMH");
            if (aPatientClass.equalsIgnoreCase("I")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CSC");
            } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E") || aPatientClass.equalsIgnoreCase("PO")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CERNER");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-TRIAL");
            }
        }

        //Add ward location in PV1_3 field 1
        if (aOutPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu).length() == 0) {
            String aWard = aOutPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room);
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aWard);
        }

        String aPARISNum = "0";
        String aPID18AccNum = aInPIDSegment.get(HL7_23.PID_18_account_number, HL7_23.CX_ID_number);
        int aCharCount = 0;
        if (aPID18AccNum.length() > 0) {
            while (aPID18AccNum.charAt(aCharCount++) == '0');
            aPARISNum = aPID18AccNum.substring(aCharCount - 1);
        }
        String aVisitNum = pPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
        if (aPARISNum.equalsIgnoreCase("0") || aVisitNum.length() == 0 || aVisitNum.equalsIgnoreCase("0")) {
            aPARISNum = "xxx";
        }
        if (aPatientClass.equalsIgnoreCase("R")) {
            aOutPV1Segment.set(HL7_23.PV1_18_patient_type, "T");
        } else if (aPARISNum.equalsIgnoreCase(aVisitNum)) {
            aOutPV1Segment.set(HL7_23.PV1_18_patient_type, aPatientClass);
        } else {
            aOutPV1Segment.set(HL7_23.PV1_18_patient_type, "P");
        }

        if (!aPARISNum.equalsIgnoreCase(aVisitNum)) {
            aOutPV1Segment.set(HL7_23.PV1_45_discharge_date_time, pPV1Segment.get(HL7_23.PV1_44_admit_date_time));
        }

        return aOutPV1Segment;
    }
    //--------------------------------------------------------------------------

    public HL7Segment processGERISSCHToUFD() {
        HL7Segment aOutSCHSegment = new HL7Segment("");
        HL7Segment aInSCHSegment = new HL7Segment(mInHL7Message.getSegment("SCH"));

        aOutSCHSegment = aInSCHSegment;

        return aOutSCHSegment;
    }

    public HL7Segment processGERISAILToUFD() {
        HL7Segment aOutAILSegment = new HL7Segment("");
        HL7Segment aInAILSegment = new HL7Segment(mInHL7Message.getSegment("AIL"));

        aOutAILSegment = aInAILSegment;

        return aOutAILSegment;
    }

}
