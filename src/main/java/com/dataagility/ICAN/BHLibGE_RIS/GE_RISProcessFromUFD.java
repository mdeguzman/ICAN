/*
 * GE_RISProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 */

package com.dataagility.ICAN.BHLibGE_RIS;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * Constructor
 * @author sohn
 */
public class GE_RISProcessFromUFD extends ProcessSegmentsFromUFD {
    /**
     * Class wide Home phone variable
     */
    String cHomePhone = "";
    /**
     * Class wide Pension number variable
     */
    String cPensionNumber = "";
    /**
     * Class wide Medicare number variable
     */
    String cMedicareNumber = "";
    /**
     * Class wide Attending doctor variable
     */
    String cAttendingDr = "";
    /**
     * Class wide DVA number variable
     */
    String cDVANumber = "";
    String cPatientClass = "";
    /**
     * Class wide HL7 message variable
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";
    /**
     * Class wide temp variable
     */
    String cSourceSystem = "";
    String cSourceFacility = "";

    /**
     * Creates a new instance of GE_RISProcessFromUFD
     * @param pHL7Message
     * @throws BHLibClasses.ICANException
     */
    public GE_RISProcessFromUFD(String pHL7Message, String pEnvironment)  throws ICANException {
        super(pHL7Message);
        mVersion = "a";
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
//------------------------------------------------------------------------------
    /**
     *
     * @throws BHLibClasses.ICANException
     * @return
     */
    public String[] processMessage() throws ICANException {

        String aGE_RISMessageArray[] = {k.NULL, k.NULL, k.NULL};

        String aSegment;
        HL7Group aGroup;
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Message aOutMess = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));
        HL7Segment aPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        HL7Segment aORCSegment = new HL7Segment(mInHL7Message.getSegment("ORC"));
        HL7Segment aOBRSegment = new HL7Segment(mInHL7Message.getSegment("OBR"));

        cSourceSystem = aMSHSegment.get(HL7_23.MSH_3_sending_application);
        cSourceFacility = aMSHSegment.get(HL7_23.MSH_4_sending_facility);

        if(mInHL7Message.isEvent("A01, A02, A03, A04, A08, A11, A12, A13, A28, A31") && (cSourceFacility.matches("ALF") || cSourceFacility.matches("SDMH"))) {

            if (aPV1Segment.isEmpty(HL7_23.PV1_45_discharge_date_time) || mInHL7Message.isEvent("A03") || getFromZBX("FINANCE", "CLAIM_NUMBER").length() > 1)  {

                if (aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"I") ||
                        aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"E") ||
                        aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"O") ||
                        aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"R") ||
                        (aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"PO") && (mInHL7Message.isEvent("A03") || mInHL7Message.isEvent("A04") || mInHL7Message.isEvent("A08")))) {

                    aOutMess = new HL7Message(processMSHFromUFD("GE_RIS").getSegment());
                    aOutMess.append(processEVNFromUFD());
                    aOutMess.append(processPIDFromUFD());
                    aOutMess.append(processNK1s_FromUFD());
                    aOutMess.append(processPV1FromUFD());
                    aOutMess.append(processPV2FromUFD());
                    aOutMess.append(processGT1s_FromUFD());
                    aOutMess.append(processZBXClaimNumberFromUFD());

                    aGE_RISMessageArray[0] = aMSHSegment.get(HL7_23.MSH_3_sending_application);
                    aGE_RISMessageArray[1] = aMSHSegment.get(HL7_23.MSH_4_sending_facility);
                    aGE_RISMessageArray[2] = aOutMess.getMessage();
                }
            }
        } else if(mInHL7Message.isEvent("O01") && cSourceFacility.matches("ALF")) {
            //change to lookup a whitelist
            CodeLookUp aLu = new CodeLookUp("GE_RIS_Whitelist.table", mEnvironment);
            String vOBR_24 = aOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            String aLuResult = aLu.getValue(vOBR_24);
            if (aLuResult.length() > 0) {

            //end change of using a lookup table 2013-11-18
         /*   if (! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "DC")  &&
                    ! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "VL") &&
                    ! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RT") &&
                    ! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "LA") &&
                    ! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MA") &&
                    ! aORCSegment.hasValue(HL7_23.ORC_1_order_control, "NA")) {
           */
                //&& ! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "NM")
                aOutMess = new HL7Message(processMSHFromUFD("GE_RIS").getSegment());
                aOutMess.append(processPIDFromUFD());
                aOutMess.append(processPV1FromUFD());
                aOutMess.append(processORCs_FromUFD());
                aOutMess.append(processOBRs_FromUFD());
                //aOutMess.append(processZB2FromUFD());

                aGE_RISMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
                aGE_RISMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
                aGE_RISMessageArray[2] = aOutMess.getMessage();
            }

        } else if(mInHL7Message.isEvent("A34") && (cSourceFacility.matches("ALF") || cSourceFacility.matches("SDMH"))) {
            aOutMess = new HL7Message(processMSHFromUFD("GE_RIS").getSegment());
            aOutMess.append(processEVNFromUFD());
            aOutMess.append(processPIDFromUFD());
            HL7Segment aMRGSegment = new HL7Segment(processMRGFromUFD().getSegment());
            //check MRG segment and fix SDMH UR
            if (cSourceFacility.matches("SDMH")) {
                String aSDMHUR = aMRGSegment.get(HL7_23.MRG_1_prior_patient_ID_internal);
                aMRGSegment.set(HL7_23.MRG_1_prior_patient_ID_internal, aSDMHUR.concat("-SDMH"));
            }
            //aOutMess.append(processMRGFromUFD());
            aOutMess.append(aMRGSegment);
            aGE_RISMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aGE_RISMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aGE_RISMessageArray[2] = aOutMess.getMessage();
        }

        return aGE_RISMessageArray;
    }

