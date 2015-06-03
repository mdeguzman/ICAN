/*
 * PROMEDICUSProcessToUFD.java
 *
 * Created on 5 January 2007, 15:57
 *
 */

package com.dataagility.ICAN.BHLibPROMEDICUS;

import com.dataagility.ICAN.BHLibClasses.*;

/**
 *
 * @author sohn
 */
public class PROMEDICUSProcessToUFD extends ProcessSegmentsToUFD {

    public HL7Message mInHL7Message;
    public String mEnvironment = "";

    /**
     * Creates a new instance of PROMEDICUSProcessToUFD
     */
    public PROMEDICUSProcessToUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "a";    // Release Version Number
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }

    public String processMessage() throws ICANException {
        HL7Message aOutHL7Message = new HL7Message(k.NULL);
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));

        if (mInHL7Message.isEvent("R01")) {
            HL7Segment aMSHSegment = processMSHToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Group aOrderObservationsGroup = processOrderObservations_ToUFD();

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aOrderObservationsGroup);

        } else if (mInHL7Message.isEvent("O01")) {
            HL7Segment aMSHSegment = processMSHToUFD();
            HL7Segment aPIDSegment = processPIDToUFD();
            HL7Segment aPV1Segment = processPV1ToUFD();
            HL7Group aOrderObservationsGroup = processOrderObservations_ToUFD();

            aOutHL7Message.append(aMSHSegment);
            aOutHL7Message.append(aPIDSegment);
            aOutHL7Message.append(aPV1Segment);
            aOutHL7Message.append(aOrderObservationsGroup);
        }
        if (aOutHL7Message.getMessage().length() > 0) {
            aOutHL7Message.append(setupZBX("MESSAGE", "SOURCE_ID", aInMSHSegment.get(HL7_23.MSH_10_message_control_ID)));
        }

        return aOutHL7Message.getMessage();
    }

    public HL7Group processOrderObservations_ToUFD() throws ICANException {
        HL7Group aOutOrderObservationsGroup = new HL7Group("");
        int aGroupCount;
        int aCount = 1;
        String aGroupID[] = HL7_23.Group_Orders;
        aGroupCount = mInHL7Message.countGroups(aGroupID);

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            aOutOrderObservationsGroup.append(mInHL7Message.getGroup(aGroupID, aCount));
        }
        return aOutOrderObservationsGroup;
    }

    public HL7Segment processPIDToUFD() throws ICANException {
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        String aSendingFac = aInMSHSegment.get(HL7_23.MSH_4_sending_facility);

        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.copy(HL7_23.PID_1_set_ID);
        aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal);
        int aPID3Count = aOutPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        for (int i = 1; i <= aPID3Count; i++) {
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).matches("MR")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "PI", i);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, aSendingFac, i);
            }
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).matches("MC")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "HIC", i);
            }
            if (aOutPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, i).matches("DVA")) {
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_type_code, "VA", i);
                aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "DVA", i);
            }
        }
        aOutPIDSegment.copy(HL7_23.PID_5_patient_name);
        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address);
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone);

        return aOutPIDSegment;
    }
}
