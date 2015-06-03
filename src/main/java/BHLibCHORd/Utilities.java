/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BHLibCHORd;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *
 * @author bugsy
 */
public class Utilities {

    public static String FormatDateTimeHL7toSQL(String hl7Date) {
        SimpleDateFormat hl7Format;
        if (hl7Date.length() == 12) {
            hl7Format = new SimpleDateFormat("yyyyMMddHHmm");
        } else if (hl7Date.length() == 14) {
            hl7Format = new SimpleDateFormat("yyyyMMddHHmmss");
        } else if (hl7Date.length() == 4) {
            hl7Format = new SimpleDateFormat("HHmm");
        } else if (hl7Date.length() == 8) {
            hl7Format = new SimpleDateFormat("yyyyMMdd");
        } else {
            hl7Format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        }

        SimpleDateFormat sqlFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm");
        try {
            Date date = hl7Format.parse(hl7Date);
            if (date.after(sqlFormat.parse("01/Jan/1753 00:00"))) {
                String sqlDate = sqlFormat.format(date);
                return sqlDate;
            } else {
                return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }

    public static String FormatDateTimeSQLtoHL7(String sqlDate) {
        SimpleDateFormat hl7Format = new SimpleDateFormat("yyyyMMddhhmm");
        SimpleDateFormat sqlFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm");
        try {
            Date date = sqlFormat.parse(sqlDate);
            String hl7Date = hl7Format.format(date);
            return hl7Date;
        } catch (ParseException e) {
            return "";
        }
    }

    public static String processDateTime(String field) {
        String aField = "";
        if (field.length() != 0) {
            if (field.length() == 14) {
                aField = "20" + field.substring(6, 8) + field.substring(3, 5)
                        + field.substring(0, 2) + field.substring(9, 11) + field.substring(12, 14);
            } else {
                aField = field.substring(6, 10) + field.substring(3, 5)
                        + field.substring(0, 2) + field.substring(11, 13) + field.substring(14, 16);
            }
        }
        return aField;
    }
}
