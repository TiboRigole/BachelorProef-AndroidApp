package com.retailsonar.retailsonar;

import com.google.api.services.calendar.CalendarScopes;

/**
 * Created by Aaron Hallaert on 4/16/2018.
 *
 * Constantes die over volledige app gebruikt worden
 *
 */

public class AppConstants {


    public static final String BASE_URL_SERVER="http://192.168.0.101:8080";

    // wordt gebruikt bij fingerprint
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    // calendar API
    public  static final String BUTTON_TEXT = "Call Google Calendar API";
    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

}
