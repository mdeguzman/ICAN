/*
 * PACSProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 */

package com.dataagility.ICAN.BHLibPACS;

import com.dataagility.ICAN.BHLibClasses.*;

import java.text.*;

/**
 *
 * @author fillinghamr
 */
public class PACSProcessFromUFD extends ProcessSegmentsFromUFD {

    BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public HL7Message mInHL7Message;
    public String mPACSType = "";
    public String mSReg1 = "";
    public String mSReg2 = "";
    public String mSReg3 = "";
    public String mSReg4 = "";
    //--------------------------------------------------------------------------
    /**
     * Creates a new instance of PACSProcessFromUFD
     */
    public PACSProcessFromUFD(String pHL7Message, String pEnvironment)  throws ICANException {
        super(pHL7Message);
        mVersion = "c";    // PACSProcessFromUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }

    public PACSProcessFromUFD(String pHL7Message, String pEnvironment, String pPACSType)  throws ICANException {
        super(pHL7Message);
        mVersion = "c";    // PACSProcessFromUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
        mPACSType = pPACSType;
    }
    //--------------------------------------------------------------------------
    public String[] processMessage() throws ICANException {
        mInHL7Message = new HL7Message(mHL7Message);
        String aPACSMessageArray[] = {k.NULL, k.NULL, k.NULL};

        String aSegment;
        HL7Group aGroup;
        HL7Message aOutMess = new HL7Message("");
        HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));
// PACS takes A01, A02, A03, A04, A05, A08, A11, A13, A21, A22 ... No A17 and No A34
// PACS ADT Structure is ... MSH, PID, PV1

        if(mInHL7Message.isEvent("A01, A02, A03, A04 A05, A08, A11, A12, A13, A21, A22")) {
            // If discharge date is empty or message is an A03 or message contains a Claim Number  ...
            // ... then send to PACS.
            HL7Segment aPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
            if (aPV1Segment.isEmpty(HL7_23.PV1_45_discharge_date_time) ||
                    mInHL7Message.isEvent("A03") ||
                    getFromZBX("FINANCE", "CLAIM_NUMBER").length() > 1)  {
                aOutMess.setSegment(processMSHFromUFD("PACS").getSegment());
                aOutMess.append(processPIDFromUFD());
                aOutMess.append(processPV1FromUFD());
            }
        } else if (mInHL7Message.isEvent("R01, R03")) {
            String aSendingApp = aMSHSegment.get(HL7_23.MSH_3_sending_application);
            HL7Segment aInOBRSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.OBR));
            String aDiagServSec = aInOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            String aOBR21FillerField2 = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2);
            boolean aProcessFlag = false;
            if (mPACSType.equalsIgnoreCase("CT")) {
                if (aSendingApp.startsWith("PARIS") && aDiagServSec.equalsIgnoreCase("RA") &&
                        aOBR21FillerField2.indexOf("CT") >= 0) {
                    aProcessFlag = true;
                }
            } else {
                if (aSendingApp.startsWith("PARIS") && aDiagServSec.equalsIgnoreCase("RA") ||
                        aSendingApp.startsWith("NMHL7")) {
                    aProcessFlag = true;
                }
            }
            if (aProcessFlag == true) {
                aMSHSegment = processMSHFromUFD("PACS");
                HL7Segment aPIDSegment = processPIDFromUFD();
                HL7Segment aPV1Segment = processPV1FromUFD();
                HL7Group aReqDetsGroup = processR01ReqDets_FromUFD();
                aOutMess.append(aMSHSegment);
                aOutMess.append(aPIDSegment);
                aOutMess.append(aPV1Segment);
                aOutMess.append(aReqDetsGroup);
            }
        } else if (mInHL7Message.isEvent("O01")) {
            String aSendingApp = aMSHSegment.get(HL7_23.MSH_3_sending_application);
            HL7Segment aInOBRSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.OBR));
            String aDiagServSec = aInOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            String aOBR21FillerField2 = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2);
            boolean aProcessFlag = false;
            if (mPACSType.equalsIgnoreCase("CT")) {
//                if (aSendingApp.startsWith("PARIS") && aDiagServSec.equalsIgnoreCase("RA") &&
//                        aOBR21FillerField2.indexOf("CT") >= 0) {
//                    aProcessFlag = true;
//                }
            } else {
                if ((aSendingApp.startsWith("CERNER") && (aDiagServSec.equalsIgnoreCase("NM") || aDiagServSec.equalsIgnoreCase("RT")))) {
                    aProcessFlag = true;
                }
            }
            if (aProcessFlag == true) {
                aMSHSegment = processMSHFromUFD("PACS");
                HL7Segment aPIDSegment = processPIDFromUFD();
                HL7Segment aPV1Segment = processPV1FromUFD();
                HL7Group aReqDetsGroup = processO01ReqDets_FromUFD();
                aOutMess.append(aMSHSegment);
                aOutMess.append(aPIDSegment);
                aOutMess.append(aPV1Segment);
                aOutMess.append(aReqDetsGroup);
            }
        }
        aPACSMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
        aPACSMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
        aPACSMessageArray[2] = aOutMess.getMessage();
        return aPACSMessageArray;
    }
    //--------------------------------------------------------------------------
    /**PACS specific processing for an Outgoing i.e "To" MSH segment.
     * @return Returns the processed HL7 MSH segment as a String.
     */
    public HL7Segment processMSHFromUFD(String pReceivingApplication) throws ICANException {
// Non copy fields are

        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);

        HL7Segment aMSHSegIN = new HL7Segment(NULL);
        HL7Segment aMSHSegOUT = new HL7Segment("MSH");

        aMSHSegIN.setSegment(aHL7Message.getSegment(HL7_24.MSH));
        mHL7Segment = aMSHSegIN;                    // In case any of the "do" functions need to see the segment
        mFacility = aMSHSegIN.get(HL7_24.MSH_4_sending_facility);
        mHospitalID = mFacility.substring(mFacility.length()-1, mFacility.length());
        mHL7MessageEvent = aMSHSegIN.get(HL7_23.MSH_9_message_type, HL7_23.CM_type);

