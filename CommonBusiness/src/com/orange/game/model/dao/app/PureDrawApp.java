package com.orange.game.model.dao.app;


public class PureDrawApp extends LearnDrawApp {

	public PureDrawApp(String appid, String gameid) {
		super(appid, gameid);
	}

	@Override
	public boolean isFree() {
		return false;
	}

	@Override
	public int registrationRewardIngot() {
		return 0;
	}

	@Override
	public int registrationRewardCoin() {
		return 0;
	}

	@Override
	public int registrationInitIngot() {
		return 0;
	}

	@Override
	public int registrationInitCoin() {
		return 0;
	}

}