//------------------------------------------------------------------------------

    public HL7Segment processZBXClaimNumberFromUFD() throws ICANException {
        HL7Message aHL7Mess = new HL7Message(mHL7Message, 0);
        HL7Segment aZBXOut = new HL7Segment("ZBX");

        if (! aHL7Mess.isEvent("O01")) {   // for ADT only ...
            aZBXOut.set("ZBX_1", "1");
            aZBXOut.set("ZBX_2", "FINANCE");
            aZBXOut.set("ZBX_3", "CLAIM_NUMBER");
            aZBXOut.set("ZBX_4", getFromZBX("FINANCE", "CLAIM_NUMBER"));
        }
        return aZBXOut;
    }

    public HL7Segment processPIDFromUFD(String pHL7MessageBlock) throws ICANException {

        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);

        HL7Segment aPIDSegmentIN = new HL7Segment(NULL);
        HL7Segment aPIDSegmentOUT = new HL7Segment("PID");
        HL7Segment aMSHSegmentIN = new HL7Segment(aHL7Message.getSegment("MSH"));

        aPIDSegmentIN.setSegment(aHL7Message.getSegment(HL7_24.PID));
        // mHL7Segment = aPIDSegmentIN;

        // Initialze aPIDSegmentOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PID_1_set_ID,
            HL7_23.PID_5_patient_name,
            HL7_23.PID_7_date_of_birth,
            HL7_23.PID_8_sex,
            HL7_23.PID_10_race,
            HL7_23.PID_15_language,
            HL7_23.PID_29_patient_death_date_time,
            HL7_23.PID_30_patient_death_indicator
        };

        aPIDSegmentOUT.linkTo(aPIDSegmentIN);
        aPIDSegmentOUT.copyFields(aCopyFields);

        aPIDSegmentOUT.set(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix, "");   // Clear the Title and force upper case names
        String aName = aPIDSegmentOUT.get(HL7_23.PID_5_patient_name);
        aPIDSegmentOUT.set(HL7_23.PID_5_patient_name, aName.toUpperCase());

        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip);

        //Andrew Grain: Process State description based on PostCode
            String aPostCodeStr = aPIDSegmentOUT.get(HL7_23.XAD_zip);
            String aStateDesc = "";
            if (aPostCodeStr.length() > 0) {
                try {
                    int aPostCode = Integer.parseInt(aPostCodeStr);
                    if (aPostCode >= 800 && aPostCode <= 999) {
                        aStateDesc = "NT";
                    } else if ((aPostCode >= 200 && aPostCode <= 299) ||
                            (aPostCode >= 2600 && aPostCode <= 2619) ||
                            (aPostCode >= 2900 && aPostCode <= 2920)) {
                        aStateDesc = "ACT";
                    } else if ((aPostCode >= 1000 && aPostCode <= 2599) ||
                            (aPostCode >= 2620 && aPostCode <= 2899) ||
                            (aPostCode >= 2921 && aPostCode <= 2999)) {
                        aStateDesc = "NSW";
                    } else if ((aPostCode >= 3000 && aPostCode <= 3999) ||
                            (aPostCode >= 8000 && aPostCode <= 8999)) {
                        aStateDesc = "VIC";
                    } else if (aPostCode == 8888) {
                        aStateDesc = "OVERSEAS";
                    } else if (aPostCode == 9990 || aPostCode == 9988) {
                        aStateDesc = "";
                    } else if ((aPostCode >= 4000 && aPostCode <= 4999) ||
                            (aPostCode >= 9000 && aPostCode <= 9799)) {
                        aStateDesc = "QLD";
                    } else if (aPostCode >= 5000 && aPostCode <= 5999) {
                        aStateDesc = "SA";
                    } else if (aPostCode >= 6000 && aPostCode <= 6999) {
                        aStateDesc = "WA";
                    } else if (aPostCode >= 7000 && aPostCode <= 7999) {
                        aStateDesc = "TAS";
                    }
                    aPIDSegmentOUT.set(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, aStateDesc);

                    aPIDSegmentOUT.setField(aName, mFacility);
                } catch (Exception e) {
                    //Do nothing, postcode is not entered or is invalid
                }
            }

        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country);
        if (aMSHSegmentIN.get(HL7_23.MSH_3_sending_application).indexOf("CSC") >= 0) {
            aPIDSegmentOUT.move(HL7_23.PID_12_county_code, HL7_23.CE_ID_code, HL7_23.PID_11_patient_address, HL7_23.XAD_country);
        } else if (aMSHSegmentIN.get(HL7_23.MSH_3_sending_application).indexOf("CERNER") >= 0) {
            aPIDSegmentOUT.move(HL7_23.PID_12_county_code, HL7_23.CE_ID_code, HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province);
        } else {
            aPIDSegmentOUT.copy(HL7_23.PID_12_county_code);
        }
        // Process Unique identifiers(UR, Medicare, Pension and DVA) held in PID-3 ....
        // NOTE :- PBS Number is also held in PID-3 (type "PB") but when required is normally passed in a Z segment.
        String aTmpField[] = aPIDSegmentIN.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        int i;
        HL7Field aInField;
        HL7Field aOutField;
        String aStr;

        for (i=0 ; i < aTmpField.length ; i++) {
            aInField = new HL7Field(aTmpField[i]);
            aOutField = new HL7Field();
            aStr = aInField.getSubField(HL7_23.CX_ID_type_code);
            if (aStr.equalsIgnoreCase("PI")) {
                if (cSourceFacility.matches("SDMH")) {
                    String aSDMHURNum = aPIDSegmentIN.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i+1);
                    if (aSDMHURNum.startsWith("0") && cSourceSystem.indexOf("CERNER") >= 0) {
                        CodeLookUp aSDMH6DigitLookUp = new CodeLookUp("SDMH_6Digit_UR.table", mEnvironment);
                        String aLUValue = aSDMH6DigitLookUp.getValue(aSDMHURNum);
                        aOutField.setSubField(aLUValue.concat("-SDMH"), HL7_23.CX_ID_number);
                    } else {
                        aOutField.setSubField(aSDMHURNum.concat("-SDMH"), HL7_23.CX_ID_number);
                    }
                } else {
                    aOutField.setSubField(aPIDSegmentIN.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i+1), HL7_23.CX_ID_number);
                }
                if (mFacility.equalsIgnoreCase("ALF")) {
                    aOutField.setSubField("ALF",HL7_23.CX_assigning_authority);
                } else if (mFacility.equalsIgnoreCase("CGMC")) {
                    aOutField.setSubField("CGMC",HL7_23.CX_assigning_authority);
                } else if (mFacility.equalsIgnoreCase("SDMH")) {
                    aOutField.setSubField("SDMH",HL7_23.CX_assigning_authority);
                }
                aPIDSegmentOUT.set(HL7_23.PID_3_patient_ID_internal,aOutField.getField());
            }
            if (aStr.equalsIgnoreCase("PEN")) {
                cPensionNumber =  aInField.getSubField(HL7_23.CX_ID_number);
            }
            if (aStr.equalsIgnoreCase("VA")) {
                cDVANumber = aInField.getSubField(HL7_23.CX_ID_number);
                aPIDSegmentOUT.set(HL7_23.PID_27_veterans_military_status, HL7_23.CE_ID_code, cDVANumber);
                String DVAType = getFromZBX("PMI", "DVA_CARD_TYPE");
                aPIDSegmentOUT.set(HL7_23.PID_27_veterans_military_status, HL7_23.CE_text, DVAType);
            }
            if (aStr.equalsIgnoreCase("MC")) {
                cMedicareNumber =  aInField.getSubField(HL7_23.CX_ID_number);
                if (cMedicareNumber.length() > 11) {
                    aPIDSegmentOUT.set(HL7_23.PID_19_SSN_number, aInField.getSubField(HL7_23.CX_ID_number).substring(0, 11));
                } else {
                    aPIDSegmentOUT.set(HL7_23.PID_19_SSN_number, aInField.getSubField(HL7_23.CX_ID_number));
                }
            }
        }

        aPIDSegmentOUT.linkTo(aPIDSegmentIN);

// The contents of PID_11_4_state_or_province has to be moved to PARIS in PID_12_country_code
//        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_11_patient_address,HL7_23.XAD_state_or_province)) {
//            aPIDSegmentOUT.move(HL7_23.PID_12_county_code, HL7_23.XAD_street_1, HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province);
//        }

