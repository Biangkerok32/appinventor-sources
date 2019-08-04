package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that handles the GUI, initialization and managing of all
 * Data Series for the Pie Chart.
 *
 * Since MPAndroidChart (in v3.1.0) does not support concentric
 * rings, an implementation was done using multiple Pie Chart
 * views by stacking them on top of another to create concentric
 * Pie Charts. Due to this difference from other Chart types,
 * this View behaves differently since each individual Pie Chart
 * ring has to be managed manually.
 */
public class PieChartView extends ChartView<PieChart, PieData> {
  private RelativeLayout rootView; // Root view to store all the Pie Chart views
  private List<PieChart> pieCharts = new ArrayList<PieChart>(); // List to store all the Pie Chart views
  private Activity activity; // Activity of the Screen (needed to initialize Pie Chart views)

  private int pieHoleRadius = 0;

  // Due to MPAndroidChart's custom Legend setting taking an array as input,
  // the Legend Entries have to be reset on adding/removal, thus a List
  // is kept to keep track of the Legend Entries. This list keeps track
  // of Legend Entries across all Pie Chart rings.
  private List<LegendEntry> legendEntries = new ArrayList<LegendEntry>();

  /**
   * Creates a new Pie Chart view instance which manages
   * all the Pie Chart rings.
   * @param context  Activity context where the view should be initialized in
   */
  public PieChartView(Activity context) {
    // Instantiate the Root View layout and the Root Chart
    rootView = new RelativeLayout(context);
    chart = new PieChart(context); // the Chart instance represents the root Pie Chart

    // Store the activity variable locally
    this.activity = context;

    // Initialize default settings for the Root Chart
    initializeDefaultSettings();
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // The Legend must be drawn inside to prevent inner rings from
    // being misaligned on the main Pie Chart.
    chart.getLegend().setDrawInside(true);

    // Override the default entries of the Legend by
    // setting them to the local Legend Entries list
    chart.getLegend().setCustom(legendEntries);
  }

  @Override
  public View getView() {
    // Returns the underlying root RelativeLayout view
    // which stores all the Pie Chart rings
    return rootView;
  }

  @Override
  public ChartDataModel createChartModel() {
    // Create and add an inner Pie Chart ring
    PieChart pieChart = createPieChartRing();

    // Return a new Pie Chart Data model linking it to
    // the created Pie Chart ring
    return new PieChartDataModel(this, pieChart, new PieData());
  }

