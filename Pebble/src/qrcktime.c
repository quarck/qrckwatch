#include "pebble.h"

#include "protocol.h"
#include "weathercodes.h"

enum {
	CHARGE_POLL_INTERVAL = 5 // every 5 mins
};

Window *window;
TextLayer *text_date_layer;
TextLayer *text_time_layer;

Layer *top_line_layer;
Layer *bottom_line_layer;

TextLayer *notifications_layer;

TextLayer *bt_status_layer;
TextLayer *phone_batt_layer;
TextLayer *watch_batt_layer;

int32_t notifications_bitmask = 0;
int8_t phone_charge_level = -1; // values outside of 0...100 are interpreted as N/A
int8_t watch_is_charging = 0;
int8_t watch_charge_level = -1; // values outside of 0...100 are ... N/A

int8_t bt_disconnected = 1;
time_t last_bt_update = 0;

int8_t weather_code = 0;

void send_request();
void display_notifications();
void display_indicators();


void top_line_layer_update_callback(Layer * layer, GContext * ctx)
{
	graphics_context_set_fill_color(ctx, GColorWhite);
	graphics_fill_rect(ctx, layer_get_bounds(layer), 0, GCornerNone);
}

void bottom_line_layer_update_callback(Layer * layer, GContext * ctx)
{
	graphics_context_set_fill_color(ctx, GColorWhite);
	graphics_fill_rect(ctx, layer_get_bounds(layer), 0, GCornerNone);
}


void handle_minute_tick(struct tm *tick_time, TimeUnits units_changed)
{
	// Need to be static because they're used by the system later.
	static char time_text[] = "00:00";
	static char date_text[] = "Xxxxxxxxx 00";

	char *time_format;

	time_t now = time(NULL);

	if (!tick_time) 
	{
		tick_time = localtime(&now);
	}

	// TODO: Only update the date when it's changed.
	strftime(date_text, sizeof(date_text), "%a  %B %e", tick_time);
	text_layer_set_text(text_date_layer, date_text);

	if (clock_is_24h_style()) 
		time_format = "%R";
	else 
		time_format = "%I:%M";

	strftime(time_text, sizeof(time_text), time_format, tick_time);

	// Kludge to handle lack of non-padded hour format string
	// for twelve hour clock.
	if (!clock_is_24h_style() && (time_text[0] == '0')) 
	{
		memmove(time_text, &time_text[1], sizeof(time_text) - 1);
	}

	text_layer_set_text(text_time_layer, time_text);

	bt_disconnected = (now - last_bt_update > 390) ? 1 : 0; // 6.5 min

	display_indicators();
	
	display_notifications();
}

