package com.realdolmen.jsf;

import com.realdolmen.annotations.Simplified;
import com.realdolmen.entity.*;
import com.realdolmen.service.ReportsQueryBuilder;
import org.eclipse.persistence.jaxb.MarshallerProperties;

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

    private ArrayList<?> records = new ArrayList<>();

    private String filter;

    private Class<?> selectedEntity = RegisteredOccupation.class;

    public void createReport() {
        createReport(0, 10);
    }

    @Transactional
    public void createReport(Integer firstResult, Integer maxResults) {
        Object result = queryBuilder
                .with(selectedEntity)
                .select(ReportsQueryBuilder.SELECT_ALL)
                .where(filter).build(firstResult, maxResults);

        if (result instanceof List) {
            records = new ArrayList((List) result);
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
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "records" + fileExtension + "\"");

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
    }

    public void selectRegisteredOccupations() {
        selectedEntity = RegisteredOccupation.class;
    }

    public void selectProject() {
        selectedEntity = Project.class;
    }

    public void selectOccupations() {
        selectedEntity = Occupation.class;
    }

    public void selectTasks() {
        selectedEntity = Task.class;
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
