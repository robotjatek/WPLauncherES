package com.robotjatek.wplauncher.Services.WeatherService;

public record WeatherData(Integer temperature, Integer weatherCode, TemperatureUnit unit, boolean isDay) {
    public String getWeatherDescription() {
        // From: https://open-meteo.com/en/docs#weather_variable_documentation
        return switch (weatherCode) {
            case 0 -> "Clear Sky";
            case 1 -> "Mainly Clear";
            case 2 -> "Partly Cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing Drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing Rain";
            case 71, 73, 75 -> "Snow";
            case 77 -> "Snow Grains";
            case 80, 81, 82 -> "Rain Showers";
            case 85, 86 -> "Snow Showers";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "";
        };
    }
}
