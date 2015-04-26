package com.orange.game.traffic.model.dao;

import java.util.concurrent.ConcurrentLinkedQueue;

@Deprecated
public class SeatList {

	ConcurrentLinkedQueue<Integer> emptySeats = new ConcurrentLinkedQueue<Integer>();
	
	final int seatNumber;
	
	public SeatList(int seatNumber){
		this.seatNumber = seatNumber;
		reset();
	}
	
	public void reset(){
		emptySeats.clear();
		for (int i=0; i<seatNumber; i++){
			emptySeats.offer(i+1);
		}
	}
	
	public int allocSeat(){		
		Integer seatId = emptySeats.poll();
		if (seatId == null)
			return -1;
		else {			
			return seatId.intValue();
		}
	}
	
	public void deallocSeat(int seatId){
		emptySeats.offer(seatId);
	}		
		
}
