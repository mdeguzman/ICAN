/*
 * DDSProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 */

package BHLibDDS;

import BHLibClasses.*;

import java.text.*;

/**
 *
 * @author sohn
 */
public class DDSProcessFromUFD extends ProcessSegmentsFromUFD {

    BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public HL7Message mInHL7Message;
    public String mSReg1 = "";
    public String mSReg2 = "";
    public String mSReg3 = "";
    public String mSReg4 = "";
//--------------------------------------------------------------------------
    /**
     * Creates a new instance of DDSProcessFromUFD
     */
    public DDSProcessFromUFD(String pHL7Message, String pEnvironment)  throws ICANException {
        super(pHL7Message);
        mVersion = "A";    // DDSProcessFromUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
//--------------------------------------------------------------------------
    public String[] processMessage() throws ICANException {
        mInHL7Message = new HL7Message(mHL7Message);
        String aDDSMessageArray[] = {k.NULL, k.NULL, k.NULL};

        String aSegment;
        HL7Group aGroup;
        HL7Message aOutMess = new HL7Message("");
        HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));

        if (mInHL7Message.isEvent("O01")) {
            String aSendingApp = aMSHSegment.get(HL7_23.MSH_3_sending_application);
            HL7Segment aInOBRSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.OBR));
            HL7Segment aInORCSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.ORC));
            String aDiagServSec = aInOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            String aOrderStatus = aInORCSegment.get(HL7_23.ORC_5_order_status);
            if ((aSendingApp.startsWith("CERNER") && aDiagServSec.equalsIgnoreCase("RA") && aOrderStatus.equalsIgnoreCase("IP")) ||
                    (aSendingApp.startsWith("PARIS") && aDiagServSec.equalsIgnoreCase("RA"))) {
                aMSHSegment = processO01MSHFromUFD();
                HL7Segment aPIDSegment = processO01PIDFromUFD();
                HL7Segment aPV1Segment = processO01PV1FromUFD();
                HL7Group aReqDetsGroup = processO01ReqDets_FromUFD();
                aOutMess.append(aMSHSegment);
                aOutMess.append(aPIDSegment);
                aOutMess.append(aPV1Segment);
                aOutMess.append(aReqDetsGroup);
            }
        }
        aDDSMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
        aDDSMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
        aDDSMessageArray[2] = aOutMess.getMessage();
        return aDDSMessageArray;
    }
//--------------------------------------------------------------------------
    public HL7Segment processO01MSHFromUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        aOutMSHSegment.linkTo(aInMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);

//        String aAssAuthNameSpcIDRegex = "11111|70080|70146|70149|70179|70211|70278|70292|73399|74299|88888";
//        String aPID3PatIDAssAuth = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, 1);
//        int aClientNum = 0;
//        try {
//            aClientNum = Integer.parseInt(aPID3PatIDAssAuth);
//        } catch (Exception e) {
//            aClientNum = 0;
//        }
//        if (aPID3PatIDAssAuth.matches(aAssAuthNameSpcIDRegex)) {
//            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-EXTN");
//            mSReg1 = "-EXTN";
//            mSReg2 = "EXTN";
//        } else if (aClientNum == 9 || (aClientNum >= 60000 && aClientNum < 65000)) {
//            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-CGMC");
//            mSReg1 = "-CGMC";
//            mSReg2 = "CGMC";
//        } else if (aClientNum == 12) {
//            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-SDMH");
//            mSReg1 = "-SDMH";
//            mSReg2 = "SDMH";
//        } else {
//            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-ALF");
//            mSReg1 = "";
//            mSReg2 = "ALF";
//        }
        aOutMSHSegment.copy(HL7_23.MSH_3_sending_application);
        aOutMSHSegment.copy(HL7_23.MSH_4_sending_facility);
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "PSCRIBE");
        aOutMSHSegment.copy(HL7_23.MSH_6_receiving_facility);
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        aOutMSHSegment.copy(HL7_23.MSH_9_message_type);
        aOutMSHSegment.copy(HL7_23.MSH_10_message_control_ID);
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");
        return aOutMSHSegment;
    }
