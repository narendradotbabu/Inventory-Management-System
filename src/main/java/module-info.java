module com.retailpos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.retailpos to javafx.fxml;
    opens com.retailpos.controller to javafx.fxml;
    opens com.retailpos.model to javafx.base;

    exports com.retailpos;
    exports com.retailpos.controller;
    exports com.retailpos.model;
    exports com.retailpos.dao;
    exports com.retailpos.factory;
    exports com.retailpos.util;
}
