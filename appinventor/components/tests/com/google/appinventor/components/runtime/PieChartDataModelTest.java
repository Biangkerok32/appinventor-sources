package com.google.appinventor.components.runtime;

import android.graphics.Color;
import android.widget.RelativeLayout;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieEntry;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class PieChartDataModelTest extends ChartDataModel2DTest<PieChartDataModel, PieData> {
  private PieChartView chartView;
  private Legend legend;

  /**
   * Test to ensure that importing from an x Column which is
   * empty and a Y column which has values results in the
   * x values to resolve to the default option (1 for first entry,
   * 2 for second, ...)
   */
  @Test
  public void testImportFromCSVEmptyColumn() {
    YailList xColumn = createTuple();
    YailList yColumn = createTuple("Y", 3f, 5f, -3f, 7f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(createEntry("1", 3f));
      add(createEntry("2", 5f));
      add(createEntry("3", -3f));
      add(createEntry("4", 7f));
    }};

    importFromCSVHelper(expectedEntries, xColumn, yColumn);
  }


  /**
   * Test to ensure that importing from a pair containing
   * valid values with an x value being a String label
   * adds the entry properly.
   */
  @Test
  public void testAddEntryFromTupleXLabel() {
    final String xValue = "Entry";
    final float yValue = 25f;

    YailList tuple = createTuple(xValue, yValue);
    model.addEntryFromTuple(tuple);

    Entry entry = model.getDataset().getEntryForIndex(0);
    Entry expectedEntry = createEntry(xValue, yValue);

    assertEquals(1, model.getDataset().getEntryCount());
    assertEntriesEqual(expectedEntry, entry);
  }

  /**
   * Test to ensure that comparing two entries which
   * have the same x and y values returns true via
   * the areEntriesEqual method.
   */
  @Test
  public void testEntriesEqual() {
    Entry entry1 = createEntry("Entry", 12f);
    Entry entry2 = createEntry("Entry", 12f);

    assertTrue(model.areEntriesEqual(entry1, entry2));
  }

  /**
   * Test to ensure that comparing two entries which
   * have the same x but different y values returns false via
   * the areEntriesEqual method.
   */
  @Test
  public void testEntriesNotEqualY() {
    Entry entry1 = createEntry("Entry", 12f);
    Entry entry2 = createEntry("Entry", 15f);

    assertFalse(model.areEntriesEqual(entry1, entry2));
  }

  /**
   * Test to ensure that comparing two entries which
   * have the same y but different x values returns false via
   * the areEntriesEqual method.
   */
  @Test
  public void testEntriesNotEqualX() {
    Entry entry1 = createEntry("Entry", 10f);
    Entry entry2 = createEntry("Entry 2", 10f);

    assertFalse(model.areEntriesEqual(entry1, entry2));
  }

  /**
   * Test to ensure that setting the Colors property
   * with a List containing a single entry properly
   * sets the colors of the Data Series and sets
   * the color to every value.
   */
  @Test
  public void testSetColorsSingleColor() {
    final int color = Color.BLUE;

    List<Integer> colorList = new ArrayList<Integer>() {{
      add(color);
    }};

    List<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple("Entry", 3f));
      add(createTuple("Entry 2", 4f));
      add(createTuple("Entry 3", 2f));
      add(createTuple("Entry 4", 9f));
    }};

   int[] expectedColors = {
       color,
       color,
       color,
       color
   };

    setColorsHelper(colorList, tuples, expectedColors);
  }

  /**
   * Test to ensure that setting the Colors property
   * with a List containing multiple entries and a
   * color for each entry properly sets the colors of
   * the Data Series and the color for each individual entry.
   */
  @Test
  public void testSetColorsMultipleColors() {
    List<Integer> colorList = new ArrayList<Integer>() {{
      add(Color.RED);
      add(Color.GREEN);
      add(Color.BLUE);
      add(Color.YELLOW);
      add(Color.CYAN);
    }};

    List<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple("Entry", 32f));
      add(createTuple("Entry 2", 40f));
      add(createTuple("Entry 3", 25f));
      add(createTuple("Entry 4", 15f));
      add(createTuple("Entry 5", 10f));
    }};

    int[] expectedColors = {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW,
        Color.CYAN
    };

    setColorsHelper(colorList, tuples, expectedColors);
  }

  /**
   * Test to ensure that setting the Colors property
   * with a List containing multiple entries and less
   * colors than entries properly sets the colors of
   * the Data Series and the color for each individual entry
   * is alternated properly between the set colors.
   */
  @Test
  public void testSetColorsMultipleEntriesRepeatColors() {
    List<Integer> colorList = new ArrayList<Integer>() {{
      add(Color.RED);
      add(Color.GREEN);
      add(Color.BLUE);
    }};

    List<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple("Entry", 32f));
      add(createTuple("Entry 2", 40f));
      add(createTuple("Entry 3", 25f));
      add(createTuple("Entry 4", 15f));
      add(createTuple("Entry 5", 10f));
      add(createTuple("Entry 6", 7f));
      add(createTuple("Entry 7", 3f));
    }};

    int[] expectedColors = {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.RED
    };

    setColorsHelper(colorList, tuples, expectedColors);
  }

  /**
   * Test to ensure that setting the Colors property
   * with a List containing invalid entries skips
   * the invalid entries, and sets the colors from the
   * valid entries.
   */
  @Test
  public void testSetColorsInvalidEntries() {
    List<Object> colorList = new ArrayList<Object>() {{
      add(Color.RED);
      add("test");
      add("random-string");
      add(Color.BLUE);
    }};

    List<Integer> colorListExpected = new ArrayList<Integer>() {{
      add(Color.RED);
      add(Color.BLUE);
    }};

    List<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple("Entry", 32f));
      add(createTuple("Entry 2", 40f));
      add(createTuple("Entry 3", 25f));
    }};

    int[] expectedColors = {
        Color.RED,
        Color.BLUE,
        Color.RED
    };

    YailList colors = YailList.makeList(colorList);
    model.setColors(colors);
    model.importFromList(tuples);

    assertEquals(colorListExpected, model.getDataset().getColors());
    assertEquals(expectedColors.length, legend.getEntries().length);

    for (int i = 0; i < expectedColors.length; ++i) {
      LegendEntry legendEntry = legend.getEntries()[i];

      assertEquals(expectedColors[i], legendEntry.formColor);
    }
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * X Value criterion and a matching x value which is a String returns true.
   */
  @Test
  public void testCriterionSatisfiedXStringMatch() {
    Entry entry = createEntry("Entry", 4f);
    final ChartDataModel.EntryCriterion criterion = ChartDataModel.EntryCriterion.XValue;
    final String value = "Entry";

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value);
    assertTrue(result);
  }

  /**
   * Test case to ensure that adding a single entry
   * adds the appropriate Legend Entry with the x value
   * as the label and the value color as the color
   */
  @Test
  public void testCheckLegendSingleEntry() {
    final int color = Color.RED;
    model.setColor(color);

    YailList tuple = createTuple("Entry", 5f);
    model.addEntryFromTuple(tuple);

    LegendEntry[] expectedEntries = {
      createLegendEntry("Entry", Color.RED)
    };

    checkLegendHelper(expectedEntries);
  }

  /**
   * Test case to ensure that adding multiple entries
   * to the Data Series adds the appropriate Legend
   * Entries representing the values (x values as labels
   * and value colors as Legend colors)
   */
  @Test
  public void testCheckLegendMultipleEntries() {
    final int color = Color.BLUE;
    model.setColor(color);

    List<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple("Entry 1", 5f));
      add(createTuple("Entry 2", 3f));
      add(createTuple("test", 7f));
      add(createTuple("test 2", 1f));
    }};

    model.importFromList(tuples);

    LegendEntry[] expectedEntries = {
        createLegendEntry("Entry 1", color),
        createLegendEntry("Entry 2", color),
        createLegendEntry("test", color),
        createLegendEntry("test 2", color)
    };

    checkLegendHelper(expectedEntries);
  }

  /**
   * Test case to ensure that adding entries from
   * different Data Models adds the appropriate entries
   * corresponding to each entry of the separate data
   * series.
   */
  @Test
  public void testCheckLegendDifferentDataModelEntries() {
    final int color1 = Color.RED;
    final int color2 = Color.GREEN;
    final int color3 = Color.BLUE;

    PieChartDataModel model2 = (PieChartDataModel)chartView.createChartModel();
    PieChartDataModel model3 = (PieChartDataModel)chartView.createChartModel();

    model.setColor(color1);
    model2.setColor(color2);
    model3.setColor(color3);

    model.addEntryFromTuple(createTuple("Entry 1", 5f));
    model2.addEntryFromTuple(createTuple("Model 2 Entry", 9f));
    model3.addEntryFromTuple(createTuple("Model 3 Entry", 1f));
    model.addEntryFromTuple(createTuple("Entry 2", 10f));

    LegendEntry[] expectedEntries = {
        createLegendEntry("Entry 1", color1),
        createLegendEntry("Model 2 Entry", color2),
        createLegendEntry("Model 3 Entry", color3),
        createLegendEntry("Entry 2", color1)
    };

    checkLegendHelper(expectedEntries);
  }

  private void checkLegendHelper(LegendEntry[] expectedEntries) {
    LegendEntry[] legendEntries = legend.getEntries();

    assertEquals(expectedEntries.length, legendEntries.length);

    for (int i = 0; i < expectedEntries.length; ++i) {
      LegendEntry expected = expectedEntries[i];
      LegendEntry actual = legendEntries[i];

      assertEquals(expected.label, actual.label);
      assertEquals(expected.formColor, actual.formColor);
    }
  }

  private void setColorsHelper(List colorList, List<YailList> tuples, int[] expectedColors) {
    YailList colors = YailList.makeList(colorList);
    model.setColors(colors);
    model.importFromList(tuples);

    assertEquals(colorList, model.getDataset().getColors());
    assertEquals(expectedColors.length, legend.getEntries().length);

    for (int i = 0; i < expectedColors.length; ++i) {
      LegendEntry legendEntry = legend.getEntries()[i];

      assertEquals(expectedColors[i], legendEntry.formColor);
    }
  }

  @Override
  public void setup() {
    chartView = new PieChartView(getForm());
    model = (PieChartDataModel)chartView.createChartModel();
    data = (PieData) model.getData();
    legend = ((PieChart)((RelativeLayout)chartView.getView()).getChildAt(0)).getLegend();
  }

  @Override
  protected void assertEntriesEqual(Entry e1, Entry e2) {
    assertEquals(e1.getClass(), e2.getClass());
    assertEquals(((PieEntry)e1).getLabel(), ((PieEntry)e2).getLabel());
    assertEquals(e1.getY(), e2.getY());
  }

  @Override
  protected YailList createTuple(Object... entries) {
    if (entries.length != 0) {
      entries[0] = entries[0].toString();
    }

    return super.createTuple(entries);
  }

  @Override
  protected Entry createEntry(Object... entries) {
    String xValue = entries[0].toString();
    float yValue = (float) entries[1];

    return new PieEntry(yValue, xValue);
  }

  private LegendEntry createLegendEntry(String x, int color) {
    LegendEntry legendEntry = new LegendEntry();
    legendEntry.label = x;
    legendEntry.formColor = color;
    return legendEntry;
  }
}
