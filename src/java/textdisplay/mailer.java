
package textdisplay;

import java.io.UnsupportedEncodingException;
import static java.lang.System.getProperties;
import static java.net.IDN.toASCII;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.mail.BodyPart;
import javax.mail.Message;
import static javax.mail.Message.RecipientType.TO;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import static javax.mail.Session.getDefaultInstance;
import static javax.mail.Transport.send;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import static javax.mail.internet.InternetAddress.parse;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import static textdisplay.Folio.getRbTok;

/**Sends mail when an error occurs
 */
public class mailer {
    
    /**
     * The default mail server used for sending e-mails.  This value is,
     * by default, read from the file {@see version.properties}.
     * 
     * @var String
     */
    String mailServer = "";
    
    /**
     * The default "from" address used when sending e-mails.  This value is,
     * by default, read from the file {@see version.properties}.
     * 
     * @var String
     */
    String mailFrom = "";
    
    
    public mailer()
    {
        this.mailServer = getRbTok("EMAILSERVER");
        this.mailFrom = getRbTok("NOTIFICATIONEMAIL");
    }
    
    
    /**
     * Returns the current date using an arbitrary, hard-coded format.
     * The format is close to ISO 8601, but it uses slashes, not hyphens
     * ("YYYY/MM/DD HH:mm:ss").
     * 
     * @return the current date
     */
    public String getDate()
    {
        // @TODO:  Move this format to config file.
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    
    /**
     * Sends an e-mail.  For use, e.g., when an error has occurred.
     * 
     * @param to
     * @param subject
     * @param messageBody
     * 
     * @throws MessagingException
     * @throws AddressException 
     */
    public void sendMail(String to, String subject, String messageBody
    ) throws MessagingException, AddressException
    {
        this.sendMail(this.mailServer, this.mailFrom, to, subject, messageBody);
     }
    
    
    /**
     * Sends an e-mail.  For use, e.g., when an error has occurred.
     * 
     * @param mailServer
     * @param from
     * @param to
     * @param subject
     * @param messageBody
     * 
     * @throws MessagingException
     * @throws AddressException 
     */
    public void sendMail(String mailServer, String from, String to,
                         String subject, String messageBody
    ) throws MessagingException, AddressException
    {
        
        // Setup mail server
        if (mailServer == null) {
            // BOZO:  This will never be used, right?  (Since Java prevents nulls here.)
            mailServer = this.mailServer;
        }
        Properties props = getProperties();
        props.put("mail.smtp.host", mailServer);
        
        // Get a mail session
        Session session = getDefaultInstance(props, null);
        
        // Define a new mail message
        Message message = new MimeMessage(session);
        
        // Set message sender
        if (from == null) {
            // BOZO:  This will never be used, right?  (Since Java prevents nulls here.)
            from = this.mailFrom;
        }
        message.setFrom(new InternetAddress(from));
        
        //message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.addRecipients(TO, unicodifyAddresses(to));
        message.setSubject(subject);
        
        // Create a message part to represent the body text
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(messageBody);
        
        //use a MimeMultipart as we need to handle the file attachments
        Multipart multipart = new MimeMultipart();
        
        //add the message body to the mime message
        multipart.addBodyPart(messageBodyPart);
        
        // Put all message parts in the message
        message.setContent(multipart);
        
        // Send the message
        send(message);
     }
    
    
    /**
     * (TODO:  Complete.)
     * 
     * @param addresses
     * @return
     * @throws AddressException 
     */
    InternetAddress[] unicodifyAddresses(String addresses) throws AddressException {
        InternetAddress[] recips = parse(toASCII(addresses), false);
        for(int i=0; i<recips.length; i++) {
            try {
                recips[i] = new InternetAddress(recips[i].getAddress(), recips[i].getPersonal(), "utf-8");
            } catch (UnsupportedEncodingException ex) {
                getLogger(mailer.class.getName()).log(SEVERE, null, ex);
            }
        }
        return recips;
    }
    
}
