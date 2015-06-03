/*
 * SQLProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.dataagility.ICAN.BHLibICUACTIVE;
import com.dataagility.ICAN.BHLibClasses.*;
import com.dataagility.ICAN.BHLibCSC.*;
//import com.dataagility.ICAN.BHLibSQL.*;

/**
 *
 * @author fillinghamr
 */
public class ICUACTIVEProcessFromUFD extends ProcessSegmentsFromUFD {

    public String mEnvironment = "";
    /**
     * Creates a new instance of SQLProcessFromUFD
     */
    public ICUACTIVEProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {
        String aSQLMessageArray[] = {k.NULL, k.NULL, k.NULL};
        String aSegment;
        HL7Group aGroup;
        HL7Message aInMess = new HL7Message(mHL7Message);
        HL7Message aOutMess = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = new HL7Segment(aInMess.getSegment(HL7_23.MSH));
        HL7Segment aPV1SegmentTemp = new HL7Segment(aInMess.getSegment("PV1"));

        if (aInMess.isEvent("A17") &&
                super.hasValue(HL7_23.MSH_9_1_message_type,"ADT") &&
                !aPV1SegmentTemp.hasValue(HL7_23.PV1_2_patient_class,"PO")) {
            aOutMess.setSegment(processMSHFromUFD("SQL").getSegment());
            aOutMess.append(processEVNFromUFD());
            HL7Group aPerson1 = processA17GroupFromUFD(1);
            int aCnt = aPerson1.countSegments();
            int i;
            for (i=1; i <= aCnt; i++) {
                aOutMess.append(aPerson1.getSegment(i));
            }
            HL7Group aPerson2 = processA17GroupFromUFD(2);
            aCnt = aPerson2.countSegments();
            for (i=1; i <= aCnt; i++) {
                aOutMess.append(aPerson2.getSegment(i));
            }
            aSQLMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aSQLMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aSQLMessageArray[2] = aOutMess.getMessage();

        } else if (aInMess.isEvent("A34") &&
                super.hasValue(HL7_23.MSH_9_1_message_type,"ADT") &&
                !aPV1SegmentTemp.hasValue(HL7_23.PV1_2_patient_class,"PO")) {
            aOutMess.setSegment(processMSHFromUFD("SQL").getSegment());
            aOutMess.append(processEVNFromUFD());
            aOutMess.append(processPIDFromUFD());
            //aOutMess.append(processPV1FromUFD());
            aOutMess.append(processMRGFromUFD());
            aSQLMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aSQLMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aSQLMessageArray[2] = aOutMess.getMessage();

        } else if (super.hasValue(HL7_23.MSH_9_1_message_type,"ADT") &&
                !aPV1SegmentTemp.hasValue(HL7_23.PV1_2_patient_class,"PO")) {
            aOutMess.setSegment(processMSHFromUFD("SQL").getSegment());
            aOutMess.append(processEVNFromUFD());
            aOutMess.append(processPIDFromUFD());

            //aOutMess.append(processZPIFromUFD());
            //aOutMess.append(processZEIFromUFD());
            aOutMess.append(processNK1s_FromUFD());
            aOutMess.append(processPV1FromUFD());
            aOutMess.append(processPV2FromUFD());
            //aOutMess.append(processZVIFromUFD());
            // OBX

            //aOutMess.append(processAL1s_FromUFD());
            aOutMess.append(processDRGFromUFD());
            aOutMess.append(processDG1s_FromUFD());
            aOutMess.append(processPR1s_FromUFD());
            aOutMess.append(processGT1s_FromUFD());
            //  ZPD
            //  ZMR
            aGroup = processZBXs_FromUFD();
            aOutMess.append(aGroup.getGroup());
            //  ZUI
            aSQLMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aSQLMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aSQLMessageArray[2] = aOutMess.getMessage();
        }
        return aSQLMessageArray;
    }
    //--------------------------------------------------------------------------------
    /**
     * SQL specific processing for an Outgoing i.e "To" MSH segment.
     * @return Returns the processed HL7 MSH segment as a String.
     */
    public HL7Segment processMSHFromUFD(String pReceivingApplication) throws ICANException {
// Non copy fields are
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);

