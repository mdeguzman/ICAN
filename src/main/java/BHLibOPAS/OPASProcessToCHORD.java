/*
 * OPASProcessToCHORD.java
 *
 * Created on 22 November 2005, 11:10
 *
 */

package BHLibOPAS;

import BHLibClasses.*;

/**
 * OPASProcessToCHORD contains the methods required to build a CHORD message
 * from a OPAS HL7 message structure
 * @author Norman Soh and Ray Fillingham 2005
 */
public class OPASProcessToCHORD extends ProcessSegmentsToUFD {
    /**
     * A class wide message class containing the input HL7 message
     */
    public HL7Message mInHL7Message;
    /**
     * Class wide variable to hold the chord identifier
     */
    public String mCHORDIdent = "";
    public String mEnvironment = "";
    //--------------------------------------------------------------------------
    /**
     * Creates a new instance of OPASProcessToCHORD
     * @param pHL7Message HL7 message text string
     * @throws BHLibClasses.ICANException ICANException exception class object
     */
    public OPASProcessToCHORD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "b";    // OPASProcessToCHORD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process the message from OPAS to CHORD structure
     * @throws BHLibClasses.ICANException ICANException exception class object
     * @return UFD HL7 message text string
     */
    public String processMessage() throws ICANException {
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        HL7Segment aEVNSegment = processEVNToUFD();
        HL7Segment aPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        HL7Segment aPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        HL7Segment aPV2Segment = new HL7Segment(mInHL7Message.getSegment("PV2"));

        aMSHSegment = processOPASMSHToCHORD(aMSHSegment);
        aEVNSegment = processOPASEVNToCHORD(aEVNSegment);
        aPIDSegment = processOPASPIDToCHORD(aPIDSegment);
        aPV1Segment = processOPASPV1ToCHORD(aPV1Segment, aMSHSegment);
        aPV2Segment = processOPASPV2ToCHORD(aPV2Segment);

        aOutHL7Message.append(aMSHSegment);
        aOutHL7Message.append(aEVNSegment);
        aOutHL7Message.append(aPIDSegment);
        aOutHL7Message.append(aPV1Segment);
        aOutHL7Message.append(aPV2Segment);
        return aOutHL7Message.getMessage();
    }
    //--------------------------------------------------------------------------
    /**
     * Process MSH segment according to CHORD requirements
     * @param pMSHSegment MSH segment class object
     * @return MSH segment class object
     */
    public HL7Segment processOPASMSHToCHORD(HL7Segment pMSHSegment) {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        aOutMSHSegment.linkTo(pMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "DGATE");

        aOutMSHSegment.copy(HL7_23.MSH_4_sending_facility);
        aOutMSHSegment.move(HL7_23.MSH_6_receiving_facility, HL7_23.MSH_4_sending_facility);
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "CHORD");
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        if (aInPV1Segment.hasValue(HL7_23.PV1_2_patient_class, "O")) {
            aOutMSHSegment.set(HL7_23.MSH_9_1_message_type, "ADT");
            aOutMSHSegment.set(HL7_23.MSH_9_2_trigger_event, "A04");
        } else {
            aOutMSHSegment.copy(HL7_23.MSH_9_message_type);
        }
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aOutMSHSegment.getVerDateTime(mVersion));
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");

        return aOutMSHSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process EVN segment according to CHORD requirements
     * @param pEVNSegment EVN segment class object
     * @return EVN segment class object
     */
    public HL7Segment processOPASEVNToCHORD(HL7Segment pEVNSegment) {
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH", 2));

        if (pEVNSegment.getSegment().length() == 0) {
            pEVNSegment = new HL7Segment("EVN");
            pEVNSegment.set(HL7_23.EVN_2_date_time_of_event, pEVNSegment.getDateTime());
        }
        if (aInPV1Segment.hasValue(HL7_23.PV1_2_patient_class, "O")) {
            pEVNSegment.set(HL7_23.EVN_1_event_type_code, "A04");
        } else {
            pEVNSegment.set(HL7_23.EVN_1_event_type_code, aInMSHSegment.get(HL7_23.MSH_9_2_trigger_event));
        }
        return pEVNSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process PID segment according to CHORD requirements
     * @param pPIDSegment PID segment class object
     * @return PID segment class object
     */
    public HL7Segment processOPASPIDToCHORD(HL7Segment pPIDSegment) {
        HL7Segment aOutPIDSegment = new HL7Segment(HL7_23.PID);
        HL7Segment aInZUISegment = new HL7Segment(mInHL7Message.getSegment("ZUI"));
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH", 2));
        aOutPIDSegment.linkTo(pPIDSegment);
        aOutPIDSegment.copy(HL7_23.PID_1_set_ID);
        int aPIDCount = 1;
        //String aUniversalPatientID = aInZUISegment.get(OPAS_23.ZUI_2_universal_patient_id);
        String aUniversalPatientID = aInZUISegment.get("ZUI_1");
        String aPatientName = pPIDSegment.get(HL7_23.PID_5_patient_name, 1);
        if (aUniversalPatientID.length() > 0) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aUniversalPatientID, aPIDCount);
            mCHORDIdent = "UID ... ".concat(aUniversalPatientID).concat(" Name ... ").concat(aPatientName);
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "UID", aPIDCount);
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "UI", aPIDCount);
            aPIDCount++;
        } else {
            //do nothing
        }

        int aPID3FieldCount = pPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3FieldCount; i++) {
            if (pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("MR") ||
                    pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("PI") ||
                    pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).equalsIgnoreCase("")) {
                aOutPIDSegment.move(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number,  aPIDCount,
                        HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                String aRecFac = aInMSHSegment.get(HL7_23.MSH_6_receiving_facility);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aRecFac, aPIDCount);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "PI", aPIDCount);
                aOutPIDSegment.move(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac,  aPIDCount,
                        HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, i);
                aPIDCount++;
                break;
            }
        }

        //process PID-4
        String aPID4AltPatientID = pPIDSegment.get(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number, 1);
        if (aPID4AltPatientID.length() > 0) {
            aOutPIDSegment.move(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number,  aPIDCount,
                    HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number, 1);
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "PEN", aPIDCount);
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "PN", aPIDCount);
            aPIDCount++;
        } else {
            //do nothing
        }

        //process PID-5
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, 1);
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix, 1);
        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);

        //set ethnic origin
        if (pPIDSegment.get(HL7_23.PID_10_race).length() == 0) {
            aOutPIDSegment.set(HL7_23.PID_10_race, "9");
        } else {
            aOutPIDSegment.copy(HL7_23.PID_10_race);
        }

        //process PID-11
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country, 1);

        //process PID-13
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);

        //process PID-14
        aOutPIDSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);

        //process PID-15
        aOutPIDSegment.copy(HL7_23.PID_15_language, HL7_23.CE_ID_code);

        //process PID-16
        aOutPIDSegment.copy(HL7_23.PID_16_marital_status);

        //process PID-17
        aOutPIDSegment.copy(HL7_23.PID_17_religion);

        //process Medicare number
        String aMedicareNum = pPIDSegment.get(HL7_23.PID_19_SSN_number);
        if (aMedicareNum.length() > 0) {
            if (aMedicareNum.trim().length() == 10) {
                String aFormatMedicareNum = aMedicareNum.trim().concat("0");
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aFormatMedicareNum, aPIDCount);
            } else {
                if (aMedicareNum.trim().length() < 10) {
                    String aFormatMedicareNum = aMedicareNum.trim();
                    aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aFormatMedicareNum, aPIDCount);
                } else {
                    String aFormatMedicareNum = aMedicareNum.substring(0, 11);
                    aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aFormatMedicareNum, aPIDCount);
                }
            }
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "HIC", aPIDCount);
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "MC", aPIDCount);
            aPIDCount++;
        } else {
            //do nothing
        }

        //process PID-21
        aOutPIDSegment.copy(HL7_23.PID_21_mothers_ID, 1);

        //process PID-23
        if (pPIDSegment.get(HL7_23.PID_23_birth_place).length() == 0) {
            aOutPIDSegment.set(HL7_23.PID_23_birth_place, "0000");
        } else {
            aOutPIDSegment.copy(HL7_23.PID_23_birth_place);
        }

        //process PID-27
        String aDVANum = pPIDSegment.get(HL7_23.PID_27_veterans_military_status);
        if (aDVANum.length() > 0) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aDVANum, aPIDCount);
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "DVA", aPIDCount);
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "DV", aPIDCount);
            aPIDCount++;
        } else {
            //do nothing
        }

        //process PID-29
        aOutPIDSegment.copy(HL7_23.PID_29_patient_death_date_time);
        return aOutPIDSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process PV1 segment according to CHORD requirements
     * @param pPV1Segment PV1 segment class object
     * @param pMSHSegment MSH segment class object
     * @return PV1 segment class object
     */
    public HL7Segment processOPASPV1ToCHORD(HL7Segment pPV1Segment, HL7Segment pMSHSegment) {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        if (pPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(pPV1Segment);

            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
//
// If In or ED Patient Class ...
            if (pPV1Segment.hasValue(HL7_23.PV1_2_patient_class, "I") || pPV1Segment.hasValue(HL7_23.PV1_2_patient_class, "E")) {
                aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);
            } else {    // .... else force it to Out.
                aOutPV1Segment.set(HL7_23.PV1_2_patient_class, "O");
            }

            String aAdmissionType = pPV1Segment.get(HL7_23.PV1_4_admission_type);
            if (aAdmissionType.indexOf("^") >= 0) {
                HL7Field aAdmTypeField = new HL7Field(aAdmissionType);
                String aAdmTypeWord = aAdmTypeField.getSubField(2);
                aOutPV1Segment.set(HL7_23.PV1_4_admission_type, aAdmTypeWord);
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            }
            mCHORDIdent = mCHORDIdent.concat(" Event: ").concat(pPV1Segment.get(HL7_23.PV1_2_patient_class));
            String aHospitalService = pPV1Segment.get(HL7_23.PV1_10_hospital_service);
            String aPatientClass = pPV1Segment.get(HL7_23.PV1_2_patient_class);
            if (((aHospitalService.length() == 0) || aHospitalService.equalsIgnoreCase("\"\"")) &&
                    aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "Emergency");
            } else {
                if (aHospitalService.indexOf("^") >= 0) {
                    HL7Field aHospServiceField = new HL7Field(aHospitalService);
                    String aHospServiceWord = aHospServiceField.getSubField(2);
                    aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, aHospServiceWord);
                } else {
                    aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
                }
            }
            String aVisitNum = pPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
            if (aVisitNum.length() > 0) {
                aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
            } else {
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, "\"\"");
            }
            aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
            if (!pMSHSegment.get(HL7_23.MSH_9_2_trigger_event).equalsIgnoreCase("A01")) {
                aOutPV1Segment.copy(HL7_23.PV1_36_discharge_disposition);
                aOutPV1Segment.copy(HL7_23.PV1_37_discharged_to_location, HL7_23.CE_ID_code);
                aOutPV1Segment.copy(HL7_23.PV1_37_discharged_to_location, HL7_23.CE_text);
                aOutPV1Segment.copy(HL7_23.PV1_45_discharge_date_time);
            }
        } else {
            //do nothing
        }
        return aOutPV1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process PV1 segment according to CHORD requirements
     * @param pPV2Segment PV2 segment class object
     * @return PV2 segment class object
     */
    public HL7Segment processOPASPV2ToCHORD(HL7Segment pPV2Segment) {
        HL7Segment aOutPV2Segment = new HL7Segment("");
        if (pPV2Segment.getSegment().length() > 0) {
            aOutPV2Segment = new HL7Segment("PV2");
            aOutPV2Segment.linkTo(pPV2Segment);
            aOutPV2Segment.copy(HL7_23.PV2_3_admit_reason, HL7_23.CE_text);
            aOutPV2Segment.move(HL7_23.PV2_3_admit_reason, HL7_23.CE_ID_code, HL7_23.PV2_3_admit_reason, HL7_23.CE_text);
        }
        return aOutPV2Segment;
    }
}
