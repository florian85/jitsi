/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.math.*;
import java.security.*;
import java.util.*;

import net.java.sip.communicator.service.protocol.jabber.*;
import org.jivesoftware.smack.packet.*;

/**
 * A straightforward extension of the IQ. A <tt>JingleIQ</tt> object is created
 * by smack via the {@link JingleIQProvider}. It contains all the information
 * extracted from a <tt>jingle</tt> IQ.
 *
 * @author Emil Ivov
 */
public class JingleIQ extends IQ
{

    /**
     * The name space that jingle belongs to.
     */
    public static final String NAMESPACE = "urn:xmpp:jingle:1";

    /**
     * The name of the element that contains the jingle data.
     */
    public static final String ELEMENT_NAME = "jingle";

    /**
     * The name of the argument that contains the jingle action value.
     */
    public static final String ACTION_ATTR_NAME = "action";

    /**
     * The name of the argument that contains the "initiator" jid.
     */
    public static final String INITIATOR_ATTR_NAME = "initiator";

    /**
     * The name of the argument that contains the "responder" jid.
     */
    public static final String RESPONDER_ATTR_NAME = "responder";

    /**
     * The name of the argument that contains the session id.
     */
    public static final String SID_ATTR_NAME = "sid";
    
    
    /**
     * The <tt>JingleAction</tt> that describes the purpose of this
     * <tt>jingle</tt> element.
     */
    private JingleAction action;

    /**
     * The full JID of the entity that has initiated the session flow. Only
     * present when the <tt>JingleAction</tt> is <tt>session-accept</tt>.
     */
    private String initiator;

    /**
     * The full JID of the entity that replies to a Jingle initiation. The
     * <tt>responder</tt> can be different from the 'to' address on the IQ-set.
     * Only present when the <tt>JingleAction</tt> is <tt>session-accept</tt>.
     */
    private String responder;

    /**
     * The ID of the Jingle session that this IQ belongs to. XEP-0167: A sid is
     * a random session identifier generated by the initiator, which
     * effectively maps to the local-part of a SIP "Call-ID" parameter
     */
    private String sid;

    /**
     * The <tt>reason</tt> extension in a <tt>jingle</tt> IQ providers machine
     * and possibly human-readable information about the reason for the action.
     */
    private ReasonPacketExtension reason;

    /**
     * Any session info extensions that this packet may contain.
     */
    private SessionInfoPacketExtension sessionInfo;

    /**
     * The list of "content" elements included in this IQ.
     */
    private final List<ContentPacketExtension> contentList
            = new ArrayList<ContentPacketExtension>();
    
    /**
     * Returns the XML string of this Jingle IQ's "section" sub-element.
     *
     * Extensions of this class must override this method.
     *
     * @return the child element section of the IQ XML.
     */
    @Override
    public String getChildElementXML()
    {
        StringBuilder bldr = new StringBuilder("<" + ELEMENT_NAME);

        bldr.append(" xmlns='" + NAMESPACE + "'");

        bldr.append(" " + ACTION_ATTR_NAME + "='" + getAction() + "'");

        if( initiator != null)
            bldr.append(" " + INITIATOR_ATTR_NAME
                    + "='" + getInitiator() + "'");

        if( responder != null)
            bldr.append(" " + RESPONDER_ATTR_NAME
                    + "='" + getResponder() + "'");

        bldr.append(" " + SID_ATTR_NAME
                + "='" + getSID() + "'");

        CharSequence extensionsXMLSeq = getExtensionsXML();
        String extensionsXML = extensionsXMLSeq.toString();
        
        if ((contentList.size() == 0)
                && (reason == null)
                && (sessionInfo == null)
                && ((extensionsXML == null) || (extensionsXML.length() == 0)))
        {
            bldr.append("/>");
        }
        else
        {
            bldr.append(">");//it is possible to have empty jingle elements

            //content
            for(ContentPacketExtension cpe : contentList)
            {
                bldr.append(cpe.toXML());
            }

            //reason
            if (reason != null)
                bldr.append(reason.toXML());

            //session-info
            //XXX: this is RTP specific so we should probably handle it in a
            //subclass
            if (sessionInfo != null)
                bldr.append(sessionInfo.toXML());

            // extensions
            if ((extensionsXML != null) && (extensionsXML.length() != 0))
                bldr.append(extensionsXML);

            bldr.append("</" + ELEMENT_NAME + ">");
        }

        return bldr.toString();
    }