        HL7Segment aMSHSegmentIN = new HL7Segment(NULL);
        HL7Segment aMSHSegmentOUT = new HL7Segment("MSH");

        aMSHSegmentIN.setSegment(aHL7Message.getSegment(HL7_24.MSH));
        mHL7Segment = aMSHSegmentIN;                    // In case any of the "do" functions need to see the segment
        mFacility = aMSHSegmentIN.get(HL7_24.MSH_4_sending_facility);
        mHospitalID = mFacility.substring(mFacility.length()-1, mFacility.length());
        mHL7MessageEvent = aMSHSegmentIN.get(HL7_23.MSH_9_message_type, "CM_2");

// Initialze aMSHSegmentOUT with those fields that are straight copies
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
        aMSHSegmentOUT.linkTo(aMSHSegmentIN);
        aMSHSegmentOUT.copyFields(aCopyFields);

        aMSHSegmentOUT.set(HL7_23.MSH_5_receiving_application, pReceivingApplication);
        aMSHSegmentOUT.set(HL7_23.MSH_7_message_date_time, aMSHSegmentIN.getDateTime());
        aMSHSegmentOUT.copy(HL7_23.MSH_9_2_trigger_event);

        return (aMSHSegmentOUT);
    }
    //--------------------------------------------------------------------------------
    /**
     * SQL specific processing for an Outgoing i.e "To Vendor" NK1 segment.
     * @return Returns the processed HL7 NK1 segment as a Group.
     */
    public HL7Group processNK1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aNK1SegmentIN = new HL7Segment(k.NULL);
        HL7Segment aNK1SegmentOUT = new HL7Segment("NK1");
        HL7Group aNK1GroupOUT = new HL7Group();
        // Initialze aNK1SegmentOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.NK1_1_set_ID,
                    HL7_23.NK1_3_next_of_kin_relationship,
                    HL7_23.NK1_4_next_of_kin__address,
                    HL7_23.NK1_7_contact_role
        };
        int aNK1GroupCount = aHL7Message.countSegments("NK1");
        for (int i = 1; i <= aNK1GroupCount; i++) {
            aNK1SegmentIN.setSegment(aHL7Message.getSegment("NK1", i));
            aNK1SegmentOUT.linkTo(aNK1SegmentIN);
            aNK1SegmentOUT.copyFields(aCopyFields);
            aNK1SegmentOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_family_name);
            aNK1SegmentOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_given_name);
            aNK1SegmentOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_middle_name);
            aNK1SegmentOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_prefix);

            aNK1SegmentOUT.copy(HL7_23.NK1_5_next_of_kin__phone, HL7_23.XTN_telephone_number);
            aNK1SegmentOUT.copy(HL7_23.NK1_6_business_phone_num, HL7_23.XTN_telephone_number);
            aNK1GroupOUT.append(aNK1SegmentOUT.getSegment());
        }
        return aNK1GroupOUT;
    }

    //--------------------------------------------------------------------------------
    /**
     * SQL specific processing for an Outgoing i.e "To" PID segment whre the PID is that contained in the HL7 message itself.
     * @return Returns the processed HL7 PID segment as a String.
     */

    public HL7Segment processPIDFromUFD() throws ICANException{
        return (this.processPIDFromUFD(mHL7Message));
    }

    /**
     * SQL specific processing for an Outgoing i.e "To" PID segment where the PID is in the [pHL7MessageBlock] .
     * @param pHL7MessageBlock Either a full HL7 Message or may just be an A17 Patient Group.
     * @return Returns the processed HL7 PID segment as a String.
     */
    public HL7Segment processPIDFromUFD(String pHL7MessageBlock) throws ICANException{

        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);

        HL7Segment aPIDSegmentIN = new HL7Segment(NULL);
        HL7Segment aPIDSegmentOUT = new HL7Segment("PID");

        aPIDSegmentIN.setSegment(aHL7Message.getSegment(HL7_24.PID));
//        mHL7Segment = aPIDSegmentIN;

