#include "pebble.h"

#include "protocol.h"
#include "weathercodes.h"

#define NUM_ICONS 22

#define NUM_ICON_POSITIONS_LINE1 4
#define NUM_ICON_POSITIONS_LINE2 4

#define NUM_ICON_POSITIONS_TOTAL (NUM_ICON_POSITIONS_LINE1 + NUM_ICON_POSITIONS_LINE2)

#define DEFAULT_WEATHER_CODE 127

#define BT_DISCONNECTED_TIMEOUT 60 * 35 /* 35 mins */

Window *window;
TextLayer *text_date_layer;
TextLayer *text_time_layer;

#define NUM_TXT_LAYERS 7
TextLayer *txt_layers[NUM_TXT_LAYERS];

TextLayer *weather_status_layer;

int32_t notifications_bitmask = 0;
int8_t phone_charge_level = -1; // values outside of 0...100 are interpreted as N/A
int8_t watch_is_charging = 0;
int8_t watch_charge_level = -1; // values outside of 0...100 are ... N/A

int8_t bt_disconnected = 1;
time_t last_bt_update = 0;

int8_t weather_code = DEFAULT_WEATHER_CODE; // out of range

void send_request();
void notifications_update_callback(Layer * layer, GContext * ctx);
void display_indicators();

// Menu items can optionally have an icon drawn with them
GBitmap *notification_icons[NUM_ICONS];

Layer* notification_layer = NULL;

static GBitmap *get_icon_for_id(int id)
{
	int idx = -1; 

	// not a very best solution, but have no std::map<int,int>
	switch (id)
	{
	case RESOURCE_ID_IMAGE_MENU_ICON:  idx = 0; break;
	case RESOURCE_ID_IMAGE_CALENDAR:  idx = 1; break;
	case RESOURCE_ID_IMAGE_EMAIL:  idx = 2; break;
	case RESOURCE_ID_IMAGE_EMERGENCY:  idx = 3; break;
	case RESOURCE_ID_IMAGE_FACEBOOK:  idx = 4; break;
	case RESOURCE_ID_IMAGE_GMAIL:  idx = 5; break;
	case RESOURCE_ID_IMAGE_GPLUS:  idx = 6; break;
	case RESOURCE_ID_IMAGE_HANGOUTS:  idx = 7; break;
	case RESOURCE_ID_IMAGE_IM:  idx = 8; break;
	case RESOURCE_ID_IMAGE_INSTGRAM:  idx = 9; break;
	case RESOURCE_ID_IMAGE_LINKEDIN:  idx = 10; break;
	case RESOURCE_ID_IMAGE_MESSAGE:  idx = 11; break;
	case RESOURCE_ID_IMAGE_NOBT:  idx = 12; break;
	case RESOURCE_ID_IMAGE_PHONE:  idx = 13; break;
	case RESOURCE_ID_IMAGE_SKYPE:  idx = 14; break;
	case RESOURCE_ID_IMAGE_VK:  idx = 15; break;
	case RESOURCE_ID_IMAGE_VOIP:  idx = 16; break;
	case RESOURCE_ID_IMAGE_WARNING:  idx = 17; break;
	case RESOURCE_ID_IMAGE_MORE_NOTIFICATIONS:  idx = 18; break;
	case RESOURCE_ID_IMAGE_EMPTY:  idx = 19; break;
	case RESOURCE_ID_IMAGE_VIBER:  idx = 20; break;
	case RESOURCE_ID_IMAGE_TELEGRAM: idx = 21; break;
	}    
	
	if (idx == -1 )
		return NULL;

	if (notification_icons[idx] == NULL)
	{
		notification_icons[idx] = gbitmap_create_with_resource(id);
	}

	return notification_icons[idx];
}


