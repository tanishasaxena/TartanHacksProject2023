CommandLine line;
PFont monoStandard;

int scrollDiff = 0;

void settings(){
  size(1600, 1000);
}
void setup(){
  frameRate(45);
  line = new CommandLine();
  
  String[] fontList = PFont.list();
  printArray(fontList);
  for(int i = 0; i < 500; i++) println(fontList[i]);
  
  PFont monoStandard = createFont("CourierNewPSMT", 44);
  textFont(monoStandard);
  
  textAlign(LEFT, TOP);
}

static class Graphics
{
  static int typelineX = 60;
  static int typelineY = 900;
  
  static int charX = 24;
  static int charY = 45;
  
  static int wrapCount = 56;
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
    textSize(40);
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
     totalTime = typeTime + 0.5;
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
     typeTime = (in.length() - directoryLen) * (1.0/20.0);
     totalTime = typeTime + 0.1;
     
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
    float prog = (curTime + 0.01) / typeTime; 
    
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
    return recentTime >= totalTime;
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
    
    textSize(40);
    
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
      int y = Graphics.typelineY - (i + 1) * 46;
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
      
      String response = runGame(txt);
      
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
  text(txt, x, y - scrollDiff); 
}
void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  
  if(e < -0.5) scrollDiff -= 30;
  if(e > 0.5) scrollDiff += 30;
  
  if(scrollDiff > 0) scrollDiff = 0;
}
