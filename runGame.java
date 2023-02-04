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
        if(specialInstructions.containsKey(input)) return (int) specialInstructions.get(key);
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


String getResponse(String input){
    //Clean Input
    input = input.toLowerCase();
    
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
        int mode = currentRoom.getInstructionMode(action + interactable);
        //println("interactable:" + interactable);
        if(action.equals("use") || action.equals("look")) return currentRoom.runInstruction(action, interactable, mode);
        else {
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
  size(1600, 1000);
}

//Creating Rooms
Room roomTest1;

void setup(){
  
  //Initialize rooms
  String description1 = "Test 1 Description";
  Interactables[] interactables1 = new Interactables[1];
  interactables1[0] = new Interactables("interactable name", "look string", "use string", true); //Boolean determines if it can be added to inventory
  HashMap<String, Integer> specials1 = new HashMap<>(); 
  roomTest1 = new Room(description1, interactables1, null, specials1);
  
  //Jumplists
  HashMap<String, Room> jumpList1 = new HashMap<>();
  jumpList1.put("jump 1 to 2", roomTest1);
  roomTest1.setJumpList(jumpList1);
  
  frameRate(45);
  line = new CommandLine();
  
  currentRoom = roomTest1;
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
  static int typelineY = 900;
  
  //60 - 900 - 40 - 24 - 45 - 56
  
  static int textSize = 25; 
  
  static float charX = 15.07;
  static int charY = 28;
  
  static int wrapCount = 88;
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
  String extractStr();
  boolean isPlayerActive();
  boolean hasTerminated();
}

class PrintJob implements Jobable
{
  float typeTime = 2;
  float totalTime = 2.5;
  boolean setupDone = false;
  
  float recentTime;
  
  String text = "";
  int[] marked; //0 = not marked, 1 = marked for send, 2 = sent
  
  public PrintJob(String in)
  {
     typeTime = in.length() * (1.0/32.0);
     totalTime = typeTime + 0.75;
     text = in;
     marked = new int[in.length()];
     setupDone = false;
     
     println("totTime " + totalTime);
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
    float prog = (curTime + 0.01) / typeTime; 
    
    int index = floor(prog * text.length());
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
  
  boolean isPlayerActive()
  {
    return recentTime >= totalTime;
  }
  boolean hasTerminated()
  {
    //println("term " + recentTime + " " + totalTime);
    return recentTime >= totalTime; 
  }
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
  
  ArrayList<Jobable> jobs = new ArrayList<Jobable>();
  float jobTime = 0;
   
  public CommandLine(){

  }
    
  public void drawBlinky()
  {
    if(!playerActive) return;
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
    
    if(playerActive)
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
    
    if(job0.hasTerminated()) {
      playerActive = true;
      jobTime = 0;
      jobs.remove(0); 
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
    if(!playerActive) return;
    if(userBox.length() > 20) return;
    cursorTime = 0;
    userBox += key;
  }
  public void keyboardBack()
  {
    if(!playerActive) return;
    if(userBox.length() == 0) return;
    cursorTime = 0;
    userBox = userBox.substring(0, userBox.length() - 1);
  }
  public void keyboardEnter()
  {
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
      
      line.assignJob(new PrintJob(response));
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

String runGame(String s){
  return s+s; 
}

void keyPressed()
{
  println("keycode " + key + " " + keyCode + " " + (key == CODED));
  if(keyCode == 16) {
    //line.newLine("textytextytexty", false);
    line.assignJob(new PrintJob("According to all known laws of aviation, there is no way a bee should be able to fly. Its wings are too small to get its fat little body off the ground. The bee, of course, flies anyways, because humans don't care what humans think are impossible."));
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
