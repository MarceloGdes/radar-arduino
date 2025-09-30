package br.unipar;

import processing.core.PApplet;
import processing.serial.Serial;

import static processing.core.PApplet.println;

public class Main {
    public static void main(String[] args) {
        println(Serial.list());
        PApplet.main("br.unipar.radar.RadarDisplay");
    }
}