// Home phone number is passed in ZB2 segment since PARIS has Home Phone hard coded to U.S. phone format.
        if(!mInHL7Message.isEvent("O01")) {
            if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number)) {
                //cHomePhone = aPIDSegmentIN.get(HL7_23.PID_13_home_phone,HL7_23.XTN_telephone_number);
                aPIDSegmentOUT.copy(HL7_23.PID_13_home_phone,HL7_23.XTN_telephone_number);
            }
            if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number)) {
                aPIDSegmentOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number);
            }
        }

        if (mFacility.equalsIgnoreCase("ALF")) {
            aPIDSegmentOUT.set(HL7_23.PID_18_account_number,HL7_23.CX_assigning_authority, "ALF");
        } else if (mFacility.equalsIgnoreCase("CGMC")) {
            aPIDSegmentOUT.set(HL7_23.PID_18_account_number,HL7_23.CX_assigning_authority, "CGMC");
        } else if (mFacility.equalsIgnoreCase("SDMH")) {
            aPIDSegmentOUT.set(HL7_23.PID_18_account_number,HL7_23.CX_assigning_authority, "SDMH");
        }

        // PARIS takes the PV1 Visit Number as the PID_28 Account Number.
        HL7Segment aPV1SegmentIN = new HL7Segment(aHL7Message.getSegment(HL7_24.PV1));
        String aPV1_19_VisitNumber = aPV1SegmentIN.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);

        aPIDSegmentOUT.set(HL7_23.PID_18_account_number, HL7_23.CX_ID_number, aPV1_19_VisitNumber);

        return aPIDSegmentOUT;
    }

    public HL7Group processNK1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aNK1SegIN = new HL7Segment(k.NULL);
        HL7Segment aNK1SegOUT = new HL7Segment("NK1");
        HL7Group aNK1GroupOUT = new HL7Group();
        // Initialze aNK1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.NK1_1_set_ID,
            HL7_23.NK1_3_next_of_kin_relationship,
            HL7_23.NK1_7_contact_role
        };
        int aNK1GroupCount = aHL7Message.countSegments("NK1");
        for (int i = 1; i <= aNK1GroupCount && i <= 2; i++) {
            aNK1SegIN.setSegment(aHL7Message.getSegment("NK1", i));
            aNK1SegOUT.linkTo(aNK1SegIN);
            aNK1SegOUT.copyFields(aCopyFields);

            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_family_name);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_given_name);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_middle_name);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_suffix);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_prefix);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_degree);
            aNK1SegOUT.set(HL7_23.NK1_2_next_of_kin_name, aNK1SegOUT.get(HL7_23.NK1_2_next_of_kin_name).toUpperCase());

            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_1);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_2);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_city);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_state_or_province);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_zip);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_country);
            aNK1SegOUT.copy(HL7_23.NK1_5_next_of_kin__phone, HL7_23.XTN_telephone_number);
            aNK1SegOUT.copy(HL7_23.NK1_6_business_phone_num, HL7_23.XTN_telephone_number);


            aNK1GroupOUT.append(aNK1SegOUT.getSegment());
        }
        return aNK1GroupOUT;
    }

    public HL7Segment processPV1FromUFD(String pHL7MessageBlock) throws ICANException {
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aPV1SegIN = new HL7Segment(NULL);
        aPV1SegIN.setSegment(aHL7Message.getSegment(HL7_24.PV1));

        HL7Segment aPV2SegIN = new HL7Segment(NULL);
        aPV2SegIN.setSegment(aHL7Message.getSegment(HL7_24.PV2));

        HL7Segment aPV1SegOUT = new HL7Segment("PV1");
        cPatientClass = aPV1SegIN.get(HL7_23.PV1_2_patient_class);
        //CodeLookUp aUnitLookUp = new CodeLookUp("Unit_PARIS.table", mFacility, mEnvironment);
//Start
        CodeLookUp aFinClassLookUp = new CodeLookUp("FinClass_GERIS.table", mFacility, mEnvironment);
//End

// Initialze aPV1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PV1_1_set_ID,
            HL7_23.PV1_2_patient_class,
            HL7_23.PV1_15_ambulatory_status,
            HL7_23.PV1_16_VIP_indicator,
            HL7_23.PV1_18_patient_type,
            HL7_23.PV1_36_discharge_disposition,
            HL7_23.PV1_37_discharged_to_location,
            HL7_23.PV1_41_account_status,
            HL7_23.PV1_45_discharge_date_time
        };

        aPV1SegOUT.linkTo(aPV1SegIN);
        aPV1SegOUT.copyFields(aCopyFields);

// Swap Pre-op "PO" patient class to "W"
        if (cPatientClass.matches("PO")) {
            aPV1SegOUT.set(HL7_23.PV1_2_patient_class, "W");
        }

//
// Patient Current Location .............
        aPV1SegOUT.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);

// No Bed for ED or for OP and for IP Room is same as Point of care.
        if (cPatientClass.equalsIgnoreCase("E") || cPatientClass.equalsIgnoreCase("O")) {
//                aPV1SegOUT.move(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
//                aPV1SegOUT.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
        } else {
// "ICU Special" "ICU patients are admitted to Surgical or General Beds identified by S1-S18 and G19-G36 (or similar).
//  Paris cannot handle the G or S prefix so we have to translate to room ICUG or ICUS for Paris.")
// 11-Apr-07: ICU expanded across to 2WS

            if ((aPV1SegIN.hasValue(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "ICU") ||
                    aPV1SegIN.hasValue(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "2WS")) &&
                    ! aPV1SegIN.isEmpty(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed )) {
//                String aBed =  aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
//                String aNu = aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location,  HL7_23.PL_point_of_care_nu) + aBed.substring(0,1);
//                aBed = aBed.substring(1);
//                aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, aNu);
//                aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aBed);
                  String aBed =  aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                  String aNu = aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location,  HL7_23.PL_point_of_care_nu);
                  aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aNu);
                  aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, aNu);
                  aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aBed);

            } else {
                aPV1SegOUT.move(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                aPV1SegOUT.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            }
        }

        if (mFacility.equalsIgnoreCase("ALF")) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "ALF");
        } else if (mFacility.equalsIgnoreCase("CGMC")) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "CGMC");
        } else if (mFacility.equalsIgnoreCase("SDMH")) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "SDMH");
        }

        if (aPV1SegIN.isEmpty(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name) ||
                aPV1SegIN.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num).equalsIgnoreCase("XX999")) {
            if (cPatientClass.equalsIgnoreCase("E")) {
                if(mFacility.equalsIgnoreCase("SDMH")) {
                    aPV1SegOUT.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, "TJ294");
                } else {
                    aPV1SegOUT.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, "SD465");
                }
            }
        } else  {
            aPV1SegOUT = doDoctor(aPV1SegOUT, HL7_23.PV1_7_attending_doctor);
        }

