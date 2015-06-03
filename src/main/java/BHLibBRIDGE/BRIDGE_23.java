package BHLibBRIDGE;

//Authors: Ray Fillingham and Norman Soh

//Organisation: The Alfred
//Year: 2005

/**
 * These are the HL7 field location mappings for Cerner Z segments
 * For Cerner, all mappings for standard HL7 segments(and HL7 sub field mappings i.e CE, CX, XPN ....)
 * are held in the HL7_23 (or HL7_24) class files.
 */

public class BRIDGE_23 {
//
    /**
     * ZFT Financial segment decodes.
     * ZFT is not really a CERNER segment but is sent as part of a custom written
     * for Bayside Billing application.
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

//public static String ZVI_1_Trauma  ="ZVI_1";
//public static String ZVI_2_Trauma_Date_Time  ="ZVI_2";
//public static String ZVI_3_Last_Trauma_Date_Time  ="ZVI_3";
//public static String ZVI_4_Referring_Comment  ="ZVI_4";
//public static String ZVI_5_Chart_Location  ="ZVI_5";
//public static String ZVI_6_Service_Category  ="ZVI_6";
//public static String ZVI_7_Care_Team  ="ZVI_7";
//public static String ZVI_8_Other_Personnel_Groups  ="ZVI_8";
//public static String ZVI_9_Admit_Mode  ="ZVI_9";
//public static String ZVI_10_Requested_Accomodation  ="ZVI_10";
//public static String ZVI_11_Information_Give_by  ="ZVI_11";
//public static String ZVI_12_Triage_Code  ="ZVI_12";
//public static String ZVI_13_Triage_Date_and_Time  ="ZVI_13";
//public static String ZVI_14_Accommodation_Reason  ="ZVI_14";

    public static String ZPI = "ZPI";
    //public static String ZPI_1_Set_Id_public ="ZPI_1";
//public static String ZPI_2_Patient_Temporary_Address  ="ZPI_2";
//public static String ZPI_3_Residence_Date  ="ZPI_3";
//public static String ZPI_4_Birth_Place  ="ZPI_4";
//public static String ZPI_5_Blood_Type  ="ZPI_5";
//public static String ZPI_6_Conception_Date_and_Time  ="ZPI_6";
//public static String ZPI_7_Death_Date_and_Time  ="ZPI_7";
    public static String ZPI_8_Family_Doctor = "ZPI_8";
    //public static String ZPI_9_Patient_Confidentiality  ="ZPI_9";
    public static String ZPI_10_Other_Provider_Person_Level = "ZPI_10";
    //public static String ZPI_11_Cause_of_Death  ="ZPI_11";
//public static String ZPI_12_Adopted_Indicator  ="ZPI_12";
    public static String ZPI_13_Church = "ZPI_13";
    //public static String ZPI_14_Autopsy_Indicator  ="ZPI_14";
    public static String ZPI_15_Species = "ZPI_15";
//public static String ZPI_16_Historical_Previous_Name ="ZPI_16";
//public static String ZPI_17_Historical_Previous_Patient_Id ="ZPI_17";
//public static String ZPI_18_Chart_Location  ="ZPI_18";
//public static String ZPI_19_Person_Address_Effective  ="ZPI_19";
//public static String ZPI_20_Disease_Alert_Code  ="ZPI_20";
//public static String ZPI_21_Process_Alert_Code  ="ZPI_21";

    public static String ZEI = "ZEI";
    //public static String ZEI_1_Set_Id  ="ZEI_1";
//public static String ZEI_2_Employment_Status_Code  ="ZEI_2";
    public static String ZEI_3_Employer_Name_free_text_only = "ZEI_3";
    public static String ZEI_4_Employer_Address = "ZEI_4";
    public static String ZEI_5_Employer_Phone = "ZEI_5";
//public static String ZEI_6_Employer_Contact_Name  ="ZEI_6";
//public static String ZEI_7_Employee_ID_Number  ="ZEI_7";
//public static String ZEI_8_Person_Occupation_free_text_only  ="ZEI_8";
//public static String ZEI_9_Employment_Start_Date_Time  ="ZEI_9";
//public static String ZEI_10_Employment_End_Date_Time  ="ZEI_10";
//public static String ZEI_11_Employer_Name_Coded  ="ZEI_11";
//public static String ZEI_12_Employer_Tax_Nbr  ="ZEI_12";
//public static String ZEI_13_Job_Code_Class  ="ZEI_13";
//public static String ZEI_14_Position  ="ZEI_14";
//public static String ZEI_15_Job_Title  ="ZEI_15";
//public static String ZEI_15_Contact_Title  ="ZEI_16";

    public static String NTE = "NTE";
    public static String NTE_1_setID = "NTE_1";
    public static String NTE_2_source_of_comment = "NTE_2";
    public static String NTE_3_comment = "NTE_3";

    public static String Group_FinSet[] = {"FT1", "ZFT"};
    public static String Group_Alerts[] = {"AL1", "ZAL"};

    /**
     * Creates a new instance of CERNER_23
     */
    public BRIDGE_23() {
    }

}
