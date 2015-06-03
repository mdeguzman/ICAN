
//Authors: Ray Fillingham and Norman Soh
//Organisation: The Alfred
//Year: 2005

package BHLibClasses;

/**
 * HL7 2.4 Standard Data Type and Segment definitions.
 * @author fillinghamr
 * Oct 14th HL7_24 only differs from HL7_23 by having the ZBX definitions in it.<p>
 * This will change once we have an HL7 2.4 Standard defintion document.
 */
/**
 * <p> HL7 Standard Data Type definitions ------------------------------------------
 * <p> ... CE Data type - used for Coded items
 * <p> ... CX Data type - used for Identifiers
 * <p> ... FC Data type - used for Financial Class
 * <p> ... PL Data type - used for Point of Care
 * <p> ... XCN Data type - used for Doctor Identifiers
 * <p> ... XPN Data type - used for Names
 * <p> ... XAD Data type - used for Addesses
 * <p> ... XTN Data type - used for Telephone numbers
 * <p> ... XON Data type - used for Organization identifiers
 * <p>
 * <p> HL7 Standard Segments definitions ------------------------------------------
 * <p> ... ACC - Accident information
 * <p> ... AL1 - Allergy information
 * <p> ... DG1 - Diagnostic coding
 * <p> ... DRG - Diagnostic coding Grouping Code
 * <p> ... EVN - Event Type
 * <p> ... GT1 - Guarantor details
 * <p> ... IN1 - Insurance details
 * <p> ... IN2 - Additional Insurance details
 * <p> ... MRG - Details of Patient Merges
 * <p> ... MSH - Message Header
 * <p> ... NK1 - Next of Kin info
 * <p> ... OBX - used for observations and Results
 * <p> ... OBR - Order Result details (i.e what and who and where it was resulted)
 * <p> ... ORC - Order details (i.e what and who and where it was ordered)
 * <p> ... PID - Patient Admin details
 * <p> ... PR1 - Surgical Procedure details
 * <p> ... PV1 - Encounter related info
 * <p> ... PV2 - Additional Encounter related info
 * <p>
 * <p> Bayside Specific Segments --------------------------------------
 * <p> ... ZBX Segment definition - Bayside Unified Z segment
 * <p> ... ZE Data Type - this is a Bayside special data type used in the ZBX segment
 * <p>
 * <p> HL7 Groups ----------------------------------------
 * <p> "Repeats" are Groups with single entries for segments that can repeat.
 * They start when the segment (e.g. "OBX") is found and end when any other segment is found.
 * <p>
 * <p> "Groups" are a sequence of multiple segments where the segment sequence can repeat.
 * Groups with Multiple segments start when the 1st segment (e.g. "ORC") is found
 * and end when any segment NOT in the group is found.
 */


public class HL7_24 {
//
    /** CE Data type - used for Coded items */
    public static String CE_ID_code = "CE_1";
    public static String CE_text = "CE_2";
    public static String CE_coding_scheme = "CE_3";
    public static String CE_alternate_ID = "CE_4";
    public static String CE_alternate_text = "CE_5";
    public static String CE_alternate_coding_scheme = "CE_6";
    /** CM Data type - used for Message Identification */
    public static String CM_type = "CM_1";
    public static String CM_event = "CM_2";
//
    /** CX Data type - used for Identifiers */
    public static String CX_ID_number = "CX_1";
    public static String CX_check_digit = "CX_2";
    public static String CX_check_digit_scheme = "CX_3";
    public static String CX_assigning_authority = "CX_4";
    public static String CX_ID_type_code = "CX_5";
    public static String CX_assigning_fac = "CX_6";
//
    /** EI Data type - used for Entity IDs */
    public static String EI_1_entity_ID = "EI_1";
    public static String EI_2_namespace_ID  = "EI_2";
    public static String EI_3_universal_ID  = "EI_3";
    public static String EI_4_universal_ID_type  = "EI_4";
//
    /** FC Data type - used for Fincial  */
    public static String FC_finacial_class = "FC_1";
    public static String FC_effective_date = "FC_2";
//
    /** PL Data type - used for Point of Care */
    public static String PL_point_of_care_nu = "PL_1";
    public static String PL_room = "PL_2";
    public static String PL_bed = "PL_3";
    public static String PL_facility_ID = "PL_4";
    public static String PL_status = "PL_5";
    public static String PL_type = "PL_6";
    public static String PL_building = "PL_7";
    public static String PL_floor = "PL_8";
    public static String PL_description = "PL_9";
//
    /** TQ Data type - used for ??? .. in OBR seg */
    public static String TQ_1_quantity = "TQ_1";
    public static String TQ_1_1_quantity = "TQ_1_1";
    public static String TQ_1_2_units = "TQ_1_2";

    public static String TQ_2_interval = "TQ_2";
    public static String TQ_2_1_repeat_pattern = "TQ_2_1";
    public static String TQ_2_2_explicit_time_interval = "TQ_2_2";

