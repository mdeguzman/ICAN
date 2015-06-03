/*
 * PARISProcessToUFD.java
 *
 * Created on 5 October 2005, 14:34
 *
 */

package BHLibPARIS_DateFix;

import BHLibClasses.*;


/**
 * PARISProcessToUFD provides methods to process a message from PARIS to UFD
 * structure.  These methods are PARIS specific.
 * @author Ray Fillingham and Norman Soh
 */
public class PARISProcessToUFD extends ProcessSegmentsToUFD {
    /**
     * Class wide HL7Message object
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";
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
     * Creates a new instance of PARISProcessToUFD
     * @param pHL7Message HL7 Message string
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public PARISProcessToUFD(String pHL7Message) throws ICANException {
        super(pHL7Message);
        mVersion = "a";    // PARISProcessToUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = System.getProperty( "com.sun.aas.domainName" );
    }
    //--------------------------------------------------------------------------
    /**
     * processMessage will convert / process the entire PARIS message into a
     * UFD HL7 message structure
     * @return Returns a UFD HL7 message
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public String processMessage() throws ICANException {
        System.out.println("processing message");
        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        HL7Segment aInORCSegment = new HL7Segment(mInHL7Message.getGroup(HL7_23.Group_Orders, 1));
        if (mInHL7Message.isEvent("R01, R03") && (aInORCSegment.get(HL7_23.ORC_5_order_status).equalsIgnoreCase("CM"))) {
            System.out.println("Its a result");
            System.out.println("processing MSH");
            HL7Segment aMSHSegment = processPARISMSHToUFD();
            System.out.println("processing PID");
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Group aPIDNTEGroup = processPARISPIDNTEToUFD();
            System.out.println("processing PV1");
            HL7Segment aPV1Segment = processPV1ToUFD();
            System.out.println("processing Group");
            HL7Group aOrderObservationsGroup = processOrderObservations_ToUFD();

            aPIDSegment = processPARISPIDToUFD(aPIDSegment);
            aPV1Segment = processPARISPV1ToUFD(aPV1Segment);

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
    public HL7Segment processPARISMSHToUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        String aPID3AssAuth = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority);
        aOutMSHSegment.linkTo(aInMSHSegment);
        boolean aClientNumNumeric = true;
        try {
            mClientNumber = Integer.parseInt(aPID3AssAuth);
        } catch (NumberFormatException ex) {
            aClientNumNumeric = false;
        }
        if (aClientNumNumeric == true) {
            if (mClientNumber == 9 || (mClientNumber >= 60000 && mClientNumber < 65000)) {
                mHospitalPrefix = "C";
                mSReg4 = "-CGMC";
            } else if (mClientNumber == 12) {
                mHospitalPrefix = "S";
                mSReg4 = "-SDMH";
            } else {
                mHospitalPrefix = "A";
                mSReg4 = "";
            }
        }

        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        if (mHospitalPrefix.equalsIgnoreCase("A")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-ALF");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "ALF");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "ALF");
        } else if(mHospitalPrefix.equalsIgnoreCase("S")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-SDMH");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "SDMH");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "SDMH");
        } else if(mHospitalPrefix.equalsIgnoreCase("C")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-CGMC");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "CGMC");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "CGMC");
        }


       //String aAssAuthNameSpcIDRegex = "11111|70080|70146|70149|70179|70211|70278|70292|73399|74299|88888|40000|40001|40230|40070|70773|12345";
       CodeLookUp aLU = new CodeLookUp("CERNER_AUTHList.table", mEnvironment);
       System.out.println("++++ " + aPID3AssAuth);
       //if (aPID3AssAuth.matches(aAssAuthNameSpcIDRegex)) {
       if (aLU.getValue(aPID3AssAuth).length() == 0){
           System.out.println("++++ Unauthorised");
           aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-EXTN");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "EXTN");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "EXTN");
            mSReg4 = "-EXTN";
            mHospitalPrefix = "E";
        }

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
    public HL7Segment processPARISPIDToUFD(HL7Segment pPIDSegment) {
        HL7Segment aOutPIDSegment = new HL7Segment("");
        aOutPIDSegment = pPIDSegment;
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");
        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal,HL7_23.CX_ID_number,
                (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal,HL7_23.CX_ID_number).substring(8)));
        if (mHospitalPrefix.equalsIgnoreCase("A")) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "ALF", 1);
            aOutPIDSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "ALF");
        } else if (mHospitalPrefix.equalsIgnoreCase("S")) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "SDMH", 1);
            aOutPIDSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "SDMH");
        } else if (mHospitalPrefix.equalsIgnoreCase("C")){
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "CGMC", 1);
            aOutPIDSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "CGMC");
        } else if (mHospitalPrefix.equalsIgnoreCase("E")){
            String aPID3ID = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
            aPID3ID = aPID3ID + "-EXTN";
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPID3ID, 1);
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "EXTN", 1);
            aOutPIDSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "EXTN");
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
    public HL7Segment processPARISPV1ToUFD(HL7Segment pPV1Segment) {
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        HL7Segment aOutPV1Segment = pPV1Segment;
        String aPatientClass = pPV1Segment.get(HL7_23.PV1_2_patient_class);

        //Swap encounter W to PO
        if (aPatientClass.equalsIgnoreCase("W")) {
            aOutPV1Segment.set(HL7_23.PV1_2_patient_class, "PO");
            aOutPV1Segment.set(HL7_23.PV1_18_patient_type, "PO");
            aPatientClass = "PO";
        }

        if (mHospitalPrefix.equalsIgnoreCase("C")) {
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "CGMC");
            if (aPatientClass.equalsIgnoreCase("I")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-CSC");
            } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-CERNER");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-TRIAL");
            }
        } else if (mHospitalPrefix.equalsIgnoreCase("S")) {
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "SDMH");
            if (aPatientClass.equalsIgnoreCase("I")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CSC");
            } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CERNER");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-TRIAL");
            }
        } else if (mHospitalPrefix.equalsIgnoreCase("A")) {
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "ALF");
            if (aPatientClass.equalsIgnoreCase("I")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CSC");
            } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E") || aPatientClass.equalsIgnoreCase("PO")) {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CERNER");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-TRIAL");
            }
        } else if (mHospitalPrefix.equalsIgnoreCase("E")) {
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "EXTN");
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
            String aAdmitDateTime = pPV1Segment.get(HL7_23.PV1_44_admit_date_time).concat("000000").substring(0, 12);
            String aVisitNumTemp = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number).substring(8, 15).concat("-").concat(aAdmitDateTime);
            aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNumTemp);
        }

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
        System.out.println("in group method");
        HL7Group aOutOrderObservationsGroup = new HL7Group("");
        int aGroupCount;
        int aCount = 1;
        String aGroupID[] = HL7_23.Group_Orders;
        aGroupCount = mInHL7Message.countGroups(aGroupID);

        CreateCurrentTime ct = new CreateCurrentTime();
        String currentDate = ct.getCurrentDate();

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            HL7Group group = new HL7Group(mInHL7Message.getGroup(aGroupID, aCount));
            //int segCount = group.countSegments();
            for(int segCount = 1; segCount <= group.countSegments() ; segCount++){

                HL7Segment segment = new HL7Segment(group.getSegment(segCount));
            System.out.println("actual segmentID: " + segment.getSegmentID());
            if (segment.getSegmentID().equalsIgnoreCase("OBX")){
                segment.set(HL7_23.OBX_14_date_time_of_the_observation, currentDate);
                    System.out.println("changing date");
                //set OBX-14 to current datetime
            }
            aOutOrderObservationsGroup.append(segment);
            }
        }

        return aOutOrderObservationsGroup;
    }

}
