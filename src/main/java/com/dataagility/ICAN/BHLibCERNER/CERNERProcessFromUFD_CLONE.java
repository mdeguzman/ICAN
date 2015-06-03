/*
 * CERNERProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 */

package com.dataagility.ICAN.BHLibCERNER;

import com.dataagility.ICAN.BHLibClasses.*;

import java.text.*;
import java.util.*;

/**
 *
 * @author fillinghamr
 */
public class CERNERProcessFromUFD_CLONE extends ProcessSegmentsFromUFD {

    BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public HL7Message mInHL7Message;
    public String mHospitalPrefix = "";
    //
    // msReg1-4 are DATAGATE equivelently named temp string holding points.
    public String mSReg1 = "";
    public String mSReg2 = "";
    public String mSReg3 = "";
    public String mSReg4 = "";
    /**
     * Creates a new instance of CERNERProcessFromUFD
     */
    public CERNERProcessFromUFD_CLONE(String pHL7Message, String pEnvironment)  throws ICANException {
        //System.out.println("++++ Processing Message To CERNER");
        super(pHL7Message);
        System.out.println("++++ Processing Message To CERNER A");
        mVersion = "d";    // CERNERProcessFromUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {
        String aCERNERMessageArray[] = {k.NULL, k.NULL, k.NULL, k.NULL, k.NULL};
        System.out.println("++++ Processing Message To CERNER B");
        String aSegment;
        HL7Group aGroup;
        mInHL7Message = new HL7Message(mHL7Message);

        HL7Message aOutMess1= new HL7Message(k.NULL);
        HL7Message aOutMess2 = new HL7Message(k.NULL);
        HL7Message aOutMess3 = new HL7Message(k.NULL);

        HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));

        if (aMSHSegment.get(HL7_23.MSH_4_sending_facility).length() == 0) {
            aCERNERMessageArray[0] = "NOP";
            aCERNERMessageArray[1] = "NOP";
            aCERNERMessageArray[2] = "";
            aCERNERMessageArray[3] = "";
            aCERNERMessageArray[4] = "";
            return aCERNERMessageArray;
        }

// Make certain we do not process CERNER back to CERNER ------------------------
        try {
            if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("CERNERPM") >= 0 ||
                    aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("EXTN") >= 0) {
                aCERNERMessageArray[0] = "NOP";
                aCERNERMessageArray[1] = "NOP";
                aCERNERMessageArray[2] = "";
                aCERNERMessageArray[3] = "";
                aCERNERMessageArray[4] = "";
                return aCERNERMessageArray;
            }
        } catch (Exception e) {
            aCERNERMessageArray[0] = "NOP";
            aCERNERMessageArray[1] = "NOP";
            aCERNERMessageArray[2] = "";
            aCERNERMessageArray[3] = "";
            aCERNERMessageArray[4] = "";
            return aCERNERMessageArray;
        }

        mHospitalPrefix = aMSHSegment.get(HL7_23.MSH_4_sending_facility).substring(0,1);

        HL7Segment aPIDSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PID));
        HL7Segment aORCSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.ORC));
        HL7Segment aOBRSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.OBR));
        mUR = aPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);

//
// ADT A17 messages .... -------------------------------------------------------
        if (mInHL7Message.isEvent("A17") && hasValue(HL7_23.MSH_9_1_message_type,"ADT")) {
            aOutMess1.append(processMSHFromUFD("CERNERPM"));
            aOutMess1.append(processEVNFromUFD());
            HL7Group aPerson1 = processA17GroupFromUFD(1);
            int aCnt = aPerson1.countSegments();
            int i;
            for (i=1; i <= aCnt; i++) {
                aOutMess1.append(aPerson1.getSegment(i));
            }
            HL7Group aPerson2 = processA17GroupFromUFD(2);
            aCnt = aPerson2.countSegments();
            for (i=1; i <= aCnt; i++) {
                HL7Segment aA17PIDSeg = new HL7Segment(aPerson2.getSegment(i));
                if (aA17PIDSeg.getSegmentID().equalsIgnoreCase("PID")) {
                    if (aA17PIDSeg.get(HL7_23.PID_29_patient_death_date_time).equalsIgnoreCase("\"\"") ||
                            aA17PIDSeg.get(HL7_23.PID_29_patient_death_date_time).equalsIgnoreCase("")) {
                        aA17PIDSeg.set(HL7_23.PID_30_patient_death_indicator, "N");
                        aOutMess1.append(aA17PIDSeg);
                    }
                } else {
                    aOutMess1.append(aPerson2.getSegment(i));
                }
            }
//
// Set up default pass back array with blank message ...
            aCERNERMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aCERNERMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aCERNERMessageArray[2] = aOutMess1.getMessage();
            aCERNERMessageArray[3] = "";
            aCERNERMessageArray[4] = "";
//
// ADT A34 messages .... -------------------------------------------------------
        } else if (mInHL7Message.isEvent("A34") && hasValue(HL7_23.MSH_9_1_message_type,"ADT")) {
            aOutMess1.append(processMSHFromUFD("CERNERPM"));
            aOutMess1.append(processEVNFromUFD());
            aOutMess1.append(processPIDFromUFD());
//            aOutMess1.append(processPV1FromUFD());
            aOutMess1.append(processMRGFromUFD());

            aCERNERMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aCERNERMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aCERNERMessageArray[2] = aOutMess1.getMessage();
            aCERNERMessageArray[3] = "";
            aCERNERMessageArray[4] = "";
//
// General ADT messages .... ---------------------------------------------------
        } else if (mInHL7Message.isEvent("A01, A02, A03, A08, A11, A12, A13, A21, A22, A28, A31")) {
            aOutMess1.append(processMSHFromUFD("CERNERPM"));
            aOutMess1.append(processEVNFromUFD());
            aOutMess1.append(processPIDFromUFD());
            aOutMess1.append(processZPIFromUFD());
            aOutMess1.append(processZEIFromUFD());
            aOutMess1.append(processNK1s_FromUFD());
            aOutMess1.append(processPV1FromUFD());
            aOutMess1.append(processPV2FromUFD());
            aOutMess1.append(processZVIFromUFD());

            aGroup = processZBXSegmentsFromUFD();
            aOutMess1.append(aGroup.getGroup());

            aOutMess1.append(processAL1s_FromUFD());
            aOutMess1.append(processDRGFromUFD());
            aOutMess1.append(processGT1s_FromUFD());
            aOutMess1.append(processIN1FromUFD());

            aCERNERMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aCERNERMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aCERNERMessageArray[2] = aOutMess1.getMessage();
            aCERNERMessageArray[3] = "";
            aCERNERMessageArray[4] = "";
//
// Results .... NMHL7 and PARIS ------------------------------------------------
        } else if (mInHL7Message.isEvent("R01, R03")) {

            boolean aNoOrderID = aORCSegment.isEmpty(HL7_23.ORC_2_placer_order_num);
            String aAuthority = aPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, 1);

            String aAuthList = "11111, 70080, 70146, 70149, 70179, 70211, 70278, 70292, 73399, 74299, 88888, 40000, 40001, 40230, 40070, 70773";

            if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("PARIS") >= 0 &&
                    !mUR.startsWith("9") &&
                    !(hasValue(HL7_23.ORC_1_order_control,"NW") && aNoOrderID == true) &&
                    !(hasValue(HL7_23.ORC_1_order_control,"SC") && hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"LA") && hasValue(HL7_23.ORC_5_order_status,"IP") && aNoOrderID == true) &&
                    !(aAuthList.indexOf(aAuthority) >= 0)) {

                aOutMess1= processORU_FromUFD();

            } else if (hasValue(HL7_23.MSH_3_sending_application, "NMHL7")) {
                aOutMess1= processORU_FromUFD();
                aOutMess2= processFILMBAG_FromUFD();

            } else if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("PARIS") >= 0 &&
                    (hasValue(HL7_23.ORC_1_order_control,"SC") &&
                    hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"RA") &&
                    !(aAuthList.indexOf(aAuthority)>= 0))) {


                if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("PARIS") >= 0 ){
                    aOutMess2= processURL_FromUFD();
                }
                aOutMess3= processFILMBAG_FromUFD();

            }
            aCERNERMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aCERNERMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aCERNERMessageArray[2] = aOutMess1.getMessage();
            aCERNERMessageArray[3] = aOutMess2.getMessage();
            aCERNERMessageArray[4] = aOutMess3.getMessage();
//
// Orders .... -----------------------------------------------------------------
        } else if (mInHL7Message.isEvent("O01")) {

            boolean aNoOrderID = aORCSegment.isEmpty(HL7_23.ORC_2_placer_order_num);
            String aAuthority = aPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, 1);
            String aAuthList = "11111, 70080, 70146, 70149, 70179, 70211, 70278, 70292, 73399, 74299, 88888, 40000, 40001, 40230, 40070, 70773";
            String aSendingApp = aMSHSegment.get(HL7_23.MSH_3_sending_application);

            if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("PARIS") >= 0 &&
                    !mUR.startsWith("9") &&
                    !(hasValue(HL7_23.ORC_1_order_control,"NW") && aNoOrderID == false) &&
                    !(hasValue(HL7_23.ORC_1_order_control,"SC") && hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"LA") && hasValue(HL7_23.ORC_5_order_status,"IP") && aNoOrderID == true) &&
                    !(aAuthList.indexOf(aAuthority)>= 0)) {
//
//  Acceptable PARIS Order message for both Path and Radiology ...
                aOutMess1= processORM_FromUFD();
            }

            if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("PARIS") >= 0 &&
                    !mUR.startsWith("9") &&
                    (hasValue(HL7_23.ORC_1_order_control,"SC") && hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"RA") &&
                    !(aAuthList.indexOf(aAuthority)>= 0))) {

                aOutMess2= processURL_FromUFD();
                aOutMess3= processFILMBAG_FromUFD();

            }
            aCERNERMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aCERNERMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aCERNERMessageArray[2] = aOutMess1.getMessage();
            aCERNERMessageArray[3] = aOutMess2.getMessage();
            aCERNERMessageArray[4] = aOutMess3.getMessage();
        }

        return aCERNERMessageArray;
    }

//
//------------------------------------------------------------------------------
// Process a ORM message for PARIS or NMHL7
    public HL7Message processORM_FromUFD()  throws ICANException {

        HL7Message aMess = new HL7Message(k.NULL);
        HL7Segment aPIDSegment = processORM_ORU_PIDFromUFD();
        HL7Group aNTEGroup = processNTEs_FromUFD();
        HL7Group aReqDetsGroup = processO01ReqDets_FromUFD(aPIDSegment);

        aMess.append(processMSHFromUFD("CERNERPM"));
        aMess.append(aPIDSegment);
        aMess.append(aNTEGroup);
        aMess.append(processORM_ORU_PV1FromUFD());
        aMess.append(aReqDetsGroup);
        return aMess;
    }
