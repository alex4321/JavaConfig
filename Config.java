import com.sun.beans.decoder.DocumentHandler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.w3c.dom.*;
import sun.tools.jar.resources.jar;

public class Config {
    private static Document LoadDocument(Path filePath) {
        File f = new File(filePath.toString());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(f);
            return doc;
        }
        catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    private static void ParseConfigParams(Node root, HashMap<String, String> config, ArrayList<String> includes) {
        NodeList rootChildren = root.getChildNodes();

        for(int i=0;i<rootChildren.getLength();i++) {
            if(rootChildren.item(i).getNodeType()==Node.ELEMENT_NODE) {
                Element item = (Element)rootChildren.item(i);
                if(item.getTagName().equals("param")) {
                    config.put(item.getAttribute("name"), item.getAttribute("value"));
                }
                else if(item.getTagName().equals("include")) {
                    includes.add(includes.size(), item.getAttribute("file"));
                }
            }
        }
    }

    private static void ReplaceVariables(HashMap<String, String> loaded, HashMap<String, String> result) {
        Pattern variable = Pattern.compile("\\!(.+?)\\!");

        while(loaded.size()!=result.size()) {
            Iterator loadedIterator = loaded.entrySet().iterator();
            while(loadedIterator.hasNext()) {
                Entry<String, String> pairs = (Entry)loadedIterator.next();

                String loadedName = pairs.getKey();
                String loadedValue = pairs.getValue();
                StringBuilder resultValue = new StringBuilder(loadedValue);

                Matcher variables = variable.matcher(resultValue);

                boolean ready = true;
                while(variables.find()) {
                    String variableName = variables.group(1);
                    if(result.get(variableName)!=null) {
                        resultValue.replace(variables.start(1)-1, variables.end(1)+1, result.get(variableName));

                        variables = variable.matcher(resultValue);
                    }
                    else {
                        ready = false;
                    }
                }

                if(ready) {
                    result.put(loadedName, resultValue.toString());
                }
            }
        }
    }

    private static void ReplaceIncludeNames(HashMap<String, String> variables, ArrayList<String> includes) {
        Pattern variable = Pattern.compile("\\!(.+?)\\!");

        for(int i=0;i<includes.size();i++) {
            StringBuilder newName = new StringBuilder(includes.get(i));
            Matcher variablesMatcher = variable.matcher(newName);
            while(variablesMatcher.find()) {
                String variableName = variablesMatcher.group(1);
                String variableValue = variables.get(variableName);
                newName.replace(variablesMatcher.start(1)-1, variablesMatcher.end(1)+1, variableValue);

                variablesMatcher = variable.matcher(newName);
            }

            includes.set(i, newName.toString());
        }
    }

    private static void LoadConfigFile(Path filePath, HashMap<String, String> result) {
        HashMap<String, String> loaded = new HashMap<String, String>();
        loaded.putAll(result);
        Document config = Config.LoadDocument(filePath);
        if(config!=null) {
            config.getDocumentElement().normalize();

            Node root = config.getElementsByTagName("config").item(0);
            NodeList rootChildren = root.getChildNodes();

            ArrayList<String> includes = new ArrayList<String>();

            Config.ParseConfigParams(root, loaded, includes);
            Config.ReplaceVariables(loaded, result);
            Config.ReplaceIncludeNames(result, includes);

            for(int i=0;i<includes.size();i++) {
                Config.LoadConfigFile(Paths.get(includes.get(i)), result);
            }
        }
    }

    public static HashMap<String, String> LoadConfig(Path filePath) {
        HashMap<String, String> loaded = new HashMap<String, String>();
        HashMap<String, String> result = new HashMap<String, String>();
        Config.LoadConfigFile(filePath, result);

        return result;
    }
}