    public static String TQ_3_duration = "TQ_3";
    public static String TQ_4_start_date_time  = "TQ_4";
    public static String TQ_5_end_date_time = "TQ_5";
    public static String TQ_6_priority = "TQ_6";
    public static String TQ_7_condition  = "TQ_7";
    public static String TQ_8_text  = "TQ_8";
    public static String TQ_9_conjunction = "TQ_9";
    public static String TQ_10_order_sequencing  = "TQ_10";
//
    /** XCN Data type - used for Doctor Identifiers */
    public static String XCN_ID_num = "XCN_1";
    public static String XCN_last_name = "XCN_2";
    public static String XCN_first_name = "XCN_3";
    public static String XCN_middle_initial_or_name = "XCN_4";
    public static String XCN_suffix = "XCN_5";
    public static String XCN_prefix = "XCN_6";
    public static String XCN_degree = "XCN_7";
    public static String XCN_code_source_table = "XCN_8";
    public static String XCN_assigning_authority = "XCN_9";
    public static String XCN_name_type = "XCN_10";
    public static String XCN_ID_check_digit = "XCN_11";
    public static String XCN_check_digit_scheme = "XCN_12";
    public static String XCN_ID_type = "XCN_13";
    public static String XCN_assigning_fac = "XCN_14";
//
    /** XPN Data type - used for Names */
    public static String XPN_family_name = "XPN_1";
    public static String XPN_given_name = "XPN_2";
    public static String XPN_middle_name = "XPN_3";
    public static String XPN_suffix = "XPN_4";
    public static String XPN_prefix = "XPN_5";
    public static String XPN_degree = "XPN_6";
    public static String XPN_name_type = "XPN_7";
    public static String XPN_name_representation = "XPN_8";
//
    /** XAD Data type - used for Addesses */
    public static String XAD_street_1 = "XAD_1";
    public static String XAD_street_2 = "XAD_2";
    public static String XAD_city = "XAD_3";
    public static String XAD_state_or_province = "XAD_4";
    public static String XAD_zip = "XAD_5";
    public static String XAD_country = "XAD_6";
    public static String XAD_type = "XAD_7";
    public static String XAD_geographic_designation = "XAD_8";
    public static String XAD_county_parish = "XAD_9";
    public static String XAD_census_tract = "XAD_10";
//
    /** XTN Data type - used for Telephone numbers */
    public static String XTN_telephone_number = "XTN_1";
    public static String XTN_telecom_use = "XTN_2";
    public static String XTN_telecom_equip_type = "XTN_3";
    public static String XTN_email_address = "XTN_4";
//public static String XTN_country_code= "XTN_5";
//public static String XTN_area_code = "XTN_6";
//public static String XTN_phone_number = "XTN_7";
//public static String XTN_extension= "XTN_8";
    public static String XTN_comment = "XTN_9";
//
    /** XON Data type - used for Organization identifiers */
    public static String XON_organization_name = "XON_1";
    public static String XON_organiz_name_type = "XON_2";
    public static String XON_ID_number = "XON_3";
    public static String XON_check_digit = "XON_4";
    public static String XON_check_digit_scheme = "XON_5";
    public static String XON_assigning_authority = "XON_6";
    public static String XON_ID_type = "XON_7";
    public static String XON_assigning_fac_ID = "XON_8";

    /** ZE Data Type ... this is a Bayside special data type used in the ZBX segment */
    public static String ZE_1_value = "ZE_1";
    public static String ZE_2_qualifier = "ZE_2";
    public static String ZE_3_units = "ZE_3";
    public static String ZE_4_flags = "ZE_4";
    public static String ZE_5_type = "ZE_5";//
    public static String ZE_6_XCN_value = "ZE_6";// Definition of Segment structures --------------------------------------------
    /** AL1 decodes - Allergy information */
    public static String AL1 = "AL1";
    public static String AL1_1_set_ID = "AL1_1";
    public static String AL1_2_allergy_type = "AL1_2";
    public static String AL1_3_allergy = "AL1_3";
    public static String AL1_4_severity = "AL1_4";
    public static String AL1_5_reaction = "AL1_5";
    public static String AL1_6_identification_date = "AL1_6";
///
// Definition of Segment structures --------------------------------------------
    /** ACC decodes - Accident information */
    public static String ACC = "ACC";
//public static String ACC_1_public  = "ACC_1";
    public static String ACC_2_public  = "ACC_2";
//public static String ACC_3_public  = "ACC_3";
    public static String ACC_4_Auto_public = "ACC_4";
//public static String ACC_5_public = "ACC_5";
//public static String ACC_6_public = "ACC_6";
//
    /** AL1 decodes - Allergy information */
//
    /** DG1 decodes - Diagnostic coding Group code */
    public static String DG1 = "DG1";
    public static String DG1_1_set_ID = "DG1_1";
//public static String DG1_2_diagnosis_coding_method = "DG1_2";
    public static String DG1_3_diagnosis_code = "DG1_3";
//public static String DG1_4_diagnosis_description = "DG1_4";
//public static String DG1_5_diagnosis_date_time = "DG1_5";
    public static String DG1_6_diagnosis_DRG_type = "DG1_6";
//public static String DG1_7_major_diagnostic_category = "DG1_7";
//public static String DG1_8_diagnostic_related_group = "DG1_8";
//public static String DG1_9_DRG_approval_indicator = "DG1_9";
//public static String DG1_10_DRG_grouper_review_code = "DG1_10";
//public static String DG1_11_outlier_type = "DG1_11";
//public static String DG1_12_outlier_days = "DG1_12";
//public static String DG1_13_outlier_cost = "DG1_13";
//public static String DG1_14_grouper_version_and_type = "DG1_14";
//public static String DG1_15_diagnosis_DRG_priority = "DG1_15";
//public static String DG1_16_diagnosing_clinician = "DG1_16";
    public static String DG1_17_diagnosis_classif = "DG1_17";
//public static String DG1_18_confidential_indic = "DG1_18";
//public static String DG1_19_attestation_date_time = "DG1_19";
//
    /** DRG decodes */
    public static String DRG = "DRG";
    public static String DRG_1_diagnostic_related_group = "DRG_1";
//public static String DRG_2_DRG_assigned_date_time = "DRG_2";
//public static String DRG_3_DRG_approval_indicator = "DRG_3";
//public static String DRG_4_DRG_grouper_review_code = "DRG_4";
//public static String DRG_5_outlier_type = "DRG_5";
//public static String DRG_6_outlier_days = "DRG_6";
//public static String DRG_7_outlier_cost = "DRG_7";
//public static String DRG_8_DRG_payor = "DRG_8";
//public static String DRG_9_outlier_reimbursement = "DRG_9";
//public static String DRG_10_confidential_indicator = "DRG_10";
//
    /** EVN decodes */
    public static String EVN = "EVN";
    public static String EVN_1_event_type_code = "EVN_1";
    public static String EVN_2_date_time_of_event = "EVN_2";
//public static String EVN_3_date_time_planned_event = "EVN_3";
//public static String EVN_4_event_reason_code = "EVN_4";
//public static String EVN_5_operator_ID = "EVN_5";
    public static String EVN_6_event_occurred = "EVN_6";
//
//
    /* FT1 decodes */
    public static String FT1_1_Set_ID = "FT1_1";
//public static String FT1_2_Transaction_ID = "FT1_2";
//public static String FT1_3_Transaction_Batch_ID = "FT1_3";
    public static String FT1_4_Transaction_Date = "FT1_4";
//public static String FT1_5_Transaction_Posting_Date = "FT1_5";
    public static String FT1_6_Transaction_Type = "FT1_6";
    public static String FT1_7_Transaction_Code = "FT1_7";
    public static String FT1_8_Transaction_Description = "FT1_8";
//public static String FT1_9_Transaction_Description_Alt = "FT1_9";
    public static String FT1_10_Transaction_Quantity = "FT1_10";
//public static String FT1_11_Transaction_Amount_Extended = "FT1_11";
//public static String FT1_12_Transaction_Amount_Unit = "FT1_12";
//public static String FT1_13_Department_Code = "FT1_13";
//public static String FT1_14_Insurance_Plan_ID = "FT1_14";
//public static String FT1_15_Insurance_Amount = "FT1_15";
    public static String FT1_16_Assigned_Patient_Location = "FT1_16";
//public static String FT1_17_Fee_Schedule = "FT1_17";
//public static String FT1_18_Patient_Type = "FT1_18";
//public static String FT1_19_Diagnosis_Code = "FT1_19";
    public static String FT1_20_Performed_by_Code = "FT1_20";
    public static String FT1_21_Ordered_by_Code = "FT1_21";
//public static String FT1_22_Unit_Cost = "FT1_22";
//public static String FT1_23_Filler_Order_Number = "FT1_23";
//public static String FT1_24_Entered_by_Code = "FT1_24";
    public static String FT1_25_Procedure_Code = "FT1_25";
//

