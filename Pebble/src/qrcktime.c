#include "pebble.h"

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

	if (!tick_time) {
		time_t now = time(NULL);
		tick_time = localtime(&now);
	}
	
	// TODO: Only update the date when it's changed.
	strftime(date_text, sizeof(date_text), "%a  %B %e", tick_time);
	text_layer_set_text(text_date_layer, date_text);

	if (clock_is_24h_style()) {
		time_format = "%R";
	} else {
		time_format = "%I:%M";
	}

	strftime(time_text, sizeof(time_text), time_format, tick_time);

	// Kludge to handle lack of non-padded hour format string
	// for twelve hour clock.
	if (!clock_is_24h_style() && (time_text[0] == '0')) {
		memmove(time_text, &time_text[1], sizeof(time_text) - 1);
	}

	text_layer_set_text(text_time_layer, time_text);

	text_layer_set_text(notifications_layer, "NO NOTIFICATIONS");
	
	text_layer_set_text(bt_status_layer, "NO CONN");
	text_layer_set_text(phone_batt_layer, "N/A");
	text_layer_set_text(watch_batt_layer, "N/A");
}

/*void notification_newNotification(DictionaryIterator *received)
{
	set_busy_indicator(true);

	int32_t id = dict_find(received, 1)->value->int32;

	uint8_t* configBytes = dict_find(received, 2)->value->data;

	uint8_t flags = configBytes[1];
	bool inList = (flags & 0x02) != 0;

	Notification* notification = notification_find_notification(id);
	if (notification == NULL)
	{
		notification = notification_add_notification();

		if (!inList)
		{
			if (config_vibrateMode > 0 && (!config_dontVibrateWhenCharging || !battery_state_service_peek().is_charging))
			{
				if (numOfNotifications == 1 && config_vibrateMode == 1)
					vibes_long_pulse();
				else
					vibes_short_pulse();

				vibrating = true;
				app_timer_register(700, vibration_stopped, NULL);
			}

			if (config_lightScreen)
				light_enable_interaction();

			appIdle = true;
			elapsedTime = 0;
		}
	}

	notification->id = id;
	notification->inList = inList;
	notification->dismissable = (flags & 0x01) != 0;
	notification->numOfChunks = dict_find(received, 4)->value->uint8;

	strcpy(notification->title, dict_find(received, 5)->value->cstring);
	strcpy(notification->subTitle, dict_find(received, 6)->value->cstring);
	notification->text[0] = 0;

	if (notification->inList)
	{
		for (int i = 0; i < numOfNotifications; i++)
		{

			Notification entry = notificationData[notificationPositions[i]];
			if (entry.id == notification->id)
				continue;

			if (entry.inList)
			{
				notification_remove_notification(i, false);
				i--;
			}
		}
	}

	if (notification->numOfChunks == 0)
	{
		notification_sendNextNotification();
	}
	else
	{
		notification_sendMoreText(notification->id, 0);
	}

	if (numOfNotifications == 1)
		refresh_notification();
	else if (config_autoSwitchNotifications)
	{
		pickedNotification = numOfNotifications - 1;
		refresh_notification();
	}
}
*/

void update_notifications()
{
	text_layer_set_text(notifications_layer, "NO NOTIFICATIONS");
}

void received_data(DictionaryIterator *received, void *context) 
{
	uint8_t packetId = dict_find(received, 0)->value->uint8;

	if (packetId == 0) // notifications bitmask
	{
		notifications_bitmask = dict_find(received, 1)->value->int32;
	}
	else if (packetId == 1) // battery charge level
	{
	}
}

void handle_deinit(void)
{
	tick_timer_service_unsubscribe();
}

void handle_init(void)
{
	window = window_create();
	window_stack_push(window, true /* Animated */ );
	window_set_background_color(window, GColorBlack);

	Layer *window_layer = window_get_root_layer(window);

	// Notifications text layer
	notifications_layer = text_layer_create(GRect(4, 4, 144-8, 64));
	text_layer_set_text_color(notifications_layer, GColorWhite);
	text_layer_set_background_color(notifications_layer, GColorClear);
	text_layer_set_font(notifications_layer,
			    fonts_get_system_font
			    (FONT_KEY_GOTHIC_14));
	layer_add_child(window_layer, text_layer_get_layer(notifications_layer));

	// Date layer
	text_date_layer = text_layer_create(GRect(8, 68, 144 - 8, 168 - 68));
	text_layer_set_text_color(text_date_layer, GColorWhite);
	text_layer_set_background_color(text_date_layer, GColorClear);
	text_layer_set_font(text_date_layer,
			    fonts_get_system_font
			    (FONT_KEY_ROBOTO_CONDENSED_21));
	layer_add_child(window_layer, text_layer_get_layer(text_date_layer));

	// Time layer
	text_time_layer = text_layer_create(GRect(7, 92, 144 - 7, 168 - 92));
	text_layer_set_text_color(text_time_layer, GColorWhite);
	text_layer_set_background_color(text_time_layer, GColorClear);
	text_layer_set_font(text_time_layer,
			    fonts_get_system_font
			    (FONT_KEY_ROBOTO_BOLD_SUBSET_49));
	layer_add_child(window_layer, text_layer_get_layer(text_time_layer));

	// Upper line layer
	GRect top_line_frame = GRect(8, 97, 130, 2);
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
	text_layer_set_text_color(phone_batt_layer, GColorWhite);
	text_layer_set_background_color(phone_batt_layer, GColorClear);
	text_layer_set_font(phone_batt_layer,
			    fonts_get_system_font
			    (FONT_KEY_GOTHIC_14));
	layer_add_child(window_layer, text_layer_get_layer(phone_batt_layer));

	// Watch battery layer
	watch_batt_layer = text_layer_create(GRect(114, 152, 28, 14));
	text_layer_set_text_color(watch_batt_layer, GColorWhite);
	text_layer_set_background_color(watch_batt_layer, GColorClear);
	text_layer_set_font(watch_batt_layer,
			    fonts_get_system_font
			    (FONT_KEY_GOTHIC_14));
	layer_add_child(window_layer, text_layer_get_layer(watch_batt_layer));


	// Time callback setup
	tick_timer_service_subscribe(MINUTE_UNIT, handle_minute_tick);
	handle_minute_tick(NULL, MINUTE_UNIT);

	// setup communication
	app_message_register_inbox_received(received_data);
	app_message_open(124, 50);

	app_comm_set_sniff_interval(SNIFF_INTERVAL_NORMAL);
}

int main(void)
{
	handle_init();

	app_event_loop();

	handle_deinit();
}
