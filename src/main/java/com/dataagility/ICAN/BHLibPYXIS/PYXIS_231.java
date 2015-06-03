package com.dataagility.ICAN.BHLibPYXIS;

//Authors: Ray Fillingham and Norman Soh
//Organisation: The Alfred
//Year: 2005

/**
 * These are the HL7 field location mappings for PYXIS Z segments
 * For PYXIS, all mappings for standard HL7 segments(and HL7 sub field mappings i.e CE, CX, XPN ....)
 * are held in the HL7_23 (or HL7_24) class files.
 */

public class PYXIS_231 {
//
    public static String ZPD = "ZPD";
    public static String ZPD_1_claim_number = "ZPD_1";
    public static String ZPD_2_patient_team = "ZPD_2";
    public static String ZPD_3_patient_height_cm = "ZPD_3";
    public static String ZPD_4_patient_weight_kg = "ZPD_4";
    public static String ZPD_5_patient_body_area_m2 = "ZPD_5";
    public static String ZPD_6_Medicare_Line_Number = "ZPD_6";
    public static String ZPD_7_Medicare_Exp_Date = "ZPD_7";
    public static String ZPD_8_Estimated_DOB = "ZPD_8";
    public static String ZPD_9_HCC_Pension_Exp_Date = "ZPD_9";
    public static String ZPD_10_PBS_Safetynet_Number = "ZPD_10";
    public static String ZPD_11_Interpreter_Required = "ZPD_11";
    public static String ZPD_12_Person_Contact_Phone_Number = "ZPD_12";

    /**
     *ZFT Financial segment decodes.
     * It is identical structure to the CSC ZFT segment.
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

    public static String ZCD_1_Record_Number  = "ZCD_1";
//public static String ZCD_2_Nature_of_Injury  = "ZCD_2";
//public static String ZCD_3_Occupation  = "ZCD_3";
//public static String ZCD_4_Informed_Employer  = "ZCD_4";
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

    public static String Group_FinSet[] = {"FT1", "ZFT"};
}
