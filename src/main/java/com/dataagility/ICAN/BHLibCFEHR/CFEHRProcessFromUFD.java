/*
 * CFEHRProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.dataagility.ICAN.BHLibCFEHR;
import com.dataagility.ICAN.BHLibClasses.*;

/**
 *
 * @author normansoh
 */
public class CFEHRProcessFromUFD extends ProcessSegmentsFromUFD {

    public String mEnvironment = "";

    /**
     * Creates a new instance of CFEHRProcessFromUFD
     */
    public CFEHRProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {

        CodeLookUp aLU = new CodeLookUp("CFEHR_URList.table", mEnvironment);
        String aCFEHRMessageArray[] = {k.NULL, k.NULL, k.NULL};
        HL7Message aInMess = new HL7Message(mHL7Message);
        HL7Message aOutMess = new HL7Message("");

        HL7Segment aMSHSegment = new HL7Segment(aInMess.getSegment(HL7_23.MSH));
        HL7Segment aPIDSegment = new HL7Segment(aInMess.getSegment(HL7_23.PID));

        String aFacility = aMSHSegment.get(HL7_23.MSH_4_sending_facility);
        int aPID3Count = aPIDSegment.countRepeatFields(HL7_23.PID_3_patient_ID_internal);
        boolean aProceedFlag = false;
        if (aFacility.equalsIgnoreCase("ALF")) {
            for (int i=1; i<=aPID3Count; i++) {
                String aPatientIDType = aPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CE_alternate_text, i);
                if (aPatientIDType.matches("MR") || aPatientIDType.matches("PI")) {
                    String aPatientID = aPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CE_ID_code, i);
                    if (aLU.getValue(aPatientID + " " + aFacility).length() == 0) {
                        //found patient in CF list
                        aProceedFlag = true;
                        break;
                    }
                }
            }
        }

        if (aProceedFlag == true) {
            //Send HL7 message

            //Parse the incoming message to fix up OBX_3 and OBR_4 segment
            int aSegmentCount = aInMess.countSegments();
            HL7Segment aSegment = new HL7Segment("");
            for (int i = 1; i <= aSegmentCount; i++) {
                aSegment.setSegment(aInMess.getSegment(i));
                if (aSegment.getSegment().startsWith("OBR|")) {
                    //Change to triplet
                    String aIdentifier = aSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                    String aText = aSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme);
                    String aCodingSystem = aSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_alternate_ID);
                    if (aCodingSystem.length() > 0) {
                        aSegment.set(HL7_23.OBR_4_Universal_Service_ID, "");
                        aSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, aIdentifier);
                        aSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text, aText);
                        aSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_coding_scheme, aCodingSystem);
                    }
                }
                if (aSegment.getSegment().startsWith("OBX|")) {
                    //Change to triplet
                    String aIdentifier = aSegment.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code);
                    String aText = aSegment.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme);
                    String aCodingSystem = aSegment.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_alternate_ID);
                    if (aCodingSystem.length() > 0) {
                        aSegment.set(HL7_23.OBX_3_observation_identifier, "");
                        aSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aIdentifier);
                        aSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text, aText);
                        aSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_coding_scheme, aCodingSystem);
                    }
                    String aOBX6Unit = aSegment.get(HL7_23.OBX_6_units);
                    aOBX6Unit = replaceInStr(aOBX6Unit, "^", "\\S\\");
                    aSegment.set(HL7_23.OBX_6_units, aOBX6Unit);
                }
                aOutMess.append(aSegment);
            }

            //aOutMess = aInMess;
            aMSHSegment.set(HL7_23.MSH_5_receiving_application, "SMARTHEALTH");
            aMSHSegment.set(HL7_23.MSH_6_receiving_facility, "SHS");
            aOutMess.setSegment(aMSHSegment.getSegment(), 1);
            aCFEHRMessageArray[0] = aMSHSegment.get(HL7_23.MSH_3_sending_application);
            aCFEHRMessageArray[1] = aMSHSegment.get(HL7_23.MSH_4_sending_facility);
            aCFEHRMessageArray[2] = aOutMess.getMessage();

            return aCFEHRMessageArray;
        }
        return aCFEHRMessageArray;
    }

    public String replaceInStr(String pSrc, String pFrom, String pTo) {
        int aPos;

        aPos = pSrc.indexOf(pFrom);
        if (aPos >= 0) {
            pSrc = pSrc.substring(0, aPos) + pTo + pSrc.substring(aPos + pFrom.length());
        }
        return pSrc;

    }
}
