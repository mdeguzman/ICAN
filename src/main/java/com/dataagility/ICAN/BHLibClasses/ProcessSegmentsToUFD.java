/*
 * ProcessFromMSH.java
 *
 * Created on 3 October 2005, 14:27
 *
 */

package com.dataagility.ICAN.BHLibClasses;

/**
 * ProcessSegmentsToUFD provides segment processing for inbound and outbound messages.
 * The methods are generic pass through processes and are not dependent on the
 * receiving or sending systems.
 *
 * Authors: Ray Fillingham and Norman Soh
 * Organisation: The Alfred
 * Year: 2005
 * @author sohn
 */
public class ProcessSegmentsToUFD {
    /**
     * The Version Number of the Relesae.
     * * This is passed to MSH_10 Message Control ID by the getVerDateTime() method.
     */
    public String mVersion = "";

    /**
     * Constant class
     */
    public BHConstants k = new BHConstants();
    /**
     * The HL7 Message to be processed
     */
    public String mHL7Message;
    /**
     * Holds the contents of MSH_9_2 Message Type.<p>
     * Typical values are ... "A01" or "A02" or A28" or "P03" or RO1" ect.
     */
    protected String mHL7MessageEvent;
    /**
     * Holds the current HL7 Message being processed.
     */
    protected HL7Segment mHL7Segment = new HL7Segment(k.NULL);
    /**
     * Holds the Facility associated with the current message being processed.<p>
     * e.g. "ALF", "CGMC", "SDMH", "BHH", "PJC", "MAR", "ANG".
     */
    protected String mFacility = "";         // Note of which Campus/Facility we are handling (i.e. "ALF", "CGMC", SDMH").
    /**
     * Holds a single char code associated with the Facility for the current message being processed.<p>
     * This is normally the 1st letter of the Facility ID ... but not always!!<p>
     * <p>
     * Normally used to prefix other values to make them unique within a destination system.<p>
     * Examples of usage are withe UR numbers, Locations and Units when the destination system is a common Database for more than one facility.
     * <p>
     */
    protected String mHospitalID = "";       // This is a single char code that is based on the campus
    //   (i.e "A", "C", "S" ... but not always!!).

    /**
     * Counter used for Segment Identification when creating a list of OBX's.
     */
    protected int mOBXSegmentCount = 0;      // Counter used when assembling OBX segments.
    /**
     * Counter used for Segment Identification when creating a list of ZBX's.
     */
    protected int mZBXSegmentCount = 1;      // Counter used when assembling ZBX segments.
    /**
     ** Note of the current Patients UR number
     */
    protected String mPatientUR = "";
    /**
     * This constructor initialises a new instance of
     * ProcessSegmentsToUFD with a HL7 Message
     * @param pHL7Message HL7 Message
     */
    public ProcessSegmentsToUFD(String pHL7Message) throws ICANException {
        mHL7Message = pHL7Message;
        mZBXSegmentCount = 1;
        mOBXSegmentCount = 0;
        mPatientUR = "";
    }
    /**
     * Generic group processing
     * @param pGroupID Group identifier
     * @return Returns a group text string
     */
    public HL7Group processGroup(String pGroupID[]) throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Group aGroup = new HL7Group(k.NULL, 0);
        int aGroupCount;
        int aCount = 1;