//--------------------------------------------------------------------------
    public HL7Segment processO01PIDFromUFD() {
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
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
        //aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, mSReg2, 1);
        aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, 1);
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
    public HL7Segment processO01PV1FromUFD() {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            String aPatientClass = aInPV1Segment.get(HL7_23.PV1_2_patient_class);
            if (aPatientClass.equalsIgnoreCase("PO")) {
                aOutPV1Segment.set(HL7_23.PV1_2_patient_class, "W");
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);
            }
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location);
            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);

            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "01");
        }
        return aOutPV1Segment;
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
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            aOutORCSegment = new HL7Segment("ORC");
            aOutORCSegment.linkTo(aInORCSegment);
            aOutORCSegment.copy(HL7_23.ORC_1_order_control);
            aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
            aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
            String aORC1OrderControl = aInORCSegment.get(HL7_23.ORC_1_order_control);
            String aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2);
            String aOBR4UniversalServiceID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
            if (aOBR4UniversalServiceID.matches("7[0-9]{5}0")) {
                mSReg3 = aOBR4UniversalServiceID.substring(1, 6);
            } else {
                mSReg3 = aOBR4UniversalServiceID;
            }

            if (mEnvironment.indexOf("TEST") >= 0) {
                mSReg4 = aOBR21Filler.substring(2, 4) + "Z" + aOBR21Filler.substring(5, 12);
                aOBR21Filler =  mSReg4 + "-" + mSReg3;
            } else {
                try {
                    aOBR21Filler = aOBR21Filler.substring(2,12) + "-" + mSReg3;
                } catch (Exception e) {
                    aOBR21Filler = aOBR21Filler + "-" + mSReg3;
                }
            }

            if (aORC1OrderControl.equalsIgnoreCase("OC")) {
                aOutORCSegment.set(HL7_23.ORC_1_order_control, "CA");
            }
            if (aORC1OrderControl.equalsIgnoreCase("SC")) {
                aOutORCSegment.set(HL7_23.ORC_5_order_status, "CM");
            } else {
                aOutORCSegment.copy(HL7_23.ORC_5_order_status);
            }
            aOutORCSegment.set(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID, aOBR21Filler);
            aOutReqDetsGroup.append(aOutORCSegment);

            //process OBR
            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);

            String aPlacerOrderNum = aInORCSegment.get(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
            if (aPlacerOrderNum.length() == 0) {
                String aORC3FillerOrderNum = aInORCSegment.get(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
                aOutOBRSegment.set(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID, aORC3FillerOrderNum);
            } else {
                aOutOBRSegment.set(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID, aPlacerOrderNum);
            }

            //aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            //aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID, aOBR21Filler);
            String aUnivServText = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            String aCodingScheme = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme);
            String aUnivServIDCode = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, aUnivServText);
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text, aCodingScheme);
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, aCodingScheme);
            aOutOBRSegment.set(HL7_23.OBR_13_Relevant_Clinical_Information, aUnivServIDCode);
            //aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID);
            //aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);

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
            if (Integer.parseInt(aOBR4UniversalServiceID) >= 7200420 &&
                    Integer.parseInt(aOBR4UniversalServiceID) <= 7200450) {
                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RF");
            } else if (Integer.parseInt(aOBR4UniversalServiceID) >= 7550030 &&
                    Integer.parseInt(aOBR4UniversalServiceID) <= 7550680) {
                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RF");
            } else {
                try {
                    aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2,4);
                } catch (Exception e) {
                    aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2);
                }
                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, aOBR21Filler);
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
            aOutReqDetsGroup.append(aOutOBRSegment);

            //process NTE
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

            //process OBX
            HL7Group aOutOBXGroup = new HL7Group("");
            int aOBXSegmentCount = aInReqDetsGroup.countSegments(HL7_23.OBX);
            for (int k = 1; k <= aOBXSegmentCount; k++) {
                HL7Segment aInOBXSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBX", k));
                aOutOBXSegment = new HL7Segment("OBX");
                aOutOBXSegment.linkTo(aInOBXSegment);
                aOutOBXSegment.copy(HL7_23.OBX_1_set_ID);
                aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
                aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier);
                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "");

                int aOBX5FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                for (int m = 1; m <= aOBX5FieldCount; m++) {
                    String aObsValue = aInOBXSegment.get(HL7_23.OBX_5_observation_value, m);
                    if (aObsValue.equalsIgnoreCase(".")) {
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, " ", m);
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
}
