/*
 * ProcessSegmentsFromUFD.java
 *
 * Created on 6 October 2005, 15:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.dataagility.ICAN.BHLibClasses;

/**
 * Generic Segment Processing Methods
 * @author r.fillingham
 */
public class ProcessSegmentsFromUFD {

    protected String CARROT_GET = "\\^";
    protected String CARROT_SET = "^";

    protected String NULL = "";
    protected String mHL7Message;
    protected String mHL7MessageEvent = NULL;
    protected HL7Segment mHL7Segment = new HL7Segment(NULL);
    protected String mFacility = "";         // Note of which Campus/Facility we are handling (i.e. "ALF", "CGMC", SDMH").
    protected String mHospitalID = "";       // This is a single char code that is based on the campus
    protected String mUR = "";               // Note of the patient UR ... sometimes used in non PID segments
    protected String mPV1NursingStation = "";               // Note of the patient UR ... sometimes used in non PID segments

    /**
     * The Version Number of the Relesae.
     * * This is passed to MSH_10 Message Control ID by the getVerDateTime() method.
     */
    protected String mVersion = "";
    protected BHConstants k = new BHConstants();
    protected int mOBXSegmentCount = 0;      // Counter used when assembling OBX segments.

    protected boolean mDatagateSynch = true;

    /**
     * Generic Segment Processing Methods
     * @param pHL7Message Initialized with the HL7 message to be processed.
     */
    public ProcessSegmentsFromUFD(String pHL7Message) throws ICANException {
        mHL7Message = pHL7Message;
        mOBXSegmentCount = 0;
        mHL7Segment = new HL7Segment(NULL);
        mFacility = "";
        mHospitalID = "";
    }
    /**
     * Generic processing for an Outgoing Group in ana A17 message.<p>
     * @return Returns the processed HL7 PID group as a HL7Group.
     */
    public HL7Group processA17GroupFromUFD( int pGroupNumber) throws ICANException {

        HL7Message aInMessage = new HL7Message(mHL7Message) ;
        String aGroup = aInMessage.getGroup(HL7_23.Group_A17_Patient, pGroupNumber);
        HL7Group aOutGroup = new HL7Group();

        HL7Segment aOutPID = processPIDFromUFD(aGroup);
        aOutGroup.append(aOutPID.getSegment());

        HL7Segment aOutPV1 = processPV1FromUFD(aGroup);
        aOutGroup.append(aOutPV1.getSegment());

        return aOutGroup;

    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" AL1 group.
     * @return Returns the processed HL7 AL1 group.
     */
    public HL7Group processAL1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aAL1SegIN = new HL7Segment(k.NULL);
        HL7Segment aAL1SegOUT = new HL7Segment("AL1");
        HL7Group aAL1GroupOUT = new HL7Group();
        // Initialze aAL1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.AL1_1_set_ID,
                    HL7_23.AL1_2_allergy_type,
                    HL7_23.AL1_3_allergy,
                    HL7_23.AL1_4_severity,
                    HL7_23.AL1_5_reaction,
                    HL7_23.AL1_6_identification_date
        };
        int aAL1GroupCount = aHL7Message.countSegments("AL1");
        for (int i = 1; i <= aAL1GroupCount; i++) {
            aAL1SegIN.setSegment(aHL7Message.getSegment("AL1", i));
            aAL1SegOUT.linkTo(aAL1SegIN);
            aAL1SegOUT.copyFields(aCopyFields);
            aAL1GroupOUT.append(aAL1SegOUT.getSegment());
        }
        return aAL1GroupOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" DG1 group.
     * @return Returns the processed HL7 DG1 group.
     */
    public HL7Group processDG1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aDG1SegIN = new HL7Segment(k.NULL);
        HL7Segment aDG1SegOUT = new HL7Segment("DG1");
        HL7Group aDG1GroupOUT = new HL7Group();
        // Initialze aDG1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.DG1_1_set_ID,
                    HL7_23.DG1_3_diagnosis_code,
                    HL7_23.DG1_6_diagnosis_DRG_type,
                    HL7_23.DG1_17_diagnosis_classif
        };
        int aDG1GroupCount = aHL7Message.countSegments("DG1");
        for (int i = 1; i <= aDG1GroupCount; i++) {
            aDG1SegIN.setSegment(aHL7Message.getSegment("DG1", i));
            aDG1SegOUT.linkTo(aDG1SegIN);
            aDG1SegOUT.copyFields(aCopyFields);
            aDG1GroupOUT.append(aDG1SegOUT.getSegment());
        }
        return aDG1GroupOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" DRG segment.
     * @return Returns the processed HL7 DRG segment as a String.
     */
    public HL7Segment processDRGFromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aDRGSegIN = new HL7Segment(NULL);
        HL7Segment aDRGSegOUT = new HL7Segment(NULL);

        if (aHL7Message.countSegments(HL7_23.DRG) == 0) {
        } else {
            aDRGSegOUT = new HL7Segment("DRG");
            aDRGSegIN.setSegment(aHL7Message.getSegment(HL7_24.DRG));

// Initialze aDRGSegOUT with those fields that are straight copies
            String aCopyFields[] =  {
                HL7_23.DRG_1_diagnostic_related_group
            };
            aDRGSegOUT.linkTo(aDRGSegIN);
            aDRGSegOUT.copyFields(aCopyFields);
        }
        return aDRGSegOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" EVN segment.
     * @return Returns the processed HL7 EVN segment as a String.
     */
    public HL7Segment processEVNFromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aEVNSegIN = new HL7Segment(NULL);
        HL7Segment aEVNSegOUT = new HL7Segment("EVN");

        aEVNSegIN.setSegment(aHL7Message.getSegment(HL7_24.EVN));

