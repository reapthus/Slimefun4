package io.github.thebusybiscuit.slimefun4.tests.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.researching.Research;
import io.github.thebusybiscuit.slimefun4.mocks.TestUtilities;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;

public class TestResearchCommand {

    private static ServerMock server;

    private static Research research;
    private static Research research2;

    @BeforeAll
    public static void load() {
        server = MockBukkit.mock();

        SlimefunPlugin plugin = MockBukkit.load(SlimefunPlugin.class);
        research = new Research(new NamespacedKey(plugin, "command_test"), 999, "Test", 10);
        research.register();

        research2 = new Research(new NamespacedKey(plugin, "command_test_two"), 1000, "Test Two", 10);
        research2.register();
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @Test
    public void testResearchAll() throws InterruptedException {
        SlimefunPlugin.getRegistry().setResearchingEnabled(true);
        Player player = server.addPlayer();
        PlayerProfile profile = TestUtilities.awaitProfile(player);

        server.executeConsole("slimefun", "research", player.getName(), "all").assertSucceeded();

        Assertions.assertTrue(profile.hasUnlocked(research));
        Assertions.assertTrue(profile.hasUnlocked(research2));
    }

    @Test
    public void testResearchSpecific() throws InterruptedException {
        SlimefunPlugin.getRegistry().setResearchingEnabled(true);
        Player player = server.addPlayer();
        PlayerProfile profile = TestUtilities.awaitProfile(player);

        server.executeConsole("slimefun", "research", player.getName(), research.getKey().toString()).assertSucceeded();

        Assertions.assertTrue(profile.hasUnlocked(research));
        Assertions.assertFalse(profile.hasUnlocked(research2));
    }

    @Test
    public void testResearchReset() throws InterruptedException {
        SlimefunPlugin.getRegistry().setResearchingEnabled(true);
        Player player = server.addPlayer();
        PlayerProfile profile = TestUtilities.awaitProfile(player);

        server.executeConsole("slimefun", "research", player.getName(), "all").assertSucceeded();

        Assertions.assertTrue(profile.hasUnlocked(research));
        Assertions.assertTrue(profile.hasUnlocked(research2));

        server.executeConsole("slimefun", "research", player.getName(), "reset").assertSucceeded();

        Assertions.assertFalse(profile.hasUnlocked(research));
        Assertions.assertFalse(profile.hasUnlocked(research2));
    }
}
