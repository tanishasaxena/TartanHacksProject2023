import java.util.*;
import java.io.*;

class Interactables implements Comparable<Interactables>{
    // Look / inspect / info / examine / Open
    // Use / interact / open
    // Grab ---- "You added <name> to your inventory"

    // Other Variables
    private boolean inventoryAddable;

    // Mode 0
    private String nameString;
    private String lookString;
    private String useString;

    // Mode 1
    // look:
    private int numLooks; // Number of times Look has been performed
    private String[] itemStack; // Whats in the trashcan
    private String endString; // If the trashcan is empty
    
    public Interactables(String name, String look, String use, boolean inventoryAddable){
        this.nameString = name;
        this.lookString = look;
        this.useString = use;
        this.inventoryAddable = inventoryAddable;
    }

    public void setLook1(String[] itemStack, String endString){
        this.numLooks = 0;
        this.itemStack = itemStack;
        this.endString = endString;
    }
    
    public String getName(){
        return this.nameString;
    }

    public boolean isInventoryAddable(){
        return inventoryAddable;
    }

    public String look(int mode){
        //println(mode);
        switch (mode) {
            case 0: return this.lookString;
            case 1: if(numLooks >= itemStack.length) return endString;
                    else{
                        return itemStack[numLooks++];
                    }
        }
        return null;
    }

    public String use(int mode){
        switch (mode) {
            case 0: return this.useString;
            case 1: return this.useString;
        }
        return null;
    }

    public String grab(int mode){
        switch (mode) {
            case 0: if(inventoryAddable) return "You added " + this.nameString + " to your inventory";
                    else return "You may not add " + this.nameString + " to your inventory"; 
        }
        return null;
    }

    public String runInstruction(String action, int mode){
        if(action.equals("look")) return this.look(mode);
        else if (action.equals("use")) return this.use(mode);
        else return this.grab(mode);
    }

    public int compareTo(Interactables other){
        return this.getName().compareTo(other.getName());
    }
}

class Room {
    private Interactables[] interactables;
    private String description; 
    private HashMap <String, Room> jumpList; // move ___, go ____, walk ____, run _____
    private HashMap <String, Integer> specialInstructions; // Action + Interactable --> mode number
    
    public Room (String description, Interactables[] interactables, HashMap <String, Room> jumpList, HashMap <String, Integer> specialInstructions){
        this.interactables = interactables;
        this.description = description;
        this.jumpList = jumpList;
        this.specialInstructions = specialInstructions;
    }
  
    public void setJumpList(HashMap <String, Room> jumpList){
        this.jumpList = jumpList;
    }
    //Returns description
    public String getDescription(){
        return this.description;
    }

    //Returns which room to jump to if input triggers a jump to another room,  otherwise return NULL;
    public Room checkJumps(String input){
        if(jumpList.containsKey(input)) return jumpList.get(input);
        else return null;
    }
    
    //Input needs to be the Action + Interactable concatenated
    //Returns 0 if it is not a special instruction, and if it is, the special instruction mode will be returned. 
    public int getInstructionMode(String input){
        if(specialInstructions.containsKey(input)) return (int) specialInstructions.get(input);
        else return 0;
    }

    public boolean isInteractable(String interactable){
        boolean res = false;
        for(int i = 0; i < interactables.length; i++){
            if(interactables[i].getName().equals(interactable)) res = true;
        }
        return res;
    }

    public Interactables getInteractable(String interactable){
        for(Interactables i : interactables){
            if(i.getName().equals(interactable)) return i;
        }
        return null;
    }

    //Action names are already preproccessed to look/use/grab
    //Non actions are not considered
    public String runInstruction(String action, String interactable, int mode){
        Interactables current = null;
        for(int i = 0; i < interactables.length; i++){
            //println(interactables[i].getName());
            if(interactables[i].getName().equals(interactable)) current = interactables[i];
            //println(current == null);
        }
        //print(current.look(mode) + " " + current.use(mode) + " " + current.grab(mode));
        if(action.equals("look")) return current.look(mode);
        else if (action.equals("use")) return current.use(mode);
        else return current.grab(mode);
    }
}

Room currentRoom;
ArrayList<Interactables> inventory;
boolean onKeyPad;

