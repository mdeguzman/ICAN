/*
 * TRENDCAREProcessFromUFD.java
 *
 * Created on 11 October 2005, 17:04
 *
 */

package EHLibTRENDCARE;

import BHLibClasses.*;
import java.text.*;

/**
 * TRENDCAREProcessFromUFD contains the methods required to build a TRENDCARE message
 * from a UFD HL7 message structure
 * @author Ray Fillingham and Norman Soh
 */
public class TRENDCAREProcessFromUFD extends ProcessSegmentsFromUFD {
    /**
     * Constant class
     */
    BHConstants k = new BHConstants();
    /**
     * Class wide HL7 message class object
     */
    public HL7Message mInHL7Message;
    /**
     * Class wide Hospital prefix variable
     */
    public String mHospitalPrefix = "";
    public int mStrLen = 0;
    /**
     * Class wide variable
     */
    public int mSReg1 = 0;
    /**
     * Class wide variable
     */
    public String mSReg2 = "";
    public String mSReg3 = "";
    //--------------------------------------------------------------------------
    /**
     * This constructor creates a new instance of TRENDCAREProcessFromUFD passing a HL7 UFD
     * message structure
     * @param pHL7Message HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public TRENDCAREProcessFromUFD(String pHL7Message) throws ICANException {
        super(pHL7Message);
        mVersion = "A";
        mHL7Message = pHL7Message;
    }
    //--------------------------------------------------------------------------
    /**
     * This method contains the methods required to build a AUSLAB HL7 message
     * @return HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public String[] processMessage() throws ICANException {

        String aTRENDCAREMessageArray[] = {k.NULL, k.NULL, k.NULL};
        HL7Message aOutHL7Message = new HL7Message("");
        mInHL7Message = new HL7Message(mHL7Message, k.TRIM_LAST);

        if (mInHL7Message.isEvent("A01, A02, A03, A08, A11, A12, A13, A21, A22")) {
            HL7Segment aMSHSegment = processMSHFromUFD();
            HL7Segment aEVNSegment = processEVNFromUFD();
            HL7Segment aPIDSegment = processPIDFromUFD(aMSHSegment);
            HL7Group aNK1Group = processNK1s_FromUFD();
            HL7Segment aPV1Segment = processPV1FromUFD(aMSHSegment);
            HL7Segment aPV2Segment = processPV2FromUFD();
            HL7Segment aGT1Segment = processGT1FromUFD();

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aEVNSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aNK1Group);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aPV2Segment);
            aOutHL7Message.append(aGT1Segment);

            aTRENDCAREMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aTRENDCAREMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aTRENDCAREMessageArray[2] = aOutHL7Message.getMessage();
        }
        return aTRENDCAREMessageArray;
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
        if (mHospitalPrefix.equalsIgnoreCase("A")) {
            mHospitalPrefix = "W";
        }
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
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "TRENDCARE");
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
    public HL7Segment processPIDFromUFD(HL7Segment pMSHSegment) {
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.copy(HL7_23.PID_1_set_ID);

        int aPID3FieldCount = aInPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3FieldCount; i++) {
            if (aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("VA") &&
                    aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i).length() > 0) {
                aOutPIDSegment.set(HL7_23.PID_27_veterans_military_status, HL7_23.CE_ID_code, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i));
                //aOutPIDSegment.set(HL7_23.PID_2_patient_ID_external, HL7_23.CX_ID_type_code, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i));
            }
            if (aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("MR") ||
                    aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("PI") ||
                    aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i), 1);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, i), 1);
            }
            if (aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("PEN") &&
                    aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i).length() > 0) {
                aOutPIDSegment.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i));
                aOutPIDSegment.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_type_code, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i));
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
    public HL7Segment processPV1FromUFD(HL7Segment pMSHSegment) {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.set(HL7_23.PV1_2_patient_class, "I");

            if (mHospitalPrefix.equalsIgnoreCase("M")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "MAR");
                String aAssPatientLocPOC = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                if (aAssPatientLocPOC.indexOf("-") >= 0) {
                    mStrLen = aAssPatientLocPOC.length();
                    mSReg1 = aAssPatientLocPOC.indexOf("-");
                    mSReg2 = aAssPatientLocPOC.substring(0, mSReg1);
                    mSReg3 = aAssPatientLocPOC.substring((mSReg1 + 1), mStrLen);
                    aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, mSReg2);
                    String aAssPatientLocBed = mSReg3.concat("-").concat(aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed));
                    aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aAssPatientLocBed);
                } else {
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                    String aAssPatientLocBed = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                    if (aAssPatientLocBed.length() == 0 || aAssPatientLocBed.equalsIgnoreCase("\"\"")) {
                        aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                    } else if (aAssPatientLocBed.length() <= 2) {
                        aAssPatientLocBed = aAssPatientLocBed.replaceAll("[^0-9]", "");
                        NumberFormat formatter = new DecimalFormat("00");
                        aAssPatientLocBed = formatter.format(Integer.parseInt(aAssPatientLocBed));
                        aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aAssPatientLocBed);
                    } else {
                        //aAssPatientLocBed is > 2 length.  Get first 2 chars
                        aAssPatientLocBed = aAssPatientLocBed.substring(0, 2);
                        aAssPatientLocBed = aAssPatientLocBed.replaceAll("[^0-9]", "");
                        NumberFormat formatter = new DecimalFormat("00");
                        aAssPatientLocBed = formatter.format(Integer.parseInt(aAssPatientLocBed));
                        aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aAssPatientLocBed);
                    }
                }
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                String aAssPatientLocBed = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                if (aAssPatientLocBed.length() == 0 || aAssPatientLocBed.equalsIgnoreCase("\"\"")) {
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                } else if (aAssPatientLocBed.length() <= 2) {
                    //aAssPatientLocBed = aAssPatientLocBed.replaceAll("[^0-9]", "");
                    //NumberFormat formatter = new DecimalFormat("00");
                    //aAssPatientLocBed = formatter.format(Integer.parseInt(aAssPatientLocBed));
                    //aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aAssPatientLocBed);
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                } else {
                    //aAssPatientLocBed is > 2 length.  Get first 2 chars
                    //aAssPatientLocBed = aAssPatientLocBed.substring(0, 2);
                    //aAssPatientLocBed = aAssPatientLocBed.replaceAll("[^0-9]", "");
                    //NumberFormat formatter = new DecimalFormat("00");
                    //aAssPatientLocBed = formatter.format(Integer.parseInt(aAssPatientLocBed));
                    //aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aAssPatientLocBed);
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                }
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID);
            }

            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_status);
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);

            if (mHospitalPrefix.equalsIgnoreCase("M")) {
                aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_facility_ID, "MAR");
                String aPriorPatientLocPOC = aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
                if (aPriorPatientLocPOC.indexOf("-") >= 0) {
                    mStrLen = aPriorPatientLocPOC.length();
                    mSReg1 = aPriorPatientLocPOC.indexOf("-");
                    mSReg2 = aPriorPatientLocPOC.substring(0, mSReg1);
                    mSReg3 = aPriorPatientLocPOC.substring((mSReg1 + 1), mStrLen);
                    aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu, mSReg2);
                    String aPriorPatientLocBed = mSReg3.concat("-").concat(aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed));
                    aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed, aPriorPatientLocBed);
                } else {
                    aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
                    String aPriorPatientLocBed = aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
                    if (aPriorPatientLocBed.length() == 0 || aPriorPatientLocBed.equalsIgnoreCase("\"\"")) {
                        aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
                    } else {
                        aPriorPatientLocBed = aPriorPatientLocBed.replaceAll("[^0-9]", "");
                        NumberFormat formatter = new DecimalFormat("00");
                        aPriorPatientLocBed = formatter.format(Integer.parseInt(aPriorPatientLocBed));
                        aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed, aPriorPatientLocBed);
                    }
                }
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
                String aPriorPatientLocBed = aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
                if (aPriorPatientLocBed.length() == 0 || aPriorPatientLocBed.equalsIgnoreCase("\"\"")) {
                    aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
                } else {
                    //aPriorPatientLocBed = aPriorPatientLocBed.replaceAll("[^0-9]", "");
                    //NumberFormat formatter = new DecimalFormat("00");
                    //aPriorPatientLocBed = formatter.format(Integer.parseInt(aPriorPatientLocBed));
                    //aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed, aPriorPatientLocBed);
                    aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
                }
                aOutPV1Segment.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_facility_ID);
            }

            int aPV1_7AttendingDocCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_7_attending_doctor);
            for (int i = 1; i <= aPV1_7AttendingDocCount; i++) {
                if (aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, i).length() > 0 &&
                        aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name, i).length() > 0) {
                    String aAttendingDocIDNum = mHospitalPrefix.concat(aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, i));
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
                    String aReferDocIDNum = mHospitalPrefix.concat(aInPV1Segment.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, i));
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
                if (aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i).length() > 0 &&
                        aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i).length() > 0) {
                    String aConsDocIDNum = mHospitalPrefix.concat(aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i));
                    aOutPV1Segment.set(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, aConsDocIDNum, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_first_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_middle_initial_or_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_suffix, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_prefix, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_degree, i);
                }
            }

            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.set(HL7_23.PV1_18_patient_type, aOutPV1Segment.get(HL7_23.PV1_2_patient_class));
            //String aVisitNum = aOutPV1Segment.get(HL7_23.PV1_2_patient_class).concat(aInPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number));
            //aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNum);
            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
            aOutPV1Segment.copy(HL7_23.PV1_20_financial_class, 1);
            aOutPV1Segment.copy(HL7_23.PV1_21_charge_price_indicator);
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
     * @return
     */
    public HL7Segment processPV2FromUFD() {
        HL7Segment aOutPV2Segment = new HL7Segment("");
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
        return aOutPV2Segment;
    }
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processGT1FromUFD() {
        HL7Segment aInGT1Segment = new HL7Segment("");
        HL7Segment aOutGT1Segment = new HL7Segment("");
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
            aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, 1);
            aOutGT1Segment.copy(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, 1);
            aOutGT1Segment.copy(HL7_23.GT1_6_guarantor_phone_home, 1);
            aOutGT1Segment.copy(HL7_23.GT1_7_guarantor_phone_business, 1);
            aOutGT1Segment.copy(HL7_23.GT1_10_guarantor_type);
        }

        return aOutGT1Segment;
    }
}