void display_notifications()
{
	static char notifications[128+30];

	memset(notifications, 0, sizeof(notifications));

	if (weather_code != 0 )
	{
		switch(weather_code)
		{
			case TORNADO: // 0, // 	tornado
				strcat(notifications, "!! TORNADO !!\n");
				break; 
			
			case TROPICAL_STORM: //1,	//	tropical storm
				strcat(notifications, "!! STORM !!\n");
				break; 
			
			case HURRICANE: //2,	//	hurricane
				strcat(notifications, "!! HURRICANE !!\n");
				break; 
			
			case SEVERE_THUNDERSTORMS: //3,	//	severe thunderstorms
				strcat(notifications, "!! THUNDERSTORM !!\n");
				break; 
	
			case ISOLATED_THUNDERSTORMS: //37,	//	isolated thunderstorms
			case SCATTERED_THUNDERSTORMS_1: //38,	//	scattered thunderstorms
			case SCATTERED_THUNDERSTORMS_2: //39,	//	scattered thunderstorms
			case ISOLATED_THUNDERSTORMS_2: //47	//	isolated thundershowers
			case THUNDERSTORMS_2: //45,	//	thundershowers
			case THUNDERSTORMS: //4,	//	thunderstorms
				strcat(notifications, "[THUNDERSTORM]\n");
				break; 
			
			case MIXED_RAIN_AND_SNOW: //5,	//	mixed rain and snow
				strcat(notifications, "[RAIN & SNOW]\n");
				break; 
			
			case MIXED_RAIND_AND_SLEET: //6,	//	mixed rain and sleet
				strcat(notifications, "[RAIN & SLEET]\n");
				break; 
			
			case MIXED_RAIN_AND_HAIL: //35,	//	mixed rain and hail
				strcat(notifications, "[RAIN & HAIL]\n");
				break; 
			
			case MIXED_SNOW_AND_SLEET: //7,	//	mixed snow and sleet
				strcat(notifications, "[SNOW & SLEET]\n");
				break; 
			
			case FREEZING_DRIZZLE: //8,	//	freezing drizzle
				strcat(notifications, "[FREEZING DRIZZLE]\n");
				break; 
			
			case DRIZZLE: //9,	//	drizzle
				strcat(notifications, "[DRIZZLE]\n");
				break; 
			
			case FREEZING_RAIN: //10,	//	freezing rain
				strcat(notifications, "[RAIN]\n");
				break; 
			
			case SHOWERS_1: //11,	//	showers
			case SHOWERS_2: //12,	//	showers
			case SCATTERED_SHOWERS: //40,	//	scattered showers
				strcat(notifications, "[SHOWERS]\n");
				break; 
			
			
			case HAIL: //17,	//	hail
				strcat(notifications, "[HAIL]\n");
				break; 
			
			case SLEET: //18,	//	sleet
				strcat(notifications, "[SLEET]\n");
				break; 
			
			case DUST: //19,	//	dust
				strcat(notifications, "[DUST]\n");
				break; 
			
			case FOGGY: //20,	//	foggy
				strcat(notifications, "[FOGGY]\n");
				break; 
			
			case HAZE: //21,	//	haze
				strcat(notifications, "[HAZE]\n");
				break; 
			
			case SMOKY: //22,	//	smoky
				strcat(notifications, "[SMOKY]\n");
				break; 
			
			case BLUSTERY: //23,	//	blustery
				strcat(notifications, "[BLUSTERY]\n");
				break; 
			
			case WINDY: //24,	//	windy
				strcat(notifications, "[WINDY]\n");
				break; 
			
			case COLD: //25,	//	cold
				strcat(notifications, "[COLD]\n");
				break; 
			

			case CLOUDY: //26,	//	cloudy
			case MOSTLY_CLOUDY_NIGHT: //27,	//	mostly cloudy (night)
			case MOSTLY_CLOUDY_DAY: //28,	//	mostly cloudy (day)
			case PARTLY_CLOUDY_NIGHT: //29,	//	partly cloudy (night)
			case PARTLY_CLOUDY_DAY: //30,	//	partly cloudy (day)
			case PARTLY_CLOUDY: //44,	//	partly cloudy
				strcat(notifications, "[CLOUDY]\n");
				break; 
			
			case CLEAR: //31,	//	clear (night)
				strcat(notifications, "[CLEAR]\n");
				break; 
			case SUNNY: //32,	//	sunny
				strcat(notifications, "[SUNNY]\n");
				break; 
			case FAIR_NIGHT: //33,	//	fair (night)
			case FAIR_DAY: //34,	//	fair (day)
				strcat(notifications, "[FAIR]\n");
				break; 
			case HOT: //36,	//	hot
				strcat(notifications, "[HOT]\n");
				break; 

			
			case SNOW_FLURRIES: //13,	//	snow flurries
			case LIGHT_SNOW_SHOWERS: //14,	//	light snow showers
				strcat(notifications, "[SNOW FLURRIES]\n");
				break; 
			
			case BLOWING_SNOW: //15,	//	blowing snow
				strcat(notifications, "[BLOWING SNOW]\n");
				break; 
			
			case SNOW: //16,	//	snow
				strcat(notifications, "[SNOW]\n");
				break; 


			case HEAVY_SNOW_1: //41,	//	heavy snow
			case HEAVY_SNOW_2: //43,	//	heavy snow
				strcat(notifications, "!! HEAVY SNOW !!\n");
				break;

			case SNOW_SHOWERS : //46,	//	snow showers
			case SCATTERED_SNOW_SHOWERS: //42,	//	scattered snow showers
				strcat(notifications, "[SNOW SHOWERS]\n");
				break;
		}
	}

	if (notifications_bitmask & NOTIFICATION_CALENDAR)
		strcat(notifications, "[CAL] "); // 6

	if (notifications_bitmask & NOTIFICATION_GMAIL)
		strcat(notifications, "[GMAIL] "); // 8, 14

	if (notifications_bitmask & NOTIFICATION_EMAIL)
		strcat(notifications, "[EMAIL] "); // 8, 22 

	if (notifications_bitmask & NOTIFICATION_PHONE)
		strcat(notifications, "[PHONE] "); // 8, 30

	if (notifications_bitmask & NOTIFICATION_MESSAGES)
		strcat(notifications, "[SMS] "); // 6, 36

	if (notifications_bitmask & NOTIFICATION_SKYPE)
		strcat(notifications, "[SKYPE] "); // 8, 44

	if (notifications_bitmask & NOTIFICATION_GOOGLEHANGOUTS)
		strcat(notifications, "[MSG] "); // 6, 50

	if (notifications_bitmask & NOTIFICATION_GOOGLEPLUS)
		strcat(notifications, "[G+] "); // 5, 55

	if (notifications_bitmask & NOTIFICATION_VOIP)
		strcat(notifications, "[VOIP] "); // 7, 62

	if (notifications_bitmask & NOTIFICATION_IM)
		strcat(notifications, "[IM] "); // 5, 67

	if (notifications_bitmask & NOTIFICATION_FACEBOOK)
		strcat(notifications, "[FB] "); // 5, 72

	if (notifications_bitmask & NOTIFICATION_LINKEDIN)
		strcat(notifications, "[LN] "); // 5, 77

	if (notifications_bitmask & NOTIFICATION_VK)
		strcat(notifications, "[VK] "); // 5, 83

	if (notifications_bitmask & NOTIFICATION_INSTGRAM)
		strcat(notifications, "[INSTGRAM] "); // 11, 94

	if (notifications_bitmask & NOTIFICATION_UNKNOWN)
		strcat(notifications, "[+++] ");

	text_layer_set_text(notifications_layer, notifications);
}

