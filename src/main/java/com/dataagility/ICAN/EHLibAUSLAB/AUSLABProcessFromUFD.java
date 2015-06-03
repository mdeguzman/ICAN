/*
 * AUSLABProcessFromUFD.java
 *
 * Created on 11 October 2005, 17:04
 *
 */

package EHLibAUSLAB;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * AUSLABProcessFromUFD contains the methods required to build a AUSLAB message
 * from a UFD HL7 message structure
 * @author Ray Fillingham and Norman Soh
 */
public class AUSLABProcessFromUFD extends ProcessSegmentsFromUFD {
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
     * This constructor creates a new instance of AUSLABProcessFromUFD passing a HL7 UFD
     * message structure
     * @param pHL7Message HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public AUSLABProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
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
        String aAUSLABMessageArray[] = {k.NULL, k.NULL, k.NULL};
        HL7Message aOutHL7Message = new HL7Message("");
        mInHL7Message = new HL7Message(mHL7Message, k.TRIM_LAST);
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (aInPV1Segment.get(HL7_23.PV1_45_discharge_date_time).length() > 0 && mInHL7Message.isEvent("A03") ||
                aInPV1Segment.get(HL7_23.PV1_45_discharge_date_time).length() == 0 && !mInHL7Message.isEvent("A03")) {
            if (mInHL7Message.isEvent("A01, A02, A03, A04, A08, A11, A12, A13, A21, A22, A28, A31")) {
                HL7Segment aMSHSegment = processMSHFromUFD();
                HL7Segment aEVNSegment = processEVNFromUFD();
                HL7Segment aPIDSegment = processPIDFromUFD(1);
                HL7Group aNK1Group = processNK1s_FromUFD();
                HL7Segment aPV1Segment = processPV1FromUFD();
                HL7Segment aPV2Segment = processPV2FromUFD();
                HL7Segment aIN1Segment = processIN1FromUFD();

                aOutHL7Message.append(aMSHSegment);
                aOutHL7Message.append(aEVNSegment);
                aOutHL7Message.append(aPIDSegment);
                aOutHL7Message.append(aNK1Group);
                aOutHL7Message.append(aPV1Segment);
                aOutHL7Message.append(aPV2Segment);
                aOutHL7Message.append(aIN1Segment);

                aAUSLABMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
                aAUSLABMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
                aAUSLABMessageArray[2] = aOutHL7Message.getMessage();
            } else if (mInHL7Message.isEvent("A34")) {
                HL7Segment aMSHSegment = processMSHFromUFD();
                HL7Segment aEVNSegment = processEVNFromUFD();
                HL7Segment aPIDSegment = processPIDFromUFD(1);
                HL7Segment aMRGSegment = processMRGFromUFD();

                aOutHL7Message.append(aMSHSegment);
                aOutHL7Message.append(aEVNSegment);
                aOutHL7Message.append(aPIDSegment);
                aOutHL7Message.append(aMRGSegment);

                aAUSLABMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
                aAUSLABMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
                aAUSLABMessageArray[2] = aOutHL7Message.getMessage();
            }
//                else if (mInHL7Message.isEvent("A17")) {
//                HL7Segment aMSHSegment = processA17MSHFromUFD();
//                HL7Segment aEVNSegment = processEVNFromUFD();
//                HL7Segment aPID1Segment = processPIDFromUFD(1);
//                HL7Segment aPV11Segment = processA17PV1FromUFD(1);
//                HL7Segment aPV21Segment = processA17PV2FromUFD(1);
//                HL7Segment aPID2Segment = processPIDFromUFD(2);
//                HL7Segment aPV12Segment = processA17PV1FromUFD(2);
//                HL7Segment aPV22Segment = processA17PV2FromUFD(2);
//
//                aOutHL7Message.append(aMSHSegment);
//                aOutHL7Message.append(aEVNSegment);
//                aOutHL7Message.append(aPID1Segment);
//                aOutHL7Message.append(aPV11Segment);
//                aOutHL7Message.append(aPV21Segment);
//                aOutHL7Message.append(aPID2Segment);
//                aOutHL7Message.append(aPV12Segment);
//                aOutHL7Message.append(aPV22Segment);
//
//                aAUSLABMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
//                aAUSLABMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
//                aAUSLABMessageArray[2] = aOutHL7Message.getMessage();
//            }
        }
        return aAUSLABMessageArray;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processMSHFromUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        mHospitalPrefix = aInMSHSegment.get(HL7_23.MSH_4_sending_facility).substring(0, 1);
        mSReg2 = aInMSHSegment.get(HL7_23.MSH_4_sending_facility);
        mSReg1 = "\\R\\" + mSReg2;
        aOutMSHSegment.linkTo(aInMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        String aSendingApp = aInMSHSegment.get(HL7_23.MSH_3_sending_application);
        int aPeriodPos = aSendingApp.indexOf(".");
        if (aPeriodPos >= 0) {
            aSendingApp = aSendingApp.substring(0, aPeriodPos);
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, aSendingApp);
        } else {
            aOutMSHSegment.copy(HL7_23.MSH_3_sending_application);
        }
        aOutMSHSegment.copy(HL7_23.MSH_4_sending_facility);
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "AUSLAB");
        aOutMSHSegment.copy(HL7_23.MSH_6_receiving_facility);
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
    public HL7Segment processPIDFromUFD(int pNum) {
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID", pNum));
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.copy(HL7_23.PID_1_set_ID);

        int aPID3FieldCount = aInPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3FieldCount; i++) {
            if (aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("VA") &&
                    aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i).length() > 0) {
                aOutPIDSegment.set(HL7_23.PID_2_patient_ID_external, HL7_23.CX_ID_number, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i));
                aOutPIDSegment.set(HL7_23.PID_2_patient_ID_external, HL7_23.CX_ID_type_code, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i));
                aOutPIDSegment.set(HL7_23.PID_2_patient_ID_external, HL7_23.CX_assigning_fac, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, i), 1);
                aOutPIDSegment.set(HL7_23.PID_2_patient_ID_external, HL7_23.CX_assigning_authority, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, i), 1);
                aOutPIDSegment.set(HL7_23.PID_27_veterans_military_status, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i));
            }
            if (aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("MR") ||
                    aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("PI") ||
                    aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, mHospitalPrefix.concat(aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i)), 1);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, i), 1);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, i), 1);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "MR", 1);
            }
            if (aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("PEN") &&
                    aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i).length() > 0) {
                aOutPIDSegment.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i));
                aOutPIDSegment.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_type_code, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i));
                aOutPIDSegment.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_assigning_fac, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, i), 1);
                aOutPIDSegment.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_assigning_authority, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, i), 1);
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
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_suffix,
                        aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_suffix, i), 1);
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix,
                        aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix, i), 1);
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_degree,
                        aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_degree, i), 1);
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type,
                        aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, i), 1);
                i = aPID5FieldCount + 1;
            }
        }

        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);

        int aPID9FieldCount = aInPIDSegment.countRepeatFields(HL7_23.PID_9_patient_alias);
        int j = 1;
        for (int i = 1; i <= aPID9FieldCount; i++) {
            if (aInPIDSegment.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_name_type, i).equalsIgnoreCase("A") &&
                    aInPIDSegment.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_family_name, i).length() > 0) {
                aOutPIDSegment.set(HL7_23.PID_9_patient_alias, HL7_23.XPN_family_name,
                        aInPIDSegment.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_family_name, i), j);
                aOutPIDSegment.set(HL7_23.PID_9_patient_alias, HL7_23.XPN_given_name,
                        aInPIDSegment.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_given_name, i), j);
                aOutPIDSegment.set(HL7_23.PID_9_patient_alias, HL7_23.XPN_middle_name,
                        aInPIDSegment.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_middle_name, i), j);
                aOutPIDSegment.set(HL7_23.PID_9_patient_alias, HL7_23.XPN_suffix,
                        aInPIDSegment.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_suffix, i), j);
                aOutPIDSegment.set(HL7_23.PID_9_patient_alias, HL7_23.XPN_prefix,
                        aInPIDSegment.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_prefix, i), j);
                aOutPIDSegment.set(HL7_23.PID_9_patient_alias, HL7_23.XPN_degree,
                        aInPIDSegment.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_degree, i), j);
                aOutPIDSegment.set(HL7_23.PID_9_patient_alias, HL7_23.XPN_name_type,
                        aInPIDSegment.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_name_type, i), j);
                j++;
            }
        }

        aOutPIDSegment.copy(HL7_23.PID_10_race);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_type, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_geographic_designation, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_county_parish, 1);
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone, 1);
        String aHomePhone = aOutPIDSegment.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number);
        aOutPIDSegment.set(HL7_23.PID_13_home_phone, HL7_23.XTN_phone_number, aHomePhone);
        aOutPIDSegment.copy(HL7_23.PID_14_business_phone, 1);
        String aWorkPhone = aOutPIDSegment.get(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number);
        aOutPIDSegment.set(HL7_23.PID_14_business_phone, HL7_23.XTN_phone_number, aWorkPhone);

        aOutPIDSegment.copy(HL7_23.PID_15_language, HL7_23.CE_ID_code);
        aOutPIDSegment.copy(HL7_23.PID_16_marital_status);
        aOutPIDSegment.copy(HL7_23.PID_17_religion);
        aOutPIDSegment.copy(HL7_23.PID_18_account_number, HL7_23.CX_ID_number);
        aOutPIDSegment.copy(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority);
        aOutPIDSegment.copy(HL7_23.PID_18_account_number, HL7_23.CX_ID_type_code);
        aOutPIDSegment.copy(HL7_23.PID_18_account_number, HL7_23.CX_assigning_fac);

        int aCharCount = 0;
        String aPID19SSNNum = aInPIDSegment.get(HL7_23.PID_19_SSN_number);
        if (aPID19SSNNum.length() > 0) {
            while (aPID19SSNNum.charAt(aCharCount++) == ' ');
            aPID19SSNNum = aPID19SSNNum.substring(aCharCount - 1);
        }
        if (aPID19SSNNum.length() > 3) {
            aOutPIDSegment.set(HL7_23.PID_19_SSN_number, aPID19SSNNum);
        } else {
            aOutPIDSegment.set(HL7_23.PID_19_SSN_number, "            " + aPID19SSNNum);
        }

        aOutPIDSegment.copy(HL7_23.PID_21_mothers_ID, HL7_23.CX_ID_number, 1);
        String aEthnicGroup = aInPIDSegment.get(HL7_23.PID_23_birth_place);
        aOutPIDSegment.set(HL7_23.PID_22_ethnic_group, aEthnicGroup);
        //aOutPIDSegment.copy(HL7_23.PID_27_veterans_military_status);
        aOutPIDSegment.copy(HL7_23.PID_29_patient_death_date_time);
        aOutPIDSegment.copy(HL7_23.PID_30_patient_death_indicator);

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
            aOutNK1Segment.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_given_name, 1);
            aOutNK1Segment.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_middle_name, 1);
            aOutNK1Segment.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_suffix, 1);
            aOutNK1Segment.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_prefix, 1);
            aOutNK1Segment.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_degree, 1);
            aOutNK1Segment.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_name_type, 1);
            aOutNK1Segment.copy(HL7_23.NK1_3_next_of_kin_relationship, 1);
            aOutNK1Segment.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_1, 1);
            aOutNK1Segment.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_2, 1);
            aOutNK1Segment.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_city, 1);
            aOutNK1Segment.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_state_or_province, 1);
            aOutNK1Segment.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_zip, 1);
            aOutNK1Segment.copy(HL7_23.NK1_5_next_of_kin__phone, 1);
            String aHomePhone = aInNK1Segment.get(HL7_23.NK1_5_next_of_kin__phone, HL7_23.XTN_telephone_number);
            aOutNK1Segment.set(HL7_23.NK1_5_next_of_kin__phone, HL7_23.XTN_phone_number, aHomePhone);
            aOutNK1Segment.copy(HL7_23.NK1_6_business_phone_num, 1);
            String aWorkPhone = aInNK1Segment.get(HL7_23.NK1_6_business_phone_num, HL7_23.XTN_telephone_number);
            aOutNK1Segment.set(HL7_23.NK1_6_business_phone_num, HL7_23.XTN_phone_number, aWorkPhone);
            aOutNK1Segment.copy(HL7_23.NK1_7_contact_role, 1);
            aOutNK1Group.append(aOutNK1Segment);
        }

        return aOutNK1Group;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processPV1FromUFD() throws ICANException {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);

            if (mInHL7Message.isEvent("A01, A02, A03, A04, A08, A11, A12, A13, A21, A22" )) {
                String aAssPatientLocPOC = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                if (aAssPatientLocPOC.length() > 0) {
                    CodeLookUp aLU = new CodeLookUp("CSC_AUSLAB_WARD.table", mEnvironment);
                    String aCode = mSReg2.concat("-").concat(aAssPatientLocPOC);
                    String aCodeValue = aLU.getValue(aCode);
                    aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aCodeValue);
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID);
                }
                aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);

                if (aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location).length() > 0) {
                    CodeLookUp aLU = new CodeLookUp("CSC_AUSLAB_WARD.table", mEnvironment);
                    String aPriorPatientLocPOC = aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
                    String aCode = mSReg2.concat("-").concat(aPriorPatientLocPOC);
                    String aCodeValue = aLU.getValue(aCode);
                    aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu, aCodeValue);
                    aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
                    aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_facility_ID);
                }

                int aPV1_7AttendingDocCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_7_attending_doctor);
                for (int i = 1; i <= aPV1_7AttendingDocCount; i++) {
                    if (aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, i).length() > 0) {
                        String aAttendingDocIDNum = aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, i).concat(mSReg1);
                        aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, aAttendingDocIDNum, i);
                        aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name, i);
                        aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_first_name, i);
                        aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_middle_initial_or_name, i);
                        aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_suffix, i);
                        aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_prefix, i);
                        aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_degree, i);
                    }
                }

                int aPV1_8ReferDocCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_8_referring_doctor);
                for (int i = 1; i <= aPV1_8ReferDocCount; i++) {
                    if (aInPV1Segment.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, i).length() > 0) {
                        String aReferDocIDNum = aInPV1Segment.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, i).concat(mSReg1);
                        aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, aReferDocIDNum, i);
                        aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_last_name, i);
                        aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_first_name, i);
                        aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_middle_initial_or_name, i);
                        aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_suffix, i);
                        aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_prefix, i);
                        aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_degree, i);
                    }
                }

                int aPV1_9ConsDocCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_9_consulting_doctor);
                for (int i = 1; i <= aPV1_9ConsDocCount; i++) {
                    if (aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i).length() > 0) {
                        if (aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i).length() == 0) {
                            //send error
                        } else {
                            String aConsDocIDNum = aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i).concat(mSReg1);
                            aOutPV1Segment.set(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, aConsDocIDNum, i);
                            aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i);
                            aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_first_name, i);
                            aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_middle_initial_or_name, i);
                            aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_suffix, i);
                            aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_prefix, i);
                            aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_degree, i);
                        }
                    }
                }

                if (aInPV1Segment.get(HL7_23.PV1_10_hospital_service).length() > 0) {
                    String aHospService = aInPV1Segment.get(HL7_23.PV1_10_hospital_service).concat(mSReg1);
                    aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, aHospService);
                } else {
                    aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
                }
                aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
                aOutPV1Segment.set(HL7_23.PV1_18_patient_type, aOutPV1Segment.get(HL7_23.PV1_2_patient_class));
                //String aVisitNum = mHospitalPrefix.concat(aOutPV1Segment.get(HL7_23.PV1_2_patient_class)).concat(aInPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number));
                String aVisitNum = mHospitalPrefix.concat(aInPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number));
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNum);
                CodeLookUp aLU = new CodeLookUp("CSC_AUSLAB_PAY_CLASS.table", mEnvironment);
                String aCode = mSReg2.concat("-").concat(aOutPV1Segment.get(HL7_23.PV1_2_patient_class)).concat("-").concat(aInPV1Segment.get(HL7_23.PV1_20_financial_class, 1));
                String aCodeValue = aLU.getValue(aCode);
                aOutPV1Segment.set(HL7_23.PV1_20_financial_class, aCodeValue);
                //aOutPV1Segment.copy(HL7_23.PV1_20_financial_class);
                aOutPV1Segment.copy(HL7_23.PV1_36_discharge_disposition);
                aOutPV1Segment.copy(HL7_23.PV1_37_discharged_to_location);
                aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
                aOutPV1Segment.copy(HL7_23.PV1_45_discharge_date_time);
                aOutPV1Segment.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_ID_number);
                aOutPV1Segment.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_assigning_authority);
                aOutPV1Segment.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_ID_type_code);
                aOutPV1Segment.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_assigning_fac);
            } else if (mInHL7Message.isEvent("A28, A31")) {
                aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, 1);
                CodeLookUp aLU = new CodeLookUp("CSC_AUSLAB_PAY_CLASS.table", mEnvironment);
                String aCode = mSReg2 + "-" + aOutPV1Segment.get(HL7_23.PV1_2_patient_class) + "-" + getFromZBX("PMI", "FINANCIAL_CLASS_REG");
                String aCodeValue = aLU.getValue(aCode);
                aOutPV1Segment.set(HL7_23.PV1_20_financial_class, aCodeValue);
            }
        }
        //Remove trailing spaces within subfields
        String aTempStr = aOutPV1Segment.getSegment();
        aTempStr = aTempStr.replaceAll(" \\^", "^");
        aOutPV1Segment.setSegment(aTempStr);
        return aOutPV1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processPV2FromUFD() {
        HL7Segment aOutPV2Segment = new HL7Segment("");
        if (mInHL7Message.isEvent("A28, A31")) {
            //do nothing
        } else {
            HL7Segment aInPV2Segment = new HL7Segment(mInHL7Message.getSegment("PV2"));
            if (aInPV2Segment.getSegment().length() > 0) {
                aOutPV2Segment = new HL7Segment("PV2");
                aOutPV2Segment.linkTo(aInPV2Segment);
                //String aAdmitReason = aInPV2Segment.get(HL7_23.PV2_3_admit_reason);
                //aOutPV2Segment.set(HL7_23.PV2_3_admit_reason, HL7_23.CE_text, aAdmitReason);
                aOutPV2Segment.copy(HL7_23.PV2_3_admit_reason);
                aOutPV2Segment.copy(HL7_23.PV2_9_expected_discharge_date);
                String aVisitPubCode = aInPV2Segment.get(HL7_23.PV2_21_visit_publicity_code);
                aOutPV2Segment.set(HL7_23.PV2_22_visit_protection_indic, aVisitPubCode);
            }
        }
        return aOutPV2Segment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processIN1FromUFD() {
        HL7Segment aOutIN1Segment = new HL7Segment("");
        HL7Segment aInIN1Segment = new HL7Segment(mInHL7Message.getSegment("IN1", 1));
        if (aInIN1Segment.getSegment().length() > 0) {
            aOutIN1Segment = new HL7Segment("IN1");
            aOutIN1Segment.linkTo(aInIN1Segment);
            aOutIN1Segment.copy(HL7_23.IN1_1_set_ID);
            aOutIN1Segment.copy(HL7_23.IN1_2_insurance_plan_ID);
            CodeLookUp aLU = new CodeLookUp("CSC_AUSLAB_FUND.table", mEnvironment);
            String aCode = aInIN1Segment.get(HL7_23.IN1_3_insurance_co_ID, 1);
            String aCodeValue = aLU.getValue(aCode);
            aOutIN1Segment.set(HL7_23.IN1_3_insurance_co_ID, aCodeValue, 1);
            aOutIN1Segment.copy(HL7_23.IN1_4_insurance_co_name, 1);
            aOutIN1Segment.copy(HL7_23.IN1_5_insurance_co_address, 1);
            aOutIN1Segment.copy(HL7_23.IN1_36_policy_number);
        }
        return aOutIN1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processMRGFromUFD() {
        HL7Segment aOutMRGSegment = new HL7Segment("MRG");
        HL7Segment aInMRGSegment = new HL7Segment(mInHL7Message.getSegment("MRG"));

        int aMRG1PriorPatientIDCount = aInMRGSegment.countRepeatFields(HL7_23.MRG_1_prior_patient_ID_internal);
        for (int i = 1; i <= aMRG1PriorPatientIDCount; i++) {
            String aID = mHospitalPrefix.concat(aInMRGSegment.get(HL7_23.MRG_1_prior_patient_ID_internal, i));
            aOutMRGSegment.set(HL7_23.MRG_1_prior_patient_ID_internal, aID, i);
        }
        return aOutMRGSegment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processA17MSHFromUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        mHospitalPrefix = aInMSHSegment.get(HL7_23.MSH_4_sending_facility).substring(0, 1);
        mSReg2 = aInMSHSegment.get(HL7_23.MSH_4_sending_facility);
        mSReg1 = "~" + mSReg2;
        aOutMSHSegment.linkTo(aInMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        String aSendingApp = aInMSHSegment.get(HL7_23.MSH_3_sending_application);
        int aPeriodPos = aSendingApp.indexOf(".");
        if (aPeriodPos >= 0) {
            aSendingApp = aSendingApp.substring(0, aPeriodPos);
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, aSendingApp);
        } else {
            aOutMSHSegment.copy(HL7_23.MSH_3_sending_application);
        }
        aOutMSHSegment.copy(HL7_23.MSH_4_sending_facility);
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "AUSLAB");
        aOutMSHSegment.copy(HL7_23.MSH_6_receiving_facility);
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        aOutMSHSegment.copy(HL7_23.MSH_9_message_type);
        aOutMSHSegment.copy(HL7_23.MSH_10_message_control_ID);
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");
        return aOutMSHSegment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @param pNum
     * @return
     */
    public HL7Segment processA17PV1FromUFD(int pNum) {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1", pNum));
        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);
            String aAssPatientLocPOC = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu).concat(mSReg1);
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aAssPatientLocPOC);
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            if (mHospitalPrefix.equalsIgnoreCase("M")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "MAR");
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID);
            }
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_status);
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            String aPriorPatientLocPOC = aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu).concat(mSReg1);
            aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu, aPriorPatientLocPOC);
            aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
            if (mHospitalPrefix.equalsIgnoreCase("M")) {
                aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_facility_ID, "MAR");
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_facility_ID);
            }
            aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_status);

            int aPV1_7AttendingDocCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_7_attending_doctor);
            for (int i = 1; i <= aPV1_7AttendingDocCount; i++) {
                if (aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, i).length() > 0 &&
                        aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name, i).length() > 0) {
                    String aAttendingDocIDNum = aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, i).concat(mSReg1);
                    aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, aAttendingDocIDNum, i);
                    aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_first_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_middle_initial_or_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_suffix, i);
                    aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_prefix, i);
                    aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_degree, i);
                }
            }

            int aPV1_8ReferDocCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_8_referring_doctor);
            for (int i = 1; i <= aPV1_8ReferDocCount; i++) {
                if (aInPV1Segment.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, i).length() > 0 &&
                        aInPV1Segment.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_last_name, i).length() > 0) {
                    String aReferDocIDNum = aInPV1Segment.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, i).concat(mSReg1);
                    aOutPV1Segment.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, aReferDocIDNum, i);
                    aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_last_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_first_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_middle_initial_or_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_suffix, i);
                    aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_prefix, i);
                    aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_degree, i);
                }
            }

            int aPV1_9ConsDocCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_9_consulting_doctor);
            for (int i = 1; i <= aPV1_9ConsDocCount; i++) {
                if (aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i).length() > 0) {
                    if (aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i).length() == 0) {
                        //send error
                    } else {
                        String aConsDocIDNum = aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i).concat(mSReg1);
                        aOutPV1Segment.set(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, aConsDocIDNum, i);
                        aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i);
                        aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_first_name, i);
                        aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_middle_initial_or_name, i);
                        aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_suffix, i);
                        aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_prefix, i);
                        aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_degree, i);
                    }
                }
            }

            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.copy(HL7_23.PV1_15_ambulatory_status, 1);
            //String aVisitNum = mHospitalPrefix.concat(aOutPV1Segment.get(HL7_23.PV1_2_patient_class)).concat(aInPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number));
            String aVisitNum = mHospitalPrefix.concat(aInPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number));
            aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNum);