// Initialze aPIDSegmentOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PID_1_set_ID,
                    HL7_23.PID_4_alternate_patient_ID,
                    HL7_23.PID_5_patient_name,
                    HL7_23.PID_7_date_of_birth,
                    HL7_23.PID_8_sex,
                    HL7_23.PID_10_race,
                    HL7_23.PID_12_county_code,
                    HL7_23.PID_15_language,
                    HL7_23.PID_16_marital_status,
                    HL7_23.PID_17_religion,
                    HL7_23.PID_18_account_number,
                    HL7_23.PID_19_SSN_number,
                    HL7_23.PID_21_mothers_ID,
                    HL7_23.PID_22_ethnic_group,
                    HL7_23.PID_23_birth_place,
                    HL7_23.PID_29_patient_death_date_time
        };

        aPIDSegmentOUT.linkTo(aPIDSegmentIN);
        aPIDSegmentOUT.copyFields(aCopyFields);

        // Process Unique identifiers(UR, Medicare, Pension and DVA) held in PID-3 ....
        // NOTE :- PBS Number is also held in PID-3 (type "PB") but when required is normally passed in a Z segment.

        String aTmpField[] =  aPIDSegmentIN.getRepeatFields(HL7_23.PID_3_patient_ID_internal);

        int i;
        HL7Field aInField;
        HL7Field aOutField;
        String aStr;
        CodeLookUp aLookUp = new CodeLookUp("SDMH_6To7Digit_UR.table", mEnvironment);

        for (i=0 ; i < aTmpField.length ; i++) {
            aInField = new HL7Field(aTmpField[i]);
            aOutField = new HL7Field();
            aStr = aInField.getSubField(HL7_23.CX_ID_type_code);
            if (aStr.equalsIgnoreCase("PI")) {
                if (mFacility.equalsIgnoreCase("SDMH") &&
                        Integer.parseInt(aInField.getSubField(HL7_23.CX_ID_number)) < 899546) {
                    String aSDMHIdentifier = aLookUp.getValue(aInField.getSubField(HL7_23.CX_ID_number));
                    aPIDSegmentOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aSDMHIdentifier);
                } else {
                    aPIDSegmentOUT.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number);
                }
                aPIDSegmentOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "PI");
                aPIDSegmentOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "MRN");
                aPIDSegmentOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_fac, mFacility);
            }
            if (aStr.equalsIgnoreCase("VA")) {
                aPIDSegmentOUT.set(HL7_23.PID_27_veterans_military_status, aInField.getSubField(HL7_23.CX_ID_number));
            }
            if (aStr.equalsIgnoreCase("PEN")) {
                aPIDSegmentOUT.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number, aInField.getSubField(HL7_23.CX_ID_number));
                aPIDSegmentOUT.set(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_type_code, "PE");
            }
            if (aStr.equalsIgnoreCase("MC")) {
                aPIDSegmentOUT.set(HL7_23.PID_19_SSN_number, aInField.getSubField(HL7_23.CX_ID_number));
            }
        }

        // Only take the first address
        String aTmpAddField[] = aPIDSegmentIN.getRepeatFields(HL7_23.PID_11_patient_address);
        aPIDSegmentOUT.set(HL7_23.PID_11_patient_address, aTmpAddField[0]);

        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_13_home_phone,HL7_23.XTN_telephone_number)) {
            aPIDSegmentOUT.copy(HL7_23.PID_13_home_phone,HL7_23.XTN_telephone_number);
        }
        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number)) {
            aPIDSegmentOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number);
        }

        return aPIDSegmentOUT;
    }
    //--------------------------------------------------------------------------------
    /**
     * SQL specific processing for an Outgoing i.e "To SQL" PV1 segment.
     * @return Returns the processed HL7 PV1 segment as a String.
     */

    public HL7Segment processPV1FromUFD() throws ICANException {
        return (this.processPV1FromUFD(mHL7Message));
    }
    public HL7Segment processPV1FromUFD(String pHL7MessageBlock) throws ICANException {
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aPV1SegmentIN = new HL7Segment(NULL);
        HL7Segment aPV1SegmentOUT = new HL7Segment("PV1");

        HL7Segment aPV2SegmentIN = new HL7Segment(NULL);
        aPV2SegmentIN.setSegment(aHL7Message.getSegment(HL7_24.PV2));

        aPV1SegmentIN.setSegment(aHL7Message.getSegment(HL7_24.PV1));

// Initialze aPV1SegmentOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PV1_1_set_ID,
                    HL7_23.PV1_2_patient_class,
                    HL7_23.PV1_3_assigned_patient_location,
                    HL7_23.PV1_4_admission_type,
                    HL7_23.PV1_5_preadmit_num,
                    HL7_23.PV1_6_prior_patient_location,
                    HL7_23.PV1_10_hospital_service,
                    HL7_23.PV1_11_temporary_location,
                    HL7_23.PV1_14_admit_source,
                    HL7_23.PV1_15_ambulatory_status,
                    HL7_23.PV1_17_admitting_doctor,
                    HL7_23.PV1_18_patient_type,
                    HL7_23.PV1_21_charge_price_indicator,
                    HL7_23.PV1_36_discharge_disposition,
                    HL7_23.PV1_37_discharged_to_location,
                    HL7_23.PV1_44_admit_date_time,
                    HL7_23.PV1_45_discharge_date_time
        };
        aPV1SegmentOUT.linkTo(aPV1SegmentIN);
        aPV1SegmentOUT.copyFields(aCopyFields);

