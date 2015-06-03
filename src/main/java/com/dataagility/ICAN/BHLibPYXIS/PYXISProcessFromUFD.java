/*
 * PYXISProcessFromUFD.java
 *
 * Created on 11 October 2005, 17:04
 *
 */

package com.dataagility.ICAN.BHLibPYXIS;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * PYXISProcessFromUFD contains the methods required to build a PYXIS message
 * from a UFD HL7 message structure
 * @author Ray Fillingham and Norman Soh
 */
public class PYXISProcessFromUFD extends ProcessSegmentsFromUFD {
    /**
     * Constant class
     */
    BHConstants k = new BHConstants();
    /**
     * Class wide HL7 message class object
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";
    //--------------------------------------------------------------------------
    /**
     * This constructor creates a new instance of PYXISProcessFromUFD passing a HL7 UFD
     * message structure
     * @param pHL7Message HL7 message text string
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public PYXISProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
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

        mVersion = "A";
        String aPYXISMessageArray[] = {k.NULL, k.NULL, k.NULL};
        HL7Message aOutHL7Message = new HL7Message();
        mInHL7Message = new HL7Message(mHL7Message, k.TRIM_LAST);

        HL7Segment aMSHSegment = processMSHFromUFD("PYXIS");
        HL7Segment aEVNSegment = processEVNFromUFD();
        HL7Segment aPIDSegment = processPIDFromUFD();

        aMSHSegment = processPYXIS_MSH(aMSHSegment);
        aEVNSegment = processPYXIS_EVN(aEVNSegment);
        aPIDSegment = processPYXIS_PID(aPIDSegment);
        HL7Segment aPV1SegmentTemp = new HL7Segment(mInHL7Message.getSegment("PV1"));

        if (mInHL7Message.isEvent("A01, A02, A03, A04, A08, A12, A13, A28, A31") &&
                !aPV1SegmentTemp.hasValue(HL7_23.PV1_2_patient_class,"PO")) {

            HL7Segment aZPDSegment = new HL7Segment(PYXIS_231.ZPD);
            HL7Segment aPV1Segment = new HL7Segment(HL7_23.PV1);
            HL7Segment aPV2Segment = new HL7Segment(HL7_23.PV2);
            HL7Group aGT1Group = processGT1s_FromUFD();

            aPV1Segment.setSegment(mInHL7Message.getSegment(HL7_23.PV1));
            aPV2Segment.setSegment(mInHL7Message.getSegment(HL7_23.PV2));
            aZPDSegment = processPYXIS_ZPD(aZPDSegment, aPV1Segment);
            aPV1Segment = processPYXIS_PV1(aPV1Segment, aPV2Segment);
            aGT1Group = processPYXIS_GT1(aGT1Group, aMSHSegment);

            aOutHL7Message.setSegment(aMSHSegment.getSegment());
            aOutHL7Message.append(aEVNSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aZPDSegment);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aGT1Group);

            aPYXISMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aPYXISMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aPYXISMessageArray[2] = aOutHL7Message.getMessage();
        } else if (mInHL7Message.isEvent("A34") &&
                !aPV1SegmentTemp.hasValue(HL7_23.PV1_2_patient_class,"PO")) {
            HL7Segment aPD1Segment = new HL7Segment(k.NULL);
            HL7Segment aMRGSegment = processMRGFromUFD();

            aPD1Segment.setSegment(mInHL7Message.getSegment("PD1"));

            aOutHL7Message.setSegment(aMSHSegment.getSegment());
            aOutHL7Message.append(aEVNSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPD1Segment);
            aOutHL7Message.append(aMRGSegment);

            aPYXISMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aPYXISMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aPYXISMessageArray[2] = aOutHL7Message.getMessage();
        }
        return aPYXISMessageArray;
    }
    //--------------------------------------------------------------------------
    /**
     * PYXIS specific processing for MSH segment
     * @param aMSHSegment MSH segment class object
     * @return MSH segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processPYXIS_MSH(HL7Segment aMSHSegment) throws ICANException {
        //Set the message date and time
        aMSHSegment.set(HL7_23.MSH_7_message_date_time, aMSHSegment.getDateTime("yyyyMMddHHmm"));
        //Set the message control id
        //aMSHSegment.set(HL7_23.MSH_10_message_control_ID, aMSHSegment.getDateTime("yyyyMMddHHmm"));
        //Set the processing id
        aMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        //Set the version id
        aMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3.1");
        return aMSHSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * PYXIS specific processing for EVN segment
     * @param aEVNSegment EVN segment class object
     * @return EVN segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processPYXIS_EVN(HL7Segment aEVNSegment) throws ICANException {

        return aEVNSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * PYXIS specific processing for PID segment
     * @param aPIDSegment PID segment class object
     * @return PID segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processPYXIS_PID(HL7Segment aPIDSegment) throws ICANException {

        //process PID-3 ID
        String aPID3Array[] = aPIDSegment.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        HL7Field aPID3Field = new HL7Field();
        HL7Field aPID3FieldTemp = new HL7Field();
        int aPID3ArrayCount = aPID3Array.length;

        for (int i = 0; i < aPID3ArrayCount; i++) {
            aPID3Field.setField(aPID3Array[i]);

            if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("PI") ||
                    aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("MR") ||
                    aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase(k.NULL)) {
                aPID3FieldTemp.setSubField(aPID3Field.getSubField(HL7_23.CX_ID_number), HL7_23.CX_ID_number);
                aPID3FieldTemp.setSubField("MR", HL7_23.CX_ID_type_code);
                aPID3FieldTemp.setSubField(mFacility, HL7_23.CX_assigning_authority);
                aPID3Field.setField(aPID3FieldTemp.getField());
            }
            if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("PEN")) {
                aPID3Field.setSubField("DSS", HL7_23.CX_assigning_authority);
            }
            if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("MC")) {
                aPID3Field.setSubField("AUSHIC", HL7_23.CX_assigning_authority);
                aPID3Field.setSubField(mFacility, HL7_23.CX_assigning_fac);
                if (aPID3Field.getSubField(HL7_23.CX_ID_number).length() <= 3) {
                    String aPaddedSpace = "            "; //12 spaces
                    String aCode = aPID3Field.getSubField(HL7_23.CX_ID_number);
                    aPID3Field.setSubField(aPaddedSpace.concat(aCode), HL7_23.CX_ID_number);
                }
            }
            if (aPID3Field.getSubField(HL7_23.CX_ID_type_code).equalsIgnoreCase("VA")) {
                aPID3Field.setSubField("DVA", HL7_23.CX_ID_type_code);
                aPID3Field.setSubField("AUSDVA", HL7_23.CX_assigning_authority);
                aPID3Field.setSubField(mFacility, HL7_23.CX_assigning_fac);
            }

            aPID3Array[i] = aPID3Field.getField();
        }
        aPIDSegment.setRepeatFields(HL7_23.PID_3_patient_ID_internal, aPID3Array);


        //process PBS safetynet number
        String aPBSNumber = getFromZBX("PMI", "PBS_SAFETYNET_NUMBER");
        if (!aPBSNumber.equalsIgnoreCase(k.NULL)) {
            String aPID3String = aPIDSegment.get(HL7_23.PID_3_patient_ID_internal);
            aPID3Field = new HL7Field();
            aPID3Field.setSubField(aPBSNumber, HL7_23.CX_ID_number);
            aPID3Field.setSubField("AUSHIC", HL7_23.CX_assigning_authority);
            aPID3Field.setSubField("SNET", HL7_23.CX_ID_type_code);
            aPID3Field.setSubField(mFacility, HL7_23.CX_assigning_fac);

            aPID3String = aPID3String.concat(k.REPEAT_SET).concat(aPID3Field.getField());
            aPIDSegment.set(HL7_23.PID_3_patient_ID_internal, aPID3String);
        }

        //process PID-5 patient name - only pass name with name type of "L"
        String aPID5Array[] = aPIDSegment.getRepeatFields(HL7_23.PID_5_patient_name);
        HL7Field aPID5Field = new HL7Field();
        int aPID5ArrayCount = aPID5Array.length;
        String aPID5String = k.NULL;

        for (int i = 0; i < aPID5ArrayCount; i++) {
            aPID5Field.setField(aPID5Array[i]);
            if (aPID5Field.getSubField(HL7_23.XPN_name_type).equalsIgnoreCase("L")) {
                aPID5String = aPID5Field.getField();
                i = aPID5ArrayCount;
                break;
            }
        }
        aPIDSegment.setField(aPID5String, HL7_23.PID_5_patient_name);

        //process first address in PID-11 Patient address
        String aPID11Array[] = aPIDSegment.getRepeatFields(HL7_23.PID_11_patient_address);
        int aPID11ArrayCount = aPID11Array.length;
        if (aPID11ArrayCount > 0) {
            aPIDSegment.set(HL7_23.PID_11_patient_address, aPID11Array[0]);
        }

        //process first phone number in PID-13 home phone
        String aPID13Array[] = aPIDSegment.getRepeatFields(HL7_23.PID_13_home_phone);
        int aPID13ArrayCount = aPID13Array.length;
        if (aPID11ArrayCount > 0) {
            aPIDSegment.set(HL7_23.PID_13_home_phone, aPID13Array[0]);
        }

        //process DVA Card Type PID_27 veterans military status
        String aDVACardType = getFromZBX("PMI", "DVA_CARD_TYPE");
        if (aDVACardType.length() > 0) {
            CodeLookUp aLookUpValue = new CodeLookUp("PYXIS_DVA_CardType.table", mEnvironment);
            String aCodeValue = aLookUpValue.getValue(aDVACardType);
            aPIDSegment.set(HL7_23.PID_27_veterans_military_status, aCodeValue);
        }

        //remove trailing spaces
        String aTempStr = aPIDSegment.getSegment();
        aTempStr = aTempStr.replaceAll(" \\^", "^");
        aPIDSegment.setSegment(aTempStr);
        return aPIDSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * PYXIS specific processing for ZPD segment
     * @param aZPDSegment ZPD segment class object
     * @param aPV1Segment PV1 segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class object
     * @return ZPD segment class object
     */
    public HL7Segment processPYXIS_ZPD(HL7Segment aZPDSegment, HL7Segment aPV1Segment) throws ICANException {
        String aDate = k.NULL;

        if (aPV1Segment.isEmpty(HL7_23.PV1_10_hospital_service) ||
                aPV1Segment.get(HL7_23.PV1_10_hospital_service).equalsIgnoreCase("\"\"")) {
            if (aPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("E")) {
                //For eastern, MERLIN will replace PYXIS - so the following if syntax should be amended later
                if (mFacility.matches("ANG|BHH|MAR|PJC")) {
                    aZPDSegment.set(PYXIS_231.ZPD_2_patient_team, "EMER");
                } else {
                    aZPDSegment.set(PYXIS_231.ZPD_2_patient_team, mFacility.concat("-EMER"));
                }
            } else {
                aZPDSegment.set(PYXIS_231.ZPD_2_patient_team, "\"\"");
            }
        } else {
            //For eastern, MERLIN will replace PYXIS - so the following if syntax should be amended later
            if (mFacility.matches("ANG|BHH|MAR|PJC")) {
                aZPDSegment.set(PYXIS_231.ZPD_2_patient_team, aPV1Segment.get(HL7_23.PV1_10_hospital_service));
            } else {
                aZPDSegment.set(PYXIS_231.ZPD_2_patient_team, mFacility.substring(0,1).concat("-").concat(aPV1Segment.get(HL7_23.PV1_10_hospital_service)));
            }
            //add claim number to ZPD_1
            String aClaimNumber = getFromZBX("FINANCE", "CLAIM_NUMBER");
            aZPDSegment.set(PYXIS_231.ZPD_1_claim_number, aClaimNumber);
        }

        //process medicare expiry date
        aDate = getFromZBX("PMI", "MEDICARE_EXPIRY");
        if (aDate.length() > 0) {
            //modify the expiry date to HL7format
            if (aDate.indexOf("/") > 0) {
                //date format as mm/yyyy
                aDate = aDate.substring(3, 7).concat(aDate.substring(0, 2));
            }
            aZPDSegment.set(PYXIS_231.ZPD_7_Medicare_Exp_Date, aDate);
            if (!aDate.equalsIgnoreCase(k.NULL)) {
                if (aDate.length() == 6) {
                    String aReg1 = aDate.substring(4, 6);
                    if (aReg1.equalsIgnoreCase("02")) {
                        aZPDSegment.set(PYXIS_231.ZPD_7_Medicare_Exp_Date, aDate.concat("28"));
                    } else if (aReg1.equalsIgnoreCase("04") ||
                            aReg1.equalsIgnoreCase("06") ||
                            aReg1.equalsIgnoreCase("09") ||
                            aReg1.equalsIgnoreCase("11")) {
                        aZPDSegment.set(PYXIS_231.ZPD_7_Medicare_Exp_Date, aDate.concat("30"));
                    } else {
                        aZPDSegment.set(PYXIS_231.ZPD_7_Medicare_Exp_Date, aDate.concat("31"));
                    }
                }
            }
        }

        //process pension expiry date
        aDate = getFromZBX("PMI", "PENSION_EXPIRY_DATE");
        if (aDate.length() > 0) {
            aZPDSegment.set(PYXIS_231.ZPD_9_HCC_Pension_Exp_Date, aDate);
        }

        return aZPDSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * PYXIS specific processing for PV1 segment
     * @param aPV1Segment PV1 segment class object
     * @return PV1 segment class object
     * @param aPV2Segment PV2 segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class
     */
    public HL7Segment processPYXIS_PV1(HL7Segment aPV1Segment, HL7Segment aPV2Segment) throws ICANException {
        HL7Segment aOutPV1Segment = new HL7Segment(HL7_23.PV1);
        if (!aPV1Segment.getSegment().equalsIgnoreCase(k.NULL)) {
            aOutPV1Segment.linkTo(aPV1Segment);
            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);
            if (aOutPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("R")) {
                //return aOutPV1Segment;
            }
            if (!aPV1Segment.isEmpty(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu)) {
                //For eastern, MERLIN will replace PYXIS - so the following if syntax should be amended later
                if (mFacility.matches("ANG|BHH|MAR|PJC")) {
                    aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu,
                            aPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu));
                } else {
                    aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu,
                            mFacility.substring(0,1).concat("-").concat(aPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu)));
                }
            } else {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "\"\"");
            }
            if (!aPV1Segment.isEmpty(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu)) {
                if (aPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("E") ||
                        aPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("O")) {
                    aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_room, "\"\"");
                } else {
                    aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "1");
                }
            } else {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "\"\"");
            }
            if (!aPV1Segment.isEmpty(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed)) {
                if (aPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("E") ||
                        aPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("O")) {
                    aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed, "\"\"");
                } else {
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                }
            } else {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, "\"\"");
            }
            if (!aPV1Segment.isEmpty(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID)) {
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID);
            } else {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "\"\"");
            }
            if (!aPV1Segment.isEmpty(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_status)) {
                //Do nothing leave as default value
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_status);
            } else {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_status, "\"\"");
            }
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            if (aPV1Segment.isEmpty(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name) ||
                    aPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num).equalsIgnoreCase("XX999")) {
                if (aPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("E")) {
                    aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, "FM264");
                } else {
                    //Do nothing
                }
            } else {
                if (aPV1Segment.isEmpty(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num)) {
                    //Throw an exception here
                } else {
                    aOutPV1Segment.copy(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num);
                }
            }
            if (!aPV1Segment.isEmpty(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number)) {
                String aVisitNumber = aPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
                String aPatientClass = aPV1Segment.get(HL7_23.PV1_2_patient_class);
                String aHospPrefix = mFacility.substring(0,1);

                if (aVisitNumber.startsWith("I")) {
                    aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
                } else if (aVisitNumber.startsWith("R")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aHospPrefix.concat(aVisitNumber));
                } else if (!aVisitNumber.startsWith("I") && aPatientClass.equalsIgnoreCase("I")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aPatientClass.concat(aVisitNumber));
                } else if (!aVisitNumber.startsWith("R") && aPatientClass.equalsIgnoreCase("R")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aHospPrefix.concat(aPatientClass.concat(aVisitNumber)));
                } else {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNumber);
                }
                ////remove leading I char from visit number if exists
                //String aVisitNumber = aPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
                //if (!aVisitNumber.startsWith("I")) {
                //    aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
                //} else {
                //    aVisitNumber = aVisitNumber.substring(1);
                //    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNumber);
                //}
            } else {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, "\"\"");
            }
            //map financial data
            if (mFacility.matches("ANG|BHH|MAR|PJC")) {
                aOutPV1Segment.copy(HL7_23.PV1_20_financial_class);
            } else {
                CodeLookUp mLook = new CodeLookUp("CERNER_PayClass_PYXIS.table", mEnvironment);
                String aCodeValue = mLook.getValue(aPV1Segment.get(HL7_23.PV1_20_financial_class));
                aOutPV1Segment.set(HL7_23.PV1_20_financial_class, aCodeValue);
            }

            if (aPV1Segment.get(HL7_23.PV1_2_patient_class).equalsIgnoreCase("O")) {
                aOutPV1Segment.set(HL7_23.PV1_44_admit_date_time, aPV2Segment.get(HL7_23.PV2_8_expected_admit_date));
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
            }
            aOutPV1Segment.copy(HL7_23.PV1_45_discharge_date_time);
        }
        return aOutPV1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     * PYXIS specific processing for GT1 segment
     * @param aGT1Group GT1 group class object
     * @param aMSHSegment MSH segment class object
     * @throws BHLibClasses.ICANException Throws ICANException Exception class object
     * @return GT1 group class object
     */
    public HL7Group processPYXIS_GT1(HL7Group aGT1Group, HL7Segment aMSHSegment) throws ICANException {
        HL7Segment aGT1Segment = new HL7Segment(HL7_23.GT1);
        HL7Group aOutGroup = new HL7Group();
        int aCountGT1Segments = aGT1Group.countSegments(HL7_23.GT1);
        for (int i = 1; i <= aCountGT1Segments; i++) {
            aGT1Segment.setSegment(aGT1Group.getSegment(HL7_23.GT1,i));
            //---
            if (!aGT1Segment.get(HL7_23.GT1_3_guarantor_name).equalsIgnoreCase(k.NULL)) {
                //Do nothing keep default value
            } else {
                aGT1Segment.set(HL7_23.GT1_3_guarantor_name, "\"\"");
            }
            //----
            aOutGroup.append(aGT1Segment);
        }
        if (aCountGT1Segments == 0 &&
                (aMSHSegment.get(HL7_23.MSH_9_2_trigger_event).equalsIgnoreCase("A01") ||
                aMSHSegment.get(HL7_23.MSH_9_2_trigger_event).equalsIgnoreCase("A04"))) {
            aGT1Segment.set(HL7_23.GT1_1_set_ID, "1");
            aGT1Segment.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name, "\"\"");
            aGT1Segment.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_given_name, "\"\"");
            aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, "\"\"");
            aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, "\"\"");
            aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, "\"\"");
            aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, "\"\"");
            aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, "\"\"");
            aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_country, "\"\"");
            aGT1Segment.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, "\"\"");
            aGT1Segment.set(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telephone_number, "\"\"");
            aGT1Segment.set(HL7_23.GT1_10_guarantor_type, "FR");
            aOutGroup.append(aGT1Segment);
        }
        aGT1Group.setGroup(aOutGroup.getGroup());
        return aGT1Group;
    }
    //--------------------------------------------------------------------------
    /**
     * PYXIS specific processing for PID segment (overiding generic version)
     * @throws BHLibClasses.ICANException Throws ICANException Exception class object
     * @return PID segment class object
     */
    public HL7Segment processPIDFromUFD() throws ICANException {
        HL7Segment aPIDSegmentIN = new HL7Segment(NULL);
        HL7Segment aPIDSegmentOUT = new HL7Segment("PID");

        aPIDSegmentIN.setSegment(mInHL7Message.getSegment(HL7_24.PID));

// Initialze aPIDSegmentOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PID_1_set_ID,
            HL7_23.PID_3_patient_ID_internal,
            HL7_23.PID_4_alternate_patient_ID,
            HL7_23.PID_5_patient_name,
            HL7_23.PID_7_date_of_birth,
            HL7_23.PID_8_sex,
            HL7_23.PID_10_race,
            HL7_23.PID_11_patient_address,
            HL7_23.PID_12_county_code,
            HL7_23.PID_29_patient_death_date_time
        };
        aPIDSegmentOUT.linkTo(aPIDSegmentIN);
        aPIDSegmentOUT.copyFields(aCopyFields);

        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_13_home_phone,HL7_23.XTN_telephone_number)) {
            aPIDSegmentOUT.set(HL7_23.PID_13_home_phone, aPIDSegmentIN.get(HL7_23.PID_13_home_phone));
        }
        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number)) {
            aPIDSegmentOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number);
            aPIDSegmentOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telecom_use);
        }
        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_15_language,HL7_23.CE_ID_code)) {
            aPIDSegmentOUT.copy(HL7_23.PID_15_language,HL7_23.CE_ID_code);
        }
        return aPIDSegmentOUT;
    }
}
