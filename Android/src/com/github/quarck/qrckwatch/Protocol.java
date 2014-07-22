package com.github.quarck.qrckwatch;

public class Protocol
{
    public static final int EntryNotificationsBitmask = 1; 
    public static final int EntryChargeLevel = 2;
    public static final int EntryWeatherAlert = 3;

    public static final int EntryDismissLevel = 200;
    public static final int EntryDismissID = 201;
    public static final int EntryDismissNumNotifications = 202;

    public static final int DismissLevelWatch = 0;
    public static final int DismissLevelPhone = 1;

    public static final int DismissableItemViber = 0;
    public static final int DismissableItemGmail = 1;
    public static final int DismissableItemCalendar = 2;
    public static final int DismissableItemMail = 3;
    public static final int DismissableItemEverything = 4;
}
