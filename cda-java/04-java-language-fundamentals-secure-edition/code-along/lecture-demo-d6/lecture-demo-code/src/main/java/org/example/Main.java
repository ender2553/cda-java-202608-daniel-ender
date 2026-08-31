package org.example;

import java.security.interfaces.RSAMultiPrimePrivateCrtKey;
import java.util.Objects;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        //RECAP ON METHODS

        int speed = 50;
        Car car = new Car("Toyota", "Blue");

        changeSpeed(speed);
        changeCarColor(car);



        System.out.println("Speed outside of method: " + speed);
        System.out.println("Car outside of method: " + car);



    }

    static void changeSpeed(int speed){
        speed = 100;
        System.out.println("Speed inside of method: " + speed);
    }

    static void changeCarColor(Car car){
        car.color = "Red"; //do not allow this - use getter with private fields
        System.out.println("Car inside of method: " + car);

    }


    // CLASSROOM EXAMPLE ONLY - NOT TYPICAL (PREFER SEPARATE CLASS FILE)
    public static class Car {

        String make;  //demo purposes - make private normally
        String color; //demo purposes - make private normally

        Car(String make, String color){
            this.make = make;
            this.color = color;
        }

        public String getMake(){
            return this.make;
        }

        public String getColor(){
            return color;
        }

        @Override
        public String toString(){
            String carDisplay = make + " (" + color + ")";
            return carDisplay;
        }


        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Car car = (Car) o;
            return Objects.equals(make, car.make) && Objects.equals(color, car.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(make, color);
        }
    }

}
