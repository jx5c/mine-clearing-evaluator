
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class MineField {
	//keep track of all existing mines, sorted by their z coordinates; the highest mine first; 
	private PriorityQueue<Mine> existingMines = new PriorityQueue<Mine>(100, new Comparator<Mine>(){
		@Override
		public int compare(Mine m1, Mine m2) {
			return m2.getZ() - m1.getZ();
		}
	});
	//the initial count of mines
	private int initialMineCount;
	//a HashMap that tracks the 2d position of all mines; key = 2d position of mines; value = mines
	private Map<String, Mine> pos2DToMineMap = new HashMap<String, Mine>(); 
	//the vessel in the mine field
	Vessel vessel = null;
	
	//for printing purposes: representing the current field in String
	private String currentfieldInStr = "";
	//check if the field has been changed, if yes, recalculate the fields before printing, if no, print the existing field
	private boolean dirtyBit = true;
	/**
	 * set the position of the vessel
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setVesselStartPosition(int x, int y, int z){
		vessel = new Vessel(x, y, z);
	}
	/**
	 * when the vessel fire a volley, change the state of mines;
	 * @param pattern the pattern of firing a volley
	 */
	public void fire(String pattern){
		//fire the volley according to the input pattern
		int vessel_x = vessel.getX();
		int vessel_y = vessel.getY();
		int[][] offset = null;
		if(pattern.equals("alpha")){
			offset = new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}};
		}else if(pattern.equals("beta")){
			offset = new int[][]{{-1,0},{0,-1},{0,1},{1,0}};
		}else if(pattern.equals("gamma")){
			offset = new int[][]{{-1,0},{0,0},{1,0}};
		}else if(pattern.equals("delta")){
			offset = new int[][]{{0,-1},{0,0},{0,1}};
		}else{
			return;
		}
		for(int i=0;i<offset.length;i++){
			clearMine(vessel_x+offset[i][0], vessel_y+offset[i][1]);
		}
		//the field has been changed
		dirtyBit = true;
	}
	
	/**
	 * delete the mine object that has the corresponding x,y position
	 * @param x: x axis of the mine
	 * @param y: y axis of the mine
	 */
	private void clearMine(int x, int y){
		String posStr = Position.mk2DPositionStr(x, y);
		if(pos2DToMineMap.containsKey(posStr)){
			existingMines.remove(pos2DToMineMap.get(posStr));
			pos2DToMineMap.remove(posStr);
		}
	}
	
	/**
	 * change the position of vessel when it moves
	 * @param direction
	 */
	public void move(String direction){
		if(direction.equals("north")){
			//increment y-coordinate of ship
			vessel.setY(vessel.getY()+1);
		}else if(direction.equals("south")){
			//decrement y-coordinate of ship
			vessel.setY(vessel.getY()-1);
		}else if(direction.equals("east")){
			//increment x-coordinate of ship
			vessel.setX(vessel.getX()+1);
		}else if(direction.equals("west")){
			//decrement x-coordinate of ship
			vessel.setX(vessel.getX()-1);
		}
		vessel.setZ(vessel.getZ()-1);
		//the field has been changed
		dirtyBit = true;
	}
	
	/**
	 * print the mine field
	 */
	public void printField(){
		//for printing purposes
		String lineBreak = System.getProperty("line.separator");
		//if the field has been changed, recalculate the field before printing
		if(dirtyBit){
			currentfieldInStr = "";
			int w_half = 0;
			int h_half = 0;
			for(Mine mine : pos2DToMineMap.values()){
				//search for the width and height that can just cover the field
				w_half = Math.max(w_half, Math.abs(mine.getX()-vessel.getX()));
				h_half = Math.max(h_half, Math.abs(mine.getY()-vessel.getY()));
			}
			int width = 2*w_half+1;
			int height = 2*h_half+1;
			int topLeftX = vessel.getX() - w_half;
			int topLeftY = vessel.getY() + h_half;	
			for(int j=topLeftY;j>topLeftY - height;j--){
				String line = "";
				for(int i=topLeftX;i<width+topLeftX;i++){	
					String posStr = Position.mk2DPositionStr(i, j);
					if(pos2DToMineMap.containsKey(posStr)){
						Mine thisMine = pos2DToMineMap.get(posStr);
						line += zDistanceToChar(vessel.getZ()-thisMine.getZ());
					}else{
						line += ".";
					}
				}
				currentfieldInStr = currentfieldInStr + line + lineBreak;
			}
			currentfieldInStr = currentfieldInStr.trim();
			dirtyBit = false;
		}
		System.out.println(currentfieldInStr);
	}
	
	/**
	 * 
	 * @param distance: distance between vessel and a mine
	 * @return the corresponding character
	 */
	private char zDistanceToChar(int distance){
		char result = '.';
		if(distance <=26 && distance>=1){
			result = (char)('a'+distance-1);
		}else if(distance <=52 && distance>=27){
			result = (char)('A'+distance-27);
		}else if(distance <= 0){
			result = '*';
		}
		return result;
	}
	
	/**
	 * check if the vessel passed a mine (i.e., the z-coordinate of a mine and the vessel match)
	 * @return
	 */
	public boolean passMine(){
		if(existingMines.size() == 0){
			return false;
		}
		//the top element in the queue is the highest mine
		return existingMines.peek().getZ() >= vessel.getZ();
	}
	
	/**
	 * check if all mines are clear;
	 * @return true if all mines are clear, else false
	 */
	public boolean allMineClear(){
		return pos2DToMineMap.size() == 0;
	}
	
	/**
	 * add a mine to the field
	 * @param mine
	 */
	public void addMine(Mine mine){
		// For a specific coordinate, only one mine information is needed: the highest mine
		String posStr = Position.mk2DPositionStr(mine.getX(), mine.getY());
    	pos2DToMineMap.put(posStr, mine);	
    	existingMines.add(mine);
    	initialMineCount++;
	}
	
	public int getInitialMineCount() {
		return initialMineCount;
	}

	public static void main(String[] args){
		String test = "a"+"\n";
		System.out.println(test.trim().length());
	}
}