String getResponse(String input){
    //Clean Input
    input = input.toLowerCase();
    
    if(onKeyPad){
        if(input.equals("15101")){
            currentRoom = floor7B;
            onKeyPad = false;
            return currentRoom.getDescription();
        } else {
            if(Math.random() < 0.25){
              return "Wrong Passcode! Remember what you learned in 15101?";
            } else {
              return "Wrong Passcode! Remember the CS faculty training course...?";
            }
        }
    }
    
    if(input.equals("inventory")){
        if(inventory.size() == 0) return "Your inventory is empty.";
        String inventoryString = inventory.get(0).getName();
        
        for(int i = 1; i < inventory.size(); i++){
            inventoryString += ", " + inventory.get(i).getName();
        }
        return inventoryString;
    }
    
    //Check for Jumps
    Room jumpTo = currentRoom.checkJumps(input);
    if(jumpTo != null){
        //println("jumped");
        //println(jumpTo.getDescription());
        currentRoom = jumpTo;
        return currentRoom.getDescription();
    } 

    //println("No Jump");
    //Parse string for action and interactable
    
    Set<String> lookSet = new HashSet<>(Arrays.asList(new String[]{"look", "inspect", "info", "examine"}));
    Set<String> useSet = new HashSet<>(Arrays.asList(new String[]{"use", "interact", "open"}));
    Set<String> grabSet = new HashSet<>(Arrays.asList(new String[]{"grab", "take"}));

    String action = "fail";
    String firstWord = input.split(" ")[0]; 

    if(lookSet.contains(firstWord)) action = "look";
    if(useSet.contains(firstWord)) action = "use";
    if(grabSet.contains(firstWord)) action = "grab";

    input = input.substring(firstWord.length());

    if(action.equals("fail")) return "You cannot do this.";
    
    //println("Action:" + action);
    
    String interactable = "fail";

    //Search all substrings for interactable
    for(int i = 0; i < input.length(); i++)
        for(int j = i + 1; j <= input.length(); j++)
            if(currentRoom.isInteractable(input.substring(i, j))) interactable = input.substring(i, j);
    //println("interactable:" + interactable);
    
    if(!interactable.equals("fail")){
        println(action + interactable);
        int mode = currentRoom.getInstructionMode(action + interactable);
        //println("interactable:" + interactable);
        if(action.equals("use") || action.equals("look")){
            if(currentRoom.isInteractable("id") && interactable.equals("id") && action.equals("use")){
                if(currentRoom != floor7A) return "There is nothing to use your ID on right now!";
                onKeyPad = true;
                return currentRoom.getInteractable("id").runInstruction(action, 0);
            }
            return currentRoom.runInstruction(action, interactable, mode);
        } else {
            Interactables current = currentRoom.getInteractable(interactable);
            if(inventory.contains(current)) return "You already have this in your inventory!";
            else {
                if(current.isInventoryAddable()) inventory.add(current);
                return currentRoom.runInstruction(action, interactable, mode);
            }
        }
    }

    for(int i = 0; i < input.length(); i++)
        for(int j = i + 1; j <= input.length(); j++)
            for(Interactables k : inventory)
                if(k.getName().equals(input.substring(i, j))) interactable = input.substring(i, j);

    if(!interactable.equals("fail")){
        if(action.equals("use") || action.equals("look")) {
            Interactables current = null;
            for(Interactables i : inventory)
                if(i.getName().equals(interactable)) {println(i.getName() + " " + interactable);current = i;}
            //println("Using inventory");
            if(current.getName().equals("id") && action.equals("use")){
                if(currentRoom != floor7A) return "There is nothing to use this ID on right now!";
                onKeyPad = true;
                return current.runInstruction(action, 0);
            }
            return current.runInstruction(action, 0);
        } else {
            return "You already have this in your inventory!";
        }
    } else {
        return "You cannot do this.";
    }
    //return null;
}

//----------------------------------------------------------------------------------------------------------------------------------------------------------------
CommandLine line;
PFont monoStandard;

int scrollDiff = 0;

void settings(){
  size(1200, 800);
}

//Creating Rooms
Room dummy;
Room tutorial;

Room floor9; //mirror

Room floor8A; //forward
Room floor8B; //open door
Room floor8C; //open door (exit)
Room floor8D; //right
Room floor8E; //use stairs

Room floor7A; //use keypad
Room floor7B; //use DDR

Room floor6; //use plant

Room floor5A; //forward
Room floor5B; //use chair
Room floor5C; //use chair

Room floor4; //grab recipe

Room floor3; //inspect cafe


Room floor2A; //left
Room floor2B; //open door
Room floor2C; //use elevator

Room floor1;  

