package org.aiwolf.ui.log;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.GameData;

/**
 * AIWolfGame for Log Viewer
 * @author tori
 *
 */
class ExGame extends AIWolfGame{

	public ExGame(GameSetting gameSeting, GameData gameData) {
		super(gameSeting, null);
		this.gameData = gameData;
	}

	public void setGameData(LogGameData gameData) {
		this.gameData = gameData;
	}

	public void setExecuted(Agent agent){
		this.gameData.setExecuteTarget(agent);
	}

	public void setAttacked(Agent agent){
		gameData.addLastDeadAgent(agent);

		// TODO GameData.attackedの仕様変更のため以下はなくなる予定
		this.gameData.setAttackedTarget(agent);
	}

}