package org.opennms.core.xml;

import java.io.ByteArrayInputStream;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class JaxbClassObjectAdapter extends XmlAdapter<Object, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(JaxbClassObjectAdapter.class);

    public JaxbClassObjectAdapter() {
        super();
        LOG.info("Initializing JaxbClassObjectAdapter.");
    }

    @Override
    public Object unmarshal(final Object from) throws Exception {
        LOG.debug("unmarshal: from = ({}){}", (from == null? null : from.getClass()), from);
        if (from == null) return null;

        if (from instanceof Element) {
            final Element e = (Element)from;
            e.normalize();
            final Class<?> clazz = JaxbUtils.getClassForElement(e.getNodeName());

            // JAXB has already turned this into an element, but we need to re-parse the XML.

            final DOMImplementationLS lsImpl = (DOMImplementationLS)e.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
            LSSerializer serializer = lsImpl.createLSSerializer();
            serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
            final String str = serializer.writeToString(e);

            return JaxbUtils.unmarshal(clazz, str);
        } else {
            LOG.error("Unsure how to determine which class to use for unmarshaling object type {}", from.getClass());
            throw new IllegalArgumentException("Unsure how to determine which class to use for unmarshaling object type " + from.getClass());
        }
    }

    @Override
    public Object marshal(final Object from) throws Exception {
        LOG.debug("marshal: from = ({}){}", (from == null? null : from.getClass()), from);
        if (from == null) return null;

        try {
            final String s = JaxbUtils.marshal(from);
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document doc = builder.parse(new ByteArrayInputStream(s.getBytes()));
            final Node node = doc.getDocumentElement();
            LOG.debug("marshal: node = {}", node);
            return node;
        } catch (final Exception e) {
            final IllegalArgumentException ex = new IllegalArgumentException("Unable to marshal object " + from, e);
            LOG.debug("ex = {}", ex, ex);
            throw ex;
        }
    }

}