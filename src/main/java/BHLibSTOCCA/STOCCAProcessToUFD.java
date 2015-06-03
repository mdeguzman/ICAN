/*
 * STOCCAProcessToUFD.java
 *
 * Created on 22 November 2005, 11:10
 *
 */

package BHLibSTOCCA;

import BHLibClasses.*;

/**
 * STOCCAProcessToUFD contains the methods required to build a UFD message
 * from a STOCCA HL7 message structure
 * @author Norman Soh and Ray Fillingham 2005
 */
public class STOCCAProcessToUFD extends ProcessSegmentsToUFD {
    /**
     * A class wide message class containing the input HL7 message
     */
    public HL7Message mInHL7Message;
    /**
     * A class wide ZBX count value used to build ZBX segments
     */
    public int mZBXCount = 1;
    public String mEnvironment = "";
    //--------------------------------------------------------------------------
    /**
     * Creates a new instance of STOCCAProcessToUFD
     * @param pHL7Message HL7 message text string
     * @throws BHLibClasses.ICANException ICANException exception class object
     */
    public STOCCAProcessToUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "B";    // STOCCAProcessToUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process the message from STOCCA to UFD structure
     * @throws BHLibClasses.ICANException ICANException exception class object
     * @return UFD HL7 message text string
     */
    public String processMessage() throws ICANException {
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));

        if (mInHL7Message.isEvent("P03")) {
            HL7Segment aMSHSegment = processMSHToUFD();
            HL7Segment aEVNSegment = processEVNToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Group aFinGroup = processFinGroups_ToUFD();
            HL7Group aZCDZBXGroup = processZCDZBXs_ToUFD();
            HL7Group aGT1Group = processGT1s_ToUFD();

            aMSHSegment = processP03MSHToUFD(aMSHSegment);
            aPIDSegment = processP03PIDToUFD(aPIDSegment);
            aEVNSegment = processP03EVNToUFD(aEVNSegment);

            aOutHL7Message.setSegment(aMSHSegment.getSegment());
            aOutHL7Message.append(aEVNSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aFinGroup);
            aOutHL7Message.append(aZCDZBXGroup);
            aOutHL7Message.append(aGT1Group);
        }
        if (aOutHL7Message.getMessage().length() > 0) {
            aOutHL7Message.append(setupZBX("MESSAGE", "SOURCE_ID", aInMSHSegment.get(HL7_23.MSH_10_message_control_ID)));
        }
        return aOutHL7Message.getMessage();
    }
    //--------------------------------------------------------------------------
    /**
     * Process MSH segment according to P03 requirements
     * @param pMSHSegment MSH segment class object
     * @return MSH segment class object
     */
    public HL7Segment processP03MSHToUFD(HL7Segment pMSHSegment) {
        pMSHSegment.set(HL7_23.MSH_3_sending_application, "STOCCA");
        pMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");
        return pMSHSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process EVN segment according to P03 requirements
     * @param pEVNSegment EVN segment class object
     * @return EVN segment class object
     */
    public HL7Segment processP03EVNToUFD(HL7Segment pEVNSegment) {
        pEVNSegment.set(HL7_23.EVN_1_event_type_code, "P03");
        return pEVNSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process PID segment according to P03 requirements
     * @param pPIDSegment PID segment class object
     * @return PID segment class object
     */
    public HL7Segment processP03PIDToUFD(HL7Segment pPIDSegment) {
        HL7Segment aInPIDSegment = new HL7Segment(pPIDSegment.getSegment());
        HL7Segment aOutPIDSegment = new HL7Segment(HL7_23.PID);
        aOutPIDSegment.setSegment(aInPIDSegment.getSegment());
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");

        //process PID_3 field
        String aPID3FieldArray[] = aInPIDSegment.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        HL7Field aPID3Field = new HL7Field();
        int aPID3FieldCount = aPID3FieldArray.length;
        for (int i = 0; i < aPID3FieldCount; i++) {
            aPID3Field.setField(aPID3FieldArray[i]);
            if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("AL") ||
                    aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("MR")) {
                if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("AL")) {
                    String aID = aPID3Field.getSubField(HL7_23.CX_ID_number);
                    aPID3Field.setSubField("PHA:".concat(aID), HL7_23.CX_ID_number);
                } else {
                    //leave ID as is...
                }
                aPID3Field.setSubField("MR", HL7_23.CX_ID_type_code);
            }
            aPID3FieldArray[i] = aPID3Field.getField();
        }
        aOutPIDSegment.setRepeatFields(HL7_23.PID_3_patient_ID_internal, aPID3FieldArray);

        return aOutPIDSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process Financial groups according to UFD requirements
     * @return Financial group class object
     */
    public HL7Group processFinGroups_ToUFD() {
        String aFinGroupID[] = STOCCA_231.Group_FinSet;
        int aFinGroupCount = mInHL7Message.countGroups(aFinGroupID);
        HL7Group aInFinGroup;
        HL7Group aOutFinGroup = new HL7Group();
        HL7Group aOutFT1Group = new HL7Group();
        HL7Group aOutZBXGroup = new HL7Group();
        HL7Segment aOutFT1Segment;
        HL7Segment aOutZBXSegment;
        HL7Segment aInFT1Segment = new HL7Segment("FT1");
        HL7Segment aInZFTSegment = new HL7Segment("ZBX");

        CodeLookUp aLU = new CodeLookUp("DefaultValues.table", mEnvironment);
        String aDefaultDr = aLU.getValue("PharmacyDr");

        for (int i = 1; i <= aFinGroupCount; i++) {
            aInFinGroup = new HL7Group(mInHL7Message.getGroup(aFinGroupID, i));
            aInFT1Segment.setSegment(aInFinGroup.getSegment("FT1"));
            aInZFTSegment.setSegment(aInFinGroup.getSegment("ZFT"));
            if (aInFT1Segment.getSegment().length() > 0) {
                aOutFT1Segment = new HL7Segment("FT1");
                aOutFT1Segment.set(HL7_23.FT1_4_Transaction_Date, aInFT1Segment.get(HL7_23.FT1_4_Transaction_Date));
                aOutFT1Segment.set(HL7_23.FT1_6_Transaction_Type, aInFT1Segment.get(HL7_23.FT1_6_Transaction_Type));
                aOutFT1Segment.set(HL7_23.FT1_7_Transaction_Code, HL7_23.CE_ID_code, "PHA");
                aOutFT1Segment.set(HL7_23.FT1_7_Transaction_Code, HL7_23.CE_text,
                        aInFT1Segment.get(HL7_23.FT1_7_Transaction_Code, HL7_23.CE_text));
                aOutFT1Segment.set(HL7_23.FT1_10_Transaction_Quantity, aInFT1Segment.get(HL7_23.FT1_10_Transaction_Quantity));
                aOutFT1Segment.set(HL7_23.FT1_11_Transaction_Amount_Extended, HL7_23.CP_1_price,
                        aInFT1Segment.get(HL7_23.FT1_11_Transaction_Amount_Extended, HL7_23.CP_1_price));
                aOutFT1Segment.set(HL7_23.FT1_20_Performed_by_Code, HL7_23.XCN_ID_num, aDefaultDr);
                if (aInFT1Segment.get(HL7_23.FT1_20_Performed_by_Code, HL7_23.XCN_ID_num).length() == 0) {
                    aOutFT1Segment.set(HL7_23.FT1_21_Ordered_by_Code, HL7_23.XCN_ID_num, aDefaultDr);
                } else {
                    String aIDNum = aInFT1Segment.get(HL7_23.FT1_20_Performed_by_Code, HL7_23.XCN_ID_num);
                    aOutFT1Segment.set(HL7_23.FT1_21_Ordered_by_Code, HL7_23.XCN_ID_num, aIDNum);
                }
                aOutFT1Group.append(aOutFT1Segment);
            }
            //Create ZBX Seg
            String aFT1SetID = aInFT1Segment.get(HL7_23.FT1_1_Set_ID);
            if (aFT1SetID.length() == 0) {
                aFT1SetID = "1";
            }
            if (aInZFTSegment.getSegment().length() > 0) {
                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "UPDATE_TYPE");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aInZFTSegment.get(STOCCA_231.ZFT_1_Charge_Type));
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "ASSIGNMENT_FLAG");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, "N");
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "REQUESTED_DATE");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aInFT1Segment.get(HL7_23.FT1_4_Transaction_Date));
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "REFERRAL_DATE");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aInFT1Segment.get(HL7_23.FT1_4_Transaction_Date));
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "BILLING_DOCTOR");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aDefaultDr);
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "INVOICE_NUMBER");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aInZFTSegment.get(STOCCA_231.ZFT_12_Invoice_Number));
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "PRINT_FLAG");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, "N");
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "INVOICE_DATE");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aInZFTSegment.get(STOCCA_231.ZFT_14_Invoice_Date));
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);
            }
        }
        aOutFinGroup.append(aOutFT1Group);
        aOutFinGroup.append(aOutZBXGroup);
        return aOutFinGroup;
    }
    //--------------------------------------------------------------------------
    /**
     * Process ZCD segment according to UFD requirements
     * @return ZBX group class object
     */
    public HL7Group processZCDZBXs_ToUFD() {
        HL7Segment aInZCDSegment = new HL7Segment(mInHL7Message.getSegment("ZCD"));
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PV1));
        HL7Segment aOutZBXSegment = new HL7Segment(k.NULL);
        HL7Group aOutZBXGroup = new HL7Group();
        if (aInZCDSegment.getSegment().length() > 0) {
            aOutZBXSegment = new HL7Segment("ZBX");
            aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
            aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
            aOutZBXSegment.set(HL7_24.ZBX_3_field, "CLAIM_NUMBER");
            String aClaimNum = aInZCDSegment.get(STOCCA_231.ZCD_11_Claim_Number);
            aOutZBXSegment.set(HL7_24.ZBX_4_value, aClaimNum);
            aOutZBXGroup.append(aOutZBXSegment);

            aOutZBXSegment = new HL7Segment("ZBX");
            aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXCount++));
            aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
            aOutZBXSegment.set(HL7_24.ZBX_3_field, "ASSIGN_BILL_NUMBER");
            String aFinClass = aInPV1Segment.get(HL7_23.PV1_20_financial_class);
            CodeLookUp aLUCompensableClass = new CodeLookUp("ValidateBillingCompensableClass.table", mEnvironment);
            aFinClass = aLUCompensableClass.getValue(aFinClass);
            if (aFinClass.length() == 0) {
                aOutZBXSegment.set(HL7_24.ZBX_4_value, "P");
            } else {
                aOutZBXSegment.set(HL7_24.ZBX_4_value, "A");
            }
            aOutZBXGroup.append(aOutZBXSegment);
        }
        return aOutZBXGroup;
    }
}
