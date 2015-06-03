/*
 * CERNER_PROMEDProcessFromUFD.java
 *
 * Created on 12 January, 15:30
 *
 */

package com.dataagility.ICAN.BHLibCERNER;

import com.dataagility.ICAN.BHLibClasses.*;

import java.text.*;
import java.util.*;

/**
 *
 * @author sohn
 */
public class CERNER_GERISProcessFromUFD_ORIG extends ProcessSegmentsFromUFD {

    BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public HL7Message mInHL7Message;
    public String mHospitalPrefix = "";

    public String mURLString = "";
    public String mURLStringFinal = "";

    public String mFilmBagString = "";
    public String mFilmBagStringFinal = "";

    public String mReportString = "";

    public String mFacilitySuffix = "";

    public String mSReg2 = "";
    public String mSReg3 = "";
    public String mSReg4 = "";

    /**
     * Creates a new instance of CERNER_CARDIOProcessFromUFD
     */
    public CERNER_GERISProcessFromUFD_ORIG(String pHL7Message, String pEnvironment)  throws ICANException {
        super(pHL7Message);
        mVersion = "a";    // Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {
        String aCERNERMessageArray[] = {k.NULL, k.NULL, k.NULL, k.NULL, k.NULL};

        String aSegment;
        HL7Group aGroup;
        mInHL7Message = new HL7Message(mHL7Message);

        HL7Message aOutMess1= new HL7Message(k.NULL);
        HL7Message aOutMess2 = new HL7Message(k.NULL);
        HL7Message aOutMess3 = new HL7Message(k.NULL);

        HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));
        HL7Segment aPIDSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PID));
        HL7Segment aORCSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.ORC));
        HL7Segment aOBRSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.OBR));
        mUR = aPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
        mHospitalPrefix = aMSHSegment.get(HL7_23.MSH_4_sending_facility).substring(0,1);

        if (mHospitalPrefix.matches("A")) {
            mFacilitySuffix = "-ALF";
        } else if (mHospitalPrefix.matches("C")) {
            mFacilitySuffix = "-CGMC";
        } else if (mHospitalPrefix.matches("S")) {
            mFacilitySuffix = "-SDMH";
        }

        // Results .... ----------------------------------------------------------------
        if (mInHL7Message.isEvent("R01")) {
            if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("GERIS") >= 0) {
                //aOutMess1= processORU_FromUFD();
                aOutMess2= processURL_FromUFD();
                aOutMess3= processFILMBAG_FromUFD();
            }

            aCERNERMessageArray[0] = aMSHSegment.get(HL7_23.MSH_3_sending_application);
            aCERNERMessageArray[1] = aMSHSegment.get(HL7_23.MSH_4_sending_facility);
//            aCERNERMessageArray[2] = aOutMess1.getMessage();
//            aCERNERMessageArray[3] = aOutMess2.getMessage();
//            aCERNERMessageArray[4] = aOutMess3.getMessage();
            aCERNERMessageArray[2] = aOutMess2.getMessage();
            aCERNERMessageArray[3] = aOutMess3.getMessage();

            // Orders .... -----------------------------------------------------------------
        } else if (mInHL7Message.isEvent("O01")) {
            if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("GERIS") >= 0) {
                aOutMess1= processORM_FromUFD();
                if (hasValue(HL7_23.ORC_1_order_control,"SC")) {
                    aOutMess2= processURL_FromUFD();
                    aOutMess3= processFILMBAG_FromUFD();
                }
            }

            aCERNERMessageArray[0] = aMSHSegment.get(HL7_23.MSH_3_sending_application);
            aCERNERMessageArray[1] = aMSHSegment.get(HL7_23.MSH_4_sending_facility);
            aCERNERMessageArray[2] = aOutMess1.getMessage();
            aCERNERMessageArray[3] = aOutMess2.getMessage();
            aCERNERMessageArray[4] = aOutMess3.getMessage();
        }

        return aCERNERMessageArray;
    }

    public HL7Message processORM_FromUFD()  throws ICANException {

        HL7Message aOutMessage = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = processMSHFromUFD("CERNERPM");
        HL7Segment aPIDSegment = processPIDFromUFD();
        HL7Group aNTEGroup = processNTEs_FromUFD();
        HL7Group aReqDetsGroup = processORMReqDets_FromUFD(aPIDSegment);

        aMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS" + mFacilitySuffix);

        aOutMessage.append(aMSHSegment);
        aOutMessage.append(aPIDSegment);
        aOutMessage.append(aNTEGroup);
        aOutMessage.append(processPV1FromUFD());
        aOutMessage.append(aReqDetsGroup);
        return aOutMessage;
    }

    public HL7Message processORU_FromUFD()  throws ICANException {

        HL7Message aOutMessage = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = processMSHFromUFD("CERNERPM");
        HL7Segment aPIDSegment = processPIDFromUFD();
        HL7Group aNTEGroup = processNTEs_FromUFD();
        HL7Group aReqDetsGroup = processORUReqDets_FromUFD(aPIDSegment);

        aMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS" + mFacilitySuffix);

        aOutMessage.append(aMSHSegment);
        aOutMessage.append(aPIDSegment);
        aOutMessage.append(aNTEGroup);
        aOutMessage.append(processPV1FromUFD());
        aOutMessage.append(aReqDetsGroup);
        return aOutMessage;
    }

    public HL7Segment processMSHFromUFD(String pReceivingApplication) throws ICANException {
// Non copy fields are

        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);

        HL7Segment aMSHSegIN = new HL7Segment(NULL);
        HL7Segment aMSHSegOUT = new HL7Segment("MSH");

        aMSHSegIN.setSegment(aHL7Message.getSegment(HL7_24.MSH));
        mHL7Segment = aMSHSegIN;                    // In case any of the "do" functions need to see the segment
        mFacility = aMSHSegIN.get(HL7_24.MSH_4_sending_facility);
        mHospitalPrefix = aMSHSegIN.get(HL7_23.MSH_4_sending_facility).substring(0, 1);

        if (mFacility.length() > 0) {
            mHospitalID = mFacility.substring(mFacility.length()-1, mFacility.length());
        }
        mHL7MessageEvent = aMSHSegIN.get(HL7_23.MSH_9_2_trigger_event);