  /**
   * Creates, initializes & attaches a new Pie Chart ring to add to the
   * Pie Chart root view.
   *
   * To be called upon creating a new PieChartDataModel.
   * @return  created Pie Chart instance
   */
  private PieChart createPieChartRing() {
    PieChart pieChart;

    if (pieCharts.isEmpty()) { // No Pie Charts have been added yet (root Pie Chart)
      pieChart = chart; // Set Pie Chart to root Pie Chart
    } else { // Inner Pie Chart
      pieChart = new PieChart(activity); // Create a new Pie Chart
      pieChart.getDescription().setEnabled(false); // Hide description
      pieChart.getLegend().setEnabled(false); // Hide legend
    };

    // Set the corresponding properties of the Pie Chart view
    // to the newly created Pie Chart
    setPieChartProperties(pieChart);

    // Create RelativeLayout params with MATCH_PARENT height and width and
    // CENTER_IN_PARENT property set to true. A future method call will
    // adjust all the necessary widths & heights.
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
        (ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    pieChart.setLayoutParams(params);

    pieCharts.add(pieChart); // Add new Pie Chart (ring) to the Pie Charts List
    rootView.addView(pieChart); // Add new Pie Chart (ring) to the root View

    // Return the newly constructed Pie Chart
    return pieChart;
  }

  /**
   * Sets the mutually defined styling proeprties to the specified Pie
   * Chart. This method allows having consistency between all the
   * instantiated Pie Chart rings.
   * @param chart  Pie Chart to apply styling settings to
   */
  private void setPieChartProperties(PieChart chart) {
    chart.setDrawEntryLabels(false);
  }

  @Override
  public synchronized void Refresh() {
    // Refresh each Pie Chart (ring) individually
    for (final PieChart pieChart : pieCharts) {
      // Notify the Data component of data changes (needs to be called
      // when Datasets get changed directly)
      pieChart.getData().notifyDataChanged();

      // Notify the Chart of Data changes (needs to be called
      // when Data objects get changed directly)
      pieChart.notifyDataSetChanged();

      // Invalidate the Chart on the UI thread (via the Handler)
      // The invalidate method should only be invoked on the UI thread
      // to prevent exceptions.
      uiHandler.post(new Runnable() {
        @Override
        public void run() {
          pieChart.invalidate();
        }
      });
    }
  }

  /**
   * Resizes, rescales and sets the radius of all the inner
   * Pie Charts according to the total count of Pie Charts
   * to create a representative concentric ring Pie Chart.
   */
  public void resizePieRings() {
    // Store width and height of last Pie Chart (since getHeight() and
    // getWidth() will not return the needed result instantly)
    int lastWidth = 0;
    int lastHeight = 0;

    // Calculate the reduction factor to apply to both the radius and the scaling.
    // The primary reduction factor here is the count of Pie Chart rings. The
    // more rings, the smaller the factor (so the main factor itself is 1/#rings)
    // The constant 0.75f was carefully picked through trial and error. It could
    // be changed to something else, which would result in the inner-most rings
    // becoming smaller. A factor of pieHoleRadius/100f is added for the reason
    // that Pie Charts with a very small pie hole radius require a lesser reduction
    // factor to maintain a larger fill percentage.
    float reductionFactor = (0.75f + pieHoleRadius/100f) / pieCharts.size();

    // Calculate the current fill radius of the Chart (100% is the maximum,
    // so we subtract the pie hole radius from the 100% to get the part
    // that is filled)
    float radius = (100f - pieHoleRadius);

    // Calculate the new hole radius. The radius is first multiplied
    // by the reduction factor (we reduce the fill radius), and then
    // the hole radius is calculated by subtracting the reduced radius
    // from 100%./
    float newHoleRadius = 100f - radius * reductionFactor;

    for (int i = 0; i < pieCharts.size(); ++i) {
      PieChart pieChart = pieCharts.get(i);

      // Change the radius of the Pie Chart according
      // to the newly calculated hole radius
      boolean lastChart = (i == pieCharts.size() - 1);
      changePieChartRadius(pieChart, newHoleRadius, lastChart);

      // FP on i != 0 always false
      if (i != 0) { // Inner Chart
        // Calculate the scaling factor to use for the width and
        // height of the Chart. The hole radius essentially represnets
        // how much free space is available inside the Chart. The value
        // is then divided by 100 to get a fraction to use as the new size.
        float scalingFactor = (newHoleRadius)/100f;

        // Compute new width & height using the scaling factor
        lastWidth = (int)(lastWidth * scalingFactor);
        lastHeight = (int)(lastHeight * scalingFactor);

        // Change the size of the current Pie Chart
        changePieChartSize(pieChart, lastWidth, lastHeight);
      } else { // Root Chart
        // Set last height & width to use for the subsequent Charts.
        lastHeight = pieChart.getHeight();
        lastWidth = pieChart.getWidth();
      }

      // Since the Legend is drawn inside, an offset is needed
      // so that the rings of the Pie Chart do not overlap with the
      // Legend. The value was chosen as an optimal value (10 is too large,
      // 5 is too small, 7 is a somewhat in the middle option)
      // TODO: This could be improved in the future to (perhaps) dynamically
      // TODO: adjust the bottom offset.
      pieChart.setExtraBottomOffset(7);
    }
  }

  /**
   * Helper method to change the radius of the specified Pie Chart
   * to the specified new radius. The new radius also depends on
   * whether this is the last (inner-most) Chart or not.
   * @param pieChart  Pie Chart to change radius of
   * @param newHoleRadius  New radius to set to the Pie Chart
   * @param lastChart  Boolean to indicate whether the specified Chart is the last one
   */
  private void changePieChartRadius(PieChart pieChart, float newHoleRadius, boolean lastChart) {
    if (!lastChart) { // Outer rings
      // Set the radius to the specified new hole radius
      pieChart.setTransparentCircleRadius(newHoleRadius);
      pieChart.setHoleRadius(newHoleRadius);

      // Draw the hole in the Pie Chart to free space
      // for subsequent inner Pie Charts
      pieChart.setDrawHoleEnabled(true);
    } else { // Inner-most ring
      // Pie hole radius is 0; Disable drawing the hole in the Pie Chart
      if (pieHoleRadius == 0) {
        pieChart.setDrawHoleEnabled(false);
      } else {
        // TODO: Improvements can be mostly made on this part

        // Calculate the difference between the new pie hole radius and the
        // current pie hole radius. OBSERVATION: for r [0, 100], this difference
        // will always be positive.
        float delta = newHoleRadius - pieHoleRadius;

        // Use 1f + delta% as a factor for the current pieHoleRadius.
        // Through trial and error, this value worked better than
        // using the pieHoleRadius or the newRadius directly on
        // the inner Chart. Using the pieHoleRadius produces
        // an inner-most Chart that is far too large than the outer
        // rings, while using the newHoleRadius produces a radius that
        // is too small for certain cases (holeRadius < 25%). Hence,
        // some percentage is used here instead. (the higher the
        // difference between the new and old radius, the bigger
        // the new radius)
        float setRadius = pieHoleRadius * (1f + (delta) / 100f);


        // Set the hole radius of the Pie chart to the calculated radius
        pieChart.setTransparentCircleRadius(setRadius);
        pieChart.setHoleRadius(setRadius);
      }
    }
  }

  /**
   * Changes the width & height of the specified Pie Chart.
   * @param pieChart  Pie Chart to change width and height of
   * @param width  New width
   * @param height  New height
   */
  private void changePieChartSize(PieChart pieChart, int width, int height) {
    // Get the RelativeLayout parameters of the Pie Chart
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)(pieChart.getLayoutParams());

    // Set width & height of the Pie Chart, and update the Layout parameters
    // of the Chart
    params.width = width;
    params.height = height;
    pieChart.setLayoutParams(params);
  }

