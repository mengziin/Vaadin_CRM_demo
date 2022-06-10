package com.example.application.views.list;

import com.example.application.data.entity.Contact;
import com.example.application.data.service.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.annotation.security.PermitAll;
import java.util.Collections;

@PermitAll
@Route(value = "",layout = MainLayout.class)
@PageTitle("Contacts | Vaadin CRM")
public class ListView extends VerticalLayout {
    Grid<Contact> grid = new Grid<>(Contact.class);
    TextField filterText = new TextField();
    ContactForm contactForm;

    CrmService service;

    public ListView(CrmService crmService) {
        this.service = crmService;
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();

        add(getToolbar(), getContent());
        
        updateList();
        
        closeEditor();
    }

    private void updateList() {
        grid.setItems(service.findAllContacts(filterText.getValue()));
    }
    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid,contactForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, contactForm);
        content.addClassNames("content");
        content.setSizeFull();
        return content;

    }

    private void configureForm() {
        contactForm = new ContactForm(service.findAllCompanies(), service.findAllStatuses());
        contactForm.setWidth("25em");
        contactForm.addListener(ContactForm.SaveEvent.class, this::saveContact);
        contactForm.addListener(ContactForm.DeleteEvent.class, this::deleteContact);
        contactForm.addListener(ContactForm.CloseEvent.class, e -> closeEditor());
    }

    private void saveContact(ContactForm.SaveEvent event) {
        service.saveContact(event.getContact());
        updateList();
        closeEditor();
    }

    private void deleteContact(ContactForm.DeleteEvent event) {
        service.deleteContact(event.getContact());
        updateList();
        closeEditor();
    }

    private void configureGrid(){
        grid.addClassNames("contact-grid");
        grid.setSizeFull();
        grid.setColumns("firstName", "lastName", "email");
        grid.addColumn(contact -> contact.getStatus().getName()).setHeader("Status");
        grid.addColumn(contact -> contact.getCompany().getName()).setHeader("Company");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event ->
                editContact(event.getValue()));
    }

    private HorizontalLayout getToolbar(){
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());


        Button addContactButton = new Button("Add Contact");

        addContactButton.addClickListener(click -> addContact());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addContactButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    public void editContact(Contact contact) {
        if (contact == null) {
            closeEditor();
        } else {
            contactForm.setContact(contact);
            contactForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        contactForm.setContact(null);
        contactForm.setVisible(false);
        removeClassName("editing");
    }

    private void addContact() {
        grid.asSingleSelect().clear();
        editContact(new Contact());
    }
}
