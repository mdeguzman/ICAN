/*
 * HEALTHLINKProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 */

package BHLibHEALTHLINK;

import BHLibClasses.*;

/**
 *
 * @author fillinghamr
 */
public class HEALTHLINKProcessFromUFD extends ProcessSegmentsFromUFD {

    BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public HL7Message mInHL7Message;
//    public String mHEALTHLINKType = "";
    public CodeLookUp mLookUp;
    public HL7Segment mInOBRSegment;
    public String mSReg1 = "";
    public String mSReg2 = "";
    public String mSReg3 = "";
    public String mSReg4 = "";
    public HL7Segment mInPV1Segment;
    public String mHLSendingApplication = "";
    public String mHLSendingFacility = "";
    public String mHLReceivingApplication = "";
    public String mHLAUSNATANumber = "";
    public String mHLProviderMailbox = "";
    public String mHLProviderName = "";
    public String mHLSourceSendingApplication = "";
    public String mHL7BaysideDoctor = "";
    public String[] mHEALTHLINKMessageArray;
    public int mHLCount = 1;

    //--------------------------------------------------------------------------
    /**
     * Creates a new instance of HEALTHLINKProcessFromUFD
     */
    public HEALTHLINKProcessFromUFD(String pHL7Message, String pEnvironment)  throws ICANException {
        super(pHL7Message);
        mVersion = "d";    // HEALTHLINKProcessFromUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
//
//    public HEALTHLINKProcessFromUFD(String pHL7Message, String pEnvironment, String pHEALTHLINKType)  throws ICANException {
//        super(pHL7Message);
//        mVersion = "c";    // HEALTHLINKProcessFromUFD Release Version Number
//        mHL7Message = pHL7Message;
//        mEnvironment = pEnvironment;
//        mHEALTHLINKType = pHEALTHLINKType;
//    }
    //--------------------------------------------------------------------------
    public String[] processMessage() throws ICANException {
        mInHL7Message = new HL7Message(mHL7Message);

        String aSegment;
        HL7Group aGroup;
        HL7Message aOutMess = new HL7Message("");

        HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));
        String aSendingApp = aMSHSegment.get(HL7_23.MSH_3_sending_application);
        if (hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"RA") || aSendingApp.startsWith("GERIS")) {
            mLookUp = new CodeLookUp("HEALTHLINK_RAD_Codes.table", mEnvironment);
        } else {
            mLookUp = new CodeLookUp("HEALTHLINK_Codes.table", mEnvironment);
        }

