package com.orange.game.model.dao.app;

public class PureDrawFreeApp extends PureDrawApp {

	public PureDrawFreeApp(String appidPureDraw, String gameIdPureDraw) {
		super(appidPureDraw, gameIdPureDraw);
	}

	@Override
	public boolean isFree() {
		return true;
	}

	@Override
	public int registrationInitIngot() {
		return 0;
	}	
	
	@Override
	public int registrationRewardIngot() {
		return 0;
	}
	
}
