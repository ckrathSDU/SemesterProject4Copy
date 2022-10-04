package dk.sdu.mmmi.coreinit.controllers;

import dk.sdu.mmmi.commmonProductionManager.IProductionManager;
import dk.sdu.mmmi.common.data.AssetManager;
import dk.sdu.mmmi.common.services.ICommunicatorService;
import dk.sdu.mmmi.coreinit.ui.ProductionApplication;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MonitorController implements Initializable {

    @FXML
    private Label agv_asset_lbl, asset_warehouse_lbl, asset_assembly_station_lbl, agv_state_lbl, as_state_lbl,
            as_last_op_lbl, agv_ts_lbl, warehouse_ts_lbl, warehouse_state_lbl, agv_battery_lbl, as_ts_lbl, as_curr_op_lbl,  agv_program_lbl;
    @FXML
    private ListView<String> warehouse_inventory_lst, ps_lst, q_lst;


    ApplicationContext context = ProductionApplication.getApplicationContext();
    AssetManager assetManager = context.getBean(AssetManager.class);
    private ObservableList<String> productions = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Asset status threading - see inner class. SleepTimer for timer between updates
        AssetUpdater assetUpdater = new AssetUpdater(1000);

        Thread t1 = new Thread(assetUpdater);
        t1.setDaemon(true);
        t1.start();

        warehouse_inventory_lst.setFocusTraversable(false);

        q_lst.setItems(productions);

    }

    @FXML
    void startProduction() {
        System.out.print("Start production...");

        Runnable r = () -> {
            for (Map.Entry<String, IProductionManager> iProductionManagerEntry : context.getBeansOfType(IProductionManager.class).entrySet()) {
                TreeMap<Integer, String> production = iProductionManagerEntry.getValue().getProductionQueue().get(q_lst.getSelectionModel().getSelectedIndex());
                iProductionManagerEntry.getValue().startProduction(production);
                System.out.println("Production finished...");
                iProductionManagerEntry.getValue().getProductionQueue().remove(q_lst.getSelectionModel().getSelectedIndex());
            }
        };

        Thread thread = new Thread(r);
        thread.start();
    }

    @FXML
    void startQueue() {
        System.out.println("Starting queue...");

        Runnable r = () -> {
            for (Map.Entry<String, IProductionManager> iProductionManagerEntry : context.getBeansOfType(IProductionManager.class).entrySet()) {
                List<TreeMap<Integer, String>> productionQueue = iProductionManagerEntry.getValue().getProductionQueue();

                while (!productionQueue.isEmpty()) {
                    iProductionManagerEntry.getValue().startProduction(productionQueue.get(0));
                    System.out.println("Production finished...");
                    productionQueue.remove(productionQueue.get(0));
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();
    }

    @FXML
    void showProductionSequence() {
        ps_lst.getItems().clear();
        try{
            String str = q_lst.getSelectionModel().getSelectedItem();
            str = str.substring(1, str.length() - 1);
            String[] operations = str.split(", ");
            for(int i = 0; i < operations.length; i++){
                ps_lst.getItems().add(operations[i]);
            }
        } catch (NullPointerException ignored) {
        }
    }

    @FXML
    void removeProductionFromQueue(){
        for (Map.Entry<String, IProductionManager> iProductionManagerEntry : context.getBeansOfType(IProductionManager.class).entrySet()) {
            iProductionManagerEntry.getValue().getProductionQueue().remove(q_lst.getSelectionModel().getSelectedIndex());
        }
    }

    public class AssetUpdater implements Runnable {

        private long sleepTime;
        private boolean running;
        private String previousQueueContent = "";

        public AssetUpdater(long sleepTime) {
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            running = true;

            while (running) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        for (Map.Entry<String, IProductionManager> iProductionManagerEntry : context.getBeansOfType(IProductionManager.class).entrySet()) {
                            String currentQueueContent = String.valueOf(iProductionManagerEntry.getValue().getProductionQueue());

                            if (!currentQueueContent.equals(previousQueueContent)) {
                                productions.clear();

                                for (TreeMap<Integer, String> tm : iProductionManagerEntry.getValue().getProductionQueue()) {
                                    productions.add(tm.toString());
                                }
                                previousQueueContent = currentQueueContent;
                            }
                        }

                        for (Map.Entry<String, ICommunicatorService> iCommunicatorServiceEntry : context.getBeansOfType(ICommunicatorService.class).entrySet()) {

                            JSONObject json = null;
                            try {
                                json = new JSONObject(iCommunicatorServiceEntry.getValue().getStatus(assetManager));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (iCommunicatorServiceEntry.getKey().equals("warehouseControlSystem")) {
                                try {
                                    warehouse_state_lbl.setText(json.getString("State"));
                                    if (json.getString("State").equals("1")) {
                                        asset_warehouse_lbl.setTextFill(Color.color(0, 1, 0));
                                        asset_warehouse_lbl.setText("Warehouse - RUNNING");
                                    } else {
                                        asset_warehouse_lbl.setTextFill(Color.color(0, 0, 0));
                                        asset_warehouse_lbl.setText("Warehouse");
                                    }
                                    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    Date timestamp = ft.parse(json.getString("DateTime"));
                                    SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                    ft2.setTimeZone(TimeZone.getTimeZone("GMT+4"));
                                    warehouse_ts_lbl.setText(ft2.format(timestamp));
                                    warehouse_inventory_lst.getItems().clear();
                                    warehouse_inventory_lst.getItems().add("ID    Item");
                                    JSONArray arrJson = json.getJSONArray("Inventory");
                                    for (int i = 0; i < arrJson.length(); i++) {
                                        JSONObject inventoryItem = new JSONObject(arrJson.getString(i));
                                        warehouse_inventory_lst.getItems().add(inventoryItem.getString("Id")
                                                + "  |  " + inventoryItem.get("Content"));
                                    }
                                } catch (JSONException e) {
                                    break;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (iCommunicatorServiceEntry.getKey().equals("AGVControlSystem")) {
                                try {
                                    agv_state_lbl.setText(json.getString("state"));
                                    if (json.getString("state").equals("2")) {
                                        agv_asset_lbl.setTextFill(Color.color(0, 1, 0));
                                        agv_asset_lbl.setText("AGV - RUNNING");
                                    } else {
                                        agv_asset_lbl.setTextFill(Color.color(0, 0, 0));
                                        agv_asset_lbl.setText("AGV");
                                    }
                                    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    Date timestamp = ft.parse(json.getString("timestamp"));
                                    SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                    ft2.setTimeZone(TimeZone.getTimeZone("GMT+4"));
                                    agv_ts_lbl.setText(ft2.format(timestamp));
                                    agv_program_lbl.setText(json.getString("program name"));
                                    agv_battery_lbl.setText(json.getString("battery"));
                                } catch (JSONException e) {
                                    break;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (iCommunicatorServiceEntry.getKey().equals("assemblyStationControlSystem")) {
                                try {
                                    as_state_lbl.setText(json.getString("State"));
                                    if (json.getString("State").equals("1")) {
                                        asset_assembly_station_lbl.setTextFill(Color.color(0, 1, 0));
                                        asset_assembly_station_lbl.setText("Assembly Station - RUNNING");
                                    } else {
                                        asset_assembly_station_lbl.setTextFill(Color.color(0, 0, 0));
                                        asset_assembly_station_lbl.setText("Assembly Station");
                                    }
                                    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    Date timestamp = ft.parse(json.getString("TimeStamp"));
                                    SimpleDateFormat ft2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                    ft2.setTimeZone(TimeZone.getTimeZone("GMT+4"));
                                    as_ts_lbl.setText(ft2.format(timestamp));
                                    as_last_op_lbl.setText(json.getString("LastOperation"));
                                    as_curr_op_lbl.setText(json.getString("CurrentOperation"));
                                } catch (JSONException e) {
                                    break;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                });
                synchronized (this) {
                    try {
                        wait(sleepTime);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted: " + Thread.currentThread());
                        running = false;
                    }
                }
            }
        }
    }
}