void init_vars(){//Initialize rooms
  //Stuff n' Things
  onKeyPad = false;

  HashMap<String, Integer> empty = new HashMap<>(); 
  String nolook = "You cannot look at this item.";
  String noUse = "You cannot use this item.";

  //DUMMY
  String descriptionD = "";
  Interactables[] interactablesD = new Interactables[0];
  HashMap<String, Integer> specialsD = new HashMap<>(); 
  dummy = new Room(descriptionD, interactablesD, null, specialsD);
  
  //TUTORIAL
  String detailsT = "You are sitting at a square desk. In front of you, there is a <laptop>. You can inspect, use, or grab the item. You can access your inventory by using 'inventory'. Feel free to explore these commands! Type 'begin' once you are done. You may also go forward, backward, left, or right in some rooms";
  Interactables[] interactablesT = new Interactables[1];
  String computerLook = "It's a Scotty laptop, the newest model in fact. On the front, it has an engraving of a scottish terrier.";
  String computerUse = "You open the laptop, and the screen turns on. You try logging in to the laptop, but the password is incorrect.";
  interactablesT[0] = new Interactables("laptop", computerLook, computerUse, true);
  tutorial = new Room(detailsT, interactablesT, null, empty);
  
  //FLOOR 9
  String details9 = "\n\n Floor 9 \n\n You enter a sterile white room. There is a <mirror> on the wall.";
  Interactables[] interactables9 = new Interactables[1];
  String mirrorLook = "You see yourself in the reflection. When you put your finger to the mirror there is a small gap between your fingertip and the reflection.";
  interactables9[0] = new Interactables("mirror", mirrorLook, noUse, false);
  floor9 = new Room(details9, interactables9, null, empty);
  
  //FLOOR 8A
  String details8A = "You pry the edge of the mirror off the wall. The frame dislodges but the mirror stays put tight. With all the effort, you almost do not notice that your thumb has slipped through the mirror. You walk through the mirror into another room. \n\n Floor 8 \n\n You are in a long corridor with a series of wooden doors to your left. It’s completely silent, save for the faint buzz of the white fluorescent lights on the ceiling.";
  Interactables[] interactables8A = new Interactables[0];
  floor8A = new Room(details8A, interactables8A, null, empty);


  //FLOOR 8B
  String details8B = "You walk forward until you eventually exit out of the hallway and into a new room. It is mostly empty other than a few chairs and a public <bulletin> board on the wall. Nearby, there is a partially open <door> with a glass panel in the middle.";
  Interactables[] interactables8B = new Interactables[1];
  String bulletinLook = "There are a few flyers on the bulletin board. One of them, a flyer with white text and a blue background, catches your eye. TartanHacks, Feb 3-4 23, it reads. You wonder what that’s about.";
  interactables8B[0] = new Interactables("bulletin", bulletinLook, noUse, false);
  floor8B = new Room(details8B, interactables8B, null, empty);

  //FLOOR 8C
  String details8C = "You open the <door> and see several black rolling chairs around a long rectangular wooden <table>.";
  Interactables[] interactables8C = new Interactables[2];
  String tableLook = "There are a few objects on the table: a box of tissues, a bottle of hand sanitizer, and an <ID> card.";
  interactables8C[0] = new Interactables("table", tableLook, noUse, false);
  String IDlook = "You closely inspect the <ID> card. The name on the card reads 'Andrew Carnegie'. The edges of the card are slightly scuffed, but the card is in good condition otherwise. It seems important. Maybe you should keep hold of it for now.";
  String IDuse  = "You scan Andrew’s ID on the card scanner. A small screen turns on and prompts you to enter a five-digit code.";
  interactables8C[1] = new Interactables("id", IDlook, IDuse, true);
  floor8C = new Room(details8C, interactables8C, null, empty);

  //FLOOR 8D
  String details8D = "You exit the room. Right outside the door, you nearly trip on a bottle of <Mountain Dew> on the ground. You look around, but there is no one to be seen. There is a hallway to your right that you have not yet explored. ";
  Interactables[] interactables8D = new Interactables[1];
  String dewGrab = "You put the Mountain Dew into your backpack.";
  interactables8D[0] = new Interactables("mountain dew", nolook, dewGrab, true);
  floor8D = new Room(details8D, interactables8D, null, empty);

  //FLOOR 8E
  String details8E = "You turn right and walk down a hallway. At the end, you see a a set of <stairs> leading to the lower floor.";
  Interactables[] interactables8E = new Interactables[0];
  floor8E = new Room(details8E, interactables8E, null, empty);
  
  //FLOOR 7
  String details7A = "The stairs have a smooth railing on both sides all the way down, and you slide down the railing to the bottom of the stairs. \n \n Floor 7 \n \n You turn around, but the stairs you just came down seem to have disappeared. In front of you, there are two round tables, each with 4 chairs. On the left, there’s a trash can next to a door.";
  Interactables[] interactables7A = new Interactables[3];
  String trashDesc = "";
  String doorDesc = "It's just a door. It's looking awfully locked.";
  String doorUse = "It's locked.";
  String scanDesc = "This scanner needs an ID.";
  String scanUse = "You aren't verified for this.";
  interactables7A[0] = new Interactables("door", doorDesc, doorUse, false);
  interactables7A[1] = new Interactables("trash", trashDesc, noUse, false);
  interactables7A[1].setLook1(new String[]{"Mountain Dew", "Hunan", "Yakisoba", "Coke", "A Post-It note with '15101' on it"}, "Nothing else here...");
  HashMap<String, Integer> specials = new HashMap<>();
  specials.put("looktrash", 1);
  interactables7A[2] = new Interactables("scanner", scanDesc, scanUse, false);
  floor7A = new Room(details7A, interactables7A, null, specials);
  
  String details7B = "You type ‘15101’ into the keypad and the door swings open. Inside, there are two bean bags, a ping pong table in the center, and a Dance Dance Revolution (<DDR>) machine.";
  Interactables[] interactables7B = new Interactables[0];
  floor7B = new Room(details7B, interactables7B, null, empty);

  //FLOOR 6
  String details6 = "You jump onto and the machine whirs on. Caramelldansen starts playing and the arrow tiles flash across the screen as you struggle to keep up. The lack of exercise is really catching up to you. Even so, you breathe a sigh of relief as the song comes to a close. Although you scored far from perfect, your non-existent opponent did far worse. Just as this thought crosses your mind, the room darkens and an electric blue light flashes across the room. \n\n Floor 6 \n\n You find yourself lying on an orange couch. It’s not particularly comfortable, but it’s a welcome surprise after such a long adventure. Unfortunately for you, you’re less than halfway through. You roll over on your side and see some more orange couches and a large potted <plant> in the corner.";
  Interactables[] interactables6 = new Interactables[1];
  String plantLook = "The plant has large, waxy leaves that shift different shades of dark green depending on the angle the light hits them. The plant is potted in a dark brown ceramic that looks a bit dusty on the outside.";
  interactables6[0] = new Interactables("plant", plantLook, noUse, false);
  floor6 = new Room(details6, interactables6, null, empty);

  //FLOOR 5A 
  String details5A = "You pick up the plant. It feels incredibly heavy, but that’s because you’re weak. You drag the plant to the center of the room. You summon all the strength in your legs and manage to lift the plant to chest level before smashing it onto the ground. The ground cracks, and you fall through. \n\n Floor 5 \n\n You come to awareness standing next to a blocked off stairwell. On your right, there’s a ledge with a small plant that has “Hope” written on the pot. The plant does not appear to be in the best condition. The leaves are browning and beginning to fall off. The dirt is extremely dry. Luckily, there is a small <bottle> of water nearby to water the plant.";
  Interactables[] interactables5A = new Interactables[1];
  String bottleUse = "You unscrew the cap of the bottle and pour a bit of water into the pot. The plant seems to look a bit better. Nice job, you’ve partially atoned for your sin of previously killing an innocent plant. Only partially though.";
  interactables5A[0] = new Interactables("bottle", nolook, bottleUse, false);
  floor5A = new Room(details5A, interactables5A, null, empty);
  
  //FLOOR 5B 
  String details5B = "You walk forward past a few slightly secluded study areas. Each of them is surrounded on two sides by whiteboards and includes a circular table and 3 <chairs>.";
  Interactables[] interactables5B = new Interactables[1];
  String lookChair = "The chairs are the famed Herman Miller Aerons. They’re pretty expensive.";
  interactables5B[0] = new Interactables("chair", lookChair, "", false);
  floor5B = new Room(details5B, interactables5B, null, empty);
  
  //FLOOR 5C
  String details5C = "You sit in the chair. It’s less comfortable than expected, but it’s fun to roll around. You roll down the rest of the hallway until you reach a helix.";
  Interactables[] interactables5C = new Interactables[1];
  String lookHelix = "There is a small sign on the helix with faded text. Squinting carefully, you read: “This Helix is for Business Purposes Only. Please do not roll down it on a chair”. Of course, you could do it anyways. \n [We do not condone actions that are not in line with CMU’s university policies. This is a work of fiction.]";
  interactables5C[0] = new Interactables("helix", lookHelix, noUse, false);
  floor5C = new Room(details5C, interactables5C, null, empty);

  //FLOOR 4
  String details4 = "You speed down the helix in your rolling chair and see an opening to get off at a different floor. The chair is quickly picking up speed but you decide to jump off at the opening. \n\n Floor 4 \n\n You tumble out of the chair and onto the floor. Your knee hurts a bit, so you roll up your pant leg to expose it. The injury isn’t bleeding, so it’s probably just a bruise. That doesn’t mean it hurts any less though. As you’re inspecting your injury, a person approaches from behind. \n 'What are you doing here?' \n ... \n 'I see, you’re on an adventure and perhaps I can be of some help. Here’s a hint for the next stage.' \n Professor Wackey leads you to a new area and offers you a yellow post-it note with a <recipe> scribbled on it.";  
  Interactables[] interactables4 = new Interactables[0];
  floor4 = new Room(details4, interactables4, null, empty);


  //FLOOR 3
  String details3 = "He wishes you the best of luck before turning around and leaving. \n \n Floor 3 \n \n You watch the retreating back of Professor Wackey. In front of you, there’s what looks to be the old remains of a cafe.";
  Interactables[] interactables3 = new Interactables[1];
  String lookCoffee = "It’s the latest Nestle model, with ten spouts for steaming milk and only one channel for extracting espresso. The design is perfect for a school that only wants hot chocolate.";
  String useCoffee = "The coffee maker looks complicated. Your smooth brain is unable to parse the situation without instructions.";
  interactables3[0] = new Interactables("coffee", lookCoffee, useCoffee, false);
  floor3 = new Room(details3, interactables3, null, empty);
  
  //FLOOR 2
  String details2A = "With the aid of the step-by-step recipe, you crawl your way through making the perfect drip of “Raspberry and Mint”-flavored coffee, according to the stale bag you found in the back of a cabinet. You take a sip to savor your hard work. \n \n Suddenly, blots of color block your vision. \n \n Floor 2 \n \n The spots slowly clear. You find yourself in a gray room, the walls have clearly been left untouched for quite some time. A <mouse> chirps at your feet.";
  Interactables[] interactables2A = new Interactables[1];
  String mouseDesc = "It's cute. <:3 )~~";
  interactables2A[0] = new Interactables("mouse", mouseDesc, noUse, false);
  floor2A = new Room(details2A, interactables2A, null, empty);

  String details2B = "You give chase to the mouse. It slips under a <door> on the right.";
  Interactables[] interactables2B = new Interactables[0];
  floor2B = new Room(details2B, interactables2B, null, empty);  
  
  String details2C = "You push open the thick wooden door. The mouse scampers out of the way. Behind the doors, you see two <elevators>.";
  Interactables[] interactables2C = new Interactables[1];
  interactables2C[0] = new Interactables("elevators", "The <elevators> have silver doors. You check yourself out in the reflection. You look okay. The dark circles under your eyes ruin the effect a bit.", noUse, false);
  floor2C = new Room(details2C, interactables2C, null, empty);
  
  //FLOOR 1
  String details1 = "You press the down button on the elevator. The light turns on, but nothing happens. \n . \n . \n. After ten torturous minutes, the elevator arrives. The doors ding as they close behind you. At long last, you reach the 1st floor. \n\n Floor 1 \n\n You enter a dusty tan room. There is nothing behind you. There is s a <shower> in the corner, a spider web is stuck to its shower head.";
  Interactables[] interactables1 = new Interactables[1];

  String showerDesc = "The walls of the <shower> are wet. The spider is struggling to hang on with its thread.";
  String showerUse = "With a shaky hand, you twist the shower’s knob. The hard mineral-filled water erodes the dust off your clothes. The mouse appears behind you and seems to clap its paws together. \n \n The rhythmic pattern of water beating down on you is soothing. Hypnotic, even. \n \n This is a sign.";
  interactables1[0] = new Interactables("shower", showerDesc, showerUse, false);
  floor1 = new Room(details1, interactables1, null, empty);

  
  //Jumplists
  HashMap<String, Room> jFloor9 = new HashMap<>();
  jFloor9.put("use mirror", floor8A);
  floor9.setJumpList(jFloor9);

  HashMap<String, Room> jFloor8A = new HashMap<>();
  jFloor8A.put("forward", floor8B);
  jFloor8A.put("go forward", floor8B);
  jFloor8A.put("move forward", floor8B);
  floor8A.setJumpList(jFloor8A);

  HashMap<String, Room> jFloor8B = new HashMap<>();
  jFloor8B.put("use door", floor8C);
  jFloor8B.put("open door", floor8C);
  floor8B.setJumpList(jFloor8B);

  HashMap<String, Room> jFloor8C = new HashMap<>();
  jFloor8C.put("use door", floor8D);
  jFloor8C.put("open door", floor8D);
  floor8C.setJumpList(jFloor8C);
  
  HashMap<String, Room> jFloor8D = new HashMap<>();
  jFloor8D.put("right", floor8E);
  jFloor8D.put("go right", floor8E);
  jFloor8D.put("move right", floor8E);
  floor8D.setJumpList(jFloor8D);

  HashMap<String, Room> jFloor8E = new HashMap<>();
  jFloor8E.put("use stairs", floor7A);
  floor8E.setJumpList(jFloor8E);

  HashMap<String, Room> jFloor7A = new HashMap<>();
  floor7A.setJumpList(jFloor7A);

  HashMap<String, Room> jFloor7B = new HashMap<>();
  jFloor7B.put("use ddr", floor6);
  floor7B.setJumpList(jFloor7B);

  HashMap<String, Room> jFloor6 = new HashMap<>();
  jFloor6.put("use plant", floor5A);
  floor6.setJumpList(jFloor6);

  HashMap<String, Room> jFloor5A = new HashMap<>();
  jFloor5A.put("forward", floor5B);
  floor5A.setJumpList(jFloor5A);
  
  HashMap<String, Room> jFloor5B = new HashMap<>();
  jFloor5B.put("use chair", floor5C);
  floor5B.setJumpList(jFloor5B);

  HashMap<String, Room> jFloor5C = new HashMap<>();
  jFloor5C.put("use chair", floor4);
  jFloor5C.put("use helix", floor4);
  floor5C.setJumpList(jFloor5C);

  HashMap<String, Room> jFloor4 = new HashMap<>();
  jFloor4.put("take recipe", floor3);
  jFloor4.put("grab recipe", floor3);
  floor4.setJumpList(jFloor4);

  HashMap<String, Room> jFloor3 = new HashMap<>();
  jFloor3.put("use recipe", floor2A);
  floor3.setJumpList(jFloor3);

  HashMap<String, Room> jFloor2A = new HashMap<>();
  jFloor2A.put("go left", floor2B);
  jFloor2A.put("move left", floor2B);
  jFloor2A.put("walk left", floor2B);
  jFloor2A.put("left", floor2B);
  floor2A.setJumpList(jFloor2A);

  HashMap<String, Room> jFloor2B = new HashMap<>();
  jFloor2B.put("open door", floor2C);
  jFloor2B.put("open doors", floor2C);
  jFloor2B.put("use door", floor2C);
  jFloor2B.put("use doors", floor2C);
  floor2B.setJumpList(jFloor2B);
  
  HashMap<String, Room> jFloor2C = new HashMap<>();
  jFloor2C.put("use elevators", floor1);
  jFloor2C.put("use elevator", floor1);
  floor2C.setJumpList(jFloor2C);
  
  HashMap<String, Room> jFloor1 = new HashMap<>();
  floor1.setJumpList(jFloor1);
  
  HashMap<String, Room> jDummy = new HashMap<>();
  jDummy.put("start", tutorial);
  dummy.setJumpList(jDummy);
  
  HashMap<String, Room> jTutorial = new HashMap<>();
  jTutorial.put("begin", floor9);
  tutorial.setJumpList(jTutorial);
}

