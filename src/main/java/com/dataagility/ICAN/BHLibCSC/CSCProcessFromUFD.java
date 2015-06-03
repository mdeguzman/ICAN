/*
 * CSCProcessFromUFD.java
 *
 * Created on 11 October 2005, 17:04
 *
 */
package com.dataagility.ICAN.BHLibCSC;

import com.dataagility.ICAN.BHLibClasses.*;
import java.text.*;

/**
 * CSCProcessFromUFD contains the methods required to build a CSC message
 * from a UFD HL7 message structure
 * @author Ray Fillingham and Norman Soh
 */
public class CSCProcessFromUFD extends ProcessSegmentsFromUFD {

    /**
     * Constant class
     */
    public BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public String aAssignBillNo = "";
    boolean aProcess = true;
    /**
     * Class wide HL7Message object
     */
    public HL7Message aInHL7Message;
    //--------------------------------------------------------------------------

    /**
     * This constructor creates a new instance of CSCProcessFromUFD passing a HL7 UFD
     * message structure
     * @param pHL7Message HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public CSCProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";    // CSCProcessFromUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------

    /**
     * This method contains the methods required to build a CSC HL7 message
     * @return CSC HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public String[] processMessage() throws ICANException {

        String aCSCMessageArray[] = {k.NULL, k.NULL, k.NULL};
        HL7Message aOutHL7Message = new HL7Message();
        aInHL7Message = new HL7Message(mHL7Message);

        if (aInHL7Message.isEvent("A03, A04, A08, A28, A31")) {
            HL7Segment aMSHSegment = processMSHFromUFD("CSC");
            HL7Segment aEVNSegment = processEVNFromUFD();
            aMSHSegment = checkMSHTime(aMSHSegment);
            aEVNSegment = checkEVNTime(aEVNSegment);
            HL7Segment aPIDSegment = processPIDFromUFD();
            HL7Group aNK1Group = processNK1s_FromUFD();
            HL7Segment aPV1Segment = processPV1FromUFD();
            HL7Group aAL1Group = processAL1s_FromUFD();
            HL7Segment aIN1Segment = new HL7Segment(HL7_23.IN1);
            HL7Segment aIN2Segment = new HL7Segment(HL7_23.IN2);

            HL7Segment aZPDSegment = new HL7Segment("ZPD");

            aMSHSegment = processCSC_MSH(aMSHSegment);
            aEVNSegment = processCSC_EVN(aEVNSegment);
            aPIDSegment = processCSC_PID(aPIDSegment);
            aNK1Group = processCSC_NK1(aNK1Group);
            //aOBXGroup = processCSC_OBX(aOBXGroup);
            aPV1Segment = processCSC_PV1(aPV1Segment);
            aAL1Group = processCSC_AL1(aAL1Group);
            aIN1Segment = processCSC_IN1(aIN1Segment);
            aIN2Segment = processCSC_IN2(aIN2Segment);
            aZPDSegment = processCSC_ZPD(aZPDSegment);

            //Check environment for TEST.  If running in TEST, add ".TEST" to MSH-5
            if (mEnvironment.indexOf("TEST") >= 0) {
                String aReceivingAppTemp = aMSHSegment.get(HL7_23.MSH_5_receiving_application);
                aMSHSegment.set(HL7_23.MSH_5_receiving_application, aReceivingAppTemp + ".TEST");
            }

            aOutHL7Message.setSegment(aMSHSegment.getSegment());
            aOutHL7Message.append(aEVNSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aNK1Group);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aAL1Group);
            aOutHL7Message.append(aIN1Segment);
            aOutHL7Message.append(aIN2Segment);
            aOutHL7Message.append(aZPDSegment);

            aCSCMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aCSCMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aCSCMessageArray[2] = aOutHL7Message.getMessage();
        } else if (aInHL7Message.isEvent("P03")) {
            HL7Segment aMSHSegment = processMSHFromUFD("CSC");
            //Set the receiving application
            String aReceivingApp = aMSHSegment.get(HL7_23.MSH_5_receiving_application) + "-" + mFacility;
            aMSHSegment.set(HL7_23.MSH_5_receiving_application, aReceivingApp);
            HL7Segment aEVNSegment = processEVNFromUFD();
            aMSHSegment = checkMSHTime(aMSHSegment);
            aEVNSegment = checkEVNTime(aEVNSegment);
            HL7Segment aPIDSegment = processPIDFromUFD();
            HL7Segment aPV1Segment = processPV1FromUFD();
            HL7Group aFinGroup = processP03FinGroups_FromUFD();
            //HL7Segment aZCDSegment = processP03ZCDFromUFD();
            HL7Group aGT1Group = processGT1s_FromUFD();
            HL7Group aACCGroup = processP03ACCs_FromUFD();

            aPIDSegment = processP03PIDFromUFD(aPIDSegment);
            aPV1Segment = processP03PV1FromUFD(aPV1Segment);
            HL7Segment aZCDSegment = processP03ZCDFromUFD(aPV1Segment);
            aGT1Group = processP03GT1s_FromUFD(aGT1Group, aPV1Segment);

            //Check environment for TEST.  If running in TEST, add ".TEST" to MSH-5
            if (mEnvironment.indexOf("TEST") >= 0) {
                String aReceivingAppTemp = aMSHSegment.get(HL7_23.MSH_5_receiving_application);
                aMSHSegment.set(HL7_23.MSH_5_receiving_application, aReceivingAppTemp + ".TEST");
            }

            aOutHL7Message.setSegment(aMSHSegment.getSegment());
            aOutHL7Message.append(aEVNSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aFinGroup);
            aOutHL7Message.append(aZCDSegment);
            aOutHL7Message.append(aGT1Group);
            aOutHL7Message.append(aACCGroup);

            aCSCMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aCSCMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aCSCMessageArray[2] = aOutHL7Message.getMessage();
        }
        return aCSCMessageArray;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for MSH segment
     * @param pMSHSegment MSH segment class object
     * @param pPV1Segment PV1 segment class object
     * @return MSH segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processCSC_MSH(HL7Segment pMSHSegment) throws ICANException {
        HL7Message aMessage = new HL7Message(mHL7Message);
        HL7Segment aOrigMSHSeg = new HL7Segment(aMessage.getSegment(HL7_23.MSH));
        String aSpeciesFlag = getFromZBX("MESSAGE", "NEW_PATIENT_FLAG");
        String aA28Flag = getFromZBX("MESSAGE", "A28_FLAG");
        if (aSpeciesFlag.equalsIgnoreCase("Y") || aA28Flag.equalsIgnoreCase("TRUE")) {
            pMSHSegment.set(HL7_23.MSH_9_2_trigger_event, "A28");
        } else {
            pMSHSegment.set(HL7_23.MSH_9_2_trigger_event, "A31");
        }
        //Set the receiving application
        String aReceivingApp = pMSHSegment.get(HL7_23.MSH_5_receiving_application) + "-" + mFacility;
        pMSHSegment.set(HL7_23.MSH_5_receiving_application, aReceivingApp);
        //Set the processing id
        pMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        //Set the version id
        pMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");
        //Move sending_facility to receiving_facility
        pMSHSegment.set(HL7_23.MSH_6_receiving_facility, aOrigMSHSeg.get(HL7_23.MSH_4_sending_facility));
        return pMSHSegment;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for EVN segment
     * @param pEVNSegment EVN segment class object
     * @return EVN segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processCSC_EVN(HL7Segment pEVNSegment) throws ICANException {
        String aSpeciesFlag = getFromZBX("MESSAGE", "NEW_PATIENT_FLAG");
        String aA28Flag = getFromZBX("MESSAGE", "A28_FLAG");
        if (aSpeciesFlag.equalsIgnoreCase("Y") || aA28Flag.equalsIgnoreCase("TRUE")) {
            pEVNSegment.set(HL7_23.EVN_1_event_type_code, "A28");
        } else {
            pEVNSegment.set(HL7_23.EVN_1_event_type_code, "A31");
        }
        return pEVNSegment;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for PID segment
     * @param pPIDSegment PID segment class object
     * @return PID segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processCSC_PID(HL7Segment pPIDSegment) throws ICANException {
        HL7Message aMessage = new HL7Message(mHL7Message);

        String aAssigningAuthority = mFacility.concat("UR");
        pPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aAssigningAuthority);

        //process PID-3 ID internal number to 6 / 7 digit number
        String aPID3Array[] = pPIDSegment.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        HL7Field aPID3Field = new HL7Field();
        CodeLookUp aLookUp = new CodeLookUp("SDMH_6Digit_UR.table", mEnvironment);
        int aPID3ArrayCount = aPID3Array.length;
        String aSDMHIdentifier = k.NULL;

        for (int i = 0; i < aPID3ArrayCount; i++) {
            aPID3Field.setField(aPID3Array[i]);
            if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("PI")) {
                if (mFacility.equalsIgnoreCase("SDMH") &&
                        Integer.parseInt(aPID3Field.getSubField(HL7_23.CX_ID_number)) < 899546) {
                    aSDMHIdentifier = aLookUp.getValue(aPID3Field.getSubField(HL7_23.CX_ID_number));
                    if (!aSDMHIdentifier.equalsIgnoreCase(k.NULL)) {
                        aPID3Field.setSubField(aSDMHIdentifier, HL7_23.CX_ID_number);
                        aPID3Array[i] = aPID3Field.getField();
                    }
                }
            }
        }
        pPIDSegment.setRepeatFields(HL7_23.PID_3_patient_ID_internal, aPID3Array);

        //process PID-5 patient name - only pass name with name type of "L"
        String aPID5Array[] = pPIDSegment.getRepeatFields(HL7_23.PID_5_patient_name);
        HL7Field aPID5Field = new HL7Field();
        int aPID5ArrayCount = aPID5Array.length;
        String aPID5String = k.NULL;

        for (int i = 0; i < aPID5ArrayCount; i++) {
            aPID5Field.setField(aPID5Array[i]);
            if (aPID5Field.getSubField(HL7_23.XPN_name_type).equalsIgnoreCase("L")) {
                if (aPID5Field.getSubField(HL7_23.XPN_prefix).equalsIgnoreCase("Rabbi")) {
                    aPID5Field.setSubField("RAB", HL7_23.XPN_prefix);
                }
                aPID5String = aPID5Field.getField().toUpperCase();
                i = aPID5ArrayCount;
                break;
            }
        }
        pPIDSegment.setField(aPID5String, HL7_23.PID_5_patient_name);

        //Change PID_9 to all uppercase
        String aPID9Field = pPIDSegment.get(HL7_23.PID_9_patient_alias);
        aPID9Field = aPID9Field.toUpperCase();
        pPIDSegment.set(HL7_23.PID_9_patient_alias, aPID9Field);

        //Check to see if PID-4 is empty with no identifier
        if (pPIDSegment.get(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number).equalsIgnoreCase(k.NULL)) {
            pPIDSegment.set(HL7_23.PID_4_alternate_patient_ID, k.NULL);
        }

        //Add preceding spaces to PID-19 if C-U or any three letter code is found
        if (pPIDSegment.get(HL7_23.PID_19_SSN_number).length() == 3) {
            String aSpacePad = "            "; //12 blank spaces
            String aPID_19 = pPIDSegment.get(HL7_23.PID_19_SSN_number);
            aPID_19 = aSpacePad.concat(aPID_19);
            pPIDSegment.set(HL7_23.PID_19_SSN_number, aPID_19);
        }

        //process home phone number
        HL7Field aHomePhoneField = new HL7Field();
        String aTelephoneNumber = k.NULL;
        String aTelecomUse = k.NULL;
        String aHomePhoneArray[] = pPIDSegment.getRepeatFields(HL7_23.PID_13_home_phone);
        int aHomePhoneArrayCount = aHomePhoneArray.length;
        for (int i = 0; i < aHomePhoneArrayCount; i++) {
            aHomePhoneField.setField(aHomePhoneArray[i]);
            aTelephoneNumber = aHomePhoneField.getSubField(HL7_23.XTN_telephone_number);
            aTelecomUse = aHomePhoneField.getSubField(HL7_23.XTN_telecom_use);
            if (aTelecomUse.equalsIgnoreCase("PRN") &&
                    !aTelephoneNumber.equalsIgnoreCase(k.NULL)) {
                pPIDSegment.set(HL7_23.PID_13_home_phone, aHomePhoneField.getField());
            }
        }

        return pPIDSegment;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for NK1 segment
     * @param pNK1Group NK1 segment class object
     * @return NK1 group class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Group processCSC_NK1(HL7Group pNK1Group) throws ICANException {
        int aNK1GroupCount = pNK1Group.countSegments();
        HL7Segment aNK1Segment = new HL7Segment("NK1");
        HL7Group aOutNK1Group = new HL7Group();
        boolean aFoundC = false;
        String aName = k.NULL;
        for (int i = 1; i <= aNK1GroupCount; i++) {
            aNK1Segment.setSegment(pNK1Group.getSegment(i));
            if (aNK1Segment.getField(HL7_23.NK1_7_contact_role).equalsIgnoreCase("C")) {
                aFoundC = true;
                aName = aNK1Segment.get(HL7_23.NK1_2_next_of_kin_name);
                aName = aName.toUpperCase();
                aNK1Segment.set(HL7_23.NK1_2_next_of_kin_name, aName);
                aOutNK1Group.append(aNK1Segment);
            }
        }
        if (aFoundC == false) {
            aNK1Segment.setSegment(pNK1Group.getSegment(1));
            aName = aNK1Segment.get(HL7_23.NK1_2_next_of_kin_name);
            aName = aName.toUpperCase();
            aNK1Segment.set(HL7_23.NK1_2_next_of_kin_name, aName);
            aOutNK1Group.append(aNK1Segment);
        }
        pNK1Group.setGroup(aOutNK1Group.getGroup());
        if (pNK1Group.getGroup().length() <= 4) {
            pNK1Group.setGroup(k.NULL);
        }
        return pNK1Group;
    }

    //--------------------------------------------------------------------------
    /**
     * CSC specific processing for ZBX segment
     * @param pZPDSegment ZPD segment class object
     * @return PID segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processCSC_ZPD(HL7Segment pZPDSegment) throws ICANException {
        String aMEDEXPDT = getFromZBX("PMI", "MEDICARE_EXPIRY");
        String aHCCPENSEXP = getFromZBX("PMI", "PENSION_EXPIRY_DATE");
        String aPBSSAFETYNET = getFromZBX("PMI", "PBS_SAFETYNET_NUMBER");
        String aINTERPREQ = getFromZBX("PMI", "INTERPRETER");
        String aDVACARDTYP = getFromZBX("PMI", "DVA_CARD_TYPE");
        String aDOB_ACCURACY = getFromZBX("PMI", "DOB_ACCURACY");

        if (aINTERPREQ.length() != 0) {
            pZPDSegment.set(CSC_23.ZPD_11_Interpreter_Required, aINTERPREQ);
        }

        if (aMEDEXPDT.length() > 3) {
            //convert the date format from MM/YYYY to YYYYMM
            aMEDEXPDT = aMEDEXPDT.substring(3, aMEDEXPDT.length()).concat(aMEDEXPDT.substring(0, 2));
        }
        pZPDSegment.set(CSC_23.ZPD_7_Medicare_Expiry_Date, aMEDEXPDT);


        if (aHCCPENSEXP.length() != 0) {
            pZPDSegment.set(CSC_23.ZPD_9_Pension_Exp_Date, aHCCPENSEXP);
        }

        if (aPBSSAFETYNET.length() != 0) {
            pZPDSegment.set(CSC_23.ZPD_10_PBS_Safetynet_Number, aPBSSAFETYNET);
        }

        if (aDVACARDTYP.length() != 0) {
            pZPDSegment.set(CSC_23.ZPD_13_DVA_Card_Type, aDVACARDTYP);
        }

        if (aDOB_ACCURACY.length() != 0) {
            pZPDSegment.set(CSC_23.ZPD_22_DOB_Accuracy, aDOB_ACCURACY);
        }

        return pZPDSegment;
    }

    //--------------------------------------------------------------------------
    /**
     * CSC specific processing for PV1 segment
     * @param pPV1Segment PV1 segment class object
     * @return PV1 segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processCSC_PV1(HL7Segment pPV1Segment) throws ICANException {
        String aOtherProvider = k.NULL;
        HL7Segment aPV1SegmentTemp = new HL7Segment("PV1");
        aPV1SegmentTemp.setField("1", HL7_23.PV1_1_set_ID);
        aOtherProvider = getFromZBX("VISIT", "GPCODE");
        aPV1SegmentTemp.setField(aOtherProvider, HL7_23.PV1_8_referring_doctor);
        if (mFacility.equalsIgnoreCase("SDMH")) {
            if (!aPV1SegmentTemp.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num).equalsIgnoreCase(k.NULL)) {
                aPV1SegmentTemp.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_type, "R");
            }
        } else {
            if (!aPV1SegmentTemp.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num).equalsIgnoreCase(k.NULL)) {
                aPV1SegmentTemp.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_type, "RD");
            }
        }

        aPV1SegmentTemp.setField(pPV1Segment.getField(HL7_23.PV1_20_financial_class), HL7_23.PV1_20_financial_class);
        pPV1Segment.setSegment(aPV1SegmentTemp.getSegment());
        return pPV1Segment;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for AL1 group
     * @param pAL1Group AL1 segment class object
     * @return AL1 group class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Group processCSC_AL1(HL7Group pAL1Group) throws ICANException {
        HL7Group aAL1OutGroup = new HL7Group();
        int aAL1Count = pAL1Group.countSegments(HL7_23.AL1);
        HL7Segment aAL1Segment = new HL7Segment(k.NULL);
        if (aAL1Count > 0) {
            aAL1Segment.setSegment(pAL1Group.getSegment(HL7_23.AL1, 1));
        }
        aAL1OutGroup.append(aAL1Segment);
        return aAL1OutGroup;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for IN1 segment
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     * @param pIN1Segment IN1 segment class object
     * @return IN1 segment class object
     */
    public HL7Segment processCSC_IN1(HL7Segment pIN1Segment) throws ICANException {
        if (!isZBXValued("PMI", "OCCUPATION")) {
            pIN1Segment.setSegment(k.NULL);
        }
        return pIN1Segment;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for IN2 segment
     * @param pIN2Segment IN2 segment class object
     * @return IN2 segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processCSC_IN2(HL7Segment pIN2Segment) throws ICANException {
        String aOccupationValue = k.NULL;
        if (isZBXValued("PMI", "OCCUPATION")) {
            aOccupationValue = getFromZBX("PMI", "OCCUPATION");
            pIN2Segment.setField(aOccupationValue, HL7_23.IN2_46_job_title);
        } else {
            pIN2Segment.setSegment(k.NULL);
        }
        return pIN2Segment;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for PID segment for P03 message
     * @param pPIDSegment PID segment
     * @return PID segment
     */
    public HL7Segment processP03PIDFromUFD(HL7Segment pPIDSegment) {
        HL7Segment aOutPIDSegment = new HL7Segment(k.NULL);

        //process PID_3 field
        HL7Field aPID3Field = new HL7Field();
        if (pPIDSegment.getSegment().length() > 0) {
            aOutPIDSegment = new HL7Segment(pPIDSegment.getSegment());
            String aPID3FieldArray[] = aOutPIDSegment.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
            int aPID3FieldArrayCount = aPID3FieldArray.length;
            for (int i = 0; i < aPID3FieldArrayCount; i++) {
                aPID3Field.setField(aPID3FieldArray[i]);
                if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("AL") ||
                        aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("PI")) {
                    if (mFacility.matches("BHH")) {
                        aPID3Field.setSubField("MR", HL7_23.CX_ID_type_code);
                    } else {
                        NumberFormat formatter = new DecimalFormat("0000000");
                        String aID = aPID3Field.getSubField(HL7_23.CX_ID_number);
                        char aChar = aID.charAt(0);
                        if (Character.isDigit(aChar)) {
                            aID = formatter.format(Integer.parseInt(aID));
                        }
                        aPID3Field.setSubField(aID, HL7_23.CX_ID_number);
                    }
                    aPID3FieldArray[i] = aPID3Field.getField();
                }
            }
            aOutPIDSegment.setRepeatFields(HL7_23.PID_3_patient_ID_internal, aPID3FieldArray);
        }

        //process PID_5 field
        String aPID5FieldArray[] = aOutPIDSegment.getRepeatFields(HL7_23.PID_5_patient_name);
        HL7Field aPID5Field = new HL7Field();
        int aPID5FieldCount = aPID5FieldArray.length;
        for (int i = 0; i < aPID5FieldCount; i++) {
            aPID5Field.setField(aPID5FieldArray[i]);
            if (aPID5Field.getSubField(HL7_23.XPN_name_type).equalsIgnoreCase("L")) {
                aOutPIDSegment.set(HL7_23.PID_5_patient_name, aPID5Field.getField());
                break;
            }
        }
        aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XCN_prefix, k.NULL);

        //process PID_11 field patient address
        String aAddress = aOutPIDSegment.get(HL7_23.PID_11_patient_address, 1);
        HL7Field aInPID_11Field = new HL7Field(aAddress);
        HL7Field aOutPID_11Field = new HL7Field(k.NULL);
        aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_street_1), HL7_23.XAD_street_1);
        aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_street_2), HL7_23.XAD_street_2);
        aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_city), HL7_23.XAD_city);
        aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_state_or_province), HL7_23.XAD_state_or_province);
        aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_zip), HL7_23.XAD_zip);
        aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_country), HL7_23.XAD_country);
        aOutPIDSegment.set(HL7_23.PID_11_patient_address, aOutPID_11Field.getField());

        //process PID_13 field patient home phone
        String aPhone = aOutPIDSegment.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.set(HL7_23.PID_13_home_phone, aPhone);

        //process PID_14 field business phone
        String aBusPhone = aOutPIDSegment.get(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.set(HL7_23.PID_14_business_phone, aBusPhone);

        return aOutPIDSegment;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for PV1 segment for P03 message
     * @param pPV1Segment PV1 segment
     * @return PV1 segment
     */
    public HL7Segment processP03PV1FromUFD(HL7Segment pPV1Segment) {
        HL7Segment aInPV1Segment = pPV1Segment;
        HL7Segment aInGT1Segment = new HL7Segment(aInHL7Message.getSegment(HL7_23.GT1, 1));
        HL7Segment aInMSHSegment = new HL7Segment(aInHL7Message.getSegment(HL7_23.MSH));
        HL7Segment aOutPV1Segment = new HL7Segment(HL7_23.PV1);
        aOutPV1Segment.set(HL7_23.PV1_1_set_ID, "1");
        String aCopyPV1Fields[] = {
            HL7_23.PV1_2_patient_class
        };
        aOutPV1Segment.linkTo(aInPV1Segment);
        aOutPV1Segment.copyFields(aCopyPV1Fields);

        if (aInGT1Segment.get(HL7_23.GT1_2_guarantor_number, HL7_23.CX_ID_number).length() == 0) {
            if (mFacility.matches("CGMC")) {
                CodeLookUp mLook = new CodeLookUp("CGMC-STOCCA_PayClass.table", mEnvironment);
                String aFinClass = aInPV1Segment.get(HL7_23.PV1_20_financial_class);
                aFinClass = mLook.getValue(aFinClass);
                aOutPV1Segment.set(HL7_23.PV1_20_financial_class, aFinClass);
            } else if (mFacility.matches("ALF")) {
                CodeLookUp mLook = new CodeLookUp("ALF-STOCCA_PayClass.table", mEnvironment);
                String aFinClass = aInPV1Segment.get(HL7_23.PV1_20_financial_class);
                aFinClass = mLook.getValue(aFinClass);
                aOutPV1Segment.set(HL7_23.PV1_20_financial_class, aFinClass);
            } else if (mFacility.matches("BHH")) {
                CodeLookUp mLook = new CodeLookUp("STOCCA_PayCl_CSCB.table", mEnvironment);
                String aFinClass = aInPV1Segment.get(HL7_23.PV1_20_financial_class);
                aFinClass = mLook.getValue(aFinClass);
                aOutPV1Segment.set(HL7_23.PV1_20_financial_class, aFinClass);
            }
        } else {
            String aGuarantorNum = aInGT1Segment.get(HL7_23.GT1_2_guarantor_number);
            aOutPV1Segment.set(HL7_23.PV1_20_financial_class, HL7_23.CX_ID_number, aGuarantorNum);
        }
        return aOutPV1Segment;

    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for Fin group for P03 message
     * @return Financial group containing FT1 and ZFT segments
     */
    public HL7Group processP03FinGroups_FromUFD() {
        HL7Group aOutFinGroup = new HL7Group();
        HL7Segment aOutFT1Segment = new HL7Segment(k.NULL);
        HL7Segment aOutZFTSegment = new HL7Segment(k.NULL);
        HL7Segment aInZBXSegment = new HL7Segment(k.NULL);
        int aFT1SegmentCount = aInHL7Message.countSegments("FT1");
        int aZBXSegmentCount = aInHL7Message.countSegments("ZBX");

        for (int i = 1; i <= aFT1SegmentCount; i++) {
            aOutFT1Segment.setSegment(aInHL7Message.getSegment("FT1", i));
            aOutZFTSegment = new HL7Segment("ZFT");
            for (int x = 1; x <= aZBXSegmentCount; x++) {
                aInZBXSegment.setSegment(aInHL7Message.getSegment(HL7_24.ZBX, x));
                String aSequenceNum = aInZBXSegment.get(HL7_24.ZBX_5_type);
                if (aSequenceNum.equalsIgnoreCase(Integer.toString(i))) {
                    String aGroupID = aInZBXSegment.get(HL7_24.ZBX_2_group);
                    String aItemID = aInZBXSegment.get(HL7_24.ZBX_3_field);
                    String aValue = aInZBXSegment.get(HL7_24.ZBX_4_value);
                    if (aItemID.equalsIgnoreCase("UPDATE_TYPE")) {
                        aOutZFTSegment.set(CSC_23.ZFT_1_Charge_Type, aValue);
                    } else if (aItemID.equalsIgnoreCase("ASSIGNMENT_FLAG")) {
                        aOutZFTSegment.set(CSC_23.ZFT_4_Assignment_Flag, aValue);
                    } else if (aItemID.equalsIgnoreCase("REQUESTED_DATE")) {
                        aOutZFTSegment.set(CSC_23.ZFT_6_Requested_Date, aValue);
                    } else if (aItemID.equalsIgnoreCase("REFERRAL_DATE")) {
                        aOutZFTSegment.set(CSC_23.ZFT_7_Referral_Date, aValue);
                    } else if (aItemID.equalsIgnoreCase("BILLING_DOCTOR")) {
                        aOutZFTSegment.set(CSC_23.ZFT_8_Billing_Doctor, aValue);
                    } else if (aItemID.equalsIgnoreCase("INVOICE_NUMBER")) {
                        aOutZFTSegment.set(CSC_23.ZFT_12_Invoice_Number, aValue);
                    } else if (aItemID.equalsIgnoreCase("PRINT_FLAG")) {
                        aOutZFTSegment.set(CSC_23.ZFT_13_Print_Flag, aValue);
                    } else if (aItemID.equalsIgnoreCase("INVOICE_DATE")) {
                        aOutZFTSegment.set(CSC_23.ZFT_14_Invoice_Date, aValue);
                    } else if (aItemID.equalsIgnoreCase("CLINIC_NAME")) {
                        aOutZFTSegment.set(CSC_23.ZFT_15_Clinic_Description, aValue);
                    }
                }
            }
            aOutFinGroup.append(aOutFT1Segment);
            aOutFinGroup.append(aOutZFTSegment);
        }
        return aOutFinGroup;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for ZCD segment for P03 message
     * @throws BHLibClasses.ICANException ICANException
     * @return ZCD segment
     */
    public HL7Segment processP03ZCDFromUFD(HL7Segment pPV1Segment) throws ICANException {
        HL7Segment aOutZCDSegment = new HL7Segment("ZCD");
        HL7Segment aInPV1Segment = new HL7Segment(aInHL7Message.getSegment(HL7_23.PV1));
        String aClaimNum = getFromZBX("FINANCE", "CLAIM_NUMBER");
        String aEmployerName = getFromZBX("FINANCE", "EMPLOYER_NAME");
        String aFinClass = aInPV1Segment.get(HL7_23.PV1_20_financial_class);
        aAssignBillNo = getFromZBX("FINANCE", "ASSIGN_BILL_NUMBER");

        //Added for CERNER inbound billing message.  ZCD segment may not come through therefore no
        //Assign bill number ZBX segment will be generated
        if (aFinClass.equalsIgnoreCase("TAC")) {
            CodeLookUp aLUDefaultGT = new CodeLookUp("DefaultGuarantors.table", mEnvironment);
            String aTACValue = aLUDefaultGT.getValue("TAC8");
            aAssignBillNo = aTACValue;
        } else if (aFinClass.equalsIgnoreCase("W/C") &&
                aEmployerName.length() > 0) {
            aAssignBillNo = "E";
        } else {
            CodeLookUp aLUCompensableClass = new CodeLookUp("ValidateBillingCompensableClass.table", mEnvironment);
            aFinClass = aLUCompensableClass.getValue(aFinClass);
            if (aFinClass.length() == 0) {
                aAssignBillNo = "P";
            } else {
                aAssignBillNo = "A";
            }
        }
        //

//        if (aAssignBillNo.length() == 0) {
//            aAssignBillNo = "P";
//        }

        aOutZCDSegment.set(CSC_23.ZCD_4_Informed_Employer, "N");
        aOutZCDSegment.set(CSC_23.ZCD_11_Claim_Number, aClaimNum);
        aOutZCDSegment.set(CSC_23.ZCD_12_Assign_Bill_to_Code, aAssignBillNo);
        aOutZCDSegment.set(CSC_23.ZCD_14_Payment_Class, aFinClass);

        //process for Eastern
        if (mFacility.matches("BHH")) {
            aOutZCDSegment.set(CSC_23.ZCD_4_Informed_Employer, "");
            String aCode = pPV1Segment.get(HL7_23.PV1_20_financial_class);
            CodeLookUp aCLU = new CodeLookUp("ValidateBillingCompensableClass.table", mEnvironment);
            String aValue = aCLU.getValue(aCode);
            if (aValue.length() > 0) {
                aOutZCDSegment.set(CSC_23.ZCD_12_Assign_Bill_to_Code, "A");
            } else {
                aOutZCDSegment.set(CSC_23.ZCD_12_Assign_Bill_to_Code, "");
            }
        }

        return aOutZCDSegment;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for ACC group for P03 message
     * @return ACC group
     */
    public HL7Group processP03ACCs_FromUFD() {
        HL7Group aOutACCGroup = new HL7Group();
        HL7Segment aInACCSegment = new HL7Segment(k.NULL);
        HL7Segment aInPV1Segment = new HL7Segment(aInHL7Message.getSegment(HL7_23.PV1));
        HL7Segment aOutACCSegment = new HL7Segment(k.NULL);
        int aACCSegmentCount = aInHL7Message.countSegments(HL7_23.ACC);
        for (int i = 1; i <= aACCSegmentCount; i++) {
            aInACCSegment.setSegment(aInHL7Message.getSegment(HL7_23.ACC, i));
            aOutACCSegment = new HL7Segment("ACC");
            String aAccDateTime = aInACCSegment.get(HL7_23.ACC_1_accident_date_time);
            if (aAccDateTime.length() > 0) {
                aOutACCSegment.set(HL7_23.ACC_1_accident_date_time, aAccDateTime);
            } else {
                aOutACCSegment.set(HL7_23.ACC_1_accident_date_time, aInPV1Segment.get(HL7_23.PV1_44_admit_date_time));
            }
            aOutACCSegment.set(HL7_23.ACC_2_accident_code, aInACCSegment.get(HL7_23.ACC_2_accident_code));
            aOutACCSegment.set(HL7_23.ACC_3_accident_location, aInACCSegment.get(HL7_23.ACC_3_accident_location));
            aOutACCGroup.append(aOutACCSegment);
        }
        return aOutACCGroup;
    }
    //--------------------------------------------------------------------------

    /**
     * CSC specific processing for GT1 group for P03 message
     * @param pGT1Group GT1 group
     * @throws BHLibClasses.ICANException ICANException
     * @return GT1 group
     */
    public HL7Group processP03GT1s_FromUFD(HL7Group pGT1Group, HL7Segment pPV1Segment) throws ICANException {
        HL7Segment aInPIDSegment = new HL7Segment(aInHL7Message.getSegment(HL7_23.PID));
        HL7Group aOutGT1Group = new HL7Group();
        //String aGuarantorType = getFromZBX("FINANCE", "ASSIGN_BILL_NUMBER");
        String aGuarantorType = aAssignBillNo;
        int aGT1GroupCount = pGT1Group.countSegments();
        HL7Segment aOutGT1Segment = new HL7Segment(k.NULL);
        for (int i = 1; i <= aGT1GroupCount; i++) {
            aOutGT1Segment.setSegment(pGT1Group.getSegment(i));
            aOutGT1Segment.set(HL7_23.GT1_10_guarantor_type, aGuarantorType);
            String aGuarantorLastName = aOutGT1Segment.get(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name);
            String aGuarantorFirstName = aOutGT1Segment.get(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_given_name);
            String aGuarantorMidName = aOutGT1Segment.get(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_middle_name);
            String aGuarantorSuffix = aOutGT1Segment.get(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_suffix);
            String aGuarantorPrefix = aOutGT1Segment.get(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_prefix);
            String aGTName = aGuarantorLastName;
            if (aGuarantorFirstName.length() > 0) {
                aGTName = aGTName.concat(",").concat(aGuarantorFirstName).concat(aGuarantorMidName);
            }
            if (aGuarantorMidName.length() > 0) {
                aGTName = aGTName.concat(" ").concat(aGuarantorMidName);
            }
            if (aGuarantorSuffix.length() > 0) {
                aGTName = aGTName.concat(" ").concat(aGuarantorSuffix);
            }
            if (aGuarantorPrefix.length() > 0) {
                aGTName = aGuarantorPrefix.concat(" ").concat(aGTName);
            }
            if (aGTName.length() > 28) {
                aGTName = aGTName.substring(0, 28);
            }
            aOutGT1Segment.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name, aGTName.toUpperCase());
            String aAddress = aOutGT1Segment.get(HL7_23.GT1_5_guarantor_address);
            aOutGT1Segment.set(HL7_23.GT1_5_guarantor_address, aAddress.toUpperCase());

            //Process for Eastern
            if (mFacility.matches("BHH")) {
                String aCode = pPV1Segment.get(HL7_23.PV1_20_financial_class);
                CodeLookUp aCLU = new CodeLookUp("ValidateBillingCompensableClass.table", mEnvironment);
                String aValue = aCLU.getValue(aCode);
                if (aValue.length() == 0) {
                    aOutGT1Segment.set(HL7_23.GT1_10_guarantor_type, "FR");
                } else {
                    aOutGT1Segment.set(HL7_23.GT1_10_guarantor_type, "A");
                }
            }

            aOutGT1Group.append(aOutGT1Segment);
        }

        //Generate a GT1 if no GT1 is present
        if (aGT1GroupCount == 0) {
            String aLastName = k.NULL;
            String aStreet1 = k.NULL;
            String aStreet2 = k.NULL;
            String aStateProvince = k.NULL;
            String aCity = k.NULL;
            String aZIP = k.NULL;
            String aPhone = k.NULL;
            String aCountry = k.NULL;
            String aAddress = k.NULL;
            aGuarantorType = k.NULL;

            aProcess = true;
            CodeLookUp aLookup = new CodeLookUp("DefaultGuarantors.table", mEnvironment);
            String aPV1FinClass = pPV1Segment.get(HL7_23.PV1_20_financial_class, HL7_23.FC_finacial_class);

            if (aPV1FinClass.equalsIgnoreCase("DVA")) {
                aLastName = aLookup.getValue("DVA1");
                aStreet1 = aLookup.getValue("DVA2");
                aStreet2 = aLookup.getValue("DVA3");
                aStateProvince = aLookup.getValue("DVA4");
                aCity = aLookup.getValue("DVA5");
                aZIP = aLookup.getValue("DVA6");
                aPhone = aLookup.getValue("DVA7");
                aGuarantorType = aLookup.getValue("DVA8");
            } else if (aPV1FinClass.equalsIgnoreCase("TAC")) {
                aLastName = aLookup.getValue("TAC1");
                aStreet1 = aLookup.getValue("TAC2");
                aStreet2 = aLookup.getValue("TAC3");
                aStateProvince = aLookup.getValue("TAC4");
                aCity = aLookup.getValue("TAC5");
                aZIP = aLookup.getValue("TAC6");
                aPhone = aLookup.getValue("TAC7");
                aGuarantorType = aLookup.getValue("TAC8");
            } else if (aPV1FinClass.equalsIgnoreCase("W/C") &&
                    getFromZBX("FINANCE", "EMPLOYER_NAME").length() > 0) {
                aLastName = getFromZBX("FINANCE", "EMPLOYER_NAME").toUpperCase();
                if (aLastName.length() > 28) {
                    aLastName = aLastName.substring(0, 28);
                }
                aAddress = getFromZBX("FINANCE", "EMPLOYER_ADDRESS").toUpperCase();
                aGuarantorType = "E";
            } else {
                aProcess = false;
            }
            if (aProcess == true) {
                aOutGT1Segment = new HL7Segment("GT1");
                aOutGT1Segment.set(HL7_23.GT1_1_set_ID, "1");
                aOutGT1Segment.set(HL7_23.GT1_2_guarantor_number, "");
                aOutGT1Segment.set(HL7_23.GT1_3_guarantor_name, aLastName);

                if (!aPV1FinClass.equalsIgnoreCase("W/C")) {
                    aOutGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aStreet1);
                    aOutGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aStreet2);
                    aOutGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aCity);
                    aOutGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_county_parish, aStateProvince);
                    aOutGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aZIP);
                    aOutGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_country, aCountry);
                } else {
                    aOutGT1Segment.set(HL7_23.GT1_5_guarantor_address, aAddress);
                }

                aOutGT1Segment.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aPhone);
                aOutGT1Segment.set(HL7_23.GT1_10_guarantor_type, aGuarantorType);
                aOutGT1Segment.set(HL7_23.GT1_11_guarantor_relationship, "");
                aOutGT1Segment.set(HL7_23.GT1_12_guarantor_SSN, "");

                aOutGT1Group.append(aOutGT1Segment.getSegment());
            }
        }
        //

        if (aGT1GroupCount == 0 && aProcess == false) {
            //All else fails, create a GT1 segment based on patient details
            aOutGT1Segment = new HL7Segment(HL7_23.GT1);
            aOutGT1Segment.set(HL7_23.GT1_1_set_ID, "1");
            String aPatientSurname = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name);
            String aPatientFirstName = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name);
            String aPatientMidName = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name);
            String aPatientPrefix = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix);
            String aGTName = aPatientSurname;
            if (aPatientFirstName.length() > 0) {
                aGTName = aGTName.concat(",").concat(aPatientFirstName);
            }
            if (aPatientMidName.length() > 0) {
                aGTName = aGTName.concat(" ").concat(aPatientMidName);
            }
            if (aPatientPrefix.length() > 0) {
                aGTName = aPatientPrefix.concat(" ").concat(aGTName);
            }
            if (aGTName.length() > 28) {
                aGTName = aGTName.substring(0, 28);
            }
            aOutGT1Segment.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name, aGTName.toUpperCase());
            String aPatientAddress = aInPIDSegment.get(HL7_23.PID_11_patient_address);

            HL7Field aInPID_11Field = new HL7Field(aPatientAddress);
            HL7Field aOutPID_11Field = new HL7Field(k.NULL);
            aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_street_1), HL7_23.XAD_street_1);
            aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_street_2), HL7_23.XAD_street_2);
            aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_city), HL7_23.XAD_city);
            aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_state_or_province), HL7_23.XAD_state_or_province);
            aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_zip), HL7_23.XAD_zip);
            aOutPID_11Field.setSubField(aInPID_11Field.getSubField(HL7_23.XAD_country), HL7_23.XAD_country);
            aPatientAddress = aOutPID_11Field.getField();

            aOutGT1Segment.set(HL7_23.GT1_5_guarantor_address, aPatientAddress.toUpperCase());
            String aPatientHomePhone = aInPIDSegment.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
            String aPatientBusPhone = aInPIDSegment.get(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
            aOutGT1Segment.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aPatientHomePhone);
            aOutGT1Segment.set(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telephone_number, aPatientBusPhone);

            String aCodeTemp = pPV1Segment.get(HL7_23.PV1_20_financial_class);
            CodeLookUp aCLUTemp = new CodeLookUp("ValidateBillingCompensableClass.table", mEnvironment);
            String aValueTemp = aCLUTemp.getValue(aCodeTemp);
            if (aValueTemp.length() == 0) {
                aOutGT1Segment.set(HL7_23.GT1_10_guarantor_type, "P");
            } else {
                aOutGT1Segment.set(HL7_23.GT1_10_guarantor_type, "A");
            }
            //aOutGT1Segment.set(HL7_23.GT1_10_guarantor_type, "P");

            //Process for Eastern
            if (mFacility.matches("BHH")) {
                String aCode = pPV1Segment.get(HL7_23.PV1_20_financial_class);
                CodeLookUp aCLU = new CodeLookUp("ValidateBillingCompensableClass.table", mEnvironment);
                String aValue = aCLU.getValue(aCode);
                if (aValue.length() == 0) {
                    aOutGT1Segment.set(HL7_23.GT1_10_guarantor_type, "FR");
                } else {
                    aOutGT1Segment.set(HL7_23.GT1_10_guarantor_type, "A");
                }
            }

            aOutGT1Group.append(aOutGT1Segment);
        }
        return aOutGT1Group;
    }

    public HL7Segment checkMSHTime(HL7Segment pSegment) {
        String aDateTime = pSegment.get(HL7_23.MSH_7_message_date_time);
        if (aDateTime.endsWith("0000")) {
            aDateTime = aDateTime.substring(0, 8) + "0001";
            pSegment.set(HL7_23.MSH_7_message_date_time, aDateTime);
        }
        return pSegment;
    }

    public HL7Segment checkEVNTime(HL7Segment pSegment) {
        String aDateTime = pSegment.get(HL7_23.EVN_2_date_time_of_event);
        if (aDateTime.endsWith("000000")) {
            aDateTime = aDateTime.substring(0, 8) + "000100";
            pSegment.set(HL7_23.EVN_2_date_time_of_event, aDateTime);
        }
        return pSegment;
    }
}
