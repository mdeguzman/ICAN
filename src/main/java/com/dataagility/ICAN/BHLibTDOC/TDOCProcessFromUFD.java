/*
 * TDOCProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.dataagility.ICAN.BHLibTDOC;
import com.dataagility.ICAN.BHLibClasses.*;
//import com.dataagility.ICAN.BHLibCSC.*;
//import com.dataagility.ICAN.BHLibSQL.*;

/**
 *
 * @author fillinghamr
 */
public class TDOCProcessFromUFD extends ProcessSegmentsFromUFD {

    public String mEnvironment = "";
    /**
     * Creates a new instance of TDOCProcessFromUFD
     */
    public TDOCProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
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
        HL7Segment aPV1Segment = new HL7Segment(aInMess.getSegment(HL7_23.PV1));
//
// Only accept Inpatient or Emergency encounter message types ...
        if (aInMess.isEvent("A01, A02, A03, A04, A08") &&
                super.hasValue(HL7_23.MSH_9_1_message_type,"ADT") &&
                   (aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"I") ||
                     aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"E"))) {
            aOutMess.setSegment(processMSHFromUFD("TDOC").getSegment());
            aOutMess.append(processPIDFromUFD());
            aSQLMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aSQLMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aSQLMessageArray[2] = aOutMess.getMessage();
//
// ... and Merges.
        } else if (aInMess.isEvent("A34") && super.hasValue(HL7_23.MSH_9_1_message_type,"ADT")) {
            aOutMess.setSegment(processMSHFromUFD("TDOC").getSegment());
            aOutMess.append(processPIDFromUFD());
            aOutMess.append(processMRGFromUFD());
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

// All events except A28 and A34 are sent as A31's to T-Doc.
        if (aMSHSegmentIN.hasValue(HL7_23.MSH_9_2_trigger_event, "A34")) {
            mHL7MessageEvent = "A34";
        } else if (aMSHSegmentIN.hasValue(HL7_23.MSH_9_2_trigger_event, "A28")) {
            mHL7MessageEvent = "A28";
        } else {
            mHL7MessageEvent = "A31";
        }
        aMSHSegmentOUT.set(HL7_23.MSH_9_2_trigger_event, mHL7MessageEvent);

        aMSHSegmentOUT.set(HL7_23.MSH_5_receiving_application, pReceivingApplication);
        aMSHSegmentOUT.set(HL7_23.MSH_7_message_date_time, aMSHSegmentIN.getDateTime());

        return (aMSHSegmentOUT);
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
                    HL7_23.PID_7_date_of_birth,
                    HL7_23.PID_8_sex,
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

        for (i=0 ; i < aTmpField.length ; i++) {
            aInField = new HL7Field(aTmpField[i]);
            aOutField = new HL7Field();
            aStr = aInField.getSubField(HL7_23.CX_ID_type_code);
            if (aStr.equalsIgnoreCase("PI")) {
                aPIDSegmentOUT.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number);
                aPIDSegmentOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "PI");
                aPIDSegmentOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, mFacility);
            }
        }
        aPIDSegmentOUT.copy(HL7_23.PID_5_patient_name,HL7_23.XPN_family_name,1);
        aPIDSegmentOUT.copy(HL7_23.PID_5_patient_name,HL7_23.XPN_given_name,1);
        aPIDSegmentOUT.copy(HL7_23.PID_5_patient_name,HL7_23.XPN_middle_name,1);

        // Only take the first address
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address,HL7_23.XAD_street_1, 1);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address,HL7_23.XAD_street_2, 1);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address,HL7_23.XAD_city, 1);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address,HL7_23.XAD_zip, 1);

        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_13_home_phone,HL7_23.XTN_telephone_number)) {
            aPIDSegmentOUT.copy(HL7_23.PID_13_home_phone,HL7_23.XTN_telephone_number);
        }
        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number)) {
            aPIDSegmentOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number);
        }

        return aPIDSegmentOUT;
    }
    //--------------------------------------------------------------------------------

}
