package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.components.common.ComponentConstants;
import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

import java.util.List;

public final class MockChart extends MockContainer {
    public static final String TYPE = "Chart";

    private static final String PROPERTY_NAME_TYPE = "Type";
    private static final String PROPERTY_NAME_DESCRIPTION = "Description";
    private static final String PROPERTY_NAME_LEGEND_ENABLED = "LegendEnabled";
    private static final String PROPERTY_NAME_GRID_ENABLED = "GridEnabled";
    private static final String PROPERTY_NAME_PIE_RADIUS = "PieRadius";
    private static final String PROPERTY_NAME_LABELS_FROM_STRING = "LabelsFromString";

    static {
        ResourcesType.setClientBundle(EmbeddedResources.INSTANCE);
    }

    protected MockChartView chartView;

    // Legal values for type are defined in
    // com.google.appinventor.components.common.ComponentConstants.java.
    private int type;

    // Keep track whether the children of the Mock Chart have been
    // reattached. The reattachment has to happen only once, since the Data
    // Series are part of the Chart object itself.
    private boolean childrenReattached = false;

    /**
     * Creates a new instance of a visible component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockChart(SimpleEditor editor) {
        super(editor, TYPE, images.image(), new MockChartLayout());

        // Since the Mcok Chart component is not a container in a normal
        // sense (attached components should not be visible), the Chart Widget
        // is added to the root panel, and the root panel itself is initialized.
        // This is done to ensure that Mock Chart Data components can be dragged
        // onto the Chart itself, rather than outside the Chart component.
        rootPanel.setStylePrimaryName("ode-SimpleMockComponent");

        // Since default type property does not invoke the setter,
        // initially, the Chart's type setter should be invoked
        // with the default value.
        setTypeProperty("0");

        initComponent(rootPanel);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        // The Children of the Mock Chart have not yet been attached
        // (this happens upon initializing the Chart which has child components)
        if (!childrenReattached) {
            // Attach all children MockComponents
            for (MockComponent child : children) {
                if (child instanceof MockChartData) {
                    // Re-add Data Components to the Mock Chart
                    ((MockChartData) child).addToChart(MockChart.this);
                }
            }

            // Update the state of children to reattached
            childrenReattached = true;
        }
    }

    @Override
    public void delete() {
        // Fully remove all attached Data components before
        // removing the Chart component
        for (int i = children.size() - 1; i >= 0; --i) {
            MockComponent child = children.get(i);
            child.delete();
        }

        super.delete();
    }

    /**
     * Sets the type of the Chart to the newly specified value.
     * @param value  new Chart type
     */
    private void setTypeProperty(String value) {
        // Update type
        type = Integer.parseInt(value);

        // Keep track whether this is the first time that
        // the Chart view is being initialized
        boolean chartViewExists = (chartView != null);

        // Remove the current Chart Widget from the root panel (if present)
        if (chartViewExists) {
            rootPanel.remove(chartView.getChartWidget());
        }

        // Create a new Chart view based on the supplied type
        chartView = createMockChartViewFromType(type);

        // Add the Chart Widget to the Root Panel (as the first widget)
        rootPanel.insert(chartView.getChartWidget(), 0);

        // Chart view already existed before, so the new Chart view must
        // be reinitialized.
        if (chartViewExists) {
            reinitializeChart();
        }
    }

  /**
   * Sets the pie radius of the Chart if the current type is
   * a Pie Chart, otherwise does nothing.
   * @param newValue  new Pie Radius value (as String)
   */
  private void setPieRadiusProperty(String newValue) {
      // Check if the Chart View is a Pie Chart to
      // change the value
      if (chartView instanceof MockPieChartView) {
        // Parse the value to an integer
        int value = Integer.parseInt(newValue);

        // Change the radius of the Pie Chart & re-draw the Chart
        ((MockPieChartView)chartView).setPieRadius(value);
        chartView.getChartWidget().draw();
      }
    }

    /**
     * Changes Chart property visibilities depending on the
     * current type of the Chart.
     *
     * Should be invoked after the Type property is changed.
     */
    private void changeChartPropertyVisibilities() {
        // Handle Pie Chart property hiding
        boolean showPieChartProperties = chartView instanceof MockPieChartView;
        showProperty(PROPERTY_NAME_PIE_RADIUS, showPieChartProperties);

        // Handle Axis Chart property hiding
        boolean showAxisChartProperties = chartView instanceof MockAxisChartView;
        showProperty(PROPERTY_NAME_GRID_ENABLED, showAxisChartProperties);
        showProperty(PROPERTY_NAME_LABELS_FROM_STRING, showAxisChartProperties);

        // If the component is currently selected, re-select it to refresh
        // the Properties panel. isSelected() should only be invoked when
        // the view is in a container, hence the additional check here.
        if (getContainer() != null && isSelected()) {
            onSelectedChange(true);
        }
    }

