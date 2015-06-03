/*
 * XCELERAProcessFromUFD.java
 *
 * Created on 5 January 2007, 15:56
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.dataagility.ICAN.BHLibXCELERA;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 *
 * @author sohn
 */
public class XCELERAProcessFromUFD extends ProcessSegmentsFromUFD {

    /**
     * Class wide HL7 message variable
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";

    /** Creates a new instance of XCELERAProcessFromUFD */
    public XCELERAProcessFromUFD(String pHL7Message, String pEnvironment)  throws ICANException {
        super(pHL7Message);
        mVersion = "a";
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {

        String aXCELERAMessageArray[] = {k.NULL, k.NULL, k.NULL};

        mInHL7Message = new HL7Message(mHL7Message);
        HL7Message aOutMess = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));
        HL7Segment aOBRSegment = new HL7Segment(mInHL7Message.getSegment("OBR"));

        if(mInHL7Message.isEvent("O01") &&
                aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("CERNERPM") > 0 &&
                aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "DC")) {
            //Cardiology ORM messages from CERNER accepted
            HL7Message aOutMessage = new HL7Message("");
            aOutMessage.append(processMSHFromUFD(aMSHSegment));
            aOutMessage.append(processPIDFromUFD(aMSHSegment));
            aOutMessage.append(processPV1FromUFD(aMSHSegment));
            aOutMessage.append(processORCFromUFD());
            aOutMessage.append(processOBRFromUFD());

            aXCELERAMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aXCELERAMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aXCELERAMessageArray[2] = aOutMessage.getMessage();
        }
        return aXCELERAMessageArray;
    }

    public HL7Segment processMSHFromUFD(HL7Segment pSegment) {
        HL7Segment aOutSegment = new HL7Segment("MSH");
        aOutSegment.linkTo(pSegment);
        aOutSegment.copy(HL7_23.MSH_2_encoding_characters);
        aOutSegment.copy(HL7_23.MSH_3_sending_application);
        aOutSegment.copy(HL7_23.MSH_4_sending_facility);
        if(pSegment.hasValue(HL7_23.MSH_4_sending_facility, "CGMC")) {
            aOutSegment.set(HL7_23.MSH_6_receiving_facility, "CGMC");
        } else {
            aOutSegment.set(HL7_23.MSH_6_receiving_facility, "ALF");
        }
        aOutSegment.set(HL7_23.MSH_5_receiving_application, "CMS");
        aOutSegment.set(HL7_23.MSH_7_message_date_time, pSegment.getDateTime());
        aOutSegment.copy(HL7_23.MSH_9_message_type);
        aOutSegment.copy(HL7_23.MSH_10_message_control_ID);
        aOutSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutSegment.set(HL7_23.MSH_11_processing_ID, "2.3");
        aOutSegment.copy("MSH_14");
        return aOutSegment;
    }

    public HL7Segment processPIDFromUFD(HL7Segment pMSHSegment) {
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        HL7Segment aOutSegment = new HL7Segment("PID");
        aOutSegment.linkTo(aInPIDSegment);
        aOutSegment.copy(HL7_23.PID_1_set_ID);
        if(pMSHSegment.hasValue(HL7_23.MSH_4_sending_facility, "CGMC")) {
            aOutSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "CGMC");
            aOutSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "CGMC", 1);
        } else {
            aOutSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "ALF");
            aOutSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "ALF", 1);
        }
        int aCount = aInPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for(int i=1; i<=aCount; i++) {
            String aIDTypeCode = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i);
            if(aIDTypeCode.matches("MR") || aIDTypeCode.matches("PI")) {
                aOutSegment.move(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1, HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                break;
            }
        }

        aCount = aInPIDSegment.countRepeatFields(HL7_23.PID_5_patient_name);
        for (int i = 1; i <= aCount; i++) {
            String aNameType = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, i);
            if(aNameType.matches("L")) {
                aOutSegment.move(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1, HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, i);
                aOutSegment.move(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1, HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, i);
                aOutSegment.move(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, 1, HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, i);
                aOutSegment.move(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, 1, HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, i);
                break;
            }
        }

        aOutSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutSegment.copy(HL7_23.PID_8_sex);
        aOutSegment.copy(HL7_23.PID_10_race);

        aOutSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, "\"\"");
        aOutSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country, 1);
        aOutSegment.move(HL7_23.PID_11_patient_address, HL7_23.XAD_county_parish, 1, HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);

        aOutSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        aOutSegment.copy(HL7_23.PID_15_language, 1);

        String aVisitNumber = aInPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
        aOutSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_ID_number, aVisitNumber);

        String aSSNNum = aInPIDSegment.get(HL7_23.PID_19_SSN_number);
        if(aSSNNum.length() > 10) {
            aSSNNum = aSSNNum.substring(0, 10);
            aOutSegment.set(HL7_23.PID_19_SSN_number, aSSNNum);
        } else {
            aOutSegment.copy(HL7_23.PID_19_SSN_number);
        }

        aOutSegment.copy(HL7_23.PID_29_patient_death_date_time);

        return aOutSegment;
    }

    public HL7Segment processPV1FromUFD(HL7Segment pMSHSegment) {
        CodeLookUp aLU = new CodeLookUp("SDMH-Dr_Translation.table", mEnvironment);
        HL7Segment aOutSegment = new HL7Segment("");
        if(mInHL7Message.countSegments("PV1") > 1) {
            HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
            aOutSegment = new HL7Segment("PV1");
            aOutSegment.linkTo(aInPV1Segment);
            aOutSegment.copy(HL7_23.PV1_1_set_ID);
            aOutSegment.copy(HL7_23.PV1_2_patient_class);
            aOutSegment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            String aPatientClass = aInPV1Segment.get(HL7_23.PV1_2_patient_class);
            if(aPatientClass.matches("E") || aPatientClass.matches("O")) {
                //Do nothing - no bed assigned for E or O patients
            } else {
                aOutSegment.move(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                aOutSegment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            }
            if(pMSHSegment.hasValue(HL7_23.MSH_4_sending_facility, "CGMC")) {
                aOutSegment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "C");
            } else {
                aOutSegment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "A");
            }
            if(aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name).length() > 0) {
                if(aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num).length() == 0) {
                    //Error - no ID number for doctor
                } else {
                    String aDoctorIDNum = aInPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num);
                    String aTempString = aLU.getValue(aDoctorIDNum);
                    if(!aTempString.matches("NOMATCH")) {
                        aDoctorIDNum = aTempString;
                    }
                    aOutSegment.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, aDoctorIDNum, 1);
                    aOutSegment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name, 1);
                    aOutSegment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_first_name, 1);
                    aOutSegment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_middle_initial_or_name, 1);
                    aOutSegment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_suffix, 1);
                    aOutSegment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_prefix, 1);
                    aOutSegment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_degree, 1);
                    aOutSegment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_code_source_table, 1);
                }
            } else {
                //Add default doctor if none is specified
                if(aPatientClass.matches("E")) {
                    aOutSegment.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, "FM264");
                }
            }

            int aCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_9_consulting_doctor);
            for(int i = 1; i <= aCount && i <= 5; i++) {
                if(aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name).length() > 0) {
                    if(aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num).length() == 0) {
                        //Error - no ID number for doctor
                    } else {
                        String aDoctorIDNum = aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num);
                        String aTempString = aLU.getValue(aDoctorIDNum);
                        if(!aTempString.matches("NOMATCH")) {
                            aDoctorIDNum = aTempString;
                        }
                        aOutSegment.set(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, aDoctorIDNum, i);
                        aOutSegment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i);
                        aOutSegment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_first_name, i);
                        aOutSegment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_middle_initial_or_name, i);
                        aOutSegment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_suffix, i);
                        aOutSegment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_prefix, i);
                        aOutSegment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_degree, i);
                        aOutSegment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_code_source_table, i);
                    }
                }
            }

            aOutSegment.copy(HL7_23.PV1_10_hospital_service);
            aOutSegment.copy(HL7_23.PV1_15_ambulatory_status);
            aOutSegment.copy(HL7_23.PV1_16_VIP_indicator);

            if(aInPV1Segment.get(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_last_name).length() > 0) {
                if(aInPV1Segment.get(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_ID_num).length() == 0) {
                    //Error - no ID number for doctor
                } else {
                    String aDoctorIDNum = aInPV1Segment.get(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_ID_num);
                    String aTempString = aLU.getValue(aDoctorIDNum);
                    if(!aTempString.matches("NOMATCH")) {
                        aDoctorIDNum = aTempString;
                    }
                    aOutSegment.set(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_ID_num, aDoctorIDNum, 1);
                    aOutSegment.copy(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_last_name, 1);
                    aOutSegment.copy(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_first_name, 1);
                    aOutSegment.copy(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_middle_initial_or_name, 1);
                    aOutSegment.copy(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_suffix, 1);
                    aOutSegment.copy(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_prefix, 1);
                    aOutSegment.copy(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_degree, 1);
                    aOutSegment.copy(HL7_23.PV1_17_admitting_doctor, HL7_23.XCN_code_source_table, 1);
                }
            }

            aOutSegment.copy(HL7_23.PV1_18_patient_type);
            aOutSegment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);

            aCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_20_financial_class);
            CodeLookUp aLUFinClass = new CodeLookUp("OCF_PayClass_XCELERA.table", mEnvironment);
            for(int i = 1; i <= aCount && i <= 5; i++) {
                String aFinClass = aInPV1Segment.get(HL7_23.PV1_20_financial_class, i);
                aFinClass = aLUFinClass.getValue(aFinClass);
                aOutSegment.set(HL7_23.PV1_20_financial_class, aFinClass, i);
            }

            aOutSegment.copy(HL7_23.PV1_36_discharge_disposition);
            aOutSegment.copy(HL7_23.PV1_37_discharged_to_location);
            aOutSegment.copy(HL7_23.PV1_41_account_status);
            aOutSegment.copy(HL7_23.PV1_44_admit_date_time);
            aOutSegment.copy(HL7_23.PV1_45_discharge_date_time);
        }
        return aOutSegment;
    }

    public HL7Segment processORCFromUFD() {
        HL7Segment aOutSegment = new HL7Segment("ORC");
        HL7Segment aInORCSegment = new HL7Segment(mInHL7Message.getSegment("ORC"));
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        aOutSegment.linkTo(aInORCSegment);
        aOutSegment.copy(HL7_23.ORC_1_order_control);
        aOutSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
        aOutSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_2_namespace_ID);
        aOutSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
        aOutSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID);
        aOutSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
        aOutSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_2_namespace_ID);
        aOutSegment.copy(HL7_23.ORC_5_order_status);
        aOutSegment.copy(HL7_23.ORC_9_date_time_of_trans);

        aOutSegment.set(HL7_23.ORC_10_entered_by, HL7_23.XCN_ID_num, "oruWARD");
        aOutSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_last_name);
        aOutSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_first_name);
        aOutSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_middle_initial_or_name);
        aOutSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_suffix);
        aOutSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_prefix);
        aOutSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_degree);
        aOutSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_code_source_table);
        aOutSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_assigning_authority);

        aOutSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_ID_num);
        aOutSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_first_name);
        aOutSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_middle_initial_or_name);
        aOutSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_suffix);
        aOutSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_prefix);
        aOutSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_degree);
        aOutSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_code_source_table);
        aOutSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_assigning_authority);

        aOutSegment.copy(HL7_23.ORC_16_order_control_code_reason, HL7_23.CE_ID_code);
        aOutSegment.copy(HL7_23.ORC_16_order_control_code_reason, HL7_23.CE_text);

        if(aInORCSegment.get(HL7_23.ORC_13_enterers_location, HL7_23.PL_point_of_care_nu).length() == 0) {
            String aPointOfCare = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            aOutSegment.set(HL7_23.ORC_13_enterers_location, HL7_23.PL_point_of_care_nu, aPointOfCare);
        } else {
            aOutSegment.copy(HL7_23.ORC_13_enterers_location, HL7_23.PL_point_of_care_nu);
        }

        return aOutSegment;
    }

    public HL7Segment processOBRFromUFD() {
        HL7Segment aOutSegment = new HL7Segment("OBR");
        HL7Segment aInOBRSegment = new HL7Segment(mInHL7Message.getSegment("OBR"));

        aOutSegment.linkTo(aInOBRSegment);

        aOutSegment.copy(HL7_23.OBR_1_Set_ID);
        aOutSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
        aOutSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);

        aOutSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
        aOutSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID);

        aOutSegment.copy(HL7_23.OBR_4_Universal_Service_ID);
        aOutSegment.set(HL7_23.OBR_10_collector_ID, HL7_23.XCN_ID_num, "HIS");

        aOutSegment.copy(HL7_23.OBR_11_Specimen_Action_Code);
        aOutSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);
        aOutSegment.copy(HL7_23.OBR_14_Specimen_Received_Date_Time);
        aOutSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.CE_ID_code);
        aOutSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.CE_text);
        aOutSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.CE_alternate_text);

        int aCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
        for(int i = 1; i <= aCount; i++) {
            aOutSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num, i);
            aOutSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name, i);
            aOutSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name, i);
            aOutSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name, i);
            aOutSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_suffix, i);
            aOutSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_prefix, i);
            aOutSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_degree, i);
            aOutSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_code_source_table, i);
        }

        aOutSegment.copy(HL7_23.OBR_18_Placers_Field_1);
        aOutSegment.copy(HL7_23.OBR_19_Placers_Field_2);
        aOutSegment.copy(HL7_23.OBR_20_Fillers_Field_1);
        aOutSegment.copy(HL7_23.OBR_21_Fillers_Field_2);
        aOutSegment.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);

        CodeLookUp aLUPriority = new CodeLookUp("OCF_Priorities_XCELERA.table", mEnvironment);
        String aPriority = aLUPriority.getValue(aInOBRSegment.get(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority));
        aOutSegment.set(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, aPriority);
        aOutSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time);
        aOutSegment.copy(HL7_23.OBR_30_Transportation_Mode);

        aCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
        for(int i = 1; i <= aCount; i++) {
            aOutSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, i);
        }

        return aOutSegment;
    }
}