// HEALTHLINK takes R01, R03 messages only
// HEALTHLINK ADT Structure is ... MSH, PID, PV1
        mInPV1Segment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PV1));
        int aHealthLinkDoctorNumber = mInPV1Segment.countRepeatFields("PV1_52");
        int aHealthLinkDoctorNumberParis  = 0;
        String aOBR_16Value = "";
        String aOBR_28Value = "";
        HL7Segment aInOBRSegment;
        aInOBRSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.OBR));
        if (aSendingApp.startsWith("PARIS") || aSendingApp.startsWith("CERNER")) {
            //copy OBR_16 to OBR_28 for Paris results
            aOBR_16Value = aInOBRSegment.get(HL7_23.OBR_16_Ordering_Provider);
            aOBR_28Value = aInOBRSegment.get(HL7_23.OBR_28_Results_Copies_To);
            if (aOBR_28Value.length() > 0) {
                aInOBRSegment.set(HL7_23.OBR_28_Results_Copies_To, aOBR_28Value.concat("~").concat(aOBR_16Value));
            } else {
                aInOBRSegment.set(HL7_23.OBR_28_Results_Copies_To, aOBR_16Value);
            }
        }

        if (aOBR_16Value.length() > 0 || aOBR_28Value.length() > 0) {
            aHealthLinkDoctorNumberParis = aInOBRSegment.countRepeatFields(HL7_23.OBR_28_Results_Copies_To);
        }
        if (aSendingApp.startsWith("GERIS")) {
            mHEALTHLINKMessageArray = new String[aHealthLinkDoctorNumber + 2];
        } else {
            mHEALTHLINKMessageArray = new String[aHealthLinkDoctorNumberParis + 2];
        }
        if (mInHL7Message.isEvent("R01, R03")) {
            //mInOBRSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.OBR));
            if ((aSendingApp.startsWith("PARIS") || aSendingApp.startsWith("CERNER")) && aHealthLinkDoctorNumberParis > 0) {
                mHLCount = 1;
                mHLSendingApplication = mLookUp.getValue("SEND_APPLICATION");
                mHLSendingFacility = mLookUp.getValue("SEND_FACILITY");
                mHLReceivingApplication = mLookUp.getValue("REC_APPLICATION");
                mHLAUSNATANumber = mLookUp.getValue("NATA_Number");
                for (int aHLDoctorCount = 1; aHLDoctorCount <= aHealthLinkDoctorNumberParis; aHLDoctorCount++) {
                    mHL7BaysideDoctor = aInOBRSegment.get(HL7_23.OBR_28_Results_Copies_To, HL7_23.XCN_ID_num, aHLDoctorCount);
                    mHLProviderMailbox = mLookUp.getValue("PRACTICE-".concat(mHL7BaysideDoctor));
                    mHLProviderName = mLookUp.getValue(mHL7BaysideDoctor);

                    if (mHLProviderName.length() > 0) {
                        aMSHSegment = processMSHFromUFD();
                        HL7Segment aPIDSegment = processPIDFromUFD();
                        HL7Group aReqDetsGroup = processReqDets_FromUFD();
                        aOutMess.append(aMSHSegment);
                        aOutMess.append(aPIDSegment);
                        aOutMess.append(aReqDetsGroup);

                        mHEALTHLINKMessageArray[1 + aHLDoctorCount] = aOutMess.getMessage();
                        aOutMess = new HL7Message("");
                    }
                }

            } else if (aSendingApp.startsWith("GERIS") && aHealthLinkDoctorNumber > 0) {
                //Proceed if report is completed
                HL7Segment aInORCSegment = new HL7Segment(mInHL7Message.getSegment("ORC"));
                String aCompletedReport = aInORCSegment.get(HL7_23.ORC_5_order_status);
                if (aCompletedReport.equalsIgnoreCase("CM"))  {
                    //GE_RIS healthlink process
                    mHLSourceSendingApplication = aSendingApp;
                    mHLCount = 1;
                    mHLSendingApplication = mLookUp.getValue("SEND_APPLICATION");
                    mHLSendingFacility = mLookUp.getValue("SEND_FACILITY");
                    mHLReceivingApplication = mLookUp.getValue("REC_APPLICATION");
                    mHLAUSNATANumber = mLookUp.getValue("NATA_Number");
                    for (int aHLDoctorCount = 1; aHLDoctorCount <= aHealthLinkDoctorNumber; aHLDoctorCount++) {
                        mHL7BaysideDoctor = mInPV1Segment.get("PV1_52", "ID_1", aHLDoctorCount);
                        mHLProviderMailbox = mLookUp.getValue("PRACTICE-".concat(mHL7BaysideDoctor));
                        mHLProviderName = mLookUp.getValue(mHL7BaysideDoctor);

                        //check if BaysideDoctor is a HealthLink Doctor
                        if (mHLProviderName.length() > 0) {
                            //process Healthlink message if mailbox is not empty
                            if (mHLProviderMailbox.length() > 0) {
                                aMSHSegment = processMSH_GERIS_FromUFD();
                                HL7Segment aPIDSegment = processPIDFromUFD();
                                HL7Group aReqDetsGroup = processReqDets_FromUFD();
                                aOutMess.append(aMSHSegment);
                                aOutMess.append(aPIDSegment);
                                aOutMess.append(aReqDetsGroup);

                                mHEALTHLINKMessageArray[1 + aHLDoctorCount] = aOutMess.getMessage();
                            } else {
                                mHEALTHLINKMessageArray[1 + aHLDoctorCount] = "";
                            }
                            aOutMess = new HL7Message("");
                        }
                    }
                }
            }
        }
        if (aSendingApp.startsWith("GERIS")) {
            mHEALTHLINKMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            mHEALTHLINKMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
        } else {
            mHEALTHLINKMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            mHEALTHLINKMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            //mHEALTHLINKMessageArray[2] = aOutMess.getMessage();
        }

        return mHEALTHLINKMessageArray;
    }

    public boolean isHEALTHLINKProvider(HL7Segment pOBRSegment) {
// Return True if this is for a valid Healthlink Dr.
        boolean aFlag = false;
        String aTmp;
        int aOrderingProviderCount = pOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
        for (int j = 1; j <= aOrderingProviderCount; j++) {
            aTmp = mLookUp.getValue(pOBRSegment.get(HL7_23.OBR_16_Ordering_Provider,HL7_23.XCN_ID_num,j));
            if (aTmp.equalsIgnoreCase("")){
            }  else {
                aFlag = true;
            }
        }
        return aFlag;
    }
    public String getHEALTHLINKProvider(HL7Segment pOBRSegment) {
// Return the Healthlink Dr.
        String aProvider = "";
        String aTmp;
        int aOrderingProviderCount = pOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
        for (int j = 1; j <= aOrderingProviderCount; j++) {
            aTmp  = mLookUp.getValue("PRACTICE-" + pOBRSegment.get(HL7_23.OBR_16_Ordering_Provider,HL7_23.XCN_ID_num,j));
            if (aTmp.equalsIgnoreCase("")){
            }  else {
                aProvider  = aTmp;
            }
        }
        return aProvider ;
    }

    //--------------------------------------------------------------------------
    /**HEALTHLINK specific processing for an Outgoing i.e "To" MSH segment.
     * @return Returns the processed HL7 MSH segment as a String.
     */
    public HL7Segment processMSHFromUFD() throws ICANException {
// Non copy fields are

        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);

        HL7Segment aMSHSegIN = new HL7Segment(NULL);
        HL7Segment aMSHSegOUT = new HL7Segment("MSH");

        aMSHSegIN.setSegment(aHL7Message.getSegment(HL7_24.MSH));
        mHL7Segment = aMSHSegIN;                    // In case any of the "do" functions need to see the segment
        mFacility = aMSHSegIN.get(HL7_24.MSH_4_sending_facility);
        mHospitalID = mFacility.substring(mFacility.length()-1, mFacility.length());
        mHL7MessageEvent = aMSHSegIN.get(HL7_23.MSH_9_message_type, HL7_23.CM_event);