// Initialze aEVNSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.EVN_1_event_type_code,
                    HL7_23.EVN_2_date_time_of_event,
                    HL7_23.EVN_6_event_occurred
        };
        aEVNSegOUT.linkTo(aEVNSegIN);
        aEVNSegOUT.copyFields(aCopyFields);

        return aEVNSegOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" GT1 segment.
     * @return Returns the processed HL7 GT1 segment as a String.
     */
    public HL7Group processGT1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aGT1SegIN = new HL7Segment(NULL);
        HL7Segment aGT1SegOUT = new HL7Segment("GT1");
        HL7Group aGT1GroupOUT = new HL7Group();

        aGT1SegIN.setSegment(aHL7Message.getSegment(HL7_24.GT1));

// Initialze aGT1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.GT1_1_set_ID,
                    HL7_23.GT1_2_guarantor_number,
                    HL7_23.GT1_3_guarantor_name,
                    HL7_23.GT1_5_guarantor_address,
                    HL7_23.GT1_6_guarantor_phone_home,
                    HL7_23.GT1_7_guarantor_phone_business,
                    HL7_23.GT1_10_guarantor_type
        };
        aGT1SegOUT.linkTo(aGT1SegIN);
        aGT1SegOUT.copyFields(aCopyFields);
        int aGT1GroupCount = aHL7Message.countSegments(HL7_24.GT1);
        for (int i = 1; i <= aGT1GroupCount; i++) {
            aGT1SegIN.setSegment(aHL7Message.getSegment("GT1", i));
            aGT1SegOUT.linkTo(aGT1SegIN);
            aGT1SegOUT.copyFields(aCopyFields);
            aGT1GroupOUT.append(aGT1SegOUT.getSegment());
        }
        return aGT1GroupOUT;

    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" IN1 segment.
     * @return Returns the processed HL7 IN1 segment as a String.
     */
    public HL7Segment processIN1FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aIN1SegIN = new HL7Segment(NULL);
        HL7Segment aIN1SegOUT = new HL7Segment(NULL);

        aIN1SegIN.setSegment(aHL7Message.getSegment(HL7_24.IN1));
        if (aIN1SegIN.isEmpty(HL7_23.IN1_2_insurance_plan_ID)) {
            return aIN1SegOUT;
        } else {
            aIN1SegOUT = new HL7Segment("IN1");
        }