// Note Attending Dr for inserion in ZB2 segment
        cAttendingDr = aPV1SegOUT.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num);

        int aReferringDoctorCount = aPV1SegIN.countRepeatFields(HL7_23.PV1_8_referring_doctor);
        for (int i = 1; i <= aReferringDoctorCount && i <= 5; i++) {
            String aReferringDoctorIDNum = aPV1SegIN.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, i);
            if (aReferringDoctorIDNum.length() > 0  && !aReferringDoctorIDNum.equalsIgnoreCase(cAttendingDr)) {
                aPV1SegOUT.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, i);
                aPV1SegOUT.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_last_name, i);
                aPV1SegOUT.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_first_name, i);
                aPV1SegOUT.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_middle_initial_or_name, i);
                aPV1SegOUT.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_suffix, i);
                aPV1SegOUT.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_prefix, i);
                aPV1SegOUT.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_degree, i);
                aPV1SegOUT.copy(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_code_source_table, i);
            }
        }

        //aPV1SegOUT = doDoctor(aPV1SegOUT, HL7_23.PV1_9_consulting_doctor);
        int aConsultingDoctorCount = aPV1SegIN.countRepeatFields(HL7_23.PV1_9_consulting_doctor);
        for (int i = 1; i <= aConsultingDoctorCount && i <= 5; i++) {
            String aConsultingDoctorIDNum = aPV1SegIN.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i);
            if (aConsultingDoctorIDNum.length() > 0  && !aConsultingDoctorIDNum.equalsIgnoreCase(cAttendingDr)) {
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_first_name, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_middle_initial_or_name, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_suffix, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_prefix, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_degree, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_code_source_table, i);
            }
        }

        if (aPV1SegIN.isEmpty(HL7_23.PV1_10_hospital_service) && (cPatientClass.equalsIgnoreCase("E")) || (cPatientClass.equalsIgnoreCase("E") && aPV1SegIN.get(HL7_23.PV1_10_hospital_service).matches("AEM"))) {
            if (mFacility.equalsIgnoreCase("ALF")) {
                aPV1SegOUT.set(HL7_23.PV1_10_hospital_service,"EMER");
            } else
                if (mFacility.equalsIgnoreCase("CGMC")) {
                aPV1SegOUT.set(HL7_23.PV1_10_hospital_service,"CEM");
                } else
                    if (mFacility.equalsIgnoreCase("SDMH")) {
                aPV1SegOUT.set(HL7_23.PV1_10_hospital_service,"SED");
                    }
        } else {
            //aPV1SegOUT.set(HL7_23.PV1_10_hospital_service, aUnitLookUp.getValue(aPV1SegIN.get(HL7_23.PV1_10_hospital_service)));
            aPV1SegOUT.copy(HL7_23.PV1_10_hospital_service);
        }

        aPV1SegOUT = doDoctor(aPV1SegOUT, HL7_23.PV1_17_admitting_doctor);

        if (aPV1SegOUT.isEmpty(HL7_23.PV1_18_patient_type)) {
            aPV1SegOUT.move(HL7_23.PV1_18_patient_type,HL7_23.PV1_2_patient_class);
        }

        if (cSourceFacility.matches("SDMH")) {
            String aSDMHVisitID = aPV1SegIN.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
            aPV1SegOUT.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, "S".concat(aSDMHVisitID));
        } else {
            aPV1SegOUT.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
        }

//Start
//        HL7Segment aMSHSegment = new HL7Segment(aHL7Message.getSegment("MSH"));
//        if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("CERNER") >= 0) {
//            //set fin class to blank if message is from CERNER
//            if (aHL7Message.isEvent("O01")) {
//                aPV1SegOUT.set(HL7_23.PV1_20_financial_class, "");
//            } else {
//                aPV1SegOUT.set(HL7_23.PV1_20_financial_class, aFinClassLookUp.getValue(aPV1SegIN.get(HL7_23.PV1_20_financial_class)));
//            }
//            //aPV1SegOUT.set(HL7_23.PV1_20_financial_class, aFinClassLookUp.getValue(aPV1SegIN.get(HL7_23.PV1_20_financial_class)));
//            //aPV1SegOUT.set(HL7_23.PV1_20_financial_class, aPV1SegIN.get(HL7_23.PV1_20_financial_class));
//        } else if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("CSC") >= 0) {
//            String aChargePriceInd = aPV1SegIN.get(HL7_23.PV1_21_charge_price_indicator);
//            aPV1SegOUT.set(HL7_23.PV1_20_financial_class, aChargePriceInd);
//        }
//End