void setup(){
  
  init_vars();
  
  frameRate(45);
  line = new CommandLine();
  
  currentRoom = dummy;
  inventory = new ArrayList<>();
  
  String[] fontList = PFont.list();
  //printArray(fontList);
  //for(int i = 0; i < 500; i++) //println(fontList[i]);
  
  PFont monoStandard = createFont("Courier New", 44);
  textFont(monoStandard);
  
  textAlign(LEFT, TOP);
}

static class Graphics
{
  static int typelineX = 60;
  static int typelineY = 750;
  
  //60 - 900 - 40 - 24 - 45 - 56
  
  static int textSize = 25; 
  
  static float charX = 15.07;
  static int charY = 28;
  
  static int wrapCount = 70;
}

void draw()
{
  line.clDraw();
}

class TextLine
{
  int r, g, b;
  String txt;
  public TextLine(int ir, int ig, int ib, String itxt)
  {
    r = ir; g = ig; b = ib; txt = itxt; 
  }
  public void tlDraw(int x, int y)
  {
    fill(r, g, b);
    textSize(Graphics.textSize);
    sText(txt, x, y);
  }
  
  public void concatChar(char s)
  {
    txt += s; 
  }
}

interface Jobable
{
  boolean doSetup();
  void giveTime(float curTime);
  int[] rgb();
  void setFont();
  String extractStr();
  int lineBreaks();
  boolean isPlayerActive();
  boolean hasTerminated();
  boolean wipeHistory();
}

