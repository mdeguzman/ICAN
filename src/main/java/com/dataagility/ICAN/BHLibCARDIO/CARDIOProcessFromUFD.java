/*
 * CARDIOProcessFromUFD.java
 *
 * Created on 11 October 2005, 17:04
 *
 */

package com.dataagility.ICAN.BHLibCARDIO;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * CARDIOProcessFromUFD contains the methods required to build a CARDIO message
 * from a UFD HL7 message structure
 *
 * @author Ray Fillingham and Norman Soh
 */
public class CARDIOProcessFromUFD extends ProcessSegmentsFromUFD {
    /**
     * Constant class
     */
    public BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public HL7Message mInHL7Message = new HL7Message("");
    //--------------------------------------------------------------------------

    /**
     * This constructor creates a new instance of CARDIOProcessFromUFD passing a HL7 UFD
     * message structure
     *
     * @param pHL7Message HL7 message text string
     * @throws ICANException ICANException
     */
    public CARDIOProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------

    /**
     * This method contains the methods required to build a CARDIO HL7 message
     *
     * @return CARDIO HL7 message text string
     * @throws ICANException ICANException
     */
    public String[] processMessage() throws ICANException {

        mVersion = "B";
        String aCARDIOMessageArray[] = {k.NULL, k.NULL, k.NULL};
        HL7Message aOutHL7Message = new HL7Message();
        mInHL7Message = new HL7Message(mHL7Message, k.TRIM_LAST);
        HL7Segment aPV1SegmentTemp = new HL7Segment(mInHL7Message.getSegment("PV1"));

        if (mInHL7Message.isEvent("A01, A02, A03, A04, A08, A11, A12, A13, A21, A22, A28, A31")
//                &&     !aPV1SegmentTemp.hasValue(HL7_23.PV1_2_patient_class,"PO")
                ) {
            HL7Segment aMSHSegment = processMSHFromUFD("CARDIO");
            HL7Segment aEVNSegment = processEVNFromUFD();
            HL7Segment aPIDSegment = processPIDFromUFD();
            HL7Group aNK1Group = processNK1s_FromUFD();
            HL7Segment aPV1Segment = processPV1FromUFD();
            HL7Segment aPV2Segment = processPV2FromUFD();
            HL7Segment aDRGSegment = processDRGFromUFD();
            HL7Group aDG1Group = processDG1s_FromUFD();
            HL7Group aPR1Group = processPR1s_FromUFD();
            HL7Segment aZPDSegment = new HL7Segment("");
            HL7Segment aZMRSegment = new HL7Segment("");
            HL7Segment aGT1Segment = processGT1FromUFD();
            HL7Group aZBXGroup = new HL7Group();

            aMSHSegment = processCARDIO_MSH(aMSHSegment);
            aPIDSegment = processCARDIO_PID(aPIDSegment);
            //aNK1Group = processCARDIO_NK1(aNK1Group);
            aPV1Segment = processCARDIO_PV1(aPV1Segment);
            aPV2Segment = processCARDIO_PV2(aPV2Segment);
            aZPDSegment = processCARDIO_ZPD(aZPDSegment, aPV1Segment);
            aZMRSegment = processCARDIO_ZMR(aZMRSegment);

            System.out.println("++++ Processing ZBX Segments for CARDIO");
            aZBXGroup = processCARDIO_ZBX(mInHL7Message);

            aOutHL7Message.setSegment(aMSHSegment.getSegment());
            aOutHL7Message.append(aEVNSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aNK1Group);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aPV2Segment);
            aOutHL7Message.append(aDRGSegment);
            aOutHL7Message.append(aDG1Group);
            aOutHL7Message.append(aPR1Group);
            aOutHL7Message.append(aZPDSegment);
            aOutHL7Message.append(aZMRSegment);
            aOutHL7Message.append(aGT1Segment);
            aOutHL7Message.append(aZBXGroup);

            aCARDIOMessageArray[0] = aMSHSegment.get(HL7_23.MSH_3_sending_application);
            aCARDIOMessageArray[1] = aMSHSegment.get(HL7_23.MSH_4_sending_facility);
            aCARDIOMessageArray[2] = aOutHL7Message.getMessage();
        }
        return aCARDIOMessageArray;
    }
    //--------------------------------------------------------------------------

    /**
     * CARDIO specific processing for MSH segment
     *
     * @param pMSHSegment MSH segment class object
     * @return MSH segment class object
     * @throws ICANException ICANException
     */
    public HL7Segment processCARDIO_MSH(HL7Segment pMSHSegment) throws ICANException {
        //Set the message date and time
        pMSHSegment.set(HL7_23.MSH_7_message_date_time, pMSHSegment.getDateTime("yyyyMMddHHmm"));
        //Set the message control id
        //aMSHSegment.set(HL7_23.MSH_10_message_control_ID, aMSHSegment.getDateTime("yyyyMMddHHmm"));
        //Set the processing id
        pMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        //Set the version id
        pMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");
        return pMSHSegment;
    }
    //--------------------------------------------------------------------------

    /**
     * CARDIO specific processing for PID segment
     *
     * @param pPIDSegment PID segment
     * @return PID segment
     * @throws ICANException ICANException
     */
    public HL7Segment processCARDIO_PID(HL7Segment pPIDSegment) throws ICANException {
        //process PID-3 ID internal number to 6 / 7 digit number
        String aPID3Array[] = pPIDSegment.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        HL7Field aPID3Field = new HL7Field();
        //CodeLookUp aLookUp = new CodeLookUp("SDMH_6Digit_UR.table", mEnvironment);
        CodeLookUp aLookUp = new CodeLookUp("SDMH_6To7Digit_UR.table", mEnvironment);
        int aPID3ArrayCount = aPID3Array.length;
        String aSDMHIdentifier = k.NULL;

        for (int i = 0; i < aPID3ArrayCount; i++) {
            aPID3Field.setField(aPID3Array[i]);
            if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("PI")) {
                if (mFacility.equalsIgnoreCase("SDMH") &&
                        Integer.parseInt(aPID3Field.getSubField(HL7_23.CX_ID_number)) < 899546) {
                    aSDMHIdentifier = aLookUp.getValue(aPID3Field.getSubField(HL7_23.CX_ID_number));
                    if (!aSDMHIdentifier.equalsIgnoreCase(k.NULL)) {
                        aPID3Field.setSubField(aSDMHIdentifier, HL7_23.CX_ID_number);
                        aPID3Array[i] = aPID3Field.getField();
                    }
                }
            }
        }
        pPIDSegment.setRepeatFields(HL7_23.PID_3_patient_ID_internal, aPID3Array);

        //process first address in PID-11 Patient address
        String aPID11Array[] = pPIDSegment.getRepeatFields(HL7_23.PID_11_patient_address);
        int aPID11ArrayCount = aPID11Array.length;
        if (aPID11ArrayCount > 0) {
            pPIDSegment.set(HL7_23.PID_11_patient_address, aPID11Array[0]);
        }

        //clear PID-9 Patient Alias
        pPIDSegment.set(HL7_23.PID_9_patient_alias, k.NULL);

        //process PID-19 SSN number padding spaces
        String aSSN = pPIDSegment.get(HL7_23.PID_19_SSN_number);
        if (aSSN.length() == 3) {
            aSSN = "            ".concat(aSSN);
            pPIDSegment.set(HL7_23.PID_19_SSN_number, aSSN);
        }

        return pPIDSegment;
    }
    //--------------------------------------------------------------------------

    /**
     * CARDIO specific processing for NK1 group
     *
     * @param pNK1Group NK1 group
     * @return NK1 group
     * @throws ICANException ICANException
     */
    public HL7Group processCARDIO_NK1(HL7Group pNK1Group) throws ICANException {
        HL7Group aOutNK1Group = new HL7Group();
        HL7Segment aNK1Segment = new HL7Segment(k.NULL);
        HL7Field aNK1_2Field = new HL7Field();
        String aLastName = k.NULL;
        String aFirstName = k.NULL;
        String aMiddleInit = k.NULL;
        String aPrefix = k.NULL;
        int aNK1SegmentCount = pNK1Group.countSegments();
        for (int i = 1; i <= aNK1SegmentCount; i++) {
            aNK1Segment.setSegment(pNK1Group.getSegment(i));
            aLastName = aNK1Segment.get(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_family_name);
            aFirstName = aNK1Segment.get(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_given_name);
            aMiddleInit = aNK1Segment.get(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_middle_name);
            aPrefix = aNK1Segment.get(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_prefix);
            aNK1_2Field.setSubField(aLastName, HL7_23.XPN_family_name);
            aNK1_2Field.setSubField(aFirstName, HL7_23.XPN_given_name);
            aNK1_2Field.setSubField(aMiddleInit, HL7_23.XPN_middle_name);
            aNK1_2Field.setSubField(aPrefix, HL7_23.XPN_prefix);
            aNK1Segment.set(HL7_23.NK1_2_next_of_kin_name, aNK1_2Field.getField());

            aOutNK1Group.append(aNK1Segment);
        }
        return aOutNK1Group;
    }
    //--------------------------------------------------------------------------

    /**
     * CARDIO specific processing for PV1 segment
     *
     * @param pPV1Segment PV1 segment
     * @return PV1 segment
     * @throws ICANException ICANException
     */
    public HL7Segment processCARDIO_PV1(HL7Segment pPV1Segment) throws ICANException {
        //set room number to 1 if patient class is I and no room number is stored in PV1_3 assigned patient location
        if (pPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room).length() == 0 &&
                pPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("I")) {
            pPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "1");
        }
        //set room number to 1 if patient class is I and no room number is stored in PV1_6 prior patient location
        if (pPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_room).length() == 0 &&
                pPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("I")) {
            pPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_room, "1");
        }

        //clear PV1-9 consulting doctor
        pPV1Segment.set(HL7_23.PV1_9_consulting_doctor, k.NULL);

        //remove prefix char in PV1-19 visit number
