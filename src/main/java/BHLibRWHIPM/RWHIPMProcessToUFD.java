/*
 * RWHIPMProcessToUFD.java
 *
 * Created on 2013-09-05
 *
 */
package BHLibRWHIPM;

import BHLibClasses.*;
//import java.text.*;

/**
 * CERNERProcessToUFD provides methods to process a message from CERNER to UFD
 * structure.  These methods are CERNER specific.
 * @author Ray Fillingham and Norman Soh
 */
public class RWHIPMProcessToUFD extends ProcessSegmentsToUFD {

    /**
     * This is a ZBX counter that keeps a record of the number of ZBX segments
     * created
     */
    public int mZBXSeqNum = 1;
    /**
     * Class wide HL7Message object
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";
    public boolean mA28Forced = false;

    /**
     * Creates a new instance of CERNERProcessToUFD
     * @param pHL7Message HL7 Message string
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public RWHIPMProcessToUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "B";    // CERNERProcessToUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------

    /**
     * processMessage will convert / process the entire CERNER message into a
     * UFD HL7 message structure
     * @return Returns a UFD HL7 message
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public String processMessage() throws ICANException {
        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));

        if (mInHL7Message.isEvent("A28, A31")) {
            aOutHL7Message.append(processMSHToUFD()); //Local
            aOutHL7Message.append(processEVNToUFD());
            aOutHL7Message.append(processPIDToUFD()); //Local
            aOutHL7Message.append(processNK1s_ToUFD());
            aOutHL7Message.append(processPV1ToUFD());
            aOutHL7Message.append(processPV2ToUFD());
//            aOutHL7Message.append(processAL1s_ToUFD());
            aOutHL7Message.append(processGT1s_ToUFD());
            aOutHL7Message.append(processOBXs_ToUFD());

        }
        if (mInHL7Message.isEvent("A28")) {
            aOutHL7Message.append(setupZBX("MESSAGE", "NEW_PATIENT_FLAG", "Y"));
        }
        if (aOutHL7Message.getMessage().length() > 0) {
            mZBXSegmentCount = mZBXSeqNum;
            aOutHL7Message.append(setupZBX("MESSAGE", "SOURCE_ID", aInMSHSegment.get(HL7_23.MSH_10_message_control_ID)));
        }
        return aOutHL7Message.getMessage();
    }

    /**
     * Returns the MSH segment from the message.  No further processing of the segment
     * is performed.
     * @return MSH segment
     */
    @Override
    public HL7Segment processMSHToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aOutMSHSegment = new HL7Segment(k.NULL);
        aOutMSHSegment.setSegment(aHL7Message.getSegment(HL7_23.MSH));
        String aMessageType = aOutMSHSegment.get(HL7_23.MSH_9_2_trigger_event).substring(0, 3);
        String aOriginatingSys = aOutMSHSegment.get(HL7_23.MSH_3_sending_application);
        if (aOriginatingSys.indexOf("CERNER") >= 0) {
            aOriginatingSys = "C";
        } else if (aOriginatingSys.indexOf("FIRSTNET") >= 0) {
            aOriginatingSys = "C";
        } else if (aOriginatingSys.indexOf("CARENET") >= 0) {
            aOriginatingSys = "C";
        } else if (aOriginatingSys.indexOf("CSC") >= 0) {
            aOriginatingSys = "H";
        } else if (aOriginatingSys.indexOf("IPM") >= 0) {
            aOriginatingSys = "I";
        } else {
            aOriginatingSys = aOriginatingSys.substring(0, 1);
        }
        String aOriginalCID = aOutMSHSegment.get(HL7_23.MSH_10_message_control_ID);

