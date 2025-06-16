#include <SoftwareSerial.h>

#define echoPin 6
#define trigPin 7
long duration; 
int distance; 
bool forwardActive = false;  // Track if moving forward

// Motor control functions
void movefwd() {
  analogWrite(10, 200); // right motor pwm
  analogWrite(11, 200); //left motor pwm
  digitalWrite(2, 0);
  digitalWrite(3, 1); 
  digitalWrite(5, 0);
  digitalWrite(4, 1); 
}

void moveback() {
  analogWrite(10, 200);
  analogWrite(11, 200);
  digitalWrite(2, 1);
  digitalWrite(3, 0); 
  digitalWrite(5, 1);
  digitalWrite(4, 0); 
}

void stop() {
  analogWrite(10, 0);
  analogWrite(11, 0);
  digitalWrite(5, 0);
  digitalWrite(4, 0); 
  digitalWrite(2, 0);
  digitalWrite(3, 0); 
}

void left_turn() {
  analogWrite(10, 200);
  analogWrite(11, 0);
  digitalWrite(2, 0);
  digitalWrite(3, 1); 
  digitalWrite(5, 0);
  digitalWrite(4, 1); 
}

void right_turn() {
  analogWrite(10, 0);
  analogWrite(11, 200);
  digitalWrite(2, 0);
  digitalWrite(3, 1); 
  digitalWrite(5, 0);
  digitalWrite(4, 1); 
}

// Get distance from ultrasonic sensor
int getDistance() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  duration = pulseIn(echoPin, HIGH);
  distance = duration * 0.034 / 2;
  return distance;
}

// Setup function
void setup() {
  pinMode(11, OUTPUT);  // Left motor enable PWM  
  pinMode(10, OUTPUT);  // Right motor enable PWM
  pinMode(3, OUTPUT);   // INA1
  pinMode(4, OUTPUT);   // INA2
  pinMode(5, OUTPUT);   // INA3
  pinMode(2, OUTPUT);   // INA4
  pinMode(trigPin, OUTPUT);  // Trig pin
  pinMode(echoPin, INPUT);   // Echo pin

  Serial.begin(9600);
  stop(); // Stop at start
}

// Main loop
void loop() {
  // Check serial input
  if (Serial.available() > 0) {
    char command = Serial.read();
    Serial.println(command);

    switch (command) {
      case 'F':
        forwardActive = true;
        Serial.println("Forward command received");
        break;

      case 'B':
        moveback();
        forwardActive = false;
        Serial.println("Moving Backward");
        break;

      case 'L':
        left_turn();
        forwardActive = false;
        Serial.println("Turning Left");
        break;

      case 'R':
        right_turn(); 
        forwardActive = false;
        Serial.println("Turning Right");
        break;

      case 'S':
        stop();
        forwardActive = false;
        Serial.println("Stopped");
        break;

      default:
        stop(); 
        forwardActive = false;
        Serial.println("Unknown command. Motors stopped.");
        break;
    }
  }

  // Always monitor distance
  int dist = getDistance();

  // Handle forward mode with distance safety
  if (forwardActive) {
    if (dist > 10) {
      movefwd();
    } else {
      stop();
      forwardActive = false;
      Serial.println("Obstacle detected! Auto stop.");
    }
  }

  delay(100); // Small delay
}