class PrintJob implements Jobable
{
  float typeTime = 2;
  float totalTime = 2.5;
  boolean setupDone = false;
  boolean doLineBreak = false;
  
  float recentTime;
  
  String text = "";
  int[] marked; //0 = not marked, 1 = marked for send, 2 = sent
  
  public PrintJob(String in)
  {
     constructorHelp(in, 32, 0.75, true);
  }
  
  public PrintJob(String in, float charsPerSec, float delayTime, boolean breakIt)
  {
     constructorHelp(in, charsPerSec, delayTime, breakIt);
  }
  
  public void constructorHelp(String in, float charsPerSec, float delayTime, boolean breakIt)
  {
     typeTime = in.length() * (1.0/charsPerSec);
     totalTime = typeTime + delayTime;
     text = in;
     marked = new int[in.length()];
     setupDone = false;
     doLineBreak = breakIt;
     
     println("totTime " + totalTime);
  }
  
  boolean doSetup()
  {
    if(setupDone) return false;
    setupDone = true;
    return doLineBreak;
  }
  void setFont()
  {
    textFont(monoStandard);
    textSize(Graphics.textSize);
  }
  void giveTime(float curTime)
  {
    recentTime = curTime;
    float prog = (curTime + 0.01) / typeTime; 
    
    int index = floor(prog * text.length());
    if(curTime > typeTime) index = text.length();

    for(int i = index; i >= 0; i--)
    {
      if(index >= text.length()) continue;
      if(marked[i] != 0) break;
      marked[i] = 1;
    }
  }
  
