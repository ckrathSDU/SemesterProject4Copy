package dk.sdu.mmmi.coreinit.controllers;

import dk.sdu.mmmi.commmonProductionManager.IProductionManager;
import dk.sdu.mmmi.coreinit.ui.ProductionApplication;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.springframework.context.ApplicationContext;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProductionHistoryController implements Initializable {

    @FXML
    private ListView<String> q_lst_history;

    @FXML
    private ListView<String> ps_lst_history;

    @FXML
    private Label pId_lbl;

    @FXML
    private Label ts_started_lbl;

    @FXML
    private Label ts_finished_lbl;



    private ApplicationContext context = ProductionApplication.getApplicationContext();
    private ObservableList<String> productionsPersisted = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UIUpdater ui = new UIUpdater(1000);

        Thread t1 = new Thread(ui);
        t1.setDaemon(true);
        t1.start();

        q_lst_history.setItems(productionsPersisted);
    }

    @FXML
    void showProductionSequence() {
        ps_lst_history.getItems().clear();

        try{
            String str = q_lst_history.getSelectionModel().getSelectedItem();
            str = str.substring(1, str.length() - 1);
            String[] operations = str.split(", ");
            for(int i = 0; i < operations.length; i++){
                ps_lst_history.getItems().add(operations[i]);
            }
            for (Map.Entry<String, IProductionManager> iProductionManagerEntry : context.getBeansOfType(IProductionManager.class).entrySet()) {
                Map<String, Object> metaData = iProductionManagerEntry.getValue().getProductionMetaData(q_lst_history.getSelectionModel().getSelectedIndex());
                pId_lbl.setText((String) metaData.get("productionId"));

                LocalDateTime min_date = (LocalDateTime) metaData.get("min_ts");
                LocalDateTime max_date = (LocalDateTime) metaData.get("max_ts");
                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                ts_started_lbl.setText(min_date.format(format));
                ts_finished_lbl.setText(max_date.format(format));

            }

        } catch (NullPointerException ignored){}
    }

    @FXML
    void addSequenceToQueue(){
        for (Map.Entry<String, IProductionManager> iProductionManagerEntry : context.getBeansOfType(IProductionManager.class).entrySet()) {
            if (q_lst_history.getSelectionModel().getSelectedIndex() != -1) {
                TreeMap<Integer, String> operationQueueTreeMap = iProductionManagerEntry.getValue().getPersistedProductions().get(q_lst_history.getSelectionModel().getSelectedIndex());
                iProductionManagerEntry.getValue().queueProduction(operationQueueTreeMap);
            }
        }
    }

    public class UIUpdater implements Runnable{

        private long sleepTime;
        private boolean running;
        private String previousQueueContent = "";

        public UIUpdater(long sleepTime){
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            running = true;

            while(running){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<String, IProductionManager> iProductionManagerEntry : context.getBeansOfType(IProductionManager.class).entrySet()) {
                            String currentQueueContent = String.valueOf(iProductionManagerEntry.getValue().getPersistedProductions());

                            if (!currentQueueContent.equals(previousQueueContent)) {
                                productionsPersisted.clear();

                                for (TreeMap<Integer, String> tm : iProductionManagerEntry.getValue().getPersistedProductions()) {
                                    productionsPersisted.add(tm.toString());
                                }
                                previousQueueContent = currentQueueContent;
                            }
                        }
                    }
                });
                synchronized (this){
                    try{
                        wait(sleepTime);
                    } catch (InterruptedException e){
                        running = false;
                    }
                }
            }
        }
    }
}