//
//------------------------------------------------------------------------------
// Process a ORU message for PARIS or NMHL7
    public HL7Message processORU_FromUFD()  throws ICANException {

        HL7Message aMess = new HL7Message(k.NULL);
        HL7Segment aPIDSegment = processORM_ORU_PIDFromUFD();
        HL7Segment aMSHSegment = processMSHFromUFD("CERNERPM");
        HL7Group aNTEGroup = processNTEs_FromUFD();
        HL7Group aReqDetsGroup = processORU_R01_FromUFD(aPIDSegment);

        if (hasValue(HL7_23.MSH_3_sending_application, "NMHL7")) {  // Force NMHL7 to be PARIS-ALF, PARIS-CGMC or PARIS-SDMH
            aMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-".concat(aMSHSegment.get(HL7_23.MSH_4_sending_facility)));
        }
        aMess.append(aMSHSegment);
        aMess.append(processORM_ORU_PIDFromUFD());
        aMess.append(aNTEGroup);
        aMess.append(processORM_ORU_PV1FromUFD());
        aMess.append(aReqDetsGroup);
        return aMess;
    }
//
//------------------------------------------------------------------------------
// Create a FILMBAG message for PARIS or NMHL7
    public HL7Message processFILMBAG_FromUFD()  throws ICANException {
        HL7Message aMess = new HL7Message(k.NULL);
        HL7Segment aPIDSegment = processORM_ORU_PIDFromUFD();
        HL7Segment aMSHSegment = processFILMBAGMSHFromUFD();
//
// Create Message 2 (i.e. FILMBAG)
        aMess.append(processFILMBAGMSHFromUFD());
        aMess.append(processORM_ORU_PIDFromUFD());
        aMess.append(processNTEs_FromUFD());
        aMess.append(processFILMBAGPV1_FromUFD(aPIDSegment));
        aMess.append(processFILMBAGReqDets_FromUFD(aPIDSegment, aMSHSegment));

        return aMess;
    }
//
//------------------------------------------------------------------------------
// Create a URL message for PARIS only.
    public HL7Message processURL_FromUFD()  throws ICANException {
        HL7Message aMess = new HL7Message(k.NULL);
        HL7Segment aPIDSegment = processORM_ORU_PIDFromUFD();
        HL7Segment aMSHSegment = processURLMSH_FromUFD();
//
// Create Message 3 (i.e. URL)
        aMess.append(processURLMSH_FromUFD());
        aMess.append(processORM_ORU_PIDFromUFD());
        aMess.append(processNTEs_FromUFD());
        aMess.append(processURLPV1FromUFD());
        aMess.append(processURLReqDets_FromUFD(aPIDSegment, aMSHSegment));

        return aMess;
    }

//------------------------------------------------------------------------------
    /**
     * Generic processing for an Outgoing i.e "To Vendor" AL1 group.
     * @return Returns the processed HL7 AL1 group.
     */
    public HL7Group processAL1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aAL1SegIN = new HL7Segment(k.NULL);
        HL7Segment aAL1SegOUT = new HL7Segment("AL1");
        HL7Group aAL1GroupOUT = new HL7Group();
        // Initialze aAL1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.AL1_1_set_ID,
            HL7_23.AL1_6_identification_date
        };

        int aAL1GroupCount = aHL7Message.countSegments("AL1");
        for (int i = 1; i <= aAL1GroupCount; i++) {
            aAL1SegIN.setSegment(aHL7Message.getSegment("AL1", i));
            if (aAL1SegIN.getField(HL7_23.AL1_2_allergy_type).equalsIgnoreCase("AL")) {
                aAL1SegOUT.linkTo(aAL1SegIN);
                aAL1SegOUT.copyFields(aCopyFields);
                aAL1SegOUT.set(HL7_23.AL1_2_allergy_type, "D");
                aAL1SegOUT.set(HL7_23.AL1_3_allergy, HL7_23.CE_text,  aAL1SegIN.get(HL7_23.AL1_3_allergy, HL7_23.CE_ID_code)) ;

                aAL1GroupOUT.append(aAL1SegOUT.getSegment());
            }
        }
        return aAL1GroupOUT;
    }

//------------------------------------------------------------------------------
    /**
     * CERNER specific processing for an Outgoing i.e "To CERNER" EVN segment.
     * @return Returns the processed HL7 EVN segment as a String.
     */
    public HL7Segment processEVNFromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aEVNSegIN = new HL7Segment(k.NULL);
        HL7Segment aEVNSegOUT = new HL7Segment("EVN");

        aEVNSegIN.setSegment(aHL7Message.getSegment(HL7_24.EVN));

        // Initialze aEVNSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.EVN_1_event_type_code,
        };
        aEVNSegOUT.linkTo(aEVNSegIN);
        aEVNSegOUT.copyFields(aCopyFields);
        if (aHL7Message.isEvent("A17") || aHL7Message.isEvent("A34")) {
            aEVNSegOUT.copy(HL7_23.EVN_2_date_time_of_event);
            aEVNSegOUT.copy(HL7_23.EVN_6_event_occurred);
        } else {
            aEVNSegOUT.set(HL7_23.EVN_2_date_time_of_event, aEVNSegIN.get(HL7_23.EVN_6_event_occurred));
        }
        //
// We send A28's to CERNER as a A31 to ovoid creating patients twice.
        if (aHL7Message.isEvent("A28")) {
            aEVNSegOUT.set(HL7_23.EVN_1_event_type_code, "A31");
        }

// We send A11's to CERNER as a A23.
        if (aHL7Message.isEvent("A11")) {
            aEVNSegOUT.set(HL7_23.EVN_1_event_type_code, "A23");
        }

        return aEVNSegOUT;
    }

//------------------------------------------------------------------------------
    /**CERNER specific processing for an Outgoing i.e "To" MSH segment.
     * @return Returns the processed HL7 MSH segment as a String.
     */
    public HL7Segment processMSHFromUFD(String pReceivingApplication) throws ICANException {
// Non copy fields are

        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);

        HL7Segment aMSHSegIN = new HL7Segment(NULL);
        HL7Segment aMSHSegOUT = new HL7Segment("MSH");

        aMSHSegIN.setSegment(aHL7Message.getSegment(HL7_24.MSH));
        mHL7Segment = aMSHSegIN;                    // In case any of the "do" functions need to see the segment
        mFacility = aMSHSegIN.get(HL7_24.MSH_4_sending_facility);
        mHospitalPrefix = aMSHSegIN.get(HL7_23.MSH_4_sending_facility).substring(0, 1);

        if (mFacility.length() > 0) {
            mHospitalID = mFacility.substring(mFacility.length()-1, mFacility.length());
        }
        mHL7MessageEvent = aMSHSegIN.get(HL7_23.MSH_9_message_type, "CM_2");

// Initialze aMSHSegOUT with those fields that are straight copies
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
        aMSHSegOUT.linkTo(aMSHSegIN);
        aMSHSegOUT.copyFields(aCopyFields);

        aMSHSegOUT.set(HL7_23.MSH_5_receiving_application, pReceivingApplication);
        aMSHSegOUT.set(HL7_23.MSH_7_message_date_time, aMSHSegIN.getDateTime());
        aMSHSegOUT.copy(HL7_23.MSH_9_2_trigger_event);
//
// We send A28's to CERNER as a A31 to ovoid creating patients twice.
        if (aHL7Message.isEvent("A28")) {
            aMSHSegOUT.set(HL7_23.MSH_9_message_type, HL7_23.CM_event, "A31");
        }
//
// We send A23's to CERNER instead of A11
        if (aHL7Message.isEvent("A11")) {
            aMSHSegOUT.set(HL7_23.MSH_9_message_type, HL7_23.CM_event, "A23");
        }

        return (aMSHSegOUT);
    }