// Force Room 1 if no room received ...
        if (!aPV1SegmentOUT.isEmpty(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu)
        && aPV1SegmentOUT.isEmpty(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room)) {
            aPV1SegmentOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "1");
        }

// Force Room 1 for previous location if no room received ...
        if (!aPV1SegmentOUT.isEmpty(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu)
        && aPV1SegmentOUT.isEmpty(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_room)) {
            aPV1SegmentOUT.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_room, "1");
        }

        aPV1SegmentOUT = doDoctor(aPV1SegmentOUT, HL7_23.PV1_7_attending_doctor);
        aPV1SegmentOUT = doDoctor(aPV1SegmentOUT, HL7_23.PV1_8_referring_doctor);
        aPV1SegmentOUT = doDoctor(aPV1SegmentOUT, HL7_23.PV1_9_consulting_doctor);

// Ensure we only get the Visit Number ...
// ... remove prefix char in PV1-19 visit number
        String aVisitNum = aPV1SegmentIN.get(HL7_23.PV1_19_visit_number);
        if (aVisitNum.startsWith("I") || aVisitNum.startsWith("R")) {
            aPV1SegmentOUT.set(HL7_23.PV1_19_visit_number, aVisitNum.substring(1));
        } else {
            aPV1SegmentOUT.copy(HL7_23.PV1_19_visit_number,HL7_23.CX_ID_number);
        }

        CodeLookUp aLookUp = new CodeLookUp("CERNER_PayClass_SQL.table", mEnvironment);
        String aCodeValue = aLookUp.getValue(aPV1SegmentIN.get(HL7_23.PV1_20_financial_class));
        aPV1SegmentOUT.set(HL7_23.PV1_20_financial_class, aCodeValue);

// For "R" encounters (A28 or A31) get the GP info and put in Referring Dr ....
        if (aHL7Message.isEvent("A28,A31")) {
            aPV1SegmentOUT.set(HL7_23.PV1_8_referring_doctor, getXCNFromZBX("VISIT", "GPCode"));
        }

        if (aPV1SegmentOUT.hasValue(HL7_23.PV1_2_patient_class, "O") ||
                (aPV1SegmentOUT.hasValue(HL7_23.PV1_2_patient_class, "E") &&
                !aPV2SegmentIN.isEmpty(HL7_23.PV2_8_expected_admit_date))) {
            aPV1SegmentOUT.set(HL7_23.PV1_44_admit_date_time, aPV2SegmentIN.get(HL7_23.PV2_8_expected_admit_date));
        } else {
            aPV1SegmentOUT.copy(HL7_23.PV1_44_admit_date_time);
        }

        return aPV1SegmentOUT;
    }