// Initialze aMSHSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.MSH_2_encoding_characters,
            HL7_23.MSH_9_message_type
//            HL7_23.MSH_10_message_control_ID,
        };

// Initialze OUT with those fields that are straight copies
        aMSHSegOUT.linkTo(aMSHSegIN);
        aMSHSegOUT.copyFields(aCopyFields);
        aMSHSegOUT.set(HL7_23.MSH_3_sending_application, mHLSendingApplication);
        aMSHSegOUT.set(HL7_23.MSH_4_sending_facility, mHLSendingFacility);
        aMSHSegOUT.set(HL7_23.MSH_5_receiving_application, mHLReceivingApplication);
        aMSHSegOUT.set(HL7_23.MSH_6_receiving_facility, mHLProviderMailbox);
        aMSHSegOUT.set(HL7_23.MSH_7_message_date_time, aMSHSegIN.getDateTime());

        String aMSHControlID = aMSHSegIN.get(HL7_23.MSH_10_message_control_ID);
        aMSHControlID = aMSHControlID.substring(0, aMSHControlID.length()-1);
        aMSHSegOUT.set(HL7_23.MSH_10_message_control_ID, aMSHControlID.concat(String.valueOf(mHLCount++)));

        aMSHSegOUT.set(HL7_23.MSH_11_processing_ID, "P");
        aMSHSegOUT.set(HL7_23.MSH_12_version_ID, "2.3.1^AUS");
        return (aMSHSegOUT);
    }
    //--------------------------------------------------------------------------
    /**
     * HEALTHLINK specific processing for an Outgoing i.e "To" PID segment where the PID is that contained in the HL7 message itself.
     * @return Returns the processed HL7 PID segment as a String.
     */
    public HL7Segment processPIDFromUFD() throws ICANException {
        return (this.processPIDFromUFD(mHL7Message));
    }
    //--------------------------------------------------------------------------
    /**
     * HEALTHLINK specific processing for an Outgoing i.e "To" PID segment where the PID is in the [pHL7MessageBlock] .
     * @param pHL7MessageBlock Either a full HL7 Message or may just be an A17 Patient Group.
     * @return Returns the processed HL7 PID segment as a String.
     */
    public HL7Segment processPIDFromUFD(String pHL7MessageBlock) throws ICANException {

        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);

        HL7Segment aPIDSegIN = new HL7Segment(k.NULL);
        HL7Segment aPIDSegOUT = new HL7Segment("PID");

        HL7Field aPID3Field = new HL7Field();
        HL7Field aPID3FieldTemp = new HL7Field();

        aPIDSegIN.setSegment(aHL7Message.getSegment(HL7_24.PID));
        String aPID3Array[] = aPIDSegIN.getRepeatFields(HL7_23.PID_3_patient_ID_internal);

        // Initialze aPIDSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PID_1_set_ID,
            HL7_23.PID_7_date_of_birth,
            HL7_23.PID_8_sex
        };

        aPIDSegOUT.linkTo(aPIDSegIN);
        aPIDSegOUT.copyFields(aCopyFields);

        // Pass all avaialbe unique ID's to HEALTHLINK ....
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
        aPIDSegOUT.setRepeatFields(HL7_23.PID_3_patient_ID_internal, aPID3Array);


