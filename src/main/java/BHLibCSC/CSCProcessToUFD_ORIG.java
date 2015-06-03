/*
 * CSCProcessToUFD.java
 *
 * Created on 11 October 2005, 15:24
 *
 */

package BHLibCSC;
import BHLibClasses.*;

/**
 * This is the primary engine for processing all HL7 Messages received from CSC.<p>
 *
 * The messages covered are "A01. A02,A03, A04, A08, A11, A12, A13, A17, A28, A31, A34".
 * @author fillinghamr
 */
public class CSCProcessToUFD_ORIG extends ProcessSegmentsToUFD {

    public String cMentalHealthID = "";
    public String mEnvironment = "";
    /**
     * Creates a new instance of CSCProcessToUFD
     * @param pHL7Message The raw message received from CSC.
     */
    public CSCProcessToUFD_ORIG(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "a";        // CSCProcessToUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }

    //--------------------------------------------------------------------------
    /**
     * The processing Method to be called from the Ican JCD.
     * @return The processed message fomatted into the UNIFIED structure.
     */
    public String processMessage() throws ICANException {
        String aSegment;
        HL7Group aGroup;
        HL7Message aInMess = new HL7Message(mHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment(aInMess.getSegment("MSH"));
        HL7Segment aOutMSHSegment = processMSHToUFD();
        if (mEnvironment.indexOf("TEST") >= 0) {
            String aSendingApp = aOutMSHSegment.get(HL7_23.MSH_3_sending_application);
            if (aSendingApp.indexOf(".TEST") >= 0) {
                aOutMSHSegment.set(HL7_23.MSH_3_sending_application, aSendingApp.substring(0, aSendingApp.length() - 5));
            }
        }
        HL7Message aOutMess = new HL7Message(aOutMSHSegment.getSegment());

        if(aInMess.isEvent("A01, A02, A03, A08, A11, A12, A13, A21, A22, A28, A31")) {
            aOutMess.append(processEVNToUFD());
            aOutMess.append(processPIDToUFD());
            aOutMess.append(processNK1s_ToUFD());
            aOutMess.append(processPV1ToUFD());
            aOutMess.append(processPV2ToUFD());
            aOutMess.append(processOBXs_ToUFD());
            aOutMess.append(processAL1s_ToUFD());
            aOutMess.append(processDG1s_ToUFD());
            aOutMess.append(processDRGToUFD());
            aOutMess.append(processPR1s_ToUFD());
            aOutMess.append(processGT1s_ToUFD());
            aOutMess.append(processInsuranceToUFD());
            aOutMess.append(processCSC_ZsegmentsToUFD());

        } else if (aInMess.isEvent("A17")) {
            aOutMess.append(processEVNToUFD().getSegment());
            aOutMess.append(processA17GroupToUFD(1));
            aOutMess.append(processA17GroupToUFD(2));

        } else if (aInMess.isEvent("A34")) {
            aOutMess.append(processEVNToUFD().getSegment());
            aOutMess.append(processPIDToUFD().getSegment());
            aOutMess.append(processMRGToUFD().getSegment());
        }
        if (aOutMess.getMessage().length() > 0) {
            aOutMess.append(setupZBX("MESSAGE", "SOURCE_ID", aInMSHSegment.get(HL7_23.MSH_10_message_control_ID)));
        }
        return aOutMess.getMessage();
    }

    //--------------------------------------------------------------------------
    /**
     * Process a CSC GT1 segment according to UFD requirements
     * @return Returns a group GT1 segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Group processGT1s_ToUFD() throws ICANException {

        HL7Message aHL7Message = new HL7Message(mHL7Message);
        HL7Group aGT1GroupOUT = new HL7Group();
        HL7Segment aGT1SegIN = new HL7Segment(k.NULL);
        HL7Segment aGT1SegOUT = new HL7Segment(k.NULL);
        HL7Segment aPV1Segment = new HL7Segment(k.NULL);

        HL7Group aGT1GroupIN = new HL7Group();
        HL7Group aZCDGroupIN = new HL7Group();
        HL7Segment aZCDSeg = new HL7Segment("ZCD");

        aGT1GroupIN = processGroup(HL7_23.Repeat_GT1);
        aZCDGroupIN = processGroup(CSC_23.Repeat_ZCD);

        String aPV1FinClass = k.NULL;
        CodeLookUp aLookup = new CodeLookUp("DefaultGuarantors.table", mEnvironment);

        int aCountGT1 = 0;
        int aCountGT1Process = 1;

        aPV1Segment.setSegment(aHL7Message.getSegment(HL7_23.PV1));
        aPV1FinClass = aPV1Segment.get(HL7_23.PV1_20_financial_class, HL7_23.FC_finacial_class);

        if (aPV1FinClass.equalsIgnoreCase("DVA")) {
            aGT1SegOUT = new HL7Segment("GT1");
            aGT1SegOUT.setField(Integer.toString(aCountGT1Process), HL7_23.GT1_1_set_ID);
            aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, aLookup.getValue("DVA1"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aLookup.getValue("DVA2"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aLookup.getValue("DVA3"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, aLookup.getValue("DVA4"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aLookup.getValue("DVA5"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aLookup.getValue("DVA6"));
            aGT1SegOUT.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aLookup.getValue("DVA7"));
            aGT1SegOUT.set(HL7_23.GT1_10_guarantor_type, aLookup.getValue("DVA8"));
            aGT1GroupOUT.append(aGT1SegOUT.getSegment());

        } else if (aPV1FinClass.equalsIgnoreCase("TAC")) {
            aGT1SegOUT = new HL7Segment("GT1");
            aGT1SegOUT.setField(Integer.toString(aCountGT1Process), HL7_23.GT1_1_set_ID);
            aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, aLookup.getValue("TAC1"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aLookup.getValue("TAC2"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aLookup.getValue("TAC3"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, aLookup.getValue("TAC4"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aLookup.getValue("TAC5"));
            aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aLookup.getValue("TAC6"));
            aGT1SegOUT.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aLookup.getValue("TAC7"));
            aGT1SegOUT.set(HL7_23.GT1_10_guarantor_type, aLookup.getValue("TAC8"));
            aGT1GroupOUT.append(aGT1SegOUT.getSegment());

        } else {
            aCountGT1 = aHL7Message.countSegments(HL7_23.GT1);
            if (aCountGT1 > 0) {
                for(int i = 1; i <= aCountGT1; i++) {
                    aGT1SegIN.setSegment(aHL7Message.getSegment(HL7_23.GT1, i));
                    String aRecordNumber = "";
                    int j;
                    int k;
                    for (j=1; j <= aZCDGroupIN.countSegments(); j++) {

                        aZCDSeg =  new HL7Segment(aZCDGroupIN.getSegment(j));
                        aRecordNumber = aZCDSeg.get(CSC_23.ZCD_1_Record_Number);
                        for (k=1; k <= aGT1GroupIN.countSegments(); k++) {
                            aGT1SegIN = new HL7Segment(aGT1GroupIN.getSegment(k));
                            if (aRecordNumber.equalsIgnoreCase(aGT1SegIN.get(HL7_23.GT1_2_guarantor_number))) {
                                aGT1SegOUT = new HL7Segment("GT1");
                                aGT1SegOUT.setField(Integer.toString(aCountGT1Process), HL7_23.GT1_1_set_ID);
                                aGT1SegOUT.linkTo(aGT1SegIN);
                                aGT1SegOUT.copy(HL7_23.GT1_1_set_ID);
                                aGT1SegOUT.copy(HL7_23.GT1_2_guarantor_number, HL7_23.CX_ID_number);
                                aGT1SegOUT.copy(HL7_23.GT1_2_guarantor_number, HL7_23.CX_check_digit);
                                aGT1SegOUT.copy(HL7_23.GT1_2_guarantor_number, HL7_23.CX_check_digit_scheme);
                                aGT1SegOUT.copy(HL7_23.GT1_2_guarantor_number, HL7_23.CX_assigning_authority);
                                aGT1SegOUT.copy(HL7_23.GT1_3_guarantor_name);
                                aGT1SegOUT.copy(HL7_23.GT1_5_guarantor_address);
                                aGT1SegOUT.copy(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number);
                                aGT1SegOUT.copy(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telecom_use);
                                aGT1SegOUT.copy(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telephone_number);
                                aGT1SegOUT.copy(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telecom_use);
                                aGT1SegOUT.copy(HL7_23.GT1_10_guarantor_type);
                                aGT1GroupOUT.append(aGT1SegOUT);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return aGT1GroupOUT;
    }

//--------------------------------------------------------------------------
    /**
     * Returns the NK1 segment/s from the message.<p>
     * For CSC the "Next of Kin Relationship" is free text and has to be validated against the "NOK_Relationship" table.<p>
     * No further processing of the segment is performed.
     * @return NK1 segment/s
     */
    public HL7Group processNK1s_ToUFD() throws ICANException {

        HL7Segment aNK1SegIN = new HL7Segment("NK1");
        HL7Segment aNK1SegOUT = new HL7Segment("NK1");
        HL7Group aNK1GroupIN = new HL7Group();
        HL7Group aNK1GroupOUT = new HL7Group();
        CodeLookUp aRelationship = new CodeLookUp("NOK_Relationship.table", mEnvironment);

        aNK1GroupIN = processGroup(HL7_23.Repeat_NK1);
        int i;
        for (i=1; i <= aNK1GroupIN.countSegments(); i++) {

            aNK1SegIN = new HL7Segment(aNK1GroupIN.getSegment(i));
            aNK1SegOUT = new HL7Segment("NK1");
            aNK1SegOUT.linkTo(aNK1SegIN);
            aNK1SegOUT.copy(HL7_23.NK1_1_set_ID);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name);
            if (mFacility.equalsIgnoreCase("BHH") ||
                    mFacility.equalsIgnoreCase("MAR") ||
                    mFacility.equalsIgnoreCase("ANG") ||
                    mFacility.equalsIgnoreCase("PJC")) {
                //Perform no translation just copy NOK
                aNK1SegOUT.copy(HL7_23.NK1_3_next_of_kin_relationship);
            } else {
                aNK1SegOUT.set(HL7_23.NK1_3_next_of_kin_relationship,
                        aRelationship.getValue(aNK1SegIN.getField(HL7_23.NK1_3_next_of_kin_relationship)));
            }
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_1);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_2);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_city);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_state_or_province);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_zip);
//            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_type);
            aNK1SegOUT.copy(HL7_23.NK1_5_next_of_kin__phone, HL7_23.XTN_telephone_number);
            aNK1SegOUT.copy(HL7_23.NK1_5_next_of_kin__phone, HL7_23.XTN_telecom_use);
            aNK1SegOUT.copy(HL7_23.NK1_6_business_phone_num, HL7_23.XTN_telephone_number);
            aNK1SegOUT.copy(HL7_23.NK1_6_business_phone_num, HL7_23.XTN_telecom_use);
            aNK1SegOUT.copy(HL7_23.NK1_7_contact_role);

            aNK1GroupOUT.append(aNK1SegOUT);
        }

        return aNK1GroupOUT;
    }