    /** GT1 decodes */
    public static String GT1 = "GT1";
    public static String GT1_1_set_ID = "GT1_1";
    public static String GT1_2_guarantor_number = "GT1_2";
    public static String GT1_3_guarantor_name = "GT1_3";
//public static String GT1_4_guarantor_spouse_name = "GT1_4";
    public static String GT1_5_guarantor_address = "GT1_5";
    public static String GT1_6_guarantor_phone_home = "GT1_6";
    public static String GT1_7_guarantor_phone_business = "GT1_7";
//public static String GT1_8_guarantor_date_of_birth = "GT1_8";
//public static String GT1_9_guarantor_sex = "GT1_9";
    public static String GT1_10_guarantor_type = "GT1_10";
//public static String GT1_11_guarantor_relationship = "GT1_11";
//public static String GT1_12_guarantor_SSN = "GT1_12";
//public static String GT1_13_guarantor_date_begin = "GT1_13";
//public static String GT1_14_guarantor_date_end = "GT1_14";
//public static String GT1_15_guarantor_priority = "GT1_15";
//public static String GT1_16_guarantor_employer_name = "GT1_16";
//public static String GT1_17_guarantor_employer_address = "GT1_17";
//public static String GT1_18_guarantor_employer_phone = "GT1_18";
//public static String GT1_19_guarantor_employee_ID_num = "GT1_19";
//public static String GT1_20_guarantor_employment_status = "GT1_20";
//public static String GT1_21_guarantor_organization = "GT1_21";
//public static String GT1_22_guar_hold_flag = "GT1_22";
//public static String GT1_23_guar_credit_rating_code = "GT1_23";
//public static String GT1_24_guar_death_date_time = "GT1_24";
//public static String GT1_25_guar_death_flag = "GT1_25";
//public static String GT1_26_guar_charge_adjust_code = "GT1_26";
//public static String GT1_27_guar_household_annual_income = "GT1_27";
//public static String GT1_28_guar_household_size = "GT1_28";
//public static String GT1_29_guar_empl_ID = "GT1_29";
//public static String GT1_30_guar_marital_status = "GT1_30";
//public static String GT1_31_guar_hire_eff_date = "GT1_31";
//public static String GT1_32_empl_stop_date = "GT1_32";
//public static String GT1_33_living_dependency = "GT1_33";
//public static String GT1_34_ambulatory_status = "GT1_34";
//public static String GT1_35_citizenship = "GT1_35";
//public static String GT1_36_primary_language = "GT1_36";
//public static String GT1_37_living_arrangement = "GT1_37";
//public static String GT1_38_publicity_indic = "GT1_38";
//public static String GT1_39_protection_indic = "GT1_39";
//public static String GT1_40_student_indic = "GT1_40";
//public static String GT1_41_religion = "GT1_41";
//public static String GT1_42_mothers_maiden_name = "GT1_42";
//public static String GT1_43_nationality = "GT1_43";
//public static String GT1_44_ethnic_group = "GT1_44";
//public static String GT1_45_contact_person_name = "GT1_45";
//public static String GT1_46_contact_person_phone = "GT1_46";
//public static String GT1_47_contact_reason = "GT1_47";
//public static String GT1_48_contact_relationship = "GT1_48";
//public static String GT1_49_job_title = "GT1_49";
//public static String GT1_50_job_code_class = "GT1_50";
//public static String GT1_51_employers_organization = "GT1_51";
//public static String GT1_52_handicap = "GT1_52";
//public static String GT1_53_job_status = "GT1_53";
//public static String GT1_54_guarantor_financial_class = "GT1_54";
//public static String GT1_55_guarantor_race = "GT1_55";
//
    /** IN1 Segment definition - Insurance details */
    public static String IN1 = "IN1";
    public static String IN1_1_set_ID = "IN1_1";
    public static String IN1_2_insurance_plan_ID = "IN1_2";
    public static String IN1_3_insurance_co_ID = "IN1_3";
    public static String IN1_4_insurance_co_name = "IN1_4";
    public static String IN1_5_insurance_co_address = "IN1_5";
//public static String IN1_6_insurance_co_contact_person = "IN1_6";
//public static String IN1_7_insurance_co_phone = "IN1_7";
//public static String IN1_8_group_number = "IN1_8";
//public static String IN1_9_group_name = "IN1_9";
//public static String IN1_10_insured_group_employer_ID = "IN1_10";
//public static String IN1_11_insured_group_employer_name = "IN1_11";
//public static String IN1_12_plan_effective_date = "IN1_12";
//public static String IN1_13_plan_expiration_date = "IN1_13";
//public static String IN1_14_authorization_info = "IN1_14";
//public static String IN1_15_plan_type = "IN1_15";
//public static String IN1_16_name_of_insured = "IN1_16";
//public static String IN1_17_insured_relationship_to_patient = "IN1_17";
//public static String IN1_18_insured_date_of_birth = "IN1_18";
//public static String IN1_19_insured_address = "IN1_19";
//public static String IN1_20_assignment_of_benefits = "IN1_20";
//public static String IN1_21_COB = "IN1_21";
//public static String IN1_22_COB_priority = "IN1_22";
//public static String IN1_23_notice_of_admission_code = "IN1_23";
//public static String IN1_24_notice_of_admission_date = "IN1_24";
//public static String IN1_25_report_of_eligibility_code = "IN1_25";
//public static String IN1_26_report_of_eligibility_date = "IN1_26";
//public static String IN1_27_release_info_code = "IN1_27";
//public static String IN1_28_pre_admit_certification = "IN1_28";
//public static String IN1_29_verification_date = "IN1_29";
//public static String IN1_30_verification_by = "IN1_30";
//public static String IN1_31_type_of_agreement_code = "IN1_31";
//public static String IN1_32_billing_status = "IN1_32";
//public static String IN1_33_lifetime_reserve_days = "IN1_33";
//public static String IN1_34_delay_before_L_R_day = "IN1_34";
//public static String IN1_35_company_plan_code = "IN1_35";
    public static String IN1_36_policy_number = "IN1_36";
//public static String IN1_37_policy_deductible = "IN1_37";
//public static String IN1_38_policy_limit__amount = "IN1_38";
//public static String IN1_39_policy_limit__days = "IN1_39";
//public static String IN1_40_room_rate__semi_private = "IN1_40";
//public static String IN1_41_room_rate__private = "IN1_41";
//public static String IN1_42_insured_employment_status = "IN1_42";
//public static String IN1_43_insured_sex = "IN1_43";
//public static String IN1_44_insured_employer_address = "IN1_44";
//public static String IN1_45_verfication_status = "IN1_45";
//public static String IN1_46_prior_insurance_plan_ID = "IN1_46";
//public static String IN1_47_coverage_type = "IN1_47";
//public static String IN1_48_handicap = "IN1_48";
//public static String IN1_49_insureds_ID = "IN1_49";
//
    /** IN2 Segment definition - Additional Insurance details */
    public static String IN2 = "IN2";
//public static String IN2_1_insureds_employee_ID = "IN2_1";
//public static String IN2_2_insureds_soc_sec_num = "IN2_2";
//public static String IN2_3_insureds_employer_name = "IN2_3";
//public static String IN2_4_employer_information_data = "IN2_4";
//public static String IN1_5_mail_claim_party = "IN1_5";
//public static String IN2_6_medicare_card_num = "IN2_6";
//public static String IN2_7_medicaid_case_name = "IN2_7";
//public static String IN2_8_medicaid_case_num = "IN2_8";
//public static String IN2_9_champus_sponsor_name = "IN2_9";
//public static String IN2_10_champus_ID_num = "IN2_10";
//public static String IN2_11_dependent_of_champus_recipient = "IN2_11t";
//public static String IN2_12_champus_organization = "IN2_12";
//public static String IN2_13_champus_station = "IN2_13";
//public static String IN2_14_champus_service = "IN2_14";
//public static String IN2_15_champus_rank_grade = "IN2_15";
//public static String IN2_16_champus_status = "IN2_16";
//public static String IN2_17_champus_retire_date = "IN2_17";
//public static String IN2_18_champus_non_avail_cert_on_file = "IN2_18";
//public static String IN2_19_baby_coverage = "IN2_19";
//public static String IN2_20_combine_baby_bill = "IN2_20";
//public static String IN2_21_blood_deductible = "IN2_21";
//public static String IN2_22_special_coverage_approval_name = "IN2_22";
//public static String IN2_23_special_coverage_approval_title = "IN2_23";
//public static String IN2_24_non_covered_ins_code = "IN2_24";
//public static String IN2_25_payor_ID = "IN2_25";
//public static String IN2_26_payor_subscriber_ID = "IN2_26";
//public static String IN2_27_eligibility_source = "IN2_27";
//public static String IN2_28_room_coverage_type_amount = "IN2_28";
//public static String IN2_29_policy_type_amount = "IN2_29";
//public static String IN2_30_daily_deductible = "IN2_30";
//public static String IN2_31_living_dependency = "IN2_31";
//public static String IN2_32_ambulatory_status = "IN2_32";
//public static String IN2_33_citizenship = "IN2_33";
//public static String IN2_34_primary_language = "IN2_34";
//public static String IN2_35_living_arrangement = "IN2_35";
//public static String IN2_36_publicity_indic = "IN2_36";
//public static String IN2_37_protection_indic = "IN2_37";
//public static String IN2_38_student_indic = "IN2_38";
//public static String IN2_39_religion = "IN2_39";
//public static String IN2_40_mothers_maiden_name = "IN2_40";
//public static String IN2_41_nationality = "IN2_41";
//public static String IN2_42_ethnic_group = "IN2_42";
//public static String IN2_43_marital_status = "IN2_43";
//public static String IN2_44_insured_empl_start_date = "IN2_44";
//public static String IN2_45_insured_empl_stop_date = "IN2_45";
    public static String IN2_46_job_title = "IN2_46";
//public static String IN2_47_job_code_class = "IN2_47";
//public static String IN2_48_job_status = "IN2_48";
//public static String IN2_49_empl_contact_person_name = "IN2_49";
//public static String IN2_50_empl_contact_person_phone = "IN2_50";
//public static String IN2_51_empl_contact_reason = "IN2_51";
//public static String IN2_52_insured_contact_person_name = "IN2_52";
//public static String IN2_53_insured_contact_person_phone = "IN2_53";
//public static String IN2_54_insured_contact_person_reason = "IN2_54";
//public static String IN2_55_relationship_to_patient_start_date = "IN2_55";
//public static String IN2_56_relationship_to_patient_stop_date = "IN2_56";
//public static String IN2_57_insurance_co_contact_reason = "IN2_57";
//public static String IN2_58_insurance_co_contact_phone = "IN2_58";
//public static String IN2_59_policy_scope = "IN2_59";
//public static String IN2_60_policy_source = "IN2_60";
//public static String IN2_61_patient_member_num = "IN2_61";
//public static String IN2_62_guar_rel_to_insured = "IN2_62";
//public static String IN2_63_insured_phone_home = "IN2_63";
//public static String IN2_64_insured_empl_phone = "IN2_64";
//public static String IN2_65_military_handicapped_prog = "IN2_65";
//public static String IN2_66_suspend_flag = "IN2_66";
//public static String IN2_67_copay_limit_flag = "IN2_67";
//public static String IN2_68_stoploss_limit_flag = "IN2_68";
//public static String IN2_69_insured_org = "IN2_69";
//public static String IN2_70_insured_empl_org = "IN2_70";
//public static String IN2_71_race = "IN2_71";
//public static String IN2_72_HCFA_pat_rel_to_insured = "IN2_72";
//
    /** MRG Segment definition - Details of Patient Merges */
    public static String MRG = "MRG";
    public static String MRG_1_prior_patient_ID_internal = "MRG_1";
//public static String MRG_2_prior_alternate_patient_ID = "MRG_2";
//public static String MRG_3_prior_patient_account_number = "MRG_3";
//public static String MRG_4_prior_patient_ID_external = "MRG_4";
//public static String MRG_5_prior_visit_num = "MRG_5";
//public static String MRG_6_prior_alternate_visit_ID = "MRG_6";
//public static String MRG_7_prior_patient_name = "MRG";
//
    /** MSH Segment definition */
    public static String MSH = "MSH";
    public static String MSH_2_encoding_characters = "MSH_2";
    public static String MSH_3_sending_application = "MSH_3";
    public static String MSH_4_sending_facility = "MSH_4";
    public static String MSH_5_receiving_application = "MSH_5";
    public static String MSH_6_receiving_facility = "MSH_6";
    public static String MSH_7_message_date_time = "MSH_7";
//public static String MSH_8_security = "MSH_8";
    public static String MSH_9_message_type = "MSH_9";
    public static String MSH_9_1_message_type = "MSH_9_1";
    public static String MSH_9_2_trigger_event = "MSH_9_2";
    public static String MSH_10_message_control_ID = "MSH_10";
    public static String MSH_11_processing_ID = "MSH_11";
    public static String MSH_12_version_ID = "MSH_12";
//public static String MSH_13_sequence_number = "MSH_13";
//public static String MSH_14_continuation_pointer = "MSH_14";
//public static String MSH_15_accept_ack_type = "MSH_15";
//public static String MSH_16_application_ack_type = "MSH_16";
//public static String MSH_17_country_code = "MSH_17";
//public static String MSH_18_character_set = "MSH_18";
//public static String MSH_19_principal_language = "MSH_19";
//
    /** NK1 Segment definition - Next of Kin info */
    public static String NK1 = "NK1";
    public static String NK1_1_set_ID = "NK1_1";
    public static String NK1_2_next_of_kin_name = "NK1_2";
    public static String NK1_3_next_of_kin_relationship = "NK1_3";
    public static String NK1_4_next_of_kin__address = "NK1_4";
    public static String NK1_5_next_of_kin__phone = "NK1_5";
    public static String NK1_6_business_phone_num = "NK1_6";
    public static String NK1_7_contact_role = "NK1_7";
//public static String NK1_8_start_date = "NK1_8";
//public static String NK1_9_end_date = "NK1_9";
//public static String NK1_10_job_title = "NK1_10";
//public static String NK1_11_job_code_class = "NK1_11";
//public static String NK1_12_employee_num = "NK1_12";
//public static String NK1_13_organization_name = "NK1_13";
//public static String NK1_14_marital_status = "NK1_14";
//public static String NK1_15_sex = "NK1_15";
//public static String NK1_16_date_of_birth = "NK1_16";
//public static String NK1_17_living_dependency = "NK1_17";
//public static String NK1_18_ambulatory_status = "NK1_18";
//public static String NK1_19_citizenship = "NK1_19";
//public static String NK1_20_primary_language = "NK1_20";
//public static String NK1_21_living_arrangement = "NK1_21";
//public static String NK1_22_publicity_indic = "NK1_22";
//public static String NK1_23_protection_indic = "NK1_23";
//public static String NK1_24_student_indic = "NK1_24";
//public static String NK1_25_religion = "NK1_25";
//public static String NK1_26_mothers_maiden_name = "NK1_26";
//public static String NK1_27_nationality = "NK1_27";
//public static String NK1_28_ethnic_group = "NK1_28";
//public static String NK1_29_contact_reason = "NK1_29";
//public static String NK1_30_contact_person_name = "NK1_30";
//public static String NK1_31_contact_person_phone = "NK1_31";
//public static String NK1_32_contact_person_address = "NK1_32";
//public static String NK1_33_next_of_kin_ID = "NK1_33";
//public static String NK1_34_job_status = "NK1_34";
//public static String NK1_35_race = "NK1_35";
//public static String NK1_36_handicap = "NK1_36";
//public static String NK1_37_contact_person_ssn = "NK1_37";
//
    /** NTE Segment definition - used for Notes on PID, on PV1 and on Orders/Results */
    public static String NTE = "NTE";
    public static String NTE_1_setID = "NTE_1";
    public static String NTE_2_source_of_comment = "NTE_2";
    public static String NTE_3_comment = "NTE_3";