//------------------------------------------------------------------------------
    /**
     * CERNER specific processing for an Outgoing i.e "To" PID segment whre the PID is that contained in the HL7 message itself.
     * @return Returns the processed HL7 PID segment as a String.
     */
    public HL7Segment processPIDFromUFD() throws ICANException {
        return (this.processPIDFromUFD(mHL7Message));
    }
    /**
     * CERNER specific processing for an Outgoing i.e "To" PID segment where the PID is in the [pHL7MessageBlock] .
     * @param pHL7MessageBlock Either a full HL7 Message or may just be an A17 Patient Group.
     * @return Returns the processed HL7 PID segment as a String.
     */
    public HL7Segment processPIDFromUFD(String pHL7MessageBlock) throws ICANException {

        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);

        HL7Segment aPIDSegIN = new HL7Segment(k.NULL);
        HL7Segment aPIDSegOUT = new HL7Segment("PID");

        aPIDSegIN.setSegment(aHL7Message.getSegment(HL7_24.PID));
        // mHL7Segment = aPIDSegIN;

        // Initialze aPIDSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PID_4_alternate_patient_ID,
            HL7_23.PID_5_patient_name,
            HL7_23.PID_7_date_of_birth,
            HL7_23.PID_8_sex,
            HL7_23.PID_9_patient_alias,
            HL7_23.PID_10_race,
            HL7_23.PID_11_patient_address,
            HL7_23.PID_12_county_code,
            HL7_23.PID_16_marital_status,
            HL7_23.PID_17_religion,
            //HL7_23.PID_18_account_number,
            HL7_23.PID_19_SSN_number,
            HL7_23.PID_21_mothers_ID,
            HL7_23.PID_29_patient_death_date_time,
            HL7_23.PID_30_patient_death_indicator
        };

        aPIDSegOUT.linkTo(aPIDSegIN);
        aPIDSegOUT.copyFields(aCopyFields);

        // Process Unique identifiers(UR, Medicare, Pension and DVA) held in PID-3 ....
        // NOTE :- PBS Number is also held in PID-3 (type "PB") but when required is normally passed in a Z segment.
        String aTmpField[] = aPIDSegIN.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        int i;
        HL7Field aInField;
        HL7Field aOutField;
        String aStr;
        int aPID3repeat = 0;

        for (i=0 ; i < aTmpField.length ; i++) {
            aInField = new HL7Field(aTmpField[i]);
            aOutField = new HL7Field();
            aStr = aInField.getSubField(HL7_23.CX_ID_type_code);
            if (aStr.equalsIgnoreCase("PI")) {

                aOutField.setSubField(aPIDSegIN.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number), HL7_23.CX_ID_number);
                if ( ! (mHL7MessageEvent.equalsIgnoreCase("A17"))) {
                    aOutField.setSubField(mFacility, HL7_23.CX_assigning_fac);
                }
                aPIDSegOUT.set(HL7_23.PID_3_patient_ID_internal,aOutField.getField(),++aPID3repeat);
                mUR = aPIDSegOUT.get(HL7_23.PID_3_patient_ID_internal,  HL7_23.CX_ID_number);
            }
            if (aStr.equalsIgnoreCase("MH")) {
                if (!aInField.getSubField(HL7_23.CX_ID_number).equalsIgnoreCase("9999999999")) {
                    aPIDSegOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aInField.getSubField(HL7_23.CX_ID_number),++aPID3repeat);
                    aPIDSegOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aInField.getSubField(HL7_23.CX_assigning_authority),aPID3repeat);
                }
            }

            if (aStr.equalsIgnoreCase("PEN")) {
                aPIDSegOUT.set(HL7_23.PID_4_alternate_patient_ID, aInField.getSubField(HL7_23.CX_ID_number));
            }
            if (aStr.equalsIgnoreCase("VA")) {
                aPIDSegOUT.set(HL7_23.PID_2_patient_ID_external, aInField.getSubField(HL7_23.CX_ID_number));
            }
            if (aStr.equalsIgnoreCase("MC")) {
                String aTmp = aInField.getSubField(HL7_23.CX_ID_number);
                aPIDSegOUT.set(HL7_23.PID_19_SSN_number, aTmp);
                if (aTmp.length() <= 3) {
                    String aPaddedSpace = "            "; //12 spaces
                    aPIDSegOUT.set(HL7_23.PID_19_SSN_number, aPaddedSpace.concat(aTmp));
                }
            }
        }
        aPIDSegOUT.linkTo(aPIDSegIN);

        String[] aRepeat = aPIDSegIN.getRepeatFields(HL7_23.PID_13_home_phone);
        aOutField = new HL7Field();
        for (i=0; i < aRepeat.length ; i++) {
            aInField = new HL7Field(aRepeat[i]);
            aOutField.setSubField(aInField.getSubField(HL7_23.XTN_telephone_number), HL7_23.XTN_telephone_number);
            aOutField.setSubField(aInField.getSubField(HL7_23.XTN_telecom_use), HL7_23.XTN_telecom_use);
            aRepeat[i] = aOutField.getField();
        }
        aPIDSegOUT.setRepeatFields(HL7_23.PID_13_home_phone,aRepeat);

        if (!aPIDSegIN.isEmpty(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number)) {
            aPIDSegOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number);
            aPIDSegOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telecom_use);
        }
        aPIDSegOUT.copy(HL7_23.PID_15_language, HL7_23.CE_ID_code);
        aPIDSegOUT.move(HL7_23.PID_22_ethnic_group, HL7_23.PID_23_birth_place);

        //Process PID_18
        HL7Segment aPV1Seg = new HL7Segment(mInHL7Message.getSegment("PV1"));
        String aVisitNum = aPV1Seg.get(HL7_23.PV1_19_visit_number);
        if (mInHL7Message.isEvent("A28, A31")) {
            aPIDSegOUT.set(HL7_23.PID_18_account_number, HL7_23.CX_ID_number, mHospitalPrefix.concat(aVisitNum));
            //aPIDSegOUT.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "ALFFN");
        } else {
            //for all other message types
            //aPIDSegOUT.copy(HL7_23.PID_18_account_number);
            aPIDSegOUT.set(HL7_23.PID_18_account_number, aVisitNum);
        }

        return aPIDSegOUT;
    }

    //------------------------------------------------------------------------------
    /**
     * Generic processing for an Outgoing Group in ana A17 message.<p>
     * @return Returns the processed HL7 PID group as a HL7Group.
     */
    public HL7Group processA17GroupFromUFD( int pGroupNumber) throws ICANException {

        HL7Message aInMessage = new HL7Message(mHL7Message) ;
        String aGroup = aInMessage.getGroup(HL7_23.Group_A17_Patient, pGroupNumber);
        HL7Group aOutGroup = new HL7Group();

        HL7Segment aOutPID = this.processPIDFromUFD(aGroup);
        aOutGroup.append(aOutPID.getSegment());

        HL7Segment aOutPV1 = this.processPV1FromUFD(aGroup);
        aOutGroup.append(aOutPV1.getSegment());

        return aOutGroup;

    }

//------------------------------------------------------------------------------
    public HL7Segment processZPIFromUFD() throws ICANException {
        HL7Message aHL7Mess = new HL7Message(mHL7Message, 0);
        HL7Segment aZPIOut = new HL7Segment("ZPI");
        String aHarpFlag;

        aHarpFlag = getFromZBX("PMI", "HARP_FLAG");
        aZPIOut.set(CERNER_23.ZPI_8_Family_Doctor, getXCNFromZBX("VISIT", "GPCode"));

        if (aHarpFlag.equalsIgnoreCase("HARP") || aHarpFlag.equalsIgnoreCase("CHORD")) {
            aZPIOut.set(CERNER_23.ZPI_13_Church, aHarpFlag);
        } else {
            if (aHL7Mess.isEvent("A01, A08, A28, A31") ) {
                aZPIOut.set(CERNER_23.ZPI_13_Church, "NULL");
            }
        }
//
// Sort out the SDMH 6/7 digit Patient UR bits
        String  aUR;
        HL7Segment aPID;
        aPID = new HL7Segment(aHL7Mess.getSegment("PID"));
        aUR = aPID.get(HL7_23.PID_3_patient_ID_internal,  HL7_23.CX_ID_number);
        if (aUR.length() == 6) {
            aZPIOut.set(CERNER_23.ZPI_15_Species, "S");
        } else {
            aZPIOut.set(CERNER_23.ZPI_15_Species, "U");
        }
        return aZPIOut;
    }

//------------------------------------------------------------------------------
    public HL7Segment processZEIFromUFD() throws ICANException {
        HL7Segment aZEIOut = new HL7Segment("ZEI");
        return aZEIOut;
    }

//------------------------------------------------------------------------------
    /**
     * CERNER specific processing for an Outgoing i.e "To CERNER" PV1 segment.
     * @return Returns the processed HL7 PV1 segment as a String.
     */
    public HL7Segment processPV1FromUFD() throws ICANException {
        return (this.processPV1FromUFD(mHL7Message));
    }
    public HL7Segment processPV1FromUFD(String pHL7MessageBlock) throws ICANException {
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aPV1SegIN = new HL7Segment(k.NULL);
        HL7Segment aPV1SegOUT = new HL7Segment("PV1");

        aPV1SegIN.setSegment(aHL7Message.getSegment(HL7_24.PV1));
        aPV1SegOUT.linkTo(aPV1SegIN);
// Alfred Centre code translation for Ward, Bed and Building
        CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
        CodeLookUp aLU_BED = new CodeLookUp("ALFCENTRE_BED.table", mEnvironment);
        //Current Location
        String aACWard = aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
        String aACBed = aACWard + "_" + aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
        String aACBuilding = "";
        aACWard = aLU_WARD.getValue(aACWard);
        aACBed = aLU_BED.getValue(aACBed);
        if (aACWard.length() > 0) {
            aACBuilding = "AC";
        }
        //Prior Location
        String aACWardPrior = aPV1SegIN.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
        String aACBedPrior = aACWardPrior + "_" + aPV1SegIN.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
        String aACBuildingPrior = "";
        aACWardPrior = aLU_WARD.getValue(aACWardPrior);
        aACBedPrior = aLU_BED.getValue(aACBedPrior);
        if (aACWardPrior.length() > 0) {
            aACBuildingPrior = "AC";
        }

// For A28 and A31 messages we dummy up a PV1 segment
        if (aHL7Message.isEvent("A28, A31")) {
            aPV1SegOUT.copy(HL7_23.PV1_1_set_ID);
            aPV1SegOUT.set(HL7_23.PV1_2_patient_class, "R");
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, mFacility);
            aPV1SegOUT.set(HL7_23.PV1_18_patient_type, "R");
            aPV1SegOUT.set(HL7_23.PV1_19_visit_number, "R" + mUR);

            if (aHL7Message.isEvent("A28")) {
                Format aDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date aDate = new Date();
                String aResult = aDateTimeFormat.format(aDate);
                aPV1SegOUT.set(HL7_23.PV1_44_admit_date_time, aResult);
                aPV1SegOUT.set(HL7_23.PV1_45_discharge_date_time, aResult);
                aPV1SegOUT.set(HL7_23.PV1_41_account_status, "D");
            }

            return aPV1SegOUT;
        }

// Initialze aPV1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PV1_1_set_ID,
            HL7_23.PV1_2_patient_class,
            HL7_23.PV1_4_admission_type,
            HL7_23.PV1_5_preadmit_num,
            HL7_23.PV1_10_hospital_service,
            HL7_23.PV1_11_temporary_location,
            HL7_23.PV1_12_pre_admit_test_indicator,
            HL7_23.PV1_13_re_admission_indicator,
            HL7_23.PV1_14_admit_source,
            HL7_23.PV1_17_admitting_doctor,
            HL7_23.PV1_18_patient_type,
            HL7_23.PV1_19_visit_number,
            HL7_23.PV1_20_financial_class,
            HL7_23.PV1_36_discharge_disposition,
            HL7_23.PV1_37_discharged_to_location,
            HL7_23.PV1_44_admit_date_time,
            HL7_23.PV1_45_discharge_date_time
        };
        aPV1SegOUT.copyFields(aCopyFields);

        if (aPV1SegOUT.get(HL7_23.PV1_45_discharge_date_time).length() > 0) {
            aPV1SegOUT.set(HL7_23.PV1_41_account_status, "D");
        }

//
// Patient Current Location .............
        if (aACWard.length() > 0) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aACWard);
        } else {
            aPV1SegOUT.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
        }

        if (! aPV1SegOUT.isEmpty(HL7_23.PV1_3_assigned_patient_location) ) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location,HL7_23.PL_room, "01");
        }

        String aBed;

        if (aACBed.length() > 0) {
            if (aACBed.length() == 1 && Character.isDigit(aACBed.charAt(0))) {
                NumberFormat formatter = new DecimalFormat("00");
                aACBed = formatter.format(Integer.parseInt(aACBed));
            }
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aACBed);
        } else {
            aBed = aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            if (aBed.length() == 1 && Character.isDigit(aBed.charAt(0))) {
                NumberFormat formatter = new DecimalFormat("00");
                aBed = formatter.format(Integer.parseInt(aBed));
            }
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aBed);
        }

        if (aPV1SegIN.hasValue(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "23")) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "SDMH");
        } else {
            aPV1SegOUT.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID);
        }
        //       aPV1SegOUT.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_status);
        if (aACBuilding.length() > 0) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_building, aACBuilding);
        }