// Initialze aMSHSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.MSH_2_encoding_characters,
            HL7_23.MSH_3_sending_application,
            HL7_23.MSH_4_sending_facility,
            HL7_23.MSH_6_receiving_facility,
            HL7_23.MSH_9_message_type,
            HL7_23.MSH_10_message_control_ID,
            HL7_23.MSH_11_processing_ID
        };

// Initialze OUT with those fields that are straight copies
        aMSHSegOUT.linkTo(aMSHSegIN);
        aMSHSegOUT.copyFields(aCopyFields);

        aMSHSegOUT.set(HL7_23.MSH_5_receiving_application, pReceivingApplication);
        aMSHSegOUT.set(HL7_23.MSH_7_message_date_time, aMSHSegIN.getDateTime());
        aMSHSegOUT.copy(HL7_23.MSH_9_2_trigger_event);
        aMSHSegOUT.set(HL7_23.MSH_12_version_ID, "2.3");

        return (aMSHSegOUT);
    }

    public HL7Segment processPIDFromUFD() {

        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");


//
// Locate and copy the Patient UR.
        int aPID3FieldCount = aInPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3FieldCount; i++) {
            String aPatientIDTypeCode = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i);
            if (aPatientIDTypeCode.equalsIgnoreCase("PI")) {
                String aPIDIDNum = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                int aPIDIDNumLength = aPIDIDNum.length();
                if (aPIDIDNumLength < 8) {
                    aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                } else {
                    aPIDIDNum = aPIDIDNum.substring(aPIDIDNumLength - 7,  aPIDIDNumLength);
                    aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPIDIDNum);
                }
                aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, i);
                mUR = aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number);
            }
        }
//
// We are only interested in the 1st instance of the name ....
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
        aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, "L", 1);
        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_geographic_designation, 1);
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_18_account_number);

        return aOutPIDSegment;
    }

    public HL7Group processNTEs_FromUFD() {
        HL7Group aOutPD1NTEGroup = new HL7Group("");
        HL7Segment aOutPD1NTESegment = new HL7Segment("");
        HL7Segment aInPD1NTESegment = new HL7Segment("");
        HL7Group aPD1NTEGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_PD1Notes, 1));
        int aPD1NTESegmentCount = aPD1NTEGroup.countSegments("NTE");

        for (int i = 1; i <= aPD1NTESegmentCount; i++) {
            aInPD1NTESegment = new HL7Segment(aPD1NTEGroup.getSegment("NTE", i));
            aOutPD1NTESegment = new HL7Segment("NTE");
            aOutPD1NTESegment.linkTo(aInPD1NTESegment);
            aOutPD1NTESegment.copy(HL7_23.NTE_1_setID);
            aOutPD1NTESegment.copy(HL7_23.NTE_2_source_of_comment);
            aOutPD1NTESegment.copy(HL7_23.NTE_3_comment);
            aOutPD1NTEGroup.append(aOutPD1NTESegment);
        }
        return aOutPD1NTEGroup;
    }

    public HL7Segment processPV1FromUFD() {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.set(HL7_23.PV1_1_set_ID, "1");
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);

            String aPatientClass = aInPV1Segment.get(HL7_23.PV1_2_patient_class);

            //aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room);
            //ALF, AC centre building location logic
            CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
            String aACWard = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            String aACBuilding = "";
            aACWard = aLU_WARD.getValue(aACWard);
            if (aACWard.length() > 0) {
                aACBuilding = "AC";
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aACWard);
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            }

            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_building, aACBuilding);

            //aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            if (mHospitalPrefix.startsWith("C")) {
                aOutPV1Segment.set(HL7_23.PV1_4_admission_type, "CGMC");
            } else if (mHospitalPrefix.startsWith("S")) {
                aOutPV1Segment.set(HL7_23.PV1_4_admission_type, "SDMH");
            }

            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.copy(HL7_23.PV1_18_patient_type);

            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number);

