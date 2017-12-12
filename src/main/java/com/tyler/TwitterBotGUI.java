package com.tyler;

import twitter4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

// This class handles my Twitter Bot Form
class TwitterBotGUI extends JFrame {
    private JTextField txtFieldStatusUpdate;
    private JButton btnUpdateStatus;
    private JPanel mainPanel;
    private JButton btnDisplayHomeTimeline;
    private JButton btnExit;
    private JComboBox<String> cboFollowers;

    private JList<String> listStatuses;
    private JLabel infoLabel;
    private JButton btnDatabase;
    private DefaultListModel<String> listModel;

    private StatusesDB database;
    private TwitterObj twitterMaster;


    TwitterBotGUI() {
        //Set up my GUI by calling these next two methods
        configureComponents();
        addListeners();

    }


    private void configureComponents() {
        setTitle("Twitter Bot");
        setContentPane(mainPanel);
        setPreferredSize(new Dimension(750, 750));
        listModel = new DefaultListModel<>();
        listStatuses.setModel(listModel);
        listStatuses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        database = new StatusesDB();
        twitterMaster = new TwitterObj();
        pack();
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        comboBoxSetUp();

    }
    // Populate the comboBox with a list of people I follow on Twitter
    private void comboBoxSetUp() {
        Twitter t = twitterMaster.configureTwitter();
        List<User> friends = getFriends(t);
        if (friends != null) {
            for (User friend : friends) {
                String name = friend.getScreenName();
                cboFollowers.addItem(name);
                cboFollowers.setSelectedIndex(-1);
            }
        }
    }

    // LISTENERS
    private void addListeners() {
        // Creates a new status on Twitter
        btnUpdateStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = txtFieldStatusUpdate.getText();
                Twitter t = twitterMaster.configureTwitter();
                if (message.length() == 0) {
                    JOptionPane.showMessageDialog(TwitterBotGUI.this, "Error: Enter a status");
                    return;
                }
                postingToTwitter(t, message);
                txtFieldStatusUpdate.setText("");
                infoLabel.setText("Status Successfully Updated");
            }
        });
        // Retrieves and displays my Timeline
        btnDisplayHomeTimeline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.removeAllElements();
                cboFollowers.setSelectedIndex(-1);
                Twitter t = twitterMaster.configureTwitter();
                List<Status> myStatuses = myTimeline(t);
                for (Status s : myStatuses) {
                    listModel.addElement(s.getUser().getName() + " --- " + s.getText());
                }
                cboFollowers.setSelectedIndex(-1);
                infoLabel.setText("Showing Home Timeline");
                }
        });
        // Retrieves and displays statuses created by my Twitter Bot
        btnDatabase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.removeAllElements();
                cboFollowers.setSelectedIndex(-1);
                infoLabel.setText("Status's created by Twitter Bot");
                ArrayList<StatusObj> dbStatuses = database.getStatuses();
                for (StatusObj s : dbStatuses) {
                    listModel.addElement(s.getText() + "  Date Created: " + s.getCreatedAt());
                }
            }
        });
        // Displays the timeline of the selected follower
        cboFollowers.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                listModel.removeAllElements();
                String friend = (String) cboFollowers.getSelectedItem();
                Twitter t = twitterMaster.configureTwitter();
                List<Status> statuses = friendTimeLine(t, friend);
                if (statuses != null) {
                    for (Status s : statuses) {
                        listModel.addElement(s.getUser().getName() + " --- " + s.getText());
                    }
                }
                try {
                    String[] search = new String[]{friend};
                    ResponseList<User> users = t.lookupUsers(search);
                    for (User user : users) {
                        infoLabel.setText(user.getName() + " Timeline");
                    }
                } catch (TwitterException twe) { twe.getMessage(); }
            }
        });
        // Exits Program
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });
    }

    // Method that posts a status to Twitter
    private void postingToTwitter(Twitter t, String message) {
        try {
            Status status = t.updateStatus(message);
            String created = String.valueOf(status.getCreatedAt());
            database.addStatus(message, created);
        } catch (TwitterException twe) { twe.getMessage(); }
    }
    // Method that returns a List of statues from my Timeline
    private static List<Status> myTimeline(Twitter t) {
        try {
            ResponseList<Status> statuses = t.getUserTimeline("FFantasyBot");
            return statuses;
        } catch (TwitterException twe) { twe.getMessage(); }
        return null;
    }
    // Method that returns a list of statuses from a Friends Timeline
    private static List<Status> friendTimeLine(Twitter t, String friend) {
        try {
            String[] search = new String[] {friend};
            ResponseList<User> users = t.lookupUsers(search);
            for (User user : users) {
                if (user.getStatus() != null) {
                    List<Status> statuses = t.getUserTimeline(user.getScreenName());
                    return statuses;
                }
            }
        } catch (TwitterException twe) { twe.getMessage(); }
        return null;
    }
    // Method that returns a list of who I follow on Twitter
    private static List<User> getFriends(Twitter t) {
        try {
            String twitterScreenName = t.getScreenName();
            return t.getFriendsList(twitterScreenName, -1);
        } catch (TwitterException twe) { twe.getMessage(); }
        return null;
    }


}
