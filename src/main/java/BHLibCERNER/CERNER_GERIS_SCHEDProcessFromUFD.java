/*
 * CERNER_GERIS_SCHEDProcessFromUFD.java
 *
 * Created on 09 November, 10:15am
 *
 */

package BHLibCERNER;

import BHLibClasses.*;

/**
 *
 * @author sohn
 */
public class CERNER_GERIS_SCHEDProcessFromUFD extends ProcessSegmentsFromUFD {

    BHConstants k = new BHConstants();
    public String mEnvironment = "";
    public HL7Message mInHL7Message;
    public String mHospitalPrefix = "";
    public String mReportString = "";
    public String mFacilitySuffix = "";
    public String mMedicareNo = "";

    public CERNER_GERIS_SCHEDProcessFromUFD(String pHL7Message, String pEnvironment)  throws ICANException {
        super(pHL7Message);
        mVersion = "a";    // Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {
        String aCERNERMessageArray[] = {k.NULL, k.NULL, k.NULL};

        String aSegment;
        HL7Group aGroup;
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Message aOutMess= new HL7Message(k.NULL);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));

        if (mInHL7Message.isEvent("S12, S14, S15, S26")) {

            HL7Segment aMSHSegment = processMSHFromUFD("SCH_RIS", "CERNERSCH");
            HL7Segment aSCHSegment = processSCHFromUFD();
            HL7Segment aPIDSegment = processPIDFromUFD();
            HL7Segment aPV1Segment = processPV1FromUFD();
            HL7Segment aAILSegment = processAILFromUFD();

            aOutMess.append(aMSHSegment);
            aOutMess.append(aSCHSegment);
            aOutMess.append(aPIDSegment);
            aOutMess.append(aPV1Segment);
            aOutMess.append(aAILSegment);
        }

        aCERNERMessageArray[0] = aInMSHSegment.get(HL7_23.MSH_3_sending_application);
        aCERNERMessageArray[1] = aInMSHSegment.get(HL7_23.MSH_4_sending_facility);
        aCERNERMessageArray[2] = aOutMess.getMessage();

        return aCERNERMessageArray;
    }

    public HL7Segment processMSHFromUFD(String pSendingApplication, String pReceivingApplication) throws ICANException {
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
        mHL7MessageEvent = aMSHSegIN.get(HL7_23.MSH_9_2_trigger_event);

// Initialze aMSHSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.MSH_2_encoding_characters,
            //HL7_23.MSH_3_sending_application,
            HL7_23.MSH_4_sending_facility,
            HL7_23.MSH_6_receiving_facility,
            HL7_23.MSH_9_message_type,
            HL7_23.MSH_10_message_control_ID,
            HL7_23.MSH_11_processing_ID
        };

// Initialze OUT with those fields that are straight copies
        aMSHSegOUT.linkTo(aMSHSegIN);
        aMSHSegOUT.copyFields(aCopyFields);
        aMSHSegOUT.set(HL7_23.MSH_3_sending_application, pSendingApplication);
        aMSHSegOUT.set(HL7_23.MSH_5_receiving_application, pReceivingApplication);
        aMSHSegOUT.set(HL7_23.MSH_7_message_date_time, aMSHSegIN.getDateTime());
        aMSHSegOUT.copy(HL7_23.MSH_9_2_trigger_event);
        aMSHSegOUT.set(HL7_23.MSH_12_version_ID, "2.3");

        return (aMSHSegOUT);
    }

    public HL7Segment processPIDFromUFD() {
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.copyFields();
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");

        //PID 3 ID processing - remove id type and move out medicare number
        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal,"");
        int aPID3Count = aInPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int x=1; x<=aPID3Count; x++) {
            if (aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, x).equalsIgnoreCase("PI")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, x));
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, x));
            }
            if (aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, x).equalsIgnoreCase("MC")) {
                mMedicareNo = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, x);
                if (mMedicareNo.length() > 3) {
                    String aPatFirstname = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name);
                    if (aPatFirstname.length() >= 3) {
                        String aFirst3CharName = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name).substring(0,3);
                        aOutPIDSegment.set(HL7_23.PID_19_SSN_number, mMedicareNo.concat(" ").concat(aFirst3CharName));
                    } else {
                        aOutPIDSegment.set(HL7_23.PID_19_SSN_number, mMedicareNo.concat(" ").concat(aPatFirstname));
                    }
                } else {
                    String aPaddedSpace = "            "; //12 spaces
                    aOutPIDSegment.set(HL7_23.PID_19_SSN_number, aPaddedSpace.concat(mMedicareNo));
                }
            }
        }

        String aAssAuth = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, 1);
        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "SCH_".concat(aAssAuth), 1);

        return aOutPIDSegment;
    }

    public HL7Segment processPV1FromUFD() {
        HL7Segment aOutPV1Segment = new HL7Segment("PV1");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        aOutPV1Segment.linkTo(aInPV1Segment);
        aOutPV1Segment.copyFields();
        aOutPV1Segment.set(HL7_23.PV1_1_set_ID, "1");

        String aAssAuth = aInPV1Segment.get(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, 1);
        aOutPV1Segment.set(HL7_23.PV1_19_visit_number, HL7_23.CX_assigning_authority, "SCH_".concat(aAssAuth));

        return aOutPV1Segment;
    }

    public HL7Segment processSCHFromUFD() {
        HL7Segment aOutSCHSegment = new HL7Segment("SCH");
        HL7Segment aInSCHSegment = new HL7Segment(mInHL7Message.getSegment("SCH"));
        aOutSCHSegment.linkTo(aInSCHSegment);
        aOutSCHSegment.copyFields();

        return aOutSCHSegment;
    }

    public HL7Segment processAILFromUFD() {
        HL7Segment aOutAILSegment = new HL7Segment("AIL");
        HL7Segment aInAILSegment = new HL7Segment(mInHL7Message.getSegment("AIL"));
        aOutAILSegment.linkTo(aInAILSegment);
        aOutAILSegment.copyFields();

        return aOutAILSegment;
    }

}