//Check and swap ALFUR/CGMCUR/SDMHUR to ALF
        int aPIDCounter01 = aPIDSegOUT.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int aIndex01 = 1; aIndex01 <= aPIDCounter01; aIndex01++) {
            if (aPIDSegOUT.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aIndex01).equalsIgnoreCase("ALFUR")) {
                aPIDSegOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "ALF", aIndex01);
            }
            if (aPIDSegOUT.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aIndex01).equalsIgnoreCase("CGMCUR")) {
                aPIDSegOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "CGMC", aIndex01);
            }
            if (aPIDSegOUT.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aIndex01).equalsIgnoreCase("SDMHUR")) {
                aPIDSegOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "SDMH", aIndex01);
            }
        }

        aPIDSegOUT.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        aPIDSegOUT.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
        aPIDSegOUT.set(HL7_23.PID_10_race, "9");                    // Force to "Unknown" since PARIS does not send this.

        aPIDSegOUT.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
        aPIDSegOUT.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        aPIDSegOUT.copy(HL7_23.PID_15_language, HL7_23.CE_ID_code, 1);

        aPIDSegOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1);
        aPIDSegOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2);
        aPIDSegOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city);
        aPIDSegOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province);
        aPIDSegOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip);
        aPIDSegOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country);
        aPIDSegOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_geographic_designation);

        //System.out.println("----->" + aPIDSegOUT.getSegment());

        return aPIDSegOUT;
    }
//--------------------------------------------------------------------------
    public HL7Group processReqDets_FromUFD() {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Segment aOutNTESegment = new HL7Segment("");
        HL7Segment aOutOBXSegment = new HL7Segment("");


        String aOrderingProvider;

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            //process ORC
            HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
            aOutORCSegment = new HL7Segment("ORC");
            aOutORCSegment.linkTo(aInORCSegment);
            aOutORCSegment.copy(HL7_23.ORC_1_order_control);

            aOutORCSegment.move(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID, HL7_23.ORC_3_filler_order_num,  HL7_23.EI_1_entity_ID);
            aOutORCSegment.set(HL7_23.ORC_2_placer_order_num, HL7_23.EI_2_namespace_ID,  "AUSNATA") ;
//            if (mHLSourceSendingApplication.startsWith("GERIS")) {
            aOutORCSegment.set(HL7_23.ORC_2_placer_order_num, HL7_23.EI_3_universal_ID, mHLAUSNATANumber) ;
//            } else {
//                aOutORCSegment.set(HL7_23.ORC_2_placer_order_num, HL7_23.EI_3_universal_ID, mLookUp.getValue("NATA_Number")) ;
//            }
            aOutORCSegment.set(HL7_23.ORC_2_placer_order_num, HL7_23.EI_4_universal_ID_type, "L") ;

            aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
            aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID, "AUSNATA") ;
//            if (mHLSourceSendingApplication.startsWith("GERIS")) {
            aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_3_universal_ID, mHLAUSNATANumber) ;
