var i = 0;
for ( i =0; i < 10; i++ ) {
	var day = 1;
	db.action.insert({c_date: new Date(2012, 8, day)});
	day++;
}

for ( i =0; i < 37; i++ ) {
	db.action.insert({c_date: new Date()});
}

function cal_total_drawing_today() {
	var today = new ISODate();
	var count = 0;
	var cursor = db.action.find();
	while ( cursor.hasNext() ) {
		var x = cursor.next();
		if(x.c_date != undefined) {
			if ( x.c_date < today )
				continue;
			count++;
		}
	}
	return cursor;
}

function insert_daily_stat(today_drawing_count) {
	db.daily_stat.insert({total_draw: today_drawing_count})
}

today_drawing_count = cal_total_drawing_today();
insert_daily_stat(today_drawing_count);