// Initialze aIN1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.IN1_1_set_ID,
                    HL7_23.IN1_2_insurance_plan_ID,
                    HL7_23.IN1_3_insurance_co_ID,
                    HL7_23.IN1_4_insurance_co_name,
                    HL7_23.IN1_5_insurance_co_address,
                    HL7_23.IN1_36_policy_number,
        };
        aIN1SegOUT.linkTo(aIN1SegIN);
        aIN1SegOUT.copyFields(aCopyFields);

        return aIN1SegOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" IN2 segment.
     * @return Returns the processed HL7 IN2 segment as a String.
     */
    public HL7Segment processIN2FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aIN2SegIN = new HL7Segment(NULL);
        HL7Segment aIN2SegOUT = new HL7Segment(NULL);

        aIN2SegIN.setSegment(aHL7Message.getSegment(HL7_24.IN2));
        if (aIN2SegIN.isEmpty(HL7_23.IN2_46_job_title)) {
            return aIN2SegOUT;
        } else {
            aIN2SegOUT = new HL7Segment("IN2");
        }


        aIN2SegIN.setSegment(aHL7Message.getSegment(HL7_24.IN2));

// Initialze aIN2SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.IN2_46_job_title,
        };
        aIN2SegOUT.linkTo(aIN2SegIN);
        aIN2SegOUT.copyFields(aCopyFields);

        return aIN2SegOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" MRG segment.
     * @return Returns the processed HL7 MRG segment as a String.
     */
    public HL7Segment processMRGFromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aMRGSegIN = new HL7Segment(NULL);
        HL7Segment aMRGSegOUT = new HL7Segment("MRG");

        aMRGSegIN.setSegment(aHL7Message.getSegment(HL7_24.MRG));

// Initialze aMRGSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.MRG_1_prior_patient_ID_internal,
        };
        aMRGSegOUT.linkTo(aMRGSegIN);
        aMRGSegOUT.copyFields(aCopyFields);

        return aMRGSegOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To" MSH segment.
     * @return Returns the processed HL7 MSH segment as a String.
     */
    public HL7Segment processMSHFromUFD(String pReceivingApplication) throws ICANException {
// Non copy fields are
//       MSH-5-receiving_application = (string-append "CSC-" MSH-4-sending_facility site_suffix)
//       MSH-7-message_date/time = TIME("%Y%m%d%H%M")
//       MSH-9-message_type.MSH-9-2_trigger_event = msg_type
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);

        HL7Segment aMSHSegIN = new HL7Segment(NULL);
        HL7Segment aMSHSegOUT = new HL7Segment("MSH");

        aMSHSegIN.setSegment(aHL7Message.getSegment(HL7_24.MSH));
        mHL7Segment = aMSHSegIN;                    // In case any of the "do" functions need to see the segment
        mFacility = aMSHSegIN.get(HL7_24.MSH_4_sending_facility);
        if (mFacility.length() > 0) {
            mHospitalID = mFacility.substring(mFacility.length()-1, mFacility.length());
        }
        mHL7MessageEvent = aMSHSegIN.get(HL7_23.MSH_9_message_type, "CM_2");

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
    /**
     * Generic processing for an Outgoing i.e "To Vendor" NK1 segment.
     * @return Returns the processed HL7 NK1 segment as a Group.
     */
    public HL7Group processNK1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aNK1SegIN = new HL7Segment(k.NULL);
        HL7Segment aNK1SegOUT = new HL7Segment("NK1");
        HL7Group aNK1GroupOUT = new HL7Group();
        // Initialze aNK1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.NK1_1_set_ID,
                    HL7_23.NK1_2_next_of_kin_name,
                    HL7_23.NK1_3_next_of_kin_relationship,
                    HL7_23.NK1_4_next_of_kin__address,
                    HL7_23.NK1_5_next_of_kin__phone,
                    HL7_23.NK1_6_business_phone_num,
                    HL7_23.NK1_7_contact_role
        };
        int aNK1GroupCount = aHL7Message.countSegments("NK1");
        for (int i = 1; i <= aNK1GroupCount; i++) {
            aNK1SegIN.setSegment(aHL7Message.getSegment("NK1", i));
            aNK1SegOUT.linkTo(aNK1SegIN);
            aNK1SegOUT.copyFields(aCopyFields);
            aNK1GroupOUT.append(aNK1SegOUT.getSegment());
        }
        return aNK1GroupOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" OBX segment.
     * @return Returns the processed HL7 OBX segment as a Group.
     */
    public HL7Group processOBXs_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aOBXSegIN = new HL7Segment(k.NULL);
        HL7Segment aOBXSegOUT = new HL7Segment("OBX");
        HL7Group aOBXGroupOUT = new HL7Group();
        // Initialze aOBXSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.OBX_1_set_ID,
                    HL7_23.OBX_2_value_type,
                    HL7_23.OBX_3_observation_identifier,
                    HL7_23.OBX_4_observation_sub_ID,
                    HL7_23.OBX_5_observation_value,
                    HL7_23.OBX_6_units,
                    HL7_23.OBX_8_abnormal_flags,
                    HL7_23.OBX_9_probability
        };
        int aOBXGroupCount = aHL7Message.countSegments("OBX");
        for (int i = 1; i <= aOBXGroupCount; i++) {
            aOBXSegIN.setSegment(aHL7Message.getSegment("OBX", i));
            aOBXSegOUT.linkTo(aOBXSegIN);
            aOBXSegOUT.copyFields(aCopyFields);
            aOBXGroupOUT.append(aOBXSegOUT.getSegment());
        }
        return aOBXGroupOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" OBR segment.
     * @return Returns the processed HL7 OBR segment as a String.
     */
    public HL7Segment processOBRFromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aOBRSegIN = new HL7Segment(NULL);
        HL7Segment aOBRSegOUT = new HL7Segment("OBR");

        aOBRSegIN.setSegment(aHL7Message.getSegment(HL7_24.OBR));

