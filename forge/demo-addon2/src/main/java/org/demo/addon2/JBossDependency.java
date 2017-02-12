package org.demo.addon2;

import java.io.OutputStream;
import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.forge.addon.javaee.servlet.ServletFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.WebResourcesFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;

import org.jboss.forge.addon.ui.context.UIBuilder;

import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;

import org.jboss.forge.addon.ui.metadata.WithAttributes;

import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.w3c.dom.*;

public class JBossDependency extends AbstractProjectCommand {

	@Inject
	private ProjectFactory projectFactory;

	@Inject
	@WithAttributes(label = "Dependency", required = true, description = "Enter dependencies")
	UIInput<String> dependencies;

	public void initializeUI(UIBuilder builder) throws Exception {
		ServletFacet<?> servletFacet = getSelectedProject(builder.getUIContext()).getFacet(ServletFacet.class);
		builder.add(dependencies);
	}

	public Result execute(UIExecutionContext context) throws Exception {
		ServletFacet<?> servletFacet = getSelectedProject(context).getFacet(ServletFacet.class);
		DirectoryResource dir = servletFacet.getWebInfDirectory();
		DirectoryResource webRoot = getSelectedProject(context).getFacet(WebResourcesFacet.class).getWebRootDirectory();
		FileResource file = (FileResource) webRoot.getChild("WEB-INF/jboss-deployment-structure.xml");
		file.createNewFile();
		OutputStream stream = file.getResourceOutputStream();
		System.out.println("Creating dependencies " + dependencies.getValue());
		String content = createXML();

		byte[] bytes = content.getBytes();
		stream.write(bytes);
		stream.close();
		return Results.success("Added jboss-deployment-structure.xml with dependencies");
	}

	@Override
	protected ProjectFactory getProjectFactory() {

		return projectFactory;
	}

	@Override
	protected boolean isProjectRequired() {

		return true;
	}

	public String createXML() {
		String strResult = null;
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("jboss-deployment-structure");
			doc.appendChild(rootElement);

			// staff elements
			Element deployment = doc.createElement("deployment");
			rootElement.appendChild(deployment);

			StringTokenizer st = new StringTokenizer(dependencies.getValue(), ",");

			while (st.hasMoreTokens()) {
				Element module = doc.createElement("module");
				deployment.appendChild(module);

				// set attribute to staff element
				Attr attr = doc.createAttribute("name");
				attr.setValue(st.nextToken());
				module.setAttributeNode(attr);
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);

			transformer.transform(source, result);
			strResult = writer.toString();

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
		return strResult;
	}
}