void handle_minute_tick(struct tm *tick_time, TimeUnits units_changed)
{
	// Need to be static because they're used by the system later.
	static char time_text[] = "00:00";
	static char date_text[] = "Xxxxxxxxx 00";
	int8_t new_bt_disconnected = 0;

	char *time_format;

	time_t now = time(NULL);

	if (!tick_time) 
	{
		tick_time = localtime(&now);
	}
	
	strftime(date_text, sizeof(date_text), "%a, %e %b", tick_time);
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

	new_bt_disconnected = bluetooth_connection_service_peek() ? 0 : 1; 

	if (time(NULL) - last_bt_update > BT_DISCONNECTED_TIMEOUT) // 7.5 mins
	{
		new_bt_disconnected = 1;
	}

	if (bt_disconnected != new_bt_disconnected)
	{
		send_request(); // force it to send us an update
	}

	bt_disconnected = new_bt_disconnected;

	if (bt_disconnected)
		phone_charge_level = -1; 

	display_indicators();
	
	layer_mark_dirty(notification_layer);
}

void display_notification_icon(GContext* ctx, int id, int *current_pos)
{
	GRect bounds; 

	if (!(*current_pos < NUM_ICON_POSITIONS_TOTAL))
		return;

	GBitmap* icon = get_icon_for_id(id);
	if (icon == NULL)
		return;

	bounds.size.w = 24;
	bounds.size.h = 24;

	if (*current_pos < NUM_ICON_POSITIONS_LINE1 )
	{
		bounds.origin.x = (*current_pos) * 28 + 2;
		bounds.origin.y = 2;
	}
	else
	{
		bounds.origin.x = (*current_pos - NUM_ICON_POSITIONS_LINE1) * 28 + 2;
		bounds.origin.y = 28 + 2;
	}
  
	graphics_draw_bitmap_in_rect(ctx, icon, bounds);

	++(*current_pos);
}

void notifications_update_callback(Layer * layer, GContext * ctx)
{
	int next_notification_icon_pos = 0;

	int weather_warning = 0;

	graphics_context_set_fill_color(ctx, GColorBlack);
	graphics_fill_rect(ctx, layer_get_bounds(layer), 0, GCornerNone);
	
	switch(weather_code)
	{
		case TORNADO: // 0, // 	tornado
		case TROPICAL_STORM: //1,	//	tropical storm
		case HURRICANE: //2,	//	hurricane
			weather_warning = 3;
			break;
	
		case SEVERE_THUNDERSTORMS: //3,	//	severe thunderstorms
			weather_warning = 1;
			break; 

		case HEAVY_SNOW_1: //41,	//	heavy snow
		case HEAVY_SNOW_2: //43,	//	heavy snow
			weather_warning = 1;
			break;
	}
  
	if (weather_warning >= 3)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_EMERGENCY, &next_notification_icon_pos);
	else if (weather_warning >= 1)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_WARNING, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_CALENDAR)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_CALENDAR, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_GMAIL)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_GMAIL, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_EMAIL)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_EMAIL, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_PHONE)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_PHONE, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_VIBER)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_VIBER, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_MESSAGES)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_MESSAGE, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_SKYPE)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_SKYPE, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_GOOGLEHANGOUTS)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_HANGOUTS, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_GOOGLEPLUS)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_GPLUS, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_VOIP)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_VOIP, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_TELEGRAM)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_TELEGRAM, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_IM)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_IM, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_FACEBOOK)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_FACEBOOK, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_LINKEDIN)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_LINKEDIN, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_VK)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_VK, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_INSTGRAM)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_INSTGRAM, &next_notification_icon_pos);

	if (notifications_bitmask & NOTIFICATION_UNKNOWN)
		display_notification_icon(ctx, RESOURCE_ID_IMAGE_MORE_NOTIFICATIONS, &next_notification_icon_pos);

/*	if (bt_disconnected)
	{
		GBitmap* icon = get_icon_for_id(RESOURCE_ID_IMAGE_NOBT);
		if (icon != NULL)
		{
			graphics_draw_bitmap_in_rect(ctx, icon, GRect(2,2,28*2-2,28-2));
		}
	} */
}

char pct_to_hex(int pct)
{
	if (pct >= 0 && pct <= 100)
	{
		int xpct = (pct+4)*15/100;
		if (xpct < 10)
			return '0' + xpct;
		return 'A' + xpct - 10;
	}
	return '?';
}