//            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
//
            if (mHospitalPrefix.equalsIgnoreCase("A")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "ALF");
                if (aPatientClass.equalsIgnoreCase("I")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CSC");
                } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CERNER");
                } else {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CERNER");
                }
            } else if (mHospitalPrefix.equalsIgnoreCase("S")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "SDMH");
                if (aPatientClass.equalsIgnoreCase("I")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CSC");
                } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CERNER");
                } else {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CERNER");
                }
            } else if (mHospitalPrefix.equalsIgnoreCase("C")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "CGMC");
                if (aPatientClass.equalsIgnoreCase("I")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-CSC");
                } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-CERNER");
                } else {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-CERNER");
                }
            }

            aOutPV1Segment.copy(HL7_23.PV1_41_account_status);
            aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
            aOutPV1Segment.copy(HL7_23.PV1_45_discharge_date_time);

            if (aOutPV1Segment.get(HL7_23.PV1_45_discharge_date_time).length() > 0) {
                aOutPV1Segment.set(HL7_23.PV1_41_account_status, "D");
            }

        }
        return aOutPV1Segment;
    }

// Process Request Details (i.e ORC, ORM, NTE's and OBX's) for ORU (Result/Report) messages
    public HL7Group processORUReqDets_FromUFD(HL7Segment pPIDSegment) {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Group aOutOBRNTEGroup = new HL7Group("");
        HL7Group aOutOBXNTEGroup = new HL7Group("");
        HL7Segment aOutOBXSegment = new HL7Segment("");
        String aOBR4_UniversalServiceID = "";

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            HL7Group aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

//process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            if (aInORCSegment.getSegment().length() > 0) {
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                aOutORCSegment.copy(HL7_23.ORC_1_order_control);
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
                HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID, "RA");
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
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_ID_num);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_last_name);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_first_name);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_middle_initial_or_name);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_suffix);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_prefix);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_degree);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_code_source_table);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_assigning_authority);
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
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID, "RA");
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            aOutOBRSegment.move(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID, "PARIS");
            aOBR4_UniversalServiceID = aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID);
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

//            if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MA")) {
//                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MBO");
//            } else if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RA")) {
//                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
//            } else {
//                aOutOBRSegment.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
//            }

            aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");

            aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                //aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
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

//
//START ======================== process OBX and OBXNTEs
            HL7Group aOutOBXGroup = new HL7Group("");
            HL7Segment aInSegment = new HL7Segment("");
            int aGroupSegmentCount = aInReqDetsGroup.countSegments();
            boolean aSkipSegment = false;
            int aInsertBlankCount = 0;
            int aOBXSegID = 1;
            int k = 0;
            while (k <= aGroupSegmentCount || aInsertBlankCount != 0) {
                if (aInsertBlankCount == 0) {    // Don't need the next segment yet we will insert a blank one instead.
                    aInSegment = new HL7Segment(aInReqDetsGroup.getNextSegment());
                    k++;
                }

                if (aSkipSegment == true && aInsertBlankCount == 0) {     // The next segment is to be discarded
                    aSkipSegment = false;
                } else {
//                    display("++++" + aInSegment.getSegmentID());
                    if (aInSegment.getSegmentID().equalsIgnoreCase("ORC") ||
                            aInSegment.getSegmentID().equalsIgnoreCase("OBR") ) {
                        // Skip OBR and ORC ... already dealt with.

                    } else if (aInSegment.getSegmentID().equalsIgnoreCase("OBX")) {

                        aOutOBXSegment = new HL7Segment("OBX");
                        aOutOBXSegment.linkTo(aInSegment);
                        aOutOBXSegment.set(HL7_23.OBX_1_set_ID, String.valueOf(aOBXSegID++));
                        aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
//                        aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code);
//                        aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text);
//                        aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme);
//                        aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID);
                        aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, aOBR4_UniversalServiceID);
                        aOutOBXSegment.copy(HL7_23.OBX_4_observation_sub_ID);


                        // Note:- Following uses "$ $" to insert a space because the set command trims spaces.
                        // ... further down, when we append segment, we then replace "$ $" by a single space char.
                        int aOBX5FieldCount = aInSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                        for (int m = 1; m <= aOBX5FieldCount; m++) {
                            String aObsValue = aInSegment.get(HL7_23.OBX_5_observation_value, m);

                            if (aObsValue.equalsIgnoreCase(".")) {  // If value only contains a "." then insert a blank line
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "$ $", m);

                            } else if (aInsertBlankCount > 0) {      // Force a blank line
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "$ $", m);
                                aInsertBlankCount--;

                            } else {                                // Process the normal conents of the OBX value
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, aObsValue, m);

                                // Check if this is the last required line of a Radiology report section.
                                if (aObsValue.indexOf("Reported by:") >= 0 && aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"DC"))  {
                                    aSkipSegment = true;     // Skip the line after "Reported By" ....
                                    aInsertBlankCount = 2;     // ... and insert two blank lines.
                                }
                            }
                        }

                        String aOBX6Unit = aInSegment.get(HL7_23.OBX_6_units);
                        aOBX6Unit = replaceInStr(aOBX6Unit, "^", "\\S\\");
                        aOutOBXSegment.set(HL7_23.OBX_6_units, aOBX6Unit);
                        aOutOBXSegment.copy(HL7_23.OBX_7_references_range);

                        int aOBX8FieldCount = aInSegment.countRepeatFields(HL7_23.OBX_8_abnormal_flags);
                        int n = 1;
                        do {
                            String aAbnormalFlag = aInSegment.get(HL7_23.OBX_8_abnormal_flags, n);
                            if (aAbnormalFlag.length() > 0) {
                                aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                            } else {
                                String aObservResultsStatus = aInSegment.get(HL7_23.OBX_11_observ_results_status, n);
                                if (aObservResultsStatus.equalsIgnoreCase("C")) {
                                    aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, "N", n);
                                } else {
                                    aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                                }
                            }
                            n++;
                        } while (n <= aOBX8FieldCount);

                        aOutOBXSegment.copy(HL7_23.OBX_11_observ_results_status);
                        aOutOBXSegment.copy(HL7_23.OBX_12_date_last_obs_normal_values);
                        aOutOBXSegment.copy(HL7_23.OBX_14_date_time_of_the_observation);
                        aOutOBXSegment.copy(HL7_23.OBX_15_producers_ID, HL7_23.CE_ID_code);
                        aOutOBXSegment.copy(HL7_23.OBX_16_responsible_observer);

                        aOutOBXGroup.append(cleanoutStr(replaceInStr(aOutOBXSegment.getSegment(), "$ $", " ")));