    /**
     * Creates and returns a new MockChartView object based on the type
     * (integer) provided
     * @param type  Chart type (integer representation)
     * @return new MockChartView object instance
     */
    private MockChartView createMockChartViewFromType(int type) {
        switch(type) {
            case ComponentConstants.CHART_TYPE_LINE:
                return new MockLineChartView();
            case ComponentConstants.CHART_TYPE_SCATTER:
                return new MockScatterChartView();
            case ComponentConstants.CHART_TYPE_AREA:
                return new MockAreaChartView();
            case ComponentConstants.CHART_TYPE_BAR:
                return new MockBarChartView();
            case ComponentConstants.CHART_TYPE_PIE:
                return new MockPieChartView();
            default:
                // Invalid argument
                throw new IllegalArgumentException("type:" + type);
        }
    }

    /**
     * Reinitializes the Chart view by reattaching all the Data
     * components and setting back all the properties.
     */
    private void reinitializeChart() {
      // Re-set all Chart properties to take effect on
      // the newly instantiated Chart View
      for (EditableProperty property : properties) {
        // The Type property should not be re-set, since
        // this method call is part of the Type setting process.
        if (!property.getName().equals(PROPERTY_NAME_TYPE)) {
          onPropertyChange(property.getName(), property.getValue());
        }
      }

      chartView.getChartWidget().draw();

      // Re-attach all children MockChartData components.
      // This is needed since the properties of the MockChart
      // are set after the Data components are attached to
      // the Chart, and thus they need to be re-attached.
      for (MockComponent child : children) {
          ((MockChartData) child).addToChart(MockChart.this);
      }
    }

    @Override
    public int getPreferredWidth() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
    }

    @Override
    public int getPreferredHeight() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        if (propertyName.equals(PROPERTY_NAME_TYPE)) {
            setTypeProperty(newValue);
            changeChartPropertyVisibilities();
        } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
            chartView.setBackgroundColor(newValue);
        } else if (propertyName.equals(PROPERTY_NAME_DESCRIPTION)) {
            chartView.setTitle(newValue);
            chartView.getChartWidget().draw(); // Title changing requires re-drawing the Chart
        } else if (propertyName.equals(PROPERTY_NAME_LEGEND_ENABLED)) {
          boolean enabled = Boolean.parseBoolean(newValue);
          chartView.setLegendEnabled(enabled);
          chartView.getChartWidget().draw();
        } else if (propertyName.equals(PROPERTY_NAME_GRID_ENABLED)) {
          if (chartView instanceof MockAxisChartView) {
            boolean enabled = Boolean.parseBoolean(newValue);
            ((MockAxisChartView)chartView).setGridEnabled(enabled);
            chartView.getChartWidget().draw();
          }
        } else if (propertyName.equals(PROPERTY_NAME_PIE_RADIUS)) {
            setPieRadiusProperty(newValue);
        }
    }

    /**
     * Creates Data components from the contents of the specified MockDataFile component.
     * The Data components are then attached as children to the Chart and the Source property
     * of each individual Data component is set accordingly.
     *
     * @param dataFileSource  MockDataFile component to instantiate components from
     */
    public void addDataFile(MockDataFile dataFileSource) {
        List<String> columnNames = dataFileSource.getColumnNames();

        for (String column : columnNames) {
            // Create a new MockCoordinateData component and attach it to the Chart
            // TODO: More data component support
            MockCoordinateData data = new MockCoordinateData(editor);
            addComponent(data);
            data.addToChart(MockChart.this);

            // Change the properties of the instantiated data component
            data.changeProperty("DataFileYColumn", column);
            data.changeProperty("Label", column);
            data.changeProperty("Source", dataFileSource.getName());
        }
    }

    /**
     * Creates a corresponding MockChartDataModel that
     * represents the current Chart type.
     * @return  new MockChartDataModel instance
     */
    public MockChartDataModel createDataModel() {
        return chartView.createDataModel();
    }

    /**
     * Refreshes the Chart view.
     */
    public void refreshChart() {
        chartView.getChartWidget().update();
    }

    /**
     * Returns the Mock Component of the Drag Source.
     *
     * @param source  DragSource instance
     * @return  MockComponent instance
     */
    protected MockComponent getComponentFromDragSource(DragSource source) {
        MockComponent component = null;
        if (source instanceof MockComponent) {
            component = (MockComponent) source;
        } else if (source instanceof SimplePaletteItem) {
            component = (MockComponent) source.getDragWidget();
        }

        return component;
    }

    @Override
    protected boolean acceptableSource(DragSource source) {
        MockComponent component = getComponentFromDragSource(source);

        return (component instanceof MockCoordinateData)
                || (isComponentAcceptableDataFileSource(component));
    }

    /**
     * Checks whether the component is an acceptable DataFile drag source for the Chart.
     * The criterion is that the Component must be of type DataFile and is
     * already instantiated in a container.
     * @param component  Component to check
     * @return  true if the component is a DataFile that is an acceptable source
     */
    private boolean isComponentAcceptableDataFileSource(MockComponent component) {
        return component instanceof MockDataFile
                && component.getContainer() != null; // DataFile must already be in it's own container
    }

    @Override
    protected boolean isPropertyVisible(String propertyName) {
        // Pie Radius property should be invisible by default, since
        // the default Chart Type is a Line Chart
        if (propertyName.equals(PROPERTY_NAME_PIE_RADIUS)) {
            return false;
        }

        return super.isPropertyVisible(propertyName);
    }
}