  int[] rgb()
  {
    return new int[]{255, 255, 255};
  }
  
  String extractStr()
  {
    if(recentTime > typeTime)
    {
      for(int i = 0; i < marked.length; i++) if(marked[i] == 0) marked[i] = 1;
    }
    String out = "";
    for(int i = 0; i < text.length(); i++)
    {
      if(marked[i] == 1)
      {
        out += text.charAt(i);
        marked[i] = 2;
      }
    }
    return out;
  }
  int lineBreaks() {return 0;}
  
  boolean isPlayerActive()
  {
    return false;
    //return recentTime >= (totalTime + 0.1);
  }
  boolean hasTerminated()
  {
    //println("term " + recentTime + " " + totalTime);
    return recentTime >= totalTime; 
  }
  boolean wipeHistory() {return false;}
}



class CommandJob implements Jobable
{
  float startTime = 0.3;
  float typeTime = 2;
  float totalTime = 2.5;
  boolean setupDone = false;
  boolean extractDir = false;
  
  float recentTime;
  
  String text = "";
  String directory = "";
  int[] marked; //0 = not marked, 1 = marked for send, 2 = sent
  
  public CommandJob(String in, int directoryLen)
  {
     startTime = 0.3;
     typeTime = (in.length() - directoryLen) * (1.0/20.0) + startTime;
     totalTime = typeTime + 0.6;
     
     text = in;
     directory = in.substring(0, directoryLen); 
     
     marked = new int[in.length()];
     for(int i = 0; i < directoryLen; i++)
     {
       if(i >= in.length()) break;
       marked[i] = 2;
     }
     setupDone = false;
     
     println("totTime " + totalTime);
  }
  
  boolean doSetup()
  {
    if(setupDone) return false;
    setupDone = true;
    return true;
  }
  void setFont()
  {
    textFont(monoStandard);
    textSize(Graphics.textSize);
  }
  void giveTime(float curTime)
  {
    recentTime = curTime;
    float prog = (curTime + 0.01 - startTime) / typeTime; 
    
    if(prog < 0) return;
    
    int index = floor(prog * (text.length() - directory.length())) + directory.length();
    for(int i = index; i >= directory.length(); i--)
    {
      if(index >= text.length()) continue;
      if(marked[i] != 0) break;
      marked[i] = 1;
    }
  }
  
  int[] rgb()
  {
    return new int[]{20, 200, 20};
  }
  
  String extractStr()
  {
    String out = "";
    for(int i = 0; i < text.length(); i++)
    {
      if(marked[i] == 1)
      {
        out += text.charAt(i);
        marked[i] = 2;
      }
    }
    if(!extractDir) out = directory + out;
    extractDir = true;
    return out;
  }
  int lineBreaks() {return 0;}
  
  boolean isPlayerActive()
  {
    return true;
    //return recentTime >= totalTime;
  }
  boolean hasTerminated()
  {
    //println("term " + recentTime + " " + totalTime);
    return recentTime >= totalTime; 
  }
  boolean wipeHistory() {return false;}

}


class ContinueJob implements Jobable
{
  boolean setupDone = false;
  float recentTime = 0;
  float totalTime = 1;
  
  
  public ContinueJob()
  {
    totalTime = 4;
  }
  
  boolean doSetup()
  {
    if(setupDone) return false;
    setupDone = true;
    return true;
  }
  void giveTime(float curTime)
  {
    recentTime = curTime;
  }
  void setFont()
  {
    textFont(monoStandard);
    textSize(Graphics.textSize);
  }
  