//        aPV1SegOUT.copy(HL7_23.PV1_20_financial_class);

        //if Emergency and financial class is INS, default fin class to H
        String aFinClasstemp = aPV1SegIN.get(HL7_23.PV1_20_financial_class);
        if (cPatientClass.equalsIgnoreCase("E") && aFinClasstemp.equalsIgnoreCase("INS")) {
            aPV1SegOUT.set(HL7_23.PV1_20_financial_class, "H");
        } else {
            aPV1SegOUT.set(HL7_23.PV1_20_financial_class, aFinClassLookUp.getValue(aPV1SegIN.get(HL7_23.PV1_20_financial_class)));
        }
        aPV1SegOUT.copy(HL7_23.PV1_21_charge_price_indicator);

        if (! aHL7Message.isEvent("O01") &&
                (cPatientClass.equalsIgnoreCase("O") ||
                ( cPatientClass.equalsIgnoreCase("E") && ! aPV2SegIN.isEmpty(HL7_23.PV2_8_expected_admit_date)))) {
            aPV1SegOUT.set(HL7_23.PV1_44_admit_date_time, aPV2SegIN.get(HL7_23.PV2_8_expected_admit_date));
        } else if (aHL7Message.isEvent("O01") && cPatientClass.equalsIgnoreCase("O")){
            //set admit date time to blank for outpatient orders
            aPV1SegOUT.set(HL7_23.PV1_44_admit_date_time, "");
        } else {
            aPV1SegOUT.copy(HL7_23.PV1_44_admit_date_time);
        }

        mPV1NursingStation = aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);

        //Norman Soh: PO code change 21Nov2007
        if (cPatientClass.matches("PO")) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "PRE");
            //aPV1SegOUT.set(HL7_23.PV1_20_financial_class, "H");
        }

        //Set pregnancy code in PV1_15
        int aOBXCount = mInHL7Message.countSegments("OBX");
        for (int j = 1; j <= aOBXCount; j++) {
            HL7Segment aOBXSegment = new HL7Segment(mInHL7Message.getSegment("OBX", j));
            String aOBXID = aOBXSegment.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text);
            if (aOBXID.equalsIgnoreCase("Pregnant")) {
                String aPregnancyStatus = aOBXSegment.get(HL7_23.OBX_5_observation_value);
                if (aPregnancyStatus.equalsIgnoreCase("Y")) {
                    aPV1SegOUT.set(HL7_23.PV1_15_ambulatory_status, "B6");
                    j = aOBXCount + 1;
                } else {
                    aPV1SegOUT.set(HL7_23.PV1_15_ambulatory_status, "A9");
                }
            }
            if (j == aOBXCount) {
                aPV1SegOUT.set(HL7_23.PV1_15_ambulatory_status, "A9");
            }
        }

        return aPV1SegOUT;
    }

    public HL7Group processGT1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aGT1SegIN = new HL7Segment(NULL);
        HL7Segment aGT1SegOUT = new HL7Segment("GT1");
        HL7Group aGT1GroupOUT = new HL7Group();
        HL7Segment aPIDSegIN = new HL7Segment(aHL7Message.getSegment("PID"));
        HL7Segment aPV1SegIN = new HL7Segment(aHL7Message.getSegment("PV1"));
        HL7Segment aMSHSegIN = new HL7Segment(aHL7Message.getSegment("MSH"));
        String aLastName = k.NULL;
        String aGivenName = k.NULL;
        String aMiddleName = k.NULL;
        String aStreet1 = k.NULL;
        String aStreet2 = k.NULL;
        String aStateProvince = k.NULL;
        String aCity = k.NULL;
        String aZIP = k.NULL;
        String aPhone = k.NULL;
        String aWorkPhone = k.NULL;
        String aCountry = k.NULL;
        String aGuarantorType = k.NULL;

        String aGT1 = aHL7Message.getSegment(HL7_24.GT1);

        if (aGT1.length() < 4 && aHL7Message.isEvent("A01, A02, A08, A12, A13") && hasValue(HL7_23.MSH_3_sending_application, "CSC-ALF|CSC-CGMC|CSC-SDMH") ||
                aGT1.length() < 4 && (hasValue(HL7_23.MSH_3_sending_application, "CERNERPM"))) {        // If no GT1 then default Guarantor to be the patient.

            String aPV1FinClass = aPV1SegIN.get(HL7_23.PV1_20_financial_class, HL7_23.FC_finacial_class);
            CodeLookUp aLookup = new CodeLookUp("DefaultGuarantors.table", mEnvironment);
            if (aPV1FinClass.equalsIgnoreCase("DVA")) {
                aLastName = aLookup.getValue("DVA1");
                aStreet1 = aLookup.getValue("DVA2");
                aStreet2 = aLookup.getValue("DVA3");
                aStateProvince = aLookup.getValue("DVA4");
                aCity = aLookup.getValue("DVA5");
                aZIP = aLookup.getValue("DVA6");
                aPhone = aLookup.getValue("DVA7");
                aGuarantorType = aLookup.getValue("DVA8");
            } else if (aPV1FinClass.equalsIgnoreCase("TAC")) {
                aLastName = aLookup.getValue("TAC1");
                aStreet1 = aLookup.getValue("TAC2");
                aStreet2 = aLookup.getValue("TAC3");
                aStateProvince = aLookup.getValue("TAC4");
                aCity = aLookup.getValue("TAC5");
                aZIP = aLookup.getValue("TAC6");
                aPhone = aLookup.getValue("TAC7");
                aGuarantorType = aLookup.getValue("TAC8");
            } else if (aPV1FinClass.equalsIgnoreCase("W/C") && hasValue(HL7_23.MSH_3_sending_application, "CERNERPM")) {
                aLastName = getFromZBX("FINANCE", "EMPLOYER_NAME");
                aStreet1 = getFromZBX("FINANCE", "EMPLOYER_ADDRESS");
//                aStreet2 = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_street_2);
//                aCity = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_city);
//                aStateProvince = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_county_parish);
//                aZIP = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_zip);
//                aCountry = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_country);
//                aPhone = aZEISegment.get(PARIS_23.ZEI_5_Employer_Phone, HL7_23.XTN_telephone_number);
//                aGuarantorType = "E";
            } else {
                aLastName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
                aGivenName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
                aMiddleName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, 1);
                aStreet1 = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
                aStreet2 = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
                aStateProvince = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
                aCity = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
                aZIP = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
                aPhone = aPIDSegIN.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
                aWorkPhone = aPIDSegIN.get(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);

                aGT1SegOUT.set(HL7_23.GT1_1_set_ID, "1");
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name, aLastName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_given_name,  aGivenName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_middle_name, aMiddleName, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aStreet1, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aStreet2, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aCity, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, aStateProvince, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aZIP, 1);
                aGT1SegOUT.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aPhone, 1);
                aGT1SegOUT.set(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telephone_number, aWorkPhone, 1);
            }
            if (aPV1FinClass.equalsIgnoreCase("DVA") ||
                    aPV1FinClass.equalsIgnoreCase("TAC") ||
                    aPV1FinClass.equalsIgnoreCase("W/C")) {
                aGT1SegOUT.set(HL7_23.GT1_1_set_ID, "1");
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name, aLastName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_given_name,  aGivenName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_middle_name, aMiddleName, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aStreet1);
                if (aPV1FinClass.equalsIgnoreCase("DVA") ||
                        aPV1FinClass.equalsIgnoreCase("TAC")) {
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aStreet2);
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aCity);
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, aStateProvince);
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aZIP);
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_country, aCountry);
                }
                aGT1SegOUT.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aPhone);
                aGT1SegOUT.set(HL7_23.GT1_10_guarantor_type, aGuarantorType);
            }
            aGT1GroupOUT.append(aGT1SegOUT.getSegment());

        } else if (aGT1.length() > 4) {

            aGT1SegIN.setSegment(aGT1);

            if (aGT1SegIN.get(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name).length() > 0) {

// Initialze aGT1SegOUT with those fields that are straight copies
                String aCopyFields[] =  {
                    HL7_23.GT1_1_set_ID,
                    HL7_23.GT1_2_guarantor_number,
                    HL7_23.GT1_3_guarantor_name,
                    HL7_23.GT1_5_guarantor_address,
                    HL7_23.GT1_6_guarantor_phone_home,
                    HL7_23.GT1_7_guarantor_phone_business,
                    HL7_23.GT1_10_guarantor_type,
                    HL7_23.GT1_11_guarantor_relationship,
                    HL7_23.GT1_12_guarantor_SSN
                };
                aGT1SegOUT.linkTo(aGT1SegIN);
                int aGT1GroupCount = aHL7Message.countSegments(HL7_24.GT1);
                for (int i = 1; i <= aGT1GroupCount; i++) {
                    aGT1SegIN.setSegment(aHL7Message.getSegment("GT1", i));
                    aGT1SegOUT.linkTo(aGT1SegIN);
                    aGT1SegOUT.copyFields(aCopyFields);
                    aGT1GroupOUT.append(aGT1SegOUT.getSegment());
                }
            } else {
                aLastName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
                aGivenName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
                aMiddleName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, 1);
                aStreet1 = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
                aStreet2 = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);

                aStateProvince = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
                aCity = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
                aZIP = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
                aPhone = aPIDSegIN.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
                aWorkPhone = aPIDSegIN.get(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);

                aGT1SegOUT.set(HL7_23.GT1_1_set_ID, "1");
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name, aLastName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_given_name,  aGivenName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_middle_name, aMiddleName, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aStreet1, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aStreet2, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aCity, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, aStateProvince, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aZIP, 1);
                aGT1SegOUT.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aPhone, 1);
                aGT1SegOUT.set(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telephone_number, aWorkPhone, 1);
                aGT1GroupOUT.append(aGT1SegOUT.getSegment());
            }
        }
        return aGT1GroupOUT;

    }

    public HL7Group processORCs_FromUFD() throws ICANException {

        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aORCSegIN = new HL7Segment(k.NULL);
        HL7Segment aOBRSegIN = new HL7Segment(k.NULL);
        HL7Segment aORCSegOUT = new HL7Segment("ORC");
        HL7Group aORCGroupOUT = new HL7Group();

        // Initialze aORCSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.ORC_1_order_control,
        };
        int aORCGroupCount = aHL7Message.countSegments("ORC");
        for (int i = 1; i <= aORCGroupCount; i++) {
            aORCSegIN.setSegment(aHL7Message.getSegment("ORC", i));
            aOBRSegIN.setSegment(aHL7Message.getSegment("OBR", i));

            aORCSegOUT.linkTo(aORCSegIN);
            aORCSegOUT.copyFields(aCopyFields);

            aORCSegOUT.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
            aORCSegOUT.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_2_namespace_ID);
            aORCSegOUT.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
            aORCSegOUT.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID);
            aORCSegOUT.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
            aORCSegOUT.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_2_namespace_ID);
            aORCSegOUT.copy(HL7_23.ORC_5_order_status);
            aORCSegOUT.copy(HL7_23.ORC_9_date_time_of_trans);
            aORCSegOUT.copy(HL7_23.ORC_16_order_control_code_reason, HL7_23.CE_ID_code);
            aORCSegOUT.copy(HL7_23.ORC_16_order_control_code_reason, HL7_23.CE_text);

            // For PARIS the Entered By Provider code is always "WARD" .. or "WARD.TEST" if in Dev environment
            CodeLookUp aLU = new CodeLookUp("DefaultValues.table", mEnvironment);
            String aORUWard = aLU.getValue("PARISWard");

            aORCSegOUT.set(HL7_23.ORC_10_entered_by,HL7_23.XCN_ID_num, aORUWard);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_last_name);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_first_name);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_middle_initial_or_name);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_suffix);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_prefix);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_degree);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_code_source_table);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_assigning_authority);

