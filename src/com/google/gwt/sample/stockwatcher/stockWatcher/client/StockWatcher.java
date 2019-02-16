package com.google.gwt.sample.stockwatcher.stockWatcher.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockWatcher implements EntryPoint {

    private static final int REFRESH_INTERVAL = 5000; //ms
    private VerticalPanel mainPanel = new VerticalPanel();
    private FlexTable stocksFlexTable = new FlexTable();
    private HorizontalPanel addPanel = new HorizontalPanel();
    private TextBox newSymbolTexBox = new TextBox();
    private Button addStockButton = new Button("Add");
    private Label lastUpdateLabel = new Label();
    private List<String> stocks = new ArrayList<>();

    public void onModuleLoad() {
        stocksFlexTable.setText(0, 0, "Smbol");
        stocksFlexTable.setText(0, 1, "Price");
        stocksFlexTable.setText(0, 2, "Change");
        stocksFlexTable.setText(0, 3, "Remove");
        stocksFlexTable
                .getRowFormatter()
                .addStyleName(0,"watchListHeader");


        addPanel.add(newSymbolTexBox);
        addPanel.add(addStockButton);
        mainPanel.add(stocksFlexTable);
        mainPanel.add(addPanel);
        mainPanel.add(lastUpdateLabel);
        RootPanel.get("stockList").add(mainPanel);
        newSymbolTexBox.setFocus(true);

        Timer refreshTimer = new Timer() {
            @Override
            public void run() {
                refreshWatchList();
            }
        };
        refreshTimer.scheduleRepeating(REFRESH_INTERVAL);


        addStockButton.addClickHandler(event -> addStock());

        newSymbolTexBox.addKeyDownHandler(event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                addStock();
            }
        });


    }

    private void addStock() {
        final String symbol = newSymbolTexBox.getText().toUpperCase().trim();
        newSymbolTexBox.setFocus(true);

        //Stock between 1 and 10 characters, numbers, letters or dots allowed
        if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
            Window.alert("'" + symbol + "' is not a valid symbol.");
            newSymbolTexBox.selectAll();
            return;
        }
        newSymbolTexBox.setText("");

        if (stocks.contains(symbol)) {
            return;
        }

        stocks.add(symbol);
        int row = stocksFlexTable.getRowCount();
        stocksFlexTable.setText(row, 0, symbol);

        Button removeStockButton = new Button("x");
        removeStockButton.addClickHandler(event -> {
            int removedIndex = stocks.indexOf(symbol);
            stocks.remove(removedIndex);
            stocksFlexTable.removeRow(removedIndex + 1);
        });
        stocksFlexTable.setWidget(row, 3, removeStockButton);

        refreshWatchList();
    }

    private void refreshWatchList() {
        final double MAX_PRICE = 100.0; //100$
        final double MAX_PRICE_CHANGE = 0.02; //+/-2%

        StockPrice[] prices = new StockPrice[stocks.size()];
        for (int i = 0; i < stocks.size(); i++) {
            double price = Random.nextDouble() * MAX_PRICE;
            double change = price * MAX_PRICE_CHANGE
                    * (Random.nextDouble() * 2.0 - 1.0);
            prices[i] = new StockPrice(stocks.get(i), price, change);
        }

        updateTable(prices);
    }

    private void updateTable(StockPrice[] prices) {
        for (int i = 0; i < prices.length; i++) {
            updateTable(prices[i]);
        }

        DateTimeFormat dateFormat = DateTimeFormat.getFormat(
                DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM
        );
        lastUpdateLabel.setText("Last update : "
                + dateFormat.format(new Date()));
    }

    private void updateTable(StockPrice price) {
        if (!stocks.contains(price.getSymbol())) {
            return;
        }

        int row = stocks.indexOf(price.getSymbol()) + 1;

        String priceText = NumberFormat.getFormat("#,##0.00").format(price.getPrice());
        NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
        String changeText = changeFormat.format(price.getChange());
        String changePercentText = changeFormat.format(price.getChangePercent());

        stocksFlexTable.setText(row, 1, priceText);
        stocksFlexTable.setText(row, 2, changeText + " (" + changePercentText + "%)");
    }
}