//        String aVisitNum = pPV1Segment.get(HL7_23.PV1_19_visit_number);
//        if (aVisitNum.startsWith("I")) {
//            pPV1Segment.set(HL7_23.PV1_19_visit_number, aVisitNum.substring(1));
//        }

        //process PV1_20
        CodeLookUp aLookUp = new CodeLookUp("CERNER_PayClass_SQL.table", mEnvironment);
        String aCode = pPV1Segment.get(HL7_23.PV1_20_financial_class);
        String aValue = aLookUp.getValue(aCode);
        pPV1Segment.set(HL7_23.PV1_20_financial_class, aValue);

        //process PV1-3 assigned patient location
        String aPV1_3Field = pPV1Segment.get(HL7_23.PV1_3_assigned_patient_location);
        if (aPV1_3Field.length() == 0) {
            pPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, "\"\"");
        }

        //process PV1-6 prior patient location
        String aPV1_6PointOfCare = pPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
        if (aPV1_6PointOfCare.length() == 0 ||
                aPV1_6PointOfCare.equalsIgnoreCase("\"\"")) {
            pPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu, "\"\"");
        }

        return pPV1Segment;
    }

    public HL7Segment processCARDIO_PV2(HL7Segment pPV2Segment) throws ICANException {
        HL7Segment aOutPV2Segment = pPV2Segment;
        String aAdmitReason = aOutPV2Segment.get(HL7_23.PV2_3_admit_reason, HL7_23.CE_text);
        aOutPV2Segment.set(HL7_23.PV2_3_admit_reason, HL7_23.CE_ID_code, aAdmitReason);
        return aOutPV2Segment;
    }
    //--------------------------------------------------------------------------

    /**
     * CARDIO specific processing for ZPD segment
     *
     * @param pZPDSegment ZPD segment
     * @param pPV1Segment PV1 segment
     * @return ZPD segment
     * @throws ICANException ICANException
     */
    public HL7Segment processCARDIO_ZPD(HL7Segment pZPDSegment, HL7Segment pPV1Segment) throws ICANException {
        HL7Segment aOutZPDSegment = new HL7Segment("ZPD");
        String aHospitalService = pPV1Segment.get(HL7_23.PV1_10_hospital_service);
        String aClaim_no = getFromZBX("FINANCE", "CLAIM_NUMBER");
        aOutZPDSegment.set(CARDIO_23_old.ZPD_2_patient_team, aHospitalService);
        aOutZPDSegment.set(CARDIO_23_old.ZPD_1, aClaim_no);
        return aOutZPDSegment;

    }
    //--------------------------------------------------------------------------

    /**
     * CARDIO specific processing for ZMR segment
     *
     * @param pZMRSegment ZMR segment
     * @return ZMR segment
     * @throws ICANException ICANException
     */
    public HL7Segment processCARDIO_ZMR(HL7Segment pZMRSegment) throws ICANException {
        String aLastMoveDateTime = getFromZBX("MEDREC", "LAST_MOVE_DATE_TIME");
        String aVolumeNumber = getFromZBX("MEDREC", "VOLUME_NUMBER");
        String aLocation = getFromZBX("MEDREC", "LOCATION");
        String aReceivedBy = getFromZBX("MEDREC", "RECEIVED_BY");
        String aExtension = getFromZBX("MEDREC", "EXTENSION");
        if (aLastMoveDateTime.length() > 0) {
            String aDate = k.NULL;
            String aTime = k.NULL;
            if (aLastMoveDateTime.length() > 12) {
                aDate = aLastMoveDateTime.substring(0, 8);
                aTime = aLastMoveDateTime.substring(8);
            } else {
//                aDate = "\"\"";
//                aTime = "\"\"";
            }
            pZMRSegment = new HL7Segment(CARDIO_23_old.ZMR);
            pZMRSegment.set(CARDIO_23_old.ZMR_1_Last_Movement_Date, aDate);
            pZMRSegment.set(CARDIO_23_old.ZMR_2_Last_Movement_Time, aTime);
            pZMRSegment.set(CARDIO_23_old.ZMR_3_Volume_Number, aVolumeNumber);
            pZMRSegment.set(CARDIO_23_old.ZMR_4_Location, aLocation);
            pZMRSegment.set(CARDIO_23_old.ZMR_5_Received_By, aReceivedBy);
            pZMRSegment.set(CARDIO_23_old.ZMR_6_Extension_phone, aExtension);
        }
        return pZMRSegment;
    }

    public HL7Segment processGT1FromUFD() throws ICANException {
        HL7Segment aInGT1Segment = new HL7Segment(mInHL7Message.getSegment("GT1", 1));
        HL7Segment aOutGT1Segment = new HL7Segment("");
        if (aInGT1Segment.getSegment().length() > 0 &&
                aInGT1Segment.get(HL7_23.GT1_3_guarantor_name).length() > 0) {
            aOutGT1Segment = new HL7Segment("GT1");
            aOutGT1Segment.linkTo(aInGT1Segment);
            aOutGT1Segment.set(HL7_23.GT1_1_set_ID, "1");
            aOutGT1Segment.copy(HL7_23.GT1_2_guarantor_number);
            aOutGT1Segment.copy(HL7_23.GT1_3_guarantor_name);
            aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1);
            aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2);
            aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city);
            aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province);
            aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip);
            aOutGT1Segment.copy(HL7_23.GT1_6_guarantor_phone_home, 1);
            aOutGT1Segment.copy(HL7_23.GT1_7_guarantor_phone_business, 1);
            aOutGT1Segment.copy(HL7_23.GT1_10_guarantor_type);
        } else if (mInHL7Message.isEvent("A04")) {
            aOutGT1Segment = new HL7Segment("GT1");
            aOutGT1Segment.set(HL7_23.GT1_1_set_ID, "1");
            aOutGT1Segment.set(HL7_23.GT1_3_guarantor_name, "");
        }

        return aOutGT1Segment;
    }
    //--------------------------------------------------------------------------

    /**
     * CARDIO specific processing for ZBX segment
     *
     * @param pHL7Message HL7 message
     * @return ZBX group
     * @throws ICANException ICANException
     */
    public HL7Group processCARDIO_ZBX(HL7Message pHL7Message) throws ICANException {

        HL7Group aZBXGroup = new HL7Group();
        HL7Segment aInZBXSegment = new HL7Segment(k.NULL);
        HL7Segment aOutZBXSegment = new HL7Segment(k.NULL);
        String aGroupID = k.NULL;
        String aIdentifier = k.NULL;
        String aValue = k.NULL;
        int aSetID = 1;
        int aCountZBXSegments = pHL7Message.countSegments("ZBX");

        //System.out.println("++++ ZBX Segments in Msg for CARDIO: " + aCountZBXSegments );

        for (int i = 1; i <= aCountZBXSegments; i++) {
            aInZBXSegment.setSegment(pHL7Message.getSegment("ZBX", i));
            aGroupID = aInZBXSegment.get(HL7_24.ZBX_2_group);
            aIdentifier = aInZBXSegment.get(HL7_24.ZBX_3_field);
            aValue = aInZBXSegment.get(HL7_24.ZBX_4_value);

            //System.out.println("++++ >>>> ZBX Segment GROUP ID: " + aGroupID + "  VALUE: " + aValue );
            // KIRAN ADDED June 24,2010 to prevent WIESValue from having / in it
            if (!aGroupID.equalsIgnoreCase("VISIT")) {
                if (aValue.length() == 6) {
                    aValue = aValue.substring(4, 6) + "/" + aValue.substring(0, 4);
                }
            }
            if (aGroupID.equalsIgnoreCase("PMI") &&
                    aIdentifier.equalsIgnoreCase("MEDICARE_EXPIRY")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "PMI");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, "MEDExp");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "Medicare Expiry Date");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "DT");
                aZBXGroup.append(aOutZBXSegment);
            }
            if (aGroupID.equalsIgnoreCase("PMI") &&
                    aIdentifier.equalsIgnoreCase("PENSION_EXPIRY_DATE")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "PMI");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, "PENExp");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "Pension Expiry Date");
                if (aValue.length() > 8) {
                    aValue = aValue.substring(0, 8);
                }
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "DT");
                aZBXGroup.append(aOutZBXSegment);
            }
            if (aGroupID.equalsIgnoreCase("PMI") &&
                    aIdentifier.equalsIgnoreCase("PBS_SAFETYNET_NUMBER")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "PMI");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, "PBSNum");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "PBS Safety Number");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);
            }
            if (aGroupID.equalsIgnoreCase("PMI") &&
                    aIdentifier.equalsIgnoreCase("DVA_CARD_TYPE")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "PMI");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, "DVACard");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "DVA Card Type");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);
            }
            if (aGroupID.equalsIgnoreCase("PMI") &&
                    aIdentifier.equalsIgnoreCase("INTERPRETER")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "PMI");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, "INTReq");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "Interpreter Required");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);
            }
            if (aGroupID.equalsIgnoreCase("PMI") &&
                    aIdentifier.equalsIgnoreCase("OCCUPATION")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "PMI");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, "PATOcc");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "Occupation");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);
            }
            if (aGroupID.equalsIgnoreCase("PMI") &&
                    aIdentifier.equalsIgnoreCase("MENTAL_HEALTH_NUMBER")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "PMI");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, "MHPid");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "Mental Health Statewide ID");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);
            }
            if (aGroupID.equalsIgnoreCase("FINANCE") &&
                    aIdentifier.equalsIgnoreCase("CLAIM_NUMBER")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCIAL");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, "CLAIMNum");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "Claim Number");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);
            }

            //System.out.println("++++ Constructing ZBX WIES SEGMENTS FOR CARDIO");
            //Covert WIES Values to CARDIO Format
            if (aGroupID.equalsIgnoreCase("VISIT") &&
                    aIdentifier.equalsIgnoreCase("WIESValue")) {

                System.out.println("++++ GROUP ID: " + aGroupID + " Identifier: " + aIdentifier + "  aValue:" + aValue);
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "VISIT");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, aIdentifier);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "WIES Value");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);

            }
            if (aGroupID.equalsIgnoreCase("VISIT") &&
                    aIdentifier.equalsIgnoreCase("WIESCoderID")) {
                System.out.println("++++ GROUP ID: " + aGroupID + " ID: " + aIdentifier);
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "VISIT");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, aIdentifier);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "WIES Coder ID");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);
            }
            if (aGroupID.equalsIgnoreCase("VISIT") &&
                    aIdentifier.equalsIgnoreCase("WIESDate")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "VISIT");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, aIdentifier);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "WIES Date");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);
            }
            if (aGroupID.equalsIgnoreCase("VISIT") &&
                    aIdentifier.equalsIgnoreCase("WIESTime")) {
                aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(aSetID++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "VISIT");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_ID_code, aIdentifier);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_text, "WIES Time");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_coding_scheme, aValue);
                aOutZBXSegment.set(HL7_24.ZBX_3_field, HL7_24.CE_alternate_ID, "ST");
                aZBXGroup.append(aOutZBXSegment);
            }

        }//traverse ZBX Count
        return aZBXGroup;
    }
}
