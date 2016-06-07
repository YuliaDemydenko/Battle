package model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ParseMessage {
	private Document xmlDoc;
	private static final Logger logger = LoggerFactory.getLogger(ParseMessage.class);
	
	public ParseMessage (String xmlString){
		DocumentBuilderFactory DocFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder xmlDocBuilder = null;
		try {
			xmlDocBuilder = DocFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Parse XML failure.");	
		}
		ByteArrayInputStream theStream;
	    theStream = new ByteArrayInputStream( xmlString.getBytes() );
	    
	    try {
			xmlDoc = xmlDocBuilder.parse(theStream);
		} catch ( IOException e) {
			logger.error("Parse XML failure.");		
		} catch (SAXException e) {
			logger.error("Parse XML failure.");		
		}
	}
	public String getTitle(){
	   return xmlDoc.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
	}
	public String getFrom(){		
		 return xmlDoc.getElementsByTagName("from").item(0).getFirstChild().getNodeValue();
	}
	public String getTo(){		
		 return xmlDoc.getElementsByTagName("to").item(0).getFirstChild().getNodeValue();
	}
	public String getContent(){		
		 return xmlDoc.getElementsByTagName("content").item(0).getFirstChild().getNodeValue();
	}
	public String getLogin(){		
		 return xmlDoc.getElementsByTagName("login").item(0).getFirstChild().getNodeValue();
	}
	public String getPassword(){		
		 return xmlDoc.getElementsByTagName("password").item(0).getFirstChild().getNodeValue();
	}
	public String getCountGamesWon(){
		return xmlDoc.getElementsByTagName("countGamesWon").item(0).getFirstChild().getNodeValue();
	}
	public String getCountLostGames(){
		return xmlDoc.getElementsByTagName("countLostGames").item(0).getFirstChild().getNodeValue();
	}
	public String getPoints(){
		return xmlDoc.getElementsByTagName("points").item(0).getFirstChild().getNodeValue();
	}
	public static User parseUser(Node userNode){		
		NodeList userNodes = userNode.getChildNodes();
		User user = new User(Integer.parseInt(userNode.getAttributes().getNamedItem("id").getTextContent()),
				userNodes.item(0).getTextContent(),
				userNodes.item(1).getTextContent(),
				Integer.parseInt(userNodes.item(2).getTextContent()),
				Integer.parseInt(userNodes.item(3).getTextContent()),
				Integer.parseInt(userNodes.item(4).getTextContent())
				);		
		return user;
	}
	
	public static String createXML(int from, int to, String title, String content){
		StringBuilder xmlString = new StringBuilder("<?xml version='1.0'?>");
		xmlString.append("<GameServer>")
			.append("<title>")
			.append(title)
			.append("</title>")
			.append("<from>")
			.append(from)
			.append("</from>")
			.append("<to>")
			.append(to)
			.append("</to>")
			.append("<content>")
			.append(content)
			.append("</content>")
			.append("</GameServer>");
		return xmlString.toString();			
	}
}
