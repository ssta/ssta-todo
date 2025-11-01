package com.ssta.todo;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

  public MainView() {
    H1 title = new H1("TODO Application");
    add(title);

    setSizeFull();
    setJustifyContentMode(JustifyContentMode.START);
    setAlignItems(Alignment.CENTER);
  }
}