//--------------------------------------------------------------------------
    /**
     * CSC specific processing for an Incoming PID Segment contained in the HL7 Message.<p>
     * @return Returns the processed HL7 PID as an HL7Segment.
     */
    public HL7Segment processPIDToUFD() throws ICANException {
        return (this.processPIDToUFD(mHL7Message));
    }
    /**
     *CSC specific processing for an Incoming PID Segment contained in [pHL7MessageBlock].<p>
     * @return The processed HL7 PID segment as an HL7Segment.
     * @param pHL7MessageBlock A block of segments containing a PID segment ... e.g. aGroup.
     */
    public HL7Segment processPIDToUFD(String pHL7MessageBlock) throws ICANException {

        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aZPDSegment = new HL7Segment(aHL7Message.getSegment("ZPD"));
        HL7Segment aInPID = new HL7Segment(k.NULL);
        HL7Segment aOutPID = new HL7Segment(HL7_23.PID);

        HL7Field aTempField;
        String aPID_3_RepeatField[] = new String[5];
        String aPID_11_RepeatField[] = new String[5];
        String aPID_13_RepeatField[] = new String[5];
        int aRepeat = 0;
        aInPID.setSegment(aHL7Message.getSegment(HL7_23.PID));

// Copy all fields indicated in aArray from IN segment ...
        aOutPID.linkTo(aInPID);

        String aArray[] = { HL7_23.PID_1_set_ID,
                HL7_23.PID_5_patient_name,
                HL7_23.PID_7_date_of_birth,
                HL7_23.PID_8_sex,
                HL7_23.PID_9_patient_alias,
                HL7_23.PID_10_race,
                HL7_23.PID_12_county_code,
                HL7_23.PID_13_home_phone,
                HL7_23.PID_14_business_phone,
                HL7_23.PID_15_language,
                HL7_23.PID_16_marital_status,
                HL7_23.PID_17_religion,
                HL7_23.PID_18_account_number,
                HL7_23.PID_19_SSN_number,
                HL7_23.PID_21_mothers_ID,
                HL7_23.PID_23_birth_place,
                HL7_23.PID_29_patient_death_date_time,
                HL7_23.PID_30_patient_death_indicator
        };
        aOutPID.copyFields(aArray);

// Check patient death date is not less than patient birth date

        String aPatientDeathDateTime = aOutPID.get(HL7_23.PID_29_patient_death_date_time);
        if (aPatientDeathDateTime.length() > 2) {
            int aBirthDate = Integer.parseInt(aOutPID.get(HL7_23.PID_7_date_of_birth).substring(0, 8));
            int aDeathDate = Integer.parseInt(aPatientDeathDateTime.substring(0, 8));
            if (aDeathDate < aBirthDate) {
                throw new ICANException("F010", mEnvironment);
            }
        }

// ... make certain it gets a Set ID.
        if ( aInPID.isEmpty(HL7_23.PID_1_set_ID))
            aOutPID.setField("1", HL7_23.PID_1_set_ID);

// Patient UR Number
        if (! aInPID.isEmpty(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number)) {
            aTempField = new HL7Field(aInPID.get(HL7_23.PID_3_patient_ID_internal));
            aTempField.setSubField("PI",HL7_23.CX_ID_type_code );
            aPID_3_RepeatField[aRepeat++] = aTempField.getField();
            mPatientUR = aInPID.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number);
        }
