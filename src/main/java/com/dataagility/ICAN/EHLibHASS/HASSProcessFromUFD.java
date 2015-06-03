/*
 * HASSProcessFromUFD.java
 *
 * Created on 11 October 2005, 17:04
 *
 */

package EHLibHASS;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * HASSProcessFromUFD contains the methods required to build a HASS message
 * from a UFD HL7 message structure
 * @author Ray Fillingham and Norman Soh
 */
public class HASSProcessFromUFD extends ProcessSegmentsFromUFD {
    /**
     * Constant class
     */
    BHConstants k = new BHConstants();
    public String mEnvironment = "";
    /**
     * Class wide HL7 message class object
     */
    public HL7Message mInHL7Message;
    /**
     * Class wide Hospital prefix variable
     */
    public String mHospitalPrefix = "";
    /**
     * Class wide variable
     */
    public String mSReg1 = "";
    /**
     * Class wide variable
     */
    public String mSReg2 = "";
    //--------------------------------------------------------------------------
    /**
     * This constructor creates a new instance of HASSProcessFromUFD passing a HL7 UFD
     * message structure
     * @param pHL7Message HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HASSProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------
    /**
     * This method contains the methods required to build a AUSLAB HL7 message
     * @return HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public String[] processMessage() throws ICANException {

        String aHASSMessageArray[] = {k.NULL, k.NULL, k.NULL};
        HL7Message aOutHL7Message = new HL7Message("");
        mInHL7Message = new HL7Message(mHL7Message, k.TRIM_LAST);

        if (mInHL7Message.isEvent("A01, A08, A11, A28, A31")) {
            HL7Segment aMSHSegment = processMSHFromUFD();
            HL7Segment aEVNSegment = processEVNFromUFD();
            HL7Segment aPIDSegment = processPIDFromUFD(aMSHSegment);
            HL7Group aNK1Group = processNK1s_FromUFD();
            HL7Segment aPV1Segment = processPV1FromUFD(aMSHSegment);
            HL7Segment aPV2Segment = processPV2FromUFD();
            HL7Group aAL1Group = processAL1s_FromUFD();
            HL7Segment aGT1Segment = processGT1FromUFD();
            HL7Segment aIN1Segment = processIN1FromUFD(aMSHSegment);

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aEVNSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aNK1Group);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aPV2Segment);
            aOutHL7Message.append(aAL1Group);
            aOutHL7Message.append(aGT1Segment);
            aOutHL7Message.append(aIN1Segment);

            aHASSMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aHASSMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aHASSMessageArray[2] = aOutHL7Message.getMessage();
        }
        return aHASSMessageArray;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processMSHFromUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        aOutMSHSegment.linkTo(aInMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "DGATE");
        aOutMSHSegment.copy(HL7_23.MSH_4_sending_facility);
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "HASS");
        aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "EDIS");
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        aOutMSHSegment.copy(HL7_23.MSH_9_1_message_type);
        aOutMSHSegment.copy(HL7_23.MSH_9_2_trigger_event);
        aOutMSHSegment.copy(HL7_23.MSH_10_message_control_ID);
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");
        return aOutMSHSegment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processEVNFromUFD() {
        HL7Segment aOutEVNSegment = new HL7Segment("EVN");
        HL7Segment aInEVNSegment = new HL7Segment(mInHL7Message.getSegment("EVN"));
        aOutEVNSegment.linkTo(aInEVNSegment);
        aOutEVNSegment.copy(HL7_23.EVN_1_event_type_code);
        aOutEVNSegment.copy(HL7_23.EVN_2_date_time_of_event);
        aOutEVNSegment.copy(HL7_23.EVN_6_event_occurred);
        return aOutEVNSegment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processPIDFromUFD(HL7Segment pMSHSegment) {
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");

        aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal);
        int aPID3FieldCount = aOutPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3FieldCount; i++) {
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("PI")) {
                String aAssFac = aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, i);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aAssFac, i);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "MR", i);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, "", i);
            }
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("MC")) {
                String aIdentifier = aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                if (aIdentifier.length() == 3) {
                    aIdentifier = "           " + aIdentifier;
                } else if (aIdentifier.length() > 3) {
                    aIdentifier = aIdentifier.replaceAll(" ", "");
                }
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aIdentifier, i);
            }
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("PEN")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "PN", i);
            }
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("VA")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "DV", i);
            }

        }


        int aPID5FieldCount = aInPIDSegment.countRepeatFields(HL7_23.PID_5_patient_name);
        for (int i = 1; i <= aPID5FieldCount; i++) {
            if (aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, i).equalsIgnoreCase("L")) {
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name,
                        aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, i), 1);
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name,
                        aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, i), 1);
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name,
                        aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, i), 1);
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix,
                        aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix, i), 1);
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type,
                        aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, i), 1);
                i = aPID5FieldCount + 1;
            }
        }

        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);

        int aPID11PatientAddressCount = aInPIDSegment.countRepeatFields(HL7_23.PID_11_patient_address);
        for (int i = 1; i <= aPID11PatientAddressCount; i++) {
            if (aInPIDSegment.get(HL7_23.PID_11_patient_address, HL7_23.XAD_type, i).equalsIgnoreCase("RES") ||
                    aInPIDSegment.get(HL7_23.PID_11_patient_address, HL7_23.XAD_type, i).equalsIgnoreCase("P")) {
                aOutPIDSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1,
                        aInPIDSegment.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, i), 1);
                aOutPIDSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2,
                        aInPIDSegment.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, i), 1);
                aOutPIDSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_city,
                        aInPIDSegment.get(HL7_23.PID_11_patient_address, HL7_23.XAD_city, i), 1);
                aOutPIDSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_zip,
                        aInPIDSegment.get(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, i), 1);
                aOutPIDSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_type, "H");
            }
        }

        if (aInPIDSegment.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1).length() > 0) {
            if (!aInPIDSegment.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telecom_use, 1).equalsIgnoreCase("PRN")) {
                aOutPIDSegment.set(HL7_23.PID_13_home_phone, "");
            } else {
                aOutPIDSegment.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
                aOutPIDSegment.set(HL7_23.PID_13_home_phone, HL7_23.XTN_telecom_use, "PRN", 1);
            }
        }
        if (aInPIDSegment.get(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1).length() > 0) {
            if (!aInPIDSegment.get(HL7_23.PID_14_business_phone, HL7_23.XTN_telecom_use, 1).equalsIgnoreCase("WPN")) {
                aOutPIDSegment.set(HL7_23.PID_14_business_phone, "");
            } else {
                aOutPIDSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
                aOutPIDSegment.set(HL7_23.PID_14_business_phone, HL7_23.XTN_telecom_use, "WPN", 1);
            }
        }

        aOutPIDSegment.copy(HL7_23.PID_15_language, HL7_23.CE_ID_code);
        aOutPIDSegment.copy(HL7_23.PID_15_language, HL7_23.CE_text);
        aOutPIDSegment.copy(HL7_23.PID_16_marital_status);
        aOutPIDSegment.copy(HL7_23.PID_17_religion);
        aOutPIDSegment.set(HL7_23.PID_22_ethnic_group, aInPIDSegment.get(HL7_23.PID_10_race));
        aOutPIDSegment.copy(HL7_23.PID_23_birth_place);
        aOutPIDSegment.copy(HL7_23.PID_29_patient_death_date_time);
        if (aInPIDSegment.get(HL7_23.PID_30_patient_death_indicator).length() > 0) {
            aOutPIDSegment.set(HL7_23.PID_30_patient_death_indicator, "Y");
        } else {
            aOutPIDSegment.set(HL7_23.PID_30_patient_death_indicator, "N");
        }
        return aOutPIDSegment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Group processNK1s_FromUFD() {
        HL7Group aOutNK1Group = new HL7Group();
        int aNK1SegmentCount = mInHL7Message.countSegments("NK1");
        HL7Segment aInNK1Segment = new HL7Segment("");
        HL7Segment aOutNK1Segment = new HL7Segment("");

        for (int i = 1; i <= aNK1SegmentCount; i++) {
            aInNK1Segment = new HL7Segment(mInHL7Message.getSegment("NK1", i));
            aOutNK1Segment = new HL7Segment("NK1");
            aOutNK1Segment.linkTo(aInNK1Segment);
            aOutNK1Segment.copy(HL7_23.NK1_1_set_ID);
            aOutNK1Segment.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_family_name, 1);
            aOutNK1Segment.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_name_type, 1);
            String aCode = aInNK1Segment.get(HL7_23.NK1_3_next_of_kin_relationship, HL7_23.CE_ID_code, 1);
            CodeLookUp aLU = new CodeLookUp("NOK_Relationship.table", mEnvironment);
            String aCodeValue = aLU.getValue(aCode);
            aOutNK1Segment.set(HL7_23.NK1_3_next_of_kin_relationship, HL7_23.CE_ID_code, aCodeValue, 1);
            aOutNK1Segment.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_1, 1);
            aOutNK1Segment.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_2, 1);
            aOutNK1Segment.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_city, 1);
            aOutNK1Segment.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_zip, 1);
            aOutNK1Segment.copy(HL7_23.NK1_5_next_of_kin__phone, HL7_23.XTN_telephone_number, 1);
            aOutNK1Segment.set(HL7_23.NK1_5_next_of_kin__phone, HL7_23.XTN_telecom_use, "PRN", 1);
            aOutNK1Segment.copy(HL7_23.NK1_6_business_phone_num, HL7_23.XTN_telephone_number, 1);
            aOutNK1Segment.set(HL7_23.NK1_6_business_phone_num, HL7_23.XTN_telecom_use, "WPN", 1);
            aOutNK1Group.append(aOutNK1Segment);
        }
        return aOutNK1Group;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processPV1FromUFD(HL7Segment pMSHSegment) throws ICANException {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (pMSHSegment.get(HL7_23.MSH_9_2_trigger_event).equalsIgnoreCase("A28") ||
                pMSHSegment.get(HL7_23.MSH_9_2_trigger_event).equalsIgnoreCase("A31")) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.set(HL7_23.PV1_1_set_ID, "1");
            aOutPV1Segment.set(HL7_23.PV1_2_patient_class, "R");
            String aReferringDoc = getFromZBX("VISIT", "REF_DOC_CODE");
            if (aReferringDoc.length() > 0) {
                aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, aReferringDoc);
            }
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, 1);
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_last_name, 1);
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_first_name, 1);
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_middle_initial_or_name, 1);
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_suffix, 1);
            aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_prefix, "DR", 1);
            aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_name_type, "L", 1);
            aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_type, "DN", 1);
        } else if (!pMSHSegment.get(HL7_23.MSH_9_2_trigger_event).equalsIgnoreCase("A34")) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.set(HL7_23.PV1_1_set_ID, "1");
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_type);
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_building);
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, 1);
            aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name, 1);
            aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_first_name, 1);
            aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_middle_initial_or_name, 1);
            aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_suffix, 1);
            aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_prefix, "DR", 1);
            aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_name_type, "L", 1);
            aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_type, "DN", 1);
            String aReferringDoc = getFromZBX("VISIT", "REF_DOC_CODE");
            if (aReferringDoc.length() > 0) {
                aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, aReferringDoc);
            }
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, 1);
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_last_name, 1);
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_first_name, 1);
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_middle_initial_or_name, 1);
            //aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_suffix, 1);
            aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_prefix, "DR", 1);
            aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_name_type, "L", 1);
            aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_type, "DN", 1);
            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.set(HL7_23.PV1_18_patient_type, aInPV1Segment.get(HL7_23.PV1_2_patient_class));
            String aVisitNumber = aInPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
            char aCharArray[] = aVisitNumber.toCharArray();
            char aChar = aCharArray[0];
            if (!Character.isDigit(aChar)) {
                aVisitNumber = aVisitNumber.substring(1, aVisitNumber.length());
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNumber);
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
            }
            aOutPV1Segment.copy(HL7_23.PV1_20_financial_class, 1);
            aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
            aOutPV1Segment.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_ID_number);
        } else {
            aOutPV1Segment = aInPV1Segment;
        }
        return aOutPV1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processPV2FromUFD() {
        HL7Segment aOutPV2Segment = new HL7Segment("");
        HL7Segment aInPV2Segment = new HL7Segment(mInHL7Message.getSegment("PV2"));
        if (aInPV2Segment.getSegment().length() > 0) {
            aOutPV2Segment = new HL7Segment("PV2");
            aOutPV2Segment.linkTo(aInPV2Segment);
            aOutPV2Segment.copy(HL7_23.PV2_33_expected_surgery_date);
        }
        return aOutPV2Segment;
    }
    public HL7Group processAL1s_FromUFD() {
        HL7Group aOutAL1Group = new HL7Group("");
        HL7Segment aInAL1Segment = new HL7Segment("");
        HL7Segment aOutAL1Segment = new HL7Segment("");
        int aAL1SegmentCount = mInHL7Message.countSegments("AL1");
        for (int i = 1; i <= aAL1SegmentCount; i++) {
            aInAL1Segment = new HL7Segment(mInHL7Message.getSegment(HL7_23.AL1, i));
            if (aInAL1Segment.get(HL7_23.AL1_3_allergy, HL7_23.CE_ID_code).length() == 0 ||
                    aInAL1Segment.get(HL7_23.AL1_3_allergy, HL7_23.CE_ID_code).equalsIgnoreCase("\"\"")) {
                //do nothing
            } else {
                aOutAL1Segment = new HL7Segment("AL1");
                aOutAL1Segment.linkTo(aInAL1Segment);
                aOutAL1Segment.copy(HL7_23.AL1_1_set_ID);
                aOutAL1Segment.copy(HL7_23.AL1_3_allergy, HL7_23.CE_ID_code);
                aOutAL1Segment.copy(HL7_23.AL1_3_allergy, HL7_23.CE_text);
                aOutAL1Group.append(aOutAL1Segment);
            }
        }
        return aOutAL1Group;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processGT1FromUFD() {
        HL7Segment aInGT1Segment = new HL7Segment("");
        HL7Segment aOutGT1Segment = new HL7Segment("");
        if (!mInHL7Message.isEvent("A11")) {
            aInGT1Segment = new HL7Segment(mInHL7Message.getSegment("GT1", 1));
            if (aInGT1Segment.getSegment().length() > 0) {
                aOutGT1Segment = new HL7Segment("GT1");
                aOutGT1Segment.linkTo(aInGT1Segment);
                aOutGT1Segment.copy(HL7_23.GT1_1_set_ID);
                aOutGT1Segment.copy(HL7_23.GT1_2_guarantor_number, 1);
                aOutGT1Segment.copy(HL7_23.GT1_3_guarantor_name, 1);
                aOutGT1Segment.copy(HL7_23.GT1_4_guarantor_spouse_name, 1);
                aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, 1);
                aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, 1);
                aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, 1);
                aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, 1);
                aOutGT1Segment.copy(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, 1);
                aOutGT1Segment.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telecom_use, "PRN", 1);
                aOutGT1Segment.copy(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telephone_number, 1);
                aOutGT1Segment.set(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telecom_use, "WPN", 1);
                aOutGT1Segment.copy(HL7_23.GT1_10_guarantor_type);
            }
        }
        return aOutGT1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processIN1FromUFD(HL7Segment pMSHSegment) {
        HL7Segment aOutIN1Segment = new HL7Segment("");
        if (pMSHSegment.get(HL7_23.MSH_9_2_trigger_event).equalsIgnoreCase("A08")) {
            HL7Segment aInIN1Segment = new HL7Segment(mInHL7Message.getSegment("IN1", 1));
            if (aInIN1Segment.getSegment().length() > 0) {
                aOutIN1Segment = new HL7Segment("IN1");
                aOutIN1Segment.linkTo(aInIN1Segment);
                aOutIN1Segment.set(HL7_23.IN1_1_set_ID, "1");
                aOutIN1Segment.copy(HL7_23.IN1_2_insurance_plan_ID, 1);
            }
        }
        return aOutIN1Segment;
    }
}
