//Thong Truong - 0356860
//Cs 9053 - Hw 08 - Chat Client

package cs9053chatclient;

import java.net.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class chatClient
{
    public static void main(String[] args)
    {
        ChatWindow clientWindow = new ChatWindow();
        clientWindow.setup();
    }

}

class ChatWindow extends JFrame
{
    private JPanel chatPanel, setupPanel;
    private JLabel hostLabel, portLabel, userLabel;
    private JButton connectButton, disconnectButton, sendButton;
    private JTextArea output;
    private JTextField hostField, portField, userField, inputField;
    private JScrollPane display;
    private Socket server;
    private PrintStream sender;
    private Scanner receiver;
    private String host, username;
    private int port;
    private boolean isPressed;

    ChatWindow()
    {
        isPressed = false;

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);

        //construct the setup panel
        hostLabel = new JLabel("Host: ");
        portLabel = new JLabel("Port: ");
        userLabel = new JLabel("Username: ");

        hostField = new JTextField(15);
        portField = new JTextField(5);
        userField = new JTextField(12);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(new PressListener());
        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(new PressListener());

        setupPanel = new JPanel(new GridBagLayout());

        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 5;
        c.anchor = GridBagConstraints.LINE_END;
        setupPanel.add(hostLabel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        setupPanel.add(hostField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_END;
        setupPanel.add(portLabel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_START;
        setupPanel.add(portField, c);

        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_END;
        setupPanel.add(userLabel, c);

        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_START;
        setupPanel.add(userField, c);

        c.gridx = 0;
        c.gridy = 4;
        c.ipady = 0;
        c.anchor = GridBagConstraints.LINE_START;
        setupPanel.add(connectButton, c);

        c.gridx = 1;
        c.gridy = 4;
        c.anchor = GridBagConstraints.LINE_END;
        setupPanel.add(disconnectButton, c);

        //construct the display
        output = new JTextArea(17,25);
        output.setEditable(false);

        display = new JScrollPane(output);

        //construct the chat panel
        inputField = new JTextField(18);
        inputField.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if(inputField.getText().equals("/dc"))
                {
                    disconnect();
                }
                else
                {
                    send(inputField.getText());
                    inputField.setText("");
                }
            }
        });

        sendButton = new JButton("Send");
        sendButton.addActionListener(new PressListener());

        chatPanel = new JPanel(new GridBagLayout());

        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 7;
        c.insets = new Insets(4,4,4,4);
        chatPanel.add(inputField, c);

        c.gridx = 1;
        c.gridy = 0;
        c.ipady = 0;
        chatPanel.add(sendButton, c);

        //construct the entire client window
        setTitle("Client");
        setSize(300, 500);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //because I use windowClosing, not windowClosed, together with setDefaultCloseOperation,
        //I don't need to do system.exit(0)
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                disconnect();
            }
        });

        setLayout(new GridBagLayout());

        c.gridx = 0;
        c.gridy = 0;
        add(setupPanel);

        c.gridx = 0;
        c.gridy = 1;
        add(display, c);

        c.gridx = 0;
        c.gridy = 2;
        add(chatPanel, c);

        enableSetup(true);
    }

    public void setup()
    {
        while(true) //the loop for connecting and disconnecting to the server
        {
            while(!isPressed) //until the connection button is pressed, sleep
            {
                try
                {
                    Thread.sleep(1);
                }
                catch(InterruptedException e)
                {
                    updateDisplay("Error: " + e.getMessage() + "\n");
                }
            }

            connect();
            isPressed = false;
        }
    }

    private void connect()
    {
        try
        {
            if(!hostField.getText().equals("") && !portField.getText().equals("") && !userField.getText().equals(""))
            {
                //get value for host, username and port
                host = hostField.getText().trim();
                username = userField.getText().trim();
                port = Integer.parseInt(portField.getText().trim());

                //setup connection
                server = new Socket(host, port);

                updateDisplay("Connected to server successfully!\n");

                sender = new PrintStream(server.getOutputStream());
                receiver = new Scanner(server.getInputStream());

                //send username over
                send(username);

                //disable the connection and the setup panel,
                //enabling the disconnect button and the chat panel
                enableSetup(false);

                //start listening
                listen();
            }
            else
            {
                updateDisplay("Error: Please enter all information required above!\n");
            }
        }
        catch(Exception e)
        {
            updateDisplay("Error: " + e.getMessage() + "\n");
        }
    }

    private void disconnect()
    {
        try
        {
            //send the command to inform the server about disconnecting
            //the user can type the command manually to disconnect also
            send("/dc");

            server.close();
            server = null;

            //re-enable the setup panel and the connect button
            //disable the chat panel and the disconnect button
            enableSetup(true);
        }
        catch(Exception e)
        {
            updateDisplay("Error: " + e.getMessage() + "\n");
        }

    }

    private void send(String text)
    {
        sender.print(text + "\r\n");
        sender.flush();
    }

    private void listen()
    {
        while(receiver.hasNextLine())
        {
            updateDisplay(receiver.nextLine() + "\n");
        }
    }

    //display output
    public void updateDisplay(String text)
    {
        output.append(text);
    }

    private void enableSetup(boolean b)
    {
        connectButton.setEnabled(b);
        disconnectButton.setEnabled(!b);

        hostField.setEnabled(b);
        portField.setEnabled(b);
        userField.setEnabled(b);

        inputField.setEnabled(!b);
        sendButton.setEnabled(!b);
    }

    class PressListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(e.getSource() == connectButton)
            {
                isPressed = true;
            }
            else if(e.getSource() == disconnectButton)
            {
                disconnect();
            }
            else if(e.getSource() == sendButton)
            {
                if(inputField.getText().equals("/dc"))
                {
                    disconnect();
                }
                else
                {
                    send(inputField.getText());
                    inputField.setText("");
                }
            }
        }
    }
}




