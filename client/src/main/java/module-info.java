open module edu.semitotal.commander.client {

    requires edu.semitotal.commander.shared.api;

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;

    requires static lombok;

    exports edu.semitotal.commander.client;
}