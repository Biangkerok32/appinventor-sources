package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.List;

public abstract class ChartDataModel<T extends DataSet, D extends ChartData> {
    protected D data;
    protected T dataset;

    /**
     * Initializes a new ChartDataModel object instance.
     *
     * @param data  Chart data instance
     */
    protected ChartDataModel(D data) {
        this.data = data;
    }

    /**
     * Returns the size of the tuples that this Data Series
     * accepts.
     *
     * @return  tuple size (integer)
     */
    protected abstract int getTupleSize();

    public T getDataset() {
        return dataset;
    }

    public ChartData getData() {
        return data;
    }

    /**
     * Changes the color of the data set.
     *
     * @param argb  new color
     */
    public void setColor(int argb) {
        dataset.setColor(argb);
    }

    /**
     * Changes the label of the data set.
     *
     * @param text  new label text
     */
    public void setLabel(String text) {
        dataset.setLabel(text);
    }

    /**
     * Sets the elements of the Data Series from a CSV-formatted String.
     *
     * @param elements String in CSV format
     */
    public void setElements(String elements) {
        // Get the expected number of tuples
        int tupleSize = getTupleSize();

        // Split all the CSV entries by comma
        String[] entries = elements.split(",");

        // Iterate over every tuple (by grouping entries)
        // We start from tupleSize - 1 since the (tupleSize - 1)-th
        // entry will be the last entry of the tuple.
        // The index is incremented by the tupleSize to move to the next
        // group of entries for a tuple.
        for (int i = tupleSize - 1; i < entries.length; i += tupleSize) {
            List<String> tupleEntries = new ArrayList<String>();

            // Iterate over all the tuple entries
            // First entry is in (i - tupleSize + 1)
            for (int j = tupleSize - 1; j >= 0; --j) {
                int index = i - j;
                tupleEntries.add(entries[index]);
            }

            // Add entry from the parsed tuple
            addEntryFromTuple(YailList.makeList(tupleEntries));
        }
    }

    /**
     * Imports data from a YailList which contains nested tuples
     *
     * @param list  YailList containing tuples
     */
    public void importFromList(YailList list) {
        // Iterate over all the tuples
        for (int i = 0; i < list.size(); ++i) {
            YailList tuple = (YailList)list.getObject(i);
            addEntryFromTuple(tuple);
        }
    }

    public void importFromCSV(CSVFile dataFile, YailList columns) {
        YailList rows = dataFile.Rows();

        ArrayList<YailList> dataColumns = new ArrayList<YailList>();

        for (int i = 0; i < columns.size(); ++i) {
            String columnName = columns.getString(i);

            if (columnName == null || columnName.equals("")) {
                // Default option
                dataColumns.add(getDefaultValues(rows.size()));
            } else {
                dataColumns.add(dataFile.getColumn(columnName));
            }
        }

        List<YailList> tuples = new ArrayList<YailList>();

        for (int i = 1; i < rows.size(); ++i) {
            ArrayList<String> tupleElements = new ArrayList<String>();

            for (YailList column : dataColumns) {
                tupleElements.add(column.getString(i));
            }

            YailList tuple = YailList.makeList(tupleElements);
            tuples.add(tuple);
        }

       importFromList(YailList.makeList(tuples));
    }

    /**
     * Adds an entry from a specified tuple.
     * @param tuple  Tuple representing the entry to add
     */
    public abstract void addEntryFromTuple(YailList tuple);

    /**
     * Deletes all the entries in the Data Series.
     */
    public void clearEntries() {
        dataset.clear();
    }

    /**
     * Sets the default styling properties of the Data Series.
     */
    protected abstract void setDefaultStylingProperties();

    /**
     * Returns a YailList of the specified size containing the
     * default values for the Data Series.
     * To be used in the context of importing data from sources
     * where the values for a certain dimension are not present
     * (e.g. y values are present, but x values are not)
     *
     * @param size  Number of entries to return
     * @return  YailList of the specified number of entries containing the default values.
     */
    protected abstract YailList getDefaultValues(int size);
}