void display_indicators()
{
	static char watch_charge_text[] = "xxx  ";
	static char phone_charge_text[] = "xxx  ";

	if (phone_charge_level >= 0 && phone_charge_level <= 100)
		snprintf(phone_charge_text, sizeof(phone_charge_text), "%d", (int)phone_charge_level);
	else
		strncpy(phone_charge_text, "N/A", sizeof(phone_charge_text));

	if (watch_charge_level >= 0 && watch_charge_level <= 100)
		snprintf(watch_charge_text, sizeof(watch_charge_text), "%c%d", (watch_is_charging ? '+':' '), (int)watch_charge_level);
	else
		strncpy(watch_charge_text, "N/A", sizeof(watch_charge_text));

	text_layer_set_text(bt_status_layer, bt_disconnected ? "NO CONN" : "");

	text_layer_set_text(phone_batt_layer, phone_charge_text);
	text_layer_set_text(watch_batt_layer, watch_charge_text);
}

void received_data(DictionaryIterator *received, void *context) 
{
	Tuple *entry = NULL;

	last_bt_update = time(NULL);
	bt_disconnected = 0; 

	entry = dict_find(received, ENTRY_NOTIFICATIONS_BITMASK); 
	if (entry != NULL)
	{
		notifications_bitmask = entry->value->int32;
		display_notifications();
	}
	
	entry = dict_find(received, ENTRY_CHARGE_LEVEL); 
	if (entry != NULL)
	{
		phone_charge_level = entry->value->uint8;
	}
	
	entry = dict_find(received, ENTRY_WEATHER_ALERT); 
	if (entry != NULL)
	{
		weather_code = entry->value->uint8;
		display_notifications();
	}
	else if (weather_code != 0 ) // alert disappeared
	{
		weather_code = 0;
		display_notifications();
	}
		
	display_indicators();
}

void watch_battery_changed(BatteryChargeState charge)
{
	watch_charge_level = charge.charge_percent;
	watch_is_charging = charge.is_charging;

	display_indicators();
}

void send_request()
{
	DictionaryIterator *iterator = NULL;
	app_message_outbox_begin(&iterator);
	dict_write_uint8(iterator, 100, 0); // some unused code -- we just need to send a 'ping' to app, without any real data
	app_message_outbox_send();
}

void handle_deinit(void)
{
	tick_timer_service_unsubscribe();
	battery_state_service_unsubscribe();
}