//
// Patient Prior Location .................
        if (aACWardPrior.length() > 0) {
            aPV1SegOUT.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu, aACWardPrior);
        } else {
            aPV1SegOUT.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
        }

        if (! aPV1SegOUT.isEmpty(HL7_23.PV1_6_prior_patient_location) ) {
            aPV1SegOUT.set(HL7_23.PV1_6_prior_patient_location,HL7_23.PL_room, "01");

            if (aACBedPrior.length() > 0) {
                if (aACBedPrior.length() == 1 && Character.isDigit(aACBedPrior.charAt(0))) {
                    NumberFormat formatter = new DecimalFormat("00");
                    aACBedPrior = formatter.format(Integer.parseInt(aACBedPrior));
                }
                aPV1SegOUT.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed, aACBedPrior);
            } else {
                aBed = aPV1SegIN.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
                if (aBed.length() == 1 && Character.isDigit(aBed.charAt(0))) {
                    NumberFormat formatter = new DecimalFormat("00");
                    aBed = formatter.format(Integer.parseInt(aBed));
                }
                aPV1SegOUT.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed, aBed);
            }

            aPV1SegOUT.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_facility_ID);
            aPV1SegOUT.copy(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_status);
            if (aACBuildingPrior.length() > 0) {
                aPV1SegOUT.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_building, aACBuildingPrior);
            }
        }

        aPV1SegOUT = doDoctor(aPV1SegOUT, HL7_23.PV1_7_attending_doctor);
        aPV1SegOUT = doDoctor(aPV1SegOUT, HL7_23.PV1_8_referring_doctor);
        aPV1SegOUT = doDoctor(aPV1SegOUT, HL7_23.PV1_9_consulting_doctor);

        aPV1SegOUT.copy(HL7_23.PV1_14_admit_source);

        // CERNER Patient Type is same as Patient Class.
        aPV1SegOUT.move(HL7_23.PV1_18_patient_type, HL7_23.PV1_2_patient_class);
//        aPV1SegOUT.copy(HL7_23.PV1_50_alternate_visit_ID, HL7_23.CX_assigning_fac);

        return aPV1SegOUT;
    }


    private HL7Segment doDoctor(HL7Segment pSegment, String pFieldID) throws ICANException {
        // Do the processing required for a Doctor entry
        pSegment.copy(pFieldID, HL7_23.XCN_ID_num);

        if (! pSegment.isEmpty(pFieldID, HL7_23.XCN_ID_num)) {
            pSegment.copy(pFieldID, HL7_23.XCN_first_name);
            pSegment.copy(pFieldID, HL7_23.XCN_last_name);
            pSegment.copy(pFieldID, HL7_23.XCN_middle_initial_or_name);
            pSegment.copy(pFieldID, HL7_23.XCN_prefix);

        }
//        pSegment.copy(pFieldID, HL7_23.XCN_degree);
        return pSegment;

    }

//------------------------------------------------------------------------------
    /**
     * Generic processing for an Outgoing i.e "To Vendor" PV2 segment.
     * @return Returns the processed HL7 PV2 segment as a String.
     */
    public HL7Segment processPV2FromUFD() throws ICANException {
        return (this.processPV2FromUFD(mHL7Message));
    }
    public HL7Segment processPV2FromUFD(String pHL7MessageBlock) throws ICANException {
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aPV2SegIN = new HL7Segment(k.NULL);
        HL7Segment aPV2SegOUT = new HL7Segment(k.NULL);
        aPV2SegIN.setSegment(aHL7Message.getSegment(HL7_24.PV2));

        if (aPV2SegIN.getSegment().length() < 3 ) {
            return aPV2SegOUT;
        }
        aPV2SegOUT = new HL7Segment("PV2");

        // Initialze aPV2SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PV2_8_expected_admit_date,
            HL7_23.PV2_9_expected_discharge_date,
        };
        aPV2SegOUT.linkTo(aPV2SegIN);
        aPV2SegOUT.copyFields(aCopyFields);

        String aTrauma = getFromZBX("PMI", "TRAUMA");
        if (aTrauma.indexOf("(MW)") >= 0) {
            aPV2SegOUT.set(HL7_23.PV2_3_admit_reason, HL7_23.CE_text, "(MW) ".concat(aPV2SegIN.get(HL7_23.PV2_3_admit_reason, HL7_23.CE_text )));
        } else if (aTrauma.indexOf("(TT)") >= 0) {
            aPV2SegOUT.set(HL7_23.PV2_3_admit_reason, HL7_23.CE_text, "(TT) ".concat(aPV2SegIN.get(HL7_23.PV2_3_admit_reason, HL7_23.CE_text )));
        } else {
            aPV2SegOUT.move(HL7_23.PV2_3_admit_reason, HL7_23.CE_text, HL7_23.PV2_3_admit_reason, HL7_23.CE_text);
        }
        aPV2SegOUT.move(HL7_23.PV2_22_visit_protection_indic, HL7_23.PV2_21_visit_publicity_code);
        return aPV2SegOUT;
    }

//------------------------------------------------------------------------------
    public HL7Segment processZVIFromUFD() throws ICANException {
        HL7Segment aZVIOut = new HL7Segment("ZVI");
        return aZVIOut;
    }

//------------------------------------------------------------------------------
    public HL7Group processZBXSegmentsFromUFD() throws ICANException {
        HL7Group aOBXOutGroup = new HL7Group();
        HL7Segment aOBXOut = new HL7Segment("OBX");
        String aOBXValue;
        String aMedRecDateTime;

        aOBXValue = getFromZBX("FINANCE", "CONTEXT");
        if (aOBXValue.equals("A")) {
            aOBXValue = getFromZBX("FINANCE", "CLAIM_NUMBER");
            aOBXOutGroup.append( processOBXFromZBX("TACWRKVR", "NM", aOBXValue).getSegment());
        }

        aOBXValue = getFromZBX("PMI", "OCCUPATION");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXOutGroup.append( processOBXFromZBX("OCCUPAT", "ST", aOBXValue).getSegment());
        }

        aOBXValue = getFromZBX("PMI", "MEDICARE_EXPIRY");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXValue = aOBXValue.substring(4, 6) + "/" + aOBXValue.substring(0, 4);
            aOBXOutGroup.append( processOBXFromZBX("MEDEXPDT", "ST", aOBXValue).getSegment());
        }

        aOBXValue = getFromZBX("PMI", "PENSION_EXPIRY_DATE");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXOutGroup.append( processOBXFromZBX("HCCPENSEXP", "DT", aOBXValue).getSegment());
        }

        aOBXValue = getFromZBX("PMI", "PBS_SAFETYNET_NUMBER");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXOutGroup.append( processOBXFromZBX("PBSSAFETYNET", "ST", aOBXValue).getSegment());
        }

        aOBXValue = getFromZBX("PMI", "INTERPRETER");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXOutGroup.append( processOBXFromZBX("INTERPREQ", "CD", aOBXValue).getSegment());
        }

        aOBXValue = getFromZBX("PMI", "DOB_ACCURACY");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXOutGroup.append( processOBXFromZBX("DOB_ACCURACY", "CD", aOBXValue).getSegment());
        }


        // Medical Record Movements ...
        aMedRecDateTime = getFromZBX("MEDREC", "LAST_MOVE_DATE_TIME");
        if ( aMedRecDateTime.length() > 5) {

            aOBXValue = aMedRecDateTime;
            aOBXOutGroup.append( processOBXFromZBX("MRLSTMOVDTTM", "DT",  aOBXValue).getSegment());

            aOBXValue = getFromZBX("MEDREC", "VOLUME_NUMBER");
            aOBXOutGroup.append( processOBXFromZBX("MRVOLNBR", "NM",  aOBXValue).getSegment());

            aOBXValue = getFromZBX("MEDREC", "LOCATION");
            aOBXOutGroup.append( processOBXFromZBX("MRLOCN", "ST",  aOBXValue).getSegment());

            aOBXValue = getFromZBX("MEDREC", "RECEIVED_BY");
            aOBXOutGroup.append( processOBXFromZBX("MRRECDBY", "ST",  aOBXValue).getSegment());

            aOBXValue = getFromZBX("MEDREC", "EXTENSION");
            aOBXOutGroup.append( processOBXFromZBX("MRRECVREXTN", "ST",  aOBXValue).getSegment());
        }

        aOBXValue = getFromZBX("PMI", "DVA_CARD_TYPE");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXOutGroup.append( processOBXFromZBX("DVACARDTYP", "CD", aOBXValue).getSegment());
        }

        aOBXValue = getFromZBX("VISIT", "ED_CONNECT_PATIENT");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXOutGroup.append( processOBXFromZBX("EDCONNECT", "CD", aOBXValue).getSegment());
        }

        return aOBXOutGroup;

    }

//------------------------------------------------------------------------------
    /** Move contents of ZBX stored data identified by pGroupID+ pItemID into an OBX segment with Segment_ID pOBXNum  */
    public HL7Segment processOBXFromZBX(String pOBXidentifier, String pOBXtype, String pOBXvalue) throws ICANException {
        HL7Segment aOBXSegment = new HL7Segment("OBX");
        aOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(++mOBXSegmentCount));
        aOBXSegment.set(HL7_23.OBX_2_value_type, pOBXtype );
        aOBXSegment.set(HL7_23.OBX_3_observation_identifier, pOBXidentifier );
        aOBXSegment.set(HL7_23.OBX_5_observation_value, pOBXvalue );

        return aOBXSegment;
    }
//
//------------------------------------------------------------------------------
// Process PID segment specific to the ORU and ORM message types
//
    public HL7Segment processORM_ORU_PIDFromUFD() {

        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");


//
// Locate and copy the Patient UR.
        int aPID3FieldCount = aInPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3FieldCount; i++) {
            String aPatientIDTypeCode = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i);
            if (aPatientIDTypeCode.equalsIgnoreCase("PI")) {
                String aPIDIDNum = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                int aPIDIDNumLength = aPIDIDNum.length();
                if (aPIDIDNumLength < 8) {
                    aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                } else {
                    aPIDIDNum = aPIDIDNum.substring(aPIDIDNumLength - 7,  aPIDIDNumLength);
                    aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPIDIDNum);
                }
                aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, i);
                mUR = aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number);
            }
        }
//
// We are only interested in the 1st instance of the name ....
        String aPID5PatientName = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        if (aPID5PatientName.startsWith("ZZZTEST-")) {
            int aSLen = aPID5PatientName.length();
            aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, aPID5PatientName.substring(8, aSLen), 1);
        } else {
            aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        }
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
        aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, "L", 1);
        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_geographic_designation, 1);
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_18_account_number);

        return aOutPIDSegment;
    }
