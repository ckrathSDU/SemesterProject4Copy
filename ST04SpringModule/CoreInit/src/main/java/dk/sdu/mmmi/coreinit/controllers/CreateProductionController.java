package dk.sdu.mmmi.coreinit.controllers;

import dk.sdu.mmmi.commmonProductionManager.IProductionManager;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.coreinit.ui.ProductionApplication;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.context.ApplicationContext;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class CreateProductionController implements Initializable {


    @FXML
    private ChoiceBox<String> choiceBoxAgv, choiceBoxAssembly, choiceBoxWarehouse;

    @FXML
    private ListView<String> prodSequenceListView;

    @FXML
    private TextField itemIDText, nameText, processIDText;

    @FXML
    private Label nameLabel;


    private ApplicationContext context = ProductionApplication.getApplicationContext();
    private AssetManager assetManager = context.getBean(AssetManager.class);
    private TreeMap<Integer, String> operationQueueTreeMap;

    private int queueCount = 1;
    private ObservableList<String> productionSequenceObservableList = FXCollections.observableArrayList();

    @FXML
    void addAGVOperation(){
        String selectedOperation = "AGV: " + choiceBoxAgv.getSelectionModel().getSelectedItem();
        //Remove whitespace
        String formatOperation = selectedOperation.replaceAll("\\s", "");
        operationQueueTreeMap.put(queueCount, formatOperation);
        productionSequenceObservableList.add(selectedOperation);
        queueCount++;
    }

    @FXML
    void addAssemblyOperation() {
        String processID = "AssemblyStation: " + processIDText.getText();
        String formatProcessID = "AssemblyStation:" + processIDText.getText();
        operationQueueTreeMap.put(queueCount, formatProcessID);
        productionSequenceObservableList.add(processID);
        queueCount++;
    }

    @FXML
    void removeOperationFromListView(){
        TreeMap<Integer, String> productionTempQueue = new TreeMap<>();
        int queueTempCount = 1;

        //The removal itself
        operationQueueTreeMap.remove(prodSequenceListView.getSelectionModel().getSelectedIndex() + 1);
        productionSequenceObservableList.remove(prodSequenceListView.getSelectionModel().getSelectedItem());

        //To update the production queue and its number
        for (String productionOperation : operationQueueTreeMap.values()){
            productionTempQueue.put(queueTempCount, productionOperation);
            queueTempCount++;
        }

        operationQueueTreeMap.clear();
        operationQueueTreeMap.putAll(productionTempQueue);

        //To update the queue count with the new queue
        setQueueCount(queueTempCount);
    }

    @FXML
    void addWarehouseOperation(){
        switch (choiceBoxWarehouse.getSelectionModel().getSelectedItem()){
            case "Insert item":
                Integer itemID = Integer.parseInt(itemIDText.getText());
                String itemName = nameText.getText();
                String productionInsert = "Warehouse:InsertItem," + itemID + "," + itemName;
                operationQueueTreeMap.put(queueCount, productionInsert);
                productionSequenceObservableList.add("Warehouse: Insert item " + itemName + ";" + itemID);
                queueCount++;
                break;
            case "Pick item":
                Integer itemIDPick = Integer.parseInt(itemIDText.getText());
                String productionPick = "Warehouse:PickItem," + itemIDPick;
                operationQueueTreeMap.put(queueCount, productionPick);
                productionSequenceObservableList.add("Warehouse: Pick item " + itemIDPick);
                queueCount++;
                break;
        }
    }

    @FXML
    void addSequenceToQueue(){
        System.out.println("Adding sequence to queue...");
        for (Map.Entry<String, IProductionManager> iProductionManagerEntry : context.getBeansOfType(IProductionManager.class).entrySet()) {
            iProductionManagerEntry.getValue().queueProduction(operationQueueTreeMap);
        }
        operationQueueTreeMap = new TreeMap<>();
        productionSequenceObservableList.clear();
        queueCount = 1;
    }

    @FXML
    void operationMoveUp() {
        if (prodSequenceListView.getSelectionModel().getSelectedIndex() != 0) {

            ObservableList<String> prodSequenceListViewTemp = prodSequenceListView.getItems();

            int selectedItemIndex = prodSequenceListView.getSelectionModel().getSelectedIndex();
            String selectedItem = prodSequenceListView.getSelectionModel().getSelectedItem();

            prodSequenceListViewTemp.add(selectedItemIndex - 1, selectedItem);
            prodSequenceListViewTemp.remove(selectedItemIndex + 1);

            prodSequenceListView.getItems().removeAll();
            prodSequenceListView.setItems(prodSequenceListViewTemp);

            String itemMovedUp = prodSequenceListView.getItems().get(selectedItemIndex - 1).replaceAll("\\s", "");
            String itemMovedDown = prodSequenceListView.getItems().get(selectedItemIndex).replaceAll("\\s", "");


            operationQueueTreeMap.put(selectedItemIndex, itemMovedUp);
            operationQueueTreeMap.put(selectedItemIndex + 1, itemMovedDown);

            prodSequenceListView.getSelectionModel().select( selectedItemIndex - 1);

        }
    }

    @FXML
    void operationMoveDown() {
        if (prodSequenceListView.getSelectionModel().getSelectedIndex() != prodSequenceListView.getItems().size() - 1) {
            ObservableList<String> prodSequenceListViewTemp = prodSequenceListView.getItems();

            int selectedItemIndex = prodSequenceListView.getSelectionModel().getSelectedIndex();
            String selectedItem = prodSequenceListView.getSelectionModel().getSelectedItem();

            prodSequenceListViewTemp.remove(selectedItemIndex);
            prodSequenceListViewTemp.add(selectedItemIndex + 1, selectedItem);


            prodSequenceListView.getItems().removeAll();
            prodSequenceListView.setItems(prodSequenceListViewTemp);


            String itemMovedDown = prodSequenceListView.getItems().get(selectedItemIndex + 1).replaceAll("\\s", "");
            String itemMovedUp = prodSequenceListView.getItems().get(selectedItemIndex).replaceAll("\\s", "");

            operationQueueTreeMap.put(selectedItemIndex + 2, itemMovedDown);
            operationQueueTreeMap.put(selectedItemIndex + 1, itemMovedUp);

            prodSequenceListView.getSelectionModel().select( selectedItemIndex + 1);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing...");

        UIUpdater u1 = new UIUpdater(10);
        Thread t1 = new Thread(u1);
        t1.setDaemon(true);
        t1.start();

        operationQueueTreeMap = new TreeMap<>();

        //Warehouse
        ObservableList<String> availableChoicesWarehouse = FXCollections.observableArrayList("Insert item", "Pick item");
        choiceBoxWarehouse.setItems(availableChoicesWarehouse);
        choiceBoxWarehouse.setValue("Insert item");

        //AGV
        ObservableList<String> availableChoicesAGV = FXCollections.observableArrayList("Move To Charger Operation", "Move To Assembly Operation",
               "Move To Storage Operation", "Put Assembly Operation", "Pick Assembly Operation", "Pick Warehouse Operation", "Put Warehouse Operation");
        choiceBoxAgv.setItems(availableChoicesAGV);
        choiceBoxAgv.setValue("Move To Charger Operation");

        //Production sequence ListView
        ObservableList<String> availableChoicesAssemblyStation = FXCollections.observableArrayList("Process ID");
        choiceBoxAssembly.setItems(availableChoicesAssemblyStation);
        choiceBoxAssembly.setValue("Process ID");

        //ListView setItems with ObservableList
        prodSequenceListView.setItems(productionSequenceObservableList);

    }

    public void setQueueCount(int queueCount) {
        this.queueCount = queueCount;
    }

    public class UIUpdater implements Runnable{

        private long sleepTime;
        private boolean running;

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

                        //Hiding "Name" field
                        if(choiceBoxWarehouse.getSelectionModel().getSelectedItem().equals("Pick item")){
                            nameText.setVisible(false);
                            nameLabel.setVisible(false);
                        } else {
                            nameText.setVisible(true);
                            nameLabel.setVisible(true);
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