// Initialze aOBRSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.OBR_1_Set_ID,
                    HL7_23.OBR_2_Placer_Order_Number,
                    HL7_23.OBR_3_Filler_Order_Number,
                    HL7_23.OBR_4_Universal_Service_ID,
                    HL7_23.OBR_5_Priority,
                    HL7_23.OBR_6_Requested_Date_Time,
                    HL7_23.OBR_7_Observation_Date_Time,
                    HL7_23.OBR_8_Observation_End_Date_Time,
                    HL7_23.OBR_9_Collection_Volume,
                    HL7_23.OBR_10_collector_ID,
                    HL7_23.OBR_11_Specimen_Action_Code,
                    HL7_23.OBR_12_Danger_Code,
                    HL7_23.OBR_13_Relevant_Clinical_Information,
                    HL7_23.OBR_14_Specimen_Received_Date_Time,
                    HL7_23.OBR_15_Specimen_Source,
                    HL7_23.OBR_16_Ordering_Provider,
                    HL7_23.OBR_17_Order_Call_Back_Phone_Number,
                    HL7_23.OBR_18_Placers_Field_1,
                    HL7_23.OBR_19_Placers_Field_2,
                    HL7_23.OBR_20_Fillers_Field_1,
                    HL7_23.OBR_21_Fillers_Field_2,
                    HL7_23.OBR_22_Results_RPT_Status_Change,
                    HL7_23.OBR_23_Charge_To_Practice,
                    HL7_23.OBR_24_Diagnostic_Service_Section_ID,
                    HL7_23.OBR_25_Results_Status,
                    HL7_23.OBR_26_parent_result,
                    HL7_23.OBR_27_Quantity_Timing,
                    HL7_23.OBR_28_Results_Copies_To,
                    HL7_23.OBR_29_Parent,
                    HL7_23.OBR_30_Transportation_Mode,
                    HL7_23.OBR_31_Reason_For_Study,
                    HL7_23.OBR_32_Principal_Result_Interpreter,
                    HL7_23.OBR_33_Assistant_Result_Interpreter,
                    HL7_23.OBR_34_technician,
                    HL7_23.OBR_35_transcriptionist,
                    HL7_23.OBR_36_Scheduled_Date_Time,
                    HL7_23.OBR_37_num_sample_containers,
                    HL7_23.OBR_38_transport_logistics_of_sample,
                    HL7_23.OBR_39_collectors_comment,
                    HL7_23.OBR_40_transport_arrange_respons,
                    HL7_23.OBR_41_transport_arranged,
                    HL7_23.OBR_42_escort_reqd,
                    HL7_23.OBR_43_planned_patient_transport_comment
        };
        aOBRSegOUT.linkTo(aOBRSegIN);
        aOBRSegOUT.copyFields(aCopyFields);

        return aOBRSegOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" ORC segment.
     * @return Returns the processed HL7 ORC segment as a String.
     */
    public HL7Segment processORCFromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aORCSegIN = new HL7Segment(NULL);
        HL7Segment aORCSegOUT = new HL7Segment("ORC");

        aORCSegIN.setSegment(aHL7Message.getSegment(HL7_24.ORC));