//            } else {
//                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_3_universal_ID, mLookUp.getValue("NATA_Number")) ;
//            }
            aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_4_universal_ID_type, "L") ;

            if (aInORCSegment.hasValue(HL7_23.ORC_5_order_status, "CM")) {
                aOutORCSegment.set(HL7_23.ORC_5_order_status, "CM");
            } else {
                aOutORCSegment.set(HL7_23.ORC_5_order_status, "A");
            }

            aOutORCSegment.copy(HL7_23.ORC_9_date_time_of_trans);
            aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider);

            aOutReqDetsGroup.append(aOutORCSegment);

            //process OBR
            HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));

            //Translate procedure mnemonic code if need be...
            //String aOBR4UniversalServiceID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
            //CodeLookUp aLU = new CodeLookUp("HEALTHLINK_Procedures.table", mEnvironment);
            //aOBR4UniversalServiceID = aLU.getValue(aOBR4UniversalServiceID);

            aOutOBRSegment = new HL7Segment("OBR");
            aOutOBRSegment.linkTo(aInOBRSegment);
            aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);

            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);

            aOutOBRSegment.move(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID, HL7_23.OBR_3_Filler_Order_Number,  HL7_23.EI_1_entity_ID);
            aOutOBRSegment.set(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID, "AUSNATA") ;
//            if (mHLSourceSendingApplication.startsWith("GERIS")) {
            aOutOBRSegment.set(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_3_universal_ID, mHLAUSNATANumber) ;
//            } else {
//                aOutOBRSegment.set(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_3_universal_ID, mLookUp.getValue("NATA_Number")) ;
//            }
            aOutOBRSegment.set(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_4_universal_ID_type, "L") ;

            aOutOBRSegment.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID, "AUSNATA") ;