//
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_ID_num);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_last_name);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_first_name);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_middle_initial_or_name);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_suffix);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_prefix);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_degree);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_code_source_table);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_assigning_authority);
//
//"ORC-13 Enterers Location" "If not provided from CERNER default to the patients location.
// "Facility Logic" "The following code is required to force the Ordering Location to either 'TAC', 'CAS' or 'RAD'
//   and the Observation Date/Time to the Transaction date/time ..... but only for Radiology Orders.
//   Note:- "RADIOLOGY_PRINTER" is ONLY populated for Radiology so we use this as a flagging mechanism for Radiology.")

            if (getFromZBX("ORDER", "RADIOLOGY_PRINTER").length() != 0) {
                if (mPV1NursingStation.matches("ED") ) {
                    if (hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "7")) {  /// Radiology?
                        aORCSegOUT.set(HL7_23.ORC_13_enterers_location, "TAC") ;
                    }  else {
                        aORCSegOUT.set(HL7_23.ORC_13_enterers_location, "CAS") ;
                    }
                } else {
                    aORCSegOUT.set(HL7_23.ORC_13_enterers_location, "RAD") ;
                }
            } else if (aORCSegIN.isEmpty(HL7_23.ORC_13_enterers_location)) {
                aORCSegOUT.set(HL7_23.ORC_13_enterers_location, HL7_23.PL_point_of_care_nu, mPV1NursingStation) ;
            } else {
                aORCSegOUT.copy(HL7_23.ORC_13_enterers_location, HL7_23.PL_point_of_care_nu) ;
            }

            //get contact no / pager number and add to ORC_14
            int aOBXGroupCount = aHL7Message.countSegments("OBX");
            for (int j = 1; j <= aOBXGroupCount; j++) {
                HL7Segment aOBXSegIN = new HL7Segment(aHL7Message.getSegment("OBX", j ));
                String aObservationID = aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text);
                String aObservationIDValue = aOBXSegIN.get(HL7_23.OBX_5_observation_value);
                if (aObservationID.startsWith("Contact No")) {
                    aORCSegOUT.set(HL7_23.ORC_14_call_back_phone_number, aObservationIDValue);
                    j = aOBXGroupCount + 1;
                }
            }

            aORCGroupOUT.append(aORCSegOUT.getSegment());

        }
        return aORCGroupOUT;
    }

    public HL7Group processOBRs_FromUFD() throws ICANException {

        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aOBRSegIN = new HL7Segment(k.NULL);
        HL7Segment aOBXSegIN = new HL7Segment(k.NULL);
        HL7Segment aNTESegIN = new HL7Segment(k.NULL);
        HL7Segment aPV1SegIN = new HL7Segment(aHL7Message.getSegment("PV1"));
        HL7Segment aOBRSegOUT = new HL7Segment("OBR");
        HL7Segment aOBXSegOUT = new HL7Segment(k.NULL);
        HL7Segment aNTESegOUT = new HL7Segment(k.NULL);
        HL7Group aOBRGroupOUT = new HL7Group();
        int aNTEcount = 0;
        int aNTEid = 1;
        int aOBXcount = 0;
        int aOBXid = 1;
        String aClinicalNotesDesc = "";

        // Initialze aOBRSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.OBR_1_Set_ID,
            HL7_23.OBR_4_Universal_Service_ID,
            HL7_23.OBR_10_collector_ID,
            HL7_23.OBR_11_Specimen_Action_Code,
            HL7_23.OBR_13_Relevant_Clinical_Information,
            HL7_23.OBR_14_Specimen_Received_Date_Time,
            HL7_23.OBR_15_Specimen_Source,
            HL7_23.OBR_18_Placers_Field_1,
            HL7_23.OBR_19_Placers_Field_2,
            HL7_23.OBR_20_Fillers_Field_1,
            HL7_23.OBR_21_Fillers_Field_2,
            HL7_23.OBR_24_Diagnostic_Service_Section_ID,
            HL7_23.OBR_30_Transportation_Mode
        };
        int aOBRGroupCount = aHL7Message.countSegments("OBR");
        for (int i = 1; i <= aOBRGroupCount; i++) {
            aOBRSegIN.setSegment(aHL7Message.getSegment("OBR", i));
            aOBRSegOUT.linkTo(aOBRSegIN);
            aOBRSegOUT.copyFields(aCopyFields);

            aOBRSegOUT.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            aOBRSegOUT.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);
            aOBRSegOUT.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
            aOBRSegOUT.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID);
            aOBRSegOUT.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.EI_1_entity_ID);
            aOBRSegOUT.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.EI_2_namespace_ID);
            aOBRSegOUT.copy(HL7_23.OBR_5_Priority);
            aOBRSegOUT.copy(HL7_23.OBR_9_Collection_Volume);
            aOBRSegOUT.copy(HL7_23.OBR_15_Specimen_Source,  HL7_23.Source_CE_1_1_Specimen_code);
            aOBRSegOUT.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text);
            aOBRSegOUT.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method);

//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider,  HL7_23.CE_ID_code);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.CE_text);

            aOBRSegOUT.set(HL7_23.OBR_10_collector_ID, "HIS");

            // For PARIS the Ordering Provider code is always "WARD" .. or "WARD.TEST" if in Dev environment
            CodeLookUp aLU = new CodeLookUp("DefaultValues.table", mEnvironment);
            String aORUWard = aLU.getValue("PARISWard");

            if (aHL7Message.isEvent("O01")) {
                aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num);
            } else {
                aOBRSegOUT.set(HL7_23.OBR_16_Ordering_Provider, aORUWard);
            }
            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name);
            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name);
            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_suffix);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_prefix);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_degree);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_code_source_table);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_assigning_authority);

