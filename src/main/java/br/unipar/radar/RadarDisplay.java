package br.unipar.radar;

import processing.core.PApplet;
import processing.serial.*;

public class RadarDisplay extends PApplet {

    // --- Variáveis globais ---
    Serial serialPort;
    String inString = "COM5";

    int screenWidth = 800;
    int screenHeight = 450;

    float radarRadius = 350;
    float radarCenterX = screenWidth / 2f;
    float radarCenterY = screenHeight - 100;

    int currentAngle = 0;
    int currentDistance = 0;

    // Histórico de pontos detectados
    class PointHistory {
        float x, y;
        int age;
        PointHistory(float x, float y, int age) {
            this.x = x;
            this.y = y;
            this.age = age;
        }
    }
    java.util.List<PointHistory> pointHistory = new java.util.ArrayList<>();

    public static void main(String[] args) {
        PApplet.main("RadarDisplay");
    }

    public void settings() {
        size(screenWidth, screenHeight);
    }

    public void setup() {
        smooth();
        textFont(createFont("Monospaced", 20));

        println("Portas seriais disponíveis:");
        println(Serial.list());

        try {
            String portName = "COM5"; // ⚠️ ajuste conforme sua porta
            println("Conectando em: " + portName);
            serialPort = new Serial(this, portName, 9600);
            serialPort.clear();
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }
    }

    public void draw() {
        background(0, 20, 0);
        drawRadarGrid();
        drawTextLabels();
        drawSweepLine(currentAngle);
        drawDetectedPoints();
        updateAndDrawHistory();
    }

    void drawRadarGrid() {
        stroke(0, 150, 0);
        noFill();
        strokeWeight(2);

        // Semicírculos
        for (int i = 1; i < 3; i++) {
            float radius = i * (radarRadius / 2.0f);
            arc(radarCenterX, radarCenterY, radius * 2, radius * 2, PI, TWO_PI);
        }

        // Linha de base
        line(radarCenterX - radarRadius, radarCenterY, radarCenterX + radarRadius, radarCenterY);

        // Linhas radiais
        for (int i = 0; i < 5; i++) {
            float angle = radians((i * 45) - 180);
            float x2 = radarCenterX + radarRadius * cos(angle);
            float y2 = radarCenterY + radarRadius * sin(angle);
            line(radarCenterX, radarCenterY, x2, y2);
        }
    }

    void drawTextLabels() {
        fill(0, 200, 0);
        noStroke();

        for (int i = 1; i < 3; i++) {
            float radiusText = i * (radarRadius / 2.0f);
            text((i * 10) + " cm", radarCenterX + radiusText + 5, radarCenterY - 5);
        }

        text("Angle: " + currentAngle + "°", 20, 40);
        text("Distance: " + currentDistance + " cm", 20, 70);
        text("Radar Display", width / 2f - 80, 40);
    }

    void drawSweepLine(int angle) {
        stroke(0, 255, 0, 150);
        strokeWeight(3);
        float radAngle = radians(angle - 180);
        float endX = radarCenterX + radarRadius * cos(radAngle);
        float endY = radarCenterY + radarRadius * sin(radAngle);
        line(radarCenterX, radarCenterY, endX, endY);
    }

    void drawDetectedPoints() {
        float maxDist = 20.0f;
        if (currentDistance > 0 && currentDistance < maxDist) {
            stroke(255, 0, 0);
            strokeWeight(5);
            float radAngle = radians(currentAngle - 180);
            float mappedDist = map(currentDistance, 0, maxDist, 0, radarRadius);
            float pointX = radarCenterX + mappedDist * cos(radAngle);
            float pointY = radarCenterY + mappedDist * sin(radAngle);
            point(pointX, pointY);
            pointHistory.add(new PointHistory(pointX, pointY, 255));
        }
    }

    void updateAndDrawHistory() {
        java.util.List<PointHistory> newHistory = new java.util.ArrayList<>();
        for (PointHistory p : pointHistory) {
            stroke(0, p.age, 0);
            strokeWeight(4);
            point(p.x, p.y);
            p.age -= 2;
            if (p.age > 0) {
                newHistory.add(p);
            }
        }
        pointHistory = newHistory;
    }

    public void serialEvent(Serial port) {
        while (port.available() > 0) {
            char inChar = port.readChar();
            if (inChar == '.') {
                String[] values = inString.split(",");
                if (values.length == 2) {
                    try {
                        currentAngle = Integer.parseInt(values[0]);
                        currentDistance = Integer.parseInt(values[1]);
                    } catch (NumberFormatException ignored) {}
                }
                inString = "";
            } else if (inChar != '\n' && inChar != '\r') {
                inString += inChar;
            }
        }
    }
}