    /** OBX Segment definition - used for observations and Results */
    public static String OBX = "OBX";
    public static String OBX_1_set_ID = "OBX_1";
    public static String OBX_2_value_type = "OBX_2";
    public static String OBX_3_observation_identifier = "OBX_3";
    public static String OBX_4_observation_sub_ID = "OBX_4";
    public static String OBX_5_observation_value = "OBX_5";
    public static String OBX_6_units = "OBX_6";
    public static String OBX_7_references_range = "OBX_7";
    public static String OBX_8_abnormal_flags = "OBX_8";
    public static String OBX_9_probability = "OBX_9";
//public static String OBX_10_nature_of_abnormal_test = "OBX_10";
    public static String OBX_11_observ_results_status = "OBX_11";
//public static String OBX_12_date_last_obs_normal_values = "OBX_12";
//public static String OBX_13_user_defined_access_checks = "OBX_13";
//public static String OBX_14_date_time_of_the_observation = "OBX_14";
//public static String OBX_15_producers_ID = "OBX_15";
//public static String OBX_16_responsible_observer = "OBX_16";
//public static String OBX_17_observation_method = "OBX_17";
//
    /** OBR Segment definition - Order Result details (i.e what and who and where it was resulted) */
    public static String OBR = "OBR";
    public static String OBR_1_Set_ID = "OBR_1";
    public static String OBR_2_Placer_Order_Number = "OBR_2";
    public static String OBR_3_Filler_Order_Number = "OBR_3";
    public static String OBR_4_Universal_Service_ID = "OBR_4";
    public static String OBR_5_Priority = "OBR_5";
    public static String OBR_6_Requested_Date_Time = "OBR_6";
    public static String OBR_7_Observation_Date_Time = "OBR_7";
    public static String OBR_8_Observation_End_Date_Time = "OBR_8";
    public static String OBR_9_Collection_Volume = "OBR_9";
    public static String OBR_10_collector_ID = "OBR_10";
    public static String OBR_11_Specimen_Action_Code = "OBR_11";
    public static String OBR_12_Danger_Code = "OBR_12";
    public static String OBR_13_Relevant_Clinical_Information = "OBR_13";
    public static String OBR_14_Specimen_Received_Date_Time = "OBR_14";
    public static String OBR_15_Specimen_Source = "OBR_15";

