package org.aiwolf.ui.log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.swing.JFrame;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Guard;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.ui.GameViewer;
import org.aiwolf.ui.res.AIWolfResource;



public class GUILogViewer {

	File logFile;
	LogGameData gameData;
	ExGame game;

	GameViewer gameLogger;
	private ContestResource contestResource;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		GUILogViewer lta = new GUILogViewer("../CEDEC2016/game/000.log.gz");
		lta.start();
	}
	
	/**
	 * 
	 * @param logFileName
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public GUILogViewer(String logFileName) throws NumberFormatException, IOException{
		this(new File(logFileName));
	}

	
	/**
	 * 
	 * @param logFile
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public GUILogViewer(File logFile) throws NumberFormatException, IOException {
		super();
		this.logFile = logFile;

	
		init();
	}

	private void init() throws FileNotFoundException, UnsupportedEncodingException, IOException {
		Map<Agent, Role> agentRoleMap = new LinkedHashMap<Agent, Role>();
		contestResource = new ContestResource();
		BufferedReader br = getBufferedReader();
		String line;
		while((line = br.readLine()) != null){
			String[] data = line.split(",");
			if(!data[1].equals("status")){
				break;
			}
			Agent agent = Agent.getAgent(Integer.parseInt(data[2]));
			Role role = Role.valueOf(data[3]);
			String name = data[5];
			agentRoleMap.put(agent, role);
			contestResource.setName(agent.getAgentIdx(), name);
		}
		br.close();
		
		GameSetting gameSetting = GameSetting.getDefaultGame(agentRoleMap.size());
		gameData = new LogGameData(gameSetting, agentRoleMap);
		game = new ExGame(gameSetting, gameData);
		gameLogger = new GameViewer(contestResource, game);
	}

	private BufferedReader getBufferedReader() throws IOException, FileNotFoundException, UnsupportedEncodingException {
		InputStream is;
		if(logFile.getName().endsWith("gz")){
			is = new GZIPInputStream(new FileInputStream(logFile)); 
		}
		else{
			is = new FileInputStream(logFile);
		}
		InputStreamReader in = new InputStreamReader(is, "UTF-8");
		BufferedReader br = new BufferedReader(in);
		return br;
	}

	public void setResource(AIWolfResource resource){
		gameLogger = new GameViewer(resource, game);
	}
	
	public AIWolfResource getResource(){
		return gameLogger.getResource();
	}
	
	public void start() throws NumberFormatException, IOException{

//		BufferedReader br = new BufferedReader(new FileReader(logFile));
		BufferedReader br = getBufferedReader();
		String line;
		while((line = br.readLine()) != null){
			gameLogger.log(line);
			String[] data = line.split(",");

			if(Integer.parseInt(data[0]) != gameData.getDay()){
				gameData = (LogGameData)gameData.nextDay();
				game.setGameData(gameData);
			}
			if(data[1].equals("status")){
				continue;
			}
			else if(data[1].equals("talk")){
				Talk talk = toTalk(data);
				gameData.addTalk(talk.getAgent(), talk);
			}
			else if(data[1].equals("whisper")){
				Talk talk = toTalk(data);
				gameData.addWisper(talk.getAgent(), talk);
			}
			else if(data[1].equals("divine")){
				Judge divine = toJudge(data);
				gameData.addDivine(divine);
			}
			else if(data[1].equals("guard")){
				Guard guard = toGuard(data);
				gameData.addGuard(guard);
			}
			else if(data[1].equals("vote")){
				Vote vote = toVote(data);
				gameData.addVote(vote);
			}
			else if(data[1].equals("attackVote")){
				Vote vote = toVote(data);
				gameData.addAttack(vote);
			}
			else if(data[1].equals("execute")){
				Agent target = Agent.getAgent(Integer.parseInt(data[2]));
				gameData.setExecuteTarget(target);
			}
			else if(data[1].equals("attack")){
				if(data[3].equals("true")){
					Agent target = Agent.getAgent(Integer.parseInt(data[2]));
					gameData.setAttackedTarget(target);
				}
			}
			else if(data[1].equals("result")){
				gameLogger.log(line);
				gameLogger.close();
				break;
			}

		}
		br.close();
	}

	protected Vote toVote(String[] data) {
		Agent agent = Agent.getAgent(Integer.parseInt(data[2]));
		Agent target = Agent.getAgent(Integer.parseInt(data[3]));
		Vote vote = new Vote(Integer.parseInt(data[2]), agent, target);
		return vote;
	}

	protected Talk toTalk(String[] data) {
		Talk talk = new Talk(Integer.parseInt(data[2]), Integer.parseInt(data[0]), Agent.getAgent(Integer.parseInt(data[3])), data[4]);
		return talk;
	}

	protected Judge toJudge(String[] data) {
		Agent target = Agent.getAgent(Integer.parseInt(data[3]));
		Judge judge = new Judge(Integer.parseInt(data[0]), Agent.getAgent(Integer.parseInt(data[2])), target, gameData.getRole(target).getSpecies());
		return judge;
	}
	
	protected Guard toGuard(String[] data) {
		Agent target = Agent.getAgent(Integer.parseInt(data[3]));
		Agent agent = Agent.getAgent(Integer.parseInt(data[2]));
		Guard guard = new Guard(Integer.parseInt(data[0]), agent, target);
		return guard;
	}

	public void setCloseOnExist(boolean b) {
		if(b){
			gameLogger.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		else{
			gameLogger.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
	}

	/**
	 * @return gameLogger
	 */
	public GameViewer getGameLogger() {
		return gameLogger;
	}

	
}