    /**
     * Sets this element's session ID value. A "sid" is a random session
     * identifier generated by the initiator, which effectively maps to the
     * local-part of a SIP "Call-ID" parameter.
     *
     * @param sid the session ID to set
     */
    public void setSID(String sid)
    {
        this.sid = sid;
    }

    /**
     * Returns this element's session ID value. A "sid" is a random session
     * identifier generated by the initiator, which effectively maps to the
     * local-part of a SIP "Call-ID" parameter.
     *
     *
     * @return this element's session ID.
     */
    public String getSID()
    {
        return sid;
    }

    /**
     * Generates a random <tt>String</tt> usable as a jingle session ID.
     *
     * @return a newly generated random sid <tt>String</tt>
     */
    public static String generateSID()
    {
        return new BigInteger(64, new SecureRandom()).toString(32);
    }

    /**
     * Sets the full JID of the entity that replies to a Jingle initiation. The
     * <tt>responder</tt> can be different from the 'to' address on the IQ-set.
     * Only present when the <tt>JingleAction</tt> is <tt>session-accept</tt>.
     *
     * @param responder the full JID of the session <tt>responder</tt>.
     */
    public void setResponder(String responder)
    {
        this.responder = responder;
    }

    /**
     * Returns the full JID of the entity that replies to a Jingle initiation.
     * The <tt>responder</tt> can be different from the 'to' address on the
     * IQ-set. Only present when the <tt>JingleAction</tt> is
     * <tt>session-accept</tt>.
     *
     * @return the full JID of the session <tt>responder</tt>
     */
    public String getResponder()
    {
        return responder;
    }

    /**
     * Sets the full JID of the entity that has initiated the session flow. Only
     * present when the <tt>JingleAction</tt> is <tt>session-accept</tt>.
     *
     * @param initiator the full JID of the initiator.
     */
    public void setInitiator(String initiator)
    {
        this.initiator = initiator;
    }

    /**
     * Returns the full JID of the entity that has initiated the session flow.
     * Only present when the <tt>JingleAction</tt> is <tt>session-accept</tt>.
     *
     * @return the full JID of the initiator.
     */
    public String getInitiator()
    {
        return initiator;
    }

    /**
     * Sets the value of this element's <tt>action</tt> attribute. The value of
     * the 'action' attribute MUST be one of the values enumerated here. If an
     * entity receives a value not defined here, it MUST ignore the attribute
     * and MUST return a <tt>bad-request</tt> error to the sender. There is no
     * default value for the 'action' attribute.
     *
     * @param action the value of the <tt>action</tt> attribute.
     */
    public void setAction(JingleAction action)
    {
        this.action = action;
    }

    /**
     * Returns the value of this element's <tt>action</tt> attribute. The value
     * of the 'action' attribute MUST be one of the values enumerated here. If
     * an entity receives a value not defined here, it MUST ignore the attribute
     * and MUST return a <tt>bad-request</tt> error to the sender. There is no
     * default value for the 'action' attribute.
     *
     * @return the value of the <tt>action</tt> attribute.
     */
    public JingleAction getAction()
    {
        return action;
    }

    /**
     * Specifies this IQ's <tt>reason</tt> extension. The <tt>reason</tt>
     * extension in a <tt>jingle</tt> IQ provides machine and possibly human
     * -readable information about the reason for the action.
     *
     * @param reason this IQ's <tt>reason</tt> extension.
     */
    public void setReason(ReasonPacketExtension reason)
    {
        this.reason = reason;
    }