    public static String Source_CE_1_Specimen_Source = "SS_1";
    public static String Source_CE_1_1_Specimen_code = "SS_1_1";
    public static String Source_CE_1_2_Specimen_text = "SS_1_2";
//        public static String Source_CE_1_3_Specimen_scheme = "SS_1_3";
//        public static String Source_CE_1_4_Specimen_alt_ID = "SS_1_4";
//        public static String Source_CE_1_5_Specimen_alt_text = "SS_1_5";
//        public static String Source_CE_1_6_Specimen_alt_coding_scheme = "SS_1_6";
    public static String Source_2_Additives = "SS_2";
    public static String Source_3_Collection_Method = "SS_3";
    public static String Source_4_CE_4_Body_Site = "SS_4";
    public static String Source_4_CE_4_1_Body_Site_code = "SS_4_1";
    public static String Source_4_CE_4_2_Body_Site_text = "SS_4_2";
//        public static String Source_CE_4_3_Body_Site_scheme = "SS_4_3";
//        public static String Source_CE_4_4_Body_Site_alt_ID = "SS_4_4";
//        public static String Source_CE_4_5_Body_Site_alt_text = "SS_4_5";
//        public static String Source_CE_4_6_Body_Site_alt_coding_scheme = "SS_4_6";
    public static String Source_CE_5_Site_Modifier = "SS_5";
    public static String Source_CE_5_1_Site_Modifier_code = "SS_5_1";
    public static String Source_CE_5_2_Site_Modifier_text = "SS_5_2";
//        public static String Source_CE_5_3_Site_Modifier_scheme = "SS_5_3";
//        public static String Source_CE_5_4_Site_Modifier_alt_ID = "SS_5_4";
//        public static String Source_CE_5_5_Site_Modifier_alt_text = "SS_5_5";
//        public static String Source_CE_5_6_Site_Modifier_alt_coding_scheme = "SS_1_6";

