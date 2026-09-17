package com.robotjatek.wplauncher.Services.ScreenNavigator;

import com.robotjatek.wplauncher.Components.UIElement;
import com.robotjatek.wplauncher.TileGrid.Position;

public interface IOverlay {
    /**
     * Sets an adorner at the given position. If not present, the adorner will be added to the overlay.
     */
    void setAdornerAt(UIElement element, Position<Float> position);

    /**
     * Removes an adorner. If not present, nothing happens.
     */
    void removeAdorner(UIElement element);
}