//            if (mHLSourceSendingApplication.startsWith("GERIS")) {
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_3_universal_ID, mHLAUSNATANumber);
//            } else {
//                aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_3_universal_ID, mLookUp.getValue("NATA_Number")) ;
//            }
            aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_4_universal_ID_type, "L") ;

            if (mHLSourceSendingApplication.startsWith("GERIS")) {
                aOutOBRSegment.copy(HL7_23.OBR_4_Universal_Service_ID);
            } else {
                aOutOBRSegment.move(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text);
                aOutOBRSegment.move(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text, HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme);
                aOutOBRSegment.move(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID);
            }

            aOutOBRSegment.copy(HL7_23.OBR_7_Observation_Date_Time);
            aOutOBRSegment.copy(HL7_23.OBR_13_Relevant_Clinical_Information);
            aOutOBRSegment.copy(HL7_23.OBR_14_Specimen_Received_Date_Time);


            if (mHLSourceSendingApplication.startsWith("GERIS")) {
                aOutOBRSegment.set(HL7_23.OBR_16_Ordering_Provider, mHLProviderName);
            } else {
//                int aOrderingProviderCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
//                for (int j = 1; j <= aOrderingProviderCount; j++) {
//                    aOutOBRSegment.set(HL7_23.OBR_16_Ordering_Provider, mLookUp.getValue(aInOBRSegment.get(HL7_23.OBR_16_Ordering_Provider,HL7_23.XCN_ID_num,j)), j);
//                }
                aOutOBRSegment.set(HL7_23.OBR_16_Ordering_Provider, mHLProviderName);
            }

            aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
            aOutOBRSegment.copy(HL7_23.OBR_22_Results_RPT_Status_Change);

            try {
                int aClientNumber = Integer.parseInt(aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code));
                if (hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"RA") || mHLSourceSendingApplication.startsWith("GERIS")) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
                }else if (aClientNumber >= 5000000 && aClientNumber <= 5149999) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "SP");
                } else if (aClientNumber >= 5150000 && aClientNumber <= 5199999) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "CP");
                } else if (aClientNumber >= 5300000 && aClientNumber <= 5399999) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "CH");
                } else if (aClientNumber >= 5500000 && aClientNumber <= 5599999) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "HM");
                } else if (aClientNumber >= 5700000 && aClientNumber <= 5799999) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "IMM");
                } else if (aClientNumber >= 5900000 && aClientNumber <= 5999999) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "SR");
                } else if (aClientNumber >= 6000000 && aClientNumber <= 6199999) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "BLB");
                } else if (aClientNumber >= 6200000 && aClientNumber <= 6299999) {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MB");
                } else {
                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "LAB");
                }

            } catch (Exception e) {
//                    aOBR21Filler = aInOBRSegment.get(HL7_23.OBR_21_Fillers_Field_2).substring(2,4);
//                    aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, aOBR21Filler);
            }

            aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);

            int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
            for (int j = 1; j <= aQuantityTimingCount; j++) {
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_1_quantity, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, j);
            }


            aOutReqDetsGroup.append(aOutOBRSegment);

            //START ======================== process OBX and OBXNTEs
            HL7Group aOutOBXGroup = new HL7Group("");
            HL7Segment aInSegment = new HL7Segment("");
            int aGroupSegmentCount = aInReqDetsGroup.countSegments();
            boolean aSkipSegment = false;
            boolean aFullText = false;
            boolean aRedoSegment = false;
            int aInsertBlankCount = 0;
            int aOBXSegID = 1;
            int k = 0;
            while (k <= aGroupSegmentCount || aInsertBlankCount != 0) {

// Get the next segment from this Group ...
// ... but only if we are not inserting blanks and if we do not need to redo the previous segemnt..
                if (aInsertBlankCount == 0 && aRedoSegment == false){
                    aInSegment = new HL7Segment(aInReqDetsGroup.getNextSegment());
                    k++;
                }
                aRedoSegment = false;
// Discard this segment if we previously set aSkiPSegment ....
                if (aSkipSegment == true && aInsertBlankCount == 0) {     // The next segment is to be discarded
                    aSkipSegment = false;
                } else {

// ... process this as a normal OBX segment ...
//                    display("++++" + aInSegment.getSegmentID());
                    if (aInSegment.getSegmentID().equalsIgnoreCase("ORC") ||
                            aInSegment.getSegmentID().equalsIgnoreCase("OBR") ) {
                        // Skip OBR and ORC ... already dealt with.

                    } else if (aInSegment.getSegmentID().equalsIgnoreCase("OBX")) {
// ... 1st TX value type when building an FT (Full Text) ...
                        if (aFullText == false && aInSegment.hasValue(HL7_23.OBX_2_value_type, "TX")) {
                            aFullText = true;
                            aOutOBXSegment = new HL7Segment("OBX");
                            aOutOBXSegment.linkTo(aInSegment);
                            aOutOBXSegment.set(HL7_23.OBX_1_set_ID, String.valueOf(aOBXSegID++));
                            aOutOBXSegment.set(HL7_23.OBX_2_value_type, "FT");
                            if (mHLSourceSendingApplication.startsWith("GERIS")) {
                                aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier);
                            } else {
                                aOutOBXSegment.move(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code,HL7_23.OBX_3_observation_identifier, HL7_23.CE_text );
                                aOutOBXSegment.move(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text,HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme );
                                aOutOBXSegment.move(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme,HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID);
                            }
                            aOutOBXSegment.copy(HL7_23.OBX_4_observation_sub_ID);


                            // Note:- Following uses "$ $" to insert a space because the set command trims spaces.
                            // ... further down, when we append segment, we then replace "$ $" by a single space char.
                            String aObsValue = aInSegment.get(HL7_23.OBX_5_observation_value);

                            // For LA and BA textual reports we have to dummy the 1st OBX to carry the Test Description.
                            if (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "LA")  || aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "BA")) {      // Force first OBX to have Test Name in it.
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, aOutOBXSegment.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text));
                                aRedoSegment = true;

                            } else if (aObsValue.equalsIgnoreCase(".")) {  // If value only contains a "." then insert a blank line
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "$ $");

                            } else if (aInsertBlankCount > 0) {      // Force a blank line
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "$ $");
                                aInsertBlankCount--;

                            } else {                                // Process the normal conents of the OBX value
                                aOutOBXSegment.set(HL7_23.OBX_5_observation_value, aObsValue);

                                // Check if this is the last required line of a Radiology report section.
                                if (aObsValue.indexOf("Reported by:") >= 0 && (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"RA")))  {
                                    aSkipSegment = true;     // Skip the line after "Reported By" ....
                                    aInsertBlankCount = 2;     // ... and insert two blank lines.
                                }
                            }

                            String aAbnormalFlag = aInSegment.get(HL7_23.OBX_8_abnormal_flags);
                            if (aAbnormalFlag.length() > 0) {
                                aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag);
                            } else {
                                String aObservResultsStatus = aInSegment.get(HL7_23.OBX_11_observ_results_status);
                                if (aObservResultsStatus.equalsIgnoreCase("C")) {
                                    aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, "N");
                                } else {
                                    aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag);
                                }
                            }

                            aOutOBXSegment.set(HL7_23.OBX_11_observ_results_status,aInOBRSegment.get(HL7_23.OBR_25_Results_Status) );