//
//------------------------------------------------------------------------------
// Process NTE segments
//
    public HL7Group processNTEs_FromUFD() {
        HL7Group aOutPD1NTEGroup = new HL7Group("");
        HL7Segment aOutPD1NTESegment = new HL7Segment("");
        HL7Segment aInPD1NTESegment = new HL7Segment("");
        HL7Group aPD1NTEGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_PD1Notes, 1));
        int aPD1NTESegmentCount = aPD1NTEGroup.countSegments("NTE");

        for (int i = 1; i <= aPD1NTESegmentCount; i++) {
            aInPD1NTESegment = new HL7Segment(aPD1NTEGroup.getSegment("NTE", i));
            aOutPD1NTESegment = new HL7Segment("NTE");
            aOutPD1NTESegment.linkTo(aInPD1NTESegment);
            aOutPD1NTESegment.copy(HL7_23.NTE_1_setID);
            aOutPD1NTESegment.copy(HL7_23.NTE_2_source_of_comment);
            aOutPD1NTESegment.copy(HL7_23.NTE_3_comment);
            aOutPD1NTEGroup.append(aOutPD1NTESegment);
        }
        return aOutPD1NTEGroup;
    }
//
//------------------------------------------------------------------------------
// Process PV1 segment specific to the ORU and ORM message types
//
    public HL7Segment processORM_ORU_PV1FromUFD() {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);

            //Alfred Centre ward and bed code lookup
            String aACWard = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            String aACBed = aACWard + "_" + aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
            CodeLookUp aLU_BED = new CodeLookUp("ALFCENTRE_BED.table", mEnvironment);

            aACWard = aLU_WARD.getValue(aACWard);
            aACBed = aLU_BED.getValue(aACBed);

            String aPatientClass = aInPV1Segment.get(HL7_23.PV1_2_patient_class);

            // Nuclear Med has PV1 Encounter types of "R" which differ from those from PARIS by the contents of the Patient Type.
            if (aPatientClass.equalsIgnoreCase("R") && aInPV1Segment.hasValue(HL7_23.PV1_18_patient_type, "R")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "NUCM");
            } else {
                if (aACWard.length() > 0) {
                    aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aACWard);
                    aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aACBed);
                    aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_building, "AC");
                } else {
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                }
            }
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "01");
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.copy(HL7_23.PV1_18_patient_type);
            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);

            if (mHospitalPrefix.equalsIgnoreCase("A")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "ALF");
                if (aPatientClass.equalsIgnoreCase("I")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CSC");
                } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E") || aPatientClass.equalsIgnoreCase("PO")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CERNER");
                } else {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-TRIAL");
                }
            } else if (mHospitalPrefix.equalsIgnoreCase("S")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "SDMH");
                if (aPatientClass.equalsIgnoreCase("I")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CSC");
                } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CERNER");
                } else {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-TRIAL");
                }
            } else if (mHospitalPrefix.equalsIgnoreCase("C")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "CGMC");
                if (aPatientClass.equalsIgnoreCase("I")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-CSC");
                } else if (aPatientClass.equalsIgnoreCase("O") || aPatientClass.equalsIgnoreCase("E")) {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-CERNER");
                } else {
                    aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-TRIAL");
                }
            }
            aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
            aOutPV1Segment.copy(HL7_23.PV1_45_discharge_date_time);
            //if discharge date is not empty, fill in D (discharge) account status
            if (aOutPV1Segment.get(HL7_23.PV1_45_discharge_date_time).length() > 0) {
                aOutPV1Segment.set(HL7_23.PV1_41_account_status, "D");
            }

        }
        return aOutPV1Segment;
    }
//
//------------------------------------------------------------------------------
// Process Request Details (i.e ORC, ORM, NTE's and OBX's) for ORU (Result/Report) messages
//
    public HL7Group processORU_R01_FromUFD(HL7Segment pPIDSegment) {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Group aOutOBRNTEGroup = new HL7Group("");
        HL7Group aOutOBXNTEGroup = new HL7Group("");
        HL7Segment aOutOBXSegment = new HL7Segment("");
        String aSReg1 = "";
        String aSReg2 = "";
        String aSReg3 = "";
        CodeLookUp aLU = new CodeLookUp("PACS_URL.table", mEnvironment);

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            HL7Group aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

//process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            if (aInORCSegment.getSegment().length() > 0) {
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                aOutORCSegment.copy(HL7_23.ORC_1_order_control);
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
                HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_5_order_status);
                aOutORCSegment.copy(HL7_23.ORC_9_date_time_of_trans);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_13_enterers_location);
                aOutReqDetsGroup.append(aOutORCSegment);
            }

//process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID);
            aOutOBRSegment.copy(HL7_23.OBR_7_Observation_Date_Time);

            int aCollectorIDCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_10_collector_ID);
            for (int j = 1; j <= aCollectorIDCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_ID_num, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_last_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_first_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_middle_initial_or_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_suffix, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_prefix, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_degree, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_code_source_table, j);
            }
            aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_text);
            aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);
            aOutOBRSegment.copy(HL7_23.OBR_14_Specimen_Received_Date_Time);
            String aSpecimenCode = aInOBRSegment.get(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code);
            String aSpecimenText = aInOBRSegment.get(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text);
            String aCollectionMethod = aInOBRSegment.get(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method);
            aOutOBRSegment.set(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code, aSpecimenCode);
            aOutOBRSegment.set(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text, aSpecimenText);
            aOutOBRSegment.set(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method, aCollectionMethod);

            int aOrderingProviderCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
            for (int j = 1; j <= aOrderingProviderCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_suffix, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_prefix, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_degree, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_code_source_table, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_19_Placers_Field_2);
            aOutOBRSegment.copy(HL7_23.OBR_20_Fillers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_21_Fillers_Field_2);
            aOutOBRSegment.copy(HL7_23.OBR_22_Results_RPT_Status_Change);

            if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MA")) {
                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MBO");
            } else if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RA")) {
                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
            } else {
                aOutOBRSegment.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            }

            aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                //aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_29_Parent);
            aOutOBRSegment.copy(HL7_23.OBR_30_Transportation_Mode);

            int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
            for (int j = 1; j <= aReasonForStudyCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_32_Principal_Result_Interpreter);

            int aTechnicianCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_34_technician);
            for (int j = 1; j <= aTechnicianCount; j++) {
                String aTechnician = aInOBRSegment.get(HL7_23.OBR_34_technician);
                aOutOBRSegment.set(HL7_23.OBR_34_technician, aTechnician, j);
            }

            aOutReqDetsGroup.append(aOutOBRSegment);

//
//START ======================== process OBX and OBXNTEs
            HL7Group aOutOBXGroup = new HL7Group("");
            HL7Segment aInSegment = new HL7Segment("");
            int aGroupSegmentCount = aInReqDetsGroup.countSegments();
            boolean aSkipSegment = false;
            int aInsertBlankCount = 0;
            int aOBXSegID = 1;
            int k = 0;
            while (k <= aGroupSegmentCount || aInsertBlankCount != 0) {
                if (aInsertBlankCount == 0) {    // Don't need the next segment yet we will insert a blank one instead.
                    aInSegment = new HL7Segment(aInReqDetsGroup.getNextSegment());
                    k++;
                }

                if (aSkipSegment == true && aInsertBlankCount == 0) {     // The next segment is to be discarded
                    aSkipSegment = false;
                } else {
//                    display("++++" + aInSegment.getSegmentID());
                    if (aInSegment.getSegmentID().equalsIgnoreCase("ORC") ||
                            aInSegment.getSegmentID().equalsIgnoreCase("OBR") ) {
                        // Skip OBR and ORC ... already dealt with.

                    } else if (aInSegment.getSegmentID().equalsIgnoreCase("OBX")) {

                        aOutOBXSegment = new HL7Segment("OBX");
                        aOutOBXSegment.linkTo(aInSegment);
                        aOutOBXSegment.set(HL7_23.OBX_1_set_ID, String.valueOf(aOBXSegID++));
                        aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
                        aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code);
                        aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text);
                        aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme);
                        aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID);
                        aOutOBXSegment.copy(HL7_23.OBX_4_observation_sub_ID);


                        // Note:- Following uses "$ $" to insert a space because the set command trims spaces.
                        // ... further down, when we append segment, we then replace "$ $" by a single space char.
                        int aOBX5FieldCount = aInSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                        for (int m = 1; m <= aOBX5FieldCount; m++) {
                            String aObsValue = aInSegment.get(HL7_23.OBX_5_observation_value, m);

                            if (aObsValue.equalsIgnoreCase(".")) {  // If value only contains a "." then insert a blank line
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "$ $", m);

                            } else if (aInsertBlankCount > 0) {      // Force a blank line
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "$ $", m);
                                aInsertBlankCount--;

                            } else {                                // Process the normal conents of the OBX value
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, aObsValue, m);

                                // Check if this is the last required line of a Radiology report section.
                                if (aObsValue.indexOf("Reported by:") >= 0 && aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"RA"))  {
                                    aSkipSegment = true;     // Skip the line after "Reported By" ....
                                    aInsertBlankCount = 2;     // ... and insert two blank lines.
                                }
                            }
                        }

                        String aOBX6Unit = aInSegment.get(HL7_23.OBX_6_units);
                        aOBX6Unit = replaceInStr(aOBX6Unit, "^", "\\S\\");
                        aOutOBXSegment.set(HL7_23.OBX_6_units, aOBX6Unit);
                        aOutOBXSegment.copy(HL7_23.OBX_7_references_range);

                        int aOBX8FieldCount = aInSegment.countRepeatFields(HL7_23.OBX_8_abnormal_flags);
                        int n = 1;
                        do {
                            String aAbnormalFlag = aInSegment.get(HL7_23.OBX_8_abnormal_flags, n);
                            if (aAbnormalFlag.length() > 0) {
                                aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                            } else {
                                String aObservResultsStatus = aInSegment.get(HL7_23.OBX_11_observ_results_status, n);
                                if (aObservResultsStatus.equalsIgnoreCase("C")) {
                                    aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, "N", n);
                                } else {
                                    aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                                }
                            }
                            n++;
                        } while (n <= aOBX8FieldCount);

                        aOutOBXSegment.copy(HL7_23.OBX_11_observ_results_status);
                        aOutOBXSegment.copy(HL7_23.OBX_12_date_last_obs_normal_values);
                        aOutOBXSegment.copy(HL7_23.OBX_14_date_time_of_the_observation);
                        aOutOBXSegment.copy(HL7_23.OBX_15_producers_ID, HL7_23.CE_ID_code);
                        aOutOBXSegment.copy(HL7_23.OBX_16_responsible_observer);

                        aOutOBXGroup.append(cleanoutStr(replaceInStr(aOutOBXSegment.getSegment(), "$ $", " ")));