// Patient Sex Indeterminate = Unknown
        if (aOutPID.get(HL7_23.PID_8_sex).equalsIgnoreCase("I")) {
            aOutPID.set(HL7_23.PID_8_sex, "U");
        }

// Correct the Rabbi prefix
        if (aOutPID.get(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix).equalsIgnoreCase("RAB")) {
            aOutPID.set(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix, "Rabbi");
        }
        if (aOutPID.get(HL7_23.PID_9_patient_alias, HL7_23.XPN_prefix).equalsIgnoreCase("RAB")) {
            aOutPID.set(HL7_23.PID_9_patient_alias, HL7_23.XPN_prefix, "Rabbi");
        }

// Pension Number and Mental Health ID
        if (! aInPID.isEmpty(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number)) {
            String aField[] = aInPID.getRepeatFields(HL7_23.PID_4_alternate_patient_ID);
            int n;

            for (n = 0; n < aField.length; n++) {
                aTempField = new HL7Field(aField[n]);
                if (aTempField.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("PE")) {
                    aTempField.setSubField("PEN",HL7_23.CX_ID_type_code );
                    aTempField.setSubField("PEN",HL7_23.CX_assigning_authority  );
                    aPID_3_RepeatField[aRepeat++] = aTempField.getField();

                } else
                    if (aTempField.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("MH")) {
                    aTempField.setSubField("MH",HL7_23.CX_ID_type_code );
                    aTempField.setSubField("MHN",HL7_23.CX_assigning_authority  );
                    aPID_3_RepeatField[aRepeat++] = aTempField.getField();
                    cMentalHealthID = aTempField.getSubField(HL7_23.CX_ID_number);
                    }

            }
        }

