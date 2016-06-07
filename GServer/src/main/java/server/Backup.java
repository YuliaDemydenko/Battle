package server;

import model.User;
import org.slf4j.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

public class Backup {
	private static final Logger logger = LoggerFactory.getLogger(Backup.class);
	
	private Document doc;
	
	public Backup() {
		try {
			doc=createDoc();
		} catch (Exception e) {
			logger.error(String.valueOf(e));
		} 
	}
	public Document getDoc(){
		return doc;
	}			
	public void addUserToXML(User user) throws ParserConfigurationException, SAXException, IOException, TransformerException{				
			Node root = doc.getElementsByTagName("users").item(0);
			Node userNode = createUserNode(user,doc);				
		    root.appendChild(userNode);		    		    
		    saveDocument(doc);		  
	}
	public synchronized void updateUser(User user) throws TransformerException, ParserConfigurationException, SAXException, IOException {
		Node userNode = createUserNode(user,doc);				
		Node oldUserNode = getElementById(""+user.getId(), doc, user);			
		doc.getElementsByTagName("users").item(0).replaceChild(userNode, oldUserNode);						
		saveDocument(doc);
	}
	public Node getElementById(String id, Document doc, User user ){
		NodeList oldUserNodeList = doc.getElementsByTagName("user");
		Node oldUserNode=null;
		for (int i=0;i<oldUserNodeList.getLength();i++){
			if (oldUserNodeList.item(i).getAttributes().getNamedItem("id").getTextContent().equals(""+user.getId())){
				oldUserNode=oldUserNodeList.item(i);
			}
		}
		return oldUserNode;
	}
	public synchronized void saveDocument (Document doc) throws TransformerException{
		 TransformerFactory transformerFactory = TransformerFactory.newInstance();
		    Transformer transformer = transformerFactory.newTransformer();
		    DOMSource source = new DOMSource(doc);
		    StreamResult result =  new StreamResult(new File("data/users.xml"));
		    transformer.transform(source, result);
	}
	public Node createUserNode(User user, Document doc) throws ParserConfigurationException, SAXException, IOException {
			Node userNode = doc.createElement("user");
			
			Attr idAttr = doc.createAttribute("id");
			idAttr.setValue(""+user.getId());
			((Element) userNode).setAttributeNode (idAttr);
			
			Node loginNode = doc.createElement("login");
			Node passwordNode = doc.createElement("password");
			Node countGamesWonNode = doc.createElement("countGamesWon");
			Node countLostGamesNode = doc.createElement("countLostGames");
			Node pointsNode = doc.createElement("points");

			loginNode.setTextContent(user.getLogin());
			passwordNode.setTextContent(user.getPassword());
			countGamesWonNode.setTextContent(""+user.getCountGamesWon());
			countLostGamesNode.setTextContent(""+user.getCountLostGames());
			pointsNode.setTextContent(""+user.getPoints());

			userNode.appendChild(loginNode);
			userNode.appendChild(passwordNode);
			userNode.appendChild(countGamesWonNode);
			userNode.appendChild(countLostGamesNode);
			userNode.appendChild(pointsNode);
			
			return userNode;
	}
	public Document createDoc() throws ParserConfigurationException, SAXException, IOException{
		String filepath = "data/users.xml";
		
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setValidating(false);
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.parse(new File(filepath));
		return doc;
	}
	
	
	public String createUserNode(User user){
		StringBuilder userNode = new StringBuilder("<user id=\"");
		userNode.append(user.getId())
			.append("\">")
			.append("<login>")
			.append(user.getLogin())
			.append("</login>")
			.append("<password>")
			.append(user.getPassword())
			.append("</password>")
			.append("<won>")
			.append(user.getCountGamesWon())
			.append("</won>")
			.append("<lost>")
			.append(user.getCountLostGames())
			.append("</lost>")
			.append("<points>")
			.append(user.getPoints())
			.append("</points>")
			.append("</user>");
		return userNode.toString();
	}
}
