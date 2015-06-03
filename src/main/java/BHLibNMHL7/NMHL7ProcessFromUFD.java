/*
 * NMHL7ProcessFromUFD.java
 *
 * Created on 11 October 2005, 17:04
 *
 */

package BHLibNMHL7;

import BHLibClasses.*;
import java.text.*;

/**
 * NMHL7ProcessFromUFD contains the methods required to build a NMHL7 message
 * from a UFD HL7 message structure
 * @author Ray Fillingham and Norman Soh
 */
public class NMHL7ProcessFromUFD extends ProcessSegmentsFromUFD {
    /**
     * Constant class
     */
    public BHConstants k = new BHConstants();
    /**
     * Class wide HL7 message object
     */
    public HL7Message mInHL7Message;
    /**
     * Class wide variable
     */
    public String mSReg1 = "";
    /**
     * Class wide variable
     */
    public String mSReg2 = "";
    /**
     * Class wide variable
     */
    public String mSReg3 = "";
    /**
     * This constructor creates a new instance of NMHL7ProcessFromUFD passing a HL7 UFD
     * message structure
     * @param pHL7Message HL7 message text string
     * @throws BHLibClasses.ICANException ICANException
     */
    public NMHL7ProcessFromUFD(String pHL7Message) throws ICANException {
        super(pHL7Message);
        mHL7Message = pHL7Message;
    }
    //--------------------------------------------------------------------------
    /**
     * This method contains the methods required to build a NMHL7 HL7 message
     * @return NMHL7 HL7 message text string
     * @throws BHLibClasses.ICANException ICANException
     */
    public String[] processMessage() throws ICANException {
        mVersion = "A";
        String aNMHL7MessageArray[] = {k.NULL, k.NULL, k.NULL};
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Message aOutHL7Message = new HL7Message("");
        HL7Segment aOBRSegment = new HL7Segment(mInHL7Message.getSegment("OBR", 1));

        if (mInHL7Message.isEvent("O01")) {
            if (aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "NM")) {
                HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
                HL7Segment aPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
                HL7Segment aPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
                HL7Segment aORCSegment = new HL7Segment(mInHL7Message.getSegment("ORC", 1));
                HL7Group aNTEGroup = new HL7Group("");

                aMSHSegment = processNMHL7MSHFromUFD(aMSHSegment);
                aPIDSegment = processNMHL7PIDFromUFD(aPIDSegment);
                aPV1Segment = processNMHL7PV1FromUFD(aPV1Segment);
                aORCSegment = processNMHL7ORCFromUFD(aORCSegment);
                aOBRSegment = processNMHL7OBRFromUFD(aOBRSegment, aORCSegment);
                aNTEGroup = processNMHL7NTEs_FromUFD(aNTEGroup);

                aOutHL7Message.append(aMSHSegment);
                aOutHL7Message.append(aPIDSegment);
                aOutHL7Message.append(aPV1Segment);
                aOutHL7Message.append(aORCSegment);
                aOutHL7Message.append(aOBRSegment);
                aOutHL7Message.append(aNTEGroup);

                aNMHL7MessageArray[0] = aMSHSegment.get(HL7_23.MSH_3_sending_application);
                aNMHL7MessageArray[1] = aMSHSegment.get(HL7_23.MSH_4_sending_facility);
                aNMHL7MessageArray[2] = aOutHL7Message.getMessage();
            }
        }
        return aNMHL7MessageArray;
    }
    //--------------------------------------------------------------------------
    /**
     * NMHL7 specific processing for MSH segment
     * @return MSH segment class object
     * @param pMSHSegment MSH segment
     * @throws BHLibClasses.ICANException ICANException
     */
    public HL7Segment processNMHL7MSHFromUFD(HL7Segment pMSHSegment) throws ICANException {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        String aSendingFac = pMSHSegment.get(HL7_23.MSH_4_sending_facility);
        if (aSendingFac.equalsIgnoreCase("CGMC")) {
            mSReg1 = "";
            mSReg2 = "CGMC";
        } else if (aSendingFac.equalsIgnoreCase("ALF")) {
            mSReg1 = "";
            mSReg2 = "ALF";
        } else {
            mSReg1 = "";
            mSReg2 = "SDMH";
        }
        //mXlateName = "_NMHL7";
        aOutMSHSegment.linkTo(pMSHSegment);

        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        aOutMSHSegment.copy(HL7_23.MSH_3_sending_application);
        aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, mSReg2);
        aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, mSReg2);
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "NMHL7");
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        aOutMSHSegment.copy(HL7_23.MSH_9_message_type);
        aOutMSHSegment.copy(HL7_23.MSH_10_message_control_ID);
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");

        return aOutMSHSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * NMHL7 specific processing for PID segment
     * @param pPIDSegment PID segment
     * @throws BHLibClasses.ICANException ICANException
     * @return PID segment
     */
    public HL7Segment processNMHL7PIDFromUFD(HL7Segment pPIDSegment) throws ICANException {
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        aOutPIDSegment.linkTo(pPIDSegment);

        aOutPIDSegment.copy(HL7_23.PID_1_set_ID);
        int aPID3FieldCount = pPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3FieldCount; i++) {
            String aPID3PatientIDType = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i);
            if (aPID3PatientIDType.equalsIgnoreCase("MR") ||
                    aPID3PatientIDType.equalsIgnoreCase("PI") ||
                    aPID3PatientIDType.length() == 0) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, mSReg2, 1);
                String aPID3PatientID = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                NumberFormat formatter = new DecimalFormat("0000000");
                aPID3PatientID = formatter.format(Integer.parseInt(aPID3PatientID));
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPID3PatientID, 1);
            }
            //copy Medicare number to PID19
            if (aPID3PatientIDType.equalsIgnoreCase("MC")) {
                String aPID3MCIDNum = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                aOutPIDSegment.set(HL7_23.PID_19_SSN_number, aPID3MCIDNum);
            }
        }

        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        //comment out code for test patient if environment is not prod and site suffix is not null

        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, 1);
        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);
        aOutPIDSegment.copy(HL7_23.PID_10_race);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_16_marital_status);
        aOutPIDSegment.copy(HL7_23.PID_23_birth_place);
        aOutPIDSegment.copy(HL7_23.PID_27_veterans_military_status);
        aOutPIDSegment.set(HL7_23.PID_28_nationality, "");

        return aOutPIDSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * NMHL7 specific processing for PV1 segment
     * @param pPV1Segment PV1 segment
     * @throws BHLibClasses.ICANException ICANException
     * @return PV1 segment
     */
    public HL7Segment processNMHL7PV1FromUFD(HL7Segment pPV1Segment) throws ICANException {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        if (pPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(pPV1Segment);

            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);
            String aPatientLocationPOC = pPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            if (aPatientLocationPOC.length() > 0) {
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room);
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID);
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_building);
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_floor);
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_description);
            } else {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "\"\"");
            }
            aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, 1);
            aOutPV1Segment.copy(HL7_23.PV1_8_referring_doctor, 1);
            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            aOutPV1Segment.copy(HL7_23.PV1_15_ambulatory_status, 1);
            aOutPV1Segment.copy(HL7_23.PV1_18_patient_type);
            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number);
            aOutPV1Segment.copy(HL7_23.PV1_20_financial_class, 1);
            aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
            aOutPV1Segment.copy(HL7_23.PV1_45_discharge_date_time);
        }
        return aOutPV1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     * NMHL7 specific processing for ORC segment
     * @param pORCSegment ORC segment
     * @throws BHLibClasses.ICANException ICANException
     * @return ORC segment
     */
    public HL7Segment processNMHL7ORCFromUFD(HL7Segment pORCSegment) throws ICANException {
        HL7Segment aOutORCSegment = new HL7Segment("ORC");
        aOutORCSegment.linkTo(pORCSegment);

        String aOrderControl = pORCSegment.get(HL7_23.ORC_1_order_control);
        if (aOrderControl.equalsIgnoreCase("OC")) {
            aOutORCSegment.set(HL7_23.ORC_1_order_control, "CA");
        } else {
            aOutORCSegment.copy(HL7_23.ORC_1_order_control);
        }

        aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num);
        aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num);

        if (aOrderControl.equalsIgnoreCase("SC")) {
            aOutORCSegment.set(HL7_23.ORC_5_order_status, "CM");
        } else {
            aOutORCSegment.copy(HL7_23.ORC_5_order_status);
        }

        aOutORCSegment.copy(HL7_23.ORC_9_date_time_of_trans);
        aOutORCSegment.copy(HL7_23.ORC_13_enterers_location);


        return aOutORCSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * NMHL7 specific processing for OBR segment
     * @param pOBRSegment OBR segment
     * @param pORCSegment ORC segment
     * @throws BHLibClasses.ICANException ICANException
     * @return OBR segment
     */
    public HL7Segment processNMHL7OBRFromUFD(HL7Segment pOBRSegment, HL7Segment pORCSegment) throws ICANException {
        HL7Segment aOutOBRSegment = new HL7Segment("OBR");
        aOutOBRSegment.linkTo(pOBRSegment);

        aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
        String aPlacerOrderNumEntityID = pOBRSegment.get(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
        mSReg2 = "000000000".concat(aPlacerOrderNumEntityID);
        int aStLen = mSReg2.length();
        int aStEnd = aStLen - 7;
        mSReg3 = mSReg2.substring(aStEnd, aStLen);

        aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
        String aDateTimeTrans = pORCSegment.get(HL7_23.ORC_9_date_time_of_trans);
        aDateTimeTrans = aDateTimeTrans.substring(2, 4);
        String aUniversalServiceCode = pOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
        String aUniversalServiceText = pOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
        String aFillerOrderNumEntityID = "NM".concat(aDateTimeTrans).concat(mSReg3).concat("-").concat(aUniversalServiceCode);
        aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, aFillerOrderNumEntityID);
        aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
        aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text, aUniversalServiceCode);
        aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, aUniversalServiceText);
        aOutOBRSegment.copy(HL7_23.OBR_7_Observation_Date_Time);
        aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, 1);

        int aOBR16FieldCount = pOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
        for (int i = 1; i <= aOBR16FieldCount; i++) {
            aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num, i);
            aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name, i);
            aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name, i);
            aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name, i);
        }

        aOutOBRSegment.copy(HL7_23.OBR_20_Fillers_Field_1);
        aOutOBRSegment.copy(HL7_23.OBR_21_Fillers_Field_2);
        aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);
        aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
        aOutOBRSegment.copy(HL7_23.OBR_22_Results_RPT_Status_Change);

        if (aUniversalServiceCode.startsWith("PET")) {
            aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "PT");
        } else {
            aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "NM");
        }
        aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);

        int aOBR27FieldCount = pOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
        for (int i = 1; i <= aOBR27FieldCount; i++) {
            aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, i);
            aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, i);
            aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, i);
        }

        int aOBR31FieldCount = pOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
        for (int i = 1; i <= aOBR31FieldCount; i++) {
            aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, i);
        }

        aOutOBRSegment.copy(HL7_23.OBR_32_Principal_Result_Interpreter);

        int aOBR34FieldCount = pOBRSegment.countRepeatFields(HL7_23.OBR_34_technician);
        String aTechnician = pOBRSegment.get(HL7_23.OBR_34_technician);
        for (int i = 1; i <= aOBR34FieldCount; i++) {
            aOutOBRSegment.set(HL7_23.OBR_34_technician, aTechnician, i);
        }

        return aOutOBRSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * NMHL7 specific processing for NTE segments
     * @param pNTEGroup NTE group
     * @throws ICANException ICANException
     * @return NTE group
     */
    public HL7Group processNMHL7NTEs_FromUFD(HL7Group pNTEGroup) throws ICANException {
        HL7Group aOutNTEGroup = new HL7Group("");
        int aNTESegmentCount = mInHL7Message.countSegments(HL7_23.NTE);
        HL7Segment aOutNTESegment = new HL7Segment("");
        HL7Segment aInNTESegment = new HL7Segment("");
        for (int i = 1; i <= aNTESegmentCount; i++) {
            aOutNTESegment = new HL7Segment("NTE");
            aInNTESegment.setSegment(mInHL7Message.getSegment("NTE", i));
            aOutNTESegment.linkTo(aInNTESegment);
            aOutNTESegment.copy(HL7_23.NTE_1_setID);
            aOutNTESegment.copy(HL7_23.NTE_2_source_of_comment);
            aOutNTESegment.copy(HL7_23.NTE_3_comment, 1);
            aOutNTEGroup.append(aOutNTESegment);
        }
        return aOutNTEGroup;
    }
}