  /**
   * Adds a new Legend Entry to the Legend of the Pie Chart view.
   * @param entry  Legend Entry to add
   */
  public void addLegendEntry(LegendEntry entry) {
    legendEntries.add(entry); // Add the Legend Entry to local reference List
    chart.getLegend().setCustom(legendEntries); // Re-set the Legend's entries to the List to update
  }

  /**
   * Removes the specified Legend Entry from the Legend of the Pie Chart view
   * @param entry  Legend Entry to remove
   */
  public void removeLegendEntry(LegendEntry entry) {
    legendEntries.remove(entry); // Remove the Legend Entry from local reference list
    chart.getLegend().setCustom(legendEntries); // Re-set the Legend's entries to the List to update
  }

  /**
   * Sets the radius of the Pie Chart (in percentage)
   *
   * E.g. 100% - full Pie Chart, 50% - donut
   *
   * @param percent  percentage of the radius to fill.
   */
  public void setPieRadius(int percent) {
    // Disallow setting percentage that does not fall
    // in the range [0, 100]
    if (percent > 100) {
      percent = 100;
    } else if (percent < 0) {
      percent = 0;
    }

    // Calculate the radius of the hole of the Pie Charts
    this.pieHoleRadius = 100 - percent;

    // Resize Pie Chart rings accordingly
    resizePieRings();
  }
}