// "OBR:27 Priorities" "PARIS has special priority codes defined that will print a label.
// These are based on user POWERCHART selection for 'Ward Collect' and a hidden 'Collected' value (defaulted to 'N').
// Combinations of OBR:11 (values 'P', 'L' or 'O') and Priority are mapped to select the desired PARIS priority that will/not print a label.
// Reporting Priority has to default to Requesting Priority if not provided.
// In format field is "requestPriority~reportPriority" and the rules are ...
//     1.  Copy requestPriorityIN to requestPriorityOUT mapping value on way through
//     2.  Does reportPriority exist? ...
//           if YES ... copy reportPriorityIN to reportPriorityOUT mapping value on way through
//           if NO  ... copy requestPriorityIN to reportPriorityOUT mapping value on way through

            if (aOBRSegIN.countRepeatFields(HL7_23.OBR_27_Quantity_Timing) > 0) {
                aLU = new CodeLookUp("CERNER_Priorities_PARIS.table", mEnvironment);
                String aDecodePriority = aOBRSegIN.get(HL7_23.OBR_11_Specimen_Action_Code);
                aDecodePriority = aDecodePriority + aOBRSegIN.get(HL7_23.OBR_27_Quantity_Timing,HL7_23.TQ_6_priority,1);
                String aPriority = aLU.getValue(aDecodePriority);
//
// ... Request Priority ...
                aOBRSegOUT.set(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, aLU.getValue(aPriority), 1);
                aOBRSegOUT.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 1);
//
// ... Reporting Priority ...
                if (aOBRSegIN.countRepeatFields(HL7_23.OBR_27_Quantity_Timing) > 1) {
                    aOBRSegOUT.set(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, aPriority, 2);
                    aOBRSegOUT.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 2);
                } else {    // ... default Reporting to Requesting priority ...
                    aOBRSegOUT.set(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, aPriority, 2);
                    aOBRSegOUT.move(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 2,
                            HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 1);
                }
            }
//
// Fudge for "COPY TO DRS" ... PARIS requires an extra Copy To Dr if any exist
//  (ie 0 Drs = blank field, 1 Dr = 2 Drs, 2 Drs = 3 ... etc).
// So we force an extra Dr 'ZZ999' if any Copy To Dr's exist.")
            int aRep = 1;
            int aCnt = aOBRSegIN.countRepeatFields(HL7_23.OBR_28_Results_Copies_To);
            if (aCnt > 0) {

                for (i = 1; i <= aCnt; i++) {
//                    if (! aOBRSegIN.isEmpty(HL7_23.OBR_28_Results_Copies_To)) {
                    aOBRSegOUT.copy(HL7_23.OBR_28_Results_Copies_To, aRep++);
//                    }
                }
            }
            //aOBRSegOUT.set(HL7_23.OBR_28_Results_Copies_To, "ZZ999", aRep++);

// Reason for study ...
            aCnt = aOBRSegIN.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
            if (aCnt > 0) {
                for (i = 1; i < aCnt; i++) {
//                   if (! aOBRSegIN.isEmpty(HL7_23.OBR_31_Reason_For_Study,HL7_23.CE_text)) {
                    aOBRSegOUT.copy(HL7_23.OBR_31_Reason_For_Study,HL7_23.CE_text, i);
//                   }
                }
            }

            if (getFromZBX("ORDER", "RADIOLOGY_PRINTER").length() != 0) {
                aOBRSegOUT.set(HL7_23.OBR_7_Observation_Date_Time,
                        aOBRSegIN.get(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 1));
            }

            //Move clinical notes in OBX to OBR_31
            int aOBXCount = mInHL7Message.countSegments("OBX");
            for (int j = 1; j <= aOBXCount; j++) {
                HL7Segment aOBXSegment = new HL7Segment(mInHL7Message.getSegment("OBX", j));
                String aOBXID = aOBXSegment.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text);
                if (aOBXID.equalsIgnoreCase("Clinical Notes (Generic)")) {
                    String aReasonForStudy = aOBXSegment.get(HL7_23.OBX_5_observation_value);
                    aReasonForStudy = aReasonForStudy.replaceAll("\\\\.br\\\\", " ");
                    aReasonForStudy = aReasonForStudy.replaceAll("&", "and");
                    aOBRSegOUT.set(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, aReasonForStudy);
                    j = aOBXCount + 1;
                }
            }

            //aOBRSegOUT.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text);

            aOBRGroupOUT.append(aOBRSegOUT.getSegment());
//
// Process any actual and forced NTE segments ...
            String aSREG1 = "";
            String aSREG2 = "";
            String aSREG3 = "";
            String aSREG4 = "";
//            String aSREG4 = "";
            int aObsCnt = 0;


//            if (getFromZBX("ORDER", "RADIOLOGY_PRINTER").length() != 0) {
//                if (! aOBRSegIN.isEmpty(HL7_23.OBR_13_Relevant_Clinical_Information)) {
//                    aNTESegOUT = new HL7Segment("NTE");
//                    aNTESegOUT.set(HL7_23.NTE_1_setID, Integer.toString(aNTEid++));
//                    aNTESegOUT.set(HL7_23.NTE_3_comment, aOBRSegIN.get(HL7_23.OBR_13_Relevant_Clinical_Information));
//                    aNTEcount++;
//
//                    aOBRGroupOUT.append(aNTESegOUT.getSegment());
//                }
//            }

//            int aOBXGroupCount = aHL7Message.countSegments("OBX");

// Scan all OBX and get NumCVC  into SREG2 and get Diabetic or Multi-Slice into SREG4
//            for (i = 1; i <= aOBXGroupCount; i++) {
//                aOBXSegIN = new HL7Segment(aHL7Message.getSegment("OBX", i ));
//                if (aOBXSegIN.hasValue(HL7_23.OBX_3_observation_identifier, "NumCVC")) {
//                    if (aOBXSegIN.hasValue(HL7_23.OBX_5_observation_value, "0")) {
//                        aSREG2 = "CVC(x" + aOBXSegIN.get(HL7_23.OBX_5_observation_value) + ")";
//                    }
//                } else if (aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text).startsWith("Diabetic Patient") &&
//                        !aOBXSegIN.isEmpty(HL7_23.OBX_5_observation_value)) {
//                    aSREG4 = "Diabetic Patient = " + aOBXSegIN.get(HL7_23.OBX_5_observation_value);
//                } else if (aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text).startsWith("Reason for Multi Slice") &&
//                        !aOBXSegIN.isEmpty(HL7_23.OBX_5_observation_value)) {
//                    aObsCnt = aOBXSegIN.countRepeatFields(HL7_23.OBX_5_observation_value);
//                    int j;
//                    for ( j=1 ; j <= aObsCnt ; j++) {
//                        if (aSREG4.length() > 0) {
//                            aSREG4 = aSREG4 + ", " + aOBXSegIN.get(HL7_23.OBX_5_observation_value, j);
//                        } else {
//                            aSREG4 = aOBXSegIN.get(HL7_23.OBX_5_observation_value, j);
//                        }
//                    }
////                    aSREG4 = "Diabetic Patient = " + aOBXSegIN.get(HL7_23.OBX_5_observation_value);
//                }
//            }

