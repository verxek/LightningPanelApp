#include "Adafruit_NeoPixel.h" // подключаем библиотеку

#define MAX_PIXELS 256
#define BUFFER_SIZE 256

//---------------------------------

// указываем количество пикселей в матрице и пин подключения
Adafruit_NeoPixel strip (MAX_PIXELS, 13, NEO_GRB + NEO_KHZ800);

int32_t g_buffer[BUFFER_SIZE];
int count_pixels = 0;
uint32_t color = 0;
int read = 0;

void setup() {
   strip.begin(); 
   strip.setBrightness(255); 
   strip.show();                     
   Serial.begin(9600);
   strip.setPixelColor(2, strip.Color(255, 255, 255));
   strip.show();
}


void loop() {

    if(Serial.available() > 0) {
    
    // digitalWrite(13, HIGH);

    Serial.readBytesUntil('\n', (uint8_t*)g_buffer, 4*BUFFER_SIZE);
    for(int i = 0; i < BUFFER_SIZE; ++i) {
      Serial.print(i);
      Serial.print(" ");
      Serial.print(g_buffer[i]);
      Serial.print(" ");
      Serial.print((g_buffer[i] >> 16) & 0xFF);
      Serial.print("_");
      Serial.print((g_buffer[i] >> 8) & 0xFF);
      Serial.print("_");
      Serial.print(g_buffer[i] & 0xFF);
      Serial.println(" ");
      strip.setPixelColor(i, strip.Color((g_buffer[i] >> 16) & 0xFF, (g_buffer[i] >> 8) & 0xFF, g_buffer[i] & 0xFF));
      strip.show();
    }
    // strip.show();
  }
   
  strip.setPixelColor(0, strip.Color(0, 255, 0));
   strip.show();
  // if(Serial.available() > 0)
  // {
  //   Serial.readBytesUntil('\n', (uint8_t*)(&color), sizeof(color));
  //   strip.setPixelColor(count_pixels, strip.Color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
  //   count_pixels++;
  // } 

  // Serial.print(sizeof(color));
  // Serial.print(" ");
  // Serial.print(read);
  // Serial.print(" ");
  // Serial.print(count_pixels);
  // Serial.print(" ");
  // Serial.print((color >> 16) & 0xFF);
  // Serial.print("_");
  // Serial.print((color >> 8) & 0xFF);
  // Serial.print("_");
  // Serial.print(color & 0xFF);
  // Serial.println(" ");

  // if (count_pixels >= MAX_PIXELS){
  //     count_pixels = 0;
  //     strip.show();
  //   }
 
}