// Do the processing required for a Doctor entry
    private HL7Segment doDoctor(HL7Segment pSegment, String pFieldID) throws ICANException {
        pSegment.copy(pFieldID, HL7_23.XCN_ID_num);
        pSegment.copy(pFieldID, HL7_23.XCN_first_name);
        pSegment.copy(pFieldID, HL7_23.XCN_last_name);
        return pSegment;
    }

    //--------------------------------------------------------------------------------
    /**
     * Generic processing for an Outgoing i.e "To Vendor" PV2 segment.
     * @return Returns the processed HL7 PV2 segment as a String.
     */
    public HL7Segment processPV2FromUFD() throws ICANException {
        return (this.processPV2FromUFD(mHL7Message));
    }

    public HL7Segment processPV2FromUFD(String pHL7MessageBlock) {
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aPV2SegmentIN = new HL7Segment(NULL);
        HL7Segment aPV2SegmentOUT = new HL7Segment(NULL);

        aPV2SegmentIN.setSegment(aHL7Message.getSegment(HL7_24.PV2));

        if (aPV2SegmentIN.getSegment().length() > 0) {
            aPV2SegmentOUT = new HL7Segment("PV2");

// Initialze aPV2SegmentOUT with those fields that are straight copies
            String aCopyFields[] =  {
                HL7_23.PV2_8_expected_admit_date,
                        HL7_23.PV2_9_expected_discharge_date,
                        HL7_23.PV2_21_visit_publicity_code,
                        HL7_23.PV2_22_visit_protection_indic,
                        HL7_23.PV2_33_expected_surgery_date
            };
            aPV2SegmentOUT.linkTo(aPV2SegmentIN);
            aPV2SegmentOUT.copyFields(aCopyFields);

// SQL takes the text of the Admit Reason as the code ....
            aPV2SegmentOUT.move(HL7_23.PV2_3_admit_reason, HL7_23.CE_ID_code, HL7_23.PV2_3_admit_reason, HL7_23.CE_text);
        }
        return aPV2SegmentOUT;
    }
    //--------------------------------------------------------------------------------

    public HL7Group processZBXs_FromUFD() throws ICANException {

        HL7Segment aZPDOut = new HL7Segment("ZPD");
        HL7Segment aZMROut = new HL7Segment("ZMR");
        HL7Message aINMessage = new HL7Message(mHL7Message);
        HL7Segment aMSH = new HL7Segment(aINMessage.getSegment("MSH"));
        HL7Group aOutGroup = new HL7Group();

        HL7Segment aPV1In = new HL7Segment(aINMessage.getSegment("PV1"));
        String aZBXValue;
        String aMedRecDateTime;
        String aVal;
//
// ZPD segment processing
        aVal = aPV1In.get(HL7_23.PV1_10_hospital_service);
        aZPDOut.setField(aVal, CSC_23.ZPD_2_Generic_Code );
        aVal = getFromZBX("FINANCE", "CLAIM_NUMBER");
        aZPDOut.setField(aVal, CSC_23.ZPD_1_History_Indicator );
        aOutGroup.append(aZPDOut);
//
// ZMR segment processing
// NOTE: _ We don't process ZMR data from CERNER since it is not an authenticated source(MRL originates on CSC).
        if (!aMSH.hasValue(HL7_23.MSH_3_sending_application, "CERNERPM")) {
            aZBXValue = getFromZBX("MEDREC", "LAST_MOVE_DATE_TIME");

            if (aZBXValue.length() >  8) {
                aZMROut.setField(aZBXValue.substring(0, 8), CSC_23.ZMR_1_Last_Movement_Date );
                aZMROut.setField(aZBXValue.substring(8), CSC_23.ZMR_2_Last_Movement_Time);
                aZMROut.setField(getFromZBX("MEDREC", "VOLUME_NUMBER"),CSC_23.ZMR_3_Volume_Number);
                aZMROut.setField(getFromZBX("MEDREC", "LOCATION"),CSC_23.ZMR_4_Location);
                aZMROut.setField(getFromZBX("MEDREC", "RECEIVED_BY"),CSC_23.ZMR_5_Received_By);
                aZMROut.setField(getFromZBX("MEDREC", "EXTENSION"),CSC_23.ZMR_6_Extension_phone);
                aOutGroup.append(aZMROut);

            }
        }
        return aOutGroup;
    }

}