//process OBX NTEs
                    } else if (aInSegment.getSegmentID().equalsIgnoreCase("NTE")) {
                        HL7Segment aOutOBXNTESegment = new HL7Segment("NTE");
                        aOutOBXNTESegment.linkTo(aInSegment);
                        aOutOBXNTESegment.copy(HL7_23.NTE_1_setID);
//                       aOutOBXNTESegment.copy(HL7_23.NTE_2_source_of_comment);
                        aOutOBXNTESegment.copy(HL7_23.NTE_3_comment);
                        aOutOBXGroup.append(cleanoutStr(aOutOBXNTESegment.getSegment()));
                    }
                }
            }
//END ======================== process OBX and OBXNTEs
            aOutReqDetsGroup.append(aOutOBXGroup);
        }
        return aOutReqDetsGroup;
    }

// Method to do a sub-string replacement.
// Note:- Replaces str.replaceAll ... which was not happy replacing "^" by "\\S\\"
    public String replaceInStr(String pSrc, String pFrom, String pTo) {
        int aPos;

        aPos = pSrc.indexOf(pFrom);
        if (aPos >= 0) {
            pSrc = pSrc.substring(0, aPos) + pTo + pSrc.substring(aPos + pFrom.length());
        }
        return pSrc;

    }

// Method to remove any data between "{" and "}"
// Note:- Replaces str.replaceAll ... which was not happy replacing "^" by "\\S\\"
    public String cleanoutStr(String pSrc) {
        int aPos1;
        int aPos2;

        aPos1 = pSrc.indexOf("{");
        aPos2 = pSrc.indexOf("}");
        while (aPos1 >= 0 && aPos2 >= 0) {
            pSrc = pSrc.substring(0, aPos1) + pSrc.substring(aPos2+1);
            aPos1 = pSrc.indexOf("{");
            aPos2 = pSrc.indexOf("}");
        }

        return pSrc;

    }

    public HL7Group processORMReqDets_FromUFD(HL7Segment pPIDSegment) {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Group aOutOBRNTEGroup = new HL7Group("");
        HL7Group aOutOBXNTEGroup = new HL7Group("");
        HL7Segment aOutOBXSegment = new HL7Segment("");
        String aOBR4_UniversalServiceID = "";
        //String aSReg1 = "";
        //String aSReg2 = "";
        //String aSReg3 = "";

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            if (aInORCSegment.getSegment().length() > 0) {
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                String aPlacerOrderNum = aInORCSegment.get(HL7_23.ORC_2_placer_order_num);
                String aOrderControl = aInORCSegment.get(HL7_23.ORC_1_order_control);
                if (aPlacerOrderNum.length() == 0) {
                    if (aOrderControl.equalsIgnoreCase("NW")) {
                        aOutORCSegment.set(HL7_23.ORC_1_order_control, "SN");
                    } else {
                        aOutORCSegment.copy(HL7_23.ORC_1_order_control);
                    }
                } else {
                    aOutORCSegment.copy(HL7_23.ORC_1_order_control);
                }
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID, "RA");
                aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_2_namespace_ID);
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
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_ID_num);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_last_name);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_first_name);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_middle_initial_or_name);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_suffix);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_prefix);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_degree);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_code_source_table);
//                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_13_enterers_location);
                aOutORCSegment.copy(HL7_23.ORC_16_order_control_code_reason);
                aOutReqDetsGroup.append(aOutORCSegment);
            }

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID, "RA");
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            aOutOBRSegment.move(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID, "PARIS");
            aOBR4_UniversalServiceID = aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID);
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

            if (aInOBRSegment.hasValue(HL7_23.OBR_3_Filler_Order_Number,HL7_23.EI_2_namespace_ID, "MA")) {
                aOutOBRSegment.move(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method,
                        HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code);
            } else {
                aOutOBRSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code);
                aOutOBRSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text);
                aOutOBRSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method);
            }
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
            aOutOBRSegment.copy(HL7_23.OBR_23_Charge_To_Practice);

