/*
 * PARISProcessToUFD.java
 *
 * Created on 5 October 2005, 14:34
 *
 */
package BHLibCERNER_PATHNET;

import BHLibCERNER.CERNER_23;
import BHLibClasses.*;

/**
 * PARISProcessToUFD provides methods to process a message from PARIS to UFD
 * structure.  These methods are PARIS specific.
 * @author Ray Fillingham and Norman Soh
 */
public class CERNER_PATHNETProcessToUFD_AttCopy extends ProcessSegmentsToUFD {

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
    public CERNER_PATHNETProcessToUFD_AttCopy(String pHL7Message) throws ICANException {
        super(pHL7Message);
        mVersion = "a";    // PARISProcessToUFD Release Version Number

//        if(!pHL7Message.contains("ORC") && pHL7Message.contains("OBR")){
//            pHL7Message = pHL7Message.replaceAll("OBR", "ORC|RE|\rOBR");
//        }
//        System.out.println(pHL7Message);
        mHL7Message = pHL7Message;
        mEnvironment = System.getProperty("com.sun.aas.domainName");
    }
    //--------------------------------------------------------------------------

    /**
     * processMessage will convert / process the entire PARIS message into a
     * UFD HL7 message structure
     * @return Returns a UFD HL7 message
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public String processMessage() throws ICANException {

        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));

        if (mInHL7Message.isEvent("R01, R03")) {
            HL7Segment aMSHSegment = processMSHToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Group aPIDNTEGroup = processPIDNTEZBXs_ToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Group aOrderObservationsGroup = processOrderObservations_ToUFD();

            //aPIDSegment = processPARISPIDToUFD(aPIDSegment);
            //aPV1Segment = processPARISPV1ToUFD(aPV1Segment);

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPIDNTEGroup);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aOrderObservationsGroup);
        } else if (mInHL7Message.isEvent("O01")) {
            HL7Segment aMSHSegment = processMSHToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Group aPIDNTEGroup = processPIDNTEZBXs_ToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Group aOrderObservationsGroup = processOrderObservations_ToUFD();

            ///aPIDSegment = processPIDToUFD();
            //aPV1Segment = processPARISPV1ToUFD(aPV1Segment);

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
     * Process a CERNER PID segment according to UFD requirements
     * @return Returns a PID segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Segment processPIDToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aInPIDSegment = new HL7Segment(k.NULL);
        HL7Segment aInOBXSegment = new HL7Segment(k.NULL);
        HL7Segment aOutPIDSegment = new HL7Segment(HL7_23.PID);

        String aTempField;
        HL7Field aPID_3Field = new HL7Field(k.NULL, k.REPEAT_GET, k.REPEAT_SET);

        HL7Field aPID_2 = new HL7Field(k.NULL);
        HL7Field aPID_3 = new HL7Field(k.NULL);
        HL7Field aPID_4 = new HL7Field(k.NULL);
        HL7Field aPID_19 = new HL7Field(k.NULL);
        HL7Field aPID_PBS = new HL7Field(k.NULL);

        String aRepeatOBXArray[] = {HL7_23.Repeat_OBX};
        int aCountOBXGroup;
        int aCount = 2;

        aInPIDSegment.setSegment(aHL7Message.getSegment(HL7_23.PID));
        if (aInPIDSegment.getSegment().length() == 0) {
            //Throw an exception for missing PID Segment.
            throw new ICANException("F004", mEnvironment);
        }
        aCountOBXGroup = aHL7Message.countGroups(aRepeatOBXArray);
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.copyFields();

        String aArray[] = {HL7_23.PID_2_patient_ID_external,
            HL7_23.PID_3_patient_ID_internal,
            HL7_23.PID_4_alternate_patient_ID,
            HL7_23.PID_19_SSN_number};

        aOutPIDSegment.clearFields(aArray);

        if (!aInPIDSegment.isEmpty(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number)) {
            aTempField = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal);
            String pid4Auth = "";
            HL7Field fPID4 = new HL7Field();
            String[] pid4Fields = aInPIDSegment.getRepeatFields(HL7_23.PID_4_alternate_patient_ID);
            for (int i = 0; i < pid4Fields.length; i++) {
                fPID4.setField(pid4Fields[i]);
                if (fPID4.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("REF_MRN")) {
                    pid4Auth = fPID4.getSubField(HL7_23.CX_assigning_authority);
                    break;
                }

            }


            aPID_3.setField(aTempField);
            aPID_3.setSubField("PI", HL7_23.CX_ID_type_code);

            if (pid4Auth !=  null && pid4Auth.length() > 0) {
                aPID_3.setSubField(pid4Auth, HL7_23.CX_assigning_authority);
            }
        }

        if (!aInPIDSegment.isEmpty(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number)) {
            aTempField = aInPIDSegment.get(HL7_23.PID_4_alternate_patient_ID);
            aPID_4.setField(aTempField);
            aPID_4.setSubField("PEN", HL7_23.CX_ID_type_code);
            aPID_4.setSubField("PEN", HL7_23.CX_assigning_authority);
        }

        if (!aInPIDSegment.isEmpty(HL7_23.PID_19_SSN_number, HL7_23.CX_ID_number)) {
            aTempField = aInPIDSegment.get(HL7_23.PID_19_SSN_number);
            aPID_19.setField(aTempField);
            aPID_19.setSubField("MC", HL7_23.CX_ID_type_code);
            aPID_19.setSubField("HIC", HL7_23.CX_assigning_authority);
            if (aPID_19.getSubField(HL7_23.CX_ID_number).indexOf(" ") == 0) {
                String aCodeID = aPID_19.getSubField(HL7_23.CX_ID_number);
                aCodeID = aCodeID.replaceAll(" ", "");
                aPID_19.setSubField(aCodeID, HL7_23.CX_ID_number);
            }
        }

        if (!aInPIDSegment.isEmpty(HL7_23.PID_2_patient_ID_external, HL7_23.CX_ID_number) &&
                aInPIDSegment.get(HL7_23.PID_2_patient_ID_external, HL7_23.CX_ID_type_code).equalsIgnoreCase("VA")) {
            aTempField = aInPIDSegment.get(HL7_23.PID_2_patient_ID_external);
            aPID_2.setField(aTempField);
            aPID_2.setSubField("VA", HL7_23.CX_ID_type_code);
            aPID_2.setSubField("DVA", HL7_23.CX_assigning_authority);
        }

        for (int i = 1; i <= aCountOBXGroup; i++) {
            HL7Group aOBXGroup = new HL7Group(aHL7Message.getGroup(aRepeatOBXArray, i));
            aInOBXSegment.setSegment(aOBXGroup.getSegment(HL7_23.OBX));
            if (aInOBXSegment.getField(HL7_23.OBX_3_observation_identifier).equalsIgnoreCase("PBSSAFETYNET")) {
                aPID_PBS.setField(aInOBXSegment.getField(HL7_23.OBX_5_observation_value));
                aPID_PBS.setSubField("PBS", HL7_23.CX_assigning_authority);
                aPID_PBS.setSubField("PB", HL7_23.CX_ID_type_code);
            }
        }

        aPID_3Field.setField(aPID_3.getField());
        if (aPID_4.getField().length() > 0) {
            aPID_3Field.setSubField(aPID_4.getField(), aCount++);
        }
        if (aPID_19.getField().length() > 0) {
            aPID_3Field.setSubField(aPID_19.getField(), aCount++);
        }
        if (aPID_2.getField().length() > 0) {
            aPID_3Field.setSubField(aPID_2.getField(), aCount++);
        }
        if (aPID_PBS.getField().length() > 0) {
            aPID_3Field.setSubField(aPID_PBS.getField(), aCount++);
        }

        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, aPID_3Field.getField());

        //Move PID-22 ethnic group to PID-23 birthplace
        aOutPIDSegment.set(HL7_23.PID_23_birth_place, aOutPIDSegment.get(HL7_23.PID_22_ethnic_group));
        aOutPIDSegment.set(HL7_23.PID_22_ethnic_group, k.NULL);


        //blank out PID18
        aOutPIDSegment.set(HL7_23.PID_18_account_number, "");

        return aOutPIDSegment;
    }
    //--------------------------------------------------------------------------

    /**
     * Process a CERNER PID NTE segment according to UFD requirements
     * @return Returns a ZBX segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Group processPIDNTEZBXs_ToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Group aPIDNTEGroup = new HL7Group(k.NULL, 0);
        HL7Group aOutPIDNTEZBXGroup = new HL7Group(k.NULL, 0);
        HL7Segment aNTESegment = new HL7Segment(k.NULL);
        HL7Segment aZBXSegment = new HL7Segment(k.NULL);
        String aZBXValue = k.NULL;

        int aCountPIDNTEGroup = 0;

        aCountPIDNTEGroup = aHL7Message.countGroups(HL7_23.Group_PIDNotes);

        if (aCountPIDNTEGroup == 1) {
            aPIDNTEGroup.setGroup(aHL7Message.getGroup(HL7_23.Group_PIDNotes, aCountPIDNTEGroup));
            for (int i = 1; i <= aPIDNTEGroup.countSegments(CERNER_23.NTE); i++) {
                aNTESegment.setSegment(aPIDNTEGroup.getSegment(CERNER_23.NTE, i));
                aZBXValue = aNTESegment.getField(CERNER_23.NTE_3_comment);
                aZBXValue = aZBXValue.concat(k.CARROT_SET).concat(aNTESegment.getField(CERNER_23.NTE_2_source_of_comment));
                setupZBX("PMI", "NOTE", aZBXValue);
                if (aOutPIDNTEZBXGroup.getGroup().equalsIgnoreCase(k.NULL)) {
                    aOutPIDNTEZBXGroup.setGroup(aZBXSegment.getSegment());
                } else {
                    aOutPIDNTEZBXGroup.append(aZBXSegment.getSegment());
                }
            }
        }

        return aOutPIDNTEZBXGroup;
    }
    //--------------------------------------------------------------------------

    /**
     * Process a CERNER PV1 segment according to UFD requirements
     * @return Returns a PV1 segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Segment processPV1ToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aInPV1Segment = new HL7Segment(k.NULL);
        HL7Segment aOutPV1Segment = new HL7Segment(k.NULL);

        aInPV1Segment.setSegment(aHL7Message.getSegment(HL7_23.PV1));
        aOutPV1Segment.linkTo(aInPV1Segment);
        aOutPV1Segment.copyFields();

// Alfred Centre code translation for Ward, Bed and Building
        CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
        CodeLookUp aLU_BED = new CodeLookUp("ALFCENTRE_BED.table", mEnvironment);
        //Current Location
        String aACWard = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
        String aACBed = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
        String aACWardBed = aACWard + "_" + aACBed;
        String aACBuilding = "";
        aACWard = aLU_WARD.getValue(aACWardBed);
        aACBed = aLU_BED.getValue(aACWardBed);
        //Prior Location
        String aACWardPrior = aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
        String aACBedPrior = aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
        String aACWardBedPrior = aACWardPrior + "_" + aACBedPrior;
        String aACBuildingPrior = "";
        aACWardPrior = aLU_WARD.getValue(aACWardBedPrior);
        aACBedPrior = aLU_BED.getValue(aACWardBedPrior);

        if (aACWard.length() > 0) {
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aACWard);
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aACBed);
        }

        if (aACWardPrior.length() > 0) {
            aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu, aACWardPrior);
            aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed, aACBedPrior);
        }

        String aAttendingDoctorLastName = aOutPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name);
        String aAttendingDoctorIDNum = aOutPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num);
        String aPatientClass = aOutPV1Segment.get(HL7_23.PV1_2_patient_class);
        String aHospitalService = aOutPV1Segment.get(HL7_23.PV1_10_hospital_service);

        if ((aAttendingDoctorLastName.equalsIgnoreCase(k.NULL) ||
                aAttendingDoctorIDNum.indexOf("XX999") == 0) &&
                aPatientClass.equalsIgnoreCase("E")) {
            if (mFacility.equalsIgnoreCase("SDMH")) {
                aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, "TJ294");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, "FM264");
            }
        }

        if (mFacility.equalsIgnoreCase("CGMC")) {
            if (((aHospitalService.length() == 0) ||
                    aHospitalService.equalsIgnoreCase("\"\"")) &&
                    aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "CEM");
            }
        } else if (mFacility.equalsIgnoreCase("SDMH")) {
            if (((aHospitalService.length() == 0) ||
                    aHospitalService.equalsIgnoreCase("\"\"")) &&
                    aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "SED");
            }
        } else {
            if (((aHospitalService.length() == 0) ||
                    aHospitalService.equalsIgnoreCase("\"\"")) &&
                    aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "AEM");
            }
        }


        //-gg copy pv1-7 -> pv1-17
          aOutPV1Segment.set(HL7_23.PV1_17_admitting_doctor,aOutPV1Segment.get(HL7_23.PV1_7_attending_doctor));


//        if ((aHospitalService.equalsIgnoreCase(k.NULL) ||
//                aHospitalService.equalsIgnoreCase("\"\"")) &&
//                aPatientClass.equalsIgnoreCase("E")) {
//            aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "EMER");
//        }

        return aOutPV1Segment;
    }


    //--------------------------------------------------------------------------
    /**
     * Translates order observation group to UFD structure.
     * @return Order observation group
     */
//    public HL7Group processOrderObservations_ToUFD() {
//        HL7Group aOutOrderObservationsGroup = new HL7Group("");
//        int aGroupCount;
//        int aCount = 1;
//
//        String aGroupID[] = HL7_23.Group_Orders;
//        aGroupCount = mInHL7Message.countGroups(aGroupID);
//
//        for (aCount = 1; aCount <= aGroupCount; aCount++) {
//            aOutOrderObservationsGroup.append(mInHL7Message.getGroup(aGroupID, aCount));
//        }
//        return aOutOrderObservationsGroup;
//    }
    /**
     * Translates order observation group to UFD structure.
     * @return Order observation group
     *
     *
     *         HL7Segment aZBXSegment = new HL7Segment(HL7_24.ZBX);
    aZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString( mZBXSegmentCount++));
    aZBXSegment.set(HL7_24.ZBX_2_group, aGroupID);
    aZBXSegment.set(HL7_24.ZBX_3_field, aItemID);
    aZBXSegment.set(HL7_24.ZBX_4_value, aValue);
    return aZBXSegment;
     *
     */
    public HL7Group processOrderObservations_ToUFD() {
        HL7Group aOutOrderObservationsGroup = new HL7Group("");
        int aGroupCount;
        int aCount = 1;
        String aGroupID[] = new String[0];
        boolean noORC = false;
        if (mInHL7Message.countSegments("ORC") == 0) {
            aGroupID = HL7_23.Group_Orders_without_ORC;
            noORC = true;
        } else {
            aGroupID = HL7_23.Group_Orders;
        }
        aGroupCount = mInHL7Message.countGroups(aGroupID);
        //aGroupCount = 1;

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            if (noORC) {
                HL7Segment ORC = new HL7Segment(HL7_24.ORC);
                ORC.set(HL7_24.ORC_1_order_control, "RE");
                aOutOrderObservationsGroup.append(ORC);
            }
            aOutOrderObservationsGroup.append(mInHL7Message.getGroup(aGroupID, aCount));
        }
        return aOutOrderObservationsGroup;
    }
}