//process OBX NTEs
                    } else if (aInSegment.getSegmentID().equalsIgnoreCase("NTE")) {
                        HL7Segment aOutOBXNTESegment = new HL7Segment("NTE");
                        aOutOBXNTESegment.linkTo(aInSegment);
                        aOutOBXNTESegment.copy(HL7_23.NTE_1_setID);
//                       aOutOBXNTESegment.copy(HL7_23.NTE_2_source_of_comment);
                        aOutOBXNTESegment.copy(HL7_23.NTE_3_comment);
                        aOutOBXGroup.append(cleanoutStr(aOutOBXNTESegment.getSegment()));
                    }
                }
            }
//END ======================== process OBX and OBXNTEs

// process URL pointer if report carries an image
            if (aInOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID).equalsIgnoreCase("RA")) {
                HL7Segment aImageOBXSegment = new HL7Segment("OBX");
                aImageOBXSegment.set(HL7_23.OBX_1_set_ID, String.valueOf(aOBXSegID++));
                aImageOBXSegment.set(HL7_23.OBX_2_value_type, "RP");

                String aUniversalServiceID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                aImageOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aUniversalServiceID);

                String aUniversalServiceIDText = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
                aImageOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text, aUniversalServiceIDText);

                String aUniversalServiceIDCodeScheme = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme);
                aImageOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme, aUniversalServiceIDCodeScheme);

                String aUniversalAlternateID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID);
                aImageOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID, aUniversalAlternateID);
                aImageOBXSegment.set(HL7_23.OBX_4_observation_sub_ID, "1");
                aSReg2 = aLU.getValue("URL");
                if (aSReg2.indexOf("pat_id") >= 0) {
                    aSReg3 = aSReg2.concat(pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number,  1)).concat(mSReg4);
                } else {
                    aSReg3 = aSReg2.concat(aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2, 12));
                }
                aImageOBXSegment.set(HL7_23.OBX_5_observation_value, aSReg3.concat("^IMAGEURL^IMAGE"), 1);
                aOutOBXGroup.append(aImageOBXSegment);
            }
            aOutReqDetsGroup.append(aOutOBXGroup);
        }
        return aOutReqDetsGroup;
    }

//
// Method to do a sub-string replacement.
// Note:- Replaces str.replaceAll ... which was not happy replacing "^" by "\\S\\"
    public String replaceInStr(String pSrc, String pFrom, String pTo) {
        int aPos;

        aPos = pSrc.indexOf(pFrom);
        if (aPos >= 0) {
            pSrc = pSrc.substring(0, aPos) + pTo + pSrc.substring(aPos + pFrom.length());
        }
        return pSrc;

    }
// Method to remove any data between "{" and "}"
// Note:- Replaces str.replaceAll ... which was not happy replacing "^" by "\\S\\"
    public String cleanoutStr(String pSrc) {
        int aPos1;
        int aPos2;

        aPos1 = pSrc.indexOf("{");
        aPos2 = pSrc.indexOf("}");
        while (aPos1 >= 0 && aPos2 >= 0) {
            pSrc = pSrc.substring(0, aPos1) + pSrc.substring(aPos2+1);
            aPos1 = pSrc.indexOf("{");
            aPos2 = pSrc.indexOf("}");
        }

        return pSrc;

    }
//
//------------------------------------------------------------------------------
// Create a FILMBAG specific MSH
//
    public HL7Segment processFILMBAGMSHFromUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        mHospitalPrefix = aInMSHSegment.get(HL7_23.MSH_4_sending_facility).substring(0, 1);
        CodeLookUp aLU = new CodeLookUp("PACS_URL.table", mEnvironment);
        mSReg2 = aLU.getValue("URL");
        aOutMSHSegment.linkTo(aInMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        if (mHospitalPrefix.equalsIgnoreCase("A")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-ALF");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "ALF");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "ALF");
            mSReg4 = "";
        } else if (mHospitalPrefix.equalsIgnoreCase("C")) {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-CGMC");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "CGMC");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "CGMC");
            mSReg4 = "-CGMC";
        } else {
            aOutMSHSegment.set(HL7_23.MSH_3_sending_application, "PARIS-SDMH");
            aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "SDMH");
            aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "SDMH");
            mSReg4 = "-SDMH";
        }
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "CERNERPM");
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        //aOutMSHSegment.copy(HL7_23.MSH_9_message_type);
        aOutMSHSegment.set(HL7_23.MSH_9_1_message_type, "ORU");
        aOutMSHSegment.set(HL7_23.MSH_9_2_trigger_event, "R01");
        String aMessageCtrlID = "FB-".concat(aInMSHSegment.get(HL7_23.MSH_10_message_control_ID));
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aMessageCtrlID);
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");

        return aOutMSHSegment;
    }
//
//------------------------------------------------------------------------------
// Create a FILMBAG specific PID
//
    public HL7Segment processFILMBAGPID_FromUFD() {
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");
        if (mHospitalPrefix.equalsIgnoreCase("A")) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "ALF", 1);
        } else if (mHospitalPrefix.equalsIgnoreCase("C")) {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "CGMC", 1);
        } else {
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "SDMH", 1);
        }
        int aPID3FieldCount = aInPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3FieldCount; i++) {
            String aPatientIDTypeCode = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i);
            if (aPatientIDTypeCode.equalsIgnoreCase("MR") || aPatientIDTypeCode.equalsIgnoreCase("PI") ||
                    aPatientIDTypeCode.length() == 0) {
                String aPIDIDNum = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                int aPIDIDNumLength = aPIDIDNum.length();
                if (aPIDIDNumLength < 8) {
                    aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
                } else {
                    aPIDIDNum = aPIDIDNum.substring(aPIDIDNumLength - 7,  aPIDIDNumLength);
                    aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPIDIDNum, 1);
                }
            }
        }
        String aPID5PatientName = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        if (aPID5PatientName.startsWith("ZZZTEST-")) {
            int aSLen = aPID5PatientName.length();
            aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, aPID5PatientName.substring(8, aSLen), 1);
        } else {
            aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        }
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
        aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, "L", 1);
        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_geographic_designation, 1);
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_18_account_number, HL7_23.CX_ID_number);

        return aOutPIDSegment;
    }

//------------------------------------------------------------------------------
    public HL7Segment processFILMBAGPV1_FromUFD(HL7Segment pPIDSegment) {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));

        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);

            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.set(HL7_23.PV1_2_patient_class, "O");

            if (mHospitalPrefix.equalsIgnoreCase("A")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "ALF");
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "ALF-CERNER");
            } else if (mHospitalPrefix.equalsIgnoreCase("C")) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "CGMC");
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "CGMC-CERNER");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "SDMH");
                aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SDMH-CERNER");
            }
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "IMAGE");
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "01");
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.set(HL7_23.PV1_18_patient_type, "P");
            String aVisitNum = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1).concat("-FILMBAG");
            aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number, aVisitNum);

            Format aDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date aDate = new Date();
            String aResult = aDateTimeFormat.format(aDate);
            aOutPV1Segment.set(HL7_23.PV1_44_admit_date_time, aResult);
            aOutPV1Segment.set(HL7_23.PV1_45_discharge_date_time, aResult);
            aOutPV1Segment.set(HL7_23.PV1_41_account_status, "D");
        }
        return aOutPV1Segment;
    }

//------------------------------------------------------------------------------
    public HL7Group processFILMBAGReqDets_FromUFD(HL7Segment pPIDSegment, HL7Segment pMSHSegment) {

        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Group aOutOBRNTEGroup = new HL7Group("");
        HL7Group aOutOBXNTEGroup = new HL7Group("");
        HL7Segment aOutOBXSegment = new HL7Segment("");
        String aFillerOrderNum = "";
        String aSReg1 = "";
        String aSReg2 = "";
        String aSReg3 = "";
        CodeLookUp aLU = new CodeLookUp("PACS_URL.table", mEnvironment);

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            if (aInORCSegment.getSegment().length() > 0) {
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                aOutORCSegment.set(HL7_23.ORC_1_order_control, "RE");
                aFillerOrderNum = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1).concat("_FILMBAG");
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID, aFillerOrderNum);
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID, "RA");
                aOutORCSegment.set(HL7_23.ORC_5_order_status, "IP");
                aOutORCSegment.copy(HL7_23.ORC_9_date_time_of_trans);
                aOutReqDetsGroup.append(aOutORCSegment);
            }

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
            aFillerOrderNum = pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1).concat("_FILMBAG");
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID, aFillerOrderNum);
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID, "RA");
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, "FILMBAGD");
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text, "FILMBAGD");
//            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, "FILMBAG");
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, "All Images Filmbag");
            aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID, "PARIS");
            aOutOBRSegment.copy(HL7_23.OBR_7_Observation_Date_Time);
            aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID);

            aOutOBRSegment.copy(HL7_23.OBR_14_Specimen_Received_Date_Time);
            aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, 1);
            aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);

            if (aInOBRSegment.isEmpty(HL7_23.OBR_20_Fillers_Field_1)) {
                String aFillersField1 = aInOBRSegment.get(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID).concat("_").concat(aInOBRSegment.get(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID).concat("_FILMBAG"));
                aOutOBRSegment.set(HL7_23.OBR_20_Fillers_Field_1, aFillersField1);
            } else {
                aOutOBRSegment.copy(HL7_23.OBR_20_Fillers_Field_1);
            }
            aOutOBRSegment.set(HL7_23.OBR_21_Fillers_Field_2, aFillerOrderNum);
            aOutOBRSegment.set(HL7_23.OBR_22_Results_RPT_Status_Change, pMSHSegment.get(HL7_23.MSH_7_message_date_time));
            aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
            aOutOBRSegment.set(HL7_23.OBR_25_Results_Status, "P");

            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                //aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
            }
            aOutOBRSegment.copy(HL7_23.OBR_29_Parent);
            aOutOBRSegment.copy(HL7_23.OBR_30_Transportation_Mode);

            aOutReqDetsGroup.append(aOutOBRSegment);

            //process IMAGE URL insertion
            HL7Segment aImageOBXSegment1 = new HL7Segment("OBX");
            aImageOBXSegment1.set(HL7_23.OBX_1_set_ID, "1");
            aImageOBXSegment1.set(HL7_23.OBX_2_value_type, "TX");
            String aUniversalServiceID = aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID);
            aImageOBXSegment1.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aUniversalServiceID);
            String aUniversalServiceIDText = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            aSReg2 = aLU.getValue("REPORT");
            aImageOBXSegment1.set(HL7_23.OBX_5_observation_value, aSReg2, 1);

            aOutReqDetsGroup.append(aImageOBXSegment1);

            HL7Segment aImageOBXSegment2 = new HL7Segment("OBX");
            aImageOBXSegment2.set(HL7_23.OBX_1_set_ID, "2");
            aImageOBXSegment2.set(HL7_23.OBX_2_value_type, "RP");
            aImageOBXSegment2.set(HL7_23.OBX_4_observation_sub_ID, "1");
            aSReg2 = aLU.getValue("FILMBAG");
            if (aSReg2.indexOf("pat_id") >= 0) {
                aSReg3 = aSReg2.concat(pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number,  1)).concat(mSReg4);
            } else {
                aSReg3 = aSReg2.concat(aOutOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2, 12));
            }
            aImageOBXSegment2.set(HL7_23.OBX_5_observation_value, aSReg3.concat("^IMAGEURL^IMAGE"), 1);
            aImageOBXSegment2.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID));

            aOutReqDetsGroup.append(aImageOBXSegment2);
        }
        return aOutReqDetsGroup;
    }

