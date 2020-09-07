package com.github.taixiongliu.hapi.dom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <b>XML document parser</b>
 * <br><br>
 * read configuration from XML file ({@code <K,V>} key=nodeName, value=textContent)
 * @author taixiong.liu
 */
public class DOMParser {
	private DocumentBuilderFactory builderFactory;
	public DOMParser() {
		// TODO Auto-generated constructor stub
		builderFactory = DocumentBuilderFactory.newInstance();
	}
	private Document parse(String filePath) {
		File file = new File(filePath);
		//change file if project created with maven
		if(!file.exists()){
			file = new File(getPath(filePath));
		}
	    Document document = null; 
        DocumentBuilder builder;
		try {
			builder = builderFactory.newDocumentBuilder();
			//parse an XML file into a Document object
	        document = builder.parse(file);
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return document; 
	}
	
	private String getPath(String filePath){
		StringBuilder sb = new StringBuilder();
		sb.append("src");
		sb.append(File.separator);
		sb.append("main");
		sb.append(File.separator);
		sb.append("resources");
		sb.append(File.separator);
		sb.append(filePath);
		return sb.toString();
	}
	
	/**
	 * 
	 * @param fileName : XML file name
	 * @return XML {@code <K,V>} nodeName(key) and textContent(value)
	 * <br><br>
	 * <b>map empty:parse error or file not exists</b>
	 */
	public Map<String, String> parseMap(String fileName){
		Map<String, String> map = new HashMap<String, String>();
        //parse result
		Document document = parse(fileName);
		//if parse error or file not exists
		if(document == null){
			return map;
		}
        //root element 
        Element rootElement = document.getDocumentElement(); 
 
        //traverse child elements 
        NodeList nodes = rootElement.getChildNodes(); 
        for (int i=0; i < nodes.getLength(); i++) { 
           Node node = nodes.item(i); 
           if (node.getNodeType() == Node.ELEMENT_NODE) {   
              Element child = (Element) node; 
              //cache child element 
              map.put(child.getNodeName(), child.getTextContent());
           } 
        } 
        
        return map;
	}
	
	/**
	 * 
	 * @param fileName : XML file name
	 * @return XML {@code <K,V>} nodeName(key) and textContent(value)
	 * <br><br>
	 * <b>map empty:parse error or file not exists</b>
	 */
	public List<Map<String, String>> parseListMap(String fileName){
		List<Map<String, String>> maps = new ArrayList<Map<String,String>>();
        //parse result
		Document document = parse(fileName);
		//if parse error or file not exists
		if(document == null){
			return maps;
		}
        //root element 
        Element rootElement = document.getDocumentElement(); 
 
        //traverse bean list
        NodeList nodes = rootElement.getChildNodes(); 
        for (int i=0; i < nodes.getLength(); i++) { 
           Node node = nodes.item(i); 
           if (node.getNodeType() == Node.ELEMENT_NODE) {   
              Element child = (Element) node; 
              Map<String, String> map = new HashMap<String, String>();
              NodeList bean = child.getChildNodes();
              //traverse child elements
              for(int j = 0; j < bean.getLength(); j ++){
            	  Node nd = bean.item(j); 
                  if (nd.getNodeType() == Node.ELEMENT_NODE) {
                	  Element cd = (Element) nd;
                	  //cache child element 
                	  map.put(cd.getNodeName(), cd.getTextContent());
                  }
              }
              maps.add(map);
           } 
        } 
        
        return maps;
	}
}