//            if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MA")) {
//                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MBO");
//            } else if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RA")) {
//                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
//            } else {
//                aOutOBRSegment.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
//            }

            aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");

            aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_29_Parent);
            if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RA") &&
                    aInORCSegment.hasValue(HL7_23.ORC_1_order_control, "NW") &&
                    aInOBRSegment.isEmpty(HL7_23.OBR_30_Transportation_Mode)) {
                aOutOBRSegment.set(HL7_23.OBR_30_Transportation_Mode, "WK");

            } else {
                aOutOBRSegment.copy(HL7_23.OBR_30_Transportation_Mode);
            }

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
            int aOBXSegmentID = 1;
            int aOBXSegmentCount = aInReqDetsGroup.countSegments(HL7_23.OBX);
            for (int k = 1; k <= aOBXSegmentCount; k++) {
                HL7Segment aInSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBX", k));
                aOutOBXSegment = new HL7Segment("OBX");
                aOutOBXSegment.linkTo(aInSegment);
                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(aOBXSegmentID++));
                aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
                //aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier);
                aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, aOBR4_UniversalServiceID);
                aOutOBXSegment.copy(HL7_23.OBX_4_observation_sub_ID);

                int aOBX5FieldCount = aInSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                for (int m = 1; m <= aOBX5FieldCount; m++) {
                    aOutOBXSegment.copy(HL7_23.OBX_5_observation_value, m);
                }
                String aOBX6Unit = aInSegment.get(HL7_23.OBX_6_units);
                aOBX6Unit = replaceInStr(aOBX6Unit, "^", "\\S\\");

                aOutOBXSegment.set(HL7_23.OBX_6_units, aOBX6Unit);
                aOutOBXSegment.copy(HL7_23.OBX_7_references_range);

                int aOBX8FieldCount = aInSegment.countRepeatFields(HL7_23.OBX_8_abnormal_flags);
                for (int n = 1; n <= aOBX8FieldCount; n++) {
                    String aAbnormalFlag = aInSegment.get(HL7_23.OBX_8_abnormal_flags);
                    aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                }

                aOutOBXSegment.copy(HL7_23.OBX_11_observ_results_status);
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

//            if (aInOBRSegment.hasValue(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID, "DC") &&
//                    aInORCSegment.hasValue(HL7_23.ORC_1_order_control, "NW")) {
//                aOutOBXSegment = new HL7Segment("OBX");
//                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(aOBXSegmentID++));
//                aOutOBXSegment.set(HL7_23.OBX_2_value_type, "NM");
//                aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, "FUTUREORDER");
//                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "0", 1);
//                aOutOBXGroup.append(aOutOBXSegment);
//            }

            aOutReqDetsGroup.append(aOutOBXGroup);
        }
        return aOutReqDetsGroup;
    }

// Create a FILMBAG message for PARIS or NMHL7
    public HL7Message processFILMBAG_FromUFD()  throws ICANException {
        HL7Message aMess = new HL7Message(k.NULL);
        HL7Segment aPIDSegment = processPIDFromUFD();
        HL7Segment aMSHSegment = processIMAGEMSH_FromUFD();
//
// Create Message 2 (i.e. FILMBAG)
        aMess.append(aMSHSegment);
        aMess.append(processPIDFromUFD());
        aMess.append(processNTEs_FromUFD());
        aMess.append(processFILMBAGPV1_FromUFD(aPIDSegment));
        aMess.append(processFILMBAGReqDets_FromUFD(aPIDSegment, aMSHSegment));

        return aMess;
    }

// Create a URL message for PARIS only.
    public HL7Message processURL_FromUFD()  throws ICANException {
        HL7Message aMess = new HL7Message(k.NULL);
        HL7Segment aPIDSegment = processPIDFromUFD();
        HL7Segment aMSHSegment = processIMAGEMSH_FromUFD();
//
// Create Message 3 (i.e. URL)
        aMess.append(aMSHSegment);
        aMess.append(processPIDFromUFD());
        aMess.append(processNTEs_FromUFD());
        aMess.append(processURLPV1FromUFD());
        aMess.append(processURLReqDets_FromUFD(aPIDSegment, aMSHSegment));

        return aMess;
    }