// Initialze aMSHSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.MSH_2_encoding_characters,
                    HL7_23.MSH_3_sending_application,
                    HL7_23.MSH_4_sending_facility,
                    HL7_23.MSH_6_receiving_facility,
                    HL7_23.MSH_9_message_type,
                    HL7_23.MSH_10_message_control_ID,
                    HL7_23.MSH_11_processing_ID,
                    HL7_23.MSH_12_version_ID
        };

// Initialze OUT with those fields that are straight copies
        aMSHSegOUT.linkTo(aMSHSegIN);
        aMSHSegOUT.copyFields(aCopyFields);

        aMSHSegOUT.set(HL7_23.MSH_5_receiving_application, pReceivingApplication);
        aMSHSegOUT.set(HL7_23.MSH_7_message_date_time, aMSHSegIN.getDateTime());
        aMSHSegOUT.copy(HL7_23.MSH_9_2_trigger_event);
        return (aMSHSegOUT);
    }
//--------------------------------------------------------------------------
    public HL7Segment processPIDFromUFD() {
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");
        String aPID3PatientID = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
        NumberFormat formatter = new DecimalFormat("0000000");
        if (!aPID3PatientID.endsWith("EXTN")) {
            if (mEnvironment.indexOf("TEST") >= 0) {
                aPID3PatientID = "Z" + formatter.format(Integer.parseInt(aPID3PatientID)).concat(mSReg1);
            } else {
                aPID3PatientID = formatter.format(Integer.parseInt(aPID3PatientID)).concat(mSReg1);
            }
        }
        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPID3PatientID, 1);

        aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, 1);
        //Check and swap CERNER ALFUR/CGMCUR/SDMHUR to ALF/CGMC/SDMH
        int aPIDCounter01 = aOutPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int aIndex01 = 1; aIndex01 <= aPIDCounter01; aIndex01++) {
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aIndex01).equalsIgnoreCase("ALFUR")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "ALF", aIndex01);
            }
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aIndex01).equalsIgnoreCase("CGMCUR")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "CGMC", aIndex01);
            }
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aIndex01).equalsIgnoreCase("SDMHUR")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "SDMH", aIndex01);
            }
        }

        //If message from CSC or PARIS append -CGMC or -SDMH to UR
        String aSendApplication = aInMSHSegment.get(HL7_23.MSH_3_sending_application);
        String aSendFacility = aInMSHSegment.get(HL7_23.MSH_4_sending_facility);
        if (aSendApplication.startsWith("CSC") || aSendApplication.startsWith("PARIS") || aSendApplication.startsWith("CERNERPM")) {
            for (int aIndex01 = 1; aIndex01 <= aPIDCounter01; aIndex01++) {
                String aAAuth = aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aIndex01);
                if (aAAuth.equalsIgnoreCase("MRN")) {
                    if (aSendFacility.startsWith("CGMC") || aSendFacility.startsWith("SDMH")) {
                        String aPatientUR = aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aIndex01);
                        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPatientUR + "-" + aSendFacility, aIndex01);
                    }
                    aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, aSendFacility, aIndex01);
                    aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aSendFacility, aIndex01);
                }
                if (aAAuth.equalsIgnoreCase("CGMC") || aAAuth.equalsIgnoreCase("SDMH")) {
                    String aPatientUR = aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aIndex01);
                    aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPatientUR + "-" + aSendFacility, aIndex01);
                }
            }
        }

        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, 1);

        //Check to see if running in test env.
        if (mEnvironment.indexOf("TEST") >= 0) {
            String aFamilyName = aOutPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
            aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, "ZZZTEST-" + aFamilyName, 1);
        }

        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        return aOutPIDSegment;
    }
