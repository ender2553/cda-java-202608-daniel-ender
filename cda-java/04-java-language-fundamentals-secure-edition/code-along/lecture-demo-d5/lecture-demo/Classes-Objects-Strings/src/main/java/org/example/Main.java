package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {

        System.out.println("====== CLASSES OBJECTS STRINGS ========");

        System.out.println("------ Asset Manager App  -------");

        //String asset1 = "Printer";

        Asset asset1 = new Asset("Printer1234");
        Asset asset2 = new Asset("Printer1234");

        Asset asset3 = new Asset();

        //later in code request asset name
        asset3.setName("Web Server");

        Object obj;

        System.out.println("asset1 == asset2: " + (asset1 == asset2) + "\n");
        System.out.println("asset1 == asset2: " + asset1.equals(asset2) + "\n");


        System.out.println(asset1.getName() + " Current Owner: " + asset1.getOwner());

        // enter your name
        asset1.setOwner("Neo");




        System.out.println(asset1.getName() + " Current Owner: " + asset1.getOwner());

        //Finish initial purchase, then transfer to dept owner
        asset1.setOwner("Trinity");

        System.out.println(asset1.getName() + " Current Owner: " + asset1.getOwner());





    }
}
