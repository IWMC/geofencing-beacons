package com.realdolmen.jsf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.realdolmen.MiscProperties;
import com.realdolmen.annotations.Simplified;
import com.realdolmen.entity.*;
import com.realdolmen.messages.Language;
import com.realdolmen.service.ReportsQueryBuilder;
import org.eclipse.persistence.jaxb.MarshallerProperties;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A controller for <code>/reports</code>.
 */
@Named("reports")
@ViewScoped
public class ReportsController extends Controller implements Serializable {

    @Inject
    @Simplified
    private transient ReportsQueryBuilder queryBuilder;

    @Inject
    private MiscProperties properties;

    @Inject
    private Language language;

    private ArrayList<? extends Object> records = new ArrayList<>();

    private ArrayList<ObjectNode> tableRecords = new ArrayList<>();

    private long querySize;
    private String filter;
    private Class<?> selectedEntity = RegisteredOccupation.class;

    public long getQuerySize() {
        return querySize;
    }

    public void setQuerySize(long querySize) {
        this.querySize = querySize;
    }

    public long getTableSize() {
        return Math.min(querySize, 10);
    }

    public void createReport() {
        createReport(0, 10);
    }

    @PostConstruct
    public void init() {
        generateTableRecords();
    }

    @Transactional
    public void createReport(Integer firstResult, Integer maxResults) {
        createReports(firstResult, maxResults, ReportsQueryBuilder.SELECT_ALL);
    }

    @Transactional
    public void createReports(Integer firstResult, Integer maxResults, String selectedColumns) {
        queryBuilder = queryBuilder
                .with(selectedEntity)
                .select(selectedColumns)
                .where(filter);

        setQuerySize(queryBuilder.getSize());
        Object result = queryBuilder.build(firstResult, maxResults);

        if (result instanceof List) {
            records = new ArrayList<>((List<?>) result);
        } else if (result instanceof ArrayNode) {
            ArrayList<ObjectNode> list = new ArrayList<>();
            ((ArrayNode) result).forEach(n -> list.add((ObjectNode) n));
            tableRecords = list;
        }
    }

    @Transactional
    public void downloadRecordsJson() throws IOException, JAXBException {
        downloadRecords("application/json", ".json");
    }

    @Transactional
    public void downloadRecordsXml() throws IOException, JAXBException {
        downloadRecords(null, ".xml");
    }

    @Transactional
    public void downloadRecords(String mediaType, String fileExtension) throws IOException, JAXBException {
        createReport(0, null);
        HttpServletResponse response = (HttpServletResponse) getFacesContext().getExternalContext().getResponse();

        response.reset();
        response.setContentType(mediaType);
        final String fileName = selectedEntity.getSimpleName() + "s - " + new Date().toString();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + fileExtension + "\"");

        OutputStream output = response.getOutputStream();
        Marshaller marshaller = JAXBContext.newInstance(JAXBList.class, selectedEntity).createMarshaller();
        if (mediaType != null) {
            marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, mediaType);
        }
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
        marshaller.marshal(new JAXBList<>(records), output);

        getFacesContext().responseComplete();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void selectEmployees() {
        selectedEntity = Employee.class;
        filter = "";
        generateTableRecords();
    }

    public void selectRegisteredOccupations() {
        selectedEntity = RegisteredOccupation.class;
        filter = "";
        generateTableRecords();
    }

    public void selectProject() {
        selectedEntity = Project.class;
        filter = "";
        generateTableRecords();
    }

    public void selectOccupations() {
        selectedEntity = Occupation.class;
        filter = "";
        generateTableRecords();
    }

    public void selectTasks() {
        selectedEntity = Task.class;
        filter = "";
        generateTableRecords();
    }

    public void generateTableRecords() {
        createReports(0, 10, properties.getString(selectedEntity.getSimpleName() + ".basicColumns"));
    }

    public List<ObjectNode> getTableRecords() {
        return tableRecords;
    }

    public List<String> getTableFieldList() {
        return Arrays.asList(properties.getString(selectedEntity.getSimpleName() + ".basicColumns").split(","));
    }

    public List<String> getPrettyTableFieldList() {
        String columns = properties.getString(selectedEntity.getSimpleName() + ".basicColumns.pretty");
        return Arrays.asList(columns.split(","));
    }

    public String translateValues(int columnIndex, JsonNode value) {
        final String stringValue = value.toString();
        if (stringValue.equalsIgnoreCase("false")) {
            return language.getString("no");
        } else if (stringValue.equalsIgnoreCase("true")) {
            return language.getString("yes");
        }

        if (getTableFieldList().get(columnIndex).equals("jobFunction")) {
            return language.getString("employee.jobtitle." + value.toString());
        }

        if (getTableFieldList().get(columnIndex).toLowerCase().contains("hour")) {
            if (value.asDouble() == value.asInt()) {
                return getLanguage().getString("project.task.hours", value.asInt());
            } else if (value.asInt() == 0) {
                return getLanguage().getString("project.task.minutes", (int) ((value.asDouble() - Math.floor(value.asDouble())) * 60));
            }

            return getLanguage().getString("project.task.hours_minutes",
                    value.asInt(),
                    (int) Math.ceil((value.asDouble() - Math.floor(value.asDouble())) * 60));
        }

        try {
            double doubleValue = Double.parseDouble(stringValue);
            if (doubleValue == (int) doubleValue) {
                return String.valueOf(Integer.parseInt(stringValue));
            }
        } catch (NumberFormatException nfex) {
        }

        return value.toString();
    }

    public String getLimitedPreviewMessage() {
        return language.getString("reports.showing_limited_size", getTableSize(), getQuerySize());
    }

    @XmlRootElement(name = "list")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    private static class JAXBList<E> {

        private List<E> list = new ArrayList<>();

        public JAXBList() {
        }

        public JAXBList(List<E> list) {
            this.list = list;
        }

        @XmlAttribute(name = "size", required = true)
        public int getSize() {
            return list.size();
        }

        @XmlElement(name = "element")
        public List<E> getValues() {
            return list;
        }
    }
}