    public static String OBR_16_Ordering_Provider = "OBR_16";
    public static String OBR_17_Order_Call_Back_Phone_Number = "OBR_17";
    public static String OBR_18_Placers_Field_1 = "OBR_18";
    public static String OBR_19_Placers_Field_2 = "OBR_19";
    public static String OBR_20_Fillers_Field_1 = "OBR_20";
    public static String OBR_21_Fillers_Field_2 = "OBR_21";
    public static String OBR_22_Results_RPT_Status_Change = "OBR_22";
    public static String OBR_23_Charge_To_Practice = "OBR_23";
    public static String OBR_24_Diagnostic_Service_Section_ID = "OBR_24";
    public static String OBR_25_Results_Status = "OBR_25";
    public static String OBR_26_parent_result = "OBR_26";
        public static String Observation_sub_ID = "PS_2";
    public static String OBR_27_Quantity_Timing = "OBR_27";
    public static String OBR_28_Results_Copies_To = "OBR_28";
    public static String OBR_29_Parent = "OBR_29";
    public static String OBR_30_Transportation_Mode = "OBR_30";
    public static String OBR_31_Reason_For_Study = "OBR_31";
    public static String OBR_32_Principal_Result_Interpreter = "OBR_32";
    public static String OBR_33_Assistant_Result_Interpreter = "OBR_33";
    public static String OBR_34_technician = "OBR_34";
    public static String OBR_35_transcriptionist = "OBR_35";
    public static String OBR_36_Scheduled_Date_Time = "OBR_36";
    public static String OBR_37_num_sample_containers = "OBR_37";
    public static String OBR_38_transport_logistics_of_sample = "OBR_38";
    public static String OBR_39_collectors_comment = "OBR_39";
    public static String OBR_40_transport_arrange_respons = "OBR_40";
    public static String OBR_41_transport_arranged = "OBR_41";
    public static String OBR_42_escort_reqd = "OBR_42";
    public static String OBR_43_planned_patient_transport_comment = "OBR_43";
//
    /** ORC Segment definition - Order details (i.e what and who and where it was ordered) */
    public static String ORC = "ORC";
    public static String ORC_1_order_control = "ORC_1";
    public static String ORC_2_placer_order_num = "ORC_2";
    public static String ORC_3_filler_order_num = "ORC_3";
    public static String ORC_4_placer_group_num = "ORC_4";
    public static String ORC_5_order_status = "ORC_5";
    public static String ORC_6_response_flag = "ORC_6";
    public static String ORC_7_timing_quantity = "ORC_7";
    public static String ORC_8_parent = "ORC_8";
    public static String ORC_9_date_time_of_trans = "ORC_9";
    public static String ORC_10_entered_by = "ORC_10";
    public static String ORC_11_verified_by = "ORC_11";
    public static String ORC_12_ordering_provider = "ORC_12";
    public static String ORC_13_enterers_location = "ORC_13";
    public static String ORC_14_call_back_phone_number = "ORC_14";
    public static String ORC_15_order_effective_date_time = "ORC_15";
    public static String ORC_16_order_control_code_reason = "ORC_16";
    public static String ORC_17_entering_organization = "ORC_17";
    public static String ORC_18_entering_device = "ORC_18";
    public static String ORC_19_action_by = "ORC_19";
//
      /** PD1 Segment definition - Patient Admin details

        PD1-1   IS      2  Living Dependency
        PD1-2   IS      2  Living Arrangement
        PD1-3   XON    90  Patient Primary Facility
        PD1-4   XCN    90  Patient Primary Care Provider Name & ID No.
        PD1-5   IS      2  Student Indicator
        PD1-6   IS      2  Handicap
        PD1-7   IS      2  Living Will
        PD1-8   IS      2  Organ Donor
        PD1-9   ID      2  Separate Bill
        PD1-10  CX      2  Duplicate Patient
        PD1-11  CE     80  Publicity Indicator
        PD1-12  ID      1  Protection Indicator


       */
    public static String PD1 = "PD1";
    public static String PD1_1_living_dependency = "PD1_1";
    public static String PD1_2_living_arrangement = "PD1_2";
    public static String PD1_3_patient_primary_facility = "PD1_3";
    public static String PD1_4_patient_primary_care_provider_name_id = "PD1_4";
    public static String PD1_5_student_indicator = "PD1_5";
    public static String PD1_6_handicap = "PD1_6";
    public static String PD1_7_living_will = "PD1_7";
    public static String PD1_8_organ_donor = "PD1_8";
    public static String PD1_9_separate_bill = "PD1_9";
    public static String PD1_10_duplicate_patient = "PD1_10";
    public static String PD1_11_publicity_indicator = "PD1_11";
    public static String PD1_12_protection_indicator = "PD1_12";


