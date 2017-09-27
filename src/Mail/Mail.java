package Mail;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.internet.MimeMessage.RecipientType;

import Config.Configuration;

public class Mail {

	public Mail() {
		
	}
	
	public void sendMail(String subject, String message) {
		// Construct the message
		String[] to =  { Configuration.toMail };
		String from = Configuration.fromMail;
		String p = Configuration.mail;
		
		java.util.Properties props = new java.util.Properties();
		String host = "smtp.gmail.com";
		props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", p);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        
        Session session = Session.getDefaultInstance(props);
        MimeMessage mailMessage = new MimeMessage(session);
        
        try {
            mailMessage.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                mailMessage.addRecipient(RecipientType.TO, toAddress[i]);
            }

            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, p);
            transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
	}
	
}