void display_indicators()
{
	const char *weather_cd = NULL;
  
    static char watchBatt[8];
    static char phoneBatt[8];
  
	switch (weather_code)
	{
		case TORNADO: // 0, // 	tornado
			weather_cd = "  !! TORNADO !!";
			break; 
		
		case TROPICAL_STORM: //1,	//	tropical storm
			weather_cd = "!! TROPICAL STORM !!";
			break; 
		
		case HURRICANE: //2,	//	hurricane
			weather_cd = " !! HURRICANE !!";
			break; 
		
		case SEVERE_THUNDERSTORMS: //3,	//	severe thunderstorms
			weather_cd = " THUNDERSTORMS !!";
			break; 

		case ISOLATED_THUNDERSTORMS: //37,	//	isolated thunderstorms
		case SCATTERED_THUNDERSTORMS_1: //38,	//	scattered thunderstorms
		case SCATTERED_THUNDERSTORMS_2: //39,	//	scattered thunderstorms
		case ISOLATED_THUNDERSTORMS_2: //47	//	isolated thundershowers
			weather_cd = " THUNDERSTORMS";
			break; 
		
		case THUNDERSTORMS_2: //45,	//	thundershowers
			weather_cd = " THUNDERSHOWERS";
			break; 
		
		case THUNDERSTORMS: //4,	//	thunderstorms
			weather_cd = " THUNDERSTORMS";
			break; 
		
		case MIXED_RAIN_AND_SNOW: //5,	//	mixed rain and snow
			weather_cd = " RAIN & SNOW";
			break; 
		
		case MIXED_RAIND_AND_SLEET: //6,	//	mixed rain and sleet
			weather_cd = " RAIN & SLEET";
			break; 
		
		case MIXED_RAIN_AND_HAIL: //35,	//	mixed rain and hail
			weather_cd = " RAIN & HAIL";
			break; 
		
		case MIXED_SNOW_AND_SLEET: //7,	//	mixed snow and sleet
			weather_cd = " SNOW & SLEET";
			break; 
		
		case FREEZING_DRIZZLE: //8,	//	freezing drizzle
			weather_cd = " FREEZING DRIZZLE";
			break; 
		
		case DRIZZLE: //9,	//	drizzle
			weather_cd = " DRIZZLE";
			break; 
		
		case FREEZING_RAIN: //10,	//	freezing rain
			weather_cd = " FREEZING RAIN";
			break; 
		
		case SHOWERS_1: //11,	//	showers
		case SHOWERS_2: //12,	//	showers
			weather_cd = " SHOWERS";
			break; 

		case SCATTERED_SHOWERS: //40,	//	scattered showers
			weather_cd = " SCTRD SHOWERS";
			break; 
		
		
		case HAIL: //17,	//	hail
			weather_cd = " HAIL";
			break; 
		
		case SLEET: //18,	//	sleet
			weather_cd = " SLEET";
			break; 
		
		case DUST: //19,	//	dust
			weather_cd = " DUST";
			break; 
		
		case FOGGY: //20,	//	foggy
			weather_cd = " FOGGY";
			break; 
		
		case HAZE: //21,	//	haze
			weather_cd = " HAZE";
			break; 
		
		case SMOKY: //22,	//	smoky
			weather_cd = " SMOKY";
			break; 
		
		case BLUSTERY: //23,	//	blustery
			weather_cd = " BLUSTERY";
			break; 
		
		case WINDY: //24,	//	windy
			weather_cd = " WINDY";
			break; 
		
		case COLD: //25,	//	cold
			weather_cd = " COLD";
			break; 
		
		case CLOUDY: //26,	//	cloudy
			weather_cd = " CLOUDY";
			break; 
		
		case MOSTLY_CLOUDY_NIGHT: //27,	//	mostly cloudy (night)
		case MOSTLY_CLOUDY_DAY: //28,	//	mostly cloudy (day)
			weather_cd = " MSTLY CLOUDY";
			break; 
		
		case PARTLY_CLOUDY_NIGHT: //29,	//	partly cloudy (night)
		case PARTLY_CLOUDY_DAY: //30,	//	partly cloudy (day)
		case PARTLY_CLOUDY: //44,	//	partly cloudy
			weather_cd = " PRTLY CLOUDY";
			break; 
		
		case CLEAR: //31,	//	clear (night)
			weather_cd = " CLEAR";
			break; 
		
		case SUNNY: //32,	//	sunny
			weather_cd = " SUNNY";
			break; 

		case FAIR_NIGHT: //33,	//	fair (night)
		case FAIR_DAY: //34,	//	fair (day)
			weather_cd = " FAIR";
			break; 

		case HOT: //36,	//	hot
			weather_cd = " HOT";
			break; 
		
		case SNOW_FLURRIES: //13,	//	snow flurries
			weather_cd = " SNOW FLURRIES";
			break; 
		
		case LIGHT_SNOW_SHOWERS: //14,	//	light snow showers
			weather_cd = "LIGHT SNOW FLURRIES";
			break; 
		
		case BLOWING_SNOW: //15,	//	blowing snow
			weather_cd = " BLOWING SNOW";
			break; 
		
		case SNOW: //16,	//	snow
			weather_cd = " SNOW";
			break; 

		case HEAVY_SNOW_1: //41,	//	heavy snow
		case HEAVY_SNOW_2: //43,	//	heavy snow
			weather_cd = "!! HEAVY SNOW !!";
			break;

		case SNOW_SHOWERS : //46,	//	snow showers
			weather_cd = " SNOW SHOWERS";
			break;
		
		case SCATTERED_SNOW_SHOWERS: //42,	//	scattered snow showers
			weather_cd = "SCTR SNOW SHWRS";
			break;
	}

	if (weather_cd != NULL)
		text_layer_set_text(weather_status_layer, weather_cd);
	else
		text_layer_set_text(weather_status_layer, "");

  if (phone_charge_level != -1) 
  {
    int idx = 0;
    
    if (phone_charge_level <= 30) 
    {
      phoneBatt[idx++] = '!';
      phoneBatt[idx++] = '!';      
    }
    
    phoneBatt[idx++] = 'p';
    if (phone_charge_level >= 95)
      phoneBatt[idx++] = 'F';
    else 
      phoneBatt[idx++] = '0' + ((phone_charge_level + 5) / 10);
    
    phoneBatt[idx++] = 0;
    
    text_layer_set_text(txt_layers[1], phoneBatt);  
  }
  else 
  {
    text_layer_set_text(txt_layers[1], "");
  }

  if (watch_charge_level != -1) 
  {
    int idx = 0;
    
    if (watch_charge_level < 30 && !watch_is_charging) 
    {
      watchBatt[idx++] = '!';
      watchBatt[idx++] = '!';           
    }
    
    if (watch_is_charging)
      watchBatt[idx++] = '+';

    watchBatt[idx++] = 'w';
    
    if (watch_charge_level >= 95)
      watchBatt[idx++] = 'F';
    else 
      watchBatt[idx++] = '0' + ((watch_charge_level + 5) / 10);
    
    watchBatt[idx++] = 0;

    text_layer_set_text(txt_layers[0], watchBatt);  
  }
  else 
  {
    text_layer_set_text(txt_layers[0], "w??");
  }

/*  for (int i =2; i < NUM_TXT_LAYERS; ++ i) 
  {
    if (i != 4)
      text_layer_set_text(txt_layers[i], "ABCD");
    else 
      text_layer_set_text(txt_layers[i], "HELLO WORLD HELL YEAH");
  } */
  
  if (bt_disconnected)
  {
    text_layer_set_text(txt_layers[4], " - NO BT CONN - ");
  }
  
}

