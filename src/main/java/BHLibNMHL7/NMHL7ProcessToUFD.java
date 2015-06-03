/*
 * NMHL7ProcessToUFD.java
 *
 * Created on 5 October 2005, 14:34
 *
 */

package BHLibNMHL7;

import BHLibClasses.*;

/**
 * NMHL7ProcessToUFD provides methods to process a message from NMHL7 to UFD
 * structure.  These methods are NMHL7 specific.
 * @author Ray Fillingham and Norman Soh
 */
public class NMHL7ProcessToUFD extends ProcessSegmentsToUFD {
    /**
     * Class wide HL7Message object
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";
    public String mSReg4 = "";
    /**
     * Creates a new instance of NMHL7ProcessToUFD
     * @param pHL7Message HL7 Message string
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public NMHL7ProcessToUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";    // NMHL7ProcessToUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------
    /**
     * processMessage will convert / process the entire NMHL7 message into a
     * UFD HL7 message structure
     * @return Returns a UFD HL7 message
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public String processMessage() throws ICANException {

        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));

        if (mInHL7Message.isEvent("R01")) {
            HL7Segment aMSHSegment = processMSHToUFD();
            aMSHSegment = processR01MSHToUFD(aMSHSegment);
            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(processPIDToUFD());
            aOutHL7Message.append(processPV1ToUFD());
            aOutHL7Message.append(processOrderObservations_ToUFD());
        }
        if (aOutHL7Message.getMessage().length() > 0) {
            aOutHL7Message.append(setupZBX("MESSAGE", "SOURCE_ID", aInMSHSegment.get(HL7_23.MSH_10_message_control_ID)));
        }
        return aOutHL7Message.getMessage();
    }
    //--------------------------------------------------------------------------
    public HL7Segment processR01MSHToUFD(HL7Segment pMSHSegment) {
        HL7Segment aOutMSHSegment = new HL7Segment("");
        aOutMSHSegment = pMSHSegment;
        String aSendingApp = aOutMSHSegment.get(HL7_23.MSH_3_sending_application);
        String aRecApp = "DGATE";
        if (aSendingApp.equalsIgnoreCase("CERNERPM")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "NMHL7");
            aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, aRecApp);
        }
        String mHospitalPrefix = pMSHSegment.get(HL7_23.MSH_4_sending_facility);
        if (mHospitalPrefix.length() > 0) {
            mHospitalPrefix = mHospitalPrefix.substring(0, 1);
        }
        if (mHospitalPrefix.equalsIgnoreCase("A")) {
            mSReg4 = "";
        } else if (mHospitalPrefix.equalsIgnoreCase("C")) {
            mSReg4 = "-CGMC";
        } else {
            mSReg4 = "-SDMH";
        }
        return aOutMSHSegment;
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
//        String aGroupID[] = HL7_23.Group_Orders;
//        aGroupCount = mInHL7Message.countGroups(aGroupID);
//
//        for (aCount = 1; aCount <= aGroupCount; aCount++) {
//            aOutOrderObservationsGroup.append(mInHL7Message.getGroup(aGroupID, aCount));
//        }
//        return aOutOrderObservationsGroup;
//    }
    public HL7Group processOrderObservations_ToUFD() {
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Group aOutOBRNTEGroup = new HL7Group("");
        HL7Group aOutOBXNTEGroup = new HL7Group("");
        HL7Segment aOutOBXSegment = new HL7Segment("");
        String aSReg1 = "";
        String aSReg2 = "";
        String aSReg3 = "";
        String aSReg4 = "";

        CodeLookUp aLU = new CodeLookUp("PACS_URL.table", mEnvironment);

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            if (aInORCSegment.getSegment().length() > 0) {
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                aOutORCSegment.copy(HL7_23.ORC_1_order_control);
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
                HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
                String aFillerOrderNum = aInOBRSegment.get(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID, aFillerOrderNum);
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID, "NM");
                aOutORCSegment.copy(HL7_23.ORC_5_order_status);
                aOutORCSegment.copy(HL7_23.ORC_9_date_time_of_trans);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_13_enterers_location);
                aOutReqDetsGroup.append(aOutORCSegment);
            }

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID, "NM");
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme);
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID, "NMHL7");
            aOutOBRSegment.copy(HL7_23.OBR_7_Observation_Date_Time);

            int aCollectorIDCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_10_collector_ID);
            for (int j = 1; j <= aCollectorIDCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_ID_num, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_last_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_first_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_middle_initial_or_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_suffix, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_prefix, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_degree, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_code_source_table, j);
            }
            aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_text);
            aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);
            aOutOBRSegment.copy(HL7_23.OBR_14_Specimen_Received_Date_Time);
            String aSpecimenCode = aInOBRSegment.get(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code);
            String aSpecimenText = aInOBRSegment.get(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text);
            String aCollectionMethod = aInOBRSegment.get(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method);
            aOutOBRSegment.set(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code, aSpecimenCode);
            aOutOBRSegment.set(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text, aSpecimenText);
            aOutOBRSegment.set(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method, aCollectionMethod);

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
            aOutOBRSegment.copy(HL7_23.OBR_19_Placers_Field_2);
            aOutOBRSegment.copy(HL7_23.OBR_20_Fillers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_21_Fillers_Field_2);
            aOutOBRSegment.copy(HL7_23.OBR_22_Results_RPT_Status_Change);
//            aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
//            aOutOBRSegment.set(HL7_23.OBR_25_Results_Status, "F");
            aOutOBRSegment.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);

            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                //aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                //aOutOBRSegment.set(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, "RT", j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_29_Parent);
            aOutOBRSegment.copy(HL7_23.OBR_30_Transportation_Mode);

            int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
            for (int j = 1; j <= aReasonForStudyCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_32_Principal_Result_Interpreter);

            int aTechnicianCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_34_technician);
            for (int j = 1; j <= aTechnicianCount; j++) {
                String aTechnician = aInOBRSegment.get(HL7_23.OBR_34_technician);
                aOutOBRSegment.set(HL7_23.OBR_34_technician, aTechnician, j);
            }

            aOutReqDetsGroup.append(aOutOBRSegment);

            //process OBR NTEs
            aOutOBRNTEGroup = new HL7Group("");
            HL7Group aInOBRNTEGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_OBRNotes, i));
            int aOBRNTESegmentCount = aInOBRNTEGroup.countSegments(HL7_23.NTE);
            for (int j = 1; j <= aOBRNTESegmentCount; j++) {
                HL7Segment aInOBRNTESegment = new HL7Segment(aInOBRNTEGroup.getSegment("NTE", j));
                HL7Segment aOutOBRNTESegment = new HL7Segment("NTE");
                aOutOBRNTESegment.linkTo(aInOBRNTESegment);
                aOutOBRNTESegment.copy(HL7_23.NTE_1_setID);
                aOutOBRNTESegment.copy(HL7_23.NTE_2_source_of_comment);
                aOutOBRNTESegment.copy(HL7_23.NTE_3_comment);
                aOutOBRNTEGroup.append(aOutOBRNTESegment);
            }
            aOutReqDetsGroup.append(aOutOBRNTEGroup);

            //process OBX and OBXNTEs
            HL7Group aOutOBXGroup = new HL7Group("");
            int aOBXSegmentCount = aInReqDetsGroup.countSegments(HL7_23.OBX);
            for (int k = 1; k <= aOBXSegmentCount; k++) {
                HL7Segment aInOBXSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBX", k));
                aOutOBXSegment = new HL7Segment("OBX");
                aOutOBXSegment.linkTo(aInOBXSegment);
                aOutOBXSegment.copy(HL7_23.OBX_1_set_ID);
                aSReg1 = aOutOBXSegment.get(HL7_23.OBX_1_set_ID);
                aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
                aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code);
                aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text);
                aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme);
                aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID, "NMHL7");

                int aOBX5FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                for (int m = 1; m <= aOBX5FieldCount; m++) {
                    String aObsValue = aInOBXSegment.get(HL7_23.OBX_5_observation_value, m);
                    if (aObsValue.equalsIgnoreCase(".")) {
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, " ", m);
                    } else {
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, aObsValue, m);
                    }
                }
                String aOBX6Unit = aInOBXSegment.get(HL7_23.OBX_6_units);
                aOBX6Unit = aOBX6Unit.replaceAll("\\^", "/S/");
                aOutOBXSegment.set(HL7_23.OBX_6_units, aOBX6Unit);
                aOutOBXSegment.copy(HL7_23.OBX_7_references_range);

                int aOBX8FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_8_abnormal_flags);
                for (int n = 1; n <= aOBX8FieldCount; n++) {
                    String aAbnormalFlag = aInOBXSegment.get(HL7_23.OBX_8_abnormal_flags);
                    if (aAbnormalFlag.length() > 0) {
                        aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                    } else {
                        String aObservResultsStatus = aInOBXSegment.get(HL7_23.OBX_11_observ_results_status);
                        if (aObservResultsStatus.equalsIgnoreCase("C")) {
                            aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, "N", n);
                        } else {
                            aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                        }
                    }
                }

                aOutOBXSegment.copy(HL7_23.OBX_11_observ_results_status);
                aOutOBXSegment.copy(HL7_23.OBX_12_date_last_obs_normal_values);
                aOutOBXSegment.copy(HL7_23.OBX_14_date_time_of_the_observation);
                aOutOBXSegment.copy(HL7_23.OBX_15_producers_ID, HL7_23.CE_ID_code);
                aOutOBXSegment.copy(HL7_23.OBX_16_responsible_observer);

                aOutOBXGroup.append(aOutOBXSegment);

                //process OBX NTEs
                aOutOBXNTEGroup = new HL7Group("");
                HL7Group aInOBXNTEGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Observation_Details, i));
                int aOBXNTESegmentCount = aInOBXNTEGroup.countSegments(HL7_23.NTE);
                for (int j = 1; j <= aOBXNTESegmentCount; j++) {
                    HL7Segment aInOBXNTESegment = new HL7Segment(aInOBXNTEGroup.getSegment("NTE", j));
                    HL7Segment aOutOBXNTESegment = new HL7Segment("NTE");
                    aOutOBXNTESegment.linkTo(aInOBXNTESegment);
                    aOutOBXNTESegment.copy(HL7_23.NTE_1_setID);
                    aOutOBXNTESegment.copy(HL7_23.NTE_3_comment);
                    aOutOBXNTEGroup.append(aOutOBXNTESegment);
                }
                aOutOBXGroup.append(aOutOBXNTEGroup);
            }
            //process URL pointer if report carries an image
//            if (aInOBRSegment.get(HL7_23.OBR_5_Priority).equalsIgnoreCase("IMAGE")) {
//                HL7Segment aImageOBXSegment = new HL7Segment("OBX");
//                int aOBXSetID = Integer.parseInt(aSReg1);
//                aSReg1 = Integer.toString(++aOBXSetID);
//                aImageOBXSegment.set(HL7_23.OBX_1_set_ID, aSReg1);
//                aImageOBXSegment.set(HL7_23.OBX_2_value_type, "RP");
//                String aUniversalServiceID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
//                aImageOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aUniversalServiceID);
//                String aUniversalServiceIDText = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
//                aImageOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text, aUniversalServiceIDText);
//                String aUniversalServiceIDCodeScheme = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme);
//                aImageOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme, aUniversalServiceIDCodeScheme);
//                aImageOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID, "NMHL7");
//                aImageOBXSegment.set(HL7_23.OBX_4_observation_sub_ID, "1");
//                aSReg2 = aLU.getValue("URL");
//                if (aSReg2.indexOf("pat_id") >= 0) {
//                    aSReg3 = aSReg2.concat(aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number,  1)).concat(mSReg4);
//                } else {
//                    aSReg3 = aSReg2.concat(aInOBRSegment.get(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID).substring(0, 11));
//                }
//                aImageOBXSegment.set(HL7_23.OBX_5_observation_value, aSReg3.concat("^IMAGEURL^IMAGE"), 1);
//                aOutOBXGroup.append(aImageOBXSegment);
//            }
            aOutReqDetsGroup.append(aOutOBXGroup);
        }
        return aOutReqDetsGroup;
    }
}