//            CodeLookUp aLU = new CodeLookUp("CSC_AUSLAB_PAY_CLASS.table", mEnvironment);
//            String aCode = mSReg2.concat("-").concat(aOutPV1Segment.get(HL7_23.PV1_2_patient_class)).concat("-").concat(aInPV1Segment.get(HL7_23.PV1_20_financial_class, 1));
//            String aCodeValue = aLU.getValue(aCode);
//            aOutPV1Segment.set(HL7_23.PV1_20_financial_class, aCodeValue);
            aOutPV1Segment.copy(HL7_23.PV1_20_financial_class);
            aOutPV1Segment.copy(HL7_23.PV1_36_discharge_disposition);
            aOutPV1Segment.copy(HL7_23.PV1_37_discharged_to_location);
            aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
            aOutPV1Segment.copy(HL7_23.PV1_45_discharge_date_time);
            aOutPV1Segment.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_ID_number);
            aOutPV1Segment.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_assigning_authority);
            aOutPV1Segment.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_ID_type_code);
            aOutPV1Segment.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_assigning_fac);
        }
        return aOutPV1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @param pNum
     * @return
     */
    public HL7Segment processA17PV2FromUFD(int pNum) {
        HL7Segment aOutPV2Segment = new HL7Segment("");
        HL7Segment aInPV2Segment = new HL7Segment(mInHL7Message.getSegment("PV2", pNum));
        if (aInPV2Segment.getSegment().length() > 0) {
            aOutPV2Segment = new HL7Segment("PV2");
            aOutPV2Segment.linkTo(aInPV2Segment);
            aOutPV2Segment.copy(HL7_23.PV2_3_admit_reason);
            aOutPV2Segment.copy(HL7_23.PV2_9_expected_discharge_date);
            String aVisitPubCode = aInPV2Segment.get(HL7_23.PV2_21_visit_publicity_code);
            aOutPV2Segment.set(HL7_23.PV2_22_visit_protection_indic, aVisitPubCode);
        }
        return aOutPV2Segment;
    }
}
