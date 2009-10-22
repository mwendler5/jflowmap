package jflowmap.data._old;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.xpath.Xb1XPath;

/**
 * @author Ilya Boyandin
 */
public class XmlFlowData extends AbstractFlowData {

	private XmlFlowData() {
		
	}

	public static IFlowData loadFrom(URL resource) throws XmlBuilderException, XmlPullParserException, IOException {
		XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
		return loadFrom(builder.parseLocation(resource.toString()));
	}
	
	public static XmlFlowData loadFrom(String filename) throws XmlPullParserException, IOException {
		XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
		return loadFrom(builder.parseReader(new FileReader(filename)));
	}
	
	@SuppressWarnings("unchecked")
	public static XmlFlowData loadFrom(XmlDocument doc) throws XmlPullParserException, IOException {
		XmlFlowData data = new XmlFlowData();
		
		List<String> flowAttrNames = new ArrayList<String>();
		List<String> flowAttrValues = new ArrayList<String>();
		
		List<XmlElement> flowElems = new Xb1XPath("/flows/flow").selectNodes(doc);
		for (XmlElement flowEl : flowElems) {
			Iterator it = flowEl.children();
			while (it.hasNext()) {
				Object child = it.next();
				if (child instanceof XmlElement) {
					XmlElement flowVal = (XmlElement)child;
					Iterator cit = flowVal.children();
					if (cit.hasNext()) {
						String value = cit.next().toString().trim();
						if (value.length() > 0) {
							flowAttrNames.add(flowVal.getName());
							flowAttrValues.add(value);
						}
					}
				}
			}
			
			data.addFlow(flowEl.getAttributeValue(null, "from"), flowEl.getAttributeValue(null, "to"),
					flowEl.getAttributeValue(null, "label"),
					flowAttrNames.toArray(new String[flowAttrNames.size()]),
					flowAttrValues.toArray(new String[flowAttrNames.size()]));

			flowAttrNames.clear();
			flowAttrValues.clear();
		}

		return data;
	}
}