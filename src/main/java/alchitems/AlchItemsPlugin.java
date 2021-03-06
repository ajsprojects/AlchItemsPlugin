package alchitems;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@PluginDescriptor(
        name = "Alch Items",
        description = "Shows you good items to alch",
        tags = {"alching", "high alch", "alch"}
)
@Slf4j
public class AlchItemsPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    ItemService itemService;

    private AlchItemsPanel panel;
    private NavigationButton navButton;
    ExecutorService executorService;

    @Override
    protected void startUp() throws Exception {
        final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "icon.png");
        this.panel = injector.getInstance(AlchItemsPanel.class);

        panel.init();
        navButton = NavigationButton.builder()
                .tooltip("Alch Items")
                .icon(icon)
                .panel(panel)
                .priority(5)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() throws Exception {
        executorService.shutdownNow();
        executorService = null;
        clientToolbar.removeNavigation(navButton);
    }

    public void fetchItemsHandlerASync() {
        log.info("Setting panel to loading items");
        panel.getItemsContainerPanel().remove(panel.getItemsPanel());
        panel.setItemsPanel(panel.buildFetchingItems());
        panel.getItemsContainerPanel().add(panel.getItemsPanel());
        panel.updateUI();

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                getItems();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executorService.shutdown();
    }

    private void getItems() {
        log.info("Fetching items...");
        panel.setAlchItemsList(itemService.getItemList());
        log.info("Finished fetching items");
        SwingUtilities.invokeLater(() -> {
            log.info("Refreshing displayed items");
            panel.refreshItemsPanelDisplay();
        });
    }
}