void received_data(DictionaryIterator *received, void *context) 
{
	Tuple *entry = NULL;

	bt_disconnected = 0;

	last_bt_update = time(NULL);

	entry = dict_find(received, ENTRY_NOTIFICATIONS_BITMASK); 
	if (entry != NULL)
	{
		notifications_bitmask = entry->value->int32;    
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
	}
	else if (weather_code != 0 ) // alert disappeared
	{
		weather_code = DEFAULT_WEATHER_CODE;
	}
	
	layer_mark_dirty(notification_layer);
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
	dict_write_uint8(iterator, MSG_REQUEST_WATCHFACE_STATUS, 0); // some unused code -- we just need to send a 'ping' to app, without any real data
	app_message_outbox_send();
}

void handle_deinit(void)
{
	int idx = 0;

	tick_timer_service_unsubscribe();
	battery_state_service_unsubscribe();

	layer_destroy(notification_layer);

	for (idx = 0; idx < NUM_ICONS; ++idx)
	{
		if (notification_icons[idx] != NULL)
			gbitmap_destroy(notification_icons[idx]);
	}
}

void handle_init(void)
{
	int idx;

	for (idx = 0; idx < NUM_ICONS; ++idx)
		notification_icons[idx] = NULL;

	last_bt_update = time(NULL)-BT_DISCONNECTED_TIMEOUT+10;

	window = window_create();
	window_stack_push(window, true /* Animated */ );
	window_set_background_color(window, GColorBlack);

	Layer *window_layer = window_get_root_layer(window);


	// Notifications layer
	notification_layer = layer_create( (GRect) { .origin = { 0, 0 }, .size = { 168, 66 } });

	layer_set_update_proc(window_layer, notifications_update_callback);
	layer_add_child(window_layer, notification_layer);

	// Date layer
	text_date_layer = text_layer_create(GRect(8, 66, 114 - 8, 25));
	text_layer_set_text_color(text_date_layer, GColorWhite);
	text_layer_set_background_color(text_date_layer, GColorClear);
	text_layer_set_font(text_date_layer, fonts_get_system_font (FONT_KEY_ROBOTO_CONDENSED_21));
	layer_add_child(window_layer, text_layer_get_layer(text_date_layer));

	// Time layer
	text_time_layer = text_layer_create(GRect(7, 89, 144 - 7, 49));
	text_layer_set_text_color(text_time_layer, GColorWhite);
	text_layer_set_background_color(text_time_layer, GColorClear);
	text_layer_set_font(text_time_layer,
			    fonts_get_system_font
			    (FONT_KEY_ROBOTO_BOLD_SUBSET_49));
	layer_add_child(window_layer, text_layer_get_layer(text_time_layer));


	// P & W letters layer
	
  for (int i =0; i < NUM_TXT_LAYERS; ++ i ) 
  {
    if (i != 4) 
    {
      txt_layers[i] = text_layer_create(GRect(114, i*13, 28, 24));
    	text_layer_set_text_color(txt_layers[i], GColorWhite);
    	text_layer_set_background_color(txt_layers[i], GColorClear);
    	text_layer_set_font(txt_layers[i], fonts_get_system_font(FONT_KEY_GOTHIC_14));
      text_layer_set_text_alignment(txt_layers[i], GTextAlignmentRight);
    	layer_add_child(window_layer, text_layer_get_layer(txt_layers[i]));      
    }
    else 
    {
      txt_layers[i] = text_layer_create(GRect(2, 52, 140, 24));
      text_layer_set_text_color(txt_layers[i], GColorWhite);
      text_layer_set_background_color(txt_layers[i], GColorClear);
      text_layer_set_font(txt_layers[i], fonts_get_system_font(FONT_KEY_GOTHIC_14));
      layer_add_child(window_layer, text_layer_get_layer(txt_layers[i])); 
    }
  }
  
  // weather alarm layer
	weather_status_layer = text_layer_create(GRect(4, 146, 144-4, 18));
	text_layer_set_text_color(weather_status_layer, GColorWhite);
	text_layer_set_background_color(weather_status_layer, GColorClear);
	text_layer_set_font(weather_status_layer,
			    fonts_get_system_font
			    (FONT_KEY_GOTHIC_18_BOLD));
	layer_add_child(window_layer, text_layer_get_layer(weather_status_layer));

	// setup communication
	app_message_register_inbox_received(received_data);
	app_message_open(124, 50);

	// battery state service 
	battery_state_service_subscribe(watch_battery_changed);
	
	// Time callback setup
	tick_timer_service_subscribe(MINUTE_UNIT, handle_minute_tick);
	handle_minute_tick(NULL, MINUTE_UNIT);
      	

	watch_battery_changed(battery_state_service_peek());

	// request everything. 
	send_request();
}

int main(void)
{
	handle_init();

	app_event_loop();

	handle_deinit();
}
