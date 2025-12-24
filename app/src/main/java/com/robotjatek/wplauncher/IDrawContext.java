package com.robotjatek.wplauncher;

public interface IDrawContext<T> {
    QuadRenderer getRenderer();

    /**
     * Calculates screen space X position of a child
     * @param element The UI element to measure
     * @return Screen space X positon
     */
    float xOf(T element);

    /**
     * Calculates screen space Y position of a child
     * @param element The UI element to measure
     * @return Screen space Y positon
     */
    float yOf(T element);

    /**
     * Calculates the width of a UI element in screen dimensions
     * @param element The UI element to measure
     * @return Screen space width of the UI element
     */
    float widthOf(T element);

    /**
     * Calculates the height of a UI element in screen dimensions
     * @param element The UI element to measure
     * @return Screen space height of the UI element
     */
    float heightOf(T element);
}
