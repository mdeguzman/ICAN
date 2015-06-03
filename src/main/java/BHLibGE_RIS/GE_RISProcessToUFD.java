/*
 * GE_RISProcessToUFD.java
 *
 * Created on 10 June 2008, 14:34
 *
 */

package BHLibGE_RIS;

import BHLibClasses.*;

/**
 * GE_RISProcessToUFD provides methods to process a message from GE_RIS to UFD
 * structure.  These methods are PARIS specific.
 * @author Ray Fillingham and Norman Soh
 */
public class GE_RISProcessToUFD extends ProcessSegmentsToUFD {
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
    public GE_RISProcessToUFD(String pHL7Message) throws ICANException {
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

        if (mInHL7Message.isEvent("R01, R03")) {
            HL7Segment aMSHSegment = processGERISMSHToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Group aPIDNTEGroup = processPARISPIDNTEToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Group aOrderObservationsGroup = processOrderObservations_ToUFD();

            aPIDSegment = processGERISPIDToUFD(aPIDSegment);
            aPV1Segment = processGERISPV1ToUFD(aPV1Segment);

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPIDNTEGroup);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aOrderObservationsGroup);
        } else if (mInHL7Message.isEvent("O01")) {
            HL7Segment aMSHSegment = processGERISMSHToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Group aPIDNTEGroup = processPARISPIDNTEToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Group aOrderObservationsGroup = processOrderObservations_ToUFD();

            aPIDSegment = processGERISPIDToUFD(aPIDSegment);
            aPV1Segment = processGERISPV1ToUFD(aPV1Segment);

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPIDNTEGroup);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aOrderObservationsGroup);
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

//        boolean aClientNumNumeric = true;
//        try {
//            mClientNumber = Integer.parseInt(aPID3AssAuth);
//        } catch (NumberFormatException ex) {
//            aClientNumNumeric = false;
//        }
//        if (aClientNumNumeric == true) {
//            if (mClientNumber == 9 || (mClientNumber >= 60000 && mClientNumber < 65000)) {
//                mHospitalPrefix = "C";
//                mSReg4 = "-CGMC";
//            } else if (mClientNumber == 12) {
//                mHospitalPrefix = "S";
//                mSReg4 = "-SDMH";
//            } else {
//                mHospitalPrefix = "A";
//                mSReg4 = "";
//            }
//        }

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

//        String aAssAuthNameSpcIDRegex = "11111|70080|70146|70149|70179|70211|70278|70292|73399|74299|88888|40000|40001|40230|40070";
//        if (aPID3AssAuth.matches(aAssAuthNameSpcIDRegex)) {
//            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-EXTN");
//            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "EXTN");
//            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "EXTN");
//            mSReg4 = "-EXTN";
//            mHospitalPrefix = "E";
//        }

        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "CERNERPM");
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
     * Process PID NTEs to UFD based on PARIS requirements
     * @return PIDNTE group
     */
    public HL7Group processPARISPIDNTEToUFD() {
        HL7Group aOutPIDNTEGroup = new HL7Group("");
        HL7Group aInPIDNTEGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_PD1Notes, 1));
        aOutPIDNTEGroup = aInPIDNTEGroup;
        return aOutPIDNTEGroup;
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

//        if (!aPARISNum.equalsIgnoreCase(aVisitNum)) {
////            String aAdmitDateTime = pPV1Segment.get(HL7_23.PV1_44_admit_date_time).concat("000000").substring(0, 12);
////            String aVisitNumTemp = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number).concat("-").concat(aAdmitDateTime);
////            aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNumTemp);
//            //aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
//        }

        //Check for "T" prefix.  If T, then remove - This change related to TESTING **REMOVE after testing**
//        String aVisitNumT = aOutPV1Segment.get(HL7_23.PV1_19_visit_number);
//        if (aVisitNumT.startsWith("T")) {
//            aVisitNumT = aVisitNumT.substring(1, aVisitNumT.length());
//            aOutPV1Segment.set(HL7_23.PV1_19_visit_number, aVisitNumT);
//        }

        if (!aPARISNum.equalsIgnoreCase(aVisitNum)) {
            aOutPV1Segment.set(HL7_23.PV1_45_discharge_date_time, pPV1Segment.get(HL7_23.PV1_44_admit_date_time));
        }

        return aOutPV1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     * Translates order observation group to UFD structure.
     * @return Order observation group
     */
    public HL7Group processOrderObservations_ToUFD() {
        HL7Group aOutOrderObservationsGroup = new HL7Group("");
        int aGroupCount;
        int aCount = 1;
        String aGroupID[] = HL7_23.Group_Orders;
        aGroupCount = mInHL7Message.countGroups(aGroupID);

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            aOutOrderObservationsGroup.append(mInHL7Message.getGroup(aGroupID, aCount));
        }
        return aOutOrderObservationsGroup;
    }
}
