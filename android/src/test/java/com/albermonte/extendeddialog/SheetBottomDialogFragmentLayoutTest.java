package com.albermonte.extendeddialog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SheetBottomDialogFragmentLayoutTest {

    @Test
    public void clampInitialSheetHeight_enforcesMinimumFiftyPercent() {
        int screenHeight = 1000;
        int contentHeight = 300;

        int result = SheetBottomDialogFragment.clampInitialSheetHeight(screenHeight, contentHeight);

        assertEquals(500, result);
    }

    @Test
    public void clampInitialSheetHeight_usesContentHeightWithinRange() {
        int screenHeight = 1000;
        int contentHeight = 650;

        int result = SheetBottomDialogFragment.clampInitialSheetHeight(screenHeight, contentHeight);

        assertEquals(650, result);
    }

    @Test
    public void clampInitialSheetHeight_capsAtEightyPercent() {
        int screenHeight = 1000;
        int contentHeight = 900;

        int result = SheetBottomDialogFragment.clampInitialSheetHeight(screenHeight, contentHeight);

        assertEquals(800, result);
    }

    @Test
    public void calculateHalfExpandedRatio_clampsToFiftyAndEightyPercent() {
        float belowRange = SheetBottomDialogFragment.calculateHalfExpandedRatio(1000, 400);
        float insideRange = SheetBottomDialogFragment.calculateHalfExpandedRatio(1000, 650);
        float aboveRange = SheetBottomDialogFragment.calculateHalfExpandedRatio(1000, 900);

        assertEquals(0.5f, belowRange, 0f);
        assertEquals(0.65f, insideRange, 0f);
        assertEquals(0.8f, aboveRange, 0f);
    }
}