//                            aOutOBXSegment.copy(HL7_23.OBX_12_date_last_obs_normal_values);
//                            aOutOBXSegment.copy(HL7_23.OBX_14_date_time_of_the_observation);

//
// ... assemble any additional TX value types into the FT ....
                        } else if (aFullText == true && aInSegment.hasValue(HL7_23.OBX_2_value_type, "TX"))  {


                            // Note:- Following uses "$ $" to insert a space because the set command trims spaces.
                            // ... further down, when we append segment, we then replace "$ $" by a single space char.
                            String aObsValue = aInSegment.get(HL7_23.OBX_5_observation_value);

                            if (aObsValue.equalsIgnoreCase(".")) {  // If value only contains a "." then insert a blank line
                                aObsValue = "$ $";

                            } else if (aInsertBlankCount > 0) {      // Force a blank line
                                aObsValue = "$ $";
                                aInsertBlankCount--;

                            } else {                                // Process the normal contents of the OBX value

                                // Check if this is the last required line of a Radiology report section.
                                if (aObsValue.indexOf("Reported by:") >= 0 && (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"RA")))  {
                                    aSkipSegment = true;     // Skip the line after "Reported By" ....
                                    aInsertBlankCount = 2;     // ... and insert two blank lines.
                                }
                            }
                            aOutOBXSegment.set(HL7_23.OBX_5_observation_value, aOutOBXSegment.get(HL7_23.OBX_5_observation_value).concat("\\.br\\" + aObsValue));


                        } else  {

                            if (aFullText == true)   {
//
// ... Will only come here is we are assembling an FT and there is a non TX segment in the list of OBX segments ....
                                aOutOBXGroup.append(cleanoutStr(replaceInStr(aOutOBXSegment.getSegment(), "\\$ \\$", " ")));
                                aFullText = false;
                            }

                            aOutOBXSegment = new HL7Segment("OBX");
                            aOutOBXSegment.linkTo(aInSegment);
                            aOutOBXSegment.set(HL7_23.OBX_1_set_ID, String.valueOf(aOBXSegID++));
                            aOutOBXSegment.copy(HL7_23.OBX_2_value_type);
                            if (mHLSourceSendingApplication.startsWith("GERIS")) {
                                aOutOBXSegment.copy(HL7_23.OBX_3_observation_identifier);
                            } else {
                                aOutOBXSegment.move(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code,HL7_23.OBX_3_observation_identifier, HL7_23.CE_text );
                                aOutOBXSegment.move(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text,HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme );
                                aOutOBXSegment.move(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme,HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID);
                            }
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
                                    if (aObsValue.indexOf("Reported by:") >= 0 && (aInOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID,"RA") ))  {
                                        aSkipSegment = true;     // Skip the line after "Reported By" ....
                                        aInsertBlankCount = 2;     // ... and insert two blank lines.
                                    }
                                }
                            }

                            String aOBX6Unit = aInSegment.get(HL7_23.OBX_6_units);

                            //aOBX6Unit = replaceInStr(aOBX6Unit, "\\^", "*");
                            aOutOBXSegment.set(HL7_23.OBX_6_units, aOBX6Unit.split("\\^")[0]);
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

                            aOutOBXGroup.append(cleanoutStr(replaceInStr(aOutOBXSegment.getSegment(), "\\$ \\$", " ")));
                        }
                    }else if (aInSegment.getSegmentID().equalsIgnoreCase("NTE")) {
                        if (aFullText == true)   {
//
// ... Will only come here is we are assembling an FT and there is a NTE segment following an OBX TX OBX segment ....
                            aOutOBXGroup.append(cleanoutStr(replaceInStr(aOutOBXSegment.getSegment(), "\\$ \\$", " ")));
                            aFullText = false;
                        }
                        aOutOBXSegment = new HL7Segment("OBX");
                        aOutOBXSegment.linkTo(aInSegment);
                        aOutOBXSegment.set(HL7_23.OBX_1_set_ID, String.valueOf(aOBXSegID++));
                        aOutOBXSegment.set(HL7_23.OBX_2_value_type, "FT");
                        aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, " " );
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, aInSegment.get(HL7_23.NTE_3_comment));
                        aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, "N" );
                        aOutOBXSegment.set(HL7_23.OBX_11_observ_results_status,aInOBRSegment.get(HL7_23.OBR_25_Results_Status) );

                        aOutOBXGroup.append(cleanoutStr(replaceInStr(aOutOBXSegment.getSegment(), "\\$ \\$", " ")));

                    }
                }
            }

            if (aFullText == true)   {
//
// ...  we have been assembling an FT and there are no more OBX segments ....
                aOutOBXGroup.append(cleanoutStr(replaceInStr(aOutOBXSegment.getSegment(), "\\$ \\$", " ")));
                aFullText = false;
            }

            //Norman Soh: Added change to replace DT with ST value type in OBX segments. 17/11/06
            int aOBXSegCount = aOutOBXGroup.countSegments();
            HL7Segment aOBXSegmentTemp = new HL7Segment("OBX");
            for (int aIndex = 1; aIndex <= aOBXSegCount; aIndex++) {
                aOBXSegmentTemp.setSegment(aOutOBXGroup.getSegment(aIndex));
                if (aOBXSegmentTemp.get(HL7_23.OBX_2_value_type).matches("DT")) {
                    aOBXSegmentTemp.set(HL7_23.OBX_2_value_type,"ST");
                    aOutOBXGroup.setSegment(aOBXSegmentTemp.getSegment(), aIndex);
                }
                //Norman Soh: Added change to perform OBX code lookup for OBX_3_ObservationID. 20/11/06
                String aOBX3ObservationID = aOBXSegmentTemp.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code);
                CodeLookUp aLU = new CodeLookUp("HEALTHLINK_Procedures.table", mEnvironment);
                String aCodeValueTemp = aLU.getValue(aOBX3ObservationID);
                if (!aCodeValueTemp.equalsIgnoreCase(aOBX3ObservationID)) {
                    //Different value obtained proceed to alter OBX_3_2
                    aOBXSegmentTemp.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text, aCodeValueTemp);
                    aOutOBXGroup.setSegment(aOBXSegmentTemp.getSegment(), aIndex);
                }
            }

            aOutReqDetsGroup.append(aOutOBXGroup);
        }

        return aOutReqDetsGroup;
    }

