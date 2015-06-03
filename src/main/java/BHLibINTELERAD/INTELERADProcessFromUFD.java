/*
 * INTELERADProcessFromUFD.java
 *
 * Created on 19 November 2009, 15:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package BHLibINTELERAD;

import BHLibClasses.*;

/**
 * @author norman soh
 */
public class INTELERADProcessFromUFD extends ProcessSegmentsFromUFD {

    public String mEnvironment = "";
    /**
     * Creates a new instance of INTELERADProcessFromUFD
     */
    public INTELERADProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "a";
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {
        String aHL7MessageArray[] = {k.NULL, k.NULL, k.NULL};
        String aSegment;
        HL7Group aGroup;
        HL7Message aInMess = new HL7Message(mHL7Message);
        HL7Message aOutMess = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = new HL7Segment(aInMess.getSegment(HL7_23.MSH));
        String aSendingApp = aMSHSegment.get(HL7_23.MSH_3_sending_application);
        String aSendingFac = aMSHSegment.get(HL7_23.MSH_4_sending_facility);

        if (aInMess.isEvent("A08, A34") &&
                (aSendingFac.equalsIgnoreCase("ALF") ||
                aSendingFac.equalsIgnoreCase("SDMH")) &&
                (aSendingApp.indexOf("CERNER") >= 0 ||
                aSendingApp.indexOf("CSC") >= 0)) {
            //logic to process patient demographics
            //aOutMess = aInMess;
            aOutMess.append(processMSHFromUFD("INTELERAD").getSegment());

            HL7Segment aInPIDSeg = new HL7Segment(aInMess.getSegment("PID"));
            HL7Segment aOutPIDSeg = new HL7Segment("PID");
            aOutPIDSeg.linkTo(aInPIDSeg);
            aOutPIDSeg.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
            if (aSendingFac.equalsIgnoreCase("SDMH")) {
                //append -SDMH to UR number
                String aPatientUR = aOutPIDSeg.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
                aOutPIDSeg.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPatientUR.concat("-SDMH"), 1);
            }
            if (aInMess.isEvent("A08")) {
                aOutPIDSeg.copy(HL7_23.PID_5_patient_name);
                aOutPIDSeg.copy(HL7_23.PID_7_date_of_birth);
                aOutPIDSeg.copy(HL7_23.PID_8_sex);
            }
            aOutMess.append(aOutPIDSeg);

            if (aInMess.isEvent("A34")) {
                aOutMess.append(aInMess.getSegment("MRG"));
            }

        } else if (aInMess.isEvent("O01") &&
                (aSendingFac.equalsIgnoreCase("ALF") ||
                aSendingFac.equalsIgnoreCase("SDMH")) &&
                aSendingApp.indexOf("GERIS") >= 0) {
            //logic to process orders
            HL7Segment aInORCSeg = new HL7Segment(aInMess.getSegment("ORC"));
            String aOrderControl = aInORCSeg.get(HL7_23.ORC_1_order_control);
            String aOrderStatus = aInORCSeg.get(HL7_23.ORC_5_order_status);
            if (aOrderControl.equalsIgnoreCase("SC") &&
                    aOrderStatus.equalsIgnoreCase("IP")) {
                aOutMess.append(processMSHFromUFD("INTELERAD").getSegment());

                //PID Segment
                HL7Segment aInPIDSeg = new HL7Segment(aInMess.getSegment("PID"));
                HL7Segment aOutPIDSeg = new HL7Segment("PID");
                aOutPIDSeg.linkTo(aInPIDSeg);
                aOutPIDSeg.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
                if (aSendingFac.equalsIgnoreCase("SDMH")) {
                    //append -SDMH to UR number
                    String aPatientUR = aOutPIDSeg.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
                    aOutPIDSeg.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPatientUR.concat("-SDMH"), 1);
                }
                aOutPIDSeg.copy(HL7_23.PID_5_patient_name);
                aOutPIDSeg.copy(HL7_23.PID_7_date_of_birth);
                aOutPIDSeg.copy(HL7_23.PID_8_sex);
                aOutMess.append(aOutPIDSeg);

                //ORC Segment
                HL7Segment aOutORCSeg = new HL7Segment("ORC");
                aOutORCSeg.linkTo(aInORCSeg);
                aOutORCSeg.copy(HL7_23.ORC_2_placer_order_num);
                String aORC3Value = aInORCSeg.get(HL7_23.ORC_3_filler_order_num, HL7_23.CX_ID_number);
                if (aORC3Value.startsWith("G")) {
                    aOutORCSeg.set(HL7_23.ORC_3_filler_order_num, aORC3Value.substring(1));
                } else {
                    aOutORCSeg.set(HL7_23.ORC_3_filler_order_num, aORC3Value);
                }
                aOutORCSeg.set(HL7_23.ORC_2_placer_order_num, aOutORCSeg.get(HL7_23.ORC_3_filler_order_num));
                aOutORCSeg.copy(HL7_23.ORC_5_order_status);
                aOutMess.append(aOutORCSeg);

                //OBR Segment
                HL7Segment aOutOBRSeg = new HL7Segment("OBR");
                HL7Segment aInOBRSeg = new HL7Segment(aInMess.getSegment("OBR"));
                aOutOBRSeg.linkTo(aInOBRSeg);
                aOutOBRSeg.copy(HL7_23.OBR_2_Placer_Order_Number);
                String aOBR3Value = aInOBRSeg.get(HL7_23.OBR_3_Filler_Order_Number, HL7_23.CX_ID_number);
                if (aOBR3Value.startsWith("G")) {
                    aOutOBRSeg.set(HL7_23.OBR_3_Filler_Order_Number, aOBR3Value.substring(1));
                } else {
                    aOutOBRSeg.set(HL7_23.OBR_3_Filler_Order_Number, aOBR3Value);
                }
                aOutOBRSeg.set(HL7_23.OBR_2_Placer_Order_Number, aOutOBRSeg.get(HL7_23.OBR_3_Filler_Order_Number));
                aOutOBRSeg.move(HL7_23.OBR_6_Requested_Date_Time, HL7_23.OBR_7_Observation_Date_Time);
                String aOBR21Value = aInOBRSeg.get(HL7_23.OBR_21_Fillers_Field_2);
                //append RAD to doctor code
                String aDocCode = aInOBRSeg.get(HL7_23.OBR_16_Ordering_Provider);
                aOutOBRSeg.set(HL7_23.OBR_16_Ordering_Provider, aDocCode.concat("RAD"));
                aOutOBRSeg.set(HL7_23.OBR_18_Placers_Field_1, aOBR21Value);
                aOutOBRSeg.set(HL7_23.OBR_19_Placers_Field_2, aOBR21Value.concat("_1"));
                aOutOBRSeg.set(HL7_23.OBR_20_Fillers_Field_1, aOBR21Value.concat("_2"));
                aOutOBRSeg.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
                aOutMess.append(aOutOBRSeg);
            }
        } else if (aInMess.isEvent("R01") &&
                (aSendingFac.equalsIgnoreCase("ALF") ||
                aSendingFac.equalsIgnoreCase("SDMH")) &&
                aSendingApp.indexOf("GERIS") >= 0) {
            //logic to process results
            HL7Segment aInORCSeg = new HL7Segment(aInMess.getSegment("ORC"));
            HL7Segment aInOBRSeg = new HL7Segment(aInMess.getSegment("OBR"));
            String aOrderControl = aInORCSeg.get(HL7_23.ORC_1_order_control);
            String aOrderStatus = aInORCSeg.get(HL7_23.ORC_5_order_status);
            String aOrderingProvider = aInOBRSeg.get(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num);

            //check if HealthLink doctor - if yes, proceed to send
            CodeLookUp aLU = new CodeLookUp("HEALTHLINK_RAD_Codes.table", mEnvironment);
            if (aLU.getValue(aOrderingProvider).length() > 0) {
                if (aOrderControl.equalsIgnoreCase("RE") &&
                        aOrderStatus.equalsIgnoreCase("CM")) {
                    aOutMess.append(processMSHFromUFD("INTELERAD").getSegment());

                    //PID Segment
                    HL7Segment aInPIDSeg = new HL7Segment(aInMess.getSegment("PID"));
                    HL7Segment aOutPIDSeg = new HL7Segment("PID");
                    aOutPIDSeg.linkTo(aInPIDSeg);
                    aOutPIDSeg.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
                    if (aSendingFac.equalsIgnoreCase("SDMH")) {
                        //append -SDMH to UR number
                        String aPatientUR = aOutPIDSeg.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
                        aOutPIDSeg.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPatientUR.concat("-SDMH"), 1);
                    }
                    aOutPIDSeg.copy(HL7_23.PID_5_patient_name);
                    aOutPIDSeg.copy(HL7_23.PID_7_date_of_birth);
                    aOutPIDSeg.copy(HL7_23.PID_8_sex);
                    aOutMess.append(aOutPIDSeg);

                    //ORC Segment
                    HL7Segment aOutORCSeg = new HL7Segment("ORC");
                    aOutORCSeg.linkTo(aInORCSeg);
                    aOutORCSeg.copy(HL7_23.ORC_2_placer_order_num);
                    String aORC3Value = aInORCSeg.get(HL7_23.ORC_3_filler_order_num, HL7_23.CX_ID_number);
                    if (aORC3Value.startsWith("G")) {
                        aOutORCSeg.set(HL7_23.ORC_3_filler_order_num, aORC3Value.substring(1));
                    } else {
                        aOutORCSeg.set(HL7_23.ORC_3_filler_order_num, aORC3Value);
                    }
                    aOutORCSeg.set(HL7_23.ORC_2_placer_order_num, aOutORCSeg.get(HL7_23.ORC_3_filler_order_num));
                    aOutORCSeg.set(HL7_23.ORC_5_order_status, "ZZ");
                    aOutMess.append(aOutORCSeg);

                    //OBR Segment
                    HL7Segment aOutOBRSeg = new HL7Segment("OBR");
                    //HL7Segment aInOBRSeg = new HL7Segment(aInMess.getSegment("OBR"));
                    aOutOBRSeg.linkTo(aInOBRSeg);
                    aOutOBRSeg.copy(HL7_23.OBR_2_Placer_Order_Number);
                    String aOBR3Value = aInOBRSeg.get(HL7_23.OBR_3_Filler_Order_Number, HL7_23.CX_ID_number);
                    if (aOBR3Value.startsWith("G")) {
                        aOutOBRSeg.set(HL7_23.OBR_3_Filler_Order_Number, aOBR3Value.substring(1));
                    } else {
                        aOutOBRSeg.set(HL7_23.OBR_3_Filler_Order_Number, aOBR3Value);
                    }
                    aOutOBRSeg.set(HL7_23.OBR_2_Placer_Order_Number, aOutOBRSeg.get(HL7_23.OBR_3_Filler_Order_Number));
                    String aOBR21Value = aInOBRSeg.get(HL7_23.OBR_21_Fillers_Field_2);
                    //append RAD to doc code
                    String aDocCode = aInOBRSeg.get(HL7_23.OBR_16_Ordering_Provider);
                    aOutOBRSeg.set(HL7_23.OBR_16_Ordering_Provider, aDocCode.concat("RAD"));
                    aOutOBRSeg.set(HL7_23.OBR_18_Placers_Field_1, aOBR21Value);
                    aOutOBRSeg.set(HL7_23.OBR_19_Placers_Field_2, aOBR21Value.concat("_1"));
                    aOutOBRSeg.set(HL7_23.OBR_20_Fillers_Field_1, aOBR21Value.concat("_2"));
                    aOutOBRSeg.copy(HL7_23.OBR_24_Diagnostic_Service_Section_ID);
                    aOutOBRSeg.copy(HL7_23.OBR_32_Principal_Result_Interpreter, HL7_23.CX_ID_number);
                    aOutOBRSeg.copy(HL7_23.OBR_35_transcriptionist, HL7_23.CX_ID_number);
                    aOutMess.append(aOutOBRSeg);

                    //OBX segments
                    int aOBXCounter = aInMess.countSegments("OBX");
                    for (int a=1; a<=aOBXCounter; a++) {
                        HL7Segment aInOBXSeg = new HL7Segment(aInMess.getSegment("OBX", a));
                        HL7Segment aOutOBXSeg = new HL7Segment("OBX");
                        aOutOBXSeg.linkTo(aInOBXSeg);
                        aOutOBXSeg.copy(HL7_23.OBX_1_set_ID);
                        aOutOBXSeg.copy(HL7_23.OBX_5_observation_value);
                        aOutOBXSeg.copy(HL7_23.OBX_11_observ_results_status);
                        aOutMess.append(aOutOBXSeg);
                    }
                }
            }
        }

        aHL7MessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
        aHL7MessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
        aHL7MessageArray[2] = aOutMess.getMessage();

        return aHL7MessageArray;
    }
}