    /** PID Segment definition - Patient Admin details */
    public static String PID = "PID";
    public static String PID_1_set_ID = "PID_1";
    public static String PID_2_patient_ID_external = "PID_2";
    public static String PID_3_patient_ID_internal = "PID_3";
    public static String PID_4_alternate_patient_ID = "PID_4";
    public static String PID_5_patient_name = "PID_5";
    public static String PID_6_mothers_maiden_name = "PID_6";
    public static String PID_7_date_of_birth = "PID_7";
    public static String PID_8_sex = "PID_8";
    public static String PID_9_patient_alias = "PID_9";
    public static String PID_10_race = "PID_10";
    public static String PID_11_patient_address = "PID_11";
    public static String PID_12_county_code = "PID_12";
    public static String PID_13_home_phone = "PID_13";
    public static String PID_14_business_phone = "PID_14";
    public static String PID_15_language = "PID_15";
    public static String PID_16_marital_status = "PID_16";
    public static String PID_17_religion = "PID_17";
    public static String PID_18_account_number = "PID_18";
    public static String PID_19_SSN_number = "PID_19";
    public static String PID_20_driver_lic_num = "PID_20";
    public static String PID_21_mothers_ID = "PID_21";
    public static String PID_22_ethnic_group = "PID_22";
    public static String PID_23_birth_place = "PID_23";
    public static String PID_24_multiple_birth_indicator = "PID_24";
    public static String PID_25_birth_order = "PID_25";
    public static String PID_26_citizenship = "PID_26";
    public static String PID_27_veterans_military_status = "PID_27";
    public static String PID_28_nationality = "PID_28";
    public static String PID_29_patient_death_date_time = "PID_29";
    public static String PID_30_patient_death_indicator = "PID_30";
//
    /** PR1 Segment definition - Surgical Procedure details */
    public static String PR1 = "PR1";
    public static String PR1_1_set_ID = "PR1_1";
//public static String PR1_2_proc_coding_method = "PR1_2";
    public static String PR1_3_proc_code = "PR1_3";
//public static String PR1_4_proc_description = "PR1_4";
//public static String PR1_5_proc_date_time = "PR1_5";
//public static String PR1_6_proc_type = "PR1_6";
//public static String PR1_7_proc_minutes = "PR1_7";
//public static String PR1_8_anesthesiologist = "PR1_8";
//public static String PR1_9_anesthesia_code = "PR1_9";
//public static String PR1_10_anesthesia_minutes = "PR1_10";
//public static String PR1_11_surgeon = "PR1_11";
//public static String PR1_12_proc_practitioner = "PR1_12";
//public static String PR1_13_consent_code = "PR1_13";
//public static String PR1_14_procedure_priority = "PR1_14";
//public static String PR1_15_associated_diag_code = "PR1_15";
//
    /** PV1 Segment definition - Encounter related info */
    public static String PV1 = "PV1";
    public static String PV1_1_set_ID = "PV1_1";
    public static String PV1_2_patient_class = "PV1_2";
    public static String PV1_3_assigned_patient_location = "PV1_3";
    public static String PV1_4_admission_type = "PV1_4";
    public static String PV1_5_preadmit_num = "PV1_5";
    public static String PV1_6_prior_patient_location = "PV1_6";
    public static String PV1_7_attending_doctor = "PV1_7";
    public static String PV1_8_referring_doctor = "PV1_8";
    public static String PV1_9_consulting_doctor = "PV1_9";
    public static String PV1_10_hospital_service = "PV1_10";
    public static String PV1_11_temporary_location = "PV1_11";
    public static String PV1_12_pre_admit_test_indicator = "PV1_12";
    public static String PV1_13_re_admission_indicator = "PV1_13";
    public static String PV1_14_admit_source = "PV1_14";
    public static String PV1_15_ambulatory_status = "PV1_15";
    public static String PV1_16_VIP_indicator = "PV1_16";
    public static String PV1_17_admitting_doctor = "PV1_17";
    public static String PV1_18_patient_type = "PV1_18";
    public static String PV1_19_visit_number = "PV1_19";
    public static String PV1_20_financial_class = "PV1_20";
    public static String PV1_21_charge_price_indicator = "PV1_21";
//public static String PV1_22_courtesy_code = "PV1_22";
//public static String PV1_23_credit_rating = "PV1_23";
//public static String PV1_24_contract_code = "PV1_24";
//public static String PV1_25_contract_effective_date = "PV1_25";
//public static String PV1_26_contract_amt = "PV1_26";
//public static String PV1_27_contract_period = "PV1_27";
//public static String PV1_28_interest_code = "PV1_28";
//public static String PV1_29_transfer_to_bad_debt_code = "PV1_29";
//public static String PV1_30_transfer_to_bad_debt_date = "PV1_30";
//public static String PV1_31_bad_debt_agency_code = "PV1_31";
//public static String PV1_32_bad_debt_transfer_amount = "PV1_32";
//public static String PV1_33_bad_debt_recovery_amount = "PV1_33";
//public static String PV1_34_delete_account_indicator = "PV1_34";
//public static String PV1_35_delete_account_date = "PV1_35";
    public static String PV1_36_discharge_disposition = "PV1_36";
    public static String PV1_37_discharged_to_location = "PV1_37";
    public static String PV1_38_diet_type = "PV1_38";
//public static String PV1_39_servicing_facility = "PV1_39";
//public static String PV1_40_bed_status = "PV1_40";
    public static String PV1_41_account_status = "PV1_41";
//public static String PV1_42_pending_location = "PV1_42";
//public static String PV1_43_prior_temporary_location = "PV1_43";
    public static String PV1_44_admit_date_time = "PV1_44";
    public static String PV1_45_discharge_date_time = "PV1_45";
//public static String PV1_46_current_patient_balance = "PV1_46";
//public static String PV1_47_total_charges = "PV1_47";
//public static String PV1_48_total_adjustments = "PV1_48";
//public static String PV1_49_total_payments = "PV1_49";
    public static String PV1_50_alternate_visit_ID = "PV1_50";
//public static String PV1_51_visit_indicator = "PV1_51";
//public static String PV1_52_other_healthcare_provider = "PV1_52";
//
    /** PV2 Segment definition - Additional Encounter related info */
    public static String PV2 = "PV2";
//public static String PV2_1_prior_pending_location = "PV2_1";
    public static String PV2_2_accommodation_code = "PV2_2";
    public static String PV2_3_admit_reason = "PV2_3";
    public static String PV2_4_transfer_reason = "PV2_4";
//public static String PV2_5_patient_valuables = "PV2_5";
//public static String PV2_6_patient_valuables_location = "PV2_6";
    public static String PV2_7_visit_user_code = "PV2_7";
    public static String PV2_8_expected_admit_date = "PV2_8";
    public static String PV2_9_expected_discharge_date = "PV2_9";
//public static String PV2_10_est_lengh_inpat_stay = "PV2_10";
//public static String PV2_11_actual_length_inpat_stay = "PV2_11";
//public static String PV2_12_visit_description = "PV2_12";
//public static String PV2_13_referral_source_code = "PV2_13";
//public static String PV2_14_previous_service_date = "PV2_14";
//public static String PV2_15_employ_illness_related = "PV2_15";
//public static String PV2_16_purge_status = "PV2_16";
//public static String PV2_17_purge_status_date = "PV2_17";
//public static String PV2_18_special_program = "PV2_18";
//public static String PV2_19_retention_indic = "PV2_19";
//public static String PV2_20_expected_num_ins_plans = "PV2_20";
    public static String PV2_21_visit_publicity_code = "PV2_21";
    public static String PV2_22_visit_protection_indic = "PV2_22";
//public static String PV2_23_clinic_org_name = "PV2_23";
//public static String PV2_24_patient_status = "PV2_24";
//public static String PV2_25_visit_priority = "PV2_25";
//public static String PV2_26_previous_treatment_date = "PV2_26";
//public static String PV2_27_expected_discharge_disposition = "PV2_27";
//public static String PV2_28_signature_on_file_date = "PV2_28";
//public static String PV2_29_first_similar_illness_date = "PV2_29";
//public static String PV2_30_patient_charge_adjustment_code = "PV2_30";
//public static String PV2_31_recurring_service_code = "PV2_31";
//public static String PV2_32_billing_media_code = "PV2_32";
    public static String PV2_33_expected_surgery_date = "PV2_33";
//public static String PV2_34_military_partnership_code = "PV2_34";
//public static String PV2_35_military_non_avail_code = "PV2_35";
//public static String PV2_36_newborn_baby_indic = "PV2_36";
//public static String PV2_37_baby_detained = "PV2_37";
//

