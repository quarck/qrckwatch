#include "pebble.h"

#include "../../Pebble/src/protocol.h"

#define NUM_MENU_SECTIONS 2

static Window *window;

static SimpleMenuLayer *simple_menu_layer;

static SimpleMenuSection menu_sections[NUM_MENU_SECTIONS];

static SimpleMenuItem menu_items_watch[NUM_DISMISSABLE_ITEMS];
static SimpleMenuItem menu_items_phone[NUM_DISMISSABLE_ITEMS];


void send_request(bool isWatch, uint8_t id)
{
	DictionaryIterator *iterator = NULL;
	app_message_outbox_begin(&iterator);
	dict_write_uint8(iterator, ENTRY_DISMISS_LEVEL, isWatch ? LEVEL_WATCH : LEVEL_PHONE); 
	dict_write_uint8(iterator, ENTRY_DISMISS_ID, id); 
	app_message_outbox_send();
}


static void dismiss_watch_callback(int index, void *ctx)
{
	send_request(true, index);

	SimpleMenuItem *menu_item = &menu_items_watch[index];
	menu_item->subtitle = "Sent";
	layer_mark_dirty(simple_menu_layer_get_layer(simple_menu_layer)); 
}

static void dismiss_phone_callback(int index, void *ctx)
{
	send_request(false, index);

	SimpleMenuItem *menu_item = &menu_items_phone[index];
	menu_item->subtitle = "Sent";
	layer_mark_dirty(simple_menu_layer_get_layer(simple_menu_layer)); 
}

// This initializes the menu upon window load
static void window_load(Window * window)
{
	menu_items_watch[DISMISSABLE_ITEM_VIBER] = (SimpleMenuItem)  { .title = "Viber", .callback = dismiss_watch_callback, };
	menu_items_watch[DISMISSABLE_ITEM_GMAIL] = 	(SimpleMenuItem) { .title = "Gmail", .callback = dismiss_watch_callback, };
	menu_items_watch[DISMISSABLE_ITEM_MAIL] = 	(SimpleMenuItem) { .title = "Mail", .callback = dismiss_watch_callback, };
	menu_items_watch[DISMISSABLE_ITEM_CALENDAR] = (SimpleMenuItem) { .title = "Calendar", .callback = dismiss_watch_callback, };
	menu_items_watch[DISMISSABLE_ITEM_EVERYTHING] = (SimpleMenuItem) { .title = "** Everything **", .callback = dismiss_watch_callback, };

	menu_items_phone[DISMISSABLE_ITEM_VIBER] = (SimpleMenuItem)  { .title = "Viber", .callback = dismiss_phone_callback, };
	menu_items_phone[DISMISSABLE_ITEM_GMAIL] = 	(SimpleMenuItem) { .title = "Gmail", .callback = dismiss_phone_callback, };
	menu_items_phone[DISMISSABLE_ITEM_MAIL] = 	(SimpleMenuItem) { .title = "Mail", .callback = dismiss_phone_callback, };
	menu_items_phone[DISMISSABLE_ITEM_CALENDAR] = (SimpleMenuItem) { .title = "Calendar", .callback = dismiss_phone_callback, };
	menu_items_phone[DISMISSABLE_ITEM_EVERYTHING] = (SimpleMenuItem) { .title = "** Everything **", .callback = dismiss_phone_callback, };

	menu_sections[0] =  (SimpleMenuSection) { .num_items = NUM_DISMISSABLE_ITEMS, .items = menu_items_watch, };
	menu_sections[1] =  (SimpleMenuSection) { .num_items = NUM_DISMISSABLE_ITEMS, .items = menu_items_phone, .title = "From phone" };

	Layer *window_layer = window_get_root_layer(window);
	GRect bounds = layer_get_frame(window_layer);

	// Initialize the simple menu layer
	simple_menu_layer =
	    simple_menu_layer_create(bounds, window, menu_sections,
				     NUM_MENU_SECTIONS, NULL);

	// Add it to the window for display
	layer_add_child(window_layer,
			simple_menu_layer_get_layer(simple_menu_layer));
}

// Deinitialize resources on window unload that were initialized on window load
void window_unload(Window * window)
{
	simple_menu_layer_destroy(simple_menu_layer);
}

void received_data(DictionaryIterator *received, void *context) 
{
	int level = LEVEL_PHONE;
	uint8_t idx = 0;

	Tuple *entry = NULL;

	entry = dict_find(received, ENTRY_DISMISS_LEVEL); 
	if (entry != NULL)
	{
		level = entry->value->uint8;
		APP_LOG(APP_LOG_LEVEL_DEBUG, "has got level: %d", (int)level);
	}
	else
	{
		APP_LOG(APP_LOG_LEVEL_DEBUG, "NO LEVEL");
	}
	
	entry = dict_find(received, ENTRY_DISMISS_ID); 
	if (entry != NULL)
	{
		idx = entry->value->uint8;
		APP_LOG(APP_LOG_LEVEL_DEBUG, "idx: %d", (int)idx);
	}
	else
	{
		APP_LOG(APP_LOG_LEVEL_DEBUG, "NO ID");
	}

	if (idx < NUM_DISMISSABLE_ITEMS)
	{
		SimpleMenuItem *menu_item = (level == LEVEL_PHONE ? &menu_items_phone[idx] : &menu_items_watch[idx]);

		if (menu_item != NULL)
		{
			menu_item->subtitle = "Dismissed";
			layer_mark_dirty(simple_menu_layer_get_layer(simple_menu_layer)); 
		}
	}
}

int main(void)
{
	// setup communication
	app_comm_set_sniff_interval(SNIFF_INTERVAL_REDUCED);
	app_message_register_inbox_received(received_data);
	app_message_open(124, 50);

	window = window_create();

	// Setup the window handlers
	window_set_window_handlers(window, 
			(WindowHandlers) 
			{
				.load = window_load,
				.unload = window_unload,
			});

	window_stack_push(window, true);

	app_event_loop();
	app_comm_set_sniff_interval(SNIFF_INTERVAL_NORMAL);

	window_destroy(window);
}
