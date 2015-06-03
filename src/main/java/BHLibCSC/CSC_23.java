package BHLibCSC;

//Authors: Ray Fillingham and Norman Soh
//Organisation: The Alfred
//Year: 2005

/**
 These are the HL7 field location mappings for CSC Z segments
 For CSC, all mappings for standard HL7 segments(and HL7 sub field mappings i.e CE, CX, XPN ....)
 are held in the HL7_23 (or HL7_24) class files.
 */

public class CSC_23 {

public static String ZMR_1_Last_Movement_Date = "ZMR_1";
public static String ZMR_2_Last_Movement_Time = "ZMR_2";
public static String ZMR_3_Volume_Number = "ZMR_3";
public static String ZMR_4_Location = "ZMR_4";
public static String ZMR_5_Received_By = "ZMR_5";
public static String ZMR_6_Extension_phone = "ZMR_6";
//
//
//public static String ZBD_1_Birth_Facility  = "ZBD_1";
//public static String ZBD_2_Gestation  = "ZBD_2";
//public static String ZBD_3_Birth_Weight  = "ZBD_3";
//public static String ZBD_4_Birth_Type  = "ZBD_4";
//public static String ZBD_5_Presentation  = "ZBD_5";
//public static String ZBD_6_Delivery_Method  = "ZBD_6";
//public static String ZBD_7_Resuscitation  = "ZBD_7";
//public static String ZBD_8_Establish_Respirations  = "ZBD_8";
//public static String ZBD_9_Research  = "ZBD_9";
//public static String ZBD_10_Other_Comments  = "ZBD_10";
//public static String ZBD_11_Apgar_at_1_Min  = "ZBD_11";
//public static String ZBD_12_Apgar_at_5_Min  = "ZBD_12";
//public static String ZBD_13_Mothers_Urno  = "ZBD_13";
//
public static String ZCD_1_Record_Number  = "ZCD_1";
//public static String ZCD_2_Nature_of_Injury  = "ZCD_2";
//public static String ZCD_3_Occupation  = "ZCD_3";
public static String ZCD_4_Informed_Employer  = "ZCD_4";
//public static String ZCD_5_Status_ident  = "ZCD_5";
//public static String ZCD_6_Vehicle_Rego  = "ZCD_6";
//public static String ZCD_7_Police_Badge_Number  = "ZCD_7";
//public static String ZCD_8_Police_Station  = "ZCD_8";
//public static String ZCD_9_Police_Service_Rank  = "ZCD_9";
//public static String ZCD_10_Ship_Name  = "ZCD_10";
public static String ZCD_11_Claim_Number  = "ZCD_11";
public static String ZCD_12_Assign_Bill_to_Code  = "ZCD_12";
public static String ZCD_13_Compensable_Context  = "ZCD_13";
public static String ZCD_14_Payment_Class  = "ZCD_14";
//
/**
 *ZFT Financial segment decodes.
 * ZFT is a CSC specific segment received in a P03 Billing message.
 */
public static String ZFT_1_Charge_Type = "ZFT_1";
public static String ZFT_2_Time_Scale = "ZFT_2";
public static String ZFT_3_Amount_Rebated = "ZFT_3";
public static String ZFT_4_Assignment_Flag = "ZFT_4";
public static String ZFT_5_Assignment_Number = "ZFT_5";
public static String ZFT_6_Requested_Date = "ZFT_6";
public static String ZFT_7_Referral_Date = "ZFT_7";
public static String ZFT_8_Billing_Doctor = "ZFT_8";
public static String ZFT_9_Anaesthetic = "ZFT_9";
public static String ZFT_10_Time_Taken = "ZFT_10";
public static String ZFT_11_Comment_Text = "ZFT_11";
public static String ZFT_12_Invoice_Number = "ZFT_12";
public static String ZFT_13_Print_Flag = "ZFT_13";
public static String ZFT_14_Invoice_Date = "ZFT_14";
public static String ZFT_15_Clinic_Description = "ZFT_15";
public static String ZFT_16_Printer_Destination_Number = "ZFT_16";
public static String ZFT_17_Practice_Code = "ZFT_17";
public static String ZFT_18_Start_Time = "ZFT_18";
public static String ZFT_19_Extended_GST_Amount = "ZFT_19";
public static String ZFT_20_Unit_GST_Amount = "ZFT_20";
public static String ZFT_21_Tax_Code = "ZFT_21";
public static String ZFT_22_GST_Inclusive_Indicator = "ZFT_22";
//
/**
 *ZPD Financial segment decodes.
 * ZPD is a CSC specific segment received in a P03 Billing message.
 */
public static String ZPD_1_History_Indicator  = "ZPD_1";
public static String ZPD_2_Generic_Code  = "ZPD_2";
public static String ZPD_3_Generic_Date  = "ZPD_3";
public static String ZPD_4_Generic_Text  = "ZPD_4";
public static String ZPD_5_Medicare_Suffix  = "ZPD_5";
public static String ZPD_6_Medicare_11th_digit  = "ZPD_6";
public static String ZPD_7_Medicare_Expiry_Date  = "ZPD_7";
public static String ZPD_9_Pension_Exp_Date = "ZPD_9";
public static String ZPD_10_PBS_Safetynet_Number = "ZPD_10";
public static String ZPD_11_Interpreter_Required = "ZPD_11";
public static String ZPD_12_Personal_Contact_Data_Phone_Numbers = "ZPD_12";
public static String ZPD_13_DVA_Card_Type = "ZPD_13";
public static String ZPD_14_Patient_Consent_Registration_Details = "ZPD_14";
public static String ZPD_15_Patient_Consent_Registration_comments = "ZPD_15";
public static String ZPD_16_Feedback_Consent_Code_Qld = "ZPD_16";
public static String ZPD_22_DOB_Accuracy = "ZPD_22";
//
//public static String ZV1_1_Referring_Doctor_History = "ZV1_1";
//public static String ZV1_2_Previous_Name = "ZV1_2";
//public static String ZV1_3_Major_Diagnositic_Category_Code  = "ZV1_3";
//public static String ZV1_4_Diagnosis_Related_Group_Code  = "ZV1_4";
//public static String ZV1_5_Admission_Status_Code  = "ZV1_5";
//public static String ZV1_6_Patient_Category  = "ZV1_6";
//public static String ZV1_7_Patient_Care_Type  = "ZV1_7";
//public static String ZV1_8_Discharge_Legal_Status  = "ZV1_8";
//public static String ZV1_9_Discharge_Comments  = "ZV1_9";
//public static String ZV1_10_Discharge_Care_Referral  = "ZV1_10";
//public static String ZV1_11_Referring_Dr_Name_Free_text  = "ZV1_11";
//public static String ZV1_12_Referring_Dr_Name_Coded  = "ZV1_12";
//public static String ZV1_13_Referring_Dr_Address_Free_text  = "ZV1_13";
//public static String ZV1_14_Referring_Dr_Address_Coded  = "ZV1_14";
//public static String ZV1_15_Referring_Dr_Name_Free_Text  = "ZV1_15";
//public static String ZV1_16_Referring_Dr_Name_Coded  = "ZV1_16";
//public static String ZV1_17_Referring_Dr_Address_Free_text  = "ZV1_17";
//public static String ZV1_18_Referring_Dr_Address_Coded  = "ZV1_18";
//public static String ZV1_19_Current_Inpatient_Status  = "ZV1_19";
//public static String ZV1_20_Attending_DR_provider  = "ZV1_20";
//public static String ZV1_21_Referring_DR_Provider_No  = "ZV1_21";
//public static String ZV1_22_Consulting_DR_Provider_No  = "ZV1_22";
//public static String ZV1_23_Admitting_DR_Provider_No  = "ZV1_23";
public static String ZV1_24_Diagnosis_Description  = "ZV1_24";
//public static String ZV1_25_Diagnosis_Date_Time  = "ZV1_25";
//public static String ZV1_26_Conscious_State  = "ZV1_26";
//public static String ZV1_27_Condition  = "ZV1_27";
//public static String ZV1_28_IV_Therapy  = "ZV1_28";
//public static String ZV1_29_Ventilation  = "ZV1_29";
//public static String ZV1_30_Isolation  = "ZV1_30";
//public static String ZV1_31_Drain_Tubes  = "ZV1_31";
public static String ZV1_32_Financial_Class  = "ZV1_32";
//public static String ZV1_33_ACAS_Status_TX_ACAS_Status = "ZV1_33";
//public static String ZV1_34_Planned_Same_Day_admission_flag = "ZV1_34";
//public static String ZV1_35_Referring_Dr_Postal_Address_Coded = "ZV1_35";
//public static String ZV1_36_Referring_Dr_Postal_Address_Coded = "ZV1_36";
//
public static String ZV1_47_WIESVALUE  = "ZV1_47";
public static String ZV1_48_WIESCoder  = "ZV1_48";
public static String ZV1_49_WIESDate  = "ZV1_49";
public static String ZV1_50_WIESTime  = "ZV1_50";

/** Creates a new instance of CSC 23 */

public static String Repeat_ZCD = "ZCD";
}