//--------------------------------------------------------------------------
    public HL7Segment processPV1FromUFD() {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        CodeLookUp aLookUp = new CodeLookUp("RADIOLOGY_FINCLASS.table", mEnvironment);

        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location);
            aOutPV1Segment.copy(HL7_23.PV1_15_ambulatory_status);
            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
//
// For Orders/Results translate Financial Class to "P" or "H".
            if (mInHL7Message.isEvent("R01, R03, O01")) {
                aOutPV1Segment.set(HL7_23.PV1_20_financial_class, aLookUp.getValue(aInPV1Segment.get(HL7_23.PV1_20_financial_class)));
            }

            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "01");
        }
        return aOutPV1Segment;
    }
//--------------------------------------------------------------------------
    public HL7Group processR01ReqDets_FromUFD() {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Segment aOutNTESegment = new HL7Segment("");
        HL7Segment aOutOBXSegment = new HL7Segment("");

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            aOutORCSegment = new HL7Segment("ORC");
            aOutORCSegment.linkTo(aInORCSegment);
            aOutORCSegment.copy(HL7_23.ORC_1_order_control);
            aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num);
            aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
            aOutORCSegment.copy(HL7_23.ORC_5_order_status);
            aOutORCSegment.copy(HL7_23.ORC_13_enterers_location);
            aOutReqDetsGroup.append(aOutORCSegment);

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            if (mInHL7Message.getMessage().indexOf("|NUCM|") == -1 && mInHL7Message.getMessage().indexOf("|NMHL7|") == -1) {
                String aOBR4UniversalServiceID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                if (aOBR4UniversalServiceID.matches("7[0-9]{5}0")) {
                    mSReg3 = aOBR4UniversalServiceID.substring(1, 6);
                } else {
                    mSReg3 = aOBR4UniversalServiceID;
                }
                aOutOBRSegment = new HL7Segment("OBR");
                aOutOBRSegment.linkTo(aInOBRSegment);
                aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);

                aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
                aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);

                String aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2);

                if (mEnvironment.indexOf("TEST") >= 0) {
                    mSReg4 = aOBR21Filler.substring(2, 4) + "Z" + aOBR21Filler.substring(5, 12);
                    aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID, mSReg4 + "-" + mSReg3);
                } else {
                    aOBR21Filler = aOBR21Filler.substring(2,12) + "-" + mSReg3;
                    aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID, aOBR21Filler);
                }

                aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID);
                aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);

                int aOrderingProviderCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
                for (int j = 1; j <= aOrderingProviderCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_suffix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_prefix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_degree, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_code_source_table, j);
                }

                aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
                aOutOBRSegment.copy(HL7_23.OBR_22_Results_RPT_Status_Change);
                try {
                    if (Integer.parseInt(aOBR4UniversalServiceID) == 7200940) {
                        aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RF");
                    } else if (Integer.parseInt(aOBR4UniversalServiceID) >= 7550030 &&
                            Integer.parseInt(aOBR4UniversalServiceID) <= 7550690) {
                        aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RF");
                    } else {
                        aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2,4);
                        aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, aOBR21Filler);
                    }
                } catch (Exception e) {
                    aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2,4);
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, aOBR21Filler);
                }
                //aOutOBRSegment.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
                //aOutOBRSegment.set(HL7_23.OBR_25_Results_Status, "F");
                aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);

                int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
                for (int j = 1; j <= aQuantityTimingCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
                }

                int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
                for (int j = 1; j <= aReasonForStudyCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
                }

                aOutOBRSegment.copy(HL7_23.OBR_32_Principal_Result_Interpreter);
            } else {
                aOutOBRSegment = new HL7Segment("OBR");
                aOutOBRSegment.linkTo(aInOBRSegment);
                aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
                aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number);
                aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID);
                aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);
                int aOrderingProviderCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
                for (int j = 1; j <= aOrderingProviderCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_suffix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_prefix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_degree, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_code_source_table, j);
                }
                aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
                aOutOBRSegment.copy(HL7_23.OBR_22_Results_RPT_Status_Change);
                aOutOBRSegment.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
                aOutOBRSegment.set(HL7_23.OBR_25_Results_Status, "F");
                int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
                for (int j = 1; j <= aQuantityTimingCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
                }
                int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
                for (int j = 1; j <= aReasonForStudyCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
                }
                aOutOBRSegment.copy(HL7_23.OBR_32_Principal_Result_Interpreter);
            }

            aOutReqDetsGroup.append(aOutOBRSegment);

            //process NTE
            if (!aInOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID).equalsIgnoreCase("NM")) {
                HL7Group aOutNTEGroup = new HL7Group("");
                int aNTESegmentCount = aInReqDetsGroup.countSegments(HL7_23.NTE);
                for (int j = 1; j <= aNTESegmentCount; j++) {
                    HL7Segment aInNTESegment = new HL7Segment(aInReqDetsGroup.getSegment("NTE", j));
                    aOutNTESegment = new HL7Segment("NTE");
                    aOutNTESegment.linkTo(aInNTESegment);
                    aOutNTESegment.copy(HL7_23.NTE_1_setID);
                    aOutNTESegment.copy(HL7_23.NTE_2_source_of_comment);
                    aOutNTESegment.copy(HL7_23.NTE_3_comment, 1);
                    aOutNTEGroup.append(aOutNTESegment);
                }
                aOutReqDetsGroup.append(aOutNTEGroup);
            }
            //process OBX
            HL7Group aOutOBXGroup = new HL7Group("");
            int aOBXSegmentCount = aInReqDetsGroup.countSegments(HL7_23.OBX);
            int aOBXSetID = 1;
            for (int k = 1; k <= aOBXSegmentCount; k++) {
                HL7Segment aInOBXSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBX", k));
                aOutOBXSegment = new HL7Segment("OBX");
                aOutOBXSegment.linkTo(aInOBXSegment);
                //aOutOBXSegment.copy(HL7_23.OBX_1_set_ID);
                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(aOBXSetID++));
                aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
                aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier);
                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "");

                int aOBX5FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                for (int m = 1; m <= aOBX5FieldCount; m++) {
                    String aObsValue = aInOBXSegment.get(HL7_23.OBX_5_observation_value, m);
                    if (aObsValue.equalsIgnoreCase(".")) {
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, " ", m);
                    } else if (aObsValue.startsWith("Reported by:")) {
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, aObsValue, m);
                        aOutOBXGroup.append(aOutOBXSegment);
                        //put a new blank OBX segment
                        aInOBXSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBX", k));
                        aOutOBXSegment = new HL7Segment("OBX");
                        aOutOBXSegment.linkTo(aInOBXSegment);
                        //aOutOBXSegment.copy(HL7_23.OBX_1_set_ID);
                        aOutOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(aOBXSetID++));
                        aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
                        aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier);
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "");
                    } else if (aObsValue.startsWith("DF /DF")) {
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "", m);
                    } else {
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, aObsValue, m);
                    }
                }
                aOutOBXGroup.append(aOutOBXSegment);
            }
            aOutReqDetsGroup.append(aOutOBXGroup);
        }

        return aOutReqDetsGroup;
    }
