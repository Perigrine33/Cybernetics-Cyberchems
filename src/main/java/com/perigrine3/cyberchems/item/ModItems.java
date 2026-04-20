package com.perigrine3.cyberchems.item;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.cyberchems.Cyberchems;
import com.perigrine3.cyberchems.item.cyberdrugs.*;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cyberchems.MODID);




    public static final DeferredItem<Item> ROID_AUTOINJECTOR = ITEMS.register("roid_autoinjector",
            () -> new RoidAutoinjector(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> STIM_AUTOINJECTOR = ITEMS.register("stim_autoinjector",
            () -> new StimAutoinjector(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> BLACKLACE_AUTOINJECTOR = ITEMS.register("blacklace_autoinjector",
            () -> new BlackLaceAutoinjector(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> IMMUNOBOOST_AUTOINJECTOR = ITEMS.register("immunoboost_autoinjector",
            () -> new ImmunoboostAutoinjector(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> WARP_AUTOINJECTOR = ITEMS.register("warp_autoinjector",
            () -> new WarpAutoinjector(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ADDICTOL_AUTOINJECTOR = ITEMS.register("addictol_autoinjector",
            () -> new AddictolAutoinjector(new Item.Properties().stacksTo(16)));




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
