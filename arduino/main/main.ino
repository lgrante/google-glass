#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <SoftwareSerial.h>

SoftwareSerial hc06(2, 3);

Adafruit_SSD1306 display(-1);

String request;

void setup()   
{
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);  
  display.clearDisplay();

  Serial.begin(9600);
  hc06.begin(9600);
}

void loop() {
  display_notification("Welcome on MyGlass");
  
  if (hc06.available()) {
    request = hc06.readString();
  }

  if (request.length() > 0) {
    Serial.println("Received that message");
    display_notification(request);
    delay(5000);
    request = "";
  }
}

void display_notification(String notification) {
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0,28);
  display.println(notification);
  display.display();
}