        mFacility = aOutMSHSegment.getField(HL7_23.MSH_4_sending_facility);
        mHL7Message = aHL7Message.getMessage();
        mHL7MessageEvent = aHL7Message.getTriggerEvent();

        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "DGATE");
        aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "SDMH");
        aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "SDMH");
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aOriginatingSys.concat(aMessageType).concat(aOriginalCID));
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");
        return aOutMSHSegment;
    }

    //--------------------------------------------------------------------------
    /**
     * Process a CERNER PID segment according to UFD requirements
     * @return Returns a PID segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    @Override
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
        String[] ids = aInPIDSegment.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 0; i < ids.length; i++) {
            HL7Field currentID = new HL7Field(ids[i]);
            String type = currentID.getSubField(HL7_23.CX_ID_type_code);
            String assignAuth = currentID.getSubField(HL7_23.CX_assigning_authority);
            if (type == null) {
                continue;
            }

            if (type.equalsIgnoreCase("MR")) {
                aPID_3.setField(ids[i]);
                aPID_3.setSubField("PI", HL7_23.CX_ID_type_code);
            }

            if (type.equalsIgnoreCase("PEN")) {
                aPID_4.setField(ids[i]);
                aPID_4.setSubField("PEN", HL7_23.CX_ID_type_code);
                aPID_4.setSubField("PEN", HL7_23.CX_assigning_authority);
            }

            if (type.equalsIgnoreCase("MC")) {
                //TODO: need to check for date//
                aPID_19.setField(ids[i]);
                aPID_19.setSubField("MC", HL7_23.CX_ID_type_code);
                aPID_19.setSubField("HIC", HL7_23.CX_assigning_authority);
                if (aPID_19.getSubField(HL7_23.CX_ID_number).indexOf(" ") == 0) {
                    String aCodeID = aPID_19.getSubField(HL7_23.CX_ID_number);
                    aCodeID = aCodeID.replaceAll(" ", "");
                    aPID_19.setSubField(aCodeID, HL7_23.CX_ID_number);
                }
            }

            if (type.startsWith("DV")) {

                aPID_2.setField(ids[i]);
                aPID_2.setSubField("VA", HL7_23.CX_ID_type_code);
                aPID_2.setSubField("DVA", HL7_23.CX_assigning_authority);
            }
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


        //Map Race
        CodeLookUp raceLookUp = new CodeLookUp("IPM_ETHGR.table", mEnvironment);
        HL7Field f10 = new HL7Field();
        f10.setField(aInPIDSegment.getField(10));
        String strRaceID = f10.getSubField(1);
        String raceID = raceLookUp.getValue(strRaceID);
        aOutPIDSegment.set(HL7_23.PID_10_race, raceID);

        //Map Language
        CodeLookUp languageLookUp = new CodeLookUp("IPM_LANGUAGE.table", mEnvironment);
        HL7Field f15 = new HL7Field();
        f15.setField(aInPIDSegment.getField(15));
        String strLanguageID = f15.getSubField(1);
        String languageID = languageLookUp.getValue(strLanguageID);
        aOutPIDSegment.set(HL7_23.PID_15_language, languageID);

        //Check PID-11 for ..A28 prefix in street 2 sub-field
        String aStreet2Add = aInPIDSegment.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2);
        if (aStreet2Add.startsWith("..A28")) {
            mA28Forced = true;
            aStreet2Add = aStreet2Add.substring(5);
            aOutPIDSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, aStreet2Add);
        }

        CodeLookUp addTypeLookup = new CodeLookUp("IPM_ADDTYPE.table", mEnvironment);
        String[] addresses = aInPIDSegment.getRepeatFields(HL7_23.PID_11_patient_address);
        for (int i = 1; i <= addresses.length; i++) {
            String ufdAddType = addTypeLookup.getValue(aInPIDSegment.get(HL7_23.PID_11_patient_address, HL7_23.XAD_type, i));
            aOutPIDSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_type, ufdAddType, i);

            aOutPIDSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_geographic_designation, "");
        }

        //Map Religion
        CodeLookUp rLookUp = new CodeLookUp("IPM_RELIGION.table", mEnvironment);

        String religionID = aInPIDSegment.get(HL7_23.PID_17_religion, HL7_23.CE_ID_code);

        if (religionID != null) {
            String rID = rLookUp.getValue(religionID);
            if (religionID.equalsIgnoreCase(rID)) {
                aOutPIDSegment.set(HL7_23.PID_17_religion, "UNK");
            } else {
                aOutPIDSegment.set(HL7_23.PID_17_religion, rID);
            }
        }

        //blank out PID18
        aOutPIDSegment.set(HL7_23.PID_18_account_number, "");

        //Map Marital Status
        CodeLookUp aLookUp = new CodeLookUp("IPM_MARITAL_STATUS.table", mEnvironment);
        HL7Field f = new HL7Field();
        f.setField(aInPIDSegment.getField(16));
        String MarStatID = f.getSubField(1);
        String msID = aLookUp.getValue(MarStatID);
        aOutPIDSegment.set(HL7_23.PID_16_marital_status, msID);

        //Move PID-22 ethnic group to PID-23 birthplace
//                HL7Field f22 = new HL7Field();
//                f22.setField(aInPIDSegment.getField(22));
//                String ethnicID = f22.getSubField(1);
//                aOutPIDSegment.set(HL7_23.PID_23_birth_place, k.NULL);
//                aOutPIDSegment.set(HL7_23.PID_23_birth_place, ethnicID);
        aOutPIDSegment.set(HL7_23.PID_22_ethnic_group, k.NULL);

        return aOutPIDSegment;
    }

    /**
     * Returns the NK1 segment/s from the message.  No further processing of the segment
     * is performed.
     * @return NK1 segment/s
     */
    @Override
    public HL7Group processNK1s_ToUFD() throws ICANException {
        return processGroup(HL7_23.Repeat_NK1);
    }

    /**
     * Generic group processing
     * @param pGroupID Group identifier
     * @return Returns a group text string
     */
    @Override
    public HL7Group processGroup(String pGroupID) throws ICANException {
        String aGroupID[] = {pGroupID};
        return processGroup(aGroupID);
    }

    /**
     * Generic group processing
     * @param pGroupID Group identifier
     * @return Returns a group text string
     */
    @Override
    public HL7Group processGroup(String pGroupID[]) throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Group aGroup = new HL7Group(k.NULL, 0);
        int aGroupCount;
        int aCount = 1;

        String aGroupID[] = pGroupID;
        aGroupCount = aHL7Message.countGroups(aGroupID);

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            HL7Segment s = new HL7Segment(HL7_23.NK1);
            s.setSegment(aHL7Message.getGroup(aGroupID, aCount));

            //Concatenate Names
            HL7Field f2 = new HL7Field(k.NULL);
            HL7Field f1Original = new HL7Field(k.NULL);
            f1Original.setField(s.get(HL7_23.NK1_2_next_of_kin_name));
            f2.setSubField(f1Original.getSubField(2) + " " + f1Original.getSubField(1), 1);
            f2.setSubField("L", 7);
            s.set(HL7_23.NK1_2_next_of_kin_name, f2.getField());

            //Map Relation
            CodeLookUp rLookUp = new CodeLookUp("IPM_RELTN.table", mEnvironment);
            HL7Field f3 = new HL7Field();
            f3.setField(s.getField(3));
            String RelationID = f3.getSubField(1);
            String rID = rLookUp.getValue(RelationID);
            if (1 == 2) {
                s.set(HL7_23.NK1_3_next_of_kin_relationship, "");
            } else {
                s.set(HL7_23.NK1_3_next_of_kin_relationship, rID);
            }

            //Trim Contact Role
            HL7Field f7 = new HL7Field(k.NULL);
            HL7Field f7Original = new HL7Field(k.NULL);
            f7Original.setField(s.get(HL7_23.NK1_7_contact_role));
            f7.setSubField(f7Original.getSubField(1), 1);
            s.set(HL7_23.NK1_7_contact_role, f7.getField());

            aGroup.append(s);
        }
        return aGroup;
    }

    /**
     * Process a CERNER PV1 segment according to UFD requirements
     * @return Returns a PV1 segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    @Override
    public HL7Segment processPV1ToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        //HL7Segment aInPD1Segment = new HL7Segment(k.NULL);
        //aInPD1Segment.setSegment(aHL7Message.getSegment(HL7_24.PD1));
        HL7Segment aOutPV1Segment = new HL7Segment(HL7_24.PV1);
        aOutPV1Segment.set(HL7_23.PV1_1_set_ID, "1");
        aOutPV1Segment.set(HL7_23.PV1_2_patient_class, "R");
        aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, "^^^SDMH");
        //aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, aInPD1Segment.get(HL7_24.PD1_4_patient_primary_care_provider_name_id, HL7_24.XCN_ID_num));
        return aOutPV1Segment;
//        aInPD1Segment.setSegment(aHL7Message.getSegment(HL7_23.PV1));
//
//
//        aOutPV1Segment.linkTo(aInPD1Segment);
//
//        aOutPV1Segment.copyFields();
//
//// Alfred Centre code translation for Ward, Bed and Building
//        CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
//        CodeLookUp aLU_BED = new CodeLookUp("ALFCENTRE_BED.table", mEnvironment);
//        //Current Location
//        String aACWard = aInPD1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
//        String aACBed = aInPD1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
//        String aACWardBed = aACWard + "_" + aACBed;
//        String aACBuilding = "";
//        aACWard = aLU_WARD.getValue(aACWardBed);
//        aACBed = aLU_BED.getValue(aACWardBed);
//        //Prior Location
//        String aACWardPrior = aInPD1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
//        String aACBedPrior = aInPD1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
//        String aACWardBedPrior = aACWardPrior + "_" + aACBedPrior;
//        String aACBuildingPrior = "";
//        aACWardPrior = aLU_WARD.getValue(aACWardBedPrior);
//        aACBedPrior = aLU_BED.getValue(aACWardBedPrior);
//
//        if (aACWard.length() > 0) {
//            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aACWard);
//            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aACBed);
//        }
//
//        if (aACWardPrior.length() > 0) {
//            aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu, aACWardPrior);
//            aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed, aACBedPrior);
//        }
//
//        String aAttendingDoctorLastName = aOutPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name);
//        String aAttendingDoctorIDNum = aOutPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num);
//        String aPatientClass = aOutPV1Segment.get(HL7_23.PV1_2_patient_class);
//        String aHospitalService = aOutPV1Segment.get(HL7_23.PV1_10_hospital_service);
//
//        if ((aAttendingDoctorLastName.equalsIgnoreCase(k.NULL) ||
//                aAttendingDoctorIDNum.indexOf("XX999") == 0) &&
//                aPatientClass.equalsIgnoreCase("E")) {
//            if (mFacility.equalsIgnoreCase("SDMH")) {
//                aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, "TJ294");
//            } else {
//                aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, "FM264");
//            }
//        }
//
//        if (mFacility.equalsIgnoreCase("CGMC")) {
//            if (((aHospitalService.length() == 0) ||
//                    aHospitalService.equalsIgnoreCase("\"\"")) &&
//                    aPatientClass.equalsIgnoreCase("E")) {
//                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "CEM");
//            }
//        } else if (mFacility.equalsIgnoreCase("SDMH")) {
//            if (((aHospitalService.length() == 0) ||
//                    aHospitalService.equalsIgnoreCase("\"\"")) &&
//                    aPatientClass.equalsIgnoreCase("E")) {
//                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "SED");
//            }
//        } else {
//            if (((aHospitalService.length() == 0) ||
//                    aHospitalService.equalsIgnoreCase("\"\"")) &&
//                    aPatientClass.equalsIgnoreCase("E")) {
//                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "AEM");
//            }
//        }
//
////        if ((aHospitalService.equalsIgnoreCase(k.NULL) ||
////                aHospitalService.equalsIgnoreCase("\"\"")) &&
////                aPatientClass.equalsIgnoreCase("E")) {
////            aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "EMER");
////        }
//
//        return aOutPV1Segment;
    }
}