// Medicare Number
        if (! aInPID.isEmpty(HL7_23.PID_19_SSN_number, HL7_23.CX_ID_number)) {
            aTempField = new HL7Field(aInPID.get(HL7_23.PID_19_SSN_number));
        } else {
            aTempField = new HL7Field("C-U");
        }
        aTempField.setSubField("MC",HL7_23.CX_ID_type_code );
        aTempField.setSubField("HIC",HL7_23.CX_assigning_authority  );
        aPID_3_RepeatField[aRepeat++] = aTempField.getField();

// DVA Number
        if (!aInPID.isEmpty(HL7_23.PID_27_veterans_military_status, HL7_23.CX_ID_number) &&
                !aInPID.get(HL7_23.PID_27_veterans_military_status).equalsIgnoreCase("\"\"")) {
            aTempField = new HL7Field(aInPID.get(HL7_23.PID_27_veterans_military_status, HL7_23.CX_ID_number));
            aTempField.setSubField("VA",HL7_23.CX_ID_type_code );
            aTempField.setSubField("DVA",HL7_23.CX_assigning_authority  );
            aPID_3_RepeatField[aRepeat++] = aTempField.getField();
        }

        aOutPID.setRepeatFields(HL7_23.PID_3_patient_ID_internal, aPID_3_RepeatField);

// Patients Address
        HL7Field aPID11Field = new HL7Field();
        HL7Field aPIDTmpField = new HL7Field();
        String aPID11Array[] = aInPID.getRepeatFields(HL7_23.PID_11_patient_address);