//--------------------------------------------------------------------------
    public HL7Group processO01ReqDets_FromUFD() {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Segment aOutNTESegment = new HL7Segment("");
        HL7Segment aOutOBXSegment = new HL7Segment("");

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            aOutORCSegment = new HL7Segment("ORC");
            aOutORCSegment.linkTo(aInORCSegment);
            aOutORCSegment.copy(HL7_23.ORC_1_order_control);
            aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num);
            aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
            aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
            String aORC1OrderControl = aInORCSegment.get(HL7_23.ORC_1_order_control);
            if (aORC1OrderControl.equalsIgnoreCase("OC")) {
                aOutORCSegment.set(HL7_23.ORC_1_order_control, "CA");
            }
            if (aORC1OrderControl.equalsIgnoreCase("SC")) {
                aOutORCSegment.set(HL7_23.ORC_5_order_status, "CM");
            } else {
                aOutORCSegment.copy(HL7_23.ORC_5_order_status);
            }
            aOutReqDetsGroup.append(aOutORCSegment);
            String aORC_9_DateTime = aInORCSegment.get(HL7_23.ORC_9_date_time_of_trans);

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            if (mInHL7Message.getMessage().indexOf("|NUCM|") == -1) {
                String aOBR4UniversalServiceID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                if (aOBR4UniversalServiceID.matches("7[0-9]{5}0")) {
                    mSReg3 = aOBR4UniversalServiceID.substring(1, 6);
                } else {
                    mSReg3 = aOBR4UniversalServiceID;
                }
                aOutOBRSegment = new HL7Segment("OBR");
                aOutOBRSegment.linkTo(aInOBRSegment);
                aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);

                aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
                aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);

                String aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2);

                if (mEnvironment.indexOf("TEST") >= 0) {
                    if (aOBR21Filler.length() == 12) {
                        mSReg4 = aOBR21Filler.substring(2, 4) + "Z" + aOBR21Filler.substring(5, aOBR21Filler.length());
                        aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID, mSReg4 + "-" + mSReg3);
                    }
                } else {
                    if (aOBR21Filler.length() == 12) {
                        aOBR21Filler = aOBR21Filler.substring(2, aOBR21Filler.length()) + "-" + mSReg3;
                    } else {
                        aOBR21Filler = aOBR21Filler + "-" + mSReg3;
                    }
                    aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID, aOBR21Filler);
                }
                aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID);
                aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);

                int aOrderingProviderCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
                for (int j = 1; j <= aOrderingProviderCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_suffix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_prefix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_degree, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_code_source_table, j);
                }

                aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
                aOutOBRSegment.copy(HL7_23.OBR_22_Results_RPT_Status_Change);
                try {
                    if (Integer.parseInt(aOBR4UniversalServiceID) == 7200940) {
                        aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RF");
                    } else if (Integer.parseInt(aOBR4UniversalServiceID) >= 7550030 &&
                            Integer.parseInt(aOBR4UniversalServiceID) <= 7550690) {
                        aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RF");
                    } else {
                        aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2,4);
                        aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, aOBR21Filler);
                    }
                } catch (Exception e) {
                    aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2);
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, aOBR21Filler);
                }

                //aOutOBRSegment.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
                //aOutOBRSegment.set(HL7_23.OBR_25_Results_Status, "F");
                aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);

                int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
                for (int j = 1; j <= aQuantityTimingCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
                }
                int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
                for (int j = 1; j <= aReasonForStudyCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
                }
                aOutOBRSegment.copy(HL7_23.OBR_32_Principal_Result_Interpreter);
            } else {
                aOutOBRSegment = new HL7Segment("OBR");
                aOutOBRSegment.linkTo(aInOBRSegment);
                aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
                String aVar2 = "000000000" + aInOBRSegment.get(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
                int aSt_Len = aVar2.length();
                int aSt_End = aSt_Len - 7;
                String aVar3 = aVar2.substring(aSt_End, aSt_Len);
                aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
                String aTempStr01 = "NM" + aORC_9_DateTime.substring(2, 4) + aVar3 + "-" + aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID, aTempStr01);

                String aCE_IDCode = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                String aCE_Text = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);

                aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, aCE_IDCode);
                aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text, aCE_IDCode);
                aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, aCE_Text);

                aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);
                int aOrderingProviderCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
                for (int j = 1; j <= aOrderingProviderCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name, j);
                }
                aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
                aOutOBRSegment.copy(HL7_23.OBR_22_Results_RPT_Status_Change);
                String aDiagnosticServiceSectionID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                if (aDiagnosticServiceSectionID.startsWith("PET")) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "PT");
                } else {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "NM");
                }
                aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);
                int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
                for (int j = 1; j <= aQuantityTimingCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
                }
                int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
                for (int j = 1; j <= aReasonForStudyCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
                }
                aOutOBRSegment.copy(HL7_23.OBR_32_Principal_Result_Interpreter);
                int aTechnicianCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_34_technician);
                for (int j = 1; j <= aTechnicianCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_34_technician, j);
                }
            }
            aOutReqDetsGroup.append(aOutOBRSegment);

            //process NTE
            if (!aInOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID).equalsIgnoreCase("NM")) {
                HL7Group aOutNTEGroup = new HL7Group("");
                int aNTESegmentCount = aInReqDetsGroup.countSegments(HL7_23.NTE);
                for (int j = 1; j <= aNTESegmentCount; j++) {
                    HL7Segment aInNTESegment = new HL7Segment(aInReqDetsGroup.getSegment("NTE", j));
                    aOutNTESegment = new HL7Segment("NTE");
                    aOutNTESegment.linkTo(aInNTESegment);
                    aOutNTESegment.copy(HL7_23.NTE_1_setID);
                    aOutNTESegment.copy(HL7_23.NTE_2_source_of_comment);
                    aOutNTESegment.copy(HL7_23.NTE_3_comment, 1);
                    aOutNTEGroup.append(aOutNTESegment);
                }
                aOutReqDetsGroup.append(aOutNTEGroup);
            }
        }

        return aOutReqDetsGroup;
    }
}
