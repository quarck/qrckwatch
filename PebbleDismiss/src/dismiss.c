#include "pebble.h"

#include "../../Pebble/src/protocol.h"

static Window *level1_window;
static SimpleMenuLayer *level1_menu_layer;
static SimpleMenuSection level1_menu_sections[1];
static SimpleMenuItem level1_menu_items[NUM_DISMISSABLE_ITEMS];

#define LEVEL2_MENU_ITEM_WATCH 0
#define LEVEL2_MENU_ITEM_PHONE 1
static Window *level2_window;
static SimpleMenuLayer *level2_menu_layer;
static SimpleMenuSection level2_menu_sections[1];
static SimpleMenuItem level2_menu_items[2];

static int current_dismiss_index = 100;

void send_request(bool isWatch, uint8_t id)
{
	DictionaryIterator *iterator = NULL;
	app_message_outbox_begin(&iterator);
	dict_write_uint8(iterator, ENTRY_DISMISS_LEVEL, isWatch ? LEVEL_WATCH : LEVEL_PHONE); 
	dict_write_uint8(iterator, ENTRY_DISMISS_ID, id); 
	app_message_outbox_send();
}


static void dismiss_level1_callback(int index, void *ctx)
{
	current_dismiss_index = index;
	window_stack_push(level2_window, true);
}

static void dismiss_level2_callback(int index, void *ctx)
{
	send_request(index == LEVEL2_MENU_ITEM_WATCH, current_dismiss_index); 
	window_stack_pop(true);
}

static void level1_window_load(Window * window)
{
	level1_menu_items[DISMISSABLE_ITEM_VIBER] = (SimpleMenuItem)  { .title = "Viber", .callback = dismiss_level1_callback, };
	level1_menu_items[DISMISSABLE_ITEM_GMAIL] = 	(SimpleMenuItem) { .title = "Gmail", .callback = dismiss_level1_callback, };
	level1_menu_items[DISMISSABLE_ITEM_MAIL] = 	(SimpleMenuItem) { .title = "Mail", .callback = dismiss_level1_callback, };
	level1_menu_items[DISMISSABLE_ITEM_CALENDAR] = (SimpleMenuItem) { .title = "Calendar", .callback = dismiss_level1_callback, };
	level1_menu_items[DISMISSABLE_ITEM_EVERYTHING] = (SimpleMenuItem) { .title = "** Everything **", .callback = dismiss_level1_callback, };

	level1_menu_sections[0] =  (SimpleMenuSection) { .num_items = NUM_DISMISSABLE_ITEMS, .items = level1_menu_items, };

	Layer *window_layer = window_get_root_layer(window);
	GRect bounds = layer_get_frame(window_layer);

	level1_menu_layer = simple_menu_layer_create(bounds, window, level1_menu_sections, 1, NULL);

	layer_add_child(window_layer, simple_menu_layer_get_layer(level1_menu_layer));
}

void level1_window_unload(Window * window)
{
	simple_menu_layer_destroy(level1_menu_layer);
}

static void level2_window_load(Window * window)
{
	level2_menu_items[LEVEL2_MENU_ITEM_WATCH] = (SimpleMenuItem)  { .title = "Watch", .callback = dismiss_level2_callback, };
	level2_menu_items[LEVEL2_MENU_ITEM_PHONE] = 	(SimpleMenuItem) { .title = "Phone", .callback = dismiss_level2_callback, };

	level2_menu_sections[0] =  (SimpleMenuSection) { .num_items = 2, .items = level2_menu_items, };

	Layer *window_layer = window_get_root_layer(window);
	GRect bounds = layer_get_frame(window_layer);

	level2_menu_layer = simple_menu_layer_create(bounds, window, level2_menu_sections, 1, NULL);

	layer_add_child(window_layer, simple_menu_layer_get_layer(level2_menu_layer));
}

void level2_window_unload(Window * window)
{
	simple_menu_layer_destroy(level2_menu_layer);
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
/*
	if (idx < NUM_DISMISSABLE_ITEMS)
	{
		SimpleMenuItem *menu_item = (level == LEVEL_PHONE ? &menu_items_phone[idx] : &level1_menu_items[idx]);

		if (menu_item != NULL)
		{
			menu_item->subtitle = "Dismissed";
			layer_mark_dirty(level1_menu_layer_get_layer(level1_menu_layer)); 
		}
	} */
}

int main(void)
{
	// setup communication
	app_comm_set_sniff_interval(SNIFF_INTERVAL_REDUCED);
	app_message_register_inbox_received(received_data);
	app_message_open(124, 50);

	level1_window = window_create();

	// Setup the window handlers
	window_set_window_handlers(level1_window, 
			(WindowHandlers) 
			{
				.load = level1_window_load,
				.unload = level1_window_unload,
			});

	level2_window = window_create();

	// Setup the window handlers
	window_set_window_handlers(level2_window, 
			(WindowHandlers) 
			{
				.load = level2_window_load,
				.unload = level2_window_unload,
			});

	window_stack_push(level1_window, true);

	app_event_loop();
	app_comm_set_sniff_interval(SNIFF_INTERVAL_NORMAL);

	window_destroy(level2_window);
	window_destroy(level1_window);
}