        String aGroupID[] = pGroupID;
        aGroupCount = aHL7Message.countGroups(aGroupID);

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            aGroup.append(aHL7Message.getGroup(aGroupID, aCount));
        }
        return aGroup;
    }
    /**
     * Generic group processing
     * @param pGroupID Group identifier
     * @return Returns a group text string
     */
    public HL7Group processGroup(String pGroupID) throws ICANException {
        String aGroupID[] = {pGroupID};
        return processGroup(aGroupID);
    }
    /**
     * Generic processing for an Outgoing Group in an A17 message.<p>
     * @return Returns the processed HL7 PID group as a HL7Group.
     */
    public HL7Group processA17GroupToUFD( int pGroupNumber) throws ICANException {

        HL7Message aInMessage = new HL7Message(mHL7Message) ;
        String aGroup = aInMessage.getGroup(HL7_23.Group_A17_Patient, pGroupNumber);
        HL7Group aOutGroup = new HL7Group();

        HL7Segment aOutPID = processPIDToUFD(aGroup);
        aOutGroup.append(aOutPID.getSegment());

        HL7Segment aOutPV1 = processPV1ToUFD(aGroup);
        aOutGroup.append(aOutPV1.getSegment());

        return aOutGroup;

    }
    /**
     * Returns the ACC segment/s from the message.  No further processing of the segment
     * is performed.
     * @return ACC segment/s
     */
    public HL7Group processACCs_ToUFD() throws ICANException {
        return processGroup(HL7_23.Repeat_ACC);
    }
    /**
     * Returns the AL1 segment/s from the message.  No further processing of the segment
     * is performed.
     * @return AL1 segment/s
     */
    public HL7Group processAL1s_ToUFD() throws ICANException {
        return processGroup(HL7_23.Repeat_AL1);
    }
    /**
     * Returns the DG1 segment/s from the message.  No further processing of the segment
     * is performed.
     * @return DG1 segment/s
     */
    public HL7Group processDG1s_ToUFD() throws ICANException {
        return processGroup(HL7_23.Repeat_DG1);
    }
    /**
     * Returns the DRG segment from the message.  No further processing of the segment
     * is performed.
     * @return DRG segment
     */
    public HL7Segment processDRGToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aDRGSegment = new HL7Segment(k.NULL);

        aDRGSegment.setSegment(aHL7Message.getSegment(HL7_23.DRG));

        return aDRGSegment;
    }
    /**
     * Returns the EVN segment from the message.  No further processing of the segment
     * is performed.
     * @return EVN segment
     */
    public HL7Segment processEVNToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aEVNSegment = new HL7Segment(k.NULL);

        aEVNSegment.setSegment(aHL7Message.getSegment(HL7_23.EVN));

        return aEVNSegment;
    }
    /**
     * Returns the GT1 segment/s from the message.  No further processing of the segment
     * is performed.
     * @return GT1 segment/s
     */
    public HL7Group processGT1s_ToUFD() throws ICANException {
        return processGroup(HL7_23.Repeat_GT1);
    }
    /**
     * Returns the MSH segment from the message.  No further processing of the segment
     * is performed.
     * @return MSH segment
     */
    public HL7Segment processMSHToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aOutMSHSegment = new HL7Segment(k.NULL);
        aOutMSHSegment.setSegment(aHL7Message.getSegment(HL7_23.MSH));
        String aMessageType = aOutMSHSegment.get(HL7_23.MSH_9_2_trigger_event);
        String aOriginatingSys = aOutMSHSegment.get(HL7_23.MSH_3_sending_application);
        if (aOriginatingSys.indexOf("CERNER") >= 0) {
            aOriginatingSys = "C";
        } else if (aOriginatingSys.indexOf("FIRSTNET") >= 0) {
            aOriginatingSys = "C";
        } else if(aOriginatingSys.indexOf("CARENET") >= 0) {
            aOriginatingSys = "C";
        } else if (aOriginatingSys.indexOf("CSC") >= 0) {
            aOriginatingSys = "H";
        } else {
            aOriginatingSys = aOriginatingSys.substring(0, 1);
        }
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aOriginatingSys.concat(aMessageType).concat(aOutMSHSegment.getVerDateTime(mVersion)));
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        mFacility = aOutMSHSegment.getField(HL7_23.MSH_4_sending_facility);
        mHL7Message = aHL7Message.getMessage();
        mHL7MessageEvent = aHL7Message.getTriggerEvent();
        return aOutMSHSegment;
    }
    /**
     * Returns the NK1 segment/s from the message.  No further processing of the segment
     * is performed.
     * @return NK1 segment/s
     */
    public HL7Group processNK1s_ToUFD() throws ICANException {
        return processGroup(HL7_23.Repeat_NK1);
    }
    /**
     * Returns the OBX segment/s from the message.  No further processing of the segment
     * is performed.
     * @return OBX segment/s
     */
    public HL7Group processOBXs_ToUFD() throws ICANException {
        return processGroup(HL7_23.Repeat_OBX);
    }
    /**
     * Generic processing for an Incoming PID Segment contained in the HL7 Message.<p>
     * @return Returns the processed HL7 PID as an HL7Segment.
     */
    public HL7Segment processPIDToUFD() throws ICANException {
        return (this.processPIDToUFD(mHL7Message));
    }
    /**
     * Generic processing for an Incoming PID Segment contained in [pHL7MessageBlock].<p>
     * @return The processed HL7 PID segment as an HL7Segment.
     * @param pHL7MessageBlock A block of segments containing a PID segment ... e.g. aGroup.
     */
    public HL7Segment processPIDToUFD(String pHL7MessageBlock) throws ICANException {

        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aInPID = new HL7Segment(k.NULL);
        HL7Segment aOutPID = new HL7Segment(HL7_23.PID);

        HL7Field aTempField;
        String aPID_3_RepeatField[] = new String[5];
        int aRepeat = 0;
        aInPID.setSegment(aHL7Message.getSegment(HL7_23.PID));

// Copy all fields from IN segment ...
        aOutPID.linkTo(aInPID);
        aOutPID.copyFields();

// ... and clean out any we need to fix up ...
        String aArray[] = { HL7_23.PID_2_patient_ID_external,
                HL7_23.PID_3_patient_ID_internal,
                HL7_23.PID_4_alternate_patient_ID,
                HL7_23.PID_19_SSN_number,
                HL7_23.PID_27_veterans_military_status};
                aOutPID.clearFields(aArray);

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

// Pension Number
                if (! aInPID.isEmpty(HL7_23.PID_4_alternate_patient_ID, HL7_23.CX_ID_number)) {
                    aTempField = new HL7Field(aInPID.get(HL7_23.PID_4_alternate_patient_ID));
                    aTempField.setSubField("PEN",HL7_23.CX_ID_type_code );
                    aTempField.setSubField("PEN",HL7_23.CX_assigning_authority  );
                    aPID_3_RepeatField[aRepeat++] = aTempField.getField();
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
                if (!aInPID.isEmpty(HL7_23.PID_2_patient_ID_external, HL7_23.CX_ID_number) &&
                        !aInPID.get(HL7_23.PID_2_patient_ID_external).equalsIgnoreCase("\"\"")) {
                    aTempField = new HL7Field(aInPID.get(HL7_23.PID_27_veterans_military_status, HL7_23.CX_ID_number));
                    aTempField.setSubField("VA",HL7_23.CX_ID_type_code );
                    aTempField.setSubField("DVA",HL7_23.CX_assigning_authority  );
                    aPID_3_RepeatField[aRepeat++] = aTempField.getField();
                }

                aOutPID.setRepeatFields(HL7_23.PID_3_patient_ID_internal, aPID_3_RepeatField);

                return aOutPID;
    }
    /**
     * Returns the PR1 segment/s from the message.  No further processing of the segment
     * is performed.
     * @return PR1 segment/s
     */
    public HL7Group processPR1s_ToUFD() throws ICANException {
        return processGroup(HL7_23.Repeat_PR1);
    }
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
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aPV1Segment = new HL7Segment(k.NULL);

        aPV1Segment.setSegment(aHL7Message.getSegment(HL7_23.PV1));
        return aPV1Segment;
    }
    /**
     * Returns the PV2 segment from the message.  No further processing of the segment
     * is performed.
     * @return PV2 segment
     */
    public HL7Segment  processPV2ToUFD() throws ICANException {
        return (this. processPV2ToUFD(mHL7Message));
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

        return aPV2Segment;
    }
    /**
     * Returns the Observations group segments from the message.  No further processing
     * of the segment is performed.
     * @return Observation Group segments from the mHL7Message
     */
    public HL7Group processObservationsToUFD() throws ICANException {
        return processGroup(HL7_23.Group_Observation_Details);
    }
    /**
     * Returns the Insurance group segment from the message.  No further processing of the segment
     * is performed.
     * @return Insurance group of segments
     */
    public HL7Group processInsuranceToUFD() throws ICANException {
        return processGroup(HL7_23.Group_Insurance);
    }
    /**
     * Returns the MRG segment from the message.  No further processing of the segment
     * is performed.
     * @return MRG segment
     */
    public HL7Segment processMRGToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aMRGSegment = new HL7Segment(k.NULL);

        aMRGSegment.setSegment(aHL7Message.getSegment(HL7_23.MRG));

        return aMRGSegment;
    }
    /**
     * Returns the SecondPatient group segment from the message.  No further processing of the segment
     * is performed.
     * @return SecondPatient group of segments
     */
    public HL7Group processSecondPatientToUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Group aSecondPatientGroup = new HL7Group(k.NULL, 0);
        int aGroupCount;
        int aCount = 1;

        String aGroupID[] = HL7_23.Group_A17_Patient;
        aGroupCount = aHL7Message.countGroups(aGroupID);

        if (aGroupCount == 2) {
            aSecondPatientGroup.append(aHL7Message.getGroup(aGroupID, 2));
        }
        return aSecondPatientGroup;
    }

    /**
     * Creates aZBX segment.
     * @param aGroupID Identification of which ZBX group the data belongs to.<p>
     * Valid Groups are ....<p>
     * "PMI", "VISIT", "FINANCE", "MEDREC", "ORDER", "MESSAGE".
     * @param aItemID The Item code indicating what the pValue is within the indicated pGroup.
     * @param aValue The actual value to be held in the ZBX_4_value field.
     * @param aXCNValue The actual Dr Field details to be held in the ZBX_6_value field.
     * @return Returns a segment text string
     */
    public HL7Segment setupZBX(String aGroupID, String aItemID, String aValue, HL7Field aXCNValue) throws ICANException {
        HL7Segment aZBXSegment = new HL7Segment(HL7_24.ZBX);
        aZBXSegment = setupZBX(aGroupID, aItemID,aValue);
        aZBXSegment.set(HL7_24.ZBX_6_XCN_value, aXCNValue.getField());
        return aZBXSegment;
    }
    public HL7Segment setupZBX(String aGroupID, String aItemID, String aValue) throws ICANException {

        HL7Segment aZBXSegment = new HL7Segment(HL7_24.ZBX);
        aZBXSegment.set(HL7_24.ZBX_1_set_ID, Integer.toString( mZBXSegmentCount++));
        aZBXSegment.set(HL7_24.ZBX_2_group, aGroupID);
        aZBXSegment.set(HL7_24.ZBX_3_field, aItemID);
        aZBXSegment.set(HL7_24.ZBX_4_value, aValue);
        return aZBXSegment;
    }
    /**
     * Creates aZBX segment.
     * is performed.
     * @param pGroupID Identification of which ZBX group the data belongs to.<p>
     * Valid Groups are ....<p>
     * "PMI", "VISIT", "FINANCE", "MEDREC", "ORDER", "MESSAGE".
     * @param pItemID The Item code indicating what the pValue is within the indicated pGroup.
     * @param pValue The actual value to be held in this ZBX_6_XCN_Value field.
     * @return Return a segment text string
     */
    public HL7Segment setupZBX(String pGroupID, String pItemID, HL7Field pValue) throws ICANException {

        HL7Segment aZBXSegment = new HL7Segment(HL7_24.ZBX);
        aZBXSegment.setField(Integer.toString( mZBXSegmentCount++), HL7_24.ZBX_1_set_ID);
        aZBXSegment.setField(pGroupID, HL7_24.ZBX_2_group);
        aZBXSegment.setField(pItemID, HL7_24.ZBX_3_field);
        aZBXSegment.setField(pValue.getField(), HL7_24.ZBX_6_XCN_value);

        return aZBXSegment;
    }
    public boolean hasValue(String pFieldID, String pCompareValue) {
        boolean aResult = false;

        String aCompareValue = pCompareValue;
        HL7Message aMsg = new HL7Message(mHL7Message);

        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        HL7Segment aNewSeg = new HL7Segment(aMsg.getSegment(aFD.ID));
        String aFieldValue = aNewSeg.getField(aFD.fieldNum);
        if (aFD.subFieldNum != 0) {
            HL7Field aField = new HL7Field(aFieldValue);
            aFieldValue = aField.getItem(aFD.subFieldNum);
        }
        if (aFieldValue.matches(aCompareValue)) {
            aResult = true;
        }

        return aResult;
    }

    public boolean hasValue(String pFieldID, String pFieldType, String pCompareValue) {
        boolean aResult = false;

        String aCompareValue = pCompareValue;
        HL7Message aMsg = new HL7Message(mHL7Message);

        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        HL7Segment aNewSeg = new HL7Segment(aMsg.getSegment(aFD.ID));
        String aFieldValue = aNewSeg.get(pFieldID, pFieldType);

        if (aFieldValue.matches(aCompareValue)) {
            aResult = true;
        }

        return aResult;
    }

}