//        String aStateDesc = "";
        aRepeat = 0;
        int i;
        for (i=0; i < aPID11Array.length; i++) {
            aPIDTmpField = new HL7Field(aPID11Array[i]);
            aPID11Field.setSubField(aPIDTmpField.getSubField(HL7_23.XAD_street_1),HL7_23.XAD_street_1);
            aPID11Field.setSubField(aPIDTmpField.getSubField(HL7_23.XAD_street_2),HL7_23.XAD_street_2);
            aPID11Field.setSubField(aPIDTmpField.getSubField(HL7_23.XAD_city),HL7_23.XAD_city);

            //Norman Soh: Process State description based on PostCode
//            String aPostCodeStr = aPIDTmpField.getSubField(HL7_23.XAD_zip);
//            if (aPostCodeStr.length() > 0) {
//                try {
//                    int aPostCode = Integer.parseInt(aPostCodeStr);
//                    if (aPostCode >= 800 && aPostCode <= 899) {
//                        aStateDesc = "NT";
//                    } else if ((aPostCode >= 200 && aPostCode <= 299) ||
//                            (aPostCode >= 2600 && aPostCode <= 2619) ||
//                            (aPostCode >= 2900 && aPostCode <= 2920)) {
//                        aStateDesc = "ACT";
//                    } else if ((aPostCode >= 1000 && aPostCode <= 2599) ||
//                            (aPostCode >= 2620 && aPostCode <= 2899) ||
//                            (aPostCode >= 2921 && aPostCode <= 2999)) {
//                        aStateDesc = "NSW";
//                    } else if ((aPostCode >= 3000 && aPostCode <= 3999) ||
//                            (aPostCode >= 8000 && aPostCode <= 8999)) {
//                        aStateDesc = "VIC";
//                    } else if (aPostCode == 8888) {
//                        aStateDesc = "OVERSEAS";
//                    } else if (aPostCode == 9990 || aPostCode == 9988) {
//                        aStateDesc = "";
//                    } else if ((aPostCode >= 4000 && aPostCode <= 4999) ||
//                            (aPostCode >= 9000 && aPostCode <= 9799)) {
//                        aStateDesc = "QLD";
//                    } else if (aPostCode >= 5000 && aPostCode <= 5999) {
//                        aStateDesc = "SA";
//                    } else if (aPostCode >= 6000 && aPostCode <= 6999) {
//                        aStateDesc = "WA";
//                    } else if (aPostCode >= 7000 && aPostCode <= 7999) {
//                        aStateDesc = "TAS";
//                    }
//                    aPID11Field.setSubField(aStateDesc, HL7_23.XAD_state_or_province);
//                } catch (Exception e) {
//                    //Do nothing, postcode is not entered or is invalid
//                }
//            }
            //

            if (aPIDTmpField.getSubField(HL7_23.XAD_zip).equalsIgnoreCase("8888")){
                aPID11Field.setSubField("OVERSEAS", HL7_23.XAD_state_or_province);
            } else {
                aPID11Field.setSubField(aPIDTmpField.getSubField(HL7_23.XAD_state_or_province), HL7_23.XAD_state_or_province);
            }

            aPID11Field.setSubField(aPIDTmpField.getSubField(HL7_23.XAD_zip),HL7_23.XAD_zip);
            aPID11Field.setSubField(aPIDTmpField.getSubField(HL7_23.XAD_country),HL7_23.XAD_country);
            aPID11Field.setSubField(aPIDTmpField.getSubField(HL7_23.XAD_type),HL7_23.XAD_type);
            aPID11Field.setSubField(aPIDTmpField.getSubField(HL7_23.XAD_geographic_designation),HL7_23.XAD_geographic_designation);
            aPID11Field.setSubField(aPIDTmpField.getSubField(HL7_23.XAD_county_parish),HL7_23.XAD_county_parish);

            aPID_11_RepeatField[aRepeat++] = aPID11Field.getField();
            if (aRepeat == 4)    {
                break;
            }
        }
        aOutPID.setRepeatFields(HL7_23.PID_11_patient_address, aPID_11_RepeatField);

//Get first home phone number only
        if (! aZPDSegment.isEmpty(CSC_23.ZPD_12_Personal_Contact_Data_Phone_Numbers)) {
            String aZPD12Array[] = aZPDSegment.getRepeatFields(CSC_23.ZPD_12_Personal_Contact_Data_Phone_Numbers);
            HL7Field aPID13Field = new HL7Field();
            String aPID13Array[] = aZPDSegment.getRepeatFields(CSC_23.ZPD_12_Personal_Contact_Data_Phone_Numbers);
            HL7Field aPID14Field = new HL7Field();

            HL7Field aZPDField;

            HL7Field aTmpField = new HL7Field();
            aRepeat = 0;

            for (i=0; i < aZPD12Array.length; i++) {
                aZPDField = new HL7Field(aZPD12Array[i]);
                if (aZPDField.getSubField(HL7_23.XTN_telecom_use).equalsIgnoreCase("PRN")) {
                    aPID13Field.setSubField(aZPDField.getSubField(HL7_23.XTN_telephone_number), HL7_23.XTN_telephone_number);
                    if (aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("H") ||
                            aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("R") ||
                            aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("P") ||
                            aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("RES") ||
                            aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("POB") ||
                            aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("PR1") ||
                            aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("PR2")) {
                        aPID13Field.setSubField("PRN", HL7_23.XTN_telecom_use);         // Home Phone
                    } else {
                        aPID13Field.setSubField("ORN", HL7_23.XTN_telecom_use);         // Overseas Phone Number
                    }

                    aPID_13_RepeatField[aRepeat++] = aPID13Field.getField();
                    if (aRepeat == 4)    {
                        break;
                    }
                }

                if (aZPDField.getSubField(HL7_23.XTN_telecom_use).equalsIgnoreCase("WPN")) {
                    //aPID14Field.setSubField(aZPDField.getSubField(HL7_23.XTN_telephone_number), HL7_23.XTN_telephone_number);
                    if (aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("H") ||
                            aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("R") ||
                            aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("P") ||
                            aZPDField.getSubField(HL7_23.XTN_comment).equalsIgnoreCase("RES")) {
                        aPID14Field.setSubField(aZPDField.getSubField(HL7_23.XTN_telephone_number), HL7_23.XTN_telephone_number);
                        aPID14Field.setSubField("WPN", HL7_23.XTN_telecom_use);         // Business Phone
                    }
                }
            }
            if (aRepeat > 0) {
                aOutPID.setRepeatFields(HL7_23.PID_13_home_phone, aPID_13_RepeatField);
            }
            aOutPID.setField(aPID14Field.getField(), HL7_23.PID_14_business_phone);

        }
        return aOutPID;
    }

