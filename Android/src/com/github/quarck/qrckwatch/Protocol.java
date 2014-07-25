package com.github.quarck.qrckwatch;

public class Protocol
{
    public static final int EntryNotificationsBitmask = 1; 
    public static final int EntryChargeLevel = 2;
    public static final int EntryWeatherAlert = 3;

    public static final int EntryDismissLevel = 110;
    public static final int EntryDismissID = 111;
    public static final int EntryDismissNumNotifications = 112;

    public static final int DismissLevelWatch = 0;
    public static final int DismissLevelPhone = 1;

    public static final int DismissableItemEverything = 0;
    public static final int DismissableItemViber = 1;
    public static final int DismissableItemGmail = 2;
    public static final int DismissableItemCalendar = 3;
    public static final int DismissableItemMail = 4; 
    public static final int DismissableItemPhone = 5;
    public static final int DismissableItemMessage = 6;
    public static final int DismissableItemGoogleHangouts = 7;
    public static final int DismissableItemSkype = 8;
}
