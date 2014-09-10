package eu.cyfronoid.audio.player;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.google.common.eventbus.EventBus;

import eu.cyfronoid.audio.player.event.Events;
import eu.cyfronoid.audio.player.resources.Resources.PropertyKey;
import eu.cyfronoid.gui.action.CommonActionListener;

public class PlayerMenu extends JMenuBar {
    private static final long serialVersionUID = -9075625627736815359L;
    private final EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);

    public PlayerMenu() {
        createFileMenu();

        createViewMenu();

        createHelpMenu();
    }

    private void createViewMenu() {
        JMenu viewMenu = new JMenu(PlayerConfigurator.getLabelFor(PropertyKey.VIEW_MENU));
        add(viewMenu);

        JMenuItem toggleSpectrumAnalyzerMenuItem = new JMenuItem(PlayerConfigurator.getLabelFor(PropertyKey.TOGGLE_SPECTRUM));
        toggleSpectrumAnalyzerMenuItem.addActionListener(CommonActionListener.EVENT_BUS_POST.get(eventBus, Events.toggleAnalyzerPanel));
        viewMenu.add(toggleSpectrumAnalyzerMenuItem);
    }

    private void createHelpMenu() {
        JMenu helpMenu = new JMenu(PlayerConfigurator.getLabelFor(PropertyKey.HELP_MENU));
        add(helpMenu);

        JMenuItem aboutMenuItem = new JMenuItem(PlayerConfigurator.getLabelFor(PropertyKey.ABOUT));
        aboutMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                PlayerConfigurator.ABOUT_DIALOG.open();
            }
        });
        helpMenu.add(aboutMenuItem);
    }

    private void createFileMenu() {
        JMenu fileMenu = new JMenu(PlayerConfigurator.getLabelFor(PropertyKey.FILE_MENU));
        add(fileMenu);

        JMenuItem newMenuItem = new JMenuItem(PlayerConfigurator.getLabelFor(PropertyKey.NEW));
        newMenuItem.addActionListener(CommonActionListener.EVENT_BUS_POST.get(eventBus, Events.newPlaylist));
        fileMenu.add(newMenuItem);

        JMenuItem openMenuItem = new JMenuItem(PlayerConfigurator.getLabelFor(PropertyKey.OPEN));
        openMenuItem.addActionListener(CommonActionListener.EVENT_BUS_POST.get(eventBus, Events.playlistOpenDialog));
        fileMenu.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem(PlayerConfigurator.getLabelFor(PropertyKey.SAVE));
        saveMenuItem.addActionListener(CommonActionListener.EVENT_BUS_POST.get(eventBus, Events.playlistSave));
        fileMenu.add(saveMenuItem);

        JMenuItem exitMenuItem = new JMenuItem(PlayerConfigurator.getLabelFor(PropertyKey.EXIT));
        exitMenuItem.addActionListener(CommonActionListener.EVENT_BUS_POST.get(eventBus, Events.closePlayer));
        fileMenu.add(exitMenuItem);
    }
}