// Initialze aORCSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.ORC_1_order_control,
                    HL7_23.ORC_2_placer_order_num,
                    HL7_23.ORC_3_filler_order_num,
                    HL7_23.ORC_4_placer_group_num,
                    HL7_23.ORC_5_order_status,
                    HL7_23.ORC_6_response_flag,
                    HL7_23.ORC_7_timing_quantity,
                    HL7_23.ORC_8_parent,
                    HL7_23.ORC_9_date_time_of_trans,
                    HL7_23.ORC_10_entered_by,
                    HL7_23.ORC_11_verified_by,
                    HL7_23.ORC_12_ordering_provider,
                    HL7_23.ORC_13_enterers_location,
                    HL7_23.ORC_14_call_back_phone_number,
                    HL7_23.ORC_15_order_effective_date_time,
                    HL7_23.ORC_16_order_control_code_reason,
                    HL7_23.ORC_17_entering_organization,
                    HL7_23.ORC_18_entering_device,
                    HL7_23.ORC_19_action_by
        };
        aORCSegOUT.linkTo(aORCSegIN);
        aORCSegOUT.copyFields(aCopyFields);

        return aORCSegOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To" PID segment.
     * @return Returns the processed HL7 PID segment as a String.
     */
    public HL7Segment processPIDFromUFD() throws ICANException {
        return (this.processPIDFromUFD(mHL7Message));
    }
    public HL7Segment processPIDFromUFD(String pHL7MessageBlock) throws ICANException {
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);

        HL7Segment aPIDSegIN = new HL7Segment(NULL);
        HL7Segment aPIDSegOUT = new HL7Segment("PID");

        aPIDSegIN.setSegment(aHL7Message.getSegment(HL7_24.PID));
        mHL7Segment = aPIDSegIN;

