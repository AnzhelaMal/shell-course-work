open module edu.semitotal.commander.shared.api {
    exports edu.semitotal.commander.api.request;
    exports edu.semitotal.commander.api.response;
    exports edu.semitotal.commander.api;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    requires static lombok;
}