    int[] rgb()
  {
    return new int[]{20, 200, 20};
  }
  
  String extractStr()
  {
    String out = "";
    return out;
  }
  int lineBreaks() {return 0;}
  
  boolean isPlayerActive()
  {
    return false;
    //return recentTime >= totalTime;
  }
  boolean hasTerminated()
  {
    //println("term " + recentTime + " " + totalTime);
    return recentTime >= totalTime; 
  }
  boolean wipeHistory() {return false;}

}


//class GibberishJob implements Jobable
//{
//  boolean setupDone = false;
//  boolean doSetup()
//  {
//    if(setupDone) return false;
//    setupDone = true;
//    return true;
//  }
//  void setFont()
//  {
//    textFont(wingdings);
//    textSize(Graphics.textSize);
//  }
  
//  int[] rgb() { return new int[]{255, 255, 255}; }
//  String extractStr() {
//    float nnextChar = 10;
//  }
//  int lineBreaks() {
//    return 0; 
//  }
//  boolean isPlayerActive() {
//    return true; 
//  }
//  boolean hasTerminated() {
//    return false; 
//  }
//  boolean wipeHistory() {
//    return false; 
//  }
//}


class JanitorJob implements Jobable
{
  float recentTime = 0;
  float totalTime = 3.8;
  float lastWipeTime = 0;
  
  boolean doSetup() {return false;}
  void giveTime(float curTime) {
    recentTime = curTime;
  }
  int[] rgb() {return new int[]{69, 69, 255};}
  void setFont() {return;}
  String extractStr() {return "";}
  int lineBreaks() {return 0;}
  boolean isPlayerActive() { return false; }
  boolean hasTerminated() {return recentTime >= totalTime;}
  boolean wipeHistory() {
    if(recentTime >= lastWipeTime+0.05)
    {
      lastWipeTime = recentTime;
      return true;
    }
    return false;
  }
}


class CommandLine 
{
  int roomID = 0;
  float roomTime = 0;
  float cursorTime = 0;
  
  boolean playerActive = true;
   
  String userBox = "";
  ArrayList<TextLine> prevLines = new ArrayList<TextLine>();
  
  String directory = "User/disruption.java> ";
  boolean pressEnterToStart = true;
  boolean startUpCutsceneComplete = false;
  
  ArrayList<Jobable> jobs = new ArrayList<Jobable>();
  float jobTime = 0;
   
  public CommandLine(){
    //assignJob(new PrintJob("BREAK --- OUT", 16, 2, true));
    //assignJob(new PrintJob("You find yourself in a room with a mirror.", 32, 2, true));
    //assignJob(new PrintJob("third msg lol ", 45, 3.5, true));
    //assignJob(new PrintJob("continue", 45, 1.5, false));

    //startUpScript();
  }
  
  public void startUpScript()
  {
    assignJob(new PrintJob("Initializing Extraction", 16, 0.3, true));
    
    assignJob(new PrintJob("...", 1.8, 0.3, false));
    assignJob(new PrintJob("...", 1.8, 0.6, false));

    assignJob(new PrintJob(" ", 16, 1.0, true));
    //getResponse("start");
    float speed = 10;
    float timeDel = 0.5;
    for(int i = 0; i < 18; i++)
    {
      String msg = (i == 0) ? "Error terminating BREAKOUT.exe" : "Reattempting to terminate BREAKOUT.exe";
      assignJob(new PrintJob(msg, speed, timeDel, true));
      speed = 1.4 * speed;
      if(i <= 18) timeDel = 0.8 * timeDel;
    }
    
    assignJob(new PrintJob("Running cleanTrace.exe", speed, 1, true));
    assignJob(new JanitorJob());
    
    //assignJob(new PrintJob("You enter a sterile white room. There's a mirror on the wall.", 32, 1, true));
    //assignJob(new PrintJob("...", 45, 1.5, true));
    
    String response = getResponse("start");
    
    String[] parsedResp = response.split("\n");
    
    for(int i = 0; i < parsedResp.length; i++)
    {
      line.assignJob(new PrintJob(parsedResp[i])); 
    }

  }
    
  public void drawBlinky()
  {
    if(!pressEnterToStart && !playerActive) return;
    if(floor(cursorTime / 0.65) % 2 == 0)
    {
      fill(255, 255, 255, 100);
      noStroke();
      int x = Graphics.typelineX;
      int strLen = directory.length() + userBox.length();
      x += strLen * Graphics.charX;
      int y = Graphics.typelineY;
      rect(x, y - scrollDiff, Graphics.charX, Graphics.charY);
    }
  }
  public void clDraw()
  {
    cursorTime += 1.0/30.0;
    background(0);
    
    textSize(Graphics.textSize);
    
    //fill(255);
    //text("wefdgdsgv", 100, 100);
    
    if(pressEnterToStart){
      fill(255, 255, 255);
      sText("tar -xvf break-out.zip", Graphics.typelineX, Graphics.typelineY);
    }
    else if(!startUpCutsceneComplete)
    {
      fill(110, 110, 110);
      sText("~/Starting Up. Do not power off your device.", Graphics.typelineX, Graphics.typelineY);
      
      if(jobs.size() == 0) startUpCutsceneComplete = true;
    }
    else if(playerActive)
    {
      fill(20, 200, 20);
      sText(directory + userBox, Graphics.typelineX, Graphics.typelineY);
    }
    else
    {
      fill(130, 130, 130);
      sText("User/WRITE_PERMISSION_OVERRIDEN>", Graphics.typelineX, Graphics.typelineY);
    }

    
    fill(255, 255, 255);
    
    for(int i = 0; i < prevLines.size(); i++)
    {
      int y = Graphics.typelineY - (i + 1) * (Graphics.charY + 2);
      int x = Graphics.typelineX;
      prevLines.get(i).tlDraw(x, y);
    }
    
    drawBlinky();
    processJobs();
  }
  