//==================================================================================================
// Method to do a sub-string replacement.
// Note:- Replaces str.replaceAll ... which was not happy replacing "^" by "\\S\\"
    public String replaceInStr(String pSrc, String pFrom, String pTo) {
        pSrc = pSrc.replaceAll(pFrom, pTo);
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

    public HL7Segment processMSH_GERIS_FromUFD() throws ICANException {

        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);

        HL7Segment aMSHSegIN = new HL7Segment(NULL);
        HL7Segment aMSHSegOUT = new HL7Segment("MSH");

        aMSHSegIN.setSegment(aHL7Message.getSegment(HL7_24.MSH));

        String aCopyFields[] =  {
            HL7_23.MSH_2_encoding_characters,
            HL7_23.MSH_9_message_type
        };

        aMSHSegOUT.linkTo(aMSHSegIN);
        aMSHSegOUT.copyFields(aCopyFields);
        aMSHSegOUT.set(HL7_23.MSH_3_sending_application, mHLSendingApplication);
        aMSHSegOUT.set(HL7_23.MSH_4_sending_facility, mHLSendingFacility);
        aMSHSegOUT.set(HL7_23.MSH_5_receiving_application, mHLReceivingApplication);
        aMSHSegOUT.set(HL7_23.MSH_6_receiving_facility, mHLProviderMailbox);
        aMSHSegOUT.set(HL7_23.MSH_7_message_date_time, aMSHSegIN.getDateTime());
        aMSHSegOUT.set(HL7_23.MSH_11_processing_ID, "P");
        aMSHSegOUT.set(HL7_23.MSH_12_version_ID, "2.3.1^AUS");

        //increment controlID when more than one report is to be sent
        String aMSHControlID = aMSHSegIN.get(HL7_23.MSH_10_message_control_ID);
        aMSHControlID = aMSHControlID.substring(0, aMSHControlID.length()-1);
        aMSHSegOUT.set(HL7_23.MSH_10_message_control_ID, aMSHControlID.concat(String.valueOf(mHLCount++)));

        return (aMSHSegOUT);
    }

}