//--------------------------------------------------------------------------
    /**
     * Returns the PV1 segment from the message.  No further processing of the segment
     * is performed.
     * @return PV1 segment
     */
    public HL7Segment processPV1ToUFD() throws ICANException {
        return (this.processPV1ToUFD(mHL7Message));
    }
    /**
     * Returns the PV1 segment from the message.  No further processing of the segment
     * is performed.
     * @param pHL7MessageBlock A group of segments containing the PV1. This may be a full HL7 message or an HL7Group.
     * @return The PV2 segment contained in the message block or NULL if PV1 does not exist in the block.
     */
    public HL7Segment processPV1ToUFD(String pHL7MessageBlock) throws ICANException {

        HL7Message aHL7Message = new HL7Message(mHL7Message);
        HL7Segment aPV1Segment = new HL7Segment("PV1");

        CodeLookUp aPayClass = new CodeLookUp("CSC_PayClass.table", mFacility, mEnvironment);


// Special processing for PV1 segments received in A28 and A31 messages.
        if (aHL7Message.isEvent("A28, A31")) {
            aPV1Segment.set(HL7_23.PV1_1_set_ID, "1");
            aPV1Segment.set(HL7_23.PV1_2_patient_class, "R");
            aPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, mFacility);
            aPV1Segment.set(HL7_23.PV1_18_patient_type, "R");
            aPV1Segment.set(HL7_23.PV1_19_visit_number, "R".concat(mPatientUR));
            if (mFacility.matches("BHH|MAR|PJC|ANG")) {
                HL7Segment aInPV1Segment = new HL7Segment(aHL7Message.getSegment(HL7_23.PV1));
                String aPV1_8ReferringDoc = aInPV1Segment.get(HL7_23.PV1_8_referring_doctor);
                aPV1Segment.set(HL7_23.PV1_8_referring_doctor, aPV1_8ReferringDoc);
            }
        } else {                      // For all other message types ... i.e. "A01 to A17"
            aHL7Message = new HL7Message(pHL7MessageBlock);
            aPV1Segment.setSegment(aHL7Message.getSegment(HL7_23.PV1));
            aPV1Segment.set(HL7_23.PV1_2_patient_class, "I");

// Sandringham funny Facility in Room position ....
            if (aPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room).equalsIgnoreCase("23")) {
                aPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "SDMH");
            }
            if (aPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_room).equalsIgnoreCase("23")) {
                aPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_room, "SDMH");
            }
            aPV1Segment.set(HL7_23.PV1_19_visit_number, "I".concat(aPV1Segment.get(HL7_23.PV1_19_visit_number)));

// Translate the Financial Class
            if (mFacility.equalsIgnoreCase("BHH") ||
                    mFacility.equalsIgnoreCase("MAR") ||
                    mFacility.equalsIgnoreCase("ANG") ||
                    mFacility.equalsIgnoreCase("PJC")) {

                //aPV1Segment.set(HL7_23.PV1_20_financial_class, aPayClass.getValue(mFacility + "-" + aPV1Segment.get(HL7_23.PV1_2_patient_class) + "-" + aPV1Segment.get(HL7_23.PV1_20_financial_class)));
            } else {
                aPV1Segment.set(HL7_23.PV1_20_financial_class, aPayClass.getValue(aPV1Segment.get(HL7_23.PV1_20_financial_class)));
            }


// Check each of the Dr's have a valid Bayside code
            String aDr;
// ... Attending Dr ....
            aDr = doDrTranslate(aPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num));
            if (aDr.equalsIgnoreCase(k.NULL)) {
                aPV1Segment.set(HL7_23.PV1_7_attending_doctor, "");
            } else {
                aPV1Segment.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, aDr);
            }

// ... Refering Dr ....
            aDr = doDrTranslate(aPV1Segment.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num));
            if (aDr.equalsIgnoreCase(k.NULL)) {
                aPV1Segment.set(HL7_23.PV1_8_referring_doctor, "");
            } else {
                aPV1Segment.set(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num, aDr);
            }

// ... Consulting Dr ....
            aDr = doDrTranslate(aPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num));
            if (aDr.equalsIgnoreCase(k.NULL)) {
                aPV1Segment.set(HL7_23.PV1_9_consulting_doctor, "");
            } else {
                aPV1Segment.set(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, aDr);
            }

