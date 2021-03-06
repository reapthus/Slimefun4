package io.github.thebusybiscuit.slimefun4.tests.items;

import java.util.Optional;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.mocks.TestUtilities;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;

public class TestItemSettings {

    private static SlimefunPlugin plugin;

    @BeforeAll
    public static void load() {
        MockBukkit.mock();
        plugin = MockBukkit.load(SlimefunPlugin.class);
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @Test
    public void testIllegalItemSettings() {
        SlimefunItem item = TestUtilities.mockSlimefunItem(plugin, "ITEM_SETTINGS_TEST", new CustomItem(Material.DIAMOND, "&cTest"));
        item.register(plugin);

        Assertions.assertThrows(IllegalArgumentException.class, () -> new ItemSetting<>("prematureInvocation", "Hello world").getValue());
        Assertions.assertThrows(IllegalArgumentException.class, () -> item.addItemSetting());
        Assertions.assertThrows(IllegalArgumentException.class, () -> item.addItemSetting((ItemSetting<String>) null));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> item.addItemSetting(new ItemSetting<>("test", "Hello World")));
    }

    @Test
    public void testAddItemSetting() {
        SlimefunItem item = TestUtilities.mockSlimefunItem(plugin, "ITEM_SETTINGS_TEST_2", new CustomItem(Material.DIAMOND, "&cTest"));
        ItemSetting<String> setting = new ItemSetting<>("test", "Hello World");

        Assertions.assertTrue(setting.isType(String.class));
        Assertions.assertFalse(setting.isType(Integer.class));

        item.addItemSetting(setting);
        item.register(plugin);

        Assertions.assertTrue(item.getItemSettings().contains(setting));

        Optional<ItemSetting<String>> optional = item.getItemSetting(setting.getKey(), String.class);
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(setting, optional.get());
        Assertions.assertEquals("Hello World", setting.getValue());

        Assertions.assertFalse(item.getItemSetting(setting.getKey(), Boolean.class).isPresent());
        Assertions.assertFalse(item.getItemSetting("I do not exist", String.class).isPresent());
    }

    @Test
    public void testUpdateItemSetting() {
        SlimefunItem item = TestUtilities.mockSlimefunItem(plugin, "ITEM_SETTINGS_TEST_3", new CustomItem(Material.DIAMOND, "&cTest"));
        ItemSetting<String> setting = new ItemSetting<>("test", "Hello World");

        item.addItemSetting(setting);
        item.register(plugin);

        Assertions.assertEquals("Hello World", setting.getValue());
        setting.update("I am different");
        Assertions.assertEquals("I am different", setting.getValue());

        Assertions.assertThrows(IllegalArgumentException.class, () -> setting.update(null));
        Assertions.assertEquals("I am different", setting.getValue());
    }

    @Test
    public void testAlreadyExistingItemSetting() {
        SlimefunItem item = TestUtilities.mockSlimefunItem(plugin, "ITEM_SETTINGS_TEST", new CustomItem(Material.DIAMOND, "&cTest"));

        item.addItemSetting(new ItemSetting<>("test", "Hello World"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> item.addItemSetting(new ItemSetting<>("test", "Hello World")));
    }
}