  public void processJobs()
  {
    if(jobs.size() == 0) return;
    jobTime += 1.0/30.0;
    Jobable job0 = jobs.get(0);
    int[] rgb = job0.rgb();
    
    if(job0.doSetup()) {
      prevLines.add(0, new TextLine(rgb[0], rgb[1], rgb[2], ""));
    }
    
    //println("jobsbs " + jobs + " " + jobs.size());
    
    job0.giveTime(jobTime);
    String typeOut = job0.extractStr();
    
    playerActive = job0.isPlayerActive();
    
    if(job0.wipeHistory())
    {
      if(prevLines.size() > 0)
      {
        println("sb " + prevLines.size());
        prevLines.remove(0);

        println("af " + prevLines.size());

      }
      //prevLines = new ArrayList<TextLine>();
    }
    
    if(job0.hasTerminated()) {
      if(job0.wipeHistory())
      {
        prevLines = new ArrayList<TextLine>();
      }
      jobTime = 0;
      jobs.remove(0); 
      if(jobs.size() == 0) playerActive = true;

    }
    
    for(int i = 0; i < typeOut.length(); i++)
    {
      narratorAdd(typeOut.charAt(i)); 
    }
    
    prevLineWrap(rgb);
  }
  
  public void prevLineWrap(int[] rgb)
  {
    if(prevLines.size() > 0 && prevLines.get(0).txt.length() > Graphics.wrapCount)
    {
      String wrapToNext = "";
      String oldWrap = prevLines.get(0).txt;
      for(int i = oldWrap.length() - 1; i >= 0; i--)
      {
        if(oldWrap.charAt(i) == ' ')
        {
          prevLines.get(0).txt = oldWrap.substring(0, i+1);
          wrapToNext = oldWrap.substring(i+1);
          break;
        }
      }
      //println("ow " + oldWrap + " ||| " + prevLines.get(0).txt + " ||| " + wrapToNext);
      prevLines.add(0, new TextLine(rgb[0], rgb[1], rgb[2], wrapToNext));
    }
  }
  
  public void assignJob(Jobable j)
  {
    jobs.add(j); 
  }
  
  public void narratorAdd(char key)
  {
    if(prevLines.size() == 0) //OR FIRST LINE IS TOO LONG NEED LINEBREAK
    {
      prevLines.add(0, new TextLine(255, 255, 255, ""));
    }
    
    prevLines.get(0).concatChar(key);
  }
  
  public void keyboardAdd(char key)
  {
    if(pressEnterToStart) return;
    if(!playerActive) return;
    if(userBox.length() > 20) return;
    cursorTime = 0;
    userBox += key;
  }
  public void keyboardBack()
  {
    if(pressEnterToStart) return;
    if(!playerActive) return;
    if(userBox.length() == 0) return;
    cursorTime = 0;
    userBox = userBox.substring(0, userBox.length() - 1);
  }
  public void keyboardEnter()
  {
    if(pressEnterToStart){
      pressEnterToStart = false;
      startUpScript();
      return; 
    }
    if(!playerActive) return;
    this.newLine(userBox, true);
  }
  public void newLine(String txt, boolean fromUser)
  {
    if(txt.length() == 0 && fromUser) return;
    if(fromUser) {
      //prevLines.add(0, new TextLine(20, 200, 20, directory + txt));
      line.assignJob(new CommandJob(directory + txt, directory.length()));
      
      String response = getResponse(txt);
      //response = "You cannot do this. \nOr can you? \nDun dun dunnnnn";
      String[] parsedResp = response.split("\n");
      println("pront " + response + " " + parsedResp.length);
      
      for(int i = 0; i < parsedResp.length; i++)
      {
        line.assignJob(new PrintJob(parsedResp[i]));
      }
    }
    else
      prevLines.add(0, new TextLine(255, 255, 255, txt));
      
      
    while(prevLines.size() > 25)
    {
      prevLines.remove(prevLines.size()-1);
    }
    if(fromUser) userBox = "";
  }
}

String runGameTest(String s){
  return s+s; 
}

void keyPressed()
{
  println("keycode " + key + " " + keyCode + " " + (key == CODED));
  if(keyCode == 16) {
    //line.newLine("textytextytexty", false);
    //line.assignJob(new PrintJob("According to all known laws of aviation, there is no way a bee should be able to fly. Its wings are too small to get its fat little body off the ground. The bee, of course, flies anyways, because humans don't care what humans think are impossible."));
  }
  
  if(key == CODED) return;
  
  if(keyCode == 8) line.keyboardBack();
  else if(keyCode == 10) line.keyboardEnter();
  else line.keyboardAdd(key);
}


public void sText(String txt, int x, int y)
{
  //println("atta " + txt + " " + scrollDiff);
  text(txt, x, y - scrollDiff); 
}
void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  
  if(e < -0.5) scrollDiff -= 30;
  if(e > 0.5) scrollDiff += 30;
  
  if(scrollDiff > 0) scrollDiff = 0;
}