void handle_init(void)
{
	window = window_create();
	window_stack_push(window, true /* Animated */ );
	window_set_background_color(window, GColorBlack);

	Layer *window_layer = window_get_root_layer(window);

	// Notifications text layer
	notifications_layer = text_layer_create(GRect(4, 4, 144-8, 72));
	text_layer_set_text_color(notifications_layer, GColorWhite);
	text_layer_set_background_color(notifications_layer, GColorClear);
	text_layer_set_font(notifications_layer,
			    fonts_get_system_font
			    (FONT_KEY_GOTHIC_18));
	layer_add_child(window_layer, text_layer_get_layer(notifications_layer));

	// Date layer
	text_date_layer = text_layer_create(GRect(8, 72, 144 - 8, 168 - 68));
	text_layer_set_text_color(text_date_layer, GColorWhite);
	text_layer_set_background_color(text_date_layer, GColorClear);
	text_layer_set_font(text_date_layer,
			    fonts_get_system_font
			    (FONT_KEY_ROBOTO_CONDENSED_21));
	layer_add_child(window_layer, text_layer_get_layer(text_date_layer));

	// Time layer
	text_time_layer = text_layer_create(GRect(7, 95, 144 - 7, 168 - 92));
	text_layer_set_text_color(text_time_layer, GColorWhite);
	text_layer_set_background_color(text_time_layer, GColorClear);
	text_layer_set_font(text_time_layer,
			    fonts_get_system_font
			    (FONT_KEY_ROBOTO_BOLD_SUBSET_49));
	layer_add_child(window_layer, text_layer_get_layer(text_time_layer));

	// Upper line layer
	GRect top_line_frame = GRect(0, 100, 144, 1);
	top_line_layer = layer_create(top_line_frame);
	layer_set_update_proc(top_line_layer, top_line_layer_update_callback);
	layer_add_child(window_layer, top_line_layer);

	// Bottom line layer
	GRect bottom_line_frame = GRect(0, 152, 144, 1);
	bottom_line_layer = layer_create(bottom_line_frame);
	layer_set_update_proc(bottom_line_layer, bottom_line_layer_update_callback);
	layer_add_child(window_layer, bottom_line_layer);


	// BT connection status layer
	bt_status_layer = text_layer_create(GRect(4, 152, 50, 14));
	text_layer_set_text_color(bt_status_layer, GColorWhite);
	text_layer_set_background_color(bt_status_layer, GColorClear);
	text_layer_set_font(bt_status_layer,
			    fonts_get_system_font
			    (FONT_KEY_GOTHIC_14));
	layer_add_child(window_layer, text_layer_get_layer(bt_status_layer));

	// Phone battery layer 
	phone_batt_layer = text_layer_create(GRect(80, 152, 28, 14));
	text_layer_set_text_alignment(phone_batt_layer, GTextAlignmentRight);
	text_layer_set_text_color(phone_batt_layer, GColorWhite);
	text_layer_set_background_color(phone_batt_layer, GColorClear);
	text_layer_set_font(phone_batt_layer,
			    fonts_get_system_font
			    (FONT_KEY_GOTHIC_14));
	layer_add_child(window_layer, text_layer_get_layer(phone_batt_layer));

	// Watch battery layer
	watch_batt_layer = text_layer_create(GRect(114, 152, 28, 14));
	text_layer_set_text_alignment(watch_batt_layer, GTextAlignmentRight);
	text_layer_set_text_color(watch_batt_layer, GColorWhite);
	text_layer_set_background_color(watch_batt_layer, GColorClear);
	text_layer_set_font(watch_batt_layer,
			    fonts_get_system_font
			    (FONT_KEY_GOTHIC_14));
	layer_add_child(window_layer, text_layer_get_layer(watch_batt_layer));

	// setup communication
	app_message_register_inbox_received(received_data);
	app_message_open(124, 50);

	// battery state service 
	battery_state_service_subscribe(watch_battery_changed);

	// Time callback setup
	tick_timer_service_subscribe(MINUTE_UNIT, handle_minute_tick);
	handle_minute_tick(NULL, MINUTE_UNIT);
      	
	// request everything. 
	send_request();

	watch_battery_changed(battery_state_service_peek());
}

int main(void)
{
	handle_init();

	app_event_loop();

	handle_deinit();
}