//------------------------------------------------------------------------------
    public HL7Group processO01ReqDets_FromUFD(HL7Segment pPIDSegment) {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Group aOutOBRNTEGroup = new HL7Group("");
        HL7Group aOutOBXNTEGroup = new HL7Group("");
        HL7Segment aOutOBXSegment = new HL7Segment("");
        //String aSReg1 = "";
        //String aSReg2 = "";
        //String aSReg3 = "";

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            if (aInORCSegment.getSegment().length() > 0) {
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                String aPlacerOrderNum = aInORCSegment.get(HL7_23.ORC_2_placer_order_num);
                String aOrderControl = aInORCSegment.get(HL7_23.ORC_1_order_control);
                if (aPlacerOrderNum.length() == 0) {
                    if (aOrderControl.equalsIgnoreCase("NW")) {
                        aOutORCSegment.set(HL7_23.ORC_1_order_control, "SN");
                    } else {
                        aOutORCSegment.copy(HL7_23.ORC_1_order_control);
                    }
                } else {
                    aOutORCSegment.copy(HL7_23.ORC_1_order_control);
                }
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_5_order_status);
                aOutORCSegment.copy(HL7_23.ORC_9_date_time_of_trans);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_13_enterers_location);
                aOutORCSegment.copy(HL7_23.ORC_16_order_control_code_reason);
                aOutReqDetsGroup.append(aOutORCSegment);
            }

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID);

            CodeLookUp aLU = new CodeLookUp("PARIS_ORM_Codes.table", mEnvironment);
            mSReg3 = aLU.getValue(aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code));
            if (mSReg3.length() == 0) {
                aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID);
            } else {
                aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, mSReg3);
            }
            aOutOBRSegment.copy(HL7_23.OBR_7_Observation_Date_Time);

            int aCollectorIDCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_10_collector_ID);
            for (int j = 1; j <= aCollectorIDCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_ID_num, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_last_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_first_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_middle_initial_or_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_suffix, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_prefix, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_degree, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_code_source_table, j);
            }
            aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_text);
            aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);
            aOutOBRSegment.copy(HL7_23.OBR_14_Specimen_Received_Date_Time);

            if (aInOBRSegment.hasValue(HL7_23.OBR_3_Filler_Order_Number,HL7_23.EI_2_namespace_ID, "MA")) {
                aOutOBRSegment.move(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method,
                        HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code);
            } else {
                aOutOBRSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code);
                aOutOBRSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text);
                aOutOBRSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method);
            }
            int aOrderingProviderCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
            for (int j = 1; j <= aOrderingProviderCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_suffix, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_prefix, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_degree, j);
                aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_code_source_table, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_19_Placers_Field_2);
            aOutOBRSegment.copy(HL7_23.OBR_20_Fillers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_21_Fillers_Field_2);
            aOutOBRSegment.copy(HL7_23.OBR_22_Results_RPT_Status_Change);
            aOutOBRSegment.copy(HL7_23.OBR_23_Charge_To_Practice);

            if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MA")) {
                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MBO");
            } else if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RA")) {
                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
            } else {
                aOutOBRSegment.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            }
            aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_29_Parent);
            if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RA") &&
                    aInORCSegment.hasValue(HL7_23.ORC_1_order_control, "NW") &&
                    aInOBRSegment.isEmpty(HL7_23.OBR_30_Transportation_Mode)) {
                aOutOBRSegment.set(HL7_23.OBR_30_Transportation_Mode, "WK");

            } else {
                aOutOBRSegment.copy(HL7_23.OBR_30_Transportation_Mode);
            }

            int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
            for (int j = 1; j <= aReasonForStudyCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_32_Principal_Result_Interpreter);

            int aTechnicianCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_34_technician);
            for (int j = 1; j <= aTechnicianCount; j++) {
                String aTechnician = aInOBRSegment.get(HL7_23.OBR_34_technician);
                aOutOBRSegment.set(HL7_23.OBR_34_technician, aTechnician, j);
            }

            aOutReqDetsGroup.append(aOutOBRSegment);

            //process OBR NTEs
            aOutOBRNTEGroup = new HL7Group("");
            HL7Group aInOBRNTEGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_OBRNotes, i));
            int aOBRNTESegmentCount = aInOBRNTEGroup.countSegments(HL7_23.NTE);
            for (int j = 1; j <= aOBRNTESegmentCount; j++) {
                HL7Segment aInOBRNTESegment = new HL7Segment(aInOBRNTEGroup.getSegment("NTE", j));
                HL7Segment aOutOBRNTESegment = new HL7Segment("NTE");
                aOutOBRNTESegment.linkTo(aInOBRNTESegment);
                aOutOBRNTESegment.copy(HL7_23.NTE_1_setID);
                aOutOBRNTESegment.copy(HL7_23.NTE_2_source_of_comment);
                aOutOBRNTESegment.copy(HL7_23.NTE_3_comment);
                aOutOBRNTEGroup.append(aOutOBRNTESegment);
            }
            aOutReqDetsGroup.append(aOutOBRNTEGroup);

            //process OBX and OBXNTEs
            HL7Group aOutOBXGroup = new HL7Group("");
            int aOBXSegmentID = 1;
            int aOBXSegmentCount = aInReqDetsGroup.countSegments(HL7_23.OBX);
            for (int k = 1; k <= aOBXSegmentCount; k++) {
                HL7Segment aInSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBX", k));
                aOutOBXSegment = new HL7Segment("OBX");
                aOutOBXSegment.linkTo(aInSegment);
                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(aOBXSegmentID++));
                aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
                aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier);
                aOutOBXSegment.copy(HL7_23.OBX_4_observation_sub_ID);

                int aOBX5FieldCount = aInSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                for (int m = 1; m <= aOBX5FieldCount; m++) {
                    aOutOBXSegment.copy(HL7_23.OBX_5_observation_value, m);
                }
                String aOBX6Unit = aInSegment.get(HL7_23.OBX_6_units);
                aOBX6Unit = replaceInStr(aOBX6Unit, "^", "\\S\\");

                aOutOBXSegment.set(HL7_23.OBX_6_units, aOBX6Unit);
                aOutOBXSegment.copy(HL7_23.OBX_7_references_range);

                int aOBX8FieldCount = aInSegment.countRepeatFields(HL7_23.OBX_8_abnormal_flags);
                for (int n = 1; n <= aOBX8FieldCount; n++) {
                    String aAbnormalFlag = aInSegment.get(HL7_23.OBX_8_abnormal_flags);
                    aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                }

                aOutOBXSegment.copy(HL7_23.OBX_11_observ_results_status);
                aOutOBXSegment.copy(HL7_23.OBX_14_date_time_of_the_observation);
                aOutOBXSegment.copy(HL7_23.OBX_15_producers_ID, HL7_23.CE_ID_code);
                aOutOBXSegment.copy(HL7_23.OBX_16_responsible_observer);

                aOutOBXGroup.append(aOutOBXSegment);

                //process OBX NTEs
                aOutOBXNTEGroup = new HL7Group("");
                HL7Group aInOBXNTEGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Observation_Details, i));
                int aOBXNTESegmentCount = aInOBXNTEGroup.countSegments(HL7_23.NTE);
                for (int j = 1; j <= aOBXNTESegmentCount; j++) {
                    HL7Segment aInOBXNTESegment = new HL7Segment(aInOBXNTEGroup.getSegment("NTE", j));
                    HL7Segment aOutOBXNTESegment = new HL7Segment("NTE");
                    aOutOBXNTESegment.linkTo(aInOBXNTESegment);
                    aOutOBXNTESegment.copy(HL7_23.NTE_1_setID);
                    aOutOBXNTESegment.copy(HL7_23.NTE_3_comment);
                    aOutOBXNTEGroup.append(aOutOBXNTESegment);
                }
                aOutOBXGroup.append(aOutOBXNTEGroup);
            }
            //process URL pointer if report carries an image
            String aDiagServiceSecID = aOutOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            if (aDiagServiceSecID.equalsIgnoreCase("XXXX") && aOBXSegmentCount == 0) {
                aOutOBXSegment = new HL7Segment("OBX");
                aLU = new CodeLookUp("PACS_URL.table", mEnvironment);
                mSReg2 = aLU.getValue("REPORT");
                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(aOBXSegmentID++));
                aOutOBXSegment.set(HL7_23.OBX_2_value_type, "TX");
                aLU = new CodeLookUp("PARIS_ORM_Codes.table", mEnvironment);
                mSReg3 = aLU.getValue(aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code));
                if (mSReg3.length() == 0) {
                    aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID));
                } else {
                    aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, mSReg3);
                }
                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, mSReg2);
                aOutOBXGroup.append(aOutOBXSegment);
            }
            if (aDiagServiceSecID.equalsIgnoreCase("XXXX")) {
                aOutOBXSegment = new HL7Segment("OBX");
                aLU = new CodeLookUp("PACS_URL.table", mEnvironment);
                mSReg2 = aLU.getValue("URL");
                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(aOBXSegmentID++));
                aOutOBXSegment.set(HL7_23.OBX_2_value_type, "RP");
                aLU = new CodeLookUp("PARIS_ORM_Codes.table", mEnvironment);
                mSReg3 = aLU.getValue(aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code));
                if (mSReg3.length() == 0) {
                    aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID));
                } else {
                    aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, mSReg3);
                }
                aOutOBXSegment.set(HL7_23.OBX_4_observation_sub_ID, "1");

                if (mSReg2.indexOf("pid") >= 0) {
                    mSReg3 = mSReg2.concat(pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1));
                } else {
                    mSReg3 = mSReg2.concat(aOutOBRSegment.get(HL7_23.OBR_20_Fillers_Field_1).substring(2, 12));
                }
                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, mSReg3.concat("^IMAGEURL^IMAGE"), 1);
                aOutOBXGroup.append(aOutOBXSegment);
            }

            if (aInOBRSegment.hasValue(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID, "RA") &&
                    aInORCSegment.hasValue(HL7_23.ORC_1_order_control, "NW")) {
                aOutOBXSegment = new HL7Segment("OBX");
                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(aOBXSegmentID++));
                aOutOBXSegment.set(HL7_23.OBX_2_value_type, "NM");
                aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, "FUTUREORDER");
                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "0", 1);
                aOutOBXGroup.append(aOutOBXSegment);
            }

            aOutReqDetsGroup.append(aOutOBXGroup);
        }
        return aOutReqDetsGroup;
    }