// Initialze aPIDSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PID_1_set_ID,
                    HL7_23.PID_4_alternate_patient_ID,
                    HL7_23.PID_5_patient_name,
                    HL7_23.PID_7_date_of_birth,
                    HL7_23.PID_8_sex,
                    HL7_23.PID_9_patient_alias,
                    HL7_23.PID_10_race,
                    HL7_23.PID_11_patient_address,
                    HL7_23.PID_12_county_code,
                    HL7_23.PID_15_language,
                    HL7_23.PID_16_marital_status,
                    HL7_23.PID_17_religion,
                    HL7_23.PID_18_account_number,
                    HL7_23.PID_19_SSN_number,
                    HL7_23.PID_21_mothers_ID,
                    HL7_23.PID_22_ethnic_group,
                    HL7_23.PID_23_birth_place,
                    HL7_23.PID_29_patient_death_date_time,
                    HL7_23.PID_30_patient_death_indicator
        };
        aPIDSegOUT.linkTo(aPIDSegIN);
        aPIDSegOUT.copyFields(aCopyFields);

        // Process Unique identifiers(UR, Medicare, Pension and DVA) held in PID-3 ....
        // NOTE :- PBS Number is also held in PID-3 (type "PB") but when required is normally passed in a Z segment.
        String aTmpField[] = aPIDSegIN.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        int i;
        HL7Field aInField;
        HL7Field aOutField;
        String aStr;
        for (i=0 ; i < aTmpField.length ; i++) {
            aInField = new HL7Field(aTmpField[i]);
            aOutField = new HL7Field();
            aStr = aInField.getSubField(HL7_23.CX_ID_type_code);
            if (aStr.equalsIgnoreCase("PI")) {
                aPIDSegOUT.set(HL7_23.PID_3_patient_ID_internal, aInField.getField());
            }
            if (aStr.equalsIgnoreCase("PEN")) {
                aPIDSegOUT.set(HL7_23.PID_4_alternate_patient_ID, aInField.getSubField(HL7_23.CX_ID_number));
                aPIDSegOUT.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_type_code, "PE");
                aPIDSegOUT.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_assigning_authority, "PE");
                aPIDSegOUT.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_assigning_fac, aInField.getSubField(HL7_23.CX_assigning_fac));
            }
            if (aStr.equalsIgnoreCase("VA")) {
                aPIDSegOUT.set(HL7_23.PID_27_veterans_military_status, aInField.getSubField(HL7_23.CX_ID_number));
            }
            if (aStr.equalsIgnoreCase("MC")) {
                aPIDSegOUT.set(HL7_23.PID_19_SSN_number, aInField.getSubField(HL7_23.CX_ID_number));
            }
        }

        //process home phone number
        //This logic caters for any repeating fields such as overseas numbers in PID_13
        if ( !aPIDSegIN.isEmpty(HL7_23.PID_13_home_phone)) {
            aPIDSegOUT.set(HL7_23.PID_13_home_phone, aPIDSegIN.get(HL7_23.PID_13_home_phone));
        }

        //process business phone number
        if ( !aPIDSegIN.isEmpty(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number)) {
            aPIDSegOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number);
            aPIDSegOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telecom_use);
        }

        return aPIDSegOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" PR1 segment.
     * @return Returns the processed HL7 PR1 segment as a String.
     */
    public HL7Group processPR1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aPR1SegIN = new HL7Segment(k.NULL);
        HL7Segment aPR1SegOUT = new HL7Segment("PR1");
        HL7Group aPR1GroupOUT = new HL7Group();
        // Initialze aPR1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PR1_1_set_ID,
                    HL7_23.PR1_3_proc_code
        };
        int aPR1GroupCount = aHL7Message.countSegments("PR1");
        for (int i = 1; i <= aPR1GroupCount; i++) {
            aPR1SegIN.setSegment(aHL7Message.getSegment("PR1", i));
            aPR1SegOUT.linkTo(aPR1SegIN);
            aPR1SegOUT.copyFields(aCopyFields);
            aPR1GroupOUT.append(aPR1SegOUT.getSegment());
        }
        return aPR1GroupOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" PV1 segment.
     * @return Returns the processed HL7 PV1 segment as a String.
     */
    public HL7Segment processPV1FromUFD() throws ICANException {
        return (this.processPV1FromUFD(mHL7Message));
    }
    public HL7Segment processPV1FromUFD(String pHL7MessageBlock) throws ICANException {
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aPV1SegIN = new HL7Segment(NULL);
        HL7Segment aPV1SegOUT = new HL7Segment("PV1");

        aPV1SegIN.setSegment(aHL7Message.getSegment(HL7_24.PV1));

// Initialze aPV1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PV1_1_set_ID,
                    HL7_23.PV1_2_patient_class,
                    HL7_23.PV1_3_assigned_patient_location,
                    HL7_23.PV1_4_admission_type,
                    HL7_23.PV1_5_preadmit_num,
                    HL7_23.PV1_6_prior_patient_location,
                    HL7_23.PV1_7_attending_doctor,
                    HL7_23.PV1_8_referring_doctor,
                    HL7_23.PV1_9_consulting_doctor,
                    HL7_23.PV1_10_hospital_service,
                    HL7_23.PV1_11_temporary_location,
                    HL7_23.PV1_12_pre_admit_test_indicator,
                    HL7_23.PV1_13_re_admission_indicator,
                    HL7_23.PV1_14_admit_source,
                    HL7_23.PV1_15_ambulatory_status,
                    HL7_23.PV1_16_VIP_indicator,
                    HL7_23.PV1_17_admitting_doctor,
                    HL7_23.PV1_18_patient_type,
                    HL7_23.PV1_19_visit_number,
                    HL7_23.PV1_20_financial_class,
                    HL7_23.PV1_21_charge_price_indicator,
                    HL7_23.PV1_36_discharge_disposition,
                    HL7_23.PV1_37_discharged_to_location,
                    HL7_23.PV1_38_diet_type,
                    HL7_23.PV1_44_admit_date_time,
                    HL7_23.PV1_45_discharge_date_time
        };
        aPV1SegOUT.linkTo(aPV1SegIN);
        aPV1SegOUT.copyFields(aCopyFields);
        mPV1NursingStation = aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);

        return aPV1SegOUT;
    }
    /**
     * Generic processing for an Outgoing i.e "To Vendor" PV2 segment.
     * @return Returns the processed HL7 PV2 segment as a String.
     */
    public HL7Segment processPV2FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aPV2SegIN = new HL7Segment(NULL);
        HL7Segment aPV2SegOUT = new HL7Segment("PV2");

        aPV2SegIN.setSegment(aHL7Message.getSegment(HL7_24.PV2));

