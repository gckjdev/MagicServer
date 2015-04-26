package com.orange.game.model.dao;

public class Item {
	public static int ITEM_TYPE_TIPS = 1;
	
	public static final int ADD_GUESS_COIN_TYPE = 10001;
	
	public static final int EXPIRE_TIME_NO_CHANGE = -1;
	
	private int itemType;
	private int amount;
	private int createDate;
	private int modifyDate;
	private int expireDate;	
	
	public Item(int itemType, int amount, int createDate, int modifyDate, int expireDate) {
		super();
		this.itemType = itemType;
		this.amount = amount;
	}
	
	public int getItemType() {
		return itemType;
	}
	public void setItemType(int itemType) {
		this.itemType = itemType;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getCreateDate() {
		return createDate;
	}

	public void setCreateDate(int createDate) {
		this.createDate = createDate;
	}

	public int getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(int modifyDate) {
		this.modifyDate = modifyDate;
	}

	public int getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(int expireDate) {
		this.expireDate = expireDate;
	}

	@Override
	public String toString() {
		return "Item [amount=" + amount + ", itemType=" + itemType + "]";
	}
	
}
