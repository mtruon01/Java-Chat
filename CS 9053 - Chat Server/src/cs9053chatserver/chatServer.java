//Thong Truong - 0356860
//CS 9503 - Hw 08 - Chat Server

package cs9053chatserver;

import java.net.*;
import java.util.*;
import java.io.*;

public class chatServer {

    static ArrayList<Socket> clients;

    public static void main(String[] args)
    {
        clients = new ArrayList<Socket>();
        ServerSocket server;

        //setup the server
        try
        {
            server = new ServerSocket(5190);

            System.out.println("Start Listening");

            while(true)
            {
                Socket client = server.accept();
                System.out.println("Connection accepted");

                clients.add(client); //add the socket to the array

                new Listener(clients.get(clients.size()-1)).start(); //create a thread to listen to the socket
            }
        }
        catch(Exception e)
        {
            displayError(e.getMessage());
        }
    }

    public static void displayError(String text)
    {
        System.out.println("Error: " + text + "\n");
    }
}

class Sender extends Thread
{
    String text;

    Sender(String s)
    {
        text = s;
    }

    public synchronized void send()
    {
        System.out.println("Send to " + chatServer.clients.size() + " clients");

        for(int i=0; i<chatServer.clients.size();i++)
        {
            try
            {
                PrintWriter output = new PrintWriter(chatServer.clients.get(i).getOutputStream());
                output.print(text + "\r\n");
                output.flush();
            }
            catch(IOException e)
            {
                chatServer.displayError(e.getMessage());
            }
        }
    }

    public void run()
    {
        send();
    }
}

class Listener extends Thread
{
    Socket client;
    Scanner input;
    String username;

    Listener(Socket c)
    {
        client = c;

        try
        {
            input = new Scanner(client.getInputStream());
        }
        catch(IOException e)
        {
            chatServer.displayError(e.getMessage());
        }
    }

    public void setUsername()
    {
        username = input.nextLine();
        System.out.println("Client " + username + " is connected.");
    }

    public void startListen()
    {
        String temp;

        //start listening and create a thread to send to all clients when receives a message
        //if the message is "/dc", then know that the client is disconnected
        while(input.hasNextLine() && !(temp=input.nextLine()).equals("/dc"))
        {
            System.out.println("Receive from " + username + ": " + temp);
            new Sender(username + ": " + temp).start();
        }

        chatServer.clients.remove(client);

        try
        {
            client.close();
            client = null;
            System.out.println("Client " + username + " is disconnected");
        }
        catch(IOException e)
        {
            chatServer.displayError(e.getMessage());
        }
    }

    public void run()
    {
        //receive username, then start listening
        setUsername();
        startListen();
    }
}
   