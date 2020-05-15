package edu.ufl.registry.server.service.hl7message.HL7Transmission;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.hoh.api.IReceivable;
import ca.uhn.hl7v2.hoh.api.ISendable;
import ca.uhn.hl7v2.hoh.hapi.api.MessageSendable;
import ca.uhn.hl7v2.hoh.hapi.client.HohClientSimple;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.message.ORU_R01;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import edu.stanford.registry.server.ServerUtils;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.Properties;

/**
 * This class is used to send to the webservice
 *
 * User: jrpence
 * Date: 9/9/14
 * Time: 1:32 PM
 */

public class HL7Sender {

    Properties configuration;
    Logger     logger;

    /**
     * Constuctor
     *
     * Sets up the logger class and logs that the class now exists.
     *
     * @author jrpence
     */
    public HL7Sender() {
        logger = Logger.getLogger( this.getClass() );
        logger.debug( "Created" );
    }

    /**
     * translate
     *
     * This is the main function, called from another class.
     *
     * @param message String the string version of the message
     *
     * @return String a string version of the response message.
     *
     * @author jrpence
     */

    public String translate( String message ) {
        if ( message == null ) {

            logger.error( "The message is null" );

            return "Fail: Message is null";
        }

        // since \n can cause an encode issue.
        message = message.replaceAll( "\\n", "\r" );

        logger.debug( "\n" + message );
        Boolean result = isHL7Message( message );
        if ( result == Boolean.TRUE ) {
            String response = this.sendMessage( message );
            logger.debug( response );
            return response;
        } else {
            return "Fail: Message is not HL7";
        }

    }

    /**
     * isHL&Message
     *
     * @param message String
     *
     * @return Boolean true if the message parses.
     *
     * @author jrpence
     */
    private Boolean isHL7Message( String message ) {
        Boolean output = Boolean.TRUE;

        try {
            // try to parse the message if it can be parsed then it is HL7
            Parser parser = new PipeParser();
            Message msg = parser.parse( message );
            logger.debug( msg.printStructure() );
        }
        catch ( EncodingNotSupportedException e ) {
            output = Boolean.FALSE;
            logger.error( e.getMessage() );
            e.printStackTrace();
        }
        catch ( HL7Exception e ) {
            output = Boolean.FALSE;
            logger.error( e.getMessage() );
            e.printStackTrace();
        }
        catch ( Exception e ) {
            output = Boolean.FALSE;
            logger.error( e.getMessage() );
            e.printStackTrace();
        }

        return output;
    }

    /**
     * send message
     *
     * @param hL7Message String a string version of the HL7 message
     *
     * @return String the String response.
     *
     * @author jrpence
     */
    private String sendMessage( String hL7Message ) {

        String stringResponse = "Error:";

        ServerUtils serverUtils = ServerUtils.getInstance();

        logger.debug( "config host = " + String.valueOf( serverUtils.getParam( "HL7Message.destinationHost" ) ) );
        logger.debug( "config port = " + String.valueOf( serverUtils.getParam( "HL7Message.destinationPort" ) ) );
        logger.debug( "config uri = " + String.valueOf( serverUtils.getParam( "HL7Message.destinationUri" ) ) );
        logger.debug( "config tls = " + String.valueOf( serverUtils.getParam( "HL7Message.destinationUseTLS" ) ) );

        String host = serverUtils.getParam( "HL7Message.destinationHost" );
        String hostUri = serverUtils.getParam( "HL7Message.destinationUri" );
        int port = Integer.parseInt( serverUtils.getParam( "HL7Message.destinationPort" ) );
        boolean tls = Boolean.parseBoolean( serverUtils.getParam( "HL7Message.destinationUseTLS" ) );

        HapiContext ctx = new DefaultHapiContext();

        CanonicalModelClassFactory mcf = new CanonicalModelClassFactory( "2.5" );

        ctx.setModelClassFactory( mcf );

        MinLowerLayerProtocol llp = new MinLowerLayerProtocol();
        llp.setCharset( Charset.forName( "UTF-8" ) );

        ctx.setLowerLayerProtocol( llp );

        Parser messageParser = null;
        Message message = null;
        try {
            ORU_R01 msg = new ORU_R01();
            msg.parse( hL7Message );
            message = msg.getMessage();

            // getting a parser
            messageParser = msg.getParser();
        }
        catch ( HL7Exception e ) {
            e.printStackTrace();
            stringResponse = stringResponse + "Failed to parse the message";
        }

        HohClientSimple clientSimple = new HohClientSimple( host, port, hostUri, messageParser );
        if ( clientSimple.isAutoClose() ) {
            logger.debug( "isAutoClose" );
        }
        if ( clientSimple.isConnected() ) {
            logger.debug( "Connected" );
        }

        try {
            ISendable< Message > sendable = new MessageSendable( message, messageParser );

            logger.debug( "Sending..." );
            clientSimple.setParser( messageParser );
            IReceivable< Message > receivable = clientSimple.sendAndReceiveMessage( sendable );

            logger.debug( "Finished Sending..." );
            Message response = receivable.getMessage();
            stringResponse = response.encode();
            logger.debug( "String response = " + stringResponse );

        }
        catch ( HL7Exception e ) {
            e.printStackTrace();
            stringResponse = stringResponse + "Failed to connect to destination";
        }
        catch ( Exception e ) {
            e.printStackTrace();
            stringResponse = stringResponse + "Failed to connect to destination";
        }

        System.out.println( "Message sent, with response = " + stringResponse );

        return stringResponse;
    }
}
