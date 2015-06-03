/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BHLibPARIS_DateFix;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author da-admin
 */
public class CreateCurrentTime {

    public String getCurrentDate(){
        String cDate = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        cDate = dateFormat.format(cal.getTime());

        return cDate;
    }


}
