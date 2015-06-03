/*
 * CERNERProcessToUFD.java
 *
 * Created on 5 October 2005, 14:34
 *
 */

package com.dataagility.ICAN.BHLibCERNER;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 * CERNERProcessToUFD provides methods to process a message from CERNER to UFD
 * structure.  These methods are CERNER specific.
 * @author Ray Fillingham and Norman Soh
 */
public class CERNERProcessToUFD extends ProcessSegmentsToUFD {
    /**
     * This is a ZBX counter that keeps a record of the number of ZBX segments
     * created
     */
    public int mZBXSeqNum= 1;
    /**
     * Class wide HL7Message object
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";
    public boolean mA28Forced = false;
    /**
     * Creates a new instance of CERNERProcessToUFD
     * @param pHL7Message HL7 Message string
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public CERNERProcessToUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "B";    // CERNERProcessToUFD Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
    //--------------------------------------------------------------------------
    /**
     * processMessage will convert / process the entire CERNER message into a
     * UFD HL7 message structure
     * @return Returns a UFD HL7 message
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public String processMessage() throws ICANException {

        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));

        if (mInHL7Message.isEvent("O01")) {        // Order processing ... NOTE :- Event is Oh! zero one not zero zero one!.
            aOutHL7Message.append(processMSHToUFD());
            aOutHL7Message.append(processPIDToUFD());

            aOutHL7Message.append(processPV1ToUFD());
            aOutHL7Message.append(processPV2ToUFD());
            aOutHL7Message.append(processAlerts_ToUFD());
            aOutHL7Message.append(processOrders_ToUFD());

        } else if (mInHL7Message.isEvent("P03")) {
            HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PID));
            HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PV1));
            String aAssignAuth = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority);
            String aFinClass = aInPV1Segment.get(HL7_23.PV1_20_financial_class);
            CodeLookUp aLU = new CodeLookUp("ValidateBillingCompensableClass.table", mEnvironment);
            aFinClass = aLU.getValue(aFinClass);
            if (aAssignAuth.equalsIgnoreCase("ALFUR") && aFinClass.length() > 0) {
                aOutHL7Message.append(processMSHToUFD());
                aOutHL7Message.append(processEVNToUFD());
                aOutHL7Message.append(processPIDToUFD());
                aOutHL7Message.append(processPV1ToUFD());
                aOutHL7Message.append(processFinGroups_ToUFD());
                aOutHL7Message.append(processZEIZBXToUFD());
                aOutHL7Message.append(processZCDZBXs_ToUFD());
                aOutHL7Message.append(processGT1s_ToUFD());
                aOutHL7Message.append(processACCs_ToUFD());
            }
        } else if (mInHL7Message.isEvent("A01, A02, A03, A04, A08, A11, A12, A13, A21, A22, A28, A31")) {
            aOutHL7Message.append(processMSHToUFD());
            aOutHL7Message.append(processEVNToUFD());
            aOutHL7Message.append(processPIDToUFD());
            aOutHL7Message.append(processNK1s_ToUFD());
            aOutHL7Message.append(processPV1ToUFD());
            aOutHL7Message.append(processPV2ToUFD());
            aOutHL7Message.append(processAL1s_ToUFD());
            aOutHL7Message.append(processGT1s_ToUFD());
            aOutHL7Message.append(processOBXs_ToUFD());
            aOutHL7Message.append(processPIDNTEZBXs_ToUFD());
            aOutHL7Message.append(processOBXZBXs_ToUFD());
            aOutHL7Message.append(processZPIZBXToUFD());
            aOutHL7Message.append(processA28ZBXToUFD());
            aOutHL7Message.append(processZEIZBXToUFD());
        }
        if (aOutHL7Message.getMessage().length() > 0) {
            mZBXSegmentCount = mZBXSeqNum;
            aOutHL7Message.append(setupZBX("MESSAGE", "SOURCE_ID", aInMSHSegment.get(HL7_23.MSH_10_message_control_ID)));
        }
        return aOutHL7Message.getMessage();

    }
    //--------------------------------------------------------------------------
    /**
     * Returns the Order Group segment's (ORC, OBR, OBX, NTE) from the message.  No further processing of the segments
     * is performed.
     * @return Order Group Segments segment/s (ORC, OBR, OBX, NTE).
     * @throws BHLibClasses.ICANException ICANException
     */
    public HL7Group processOrders_ToUFD() throws ICANException {
        HL7Group aOrderGroup = new HL7Group();
        HL7Message aMessage = new HL7Message(mHL7Message);
        int i;
        int aCnt = aMessage.countGroups(HL7_23.Group_Orders);
        for (i = 1; i <= aCnt; i++) {
            aOrderGroup.append(aMessage.getGroup(HL7_23.Group_Orders,  i));
        }

        //Fix up Discontinued orders to Cancelled orders
        aCnt = aOrderGroup.countSegments();
        for (i = 1; i <= aCnt; i++) {
            HL7Segment aSegment = new HL7Segment(aOrderGroup.getSegment(i));
            if (aSegment.getSegmentID().equalsIgnoreCase("ORC")) {
                //String aORCControl = aSegment.get(HL7_23.ORC_1_order_control);
                String aORCStatus = aSegment.get(HL7_23.ORC_5_order_status);
                if (aORCStatus.matches("DC")) {
                    aSegment.set(HL7_23.ORC_1_order_control, "OC");
                    aSegment.set(HL7_23.ORC_5_order_status, "CA");
                    aOrderGroup.setSegment(aSegment.getSegment(), i);
                }
            }
        }

// Create the Radiology Printer Routing flag if required
//
//# Comment ...  "PRINTER ROUTING" "The following code is required to route Radiology requsitions to the correct printer depending on Priority, Ordering Source
//         and Diagnostic Section Sub ID (ie XRAY, CT, MRI .....).

        HL7Segment aOBRSeg = new HL7Segment(mInHL7Message.getSegment("OBR"));
        HL7Segment aPV1Seg = new HL7Segment(mInHL7Message.getSegment("PV1"));

        String aPriority = "X";        //sreg1
        String aPatLoc = "X";          // sreg2
        String aServiceSection = aOBRSeg.get(HL7_23.OBR_24_Diagnostic_Service_Section_ID);    //sreg3

        if (aOBRSeg.hasValue(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, "AH", 1)) {
            aPriority = "A";
        }
        if (aPV1Seg.hasValue(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "ED")) {
            aPatLoc = "E";
        } else {
            if (aPV1Seg.hasValue(HL7_23.PV1_2_patient_class, "I")) {
                aPatLoc = "I";
            }
        }

        CodeLookUp aLU = new CodeLookUp("PARIS_LOC_PRINTERS.table", mEnvironment);
        //Alfred Centre ward+bed lookup
        CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
        String aWard = aPV1Seg.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu) + "_" + aPV1Seg.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
        aWard = aLU_WARD.getValue(aWard);
        String aPatLocPrintMap = "";
        if (aWard.length() > 0) {
            aPatLocPrintMap = aLU.getValue(aWard);
        } else {
            aPatLocPrintMap = aLU.getValue(aPV1Seg.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu));
        }

        aLU = new CodeLookUp("PARIS_PRINTERS.table", mEnvironment);
        String aPARISPrinter = aLU.getValue(aPriority + aPatLoc + aServiceSection + aPatLocPrintMap);

        HL7Segment aOutZBXSegment = new HL7Segment("ZBX");
        aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
        aOutZBXSegment.set(HL7_24.ZBX_2_group, "ORDER");
        aOutZBXSegment.set(HL7_24.ZBX_3_field, "RADIOLOGY_PRINTER");
        aOutZBXSegment.set(HL7_24.ZBX_4_value, aPARISPrinter);
        aOutZBXSegment.set(HL7_24.ZBX_5_type, "ST");
        aOrderGroup.append(aOutZBXSegment);

        return aOrderGroup;
    }
    //--------------------------------------------------------------------------
    /**
     * Returns the Alert Group segment's (ORC, OBR, OBX, NTE) from the message.  No further processing of the segments
     * is performed.
     * @return Alert Group Segments segment/s (ORC, OBR, OBX, NTE).
     * @throws BHLibClasses.ICANException ICANException
     */
    public HL7Group processAlerts_ToUFD() throws ICANException {
        HL7Group aAlertGroup = new HL7Group();
        HL7Message aMessage = new HL7Message(mHL7Message);
        int i;
        int aCnt = aMessage.countGroups(CERNER_23.Group_Alerts);
        for (i = 1; i <= aCnt; i++) {
            aAlertGroup.append(aMessage.getGroup(CERNER_23.Group_Alerts,  i));
        }

        return aAlertGroup;
    }
    //--------------------------------------------------------------------------
    /**
     * Process a CERNER PID segment according to UFD requirements
     * @return Returns a PID segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Segment processPIDToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aInPIDSegment = new HL7Segment(k.NULL);
        HL7Segment aInOBXSegment = new HL7Segment(k.NULL);
        HL7Segment aOutPIDSegment = new HL7Segment(HL7_23.PID);

        String aTempField;
        HL7Field aPID_3Field = new HL7Field(k.NULL, k.REPEAT_GET, k.REPEAT_SET);

        HL7Field aPID_2 = new HL7Field(k.NULL);
        HL7Field aPID_3 = new HL7Field(k.NULL);
        HL7Field aPID_4 = new HL7Field(k.NULL);
        HL7Field aPID_19 = new HL7Field(k.NULL);
        HL7Field aPID_PBS = new HL7Field(k.NULL);

        String aRepeatOBXArray[] = {HL7_23.Repeat_OBX};
        int aCountOBXGroup;
        int aCount = 2;

        aInPIDSegment.setSegment(aHL7Message.getSegment(HL7_23.PID));
        if (aInPIDSegment.getSegment().length() == 0) {
            //Throw an exception for missing PID Segment.
            throw new ICANException("F004", mEnvironment);
        }
        aCountOBXGroup = aHL7Message.countGroups(aRepeatOBXArray);
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.copyFields();

        String aArray[] = { HL7_23.PID_2_patient_ID_external,
                HL7_23.PID_3_patient_ID_internal,
                HL7_23.PID_4_alternate_patient_ID,
                HL7_23.PID_19_SSN_number};

                aOutPIDSegment.clearFields(aArray);

                if (!aInPIDSegment.isEmpty(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number)) {
                    aTempField = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal);
                    aPID_3.setField(aTempField);
                    aPID_3.setSubField("PI", HL7_23.CX_ID_type_code);
                }

                if (!aInPIDSegment.isEmpty(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number)) {
                    aTempField = aInPIDSegment.get(HL7_23.PID_4_alternate_patient_ID);
                    aPID_4.setField(aTempField);
                    aPID_4.setSubField("PEN", HL7_23.CX_ID_type_code);
                    aPID_4.setSubField("PEN",  HL7_23.CX_assigning_authority);
                }

                if (!aInPIDSegment.isEmpty(HL7_23.PID_19_SSN_number, HL7_23.CX_ID_number)) {
                    aTempField = aInPIDSegment.get(HL7_23.PID_19_SSN_number);
                    aPID_19.setField(aTempField);
                    aPID_19.setSubField("MC", HL7_23.CX_ID_type_code);
                    aPID_19.setSubField("HIC",  HL7_23.CX_assigning_authority);
                    if (aPID_19.getSubField(HL7_23.CX_ID_number).indexOf(" ") == 0) {
                        String aCodeID = aPID_19.getSubField(HL7_23.CX_ID_number);
                        aCodeID = aCodeID.replaceAll(" ", "");
                        aPID_19.setSubField(aCodeID, HL7_23.CX_ID_number);
                    }
                }

                if (!aInPIDSegment.isEmpty(HL7_23.PID_2_patient_ID_external, HL7_23.CX_ID_number) &&
                        aInPIDSegment.get(HL7_23.PID_2_patient_ID_external, HL7_23.CX_ID_type_code).equalsIgnoreCase("VA")) {
                    aTempField = aInPIDSegment.get(HL7_23.PID_2_patient_ID_external);
                    aPID_2.setField(aTempField);
                    aPID_2.setSubField("VA", HL7_23.CX_ID_type_code);
                    aPID_2.setSubField("DVA", HL7_23.CX_assigning_authority);
                }

                for (int i = 1; i <= aCountOBXGroup; i++) {
                    HL7Group aOBXGroup = new HL7Group(aHL7Message.getGroup(aRepeatOBXArray, i));
                    aInOBXSegment.setSegment(aOBXGroup.getSegment(HL7_23.OBX));
                    if (aInOBXSegment.getField(HL7_23.OBX_3_observation_identifier).equalsIgnoreCase("PBSSAFETYNET")) {
                        aPID_PBS.setField(aInOBXSegment.getField(HL7_23.OBX_5_observation_value));
                        aPID_PBS.setSubField("PBS", HL7_23.CX_assigning_authority);
                        aPID_PBS.setSubField("PB", HL7_23.CX_ID_type_code);
                    }
                }

                aPID_3Field.setField(aPID_3.getField());
                if (aPID_4.getField().length() > 0) {
                    aPID_3Field.setSubField(aPID_4.getField(), aCount++);
                }
                if (aPID_19.getField().length() > 0) {
                    aPID_3Field.setSubField(aPID_19.getField(), aCount++);
                }
                if (aPID_2.getField().length() > 0) {
                    aPID_3Field.setSubField(aPID_2.getField(), aCount++);
                }
                if (aPID_PBS.getField().length() > 0) {
                    aPID_3Field.setSubField(aPID_PBS.getField(), aCount++);
                }

                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, aPID_3Field.getField());

                //Move PID-22 ethnic group to PID-23 birthplace
                aOutPIDSegment.set(HL7_23.PID_23_birth_place, aOutPIDSegment.get(HL7_23.PID_22_ethnic_group));
                aOutPIDSegment.set(HL7_23.PID_22_ethnic_group, k.NULL);

                //Check PID-11 for ..A28 prefix in street 2 sub-field
                String aStreet2Add = aInPIDSegment.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2);
                if (aStreet2Add.startsWith("..A28")) {
                    mA28Forced = true;
                    aStreet2Add = aStreet2Add.substring(5);
                    aOutPIDSegment.set(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, aStreet2Add);
                }

                //blank out PID18
                aOutPIDSegment.set(HL7_23.PID_18_account_number, "");

                return aOutPIDSegment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process a CERNER PID NTE segment according to UFD requirements
     * @return Returns a ZBX segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Group processPIDNTEZBXs_ToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Group aPIDNTEGroup = new HL7Group(k.NULL, 0);
        HL7Group aOutPIDNTEZBXGroup = new HL7Group(k.NULL, 0);
        HL7Segment aNTESegment = new HL7Segment(k.NULL);
        HL7Segment aZBXSegment = new HL7Segment(k.NULL);
        String aZBXValue = k.NULL;

        int aCountPIDNTEGroup = 0;

        aCountPIDNTEGroup = aHL7Message.countGroups(HL7_23.Group_PIDNotes);

        if (aCountPIDNTEGroup == 1) {
            aPIDNTEGroup.setGroup(aHL7Message.getGroup(HL7_23.Group_PIDNotes, aCountPIDNTEGroup));
            for(int i = 1; i <= aPIDNTEGroup.countSegments(CERNER_23.NTE); i++) {
                aNTESegment.setSegment(aPIDNTEGroup.getSegment(CERNER_23.NTE, i));
                aZBXSegment = new HL7Segment(HL7_24.ZBX);
                aZBXSegment.setField(Integer.toString(mZBXSeqNum++), HL7_24.ZBX_1_set_ID);
                aZBXSegment.setField("PMI", HL7_24.ZBX_2_group);
                aZBXSegment.setField("NOTE", HL7_24.ZBX_3_field);
                aZBXValue = aNTESegment.getField(CERNER_23.NTE_3_comment);
                aZBXValue = aZBXValue.concat(k.CARROT_SET).concat(aNTESegment.getField(CERNER_23.NTE_2_source_of_comment));
                aZBXSegment.setField(aZBXValue, HL7_24.ZBX_4_value);
                if (aOutPIDNTEZBXGroup.getGroup().equalsIgnoreCase(k.NULL)) {
                    aOutPIDNTEZBXGroup.setGroup(aZBXSegment.getSegment());
                } else {
                    aOutPIDNTEZBXGroup.append(aZBXSegment.getSegment());
                }
            }
        }

        return aOutPIDNTEZBXGroup;
    }
    //--------------------------------------------------------------------------
    /**
     * Process a CERNER PV1 segment according to UFD requirements
     * @return Returns a PV1 segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Segment processPV1ToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aInPV1Segment = new HL7Segment(k.NULL);
        HL7Segment aOutPV1Segment = new HL7Segment(k.NULL);

        aInPV1Segment.setSegment(aHL7Message.getSegment(HL7_23.PV1));
        aOutPV1Segment.linkTo(aInPV1Segment);
        aOutPV1Segment.copyFields();

// Alfred Centre code translation for Ward, Bed and Building
        CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
        CodeLookUp aLU_BED = new CodeLookUp("ALFCENTRE_BED.table", mEnvironment);
        //Current Location
        String aACWard = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
        String aACBed = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
        String aACWardBed = aACWard + "_" + aACBed;
        String aACBuilding = "";
        aACWard = aLU_WARD.getValue(aACWardBed);
        aACBed = aLU_BED.getValue(aACWardBed);
        //Prior Location
        String aACWardPrior = aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu);
        String aACBedPrior = aInPV1Segment.get(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed);
        String aACWardBedPrior = aACWardPrior + "_" + aACBedPrior;
        String aACBuildingPrior = "";
        aACWardPrior = aLU_WARD.getValue(aACWardBedPrior);
        aACBedPrior = aLU_BED.getValue(aACWardBedPrior);

        if (aACWard.length() > 0) {
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, aACWard);
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aACBed);
        }

        if (aACWardPrior.length() > 0) {
            aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_point_of_care_nu, aACWardPrior);
            aOutPV1Segment.set(HL7_23.PV1_6_prior_patient_location, HL7_23.PL_bed, aACBedPrior);
        }

        String aAttendingDoctorLastName = aOutPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name);
        String aAttendingDoctorIDNum = aOutPV1Segment.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num);
        String aPatientClass = aOutPV1Segment.get(HL7_23.PV1_2_patient_class);
        String aHospitalService = aOutPV1Segment.get(HL7_23.PV1_10_hospital_service);

        if ((aAttendingDoctorLastName.equalsIgnoreCase(k.NULL) ||
                aAttendingDoctorIDNum.indexOf("XX999") == 0) &&
                aPatientClass.equalsIgnoreCase("E")) {
            if (mFacility.equalsIgnoreCase("SDMH")) {
                aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, "TJ294");
            } else {
                aOutPV1Segment.set(HL7_23.PV1_7_attending_doctor, "FM264");
            }
        }

        if (mFacility.equalsIgnoreCase("CGMC")) {
            if (((aHospitalService.length() == 0) ||
                    aHospitalService.equalsIgnoreCase("\"\"")) &&
                    aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "CEM");
            }
        } else if (mFacility.equalsIgnoreCase("SDMH")) {
            if (((aHospitalService.length() == 0) ||
                    aHospitalService.equalsIgnoreCase("\"\"")) &&
                    aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "SED");
            }
        } else {
            if (((aHospitalService.length() == 0) ||
                    aHospitalService.equalsIgnoreCase("\"\"")) &&
                    aPatientClass.equalsIgnoreCase("E")) {
                aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "AEM");
            }
        }

//        if ((aHospitalService.equalsIgnoreCase(k.NULL) ||
//                aHospitalService.equalsIgnoreCase("\"\"")) &&
//                aPatientClass.equalsIgnoreCase("E")) {
//            aOutPV1Segment.set(HL7_23.PV1_10_hospital_service, "EMER");
//        }

        return aOutPV1Segment;
    }
    //--------------------------------------------------------------------------
    /**
     * Process a CERNER ZPI segment according to UFD requirements
     * @return Returns a ZBX segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Group processZPIZBXToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Group aOutZBXGroup = new HL7Group();
        HL7Segment aInZPISegment = new HL7Segment(k.NULL);
        HL7Segment aOutZBXSegment = new HL7Segment(k.NULL);
        HL7Segment aZBXSegment = new HL7Segment(k.NULL);
        String aZBXValue = k.NULL;
        String aXCNValue = k.NULL;

        aInZPISegment.setSegment(aHL7Message.getSegment(CERNER_23.ZPI));
        if (!aInZPISegment.get(CERNER_23.ZPI_10_Other_Provider_Person_Level, HL7_23.XCN_ID_num).equalsIgnoreCase(k.NULL)) {
            aZBXSegment = new HL7Segment(HL7_24.ZBX);
            aZBXSegment.setField(Integer.toString(mZBXSeqNum++), HL7_24.ZBX_1_set_ID);
            aZBXSegment.setField("VISIT", HL7_24.ZBX_2_group);
            aZBXSegment.setField("GPCODE", HL7_24.ZBX_3_field);
            aZBXValue = aInZPISegment.get(CERNER_23.ZPI_10_Other_Provider_Person_Level, HL7_23.XCN_ID_num);
            aZBXSegment.setField(aZBXValue, HL7_24.ZBX_4_value);
            aXCNValue = aInZPISegment.get(CERNER_23.ZPI_10_Other_Provider_Person_Level);
            aZBXSegment.setField(aXCNValue, HL7_24.ZBX_6_XCN_value);
            aOutZBXGroup.append(aZBXSegment);
        }

        aZBXValue = aInZPISegment.get(CERNER_23.ZPI_15_Species);
        aZBXSegment = new HL7Segment(HL7_24.ZBX);
        aZBXSegment.setField(Integer.toString(mZBXSeqNum++), HL7_24.ZBX_1_set_ID);
        aZBXSegment.setField("MESSAGE", HL7_24.ZBX_2_group);
        aZBXSegment.setField("NEW_PATIENT_FLAG", HL7_24.ZBX_3_field);
        aZBXSegment.setField(aZBXValue, HL7_24.ZBX_4_value);
        aOutZBXGroup.append(aZBXSegment);

        return aOutZBXGroup;
    }
    //--------------------------------------------------------------------------
    /**
     * Process a CERNER OBX segment according to UFD requirements
     * @return Returns a ZBX segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Group processOBXZBXs_ToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Group aOutOBXZBXGroup = new HL7Group(k.NULL, 0);
        HL7Segment aInOBXSegment = new HL7Segment(k.NULL);
        HL7Segment aZBXSegment = new HL7Segment(k.NULL);
        String aZBXValue = k.NULL;
        String aOBXID = k.NULL;
        String aZBXGroup = k.NULL;
        String aZBXFieldID = k.NULL;

        int aCountOBX = 0;

        aCountOBX = aHL7Message.countSegments(HL7_23.OBX);

        if (aCountOBX > 0) {
            for(int i = 1; i <= aCountOBX; i++) {
                aInOBXSegment.setSegment(aHL7Message.getSegment(HL7_23.OBX, i));
                aZBXGroup = k.NULL;
                aZBXFieldID = k.NULL;
                aOBXID = aInOBXSegment.get(HL7_23.OBX_3_observation_identifier);
                if (aOBXID.equalsIgnoreCase("MEDEXPDT")) {
                    aZBXGroup = "PMI";
                    aZBXFieldID = "MEDICARE_EXPIRY";
                } else if (aOBXID.equalsIgnoreCase("HCCPENSEXP")) {
                    aZBXGroup = "PMI";
                    aZBXFieldID = "PENSION_EXPIRY_DATE";
                } else if (aOBXID.equalsIgnoreCase("PBSSAFETYNET")) {
                    aZBXGroup = "PMI";
                    aZBXFieldID = "PBS_SAFETYNET_NUMBER";
                } else if (aOBXID.equalsIgnoreCase("DVACARDTYP")) {
                    aZBXGroup = "PMI";
                    aZBXFieldID = "DVA_CARD_TYPE";
                } else if (aOBXID.equalsIgnoreCase("INTERPREQ")) {
                    aZBXGroup = "PMI";
                    aZBXFieldID = "INTERPRETER";
                } else if (aOBXID.equalsIgnoreCase("OCCUPAT")) {
                    aZBXGroup = "PMI";
                    aZBXFieldID = "OCCUPATION";
                } else if (aOBXID.equalsIgnoreCase("EDCONNECT")) {
                    aZBXGroup = "VISIT";
                    aZBXFieldID = "ED_CONNECT_PATIENT";
                } else if (aOBXID.equalsIgnoreCase("TACWRKVR")) {
                    aZBXGroup = "FINANCE";
                    aZBXFieldID = "CLAIM_NUMBER";
                } else if (aOBXID.equalsIgnoreCase("MRLSTMOVDTTM")) {
                    aZBXGroup = "MEDREC";
                    aZBXFieldID = "LAST_MOVE_DATE_TIME";
                } else if (aOBXID.equalsIgnoreCase("MRVOLNBR")) {
                    aZBXGroup = "MEDREC";
                    aZBXFieldID = "VOLUME_NUMBER";
                } else if (aOBXID.equalsIgnoreCase("MRLOCN")) {
                    aZBXGroup = "MEDREC";
                    aZBXFieldID = "LOCATION";
                } else if (aOBXID.equalsIgnoreCase("MRRECDBY")) {
                    aZBXGroup = "MEDREC";
                    aZBXFieldID = "RECEIVED_BY";
                } else if (aOBXID.equalsIgnoreCase("MRRECVREXTN")) {
                    aZBXGroup = "MEDREC";
                    aZBXFieldID = "EXTENSION";
                } else if (aOBXID.equalsIgnoreCase("AMBCASENBR")) {
                    aZBXGroup = "VISIT";
                    aZBXFieldID = "AMBULANCE_CASE_NUMBER";
                } else if (aOBXID.equalsIgnoreCase("BIPAPCPAP")) {
                    aZBXGroup = "VISIT";
                    aZBXFieldID = "BIPAP";
                } else if (aOBXID.equalsIgnoreCase("UDPROBGRP")) {
                    aZBXGroup = "VISIT";
                    aZBXFieldID = "UD_PROBLEM_GROUP";
                } else if (aOBXID.equalsIgnoreCase("DOB_ACCURACY")) {
                    aZBXGroup = "PMI";
                    aZBXFieldID = "DOB_ACCURACY";
                }
                if (!aZBXGroup.equalsIgnoreCase(k.NULL) && !aZBXFieldID.equalsIgnoreCase(k.NULL)) {
                    aZBXSegment = new HL7Segment(HL7_24.ZBX);
                    aZBXSegment.setField(Integer.toString(mZBXSeqNum++), HL7_24.ZBX_1_set_ID);
                    aZBXSegment.setField(aZBXGroup, HL7_24.ZBX_2_group);
                    aZBXSegment.setField(aZBXFieldID, HL7_24.ZBX_3_field);
                    aZBXValue = aInOBXSegment.getField(HL7_23.OBX_5_observation_value);
                    aZBXSegment.setField(aZBXValue, HL7_24.ZBX_4_value);
                    if (aOutOBXZBXGroup.getGroup().equalsIgnoreCase(k.NULL)) {
                        aOutOBXZBXGroup.setGroup(aZBXSegment.getSegment());
                    } else {
                        aOutOBXZBXGroup.append(aZBXSegment.getSegment());
                    }
                }
            }
        }

        return aOutOBXZBXGroup;
    }
    //--------------------------------------------------------------------------
    /**
     * Process a CERNER OBX segment according to UFD requirements
     * @return Returns a OBX segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Group processOBXs_ToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Group aOutOBXGroup = new HL7Group(k.NULL, 0);
        HL7Segment aInOBXSegment = new HL7Segment(k.NULL);
        HL7Segment aOBXSegment = new HL7Segment(k.NULL);
        boolean aProcess = true;
        String aOBXID = k.NULL;

        int aCountOBX = 0;
        int aCountOBXProcess = 1;

        aCountOBX = aHL7Message.countSegments(HL7_23.OBX);

        if (aCountOBX > 0) {
            for(int i = 1; i <= aCountOBX; i++) {
                aInOBXSegment.setSegment(aHL7Message.getSegment(HL7_23.OBX, i));
                aProcess = true;
                aOBXID = aInOBXSegment.get(HL7_23.OBX_3_observation_identifier);
                if (aOBXID.equalsIgnoreCase("MEDEXPDT")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("HCCPENSEXP")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("PBSSAFETYNET")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("DVACARDTYP")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("INTERPREQ")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("OCCUPAT")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("EDCONNECT")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("TACWRKVR")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("MRLSTMOVDTTM")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("MRVOLNBR")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("MRLOCN")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("MRRECDBY")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("MRRECVREXTN")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("AMBCASENBR")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("BIPAPCPAP")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("UDPROBGRP")) {
                    aProcess = false;
                } else if (aOBXID.equalsIgnoreCase("DOB_ACCURACY")) {
                    aProcess = false;
                }
                if (aProcess == true) {
                    aOBXSegment.setSegment(aInOBXSegment.getSegment());
                    aOBXSegment.setField(Integer.toString(aCountOBXProcess++), HL7_23.OBX_1_set_ID);
                    if (aOutOBXGroup.getGroup().equalsIgnoreCase(k.NULL)) {
                        aOutOBXGroup.setGroup(aOBXSegment.getSegment());
                    } else {
                        aOutOBXGroup.append(aOBXSegment.getSegment());
                    }
                }
            }
        }

        return aOutOBXGroup;
    }
    //--------------------------------------------------------------------------
    /**
     * Process a CERNER GT1 segment according to UFD requirements
     * @return Returns a GT1 segment/s
     * @throws BHLibClasses.ICANException ICANException Exception class object
     */
    public HL7Group processGT1s_ToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Group aOutGT1Group = new HL7Group(k.NULL, 0);
        HL7Segment aInGT1Segment = new HL7Segment(k.NULL);
        HL7Segment aGT1Segment = new HL7Segment(k.NULL);
        HL7Segment aPV1Segment = new HL7Segment(k.NULL);
        HL7Segment aZEISegment = new HL7Segment(k.NULL);
        boolean aProcess = true;
        String aPV1FinClass = k.NULL;
        CodeLookUp aLookup = new CodeLookUp("DefaultGuarantors.table", mEnvironment);

        int aCountGT1 = 0;
        int aCountGT1Process = 1;

        String aLastName = k.NULL;
        String aStreet1 = k.NULL;
        String aStreet2 = k.NULL;
        String aStateProvince = k.NULL;
        String aCity = k.NULL;
        String aZIP = k.NULL;
        String aPhone = k.NULL;
        String aCountry = k.NULL;
        String aGuarantorType = k.NULL;

        aCountGT1 = aHL7Message.countSegments(HL7_23.GT1);
        aPV1Segment.setSegment(aHL7Message.getSegment(HL7_23.PV1));
        aZEISegment.setSegment(aHL7Message.getSegment(CERNER_23.ZEI));
        aPV1FinClass = aPV1Segment.get(HL7_23.PV1_20_financial_class, HL7_23.FC_finacial_class);

        if (aCountGT1 > 0) {
            for(int i = 1; i <= aCountGT1; i++) {
                aInGT1Segment.setSegment(aHL7Message.getSegment(HL7_23.GT1, i));
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
                } else if (aPV1FinClass.equalsIgnoreCase("W/C")) {
                    aLastName = aZEISegment.get(CERNER_23.ZEI_3_Employer_Name_free_text_only);
                    aStreet1 = aZEISegment.get(CERNER_23.ZEI_4_Employer_Address, HL7_23.XAD_street_1);
                    aStreet2 = aZEISegment.get(CERNER_23.ZEI_4_Employer_Address, HL7_23.XAD_street_2);
                    aCity = aZEISegment.get(CERNER_23.ZEI_4_Employer_Address, HL7_23.XAD_city);
                    aStateProvince = aZEISegment.get(CERNER_23.ZEI_4_Employer_Address, HL7_23.XAD_county_parish);
                    aZIP = aZEISegment.get(CERNER_23.ZEI_4_Employer_Address, HL7_23.XAD_zip);
                    aCountry = aZEISegment.get(CERNER_23.ZEI_4_Employer_Address, HL7_23.XAD_country);
                    aPhone = aZEISegment.get(CERNER_23.ZEI_5_Employer_Phone, HL7_23.XTN_telephone_number);
                    aGuarantorType = "E";
                } else {
                    aProcess = false;
                }
                if (aProcess == true) {
                    aGT1Segment.setSegment(aInGT1Segment.getSegment());
                    aGT1Segment.set(HL7_23.GT1_1_set_ID, Integer.toString(aCountGT1Process++));
                    aGT1Segment.set(HL7_23.GT1_2_guarantor_number, "");
                    aGT1Segment.set(HL7_23.GT1_3_guarantor_name, aLastName);
                    aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aStreet1);
                    aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aStreet2);
                    aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aCity);
                    aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_county_parish, aStateProvince);
                    aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aZIP);
                    aGT1Segment.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_country, aCountry);
                    aGT1Segment.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aPhone);
                    aGT1Segment.set(HL7_23.GT1_10_guarantor_type, aGuarantorType);
                    aGT1Segment.set(HL7_23.GT1_11_guarantor_relationship, "");
                    aGT1Segment.set(HL7_23.GT1_12_guarantor_SSN, "");
                    if (aOutGT1Group.getGroup().equalsIgnoreCase(k.NULL)) {
                        aOutGT1Group.setGroup(aGT1Segment.getSegment());
                    } else {
                        aOutGT1Group.append(aGT1Segment.getSegment());
                    }
                } else {
                    aInGT1Segment.set(HL7_23.GT1_1_set_ID, Integer.toString(aCountGT1Process++));
                    if (aOutGT1Group.getGroup().equalsIgnoreCase(k.NULL)) {
                        aOutGT1Group.setGroup(aInGT1Segment.getSegment());
                    } else {
                        aOutGT1Group.append(aInGT1Segment.getSegment());
                    }
                }
            }
        }

        return aOutGT1Group;
    }
    //--------------------------------------------------------------------------
    /**
     * Process a CERNER Financial group according to UFD requirements
     * @return Financial group consisting of FT1 and ZBX segments
     * @throws BHLibClasses.ICANException ICANException
     */
    public HL7Group processFinGroups_ToUFD() throws ICANException {
        String aFinGroupID[] = CERNER_23.Group_FinSet;
        int aFinGroupCount = mInHL7Message.countGroups(aFinGroupID);
        HL7Group aInFinGroup;
        HL7Group aOutFinGroup = new HL7Group();
        HL7Group aOutFT1Group = new HL7Group();
        HL7Group aOutZBXGroup = new HL7Group();
        HL7Segment aOutFT1Segment;
        HL7Segment aOutZBXSegment;
        HL7Segment aInFT1Segment = new HL7Segment("FT1");
        HL7Segment aInZFTSegment = new HL7Segment("ZBX");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment(HL7_23.PV1));

        CodeLookUp aLU = new CodeLookUp("DefaultValues.table", mEnvironment);
        String aDefaultDr = aLU.getValue("PharmacyDr");
        String aTransCode = aLU.getValue("EMGItemCode");
        String aEmergencyDr = aLU.getValue("EmergencyDr");
        String aOutPatientDr = aLU.getValue("OutpatientDr");

        for (int i = 1; i <= aFinGroupCount; i++) {
            aInFinGroup = new HL7Group(mInHL7Message.getGroup(aFinGroupID, i));
            aInFT1Segment.setSegment(aInFinGroup.getSegment("FT1"));
            aInZFTSegment.setSegment(aInFinGroup.getSegment("ZFT"));
            if (aInFT1Segment.getSegment().length() > 0) {
                aOutFT1Segment = new HL7Segment("FT1");
                aOutFT1Segment.set(HL7_23.FT1_4_Transaction_Date, aInFT1Segment.get(HL7_23.FT1_4_Transaction_Date));
                aOutFT1Segment.set(HL7_23.FT1_6_Transaction_Type, aInFT1Segment.get(HL7_23.FT1_6_Transaction_Type));

                String aTransIDCode = aInFT1Segment.get(HL7_23.FT1_7_Transaction_Code, HL7_23.CE_ID_code);
                if (aTransIDCode.equalsIgnoreCase("EMG")) {
                    aOutFT1Segment.set(HL7_23.FT1_7_Transaction_Code, HL7_23.CE_ID_code, aTransCode);
                } else {
                    aOutFT1Segment.set(HL7_23.FT1_7_Transaction_Code, HL7_23.CE_ID_code,
                            aInFT1Segment.get(HL7_23.FT1_7_Transaction_Code, HL7_23.CE_ID_code));
                }

                //Alfred Centre Ward lookup
                CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
                String aWard = aLU_WARD.getValue(aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu) + "_" + aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed));
                if (aWard.length() > 0) {
                    aOutFT1Segment.set(HL7_23.FT1_16_Assigned_Patient_Location, HL7_23.PL_point_of_care_nu, aWard);
                } else {
                    aOutFT1Segment.set(HL7_23.FT1_16_Assigned_Patient_Location, HL7_23.PL_point_of_care_nu,
                            aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu));
                }

                String aPerformedIDCode = aInFT1Segment.get(HL7_23.FT1_20_Performed_by_Code, HL7_23.XCN_ID_num);
                if (aPerformedIDCode.equalsIgnoreCase("EMG")) {
                    aOutFT1Segment.set(HL7_23.FT1_20_Performed_by_Code, HL7_23.XCN_ID_num, aEmergencyDr);
                } else {
                    if (aPerformedIDCode.length() == 0) {
                        String aOrderedIDCode = aInFT1Segment.get(HL7_23.FT1_21_Ordered_by_Code, HL7_23.XCN_ID_num);
                        if (aOrderedIDCode.length() == 0) {
                            //do nothing
                        } else {
                            aOutFT1Segment.set(HL7_23.FT1_20_Performed_by_Code, HL7_23.XCN_ID_num, aOrderedIDCode);
                        }
                    } else {
                        aOutFT1Segment.set(HL7_23.FT1_20_Performed_by_Code, HL7_23.XCN_ID_num,
                                aInFT1Segment.get(HL7_23.FT1_20_Performed_by_Code, HL7_23.XCN_ID_num));
                    }
                    aOutFT1Segment.set(HL7_23.FT1_21_Ordered_by_Code, HL7_23.XCN_ID_num,
                            aInPV1Segment.get(HL7_23.PV1_8_referring_doctor, HL7_23.XCN_ID_num));
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
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "UPDATE_TYPE");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, "IN");
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "ASSIGNMENT_FLAG");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, "N");
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "REQUESTED_DATE");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aInZFTSegment.get(CERNER_23.ZFT_6_Requested_Date));
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "REFERRAL_DATE");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aInZFTSegment.get(CERNER_23.ZFT_7_Referral_Date));
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "BILLING_DOCTOR");
                String aBillingDr = aInZFTSegment.get(CERNER_23.ZFT_8_Billing_Doctor);
                if (aBillingDr.equalsIgnoreCase("EMG")) {
                    aOutZBXSegment.set(HL7_24.ZBX_4_value, aEmergencyDr);
                } else {
                    aOutZBXSegment.set(HL7_24.ZBX_4_value, aOutPatientDr);
                }
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "PRINT_FLAG");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aInZFTSegment.get(CERNER_23.ZFT_13_Print_Flag));
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "INVOICE_DATE");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aOutZBXSegment.getDateTime("yyyyMMddHHmm"));
                aOutZBXSegment.set(HL7_24.ZBX_5_type, aFT1SetID);
                aOutZBXGroup.append(aOutZBXSegment);

                aOutZBXSegment = new HL7Segment("ZBX");
                aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
                aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
                aOutZBXSegment.set(HL7_24.ZBX_3_field, "CLINIC_NAME");
                String aClinicName = aInZFTSegment.get(CERNER_23.ZFT_15_Clinic_Description);
                if (aClinicName.length() > 0) {
                    aOutZBXSegment.set(HL7_24.ZBX_4_value, aClinicName);
                } else {
                    String aPointOfCare = aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location,
                            HL7_23.PL_point_of_care_nu);

                    //Alfred Centre Ward code lookup
                    CodeLookUp aLU_WARD = new CodeLookUp("ALFCENTRE_WARD.table", mEnvironment);
                    String aWard = aLU_WARD.getValue(aPointOfCare + "_" + aInPV1Segment.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed));
                    if (aWard.length() > 0) {
                        aOutZBXSegment.set(HL7_24.ZBX_4_value, aWard);
                    } else {
                        aOutZBXSegment.set(HL7_24.ZBX_4_value, aPointOfCare);
                    }
                }
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
     * Process a CERNER ZEI segment according to UFD requirements
     * @return ZBX segment
     * @throws BHLibClasses.ICANException ICANException
     */
    public HL7Group processZEIZBXToUFD() throws ICANException {
        HL7Segment aInZEISegment = new HL7Segment(mInHL7Message.getSegment(CERNER_23.ZEI));
        HL7Segment aOutZBXSegment = new HL7Segment(k.NULL);
        HL7Group aOutZBXGroup = new HL7Group(k.NULL);

        if (aInZEISegment.getSegment().length() > 0) {
            aOutZBXSegment = new HL7Segment("ZBX");
            aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
            aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
            aOutZBXSegment.set(HL7_24.ZBX_3_field, "EMPLOYER_NAME");
            aOutZBXSegment.set(HL7_24.ZBX_4_value, aInZEISegment.get(CERNER_23.ZEI_3_Employer_Name_free_text_only));
            aOutZBXGroup.append(aOutZBXSegment);

            aOutZBXSegment = new HL7Segment("ZBX");
            aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
            aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
            aOutZBXSegment.set(HL7_24.ZBX_3_field, "EMPLOYER_ADDRESS");
            aOutZBXSegment.set(HL7_24.ZBX_4_value, aInZEISegment.get(CERNER_23.ZEI_4_Employer_Address));
            aOutZBXGroup.append(aOutZBXSegment);
        }
        return aOutZBXGroup;
    }
    //--------------------------------------------------------------------------
    /**
     * Process a CERNER ZCD group according to UFD requirements
     * @return ZBX group
     * @throws BHLibClasses.ICANException ICANException
     */
    public HL7Group processZCDZBXs_ToUFD() throws ICANException {
        HL7Segment aInZCDSegment = new HL7Segment(mInHL7Message.getSegment("ZCD"));
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        HL7Segment aInZEISegment = new HL7Segment(mInHL7Message.getSegment("ZEI"));
        HL7Segment aOutZBXSegment = new HL7Segment(k.NULL);
        HL7Group aOutZBXGroup = new HL7Group();
        if (aInZCDSegment.getSegment().length() > 0) {
            aOutZBXSegment = new HL7Segment("ZBX");
            aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
            aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
            aOutZBXSegment.set(HL7_24.ZBX_3_field, "CLAIM_NUMBER");
            String aClaimNum = aInZCDSegment.get("ZCD_10");
            aOutZBXSegment.set(HL7_24.ZBX_4_value, aClaimNum);
            aOutZBXGroup.append(aOutZBXSegment);

            String aFinClass = aInPV1Segment.get(HL7_23.PV1_20_financial_class);

            aOutZBXSegment = new HL7Segment("ZBX");
            aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
            aOutZBXSegment.set(HL7_24.ZBX_2_group, "FINANCE");
            aOutZBXSegment.set(HL7_24.ZBX_3_field, "ASSIGN_BILL_NUMBER");
            if (aFinClass.equalsIgnoreCase("TAC")) {
                CodeLookUp aLUDefaultGT = new CodeLookUp("DefaultGuarantors.table", mEnvironment);
                String aTACValue = aLUDefaultGT.getValue("TAC8");
                aOutZBXSegment.set(HL7_24.ZBX_4_value, aTACValue);
            } else if (aFinClass.equalsIgnoreCase("W/C") &&
                    aInZEISegment.get(CERNER_23.ZEI_3_Employer_Name_free_text_only).length() > 0) {
                aOutZBXSegment.set(HL7_24.ZBX_4_value, "E");
            } else {
                CodeLookUp aLUCompensableClass = new CodeLookUp("ValidateBillingCompensableClass.table", mEnvironment);
                aFinClass = aLUCompensableClass.getValue(aFinClass);
                if (aFinClass.length() == 0) {
                    aOutZBXSegment.set(HL7_24.ZBX_4_value, "P");
                } else {
                    aOutZBXSegment.set(HL7_24.ZBX_4_value, "A");
                }
            }
            aOutZBXGroup.append(aOutZBXSegment);
        }
        return aOutZBXGroup;
    }
    public HL7Segment processA28ZBXToUFD() throws ICANException {
        HL7Segment aOutZBXSegment = new HL7Segment("");
        if (mA28Forced == true) {
            aOutZBXSegment = new HL7Segment(HL7_24.ZBX);
            aOutZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString(mZBXSeqNum++));
            aOutZBXSegment.set(HL7_24.ZBX_2_group, "MESSAGE");
            aOutZBXSegment.set(HL7_24.ZBX_3_field, "A28_FLAG");
            aOutZBXSegment.set(HL7_24.ZBX_4_value, "TRUE");
        }
        return aOutZBXSegment;
    }
}
