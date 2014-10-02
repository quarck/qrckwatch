#include "pebble.h"

#include "../../Pebble/src/protocol.h"

static Window *level1_window;
static SimpleMenuLayer *level1_menu_layer;
static SimpleMenuSection level1_menu_sections[1];
static SimpleMenuItem level1_menu_items[NUM_DISMISSABLE_ITEMS];
static GBitmap *level1_menu_icon_image[NUM_DISMISSABLE_ITEMS];


void send_request(bool isWatch, uint8_t id)
{
	DictionaryIterator *iterator = NULL;
	app_message_outbox_begin(&iterator);
	dict_write_uint8(iterator, ENTRY_DISMISS_LEVEL, isWatch ? LEVEL_WATCH : LEVEL_PHONE); 
	dict_write_uint8(iterator, ENTRY_DISMISS_ID, id); 
	app_message_outbox_send();
}

int idx2id(int idx)
{
	int id = idx + 1;
	if (idx == NUM_DISMISSABLE_ITEMS -1 )
		id = DISMISSABLE_ITEM_EVERYTHING;
	return id;
}

int id2idx(int id)
{
	int idx = id - 1;
	if (id == DISMISSABLE_ITEM_EVERYTHING) 
		idx = NUM_DISMISSABLE_ITEMS-1;
	return idx;
}


static void dismiss_level1_callback(int index, void *ctx)
{
	int id = idx2id(index);

	if (id == DISMISSABLE_ITEM_HIDE_EVERYTHING)
		send_request(true, DISMISSABLE_ITEM_EVERYTHING); 
	else
		send_request(false, id);

	window_stack_pop_all(true);
}


void do_menu(int id, int imgid, const char *title)
{
	int pos = id2idx(id);
	int iconid = id;

	if (iconid == DISMISSABLE_ITEM_HIDE_EVERYTHING)
		iconid = DISMISSABLE_ITEM_EVERYTHING;

	level1_menu_icon_image[iconid] = gbitmap_create_with_resource(imgid); 
	level1_menu_items[pos] = 
		(SimpleMenuItem) { .title = title, .callback = dismiss_level1_callback, .icon = level1_menu_icon_image[iconid], };
}

static void level1_window_load(Window * window)
{
	
	do_menu(DISMISSABLE_ITEM_VIBER, RESOURCE_ID_IMAGE_VIBER, "Viber");
	do_menu(DISMISSABLE_ITEM_GMAIL, RESOURCE_ID_IMAGE_GMAIL, "Gmail");
	do_menu(DISMISSABLE_ITEM_MAIL, RESOURCE_ID_IMAGE_EMAIL, "Mail");
	do_menu(DISMISSABLE_ITEM_CALENDAR, RESOURCE_ID_IMAGE_CALENDAR, "Calendar");
	do_menu(DISMISSABLE_ITEM_PHONE, RESOURCE_ID_IMAGE_PHONE, "Phone");
	do_menu(DISMISSABLE_ITEM_MESSAGES, RESOURCE_ID_IMAGE_MESSAGE, "SMS");
	do_menu(DISMISSABLE_ITEM_GOOGLEHANGOUTS, RESOURCE_ID_IMAGE_HANGOUTS, "Hangouts");
	do_menu(DISMISSABLE_ITEM_SKYPE, RESOURCE_ID_IMAGE_SKYPE, "Skype");
	
	do_menu(DISMISSABLE_ITEM_HIDE_EVERYTHING, RESOURCE_ID_IMAGE_MENU_ICON, "** Hide All **");
	do_menu(DISMISSABLE_ITEM_EVERYTHING, RESOURCE_ID_IMAGE_MENU_ICON, "** Everything **");

	level1_menu_sections[0] =  (SimpleMenuSection) { .num_items = NUM_DISMISSABLE_ITEMS, .items = level1_menu_items, };

	Layer *window_layer = window_get_root_layer(window);
	GRect bounds = layer_get_frame(window_layer);

	level1_menu_layer = simple_menu_layer_create(bounds, window, level1_menu_sections, 1, NULL);

	layer_add_child(window_layer, simple_menu_layer_get_layer(level1_menu_layer));
}

void level1_window_unload(Window * window)
{
	gbitmap_destroy(level1_menu_icon_image[DISMISSABLE_ITEM_EVERYTHING]);
	gbitmap_destroy(level1_menu_icon_image[DISMISSABLE_ITEM_VIBER]);
	gbitmap_destroy(level1_menu_icon_image[DISMISSABLE_ITEM_GMAIL]);
	gbitmap_destroy(level1_menu_icon_image[DISMISSABLE_ITEM_MAIL]);
	gbitmap_destroy(level1_menu_icon_image[DISMISSABLE_ITEM_CALENDAR]);
	gbitmap_destroy(level1_menu_icon_image[DISMISSABLE_ITEM_PHONE]);
	gbitmap_destroy(level1_menu_icon_image[DISMISSABLE_ITEM_MESSAGES]);
	gbitmap_destroy(level1_menu_icon_image[DISMISSABLE_ITEM_GOOGLEHANGOUTS]);
	gbitmap_destroy(level1_menu_icon_image[DISMISSABLE_ITEM_SKYPE]);

	simple_menu_layer_destroy(level1_menu_layer);
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
}

int main(void)
{
	app_comm_set_sniff_interval(SNIFF_INTERVAL_REDUCED);
	app_message_register_inbox_received(received_data);
	app_message_open(124, 50);

	level1_window = window_create();

	window_set_window_handlers(level1_window, 
			(WindowHandlers) 
			{
				.load = level1_window_load,
				.unload = level1_window_unload,
			});

	window_stack_push(level1_window, true);

	app_event_loop();
	app_comm_set_sniff_interval(SNIFF_INTERVAL_NORMAL);

	window_destroy(level1_window);
}