// Check for CSC sending invalid discharge/admit times of
            aPV1Segment.set(HL7_23.PV1_44_admit_date_time, doValidTimeCheck(aPV1Segment.get(HL7_23.PV1_44_admit_date_time)));
            aPV1Segment.set(HL7_23.PV1_45_discharge_date_time, doValidTimeCheck(aPV1Segment.get(HL7_23.PV1_45_discharge_date_time)));
        }
        return aPV1Segment;

    }

// Method checks we do not have a 24:00 hrs time since some systems (STOCCA)consider that an invalid time (i.e midnight is 00:00hrs).
    private String doValidTimeCheck(String pDateTime) {
        String aDateTime = pDateTime;

        if (pDateTime.length() > 0) {
            String aTime = pDateTime.substring(8);
            if (aTime.matches("240000")) {
                aDateTime = pDateTime.substring(0,8) + "235900";
            }
        }
        return aDateTime;
    }
// Method to check that a Dr code is valid Bayside format "aannn" or is a valid numeric SDMH code or a CGMC "NULL" doctor
    private String doDrTranslate(String pDrCode) {

        byte[] aCode = pDrCode.getBytes();
        String aDrCode = pDrCode;

        if (aCode.length == 5 && !mFacility.matches("BHH|PJC|MAR|ANG")) {
            int i;
            for (i = 1; i < aCode.length; i++) {
                if ((i == 1 || i == 2) && (aCode[i] < 'A' || aCode[i] > 'Z')) {   // Check for "aa" part of format
                    aDrCode = k.NULL ;
                } else if ((i == 3 || i == 4 || i==5) && (aCode[i] < '0' || aCode[i] > '9')) {   // Check for "nnn" part of format
                    aDrCode = k.NULL ;
                }
            }
            return pDrCode;

        } else {
            if (mFacility.equalsIgnoreCase("SDMH")) {
                CodeLookUp aSDMHDrTranslate = new CodeLookUp("Dr_Translation.table", "SDMH", mEnvironment);
                aDrCode = aSDMHDrTranslate.getValue(pDrCode);

                if (aDrCode.length() == 5 ) {
                    aDrCode = k.NULL;
                }
            } else if (mFacility.equalsIgnoreCase("CGMC")) {
                if (! pDrCode.equalsIgnoreCase("UNK")) {
                    aDrCode = k.NULL ;
                }
            } else if (mFacility.equalsIgnoreCase("ALF")) {
                aDrCode = k.NULL ;
            } else {
                //covers eastern health systems
                aDrCode = pDrCode;
            }
        }
        return aDrCode;

    }
    /**
     * Returns the PV2 segment from the message.  No further processing of the segment
     * is performed.
     * @param pHL7MessageBlock A group of segments containing the PV2. This may be a full HL7 message or an HL7Group.
     * @return The PV2 segment contained in the message block or NULL if PV2 does not exist in the block.
     */

    public HL7Segment  processPV2ToUFD(String pHL7MessageBlock) throws ICANException {
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aPV2Segment = new HL7Segment(k.NULL);

        aPV2Segment.setSegment(aHL7Message.getSegment(HL7_23.PV2));
        if (aPV2Segment.getSegment().length() > 0) {
            aPV2Segment.linkTo(aPV2Segment);
            aPV2Segment.move(HL7_23.PV2_3_admit_reason, HL7_23.CE_text, HL7_23.PV2_3_admit_reason, HL7_23.CE_ID_code);
            aPV2Segment.set(HL7_23.PV2_3_admit_reason, HL7_23.CE_ID_code, "");
        }
        return aPV2Segment;
    }