// Initialze aPV2SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PV2_3_admit_reason,
                    HL7_23.PV2_4_transfer_reason,
                    HL7_23.PV2_8_expected_admit_date,
                    HL7_23.PV2_9_expected_discharge_date,
                    HL7_23.PV2_21_visit_publicity_code,
                    HL7_23.PV2_22_visit_protection_indic,
                    HL7_23.PV2_33_expected_surgery_date
        };
        aPV2SegOUT.linkTo(aPV2SegIN);
        aPV2SegOUT.copyFields(aCopyFields);

        return aPV2SegOUT;
    }
    /**
     * Locate and return the Item identified by [pItemID] within the [pGroupID] within a ZBX segment
     * @param pGroupID The ZBX Group identifier. Examples are "PMI", "VISIT", "FINANCE", "MEDREC", "ORDER".
     * @param pItemID Specific item identifier within the specificed pGroup. Examples are "GPCode", "OCCUPATION", "CLAIM_NUMBER".
     * @return The String Value contained within the ZBX segment or NULL if  no such value exists.
     */
    public String getFromZBX(String pGroupID, String pItemID) throws ICANException {
        HL7Message aHL7mess = new HL7Message(mHL7Message);
        String aZBXArray[] = aHL7mess.getRepeatSegment(HL7_23.Repeat_ZBX, 1);
        HL7Segment aZBX;

        int i;
        String aReturn, aGroupStr, aItemStr;
        aReturn = k.NULL;         // Default if no matches are found
        for (i=0; i < aZBXArray.length ; i++) {
            aZBX = new HL7Segment(aZBXArray[i]);
            aGroupStr = aZBX.getField(HL7_24.ZBX_2_group);
            if (aGroupStr.equalsIgnoreCase(pGroupID)) {
                aItemStr = aZBX.get(HL7_24.ZBX_3_field,HL7_24.ZE_1_value);
                if (aItemStr.equalsIgnoreCase(pItemID)) {
                    aReturn = aZBX.getField(HL7_24.ZBX_4_value);
                    break;
                }
            }
        }
        return aReturn;
    }
    /**
     * Locate and return the String XCN Item identified by [pItemID] within the [pGroupID] within a ZBX segment
     * @param pGroupID The ZBX Group identifier. Example ... "PMI".
     * @param pItemID Specific item identifier within the specificed pGroup. Example ... "GPCode".
     * @return The String held in the XCN field of the ZBX segment. This is primarily used to hold the full details of a Dr.
     */
    public String getXCNFromZBX(String pGroupID, String pItemID) throws ICANException {
        HL7Message aHL7mess = new HL7Message(mHL7Message);
        String aZBXArray[] = aHL7mess.getRepeatSegment(HL7_23.Repeat_ZBX, 1);
        HL7Segment aZBX;
        HL7Field aField = new HL7Field();

        int i;
        String aReturn, aGroupStr, aItemStr;
        aReturn = k.NULL;         // Default if no matches are found
        for (i=0; i < aZBXArray.length ; i++) {
            aZBX = new HL7Segment(aZBXArray[i]);
            aGroupStr = aZBX.getField(HL7_24.ZBX_2_group);
            if (aGroupStr.equalsIgnoreCase(pGroupID)) {
                aItemStr = aZBX.get(HL7_24.ZBX_3_field,HL7_24.ZE_1_value);
                if (aItemStr.equalsIgnoreCase(pItemID)) {
                    if (! aZBX.isEmpty(HL7_24.ZBX_6_XCN_value, HL7_24.XCN_ID_num)) {
                        aField.setSubField(aZBX.get(HL7_24.ZBX_6_XCN_value, HL7_24.XCN_ID_num),HL7_24.XCN_ID_num );
                        aField.setSubField(aZBX.get(HL7_24.ZBX_6_XCN_value, HL7_24.XCN_last_name),HL7_24.XCN_last_name );
                        aField.setSubField(aZBX.get(HL7_24.ZBX_6_XCN_value, HL7_24.XCN_first_name),HL7_24.XCN_first_name );
//                   aField.setSubField(aZBX.get(HL7_24.ZBX_6_XCN_value, HL7_24.XCN_middle_initial_or_name),HL7_24.XCN_middle_initial_or_name );
                        aField.setSubField(aZBX.get(HL7_24.ZBX_6_XCN_value, HL7_24.XCN_prefix),HL7_24.XCN_prefix );
                    }
                    aReturn = aField.getField();
                    break;
                }
            }
        }
        return aReturn;
    }

    public boolean isZBXValued(String pGroupID, String pItemID) throws ICANException {
        HL7Message aHL7mess = new HL7Message(mHL7Message);
        String aZBXArray[] = aHL7mess.getRepeatSegment(HL7_23.Repeat_ZBX, 1);
        HL7Segment aZBXSegment;
        boolean aFound = false;
        int i;

        String aGroupStr, aItemStr;
        for (i=0; i < aZBXArray.length ; i++) {
            aZBXSegment = new HL7Segment(aZBXArray[i]);
            aGroupStr = aZBXSegment.getField(HL7_24.ZBX_2_group);
            if (aGroupStr.equalsIgnoreCase(pGroupID)) {
                aItemStr = aZBXSegment.get(HL7_24.ZBX_3_field,HL7_24.ZE_1_value);
                if (aItemStr.equalsIgnoreCase(pItemID)) {
                    aFound = true;
                    break;
                }
            }
        }
        return aFound;
    }

    public boolean hasValue(String pFieldID, String pCompareValue) {
        boolean aResult = false;

        String aCompareValue = pCompareValue;
        HL7Message aMsg = new HL7Message(mHL7Message);

        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        HL7Segment aNewSeg = new HL7Segment(aMsg.getSegment(aFD.ID));
        String aFieldValue = aNewSeg.getField(aFD.fieldNum);
        if (aFD.subFieldNum != 0) {
            HL7Field aField = new HL7Field(aFieldValue);
            aFieldValue = aField.getItem(aFD.subFieldNum);
        }
        if (aFieldValue.matches(aCompareValue)) {
            aResult = true;
        }

        return aResult;
    }

    public boolean hasValue(String pFieldID, String pFieldType, String pCompareValue) {
        boolean aResult = false;

        String aCompareValue = pCompareValue;
        HL7Message aMsg = new HL7Message(mHL7Message);

        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        HL7Segment aNewSeg = new HL7Segment(aMsg.getSegment(aFD.ID));
        String aFieldValue = aNewSeg.get(pFieldID, pFieldType);

        if (aFieldValue.matches(aCompareValue)) {
            aResult = true;
        }

        return aResult;
    }

     public static void display(String pInput) {
        String aList[] = pInput.split("\r");
        int i;
        for (i=0; i < aList.length; i++) {
            System.out.println(aList[i]);
        }
    }


}