    /**
     * Returns this IQ's <tt>reason</tt> extension. The <tt>reason</tt>
     * extension in a <tt>jingle</tt> IQ provides machine and possibly human
     * -readable information about the reason for the action.
     *
     * @return this IQ's <tt>reason</tt> extension.
     */
    public ReasonPacketExtension getReason()
    {
        return reason;
    }

    /**
     * Returns a reference (and not a copy so be careful how you are handling
     * it) of this element's content list.
     *
     * @return a reference to this element's content list.
     */
    public List<ContentPacketExtension> getContentList()
    {
        synchronized(contentList)
        {
            return new ArrayList<ContentPacketExtension>(contentList);
        }
    }

    /**
     * Adds <tt>contentPacket</tt> to this IQ's content list.
     *
     * @param contentPacket the content packet extension we'd like to add to
     * this element's content list.
     */
    public void addContent(ContentPacketExtension contentPacket)
    {
        synchronized(contentList)
        {
            this.contentList.add(contentPacket);
        }
    }

    /**
     * Determines if this packet contains a <tt>content</tt> with a child
     * matching the specified <tt>contentType</tt>. The method is meant to allow
     * to easily determine the purpose of a jingle IQ. A telephony initiation
     * IQ would for example contain a <tt>content</tt> element of type {@link
     * RtpDescriptionPacketExtension}.
     *
     * @param contentType the type of the content child we are looking for.
     *
     * @return <tt>true</tt> if one of this IQ's <tt>content</tt> elements
     * contains a child of the specified <tt>contentType</tt> and <tt>false</tt>
     * otherwise.
     */
    public boolean containsContentChildOfType(
            Class<? extends PacketExtension> contentType)
    {
        if(getContentForType(contentType) != null)
            return true;

        return false;
    }

    /**
     * Determines if this packet contains a <tt>content</tt> with a child
     * matching the specified <tt>contentType</tt> and returns it. Returns
     * <tt>null</tt> otherwise. The method is meant to allow to easily extract
     * specific IQ elements like an RTP description for example.
     *
     * @param contentType the type of the content child we are looking for.
     *
     * @return a reference to the content element that has a child of the
     * specified <tt>contentType</tt> or <tt>null</tt> if no such child was
     * found.
     */
    public ContentPacketExtension getContentForType(
            Class<? extends PacketExtension> contentType)
    {
        synchronized(contentList)
        {
            for(ContentPacketExtension content : contentList)
            {
                PacketExtension child
                        = content.getFirstChildOfType(contentType);
                if(child != null)
                    return content;
            }
        }

        return null;
    }

    /**
     * Finds <tt>ContentPacketExtension</tt> that matches given
     * <tt>contentName</tt>.
     * @param contentName the name of the content for which extension will be
     *                    returned
     * @return <tt>ContentPacketExtension</tt> that matches given
     *         <tt>contentName</tt> or <tt>null</tt> if not found.
     */
    public ContentPacketExtension getContentByName(String contentName)
    {
        synchronized(contentList)
        {
            for(ContentPacketExtension content : contentList)
            {
                if (contentName.equals(content.getName()))
                {
                    return content;
                }
            }
        }
        return null;
    }

    /**
     * Sets <tt>si</tt> as the session info extension for this packet.
     *
     * @param si a {@link SessionInfoPacketExtension} that we'd like to add
     * here.
     */
    public void setSessionInfo(SessionInfoPacketExtension si)
    {
        this.sessionInfo = si;
    }

    /**
     * Returns a {@link SessionInfoPacketExtension} if this <tt>JingleIQ</tt>
     * contains one and <tt>null</tt> otherwise.
     *
     * @return a {@link SessionInfoPacketExtension} if this <tt>JingleIQ</tt>
     * contains one and <tt>null</tt> otherwise.
     */
    public SessionInfoPacketExtension getSessionInfo()
    {
        return this.sessionInfo;
    }
}