//--------------------------------------------------------------------------
    /**
     * Extracts all of the information required from the CSC Z segments(i.e Vendor specific segments) and moves it into the relevantv UNIFIED ZBX segement's.
     * @return Returns an HL7Group containing all of the ZBX segments.
     */
    public HL7Group processCSC_ZsegmentsToUFD() throws ICANException {

        HL7Message aInMessage = new HL7Message(mHL7Message) ;
        HL7Group aOutGroup = new HL7Group();
        HL7Segment aPID = new HL7Segment(aInMessage.getSegment("PID"));
        HL7Segment aPV1 = new HL7Segment(aInMessage.getSegment("PV1"));
        HL7Segment aIN2 = new HL7Segment(aInMessage.getSegment("IN2"));
        HL7Segment aZV1 = new HL7Segment(aInMessage.getSegment("ZV1"));
        HL7Segment aZPD = new HL7Segment(aInMessage.getSegment("ZPD"));
        HL7Segment aZMR = new HL7Segment(aInMessage.getSegment("ZMR"));
        HL7Segment aZCD = new HL7Segment(aInMessage.getSegment("ZCD"));

// Patient (i.e PMI)related info
        aOutGroup.append(setupZBX("PMI", "TRAUMA", aZV1.getField(CSC_23.ZV1_24_Diagnosis_Description)));
        aOutGroup.append(setupZBX("PMI", "OCCUPATION", aIN2.get(HL7_23.IN2_46_job_title)));
        aOutGroup.append(setupZBX("PMI", "FINANCIAL_CLASS_REG", aZV1.getField(CSC_23.ZV1_32_Financial_Class)));

        if (! aPID.isEmpty(HL7_23.PID_27_veterans_military_status,  HL7_23.CE_text)) {
            aOutGroup.append(setupZBX("PMI", "DVA_CARD_TYPE", aPID.get(HL7_23.PID_27_veterans_military_status,  HL7_23.CE_text)));
        }

// ED Connect
        String aGeneric = aZPD.getField(CSC_23.ZPD_4_Generic_Text);
        aGeneric = aGeneric.toUpperCase();

        if (aGeneric.indexOf("EDCON") >= 0) {
            aOutGroup.append(setupZBX("VISIT", "ED_CONNECT_PATIENT", "Y" ));
        } else if (aInMessage.isEvent("A01, A08, A28, A31")) {
            aOutGroup.append(setupZBX("VISIT", "ED_CONNECT_PATIENT", "N" ));
        }

// HARP/CHORD logic
        if (aGeneric.indexOf("HARP") >= 0) {
            aOutGroup.append(setupZBX("PMI", "HARP_FLAG", "HARP"));
        } else if (aGeneric.indexOf("CHORD") >= 0) {
            aOutGroup.append(setupZBX("PMI", "HARP_FLAG", "CHORD"));
        } else {
            aOutGroup.append(setupZBX("PMI", "HARP_FLAG", "NULL"));
        }

        aOutGroup.append(setupZBX("PMI", "INTERPRETER", aZPD.getField(CSC_23.ZPD_11_Interpreter_Required)));
        aOutGroup.append(setupZBX("PMI", "MEDICARE_EXPIRY", aZPD.getField(CSC_23.ZPD_7_Medicare_Expiry_Date)));
        aOutGroup.append(setupZBX("PMI", "PENSION_EXPIRY_DATE", aZPD.getField(CSC_23.ZPD_9_Pension_Exp_Date)));
        aOutGroup.append(setupZBX("PMI", "PBS_SAFETYNET_NUMBER", aZPD.getField(CSC_23.ZPD_10_PBS_Safetynet_Number)));
        aOutGroup.append(setupZBX("PMI", "DOB_ACCURACY", aZPD.getField("ZPD_22")));

// Visit related A28 and A31 specific info
        if (aInMessage.isEvent("A28, A31")) {
            if (! aPV1.isEmpty(HL7_23.PV1_8_referring_doctor)) {
                HL7Field aDrField = new HL7Field(aPV1.get(HL7_23.PV1_8_referring_doctor));
                String aDr = doDrTranslate(aPV1.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num));
                aDrField.setSubField(aDr, HL7_23.XCN_ID_num);
                aOutGroup.append(setupZBX("VISIT", "GPCode", aDr, aDrField));
            }
        }
// Create referring doctor ZBX segment for HASS system interface which requires doctors that do not have ID numbers
        String aReferringDoctor = aPV1.get(HL7_23.PV1_8_referring_doctor);
        aOutGroup.append(setupZBX("VISIT", "REF_DOC_CODE", aReferringDoctor));

// Medical Records Movement
        String aMedRecDate = aZMR.getField(CSC_23.ZMR_1_Last_Movement_Date);
        String aMedRecTime = aZMR.getField(CSC_23.ZMR_2_Last_Movement_Time);
        aOutGroup.append(setupZBX("MEDREC", "LAST_MOVE_DATE_TIME", aMedRecDate + aMedRecTime));
        aOutGroup.append(setupZBX("MEDREC", "VOLUME_NUMBER", aZMR.getField(CSC_23.ZMR_3_Volume_Number)));
        aOutGroup.append(setupZBX("MEDREC", "LOCATION", aZMR.getField(CSC_23.ZMR_4_Location)));
        aOutGroup.append(setupZBX("MEDREC", "RECEIVED_BY", aZMR.getField(CSC_23.ZMR_5_Received_By)));
        aOutGroup.append(setupZBX("MEDREC", "EXTENSION", aZMR.getField(CSC_23.ZMR_6_Extension_phone)));

// Claim number and context
        if (! aZCD.isEmpty(CSC_23.ZCD_11_Claim_Number))  {
            String aClaimNumber = aZCD.get(CSC_23.ZCD_11_Claim_Number);
            String aContext = aZCD.get(CSC_23.ZCD_13_Compensable_Context);
            aOutGroup.append(setupZBX("FINANCE", "CLAIM_NUMBER", aClaimNumber));
            aOutGroup.append(setupZBX("FINANCE", "CONTEXT", aContext));
        }
// Mental Health number
        aOutGroup.append(setupZBX("PMI", "MENTAL_HEALTH_NUMBER", cMentalHealthID));

        return aOutGroup;
    }
}