    /** ZBX Segment definition ... Bayside Unified Z segment */
    public static String ZBX = "ZBX";
    public static String ZBX_1_set_ID = "ZBX_1";
    public static String ZBX_2_group = "ZBX_2";
    public static String ZBX_3_field = "ZBX_3";
    public static String ZBX_4_value = "ZBX_4";
    public static String ZBX_5_type = "ZBX_5";
    public static String ZBX_6_XCN_value = "ZBX_6";
//

    /* Definition of HL7 Groups ---------------------------------------- */
    /** "Repeats" are Groups with single entries for segments that can repeat
     * They start when the segment (e.g. "OBX") is found
     * and end when any other segment is found. */
    public static String Repeat_ACC = "ACC";
    public static String Repeat_AL1 = "AL1";
    public static String Repeat_DG1 = "DG1";
    public static String Repeat_FT1 = "FT1";
    public static String Repeat_GT1 = "GT1";
    public static String Repeat_NK1 = "NK1";
    public static String Repeat_NTE = "NTE";
    public static String Repeat_OBX = "OBX";
    public static String Repeat_PR1 = "PR1";
    public static String Repeat_ZBX = "ZBX";
//
    /** "Groups" are a sequence of multiple segments where the segment sequence can repeat.
     * Groups with Multiple segments start when the 1st segment (e.g. "ORC") is found
     * and end when any segment NOT in the group is found. */
    public static String Group_Observation_Details[] = {"OBX","NTE"};
    public static String Group_Orders[] = {"ORC","OBR","NTE","OBX"};
    public static String Group_A17_Patient[] = {"PID", "PV1", "PV2"};
    public static String Group_Insurance[] = {"IN1", "IN2", "IN3"};
    public static String Group_PIDNotes[] = {"PID", "NTE"};
    public static String Group_PD1Notes[] = {"PD1", "NTE"};
    public static String Group_OBRNotes[] = {"OBR", "NTE"};
}