// Having picked out the CVC, Diabetic and Multislice, ensure they are sent to PARIS in
// the required priority order.
//            for (i = 1; i <= aOBXGroupCount; i++) {
//                aOBXSegIN = new HL7Segment(aHL7Message.getSegment("OBX", i ));
//
//                if (aOBXSegIN.isEmpty(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code)
//                || aOBXSegIN.hasValue(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code,"NumCVC")) {
//                    if (aSREG4.length() > 0)  {
//                        aNTESegOUT = new HL7Segment("NTE");
//                        aNTESegOUT.set(HL7_23.NTE_1_setID, Integer.toString(aNTEid++));
//                        aNTESegOUT.set(HL7_23.NTE_3_comment, aSREG4);
//                        aOBRGroupOUT.append(aNTESegOUT.getSegment());
//                        aNTEcount++;
//                        aSREG4 = "";
//                    }
//                } else {
//                    if (aOBXSegIN.hasValue(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code,"Invasive Devices")) {
//                        aNTESegOUT = new HL7Segment("NTE");
//                        aNTESegOUT.set(HL7_23.NTE_1_setID, Integer.toString(aNTEid++));
//                        aSREG1 ="";
//                        aObsCnt = aOBXSegIN.countRepeatFields(HL7_23.OBX_5_observation_value);
//                        int j;
//                        for ( j=1 ; j <= aObsCnt ; j++) {
//                            aSREG3 = aOBXSegIN.get(HL7_23.OBX_5_observation_value, j);
//                            if (aSREG3.matches("CVC")) {
//                                if (aSREG2.length() > 0) {
//                                    aSREG3 = aSREG2;
//                                    aSREG2="";
//                                }
//                            }
//                            if (aSREG1.length() > 0) {
//                                aSREG1 = aSREG1 + ", " + aSREG3;
//                            } else {
//                                aSREG1 = aSREG3;
//                            }
//                        }
//
//                        if (aSREG2.length() > 0) {
//                            if (aSREG1.length() > 0) {
//                                aSREG1 = aSREG2 + ", " + aSREG1;
//                            } else {
//                                aSREG1 = aSREG2;
//                            }
//                        }
//                        aNTESegOUT.set(HL7_23.NTE_3_comment, aSREG1);
//                        aOBRGroupOUT.append(aNTESegOUT.getSegment());
//                        aNTEcount++;
//                    }
//                }
//            }
        }



// Now process any real OBX segments for GERIS - Consolidate all OBX into one segment
        String aObservationIDFinal = "";

        aOBXSegOUT = new HL7Segment("OBX");
        aOBXSegOUT.set(HL7_23.OBX_1_set_ID,"1");
        aOBXSegOUT.set(HL7_23.OBX_2_value_type, "ST");
        aOBXSegOUT.set(HL7_23.OBX_3_observation_identifier, "^ORDER DETAILS");
        aOBXSegOUT.set(HL7_23.OBX_6_units, "");

        int aOBXGroupCount = aHL7Message.countSegments("OBX");
        for (int i = 1; i <= aOBXGroupCount; i++) {
            aOBXSegIN = new HL7Segment(aHL7Message.getSegment("OBX", i ));
            String aObservationID = aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text);
            String aObservationIDValue = aOBXSegIN.get(HL7_23.OBX_5_observation_value);
            if (! aObservationID.startsWith("Clinical Notes (Generic)") &&
                    ! aObservationID.startsWith("Pregnant") &&
                    //! aObservationID.startsWith("Infectious Diseases") &&
                    ! aObservationID.startsWith("Contact No")) {
//Start
//                if (aObservationIDFinal.length() == 0) {
//                    if (aObservationIDValue.length() > 0) {
//                        aObservationIDFinal = aObservationID.concat(" = ").concat(aObservationIDValue);
//                    } else {
//                        aObservationIDFinal = aObservationID.concat(" = ").concat("Not Valued");
//                    }
//                } else {
//                    if (aObservationIDValue.length() > 0) {
//                        aObservationIDFinal = aObservationIDFinal.concat(", ").concat(aObservationID).concat(" = ").concat(aObservationIDValue);
//                    } else {
//                        aObservationIDFinal = aObservationIDFinal.concat(", ").concat(aObservationID).concat(" = ").concat("Not Valued");
//                    }
//                }
//End

                if (aObservationID.startsWith("Diabetic Patient")) {
                    aOBXSegOUT.set("OBX_5", "FLD_1", aObservationIDValue);
                } else if (aObservationID.startsWith("Language")) {
                    aOBXSegOUT.set("OBX_5", "FLD_2", aObservationIDValue);
                } else if (aObservationID.startsWith("Infectious Diseases")) {
                    //swap tilda with ampersand character
                    aObservationIDValue = aObservationIDValue.replaceAll("~", "&");
                    aOBXSegOUT.set("OBX_5", "FLD_3", aObservationIDValue);
                } else if (aObservationID.startsWith("EPICARDIAL/CARDIAC PACEMAKER")) {
                    aOBXSegOUT.set("OBX_5", "FLD_4_1", aObservationIDValue);
                } else if (aObservationID.startsWith("CEREBRAL ANEURYSM CLIP")) {
                    aOBXSegOUT.set("OBX_5", "FLD_4_2", aObservationIDValue);
                } else if (aObservationID.startsWith("COCHLEAR IMPLANT")) {
                    aOBXSegOUT.set("OBX_5", "FLD_4_3", aObservationIDValue);
                } else if (aObservationID.startsWith("Hx EYE INJURY CAUSED BY METAL")) {
                    aOBXSegOUT.set("OBX_5", "FLD_4_4", aObservationIDValue);
                } else if (aObservationID.startsWith("Hx METAL WORKER/WELDER")) {
                    aOBXSegOUT.set("OBX_5", "FLD_4_5", aObservationIDValue);
                } else if (aObservationID.startsWith("ANY METAL IMPLANT")) {
                    aOBXSegOUT.set("OBX_5", "FLD_4_6", aObservationIDValue);
                } else if (aObservationID.startsWith("PATIENT ACTIVELY ASSESSED BY ME")) {
                    aOBXSegOUT.set("OBX_5", "FLD_4_7", aObservationIDValue);
                }

            }
        }

//Start
//        if (aObservationIDFinal.length() > 0) {
//            aOBXSegOUT = new HL7Segment("OBX");
//            aOBXSegOUT.linkTo(aOBXSegIN);
//            aOBXSegOUT.set(HL7_23.OBX_1_set_ID,"1");
//            aOBXSegOUT.set(HL7_23.OBX_2_value_type, "ST");
//            aOBXSegOUT.set(HL7_23.OBX_3_observation_identifier, "^ORDER DETAILS");
//            aOBXSegOUT.set(HL7_23.OBX_5_observation_value, aObservationIDFinal);
//            aOBXSegOUT.set(HL7_23.OBX_6_units, "");
//            aOBXSegOUT.set(HL7_23.OBX_7_references_range, "");
//            aOBXSegOUT.set(HL7_23.OBX_8_abnormal_flags, "");
//            aOBRGroupOUT.append(aOBXSegOUT.getSegment());
//        }
//End

        if (aOBXSegOUT.getSegment().length() > 0) {
            aOBRGroupOUT.append(aOBXSegOUT.getSegment());
        }

        return aOBRGroupOUT;
    }

    private HL7Segment doDoctor(HL7Segment pSegment, String pFieldID) throws ICANException {
        pSegment.copy(pFieldID, HL7_23.XCN_ID_num);
        if (! pSegment.isEmpty(pFieldID, HL7_23.XCN_ID_num)) {
            pSegment.copy(pFieldID, HL7_23.XCN_first_name);
            pSegment.copy(pFieldID, HL7_23.XCN_last_name);
            pSegment.copy(pFieldID, HL7_23.XCN_middle_initial_or_name);
            pSegment.copy(pFieldID, HL7_23.XCN_prefix);
        }
        return pSegment;
    }

}
