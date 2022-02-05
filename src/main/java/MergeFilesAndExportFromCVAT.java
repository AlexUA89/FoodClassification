import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MergeFilesAndExportFromCVAT {

    static List<List<String>> dataFromFile = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        dataFromFile = readData();
        System.out.println(dataFromFile);
        NodeList xml = readXml();
        for (List<String> row : dataFromFile) {
            mergeRowWithLabeling(row, xml);
        }
        writeData(dataFromFile);
    }

    public static void writeData(List<List<String>> data) throws IOException {
        try (PrintWriter writer = new PrintWriter("result.csv")) {
            for (List<String> row : data) {
                String joinedRow = row.stream().collect(Collectors.joining(","));
                joinedRow += '\n';
                writer.write(joinedRow);
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    public static List<List<String>> readData() throws IOException {
        int count = 0;
        String file = "sheet.csv";
        List<List<String>> content = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                content.add(new ArrayList<>(Arrays.asList(line.split(","))));
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        }
        return content;
    }

    public static NodeList readXml() {
        try {
//creating a constructor of file class and parsing an XML file
            File file = new File("common.xml");
//an instance of factory that gives a document builder
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//an instance of builder to parse the specified xml file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("image");
            return nodeList;
// nodeList is not iterable, so we are using for loop

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void mergeRowWithLabeling(List<String> row, NodeList nodeList) {
        for (int itr = 0; itr < nodeList.getLength(); itr++) {
            Node image = nodeList.item(itr);
            String xmlFileName = image.getAttributes().getNamedItem("name").getTextContent().replace("--", ":").replace("_", "/");
            if (xmlFileName.equals(row.get(2))) {
                NodeList boxList = ((Element) image).getElementsByTagName("box");
                for (int boxItr = 0; boxItr < boxList.getLength(); boxItr++) {
                    Element box = (Element) boxList.item(boxItr);
                    adjustRow(box, row);
                }
            }
        }
    }

    private static Element findElementById(NodeList list, String name) {
        for (int itr = 0; itr < list.getLength(); itr++) {
            Element node = (Element) list.item(itr);
            if (name.equals(node.getAttribute("name"))) {
                return node;
            }
        }
        return null;
    }

    private static void adjustRow(Element box, List<String> row) {
        String coord = "xtl=" + box.getAttribute("xtl") + "; " + "ytl=" + box.getAttribute("ytl") + "; " +
                "xbr=" + box.getAttribute("xbr") + "; " + "ytl=" + box.getAttribute("ybr") + "; ";
        NodeList attributes = box.getElementsByTagName("attribute");
        String mealType = findElementById(attributes,"Meal Type").getTextContent();
        String healt = findElementById(attributes,"Health Benefits").getTextContent();

        // preparation
        String preparation = "";
        if (Boolean.parseBoolean(findElementById(attributes,"Raw").getTextContent())) {
            preparation += "Raw; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Boiled").getTextContent())) {
            preparation += "Boiled; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Baked").getTextContent())) {
            preparation += "Baked; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Fried").getTextContent())) {
            preparation += "Fried; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Deep Fried").getTextContent())) {
            preparation += "Deep Fried; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Preparation other").getTextContent())) {
            preparation += "Other; ";
        }


        // ingridients
        String ingridients = "";
        if (Boolean.parseBoolean(findElementById(attributes,"Other").getTextContent())) {
            ingridients += "Other; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Fruit").getTextContent())) {
            ingridients += "Fruit; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Vegetables").getTextContent())) {
            ingridients += "Vegetables; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Diary Products").getTextContent())) {
            ingridients += "Diary Products; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Egg").getTextContent())) {
            ingridients += "Egg; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Seafood").getTextContent())) {
            ingridients += "Seafood; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Meat").getTextContent())) {
            ingridients += "Meat; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Rice").getTextContent())) {
            ingridients += "Rice; ";
        }
        if (Boolean.parseBoolean(findElementById(attributes,"Bread & Wheat Products").getTextContent())) {
            ingridients += "Bread & Wheat Products; ";
        }

        row.add(ingridients);
        row.add(mealType);
        row.add(preparation);
        row.add(healt);
        row.add(coord);
    }

}