//------------------------------------------------------------------------------
    public HL7Segment processURLMSH_FromUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        aOutMSHSegment.linkTo(aInMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        aOutMSHSegment.copy(HL7_23.MSH_3_sending_application);
        aOutMSHSegment.copy(HL7_23.MSH_4_sending_facility);
        aOutMSHSegment.copy(HL7_23.MSH_6_receiving_facility);
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "CERNERPM");
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        aOutMSHSegment.set(HL7_23.MSH_9_1_message_type, "ORU");
        aOutMSHSegment.set(HL7_23.MSH_9_2_trigger_event, "R01");
        aOutMSHSegment.copy(HL7_23.MSH_10_message_control_ID);
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");
        return aOutMSHSegment;
    }

//------------------------------------------------------------------------------
    public HL7Segment processURLPIDFromUFD() {
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");
        int aPID3FieldCount = aInPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3FieldCount; i++) {
            String aPatientIDTypeCode = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i);
            if (aPatientIDTypeCode.equalsIgnoreCase("MR") || aPatientIDTypeCode.equalsIgnoreCase("PI") ||
                    aPatientIDTypeCode.length() == 0) {
                String aPIDIDNum = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, i);
                int aPIDIDNumLength = aPIDIDNum.length();
                if (aPIDIDNumLength < 8) {
                    aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
                } else {
                    aPIDIDNum = aPIDIDNum.substring(aPIDIDNumLength - 7,  aPIDIDNumLength);
                    aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPIDIDNum, 1);
                }
            }
        }

        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
        aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, "L", 1);
        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_geographic_designation, 1);
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_18_account_number);

        return aOutPIDSegment;
    }

//------------------------------------------------------------------------------
    public HL7Segment processURLPV1FromUFD() {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (aInPV1Segment.getSegment().length() > 0) {
            aOutPV1Segment = new HL7Segment("PV1");
            aOutPV1Segment.linkTo(aInPV1Segment);
            aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
            aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);

            String aACWard = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            String aACBed = aACWard + "_" + aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
            CodeLookUp aLU_BED = new CodeLookUp("ALFCENTRE_BED.table", mEnvironment);

            aACWard = aLU_WARD.getValue(aACWard);
            aACBed = aLU_BED.getValue(aACBed);

            if (aACWard.length() > 0) {
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aACWard);
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aACBed);
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_building, "AC");
            } else {
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
            }

            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "01");
            aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID);
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority);
            aOutPV1Segment.copy(HL7_23.PV1_18_patient_type);
            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
            aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
            aOutPV1Segment.copy(HL7_23.PV1_45_discharge_date_time);

            if (aOutPV1Segment.get(HL7_23.PV1_45_discharge_date_time).length() > 0) {
                aOutPV1Segment.set(HL7_23.PV1_41_account_status, "D");
            }
        }
        return aOutPV1Segment;
    }

//------------------------------------------------------------------------------
    public HL7Group processURLReqDets_FromUFD(HL7Segment pPIDSegment, HL7Segment pMSHSegment) {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Group aOutOBRNTEGroup = new HL7Group("");
        HL7Group aOutOBXNTEGroup = new HL7Group("");
        HL7Segment aOutOBXSegment = new HL7Segment("");
        //String aSReg1 = "";
        //String aSReg2 = "";
        //String aSReg3 = "";

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            if (aInORCSegment.getSegment().length() > 0) {
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                aOutORCSegment.set(HL7_23.ORC_1_order_control, "RE");
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_2_namespace_ID);
                aOutORCSegment.copy(HL7_23.ORC_5_order_status);
                aOutORCSegment.copy(HL7_23.ORC_9_date_time_of_trans);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_13_enterers_location);
                aOutORCSegment.copy(HL7_23.ORC_16_order_control_code_reason);
                aOutReqDetsGroup.append(aOutORCSegment);
            }

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID);
            aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID);
            aOutOBRSegment.copy(HL7_23.OBR_7_Observation_Date_Time);

            int aCollectorIDCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_10_collector_ID);
            for (int j = 1; j <= aCollectorIDCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_ID_num, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_last_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_first_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_middle_initial_or_name, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_suffix, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_prefix, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_degree, j);
                aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_code_source_table, j);
            }
            aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_text);
            aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);
            aOutOBRSegment.copy(HL7_23.OBR_14_Specimen_Received_Date_Time);
            String aSpecimenCode = aInOBRSegment.get(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code);
            String aSpecimenText = aInOBRSegment.get(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text);
            String aCollectionMethod = aInOBRSegment.get(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method);
            aOutOBRSegment.set(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_1_Specimen_code, aSpecimenCode);
            aOutOBRSegment.set(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text, aSpecimenText);
            aOutOBRSegment.set(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method, aCollectionMethod);
            aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_19_Placers_Field_2);
            aOutOBRSegment.copy(HL7_23.OBR_20_Fillers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_21_Fillers_Field_2);
            aOutOBRSegment.set(HL7_23.OBR_22_Results_RPT_Status_Change, pMSHSegment.get(HL7_23.MSH_7_message_date_time) + "00");
            aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
            aOutOBRSegment.set(HL7_23.OBR_25_Results_Status, "P");
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
            aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
            }

            aOutOBRSegment.copy(HL7_23.OBR_29_Parent);
            aOutOBRSegment.copy(HL7_23.OBR_30_Transportation_Mode);

            int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
            for (int j = 1; j <= aReasonForStudyCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
            }

            aOutOBRSegment.set(HL7_23.OBR_32_Principal_Result_Interpreter, "R375RAD");

            int aTechnicianCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_34_technician);
            for (int j = 1; j <= aTechnicianCount; j++) {
                String aTechnician = aInOBRSegment.get(HL7_23.OBR_34_technician);
                aOutOBRSegment.set(HL7_23.OBR_34_technician, aTechnician, j);
            }

            aOutReqDetsGroup.append(aOutOBRSegment);

            //process OBX
            HL7Group aOutOBXGroup = new HL7Group("");
            int aOBXSegmentCount = aInReqDetsGroup.countSegments(HL7_23.OBX);
            for (int k = 1; k <= aOBXSegmentCount; k++) {
                HL7Segment aInOBXSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBX", k));
                aOutOBXSegment = new HL7Segment("OBX");
                aOutOBXSegment.linkTo(aInOBXSegment);
                aOutOBXSegment.copy(HL7_23.OBX_1_set_ID);
                aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
                aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier);
                aOutOBXSegment.copy(HL7_23.OBX_4_observation_sub_ID);

                int aOBX5FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                for (int m = 1; m <= aOBX5FieldCount; m++) {
                    aOutOBXSegment.copy(HL7_23.OBX_5_observation_value, m);
                }
                String aOBX6Unit = aInOBXSegment.get(HL7_23.OBX_6_units);
                aOBX6Unit = replaceInStr(aOBX6Unit, "^", "\\S\\");

                aOutOBXSegment.set(HL7_23.OBX_6_units, aOBX6Unit);
                aOutOBXSegment.copy(HL7_23.OBX_7_references_range);

                int aOBX8FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_8_abnormal_flags);
                for (int n = 1; n <= aOBX8FieldCount; n++) {
                    aOutOBXSegment.copy(HL7_23.OBX_8_abnormal_flags, n);
                }

                aOutOBXSegment.copy(HL7_23.OBX_11_observ_results_status);
                aOutOBXSegment.copy(HL7_23.OBX_14_date_time_of_the_observation);
                aOutOBXSegment.copy(HL7_23.OBX_15_producers_ID, HL7_23.CE_ID_code);
                aOutOBXSegment.copy(HL7_23.OBX_16_responsible_observer);

                aOutOBXGroup.append(aOutOBXSegment);

            }
            //process URL pointer if report carries an image
            String aDiagServiceSecID = aInOBRSegment.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
            if (aDiagServiceSecID.equalsIgnoreCase("XX") && aOBXSegmentCount == 0) {
                aOutOBXSegment = new HL7Segment("OBX");
                CodeLookUp aLU = new CodeLookUp("PACS_URL.table", mEnvironment);
                mSReg2 = aLU.getValue("REPORT");
                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, "1");
                aOutOBXSegment.set(HL7_23.OBX_2_value_type, "TX");
                aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID));
                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, mSReg2);
                aOutOBXGroup.append(aOutOBXSegment);
            }
            HL7Segment aInOBRSegmentTemp = new HL7Segment(aOutReqDetsGroup.getSegment("OBR", 1));
            String aStartDateTime = aInOBRSegmentTemp.get(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time);
            if (aDiagServiceSecID.equalsIgnoreCase("RA")) {
                aOutOBXSegment = new HL7Segment("OBX");
                CodeLookUp aLU = new CodeLookUp("PACS_URL.table", mEnvironment);
                mSReg2 = aLU.getValue("URL");
                aOutOBXSegment.set(HL7_23.OBX_1_set_ID, "1");
                aOutOBXSegment.set(HL7_23.OBX_2_value_type, "RP");
                aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aOutOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID));
                aOutOBXSegment.set(HL7_23.OBX_4_observation_sub_ID, "1");

                if (mSReg2.indexOf("pid") >= 0) {
                    String aHospID = pMSHSegment.get(HL7_23.MSH_4_sending_facility).substring(0, 1);
                    if (aHospID.equalsIgnoreCase("C")) {
                        mSReg4 = "-CGMC";
                    } else if (aHospID.equalsIgnoreCase("S")) {
                        mSReg4 = "-SDMH";
                    } else {
                        mSReg4 = "";
                    }
                    mSReg3 = mSReg2.concat(pPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1)).concat(mSReg4);
                } else {
                    mSReg3 = mSReg2.concat(aOutOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2, 12));
                }
                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, mSReg3.concat("^IMAGEURL^IMAGE"), 1);
                aOutOBXGroup.append(aOutOBXSegment);
            }
            aOutReqDetsGroup.append(aOutOBXGroup);
        }
        return aOutReqDetsGroup;
    }
}
