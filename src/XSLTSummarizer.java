package de.spieleck.ingress.hackstat;

import java.util.Date;
import java.text.SimpleDateFormat;

import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

public class XSLTSummarizer
    implements Summarizer
{
    private final static SimpleDateFormat CREATE_DF = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private TransformerHandler trans;
    public final static String NSPC = "de.spieleck.ingress.hackstat";
    private final static AttributesImpl NOATT = new AttributesImpl();

    public XSLTSummarizer() 
        throws TransformerConfigurationException, SAXException
    { 
        SAXTransformerFactory fac = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        trans = fac.newTransformerHandler();
        Transformer transf = trans.getTransformer();
        transf.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setResult(new StreamResult("out.xml"));
        internalStart();
    }

    public XSLTSummarizer(String fName) 
        throws TransformerConfigurationException, SAXException
    {
        this(fName, "out.html");
    }

    public XSLTSummarizer(String fName, String oName) 
        throws TransformerConfigurationException, SAXException
    { 
        SAXTransformerFactory fac = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        trans = fac.newTransformerHandler(new StreamSource(fName));
        trans.setResult(new StreamResult(oName));
        internalStart();
    }

    private void internalStart()
        throws SAXException
    {
        trans.startDocument();
        startElement("hackstat");
        startElement("created");
        text(CREATE_DF.format(new Date()));
        endElement("created");
    }

    private double f, f2;
    private char[] buffer = new char[256];

    //-----------------------------------------------
    private void text(CharSequence seq)
        throws SAXException
    {
        int l = seq.length();
        if ( seq.length() > l ) buffer = new char[l];
        for(int i = 0; i < l; i++) buffer[i] = seq.charAt(i);
        trans.characters(buffer, 0, l);
    }

    private void text(String name, double value)
        throws SAXException
    {
        startElement(name);
        // text(String.format("%f",value));
        text(Double.toString(value));
        endElement(name);
    }

    private void startElement(String name)
        throws SAXException
    {
        startElement(name, NOATT);
    }

    private void startElement(String name, Attributes atts)
        throws SAXException
    {
        trans.startElement(NSPC, name, "hs:"+name, atts);
    }

    private void endElement(String name)
        throws SAXException
    {
        trans.endElement(NSPC, name, "hs:"+name);
    }

    private void key(Object key)
        throws SAXException
    {
        startElement("key");
        text(key.toString());
        endElement("key");
    }

    //-----------------------------------------------

    public void startColumn(CharSequence column)
        throws SAXException
    {
        startElement("column");
        key(column);
    }

    public void endColumn()
        throws SAXException
    {
        endElement("column");
    };

    public void start(CharSequence label)
        throws SAXException
    {
        startElement("stats");
        key(label);
    }

    public void description(CharSequence desc)
        throws SAXException
    {
        startElement("description");
        text(desc);
        endElement("description");
    }

    public void setNorms(double f, double f2)
    {
        this.f = f;
        this.f2 = f2;
    }

    public void item(Object key, int h)
        throws SAXException
    {
        startElement("item");
        key(key);
        startElement("absolute");
        text(Integer.toString(h));
        endElement("absolute");
        text("percentage", h*f);
        if ( f != f2 ) text("percentage2", h*f2);
        endElement("item");
    }

    public void item(Object key, Stats1D hs)
        throws SAXException
    {
        startElement("item2");
        key(key);
        double h = hs.sum();
        double s = hs.sdev();
        double m = 100.0*hs.average();
        double d = 2.0*100.0*hs.sdevAvg();
        text("average", h);
        text("sdev", s);
        text("min", m-d);
        text("max", m+d);
        endElement("item2");
    }

    public void value(CharSequence msg, double v)
        throws SAXException
    {
        startElement("value");
        key(msg);
        text("number", v);
        endElement("value");
    }

    public void finish(int sum)
        throws SAXException
    {
        text("sum", f * sum);
        endElement("stats");
    }

    public void close()
        throws SAXException
    {
        endElement("hackstat");
        trans.endDocument();
    }

}
