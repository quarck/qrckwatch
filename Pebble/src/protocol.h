#ifndef __QRCKWATCH_RPTOCOL_H_INCLUDED__
#define __QRCKWATCH_RPTOCOL_H_INCLUDED__

enum Message
{
	ENTRY_NOTIFICATIONS_BITMASK 	= 1, 
	ENTRY_CHARGE_LEVEL	= 2,
	ENTRY_WEATHER_ALERT	= 3,

	MSG_REQUEST_WATCHFACE_STATUS = 100,

	ENTRY_DISMISS_LEVEL = 110, 
	ENTRY_DISMISS_ID = 111, 
	ENTRY_DISMISS_NUM_NOTIFICATIONS = 112 // only in response
};

enum Notifications 
{
	NOTIFICATION_UNKNOWN 	= 0X01,
	NOTIFICATION_CALENDAR 	= 0X02,
	NOTIFICATION_GOOGLEHANGOUTS	= 0X04,
	NOTIFICATION_GOOGLEPLUS	= 0x08,
	NOTIFICATION_MESSAGES	= 0X10,
	NOTIFICATION_EMAIL	= 0X20,
	NOTIFICATION_GMAIL	= 0X40,
	NOTIFICATION_PHONE	= 0X80,
	NOTIFICATION_SKYPE	= 0X100,
	NOTIFICATION_VOIP	= 0X200,
	NOTIFICATION_IM	= 0X400,
	NOTIFICATION_FACEBOOK	= 0X800,
	NOTIFICATION_LINKEDIN	= 0X1000,
	NOTIFICATION_VK	= 0X2000,
	NOTIFICATION_INSTGRAM = 0X4000,
	NOTIFICATION_VIBER 	= 0x8000
};


enum DismissLevel 
{
	LEVEL_WATCH = 0, 
	LEVEL_PHONE = 1
};

enum DismissableId
{
	DISMISSABLE_ITEM_EVERYTHING	= 0,
	DISMISSABLE_ITEM_VIBER 		= 1, 
	DISMISSABLE_ITEM_GMAIL		= 2, 
	DISMISSABLE_ITEM_CALENDAR 	= 3, 
	DISMISSABLE_ITEM_MAIL		= 4,
	DISMISSABLE_ITEM_PHONE		= 5,
	DISMISSABLE_ITEM_MESSAGES	= 6,
	DISMISSABLE_ITEM_GOOGLEHANGOUTS	= 7,
	DISMISSABLE_ITEM_SKYPE		= 8,
	NUM_DISMISSABLE_ITEMS		= 9
};


#endif
