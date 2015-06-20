package com.gmail.ngreco.client;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String user = "test.nico.greco@gmail.com";
        String password = "testpassword1";
        String protocol = "imap";
        String host = "imap.gmail.com";
        String port = "993";

        try {
            EmailReceiver receiver = (EmailReceiver) new EmailReceiver().execute(protocol, host, port, user, password);

        } catch (Exception e) {
            Log.e("EmailReceiver", e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private class EmailReceiver extends AsyncTask<String, Integer, String> {
        /**
         /* Returns a Properties object which is configured for a POP3/IMAP server
         /*
         /* @param protocol either "imap" or "pop3"
         /* @param host
         /* @param port
         /* @return a Properties object
         /*/

        protected String doInBackground(String... urls) {
            String value;
            value = this.downloadEmails(urls[0], urls[1], urls[2], urls[3], urls[4]);
            return value;
        }


        @Override
        protected void onPostExecute(String result) {
            TextView txt = (TextView) findViewById(R.id.textView2);
            txt.setText(result);
        }




        private Properties getServerProperties(String protocol, String host,
                                               String port) {
            Properties properties = new Properties();
            Properties props = new Properties();

            // server setting
            properties.put(String.format("mail.%s.host", protocol), host);
            //Set host address
            props.setProperty("mail.imap.host", "imap.gmail.com");

            properties.put(String.format("mail.%s.port", protocol), port);
            //Set specified port
            props.setProperty("mail.imap.port", "993");

            // SSL setting
            properties.setProperty(
                    String.format("mail.%s.socketFactory.class", protocol),
                    "javax.net.ssl.SSLSocketFactory");
            //Using SSL
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            properties.setProperty(
                    String.format("mail.%s.socketFactory.fallback", protocol),"false");
            props.setProperty("mail.imap.socketFactory.fallback", "false");


            properties.setProperty(
                    String.format("mail.%s.socketFactory.port", protocol),
                    String.valueOf(port));


            //IMAPS protocol
            props.setProperty("mail.store.protocol", "imap");

            //return properties;
            return props;
        }

        /**
         * Downloads new messages and fetches details for each message.
         * @param protocol
         * @param host
         * @param port
         * @param userName
         * @param password
         */
        public String downloadEmails(String protocol, String host, String port,
                                     String userName, String password) {

            Properties properties = getServerProperties(protocol, host, port);
            Properties props = getServerProperties(protocol, host, port);



            Session session = Session.getDefaultInstance(properties);

            try {


                //Setting IMAP session
                Session imapSession = Session.getInstance(props);

                Store store = imapSession.getStore("imap");


//Connect to server by sending username and password.
//Example mailServer = imap.gmail.com, username = abc, password = abc
                //store.connect(mailServer, account.username, account.password);
//Get all mails in Inbox Forlder
                //inbox = store.getFolder("Inbox");
                //inbox.open(Folder.READ_ONLY);
                //Return result to array of message
                //Message[] result = inbox.getMessages();


                // connects to the message store
                //Store store = session.getStore(protocol);
                store.connect("imap.gmail.com", 993, userName, password);

                // opens the inbox folder
                Folder folderInbox = store.getFolder("INBOX");
                folderInbox.open(Folder.READ_ONLY);

                // fetches new messages from server
                Message[] messages = folderInbox.getMessages();

                for (int i = 0; i < messages.length; i++) {
                    Message msg = messages[i];
                    Address[] fromAddress = msg.getFrom();
                    String from = fromAddress[0].toString();
                    String subject = msg.getSubject();
                    String toList = parseAddresses(msg
                            .getRecipients(Message.RecipientType.TO));
                    String ccList = parseAddresses(msg
                            .getRecipients(Message.RecipientType.CC));
                    String sentDate = msg.getSentDate().toString();

                    String contentType = msg.getContentType();
                    String messageContent = "";

                    if (contentType.contains("text/plain")
                            || contentType.contains("text/html")) {
                        try {
                            Object content = msg.getContent();
                            if (content != null) {
                                messageContent = content.toString();
                            }
                        } catch (Exception ex) {
                            messageContent = "[Error downloading content]";
                            ex.printStackTrace();
                        }
                    }

                    // print out details of each message
                    System.out.println("Message #" + (i + 1) + ":");
                    System.out.println("\t From: " + from);
                    System.out.println("\t To: " + toList);
                    System.out.println("\t CC: " + ccList);
                    System.out.println("\t Subject: " + subject);
                    System.out.println("\t Sent Date: " + sentDate);
                    System.out.println("\t Message: " + messageContent);

                    return "lista messaggi";
                }

                // disconnect
                folderInbox.close(false);
                store.close();

            } catch (NoSuchProviderException ex) {
                System.out.println("No provider for protocol: " + protocol);
                ex.printStackTrace();
            } catch (MessagingException ex) {
                System.out.println("Could not connect to the message store");
                ex.printStackTrace();
                return ex.getMessage();
            }
            return "default";
        }

        /**
         * Returns a list of addresses in String format separated by comma
         *
         * @param address an array of Address objects
         * @return a string represents a list of addresses
         */
        private String parseAddresses(Address[] address) {
            String listAddress = "";

            if (address != null) {
                for (int i = 0; i < address.length; i++) {
                    listAddress += address[i].toString() + ", ";
                }
            }
            if (listAddress.length() > 1) {
                listAddress = listAddress.substring(0, listAddress.length() - 2);
            }

            return listAddress;
        }
    }
}