//    public HL7Segment processURLMSH_FromUFD() {
//        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
//        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
//        aOutMSHSegment.linkTo(aInMSHSegment);
//        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
//        aOutMSHSegment.copy(HL7_23.MSH_3_sending_application);
//        aOutMSHSegment.copy(HL7_23.MSH_4_sending_facility);
//        aOutMSHSegment.copy(HL7_23.MSH_6_receiving_facility);
//        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "CERNERPM");
//        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
//        aOutMSHSegment.set(HL7_23.MSH_9_1_message_type, "ORU");
//        aOutMSHSegment.set(HL7_23.MSH_9_2_trigger_event, "R01");
//        aOutMSHSegment.copy(HL7_23.MSH_10_message_control_ID);
//        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
//        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");
//        return aOutMSHSegment;
//    }

    public HL7Segment processIMAGEMSH_FromUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        mHospitalPrefix = aInMSHSegment.get(HL7_23.MSH_4_sending_facility).substring(0, 1);
        //CodeLookUp aLU = new CodeLookUp("PACS_URL.table", mEnvironment);
        //String mSReg2 = aLU.getValue("URL");
        aOutMSHSegment.linkTo(aInMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        if (mHospitalPrefix.equalsIgnoreCase("A")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-ALF");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "ALF");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "ALF");
            //String mSReg4 = "";
        } else if (mHospitalPrefix.equalsIgnoreCase("C")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-CGMC");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "CGMC");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "CGMC");
            //String mSReg4 = "-CGMC";
        } else {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-SDMH");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "SDMH");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "SDMH");
            //String mSReg4 = "-SDMH";
        }
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "CERNERPM");
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        //aOutMSHSegment.copy(HL7_23.MSH_9_message_type);
        aOutMSHSegment.set(HL7_23.MSH_9_1_message_type, "ORU");
        aOutMSHSegment.set(HL7_23.MSH_9_2_trigger_event, "R01");
        String aMessageCtrlID = "FB-".concat(aInMSHSegment.get(HL7_23.MSH_10_message_control_ID));
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aMessageCtrlID);
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");

        return aOutMSHSegment;
    }

    public HL7Segment processFILMBAGPV1_FromUFD(HL7Segment pPIDSegment) {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));

        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);

            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.set(HL7_23.PV1_2_patient_class, "O");

            if (mHospitalPrefix.equalsIgnoreCase("A")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "ALF");
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CERNER");
            } else if (mHospitalPrefix.equalsIgnoreCase("C")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "CGMC");
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-CERNER");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "SDMH");
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CERNER");
            }
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "IMAGE");
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "01");
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.set(HL7_23.PV1_18_patient_type, "P");
            String aVisitNum = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1).concat("-FILMBAG");
            aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNum);

            Format aDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date aDate = new Date();
            String aResult = aDateTimeFormat.format(aDate);
            aOutPV1Segment.set(HL7_23.PV1_44_admit_date_time, aResult);
            aOutPV1Segment.set(HL7_23.PV1_45_discharge_date_time, aResult);
            aOutPV1Segment.set(HL7_23.PV1_41_account_status, "D");
        }
        return aOutPV1Segment;
    }

    public HL7Segment processURLPV1FromUFD() {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);

            String aACWard = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            String aACBed = aACWard + "_" + aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
            aACWard = aLU_WARD.getValue(aACWard);
            if (aACWard.length() > 0) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aACWard);
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_building, "AC");
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            }

            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "01");
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID);
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority);
            aOutPV1Segment.copy(HL7_23.PV1_18_patient_type);
            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
            aOutPV1Segment.copy(HL7_23.PV1_41_account_status);
            aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
            aOutPV1Segment.copy(HL7_23.PV1_45_discharge_date_time);

            if (aOutPV1Segment.get(HL7_23.PV1_45_discharge_date_time).length() > 0) {
                aOutPV1Segment.set(HL7_23.PV1_41_account_status, "D");
            }
        }
        return aOutPV1Segment;
    }

    public HL7Group processFILMBAGReqDets_FromUFD(HL7Segment pPIDSegment, HL7Segment pMSHSegment) {

        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Group aOutOBRNTEGroup = new HL7Group("");
        HL7Group aOutOBXNTEGroup = new HL7Group("");
        HL7Segment aOutOBXSegment = new HL7Segment("");
        String aFillerOrderNum = "";
        String aSReg1 = "";
        String aSReg2 = "";
        String aSReg3 = "";
        CodeLookUp aLU = new CodeLookUp("GERISPACS_URL.table", mEnvironment);

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            if (aInORCSegment.getSegment().length() > 0) {
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                aOutORCSegment.set(HL7_23.ORC_1_order_control, "RE");
                aFillerOrderNum = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1).concat("_FILMBAG");
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID, aFillerOrderNum);
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID, "RA");
                aOutORCSegment.set(HL7_23.ORC_5_order_status, "IP");
                aOutORCSegment.copy(HL7_23.ORC_9_date_time_of_trans);
                aOutReqDetsGroup.append(aOutORCSegment);
            }

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
            aFillerOrderNum = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1).concat("_FILMBAG");
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID, aFillerOrderNum);
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID, "RA");
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, "FILMBAGD");
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text, "FILMBAGD");
//            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, "FILMBAG");
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, "All Images Filmbag");
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID, "PARIS");
            aOutOBRSegment.copy(HL7_23.OBR_7_Observation_Date_Time);
            aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID);

            aOutOBRSegment.copy(HL7_23.OBR_14_Specimen_Received_Date_Time);
            aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, 1);
            aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);

            if (aInOBRSegment.isEmpty(HL7_23.OBR_20_Fillers_Field_1)) {
                String aFillersField1 = aInOBRSegment.get(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID).concat("_").concat(aInOBRSegment.get(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID).concat("_FILMBAG"));
                aOutOBRSegment.set(HL7_23.OBR_20_Fillers_Field_1, aFillersField1);
            } else {
                aOutOBRSegment.copy(HL7_23.OBR_20_Fillers_Field_1);
            }
            aOutOBRSegment.set(HL7_23.OBR_21_Fillers_Field_2, aFillerOrderNum);
            aOutOBRSegment.set(HL7_23.OBR_22_Results_RPT_Status_Change, pMSHSegment.get(HL7_23.MSH_7_message_date_time));
            aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
            if (aInOBRSegment.get(HL7_23.OBR_25_Results_Status).length() == 0) {
                aOutOBRSegment.set(HL7_23.OBR_25_Results_Status, "P");
            } else {
                aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);
            }

            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                //aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
            }
            aOutOBRSegment.copy(HL7_23.OBR_29_Parent);
            aOutOBRSegment.copy(HL7_23.OBR_30_Transportation_Mode);

            aOutReqDetsGroup.append(aOutOBRSegment);

            //process IMAGE URL insertion
            HL7Segment aImageOBXSegment1 = new HL7Segment("OBX");
            aImageOBXSegment1.set(HL7_23.OBX_1_set_ID, "1");
            aImageOBXSegment1.set(HL7_23.OBX_2_value_type, "TX");
            String aUniversalServiceID = aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID);
            aImageOBXSegment1.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aUniversalServiceID);
            String aUniversalServiceIDText = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            aSReg2 = aLU.getValue("REPORT");
            aImageOBXSegment1.set(HL7_23.OBX_5_observation_value, aSReg2, 1);

            aOutReqDetsGroup.append(aImageOBXSegment1);

            HL7Segment aImageOBXSegment2 = new HL7Segment("OBX");
            aImageOBXSegment2.set(HL7_23.OBX_1_set_ID, "2");
            aImageOBXSegment2.set(HL7_23.OBX_2_value_type, "RP");
            aImageOBXSegment2.set(HL7_23.OBX_4_observation_sub_ID, "1");
            aSReg2 = aLU.getValue("FILMBAG");
            if (aSReg2.indexOf("pat_id") >= 0) {
                aSReg3 = aSReg2.concat(pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number,  1)).concat(mSReg4);
                if (mHospitalPrefix.matches("S")) {
                    aSReg3 = aSReg3.concat("-SDMH");
                }
            } else {
                aSReg3 = aSReg2.concat(aOutOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2, 12));
            }
            aImageOBXSegment2.set(HL7_23.OBX_5_observation_value, aSReg3.concat("^IMAGEURL^IMAGE"), 1);
            aImageOBXSegment2.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID));

            aOutReqDetsGroup.append(aImageOBXSegment2);
        }
        return aOutReqDetsGroup;
    }

    public HL7Group processURLReqDets_FromUFD(HL7Segment pPIDSegment, HL7Segment pMSHSegment) {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Group aOutOBRNTEGroup = new HL7Group("");
        HL7Group aOutOBXNTEGroup = new HL7Group("");
        HL7Segment aOutOBXSegment = new HL7Segment("");
        //String aSReg1 = "";
        //String aSReg2 = "";
        //String aSReg3 = "";

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            if (aInORCSegment.getSegment().length() > 0) {
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                aOutORCSegment.set(HL7_23.ORC_1_order_control, "RE");
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
                //aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_2_namespace_ID);
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
                aOutORCSegment.copy(HL7_23.ORC_16_order_control_code_reason);
                aOutReqDetsGroup.append(aOutORCSegment);
            }

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            //aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID);
            aOutOBRSegment.move(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID, "PARIS");
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
            aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_19_Placers_Field_2);
            aOutOBRSegment.copy(HL7_23.OBR_20_Fillers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_21_Fillers_Field_2);
            aOutOBRSegment.set(HL7_23.OBR_22_Results_RPT_Status_Change, pMSHSegment.get(HL7_23.MSH_7_message_date_time) + "00");
            aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
            if (aInOBRSegment.get(HL7_23.OBR_25_Results_Status).length() == 0) {
                aOutOBRSegment.set(HL7_23.OBR_25_Results_Status, "P");
            } else {
                aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);
            }
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_29_Parent);
            aOutOBRSegment.copy(HL7_23.OBR_30_Transportation_Mode);

            int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
            for (int j = 1; j <= aReasonForStudyCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
            }

            aOutOBRSegment.set(HL7_23.OBR_32_Principal_Result_Interpreter, "R375RAD");

            int aTechnicianCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_34_technician);
            for (int j = 1; j <= aTechnicianCount; j++) {
                String aTechnician = aInOBRSegment.get(HL7_23.OBR_34_technician);
                aOutOBRSegment.set(HL7_23.OBR_34_technician, aTechnician, j);
            }

            aOutReqDetsGroup.append(aOutOBRSegment);

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
                aOutOBXSegment.move(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme, HL7_23.OBX_3_observation_identifier, HL7_23.CE_text);
                aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID, "PARIS");
                aOutOBXSegment.copy(HL7_23.OBX_4_observation_sub_ID);

                int aOBX5FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                for (int m = 1; m <= aOBX5FieldCount; m++) {
                    aOutOBXSegment.copy(HL7_23.OBX_5_observation_value, m);
                }
                String aOBX6Unit = aInOBXSegment.get(HL7_23.OBX_6_units);
                aOBX6Unit = replaceInStr(aOBX6Unit, "^", "\\S\\");

                aOutOBXSegment.set(HL7_23.OBX_6_units, aOBX6Unit);
                aOutOBXSegment.copy(HL7_23.OBX_7_references_range);

                int aOBX8FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_8_abnormal_flags);
                for (int n = 1; n <= aOBX8FieldCount; n++) {
                    aOutOBXSegment.copy(HL7_23.OBX_8_abnormal_flags, n);
                }

                aOutOBXSegment.copy(HL7_23.OBX_11_observ_results_status);
                aOutOBXSegment.copy(HL7_23.OBX_14_date_time_of_the_observation);
                aOutOBXSegment.copy(HL7_23.OBX_15_producers_ID, HL7_23.CE_ID_code);
                aOutOBXSegment.copy(HL7_23.OBX_16_responsible_observer);

                aOutOBXGroup.append(aOutOBXSegment);

            }
            //process URL pointer if report carries an image
            String aDiagServiceSecID = aInOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            if (aDiagServiceSecID.equalsIgnoreCase("XX") && aOBXSegmentCount == 0) {
                aOutOBXSegment = new HL7Segment("OBX");
                CodeLookUp aLU = new CodeLookUp("GERISPACS_URL.table", mEnvironment);
                mSReg2 = aLU.getValue("REPORT");
                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, "1");
                aOutOBXSegment.set(HL7_23.OBX_2_value_type, "TX");
                aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID));
                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, mSReg2);
                aOutOBXGroup.append(aOutOBXSegment);
            }
            HL7Segment aInOBRSegmentTemp = new HL7Segment(aOutReqDetsGroup.getSegment("OBR", 1));
            String aStartDateTime = aInOBRSegmentTemp.get(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time);

            aOutOBXSegment = new HL7Segment("OBX");
            CodeLookUp aLU = new CodeLookUp("GERISPACS_URL.table", mEnvironment);
            mSReg2 = aLU.getValue("URL");
            aOutOBXSegment.set(HL7_23.OBX_1_set_ID, "1");
            aOutOBXSegment.set(HL7_23.OBX_2_value_type, "RP");
            aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID));
            aOutOBXSegment.set(HL7_23.OBX_4_observation_sub_ID, "1");

            if (mSReg2.indexOf("pid") >= 0) {
                String aHospID = pMSHSegment.get(HL7_23.MSH_4_sending_facility).substring(0, 1);
                if (aHospID.equalsIgnoreCase("C")) {
                    mSReg4 = "-CGMC";
                } else if (aHospID.equalsIgnoreCase("S")) {
                    mSReg4 = "-SDMH";
                } else {
                    mSReg4 = "";
                }
                mSReg3 = mSReg2.concat(pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1)).concat(mSReg4);
            } else {
                //mSReg3 = mSReg2.concat(aOutOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2, 12));
                mSReg3 = mSReg2.concat(aOutOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2));
            }
            aOutOBXSegment.set(HL7_23.OBX_5_observation_value, mSReg3.concat("^IMAGEURL^IMAGE"), 1);
            aOutOBXGroup.append(aOutOBXSegment);

            aOutReqDetsGroup.append(aOutOBXGroup);
        }
        return aOutReqDetsGroup;
    }
}
