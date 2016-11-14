
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Evaluator {

	MineField mineField = new MineField();
	ArrayList<String> steps = new ArrayList<String>();
	int totalMovement = 0;
	int totalFireMade = 0;
	static final String pass = "pass";
	static final String fail = "fail (0)";
	
	/**
	 * load the field data from the input file
	 * @param file file name of the field
	 * @throws FileNotFoundException 
	 * @throws InputFileFormatException 
	 */
	public void loadField(String fileName) throws FileNotFoundException, InputFileFormatException{
		File file = new File(fileName);
        Scanner sc = new Scanner(file);
        int y_height = 0;
        int x_width = -1;
        if(!sc.hasNext()){
        	throw new InputFileFormatException(InputFileFormatException.wrongFieldLength);
        }
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            char[] chars = line.toCharArray();
            if(x_width == -1){
            	x_width = chars.length;
            }else if(x_width != chars.length){
            	throw new InputFileFormatException(InputFileFormatException.wrongFieldLength);
            }
            for(int i=0;i<chars.length;i++){
            	char c = chars[i];
            	if(c == '.'){
            		continue;
            	}
            	int z = 0;
            	if(c >= 'a' &&  c <= 'z'){
            		z = -(c - 'a' + 1);
            	}else if(c >= 'A' &&  c <= 'Z'){
            		z = -(c - 'A' + 27);
            	}else{
            		throw new InputFileFormatException(InputFileFormatException.InvalidFieldCharacter);
            	}
            	Mine mine = new Mine(i, y_height, z);
            	mineField.addMine(mine);
            }
            --y_height;
        }
        if(x_width%2 == 0 || y_height%2 == 0){
        	throw new InputFileFormatException(InputFileFormatException.NotCenteredVessel);
        }
        sc.close();
        mineField.setVesselStartPosition(x_width/2, y_height/2, 0);
	}
	
	/**
	 * load the script; 
	 * all steps are saved in the arraylist steps
	 * @param scriptFile file name of the script
	 * @throws FileNotFoundException 
	 */
	public void loadScript(String scriptFile) throws FileNotFoundException{
		File file = new File(scriptFile);
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String step = sc.nextLine();
            steps.add(step);
        }
        sc.close();
	}
	
	/**
	 * do the evaluation 
	 */
	public void runEvaluation(){
		//SPEACIAL CASE: unsolvable mine field 
		if(mineField.passMine()){
			System.out.println(fail);
			return;
		}
		
		for(int i=0;i<steps.size();i++){
			//CASE 3: all mines cleared, but steps remaining – pass (1 point)
			if(mineField.allMineClear()){
				System.out.println(pass + "(1)");
				return;
			}
			String step = steps.get(i).trim();
			//Printing every step: part one is the step number
			System.out.println("Step "+(i+1));
			//print a optional empty line here
			System.out.println();
			
			//Printing every step: part two is the current mine field
			mineField.printField();
			//print a optional empty line here
			System.out.println();
			
			//Printing every step: part three is the current instructions
			System.out.println(step);
			//print a optional empty line here
			System.out.println();
			
			//first instruction is fire_pattern;
			//second instruction is direction;
			String[] instructions = processStepStr(step);
			//run the step
			
			mineField.fire(instructions[1]);
			mineField.move(instructions[0]);
			
			//Printing every step: part four is the resultant field
			mineField.printField();
			//print a optional empty line here
			System.out.println();
			
			//CASE 1: passed a mine – fail (0 points)
			if(mineField.passMine()){
				System.out.println(fail);
				return;
			}
		}
		//CASE 4: all mines cleared, no steps remaining – pass
		if(mineField.allMineClear()){
			System.out.println(pass + "("+computerScore()+")");
		}else{
		//CASE 3: script completed but mines still remaining – fail (0 points)
			System.out.println(fail);
		}
		
	}

	/**
	 * Make a string[] to save the instructions for fire and movement. The default instructions are both empty, the vessel will just drop one 
	 * If invalid characters appear, ignore them and use default instructions
	 * @param step: the step string read in from the script file
	 * @return
	 */
	private String[] processStepStr(String step){
		String fire_pattern = "";
		String move_direction = "";
		if(step.length()>0){
			if(step.indexOf(" ")==-1){
				if(isDirection(step)){
					totalMovement++;
					move_direction = step;
				}else if(isFirePattern(step)){
					totalFireMade++;
					fire_pattern = step;
				}
				//if no appropriate instruction found, the default instruction assumes the step is an empty line
			}else{
				String possibleFireInstruction = step.split("\\s+")[0]; 
				if(isFirePattern(possibleFireInstruction)){
					fire_pattern = 	possibleFireInstruction;
					totalFireMade++;
				}
				String possibleMoveInstruction = step.split("\\s+")[1];
				if(isDirection(possibleMoveInstruction)){
					totalMovement++;
					move_direction = possibleMoveInstruction;	
				}
			}
		}
		String[] commands = new String[]{move_direction, fire_pattern};
		return commands;
	}
	
	private boolean isDirection(String cmd){
		return cmd.equals("north") || cmd.equals("south") 
				|| cmd.equals("east") || cmd.equals("west"); 
	}
	
	private boolean isFirePattern(String cmd){
		return cmd.equals("alpha") || cmd.equals("beta") 
				|| cmd.equals("gamma") || cmd.equals("delta"); 
	}
	
	
	/**
	 * 
	 * @return the score if the script succeeds
	 */
	private int computerScore(){
		int initialMineCount = mineField.getInitialMineCount();
		//The starting score is 10 times the initial number of mines in the cuboid
		int totalScore = initialMineCount *10;
		//Subtract 5 points for every shot fired, but no more than 5 times the number of initial mines.
		if(totalFireMade > initialMineCount){
			totalScore -= 5*initialMineCount;
		}else{
			totalScore -= 5*totalFireMade;
		}
		//subtract 2 points for every km moved north, south east or west, but no more that 3 times the number of initial mines.
		if(2*totalMovement > 3*initialMineCount){
			totalScore -= 3*initialMineCount;
		}else{
			totalScore -= 2*totalMovement;
		}
		return totalScore;
	}
	
	
	public static void main(String[] args) {
		if(args.length < 2){
			System.out.println("Error input, needs two parameters");
			System.out.println("Usage: Java Evaluator field_file script_file");
			return;
		}
		Evaluator eva = new Evaluator();
		try {
			eva.loadField(args[0]);
			eva.loadScript(args[1]);
		} catch (FileNotFoundException e) {
			System.err.println("Input files not found!");
			System.exit(1);
		} catch (InputFileFormatException e){
			e.printMessage();
			System.exit(1);
		}
		eva.runEvaluation();
	}
}

class InputFileFormatException extends Exception{
	private static final long serialVersionUID = -4028695614324452291L;
	String errorMessage = "";
	final static int wrongFieldLength = 1;
	final static int InvalidFieldCharacter = 2;
	final static int NotCenteredVessel = 3;
	
	InputFileFormatException(int errorCode){
		if(errorCode == wrongFieldLength){
			errorMessage = "The lines in the field file don't have the same length";
		}else if(errorCode == InvalidFieldCharacter){
			errorMessage = "The field file contains invalid character!";
		}else if(errorCode == NotCenteredVessel){
			errorMessage = "The vessel cannot be the center of the input field!";
		}
	}
	
	public void printMessage(){
		System.err.println(this.errorMessage);
	}
}
