
//插入contest数据，支持评委等信息，新年唱歌比赛
db.contest.insert({
            "_id" : ObjectId("988888888888888820150425"),
            "cate" : 0,
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "图说流行语",
            "contest_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150425_contest.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150425_rule.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2015-04-24T16:00:00Z"),
            "e_date" : ISODate("2015-05-05T16:00:00Z"),
            "vote_start_date" : ISODate("2015-04-24T16:00:00Z"),
            "vote_end_date" : ISODate("2015-05-06T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150425_contest.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150425_rule.jpg",
            "contestants_only" : false,
            "contestants": [],
            "judges" : [ "4fc3089a26099b2ca8c7a4ab", "4f95717e260967aa715a5af4" ],
            "reporters" : [],
            "anonymous" : true,
            "max_flower_per_opus" : 3,
            "max_flower_per_contest" : 3000,
            "submit_count" : 1,
            "flower_rank_weight" : 3,
            "judge_rank_weight" : 60,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "特别创意奖", "3" : "最具潜力奖"},
            "status" : 2,
            "group" : false,
            "award_rules" : [
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
                ],
            "award_total" : 51500

            });



//插入contest数据，支持评委等信息，新年唱歌比赛
db.contest.insert({
            "_id" : ObjectId("988888888888888820150320"),
            "cate" : 0,
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "图说流行语",
            "contest_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150320_contest.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150320_rule.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2015-03-19T16:00:00Z"),
            "e_date" : ISODate("2015-03-30T16:00:00Z"),
            "vote_start_date" : ISODate("2015-03-19T16:00:00Z"),
            "vote_end_date" : ISODate("2015-03-31T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150320_contest.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150320_rule.jpg",
            "contestants_only" : false,
            "contestants": [],
            "judges" : [],
            "reporters" : [],
            "anonymous" : true,
            "max_flower_per_opus" : 3,
            "max_flower_per_contest" : 3000,
            "submit_count" : 1,
            "flower_rank_weight" : 3,
            "judge_rank_weight" : 60,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "特别创意奖", "3" : "最具潜力奖"},
            "status" : 2,
            "group" : false,
            "award_rules" : [
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
                ],
            "award_total" : 51500

            });




db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{judges:"50f4ad58e4b05bb0f07e9a08"}});
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{judges:"5087b337e4b0e39b1782d683"}});
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{judges:"50d5ca23e4b0d73d234e6bbb"}});
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{judges:"522363fae4b030c9cfe85f6a"}});


db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{reporters:"517e7461e4b0a2a6ee6e8d59"}});
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{reporters:"51021f70e4b0a04f9ebc8ed5"}});
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{reporters:"51ef4440e4b0705e61162244"}});
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{reporters:"53f9e20de4b013e9b6b8fc1a"}});
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{reporters:"52bcde6ee4b035f5ca92544b"}});

db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{reporters:"54733c18e4b0b269e60451f1"}});
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{reporters:"5427e766e4b07f0fae62c90a"}});

db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{reporters:"518d0fcfe4b0d2e09c021b31"}});

db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$addToSet:{reporters:"518d0fcfe4b0d2e09c021b31"}});


db.contest.update({"_id" : ObjectId("988888888888888820150320")},{$addToSet:{judges:"4fc3089a26099b2ca8c7a4ab"}});
db.contest.update({"_id" : ObjectId("988888888888888820150320")},{$addToSet:{judges:"4f95717e260967aa715a5af4"}});




51021f70e4b0a04f9ebc8ed5
51ef4440e4b0705e61162244
53f9e20de4b013e9b6b8fc1a
52bcde6ee4b035f5ca92544b





db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$addToSet:{judges:"888888888888888888888888"}});

db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$addToSet:{judges:"51191487e4b098c397bc56be"}});



db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$addToSet:{reporters:"517e7461e4b0a2a6ee6e8d59"}});
db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$addToSet:{reporters:"542aa870e4b01eb7e75ae5ab"}});
db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$addToSet:{reporters:"53bd0a17e4b0b9979e5d576f"}});


db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$addToSet:{reporters:"53661e00e4b0b4abf0e80ae1"}});



db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$addToSet:{judges:"522446880364908b37cc137d"}});

db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$addToSet:{reporters:"502c3e422609804d43083768"}});
db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$addToSet:{reporters:"5427e766e4b07f0fae62c90a"}});

db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$pull:{reporters:"502c3e422609804d43083768"}});
db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$pull:{reporters:"50f0f98ae4b07039cd242951"}});
db.contest.update({"_id" : ObjectId("988888888888888820150123")},{$pull:{reporters:"50f0f98ae4b07039cd242951"}});

//插入contest数据，支持评委等信息，新年唱歌比赛
db.contest.insert({
            "_id" : ObjectId("988888888888888820150214"),
            "cate" : 0,
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "羊年快乐",
            "contest_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150214_contest.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150214_rule.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2015-02-13T16:00:00Z"),
            "e_date" : ISODate("2015-02-24T16:00:00Z"),
            "vote_start_date" : ISODate("2015-02-13T16:00:00Z"),
            "vote_end_date" : ISODate("2015-02-25T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150214_contest.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820150214_rule.jpg",
            "contestants_only" : false,
            "contestants": [],
            "judges" : [],
            "reporters" : [],
            "anonymous" : true,
            "max_flower_per_opus" : 3,
            "max_flower_per_contest" : 3000,
            "submit_count" : 1,
            "flower_rank_weight" : 3,
            "judge_rank_weight" : 60,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "特别创意奖", "3" : "最具潜力奖"},
            "status" : 2,
            "group" : false,
            "award_rules" : [
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
                ],
            "award_total" : 51500

            });

// 设置为未开始
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$set:{status:1}});

// 设置为开始
db.contest.update({"_id" : ObjectId("988888888888888820150214")},{$set:{status:1}});

db.setProfilingLevel(1);
db.system.profile.find( { millis : { $gt : 100 } } );

db.user.ensureIndex({"xiaoji" : 1})
db.user.ensureIndex({"device_id" : 1})

db.user.ensureIndex({"device_ids" : 1})

db.user.ensureIndex({"email" : 1})
db.user.ensureIndex({"level_draw.level" : 1})
db.user.ensureIndex({"level_sing.level" : 1})
db.user.ensureIndex({"nick_name" : 1})

db.user.ensureIndex({"sina_id" : 1})
db.user.ensureIndex({"qq_id" : 1})

db.user.ensureIndex({"is_robot":1})
db.user.ensureIndex({"balance":-1})
db.user.ensureIndex({"v_date":-1})
db.user.ensureIndex({"c_date":-1})
db.user.ensureIndex({"app_list":-1})

db.user.findOne({nick_name:"💀OP💫little-begin'元酱💀"})

db.user.insert({
	"_id" : ObjectId("53b9f662e4b0642d70baad71"),
	"app_id" : "513819630",
	"nick_name" : "Somebody 38389",
	"device_model" : "iPhone",
	"device_id" : "AB37FDF0-F963-4644-A2BF-C6BB2307C3F2",
	"device_os" : "iPhone OS_7.1.2",
	"device_token" : "",
	"language" : "zh-Hans",
	"country_code" : "CN",
	"c_date" : ISODate("2014-03-18T08:00:33.098Z"),
	"source_id" : "513819630",
	"xiaoji" : "139143176",
	"status" : "1",
	"shake_xiaoji" : false,
	"version" : "8.3",
	"cal_take_coins" : true,
	"items" : [
		{
			"type" : 1,
			"amount" : 10
		},
		{
			"type" : 1101,
			"amount" : 1
		},
		{
			"type" : 1102,
			"amount" : 1
		},
		{
			"type" : 1103,
			"amount" : 1
		},
		{
			"type" : 1104,
			"amount" : 1
		},
		{
			"type" : 1105,
			"amount" : 1
		},
		{
			"type" : 1106,
			"amount" : 1
		},
		{
			"type" : 1401,
			"amount" : 1
		},
		{
			"type" : 1402,
			"amount" : 1
		},
		{
			"type" : 1406,
			"amount" : 1
		},
		{
			"type" : 1407,
			"amount" : 1
		}
	],
	"level_draw" : { "experience" : 1, "level" : 1 },
	"level_sing" : { "experience" : 1, "level" : 1 },
	"app_list" : [
		"513819630"
	],
	"balance" : 500,
	"ingot_balance" : 0
})


db.user.insert({
	"_id" : ObjectId("53b9f662e4b0642d70baad71"),
	"app_id" : "513819630",
	"nick_name" : "Somebody 38389",
	"device_model" : "iPhone",
	"device_id" : "AB37FDF0-F963-4644-A2BF-C6BB2307C3F2",
	"device_os" : "iPhone OS_7.1.2",
	"device_token" : "",
	"language" : "zh-Hans",
	"country_code" : "CN",
	"c_date" : ISODate("2014-03-18T08:00:33.098Z"),
	"source_id" : "513819630",
	"xiaoji" : "139143176",
	"status" : "1",
	"shake_xiaoji" : false,
	"version" : "8.3",
	"cal_take_coins" : true,
	"items" : [
		{
			"type" : 1,
			"amount" : 10
		},
		{
			"type" : 1101,
			"amount" : 1
		},
		{
			"type" : 1102,
			"amount" : 1
		},
		{
			"type" : 1103,
			"amount" : 1
		},
		{
			"type" : 1104,
			"amount" : 1
		},
		{
			"type" : 1105,
			"amount" : 1
		},
		{
			"type" : 1106,
			"amount" : 1
		},
		{
			"type" : 1401,
			"amount" : 1
		},
		{
			"type" : 1402,
			"amount" : 1
		},
		{
			"type" : 1406,
			"amount" : 1
		},
		{
			"type" : 1407,
			"amount" : 1
		}
	],
	"app_list" : [
		"513819630"
	],
	"balance" : 500,
	"level_draw" : {
    		"experience" : NumberLong(1),
    		"level" : NumberLong(1)
    	},
    	"level_info" : [
    		{
    			"experience" : NumberLong(1),
    			"level" : NumberLong(1),
    			"source_id" : "Draw"
    		},
    		{
    			"source_id" : "Dice",
    			"level" : 1,
    			"experience" : NumberLong(0)
    		},
    		{
    			"source_id" : "Zhajinhua",
    			"level" : 1,
    			"experience" : NumberLong(0)
    		}
    	],
    	"level_sing" : {
    		"experience" : 1,
    		"level" : 1
    	},
	"ingot_balance" : 0
})

db.user.insert({
               	"_id" : ObjectId("53b9f662e4b0642d70baad71"),
               	"xiaoji" : "139143176",
               	"app_id" : "513819630",
               	"app_list" : [
               		"513819630"
               	],
               	"balance" : 500,
               	"c_date" : ISODate("2014-07-07T06:37:16.979Z"),
               	"cal_take_coins" : true,
               	"country_code" : "CN",
               	"device_id" : "",
               	"device_model" : "iPhone",
               	"device_os" : "iPhone OS_7.1.2",
               	"device_token" : "",
               	"ingot_balance" : 0,
               	"items" : [
               		{
               			"type" : 1,
               			"amount" : 10
               		},
               		{
               			"type" : 1101,
               			"amount" : 1
               		},
               		{
               			"type" : 1102,
               			"amount" : 1
               		},
               		{
               			"type" : 1103,
               			"amount" : 1
               		},
               		{
               			"type" : 1104,
               			"amount" : 1
               		},
               		{
               			"type" : 1105,
               			"amount" : 1
               		},
               		{
               			"type" : 1106,
               			"amount" : 1
               		},
               		{
               			"type" : 1300,
               			"amount" : 1
               		},
               		{
               			"type" : 1401,
               			"amount" : 1
               		},
               		{
               			"type" : 1402,
               			"amount" : 1
               		},
               		{
               			"type" : 1403,
               			"amount" : 1
               		},
               		{
               			"type" : 1404,
               			"amount" : 1
               		},
               		{
               			"type" : 1406,
               			"amount" : 1
               		},
               		{
               			"type" : 1407,
               			"amount" : 1
               		},
               		{
               			"type" : 1408,
               			"amount" : 1
               		},
               		{
               			"type" : 1409,
               			"amount" : 1
               		}
               	],
               	"language" : "zh-Hans",
               	"nick_name" : "-5S",
               	"shake_xiaoji" : false,
               	"source_id" : "513819630",
               	"status" : "1",
               	"version" : "9.11",
               }
);


app_list


//    db.user.find({c_date:{$gt:ISODate("2014-03-21T16:25:40.963Z")}, "xiaoji":{$exists:false}}).count()
//    db.user.find({c_date:{$gt:ISODate("2014-03-21T16:25:40.963Z")}, "xiaoji":null}).count()
    
    
//    db.user.find({c_date:{"$gt":ISODate("2014-01-01T16:25:40.963Z"), "$lt":ISODate("2014-02-01T16:25:40.963Z")}}).count()
//
//    db.user.find({c_date:{"$gt":ISODate("2013-12-01T16:25:40.963Z"), "$lt":ISODate("2014-01-01T16:25:40.963Z")}}).count()
//    
//     db.user.find({c_date:{"$gt":ISODate("2013-12-01T16:25:40.963Z"), "$lt":ISODate("2014-01-01T16:25:40.963Z")}, "xiaoji":null}).count()
//        
//    db.user.find({c_date:{"$gt":ISODate("2013-12-01T16:25:40.963Z"), "$lt":ISODate("2014-01-01T16:25:40.963Z")}, v_date:{$gt:ISODate("2014-01-01T16:25:40.963Z"), $lt:ISODate("2014-02-01T16:25:40.963Z")}}).count()
                       s

//add robot d

db.user.insert({"_id":ObjectId("999999999999999999999000"), "nick_name":"Lily@Moon", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/120504/co120504104A9-1-lp.jpg", "gender":"f", "location":"UK", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }] });

db.user.insert({"_id":ObjectId("999999999999999999999001"), "nick_name":"LikeAFox", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/120407/co12040FZ942-5-lp.jpg", "gender":"m", "location":"LA USA", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999002"), "nick_name":"Tina", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/111107/3-11110H30558.jpg", "gender":"f", "location":"New York, USA", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999003"), "nick_name":"Hugo", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/120416/co120416093105-0-lp.jpg", "gender":"m", "location":"UK", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999004"), "nick_name":"Jan Vans", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/120421/co120421091P1-6-lp.jpg", "gender":"m", "location":"LA USA", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999005"), "nick_name":"Vivian", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160G647.jpg", "gender":"f", "location":"UK", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999006"), "nick_name":"Johnson", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/120421/co120421091P1-5-lp.jpg", "gender":"m", "location":"New York, USA", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999007"), "nick_name":"Allen J", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/120404/co120404100521-6-lp.jpg", "gender":"f", "location":"LA USAJ", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999008"), "nick_name":"Miaotiao", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160G648.jpg", "gender":"f", "location":"UK", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999009"), "nick_name":"Julie", "avatar":"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160G645-50.jpg", "gender":"f", "location":"UK", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999010"), "nick_name":"Annetta", "avatar":"http://jic.gexing.com/i/t/touxiang/8/2.jpg", "gender":"f", "location":"Toronto", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999011"), "nick_name":"Shirley", "avatar":"http://jic.gexing.com/i/t/touxiang/15/2.jpg", "gender":"f", "location":"HK", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999012"), "nick_name":"Linus", "avatar":"http://www.touxiang.cn/uploads/allimg/111028/002400HP-0-lp.jpg", "gender":"m", "location":"Guangzhou", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999013"), "nick_name":"CoolMan", "avatar":"http://www.touxiang.cn/uploads/allimg/111102/1251435c7-0-lp.jpg", "gender":"m", "location":"Warszawa", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999014"), "nick_name":"JiaJunpeng", "avatar":"http://www.touxiang.cn/uploads/allimg/c111103/1320302G3140-63L45_lit.jpg", "gender":"m", "location":"Nanjing", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999015"), "nick_name":"LoveKakashi", "avatar":"http://wenwen.soso.com/p/20081127/20081127134154-1778131557.jpg", "gender":"m", "location":"Shenzhen", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999016"), "nick_name":"aiyuanyuan", "avatar":"http://news.xinhuanet.com/video/2008-03/25/xin_032030525110915616363.jpg", "gender":"m", "location":"Xi'an", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999017"), "nick_name":"Kathie", "avatar":"http://p3.qqgexing.com/touxiang/20120824/0831/5036cb4715af7.jpg", "gender":"f", "location":"Melbourne", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999018"), "nick_name":"Lolita", "avatar":"http://list.image.baidu.com/t/image_category/galleryimg/womenstar/us/bu_lan_ni.jpg", "gender":"f", "location":"Shanghai", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999019"), "nick_name":"kiss", "avatar":"http://www.touxiang.cn/uploads/20120829/29-054441_116-lp.jpg", "gender":"f", "location":"Chengdu", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999020"), "nick_name":"Taylor", "avatar":"http://www.touxiang.cn/uploads/20120829/29-014707_935-lp.jpg", "gender":"f", "location":"Soul", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999021"), "nick_name":"Shira", "avatar":"http://www.touxiang.cn/uploads/20120903/03-061347_802-lp.jpg", "gender":"f", "location":"Chongqing", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999022"), "nick_name":"Love", "avatar":"http://www.touxiang.cn/uploads/20120904/04-021612_601-lp.jpg", "gender":"f", "location":"Chongqing", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999023"), "nick_name":"guakenan", "avatar":"http://list.image.baidu.com/t/image_category/galleryimg/face/cartoon/ke_nan.jpg", "gender":"m", "location":"Beijing", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999024"), "nick_name":"I'm muscle", "avatar":"http://www.touxiang.cn/uploads/20120830/30-020117_52-lp.jpg", "gender":"m", "location":"Heilongjiang", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999025"), "nick_name":"shyane", "avatar":"http://list.image.baidu.com/t/image_category/galleryimg/menstar/ml/liu_ye.jpg", "gender":"m", "location":"Qingdao", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999026"), "nick_name":"Patti", "avatar":"http://p4.qqgexing.com/touxiang/20120906/1648/504863636ef3f.jpg", "gender":"f", "location":"Houston", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999027"), "nick_name":"Fanny", "avatar":"http://p3.qqgexing.com/touxiang/20120829/1350/503dad9a807e9.jpg", "gender":"f", "location":"Paris", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999028"), "nick_name":"Nancy", "avatar":"http://p2.qqgexing.com/touxiang/20120906/1748/5048716a15fd0.jpg", "gender":"f", "location":"Hangzhou", "is_robot":1,"level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});

db.user.insert({"_id":ObjectId("999999999999999999999029"), "nick_name":"Aixuan", "avatar":"http://p4.qqgexing.com/touxiang/20120907/1035/50495d6a38ae0.jpg", "gender":"f", "location":"Xiamen", "is_robot":1, "level_info" : [ { "source_id" : "Draw", "level" : 1, "experience" : NumberLong(0) }, { "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }]});


// 设置用户封号权限
db.user.update({xiaoji:"139866388"},{$set:{permissions:["black_user"]}})

// 过期expire索引
db.black_board_user.ensureIndex({v_date:1}, {expireAfterSeconds:15})


// draw table index
db.draw.ensureIndex({create_date: -1, data_len:-1, avatar:1});
db.draw.ensureIndex({random :1,lang:1});

db.action.ensureIndex({type :1,create_uid:1, user_list:1, opus_creator_uid:1, correct:1});
db.action.ensureIndex({match_times :1});
db.action.ensureIndex({hot :1});
db.action.ensureIndex({random :1,lang:1});
db.action.ensureIndex({random :1,lang:1});
db.action.ensureIndex({random :1,lang:1});
db.action.ensureIndex({random :1,lang:1});

// user table index
db.user.ensureIndex({device_id:1},{background:true});
db.user.ensureIndex({email:1, nick_name:1, sina_id:1, qq_id:1, facebook_id:1, sina_nick:1, qq_nick:1},{background:true});
db.user.ensureIndex({"fans.fid":1},{background:true});
db.user.ensureIndex({"follows.fid":1},{background:true});

// room table index
db.room.ensureIndex({room_name:1, nick_name:1});
db.userroom.ensureIndex({room_user_id:1, room_type:1});


//查询IAP欺诈用户，锦囊
db.user.find({"items.type":1, "items.amount":{"$gte":50}}).count()
db.user.update({"items.type":1, "items.amount":{"$gte":50}}, {$set:{"items" : [ { "type" : 1, "amount" : 5 }]}}, false, true)


//查询IAP欺诈用户，金额
db.user.find({"balance":{"$gte":5000}}).count()
db.user.update({"balance":{$gte:5000}}, {$set:{balance:1}}, false, true)

//移除action表的所有索引
db.runCommand({dropIndexes:'action', index : ""})


//获取离线作品
db.action.ensureIndex({language:1, type:1, opus_status:1, guess_times:1,data_len:-1},{name:"offlineOpus"});
db.action.ensureIndex({data_len:1, avatar:1, c_date:-1, opus_status:1},{name:"recentOpus"});
db.action.ensureIndex({target_uid:1, type:1, opus_status:1},{name:"drawToMeOpus",background:true});

//动态列表,change by Benson, no index is the best index sometimes... only use {_id:-1}
db.action.ensureIndex({type:1,opus_status :1, create_uid:1, opus_creator_uid:1, target_uid:1},{name:"feedList"});
db.timeline.ensureIndex({owner:1},{name:"AllFeedList"});
db.timeline.ensureIndex({timeline_count:-1},{name:"sortTimlineCount"});

db.comment.ensureIndex({owner:1},{name:"AllCommentList"});
db.comment.ensureIndex({comment_count:-1},{name:"sortCommentCount"});

//新动态数
db.action.ensureIndex({type :1, opus_status:1, c_date:1,opus_creator_uid:1, target_uid:1,create_uid:1},{name:"feedListCount"});

//最新动态


//热榜
db.action.ensureIndex({hot : -1, type:1,  opus_status:1, language:1},{name:"hotFeedList"});
//查询用户评论
db.action.ensureIndex({opus_id:1, opus_status:1, type:1, has_words:1},{name:"commentList"});

//初始化没有等级用户的数据
db.user.update({level_info:{$exists:false}}, {$set:{level_info:[{ "source_id" : "Draw", "experience" : NumberLong(0), "level" : 1 } ]}}, false, true)


//一次性更新用户等级信息
//var cursor=db.user.find({_id:ObjectId("50d64b76e4b0d73d234e7948")});
var cursor=db.user.find();
var count=0;
var update_count = 0;
while(cursor.hasNext()){
 var x=cursor.next();
 count++;
   if(x.level_info != undefined){
        var old;
        var size = x.level_info.length;
     //for (old in x.level_info){
     for (i=0; i<size; i++){
        old = x.level_info[i];
       if (old["source_id"] == "Draw"){
         x.level_draw = {};
         x.level_draw["level"] = old["level"];
         x.level_draw["experience"] = old["experience"];
       }
       else if (old["source_id"] == "Dice"){
         x.level_dice = {};
         x.level_dice.level = old["level"];
         x.level_dice.experience = old["experience"];
       }
       else if (old["source_id"] == "Zhajinhua"){
         x.level_zhajinhua = {};
         x.level_zhajinhua.level = old["level"];
         x.level_zhajinhua.experience = old["experience"];
       }
     }

     db.user.update({_id:x._id},{$set:{"level_draw":x.level_draw, "level_dice":x.level_dice, "level_zhajinhua":x.level_zhajinhua}});
     update_count++;
   }

  if (count%100 == 0){
    print("total "+count+" processed, total "+update_count+" updated");
  }
}

print("completed! total "+count+" processed, total "+update_count+" updated");



//一次性更新热榜得分。
var cursor=db.action.find({"type":{"$in":[1,5]}});
while(cursor.hasNext()){
 var x=cursor.next();
   if(x.guess_times == undefined){
	x.guess_times = 0;
   }
   if(x.correct_times == undefined){
	x.correct_times = 0;
   }
   if(x.comment_times == undefined){
	x.comment_times = 0;
   }
   if(x.data_len == undefined){
      x.data_len = 1200;
   }
   var value = x.guess_times+x.correct_times*2+x.comment_times+x.data_len*0.0007;
   var l = Math.log(value)/Math.log(1.1);
   x.hot=x.c_date.getTime()/(3600000.0 * 2.0) + l;
   db.action.update({_id:x._id},x);	
}

//一次性更新用户相关用户ID
var cursor=db.action.find({"type":{"$in":[1,2,5]}});
while(cursor.hasNext()){
   var x=cursor.next();
   if (x.related_uid == undefined) {
   	var related_uid_list = [];
   	if (x.create_uid != undefined) {
   		related_uid_list.push(x.create_uid);
   	}
   	if (x.opus_creator_uid != undefined) {
   		related_uid_list.push(x.opus_creator_uid);
   	}
   	if (x.target_uid != undefined) {
   		related_uid_list.push(x.target_uid);
   	}
   	x.related_uid = related_uid_list;
   	db.action.update({
   		_id: x._id
   	}, x);
   }
}


//把恶意用户作品刷走
db.action.findOne({"_id":ObjectId("4ff7797884a693466354875d")});
db.action.update({"_id":ObjectId("4ff7797884a693466354875d")},{"$set":{"data_len":1,"hot":0}})

//将经验数据里面的source_id统一变成Draw
db.user.update({"level_info.source_id":"Game"},{"$set":{"level_info.$.source_id":"Draw"}},false,true)
db.user.update({"level_info.source_id":"513819630"},{"$set":{"level_info.$.source_id":"Draw"}},false,true)

//给老用户送十朵花十个番茄
 db.user.update({},{"$pushAll":{"items":[{"type":6,"amount":10},{"type":7,"amount":10}]}},"false", "true");

//构建未读的消息
db.message.update({reciever_del_flag:null},{$set:{reciever_del_flag:0}},false,true)
db.message.update({sender_del_flag:null},{$set:{sender_del_flag:0}},false,true)

//插入海报

//device_type 表示支持什么设备，1是iphone，2是ipad
//game_id 表示支持哪些游戏
//type 
//	public static int BoardTypeDefault = 1;
//	public static int BoardTypeWeb = 2;
//	public static int BoardTypeAd = 3;
//	public static int BoardTypeImage = 4;


//插入默认海报
db.board.insert({"_id" : ObjectId("50402f5bc592ec3bb7580327") ,"device_type" : [ 2 , 1] ,  "status" : 1 , "version" : "1" , "type" : 1});
//插入本地web海报(需替换remote_url)
db.board.insert({"_id" : ObjectId("50402f5bc592ec3bb7580328") , "device_type" : [ 2 , 1] ,  "status" : 1 , "version" : "2.0" , "type" : 2 , "web_type" : 1 , "remote_url" : "http://192.167.1.123/test.zip"});
//插入远程web海报(需替换remote_url)
db.board.insert({"_id" : ObjectId("50402f5bc592ec3bb7580329"),  "device_type" : [ 2 , 1] ,  "status" : 1 , "version" : "1" , "type" : 2 , "web_type" : 2 , "remote_url" : "http://192.167.1.123"});
//插入图片海报(需替换image和click_url)

db.board.insert({"index" : 0 , "device_type" : [ 2 , 1] , "game_id" : [ "Dice" , "Draw"] , "status" : 1 , "version" : "1" , "type" : 4 , "image" : "http://place100.com:8080/draw_image/20120816/62fa4ae0-e7b6-11e1-90f6-00163e017466.jpg" , "click_url" : "tgb://board?type=1&game=draw&func=feed"});
//插入广告海报(需替换ad_list)
db.board.insert({"index" : 3 , "status" : 1 , "version" : "1" , "type" : 3 , "device_type" : [ 2 , 1] , "game_id" : [ "Dice" , "Draw"] , "ad_list" : [ { "platform" : 2 , "publish_id" : "3b47607e44f94d7c948c83b7e6eb800e"} , { "platform" : 1 , "publish_id" : "eb4ce4f0a0f1f49b6b29bf4c838a5147"}] , "ad_number" : 2});


//猜猜画画海报初始化数据

//欢乐大话骰海报初始化数据

db.board.insert({"_id" : ObjectId("50402f5bc592ec3bb758032a"),  "device_type" : [ 2 , 1] ,  "status" : 1 , "version" : "1" , "type" : 3 , "image" : "http://192.167.1.123/BoardControl/image.png" , "ad_image":"http://192.167.1.123/BoardControl/ad_image.png","click_url" : "tgb://board?type=1&game=draw&func=feed","publish_id":"3b47607e44f94d7c948c83b7e6eb800e","platform":2});
db.board.insert({"_id" : ObjectId("50402f5bc592ec3bb758032a"),  "device_type" : [ 2 , 1] ,  "status" : 1 , "version" : "1" , "type" : 3 , "image" : "http://192.167.1.123/BoardControl/image.png" , "ad_image":"http://192.167.1.123/BoardControl/ad_image.png","click_url" : "tgb://board?type=1&game=draw&func=feed","publish_id":"eb4ce4f0a0f1f49b6b29bf4c838a5147","platform":2});



//向game_board插入数据，定义了海报在哪个游戏中的index
db.game_board.insert({"board_id":"50402f5bc592ec3bb7580327", "index" : -1, "game_id" :  "Draw"  })
db.game_board.insert({"board_id":"50402f5bc592ec3bb758032a", "index" : 0, "game_id" :  "Draw"  })
db.game_board.insert({"board_id":"50402f5bc592ec3bb7580328", "index" : 1, "game_id" :  "Draw"  })

db.game_board.insert({"board_id":"50402f5bc592ec3bb7580327", "index" : -1, "game_id" : "Dice" })
db.game_board.insert({"board_id":"50402f5bc592ec3bb7580329", "index" : 0, "game_id" :  "Dice"  })
db.game_board.insert({"board_id":"50402f5bc592ec3bb758032a", "index" : 1, "game_id" :  "Dice" })

db.board.ensureIndex({status:1, device_type:1},{name:"BoardIndex"});
db.game_board.ensureIndex({game_id:1, board_id:1},{name:"BoardIndexInGame"});


// 欢乐大话骰，海报初始化数据
db.board.insert({"_id" : ObjectId("888888888888888888880000"),  "device_type" : [ 2 , 1] ,  "status" : 1 , "version" : "1" , "type" : 1});
db.board.insert({"_id" : ObjectId("888888888888888888880001"),  "device_type" : [ 2 , 1] ,  "status" : 1 , "version" : "1" , "type" : 3 , "image" : "http://www.you100.me:8080/board/image/draw_en.png" , "ad_image":"http://www.you100.me:8080/board/image/draw_ad_en.png", "cn_image" : "http://www.you100.me:8080/board/image/draw.png" , "cn_ad_image":"http://www.you100.me:8080/board/image/draw_ad.png","click_url" : "http://phobos.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=513819630&mt=8", "publish_id":"","platform":0});
db.game_board.insert({"board_id":"888888888888888888880000", "index" : -1, "game_id" : "Dice" })
db.game_board.insert({"board_id":"888888888888888888880001", "index" : 0, "game_id" :  "Dice"  })

// 猜猜画画
db.board.insert({"_id" : ObjectId("888888888888888888881000"),  "device_type" : [ 2 , 1] ,  "status" : 1 , "version" : "1" , "type" : 1});
db.board.insert({"_id" : ObjectId("888888888888888888881001"),  "device_type" : [ 2 , 1] ,  "status" : 1 , "version" : "1" , "type" : 3 , "image" : "http://www.you100.me:8080/board/image/dice_default_board_en@2x.jpg" , "ad_image":"http://www.you100.me:8080/board/image/dice_default_board_ad_en@2x.jpg", "cn_image" : "http://www.you100.me:8080/board/image/dice_default_board@2x.jpg" , "cn_ad_image":"http://www.you100.me:8080/board/image/dice_default_board_ad@2x.jpg", "click_url" : "http://phobos.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=513819630&mt=8", "publish_id":"","platform":0});
db.game_board.insert({"board_id":"888888888888888888881000", "index" : -1, "game_id" : "Draw" })
db.game_board.insert({"board_id":"888888888888888888881001", "index" : 0, "game_id" :  "Draw"  })

//给所有用户增加大话骰经验数据
db.user.update({},{"$push":{"level_info":{ "source_id" : "Dice", "level" : 1, "experience" : NumberLong(0) }}},false,true)
db.user.update({},{"$push":{"level_info":{ "source_id" : "Zhajinhua", "level" : 1, "experience" : NumberLong(0) }}},false,true)


db.his_items.ensureIndex({uid:1,type:1},{name:"findItemByUid",background:true});

// contest index
db.action.ensureIndex({contest_id:1,opus_status:1,language:1,contest_score:-1},{name:"getContestOpusIndex",background:true});
db.action.ensureIndex({contest_id:1,opus_status:1,c_date:-1},{name:"getNewContestOpusIndex1",background:true});
db.action.ensureIndex({contest_id:1,create_uid:1,opus_status:1,id:-1},{name:"getMyContestOpusIndex",background:true});

db.action.ensureIndex({contest_id:1,opus_status:1,language:1,_id:-1,contest_score:-1},{name:"contestOpusIndex",background:true});

//BBS 数据库索引
db.bbs_board.ensureIndex({"index":1, "game_id":1},{name:"getBoardIndex"}); 
db.bbs_post.ensureIndex({"board_id":1, "status":1, "m_date":-1, "action_count":-1},{name:"getBoardPostIndex"}); 
db.bbs_post.ensureIndex({"c_user.uid":1, "status":1},{name:"getUserPostIndex"}); 
db.bbs_action.ensureIndex({"action_source.postId":1, "type":1, "status":1},{name:"getPostActionIndex"}); 
db.bbs_action.ensureIndex({"action_source.post_uid":1, "action_source.action_uid":1, "status":1},{name:"getUserActionIndex"}); 

//删除恶意用户所扔的所有番茄
db.action.update({create_uid:"5029f5fd2609596f142cebe8",type:7},{$set:{opus_status:1}},false,true)
//删除恶意用户所扔的所有鲜花
db.action.update({create_uid:"5029f5fd2609596f142cebe8",type:6},{$set:{opus_status:1}},false,true)
//将一幅作品的番茄数目重置为0
db.action.update({_id:ObjectId("5059ae0b84a615835c56a3eb")},{$set:{tomato_times:0}})
//将用户的等级和金币都清空
db.user.update({_id:ObjectId("5029f9862609596f142ced3a")},{$set:{balance:0,	"level_info" : [
		{
			"experience" : NumberLong(11),
			"level" : 1,
			"source_id" : "Draw"
		},
		{
			"source_id" : "Dice",
			"level" : 1,
			"experience" : NumberLong(0)
		}
	]}})

	
//一次性更新用户排名
var cursor=db.user.find();
while(cursor.hasNext()){
   var x=cursor.next();
   if (x.level_info != undefined && x.level_info[0] != undefined) {
	   x.draw_rank_score = x.level_info[0].experience;	   
   }else{
	   x.draw_rank_score = 0;
   	db.user.update({
   		_id: x._id
   	}, x);
   }
}

//一次性将用户的关系转移到relation表
var cursor=db.user.find();
while(cursor.hasNext()){
   var x=cursor.next();
   if(x.follows != undefined) {
     var uid = x._id;
     var i = 0;
     for(var y in x.follows){
		var fid= ObjectId(x.follows[i].fid);
          var c_date = x.follows[i].c_date;
          var source_id = x.follows[i].source_id;
          db.relation.insert({"uid":uid,"fid":fid,"c_date":c_date,"source_id":source_id,type:1});
          ++i;
     }
   }
}
//为relation表建立索引。
db.relation.ensureIndex({uid:1,type:1,c_date:-1},{name:"followIndex",background:true})
db.relation.ensureIndex({fid:1,type:1,c_date:-1},{name:"fanIndex",background:true})


//Message_stat Indexes
db.message_stat.ensureIndex({user_id:1,fid:1,m_date:-1},{name:"getMessageStatList",background:true});
//Message Index
db.message.ensureIndex({fromUserId:1,toUserId:1,status:1,reciever_del_flag:1,sender_del_flag:1,m_date:-1},{name:"getMessageList",background:true});



db.user.ensureIndex({draw_rank_score:1}, {name:"rankDrawUser",background:true});
db.action.ensureIndex({history_score:-1}, {name:"historyScore",background:true});

db.exchange_ad.remove();
db.exchange_ad.insert({"name":"draw1", "app_url":"https://itunes.apple.com/cn/app/draw-lively/id513819630?mt=8"});
db.exchange_ad.insert({"name":"dice1", "app_url":"https://itunes.apple.com/cn/app/huan-le-da-hua-tou/id557072001?mt=8"});

db.exchange_ad.insert({"name":"draw2", "app_url":"http://phobos.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=557072001&mt=8"});
db.exchange_ad.insert({"name":"dice2", "app_url":"http://phobos.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=513819630&mt=8"});





db.exchange_ad.insert({"name":"test", "app_url":"http://www.baidu.com"});


double lenWeight = (drawDataLen != 0) ? (drawDataLen)
		: DEFAULT_DATA_LENGTH;
lenWeight *= DATA_LENGTH_COEFFICIENT;

double rank = (flowerTimes - tomatoTimes) * RANK_COEFFICIENT
		+ saveTimes * SAVE_COEFFICIENT;

double p = correctTimes * CORRECT_COEFFICIENT + guessTimes
		+ commentTimes + lenWeight + rank;

//一次性更新总榜得分。
var cursor=db.action.find({"type":{"$in":[1,5]}});
while(cursor.hasNext()){
 var x=cursor.next();
   if(x.guess_times == undefined){
	x.guess_times = 0;
   }
   if(x.correct_times == undefined){
	x.correct_times = 0;
   }
   if(x.comment_times == undefined){
	x.comment_times = 0;
   }
   if(x.flower_times == undefined){
	   x.flower_times = 0;
   }
   if(x.tomato_times == undefined){
	   x.tomato_times = 0;
   }
   if(x.save_times == undefined){
		x.save_times = 0;
   }
   if(x.data_len == undefined){
      x.data_len = 1200;
   }else if(x.data_len > 7200){
	   x.data_len = 7200;
   }
   var v1 = (x.flower_times-x.tomato_times)*3.0 + x.save_times*2.5;
   var value = v1 + x.guess_times+x.correct_times*2+x.comment_times+x.data_len*0.0007;
   x.history_score = value;
   db.action.update({_id:x._id},x);	
}

db.relation.ensureIndex({uid:1, game_status.status:1, game_status.server_id:1})
db.user_game_status.ensureIndex({uid:1, server_id:1});


//插入contest数据
db.contest.insert(
		{
			"_id" : ObjectId("888888888888888888890001"),
			"opus_count" : 0,
			"participant_count" : 0,
			"language" : 1,
			"type" : 2,
			"title" : "梦想小屋大赛",
			"contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890002_contest_ipad.png",
			"statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890002_rule_ipad.png",
			"submit_count" : 1,
			"s_date" : ISODate("2012-12-15T16:00:00Z"),
			"e_date" : ISODate("2012-12-25T16:00:00Z"),
			"contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890002_contest_ipad.png",
			"statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890002_rule_ipad.png"
		})
		
//插入contest数据
db.contest.insert(
        {
            "_id" : ObjectId("888888888888888888890006"),
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "儿童节画画大赛",
            "contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2013-05-29T16:00:00Z"),
            "e_date" : ISODate("2013-06-07T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg"
        })

//插入contest数据，支持评委等信息
db.contest.insert({
            "_id" : ObjectId("988888888888888820130822"),
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "测试画画大赛",
            "contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2013-08-19T16:00:00Z"),
            "e_date" : ISODate("2013-08-30T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
          "judges" : ["51ef9b3f0364966a8208b3a2"],
            "reporters" : ["51ef9d000364966a8208b3a3", "51efa02d03642da5f8dfb495"],
            "anonymous" : true,
            "vote_start_date" : ISODate("2013-08-21T16:00:00Z"),
            "vote_end_date" : ISODate("2013-08-29T16:00:00Z"),
            "max_flower_per_opus" : 5,
            "max_flower_per_contest" : 10,
            "winner_list" : [ { type:1, name:"名次", "uid":"51efa02d03642da5f8dfb495", "rank":3, "score":1000, "award_coins":3500, "contest_id":"988888888888888820130820" }],
            "award_list" : [ { type:2, name:"创意奖", "uid":"51efa02d03642da5f8dfb495", "rank":1, "score":1234, "award_coins":5000, "contest_id":"988888888888888820130820" } ],
            "rank_types" : {"1":"名次", "2" : "创意奖", "3" : "潜力奖"},
            "status" : 1
            });


//插入contest数据，支持评委等信息
db.contest.insert({
            "_id" : ObjectId("988888888888888820130901"),
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "非匿名画画大赛",
            "contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2013-08-01T16:00:00Z"),
            "e_date" : ISODate("2013-09-30T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "judges" : ["51ef9b3f0364966a8208b3a2"],
            "reporters" : ["51ef9d000364966a8208b3a3", "51efa02d03642da5f8dfb495"],
            "anonymous" : false,
            "vote_start_date" : ISODate("2013-08-21T16:00:00Z"),
            "vote_end_date" : ISODate("2013-09-30T16:00:00Z"),
            "max_flower_per_opus" : 5,
            "max_flower_per_contest" : 10,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "创意奖", "3" : "潜力奖"},
            "status" : 1
            });

//插入contest数据，支持评委等信息
db.contest.insert({
            "_id" : ObjectId("988888888888888820130903"),
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "提交结束/只能投花",
            "contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2013-08-01T16:00:00Z"),
            "e_date" : ISODate("2013-08-30T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "judges" : ["51ef9b3f0364966a8208b3a2"],
            "reporters" : ["51ef9d000364966a8208b3a3", "51efa02d03642da5f8dfb495"],
            "anonymous" : true,
            "vote_start_date" : ISODate("2013-08-21T16:00:00Z"),
            "vote_end_date" : ISODate("2013-09-30T16:00:00Z"),
            "max_flower_per_opus" : 5,
            "max_flower_per_contest" : 10,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "创意奖"},
            "status" : 2
            });


//插入contest数据，支持评委等信息
db.contest.insert({
            "_id" : ObjectId("988888888888888820130904"),
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "提交结束/不能投花",
            "contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2013-08-01T16:00:00Z"),
            "e_date" : ISODate("2013-08-30T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "judges" : ["51ef9b3f0364966a8208b3a2"],
            "reporters" : ["51ef9d000364966a8208b3a3", "51efa02d03642da5f8dfb495"],
            "anonymous" : true,
            "vote_start_date" : ISODate("2013-08-21T16:00:00Z"),
            "vote_end_date" : ISODate("2013-09-09T16:00:00Z"),
            "max_flower_per_opus" : 5,
            "max_flower_per_contest" : 10,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "创意奖"},
            "status" : 2
            });

db.contest.update({"_id" : ObjectId("988888888888888820130903")},{$set:{e_date:ISODate("2013-08-30T16:00:00Z")}});


db.contest.insert({
            "_id" : ObjectId("988888888888888820130905"),
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "XXXX",
            "contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2013-08-01T16:00:00Z"),
            "e_date" : ISODate("2013-09-18T16:00:00Z"),
            "vote_start_date" : ISODate("2013-08-21T16:00:00Z"),
            "vote_end_date" : ISODate("2013-09-29T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "contestants_only" : false,
            "contestants": [ ],
            "judges" : [ ],
            "reporters" : [ ],
            "anonymous" : true,
            "max_flower_per_opus" : 3,
            "max_flower_per_contest" : 300,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "创意奖" },
            "status" : 2
            });


//插入contest数据，支持评委等信息，新年唱歌比赛
db.contest.insert({
            "_id" : ObjectId("988888888888888820131231"),
            "cate" : 1,
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "说出你的新年祝福",
            "contest_url" : "http://58.215.184.18:8080/contest/image/988888888888888820131231_contest.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/988888888888888820131231_rule.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2013-12-30T16:00:00Z"),
            "e_date" : ISODate("2014-01-08T16:00:00Z"),
            "vote_start_date" : ISODate("2013-12-31T16:00:00Z"),
            "vote_end_date" : ISODate("2014-01-09T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820131231_contest.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820131231_rule.jpg",
            "contestants_only" : false,
            "contestants": [],
            "judges" : [],
            "reporters" : [],
            "anonymous" : true,
            "max_flower_per_opus" : 3,
            "max_flower_per_contest" : 3000,
            "submit_count" : 1,
            "flower_rank_weight" : 3,
            "judge_rank_weight" : 60,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次"},
            "status" : 2
            });


//插入contest数据，支持评委等信息，新年唱歌比赛
db.contest.insert({
            "_id" : ObjectId("988888888888888820140128"),
            "cate" : 1,
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "谢谢你伴我成长",
            "contest_url" : "http://58.215.184.18:8080/contest/image/988888888888888820140128_contest.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/988888888888888820140128_rule.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2014-01-29T16:00:00Z"),
            "e_date" : ISODate("2014-02-11T16:00:00Z"),
            "vote_start_date" : ISODate("2014-01-29T16:00:00Z"),
            "vote_end_date" : ISODate("2014-02-12T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820140128_contest.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820140128_rule.jpg",
            "contestants_only" : false,
            "contestants": [],
            "judges" : [],
            "reporters" : [],
            "anonymous" : false,
            "max_flower_per_opus" : 3,
            "max_flower_per_contest" : 3000,
            "submit_count" : 1,
            "flower_rank_weight" : 3,
            "judge_rank_weight" : 60,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次"},
            "status" : 2,
            "group" : false
            });

//插入contest数据，支持评委等信息，新年唱歌比赛
db.contest.insert({
            "_id" : ObjectId("988888888888888820140129"),
            "cate" : 0,
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "新年快乐",
            "contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888820140128_contest.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888820140128_rule.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2014-01-29T16:00:00Z"),
            "e_date" : ISODate("2014-02-11T16:00:00Z"),
            "vote_start_date" : ISODate("2014-01-29T16:00:00Z"),
            "vote_end_date" : ISODate("2014-02-12T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888820140128_contest.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888820140128_rule.jpg",
            "contestants_only" : false,
            "contestants": [],
            "judges" : [],
            "reporters" : [],
            "anonymous" : true,
            "max_flower_per_opus" : 3,
            "max_flower_per_contest" : 3000,
            "submit_count" : 1,
            "flower_rank_weight" : 3,
            "judge_rank_weight" : 60,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "特别创意奖", "3" : "最具潜力奖"},
            "status" : 2,
            "group" : false
            });

//插入contest数据，支持评委等信息
db.contest.insert({
            "_id" : ObjectId("988888888888888820130999"),
            "cate" : 0,
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "XXXX",
            "contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2013-08-01T16:00:00Z"),
            "e_date" : ISODate("2013-09-18T16:00:00Z"),
            "vote_start_date" : ISODate("2013-08-21T16:00:00Z"),
            "vote_end_date" : ISODate("2013-09-29T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_contest_ipad.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890006_rule_ipad.jpg",
            "contestants_only" : false,
            "contestants": [],
            "judges" : [],
            "reporters" : [],
            "anonymous" : true,
            "max_flower_per_opus" : 3,
            "max_flower_per_contest" : 300,
            "submit_count" : 1,
            "flower_rank_weight" : 3,
            "judge_rank_weight" : 60,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "特别创意奖", "3" : "最具潜力奖"},
            "status" : 2
            });

//插入contest数据，支持评委等信息，新年唱歌比赛
db.contest.insert({
            "_id" : ObjectId("988888888888888820140601"),
            "cate" : 0,
            "opus_count" : 0,
            "participant_count" : 0,
            "language" : 1,
            "type" : 2,
            "title" : "儿童节快乐",
            "contest_url" : "http://58.215.184.18:8080/contest/image/988888888888888820140601_contest.jpg",
            "statement_url" : "http://58.215.184.18:8080/contest/image/988888888888888820140601_rule.jpg",
            "submit_count" : 1,
            "s_date" : ISODate("2014-05-30T16:00:00Z"),
            "e_date" : ISODate("2014-06-10T16:00:00Z"),
            "vote_start_date" : ISODate("2014-05-30T16:00:00Z"),
            "vote_end_date" : ISODate("2014-06-11T16:00:00Z"),
            "contest_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820140601_contest.jpg",
            "statement_ipad_url" : "http://58.215.184.18:8080/contest/image/988888888888888820140601_rule.jpg",
            "contestants_only" : false,
            "contestants": [],
            "judges" : [],
            "reporters" : [],
            "anonymous" : true,
            "max_flower_per_opus" : 3,
            "max_flower_per_contest" : 3000,
            "submit_count" : 1,
            "flower_rank_weight" : 3,
            "judge_rank_weight" : 60,
            "winner_list" : [ ],
            "award_list" : [  ],
            "rank_types" : {"1":"名次", "2" : "特别创意奖", "3" : "最具潜力奖"},
            "status" : 2,
            "group" : false,
            "award_rules" : [
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
                ],
            "award_total" : 51500

            });


db.contest.update({"_id" : ObjectId("888888888888888888890007")},{$addToSet:{judges:"522446880364908b37cc137d"}});
db.contest.update({"_id" : ObjectId("888888888888888888890007")},{$addToSet:{judges:"522446880364908b37cc137d"}});
db.contest.update({"_id" : ObjectId("888888888888888888890007")},{$addToSet:{judges:"522446880364908b37cc137d"}});

db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$set:{s_date:ISODate("2014-05-31T16:00:00Z")}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$set:{vote_start_date:ISODate("2014-01-27T16:00:00Z")}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$set:{"award_rules" : [ 	20000, 	15000, 	10000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000 ]}});

db.contest.update({"_id" : ObjectId("988888888888888820140128")},{$set:{s_date:ISODate("2014-01-27T16:00:00Z")}});
db.contest.update({"_id" : ObjectId("988888888888888820140128")},{$set:{vote_start_date:ISODate("2014-01-27T16:00:00Z")}});
db.contest.update({"_id" : ObjectId("988888888888888820140128")},{$set:{"award_rules" : [ 	20000, 	15000, 	10000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000, 	5000 ]}});


db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"52c4ef72e4b035f5ca930a12"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"5152e423e4b0c93e8e1dd00c"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"503a2fe32609ffed65eba6d1"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"516d4d6ee4b0e33f70c1cd7d"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"51d14b08e4b078f0023861df"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"51d3f69ee4b020f7419ebc47"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"51b1db12e4b08ab7e19c7041"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"51021f70e4b0a04f9ebc8ed5"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"51985eb6e4b021ddc794a309"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"51061cfbe4b00bd3d109f23f"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{reporters:"503ab8592609ffed65ebb1bb"}});

db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{judges:"50fb6f45e4b05bb0f0801a27"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{judges:"5108ce40e4b0fcf06b9834b5"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{judges:"51062a87e4b00bd3d109f64c"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{judges:"50e820b8e4b07039cd227c66"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{judges:"52344605e4b058f58458beb9"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{judges:"51191487e4b098c397bc56be"}});
db.contest.update({"_id" : ObjectId("988888888888888820140129")},{$addToSet:{judges:"4fc3089a26099b2ca8c7a4ab"}});


db.contest.update({"_id" : ObjectId("988888888888888820140128")},{$addToSet:{reporters:"52da7069e4b054fc77f8e90a"}});
db.contest.update({"_id" : ObjectId("988888888888888820140128")},{$addToSet:{reporters:"51e54be2e4b0ebebc23dcdd6"}});
db.contest.update({"_id" : ObjectId("988888888888888820140128")},{$addToSet:{reporters:"50d98e4fe4b0d73d234ee56c"}});
db.contest.update({"_id" : ObjectId("988888888888888820140128")},{$addToSet:{reporters:"4fc3089a26099b2ca8c7a4ab"}});


db.contest.update({"_id" : ObjectId("988888888888888820140128")},{$addToSet:{judges:"4fc3089a26099b2ca8c7a4ab"}});



db.contest.update({"_id" : ObjectId("888888888888888888890010")},{$addToSet:{reporters:"50fcf619e4b089463b07294b"}});

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$set:{vote_end_date:ISODate("2013-09-06T16:00:00Z")}});
db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$pull:{judges:"51efa02d03642da5f8dfb495"}});
db.contest.update({"_id" : ObjectId("988888888888888820130902")},{$addToSet:{judges:"4f86469d260958163895b958"}});
db.contest.update({"_id" : ObjectId("988888888888888820130902")},{$addToSet:{reporters:"4f86469d260958163895b958"}});
db.contest.update({"_id" : ObjectId("988888888888888820130901")},{$set:{status:1}});


db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{judges:"5113410ae4b0318bbac99104"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{judges:"4fc3089a26099b2ca8c7a4ab"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{judges:"51092cabe4b012bd200ef284"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{judges:"50f4ad58e4b05bb0f07e9a08"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{judges:"50d5ca23e4b0d73d234e6bbb"}});

db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"50e820b8e4b07039cd227c66"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"529ae172e4b07a5bd91be9c5"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"50e4087be4b07039cd21d735"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"52466ceae4b02cb2315a2869"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"503a2fe32609ffed65eba6d1"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"51ef4440e4b0705e61162244"}});

db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"5203134ce4b030c9cfe21d1a"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"52c4ef72e4b035f5ca930a12"}});

db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"532053e4e4b00bad6d8b94bb"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"5362ff53e4b0b4abf0e7a38b"}});

db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"50fcf619e4b089463b07294b"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"52d64adee4b09b66ed080382"}});
db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:"51e54be2e4b0ebebc23dcdd6"}});


db.contest.update({"_id" : ObjectId("988888888888888820140601")},{$addToSet:{reporters:["50e820b8e4b07039cd227c66", "529ae172e4b07a5bd91be9c5", "50e4087be4b07039cd21d735", "52466ceae4b02cb2315a2869", "503a2fe32609ffed65eba6d1", "51ef4440e4b0705e61162244"]}});


50e820b8e4b07039cd227c66, 仔仔
529ae172e4b07a5bd91be9c5, 节操酱
50e4087be4b07039cd21d735, OP💫我是爸爸妈妈的好孩子'两米六
52466ceae4b02cb2315a2869, ✨⭐你才到碗里去⭐✨逗长=∇=沫沫 奥特
503a2fe32609ffed65eba6d1, 💀OP💫船员•银铃💀
51ef4440e4b0705e61162244, 🍥DR💫惜ཡ妙ོ

5203134ce4b030c9cfe21d1a, -锦年-
52c4ef72e4b035f5ca930a12, ds🔝木马
532053e4e4b00bad6d8b94bb, 🔶🔹♔Noka°🔸🔷【⬅️复活quq
5362ff53e4b0b4abf0e7a38b, 新梦杂志社&社长&洛筱柒

50fcf619e4b089463b07294b, Forever浅笑_思)
52d64adee4b09b66ed080382, Pokemon🌟长耳兔•雪莉 想变成喵咪の兔
51e54be2e4b0ebebc23dcdd6,  ✟Cross✟生命复活者 ♔月♔「幽」

儿童节比赛评委团如下：

🔝DS🔹兜兜飞🔹
💦快乐💦
💀OP💫二番队队长'ageless💀
蟲宠不乖o(^▽^)o

儿童节比赛记者团如下：

仔仔
节操酱
OP💫我是爸爸妈妈的好孩子'两米六
✨⭐你才到碗里去⭐✨逗长=∇=沫沫 奥特
💀OP💫船员•银铃💀
🍥DR💫惜ཡ妙ོ
-锦年-
ds🔝木马
🔶🔹♔Noka°🔸🔷【⬅️复活quq
新梦杂志社&社长&洛筱柒
Forever浅笑_思)
Pokemon🌟长耳兔•雪莉 想变成喵咪の兔
✟Cross✟生命复活者 ♔月♔「幽」

再次感谢本次评委和记者团们对本次比赛的大力支持！

皮酥，我因为好久没上小吉其实以前还有一个号，但没领小吉号，叫，还能能找回来吗？

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$set:{status:3}});

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$set:{vote_end_date:ISODate("2013-09-06T16:00:00Z")}});

db.contest.update({"_id" : ObjectId("988888888888888820131231")},{$addToSet:{reporters:"4fc3089a26099b2ca8c7a4ab"}});

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$addToSet:{reporters:"4fc3089a26099b2ca8c7a4ab"}});

Bao ziw
pokemen⚡️负电拍拍
painter卑鄙菌
DR🔥惜 妙🔥
♠MH🔱族长 🔮熙月冥🌙月君💫小冥🎶
🔝ds🔹 梨开 🔹
🍥DR💫雪酱{C-Y}•雪酱

db.contest.update({"_id" : ObjectId("988888888888888820131231")},{$addToSet:{reporters:"52399eb6e4b0785c50d1e2e0"}});
db.contest.update({"_id" : ObjectId("988888888888888820131231")},{$addToSet:{reporters:"52ac562fe4b07add7dd20ae8"}});



db.contest.update({"_id" : ObjectId("888888888888888888890007")},{$addToSet:{judges:"522446880364908b37cc137d"}});

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$addToSet:{judges:"4f95717e260967aa715a5af4"}});

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$set:{vote_end_date:ISODate("2013-09-06T16:00:00Z")}});
db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$pull:{judges:"51efa02d03642da5f8dfb495"}});
db.contest.update({"_id" : ObjectId("988888888888888820130902")},{$addToSet:{judges:"4f86469d260958163895b958"}});
db.contest.update({"_id" : ObjectId("988888888888888820130902")},{$addToSet:{reporters:"4f86469d260958163895b958"}});
db.contest.update({"_id" : ObjectId("988888888888888820130901")},{$set:{status:1}});

db.contest.update({"_id" : ObjectId("988888888888888820130901")},{$set:{flower_rank_weight:4, judge_rank_weight:80}});
db.contest.update({"_id" : ObjectId("988888888888888820130901")},{$set:{status:3}});


db.contest.update({"_id" : ObjectId("988888888888888820130905")},{$set:{"contestants_only":true}, $addToSet:{"contestants":"4f86469d260958163895b958"}});


                                              flower_rank_weight  3
                                              judge_rank_weight   60


db.user.insert(
{
	"_id" : ObjectId("4f86469d260958163895b958"),
	"app_id" : "513819630",
	"app_list" : [
		"513819630"
	],
	"balance" : 920,
	"c_date" : ISODate("2013-07-24T09:36:45.910Z"),
	"country_code" : "CN",
	"device_id" : "",
	"device_model" : "iPad Simulator",
	"device_os" : "iPhone OS_6.1",
	"device_token" : "",
	"device_type" : 1,
	"drawtome_count" : 0,
	"gender" : "f",
	"guess_lang" : 0,
	"ingot_balance" : 0,
	"is_jb" : false,
	"items" : [
		{
			"type" : 1,
			"amount" : 10
		}
	],
	"language" : "zh-Hans",
	"nick_name" : "test 1",
	"open_info" : 0,
	"source_id" : "513819630",
	"status" : "1",
	"version" : "6.91",
	"zodiac" : 0
}
);


http://192.168.1.198:8000/api/i?&m=generateNumber&tp=1&prefix=139&gid=Draw
http://192.168.1.198:8000/api/i?&m=generateNumber&tp=2&prefix=123&gid=Draw

http://192.168.1.198:8000/api/i?&m=clearUserNumber&xn=139327763&gid=Draw



http://192.168.1.198:8000/api/i?&m=setUserNumber&uid=523826dd0364f0575cd1ef13&app=513819630&xn=123123123


http://you100.me:8001/api/i?&m=setUserNumber&uid=503b7f962609ffed65ebd07d&app=513819630&xn=123222222&gid=Draw



http://you100.me:8001/api/i?&m=setUserNumber&uid=510d0137e4b012bd201057a9&app=513819630&xn=123061515&gid=Draw
http://you100.me:8001/api/i?&m=setUserNumber&uid=506bd178e4b03da703ad0596&app=513819630&xn=123061616&gid=Draw
http://you100.me:8001/api/i?&m=setUserNumber&uid=51f1dddbe4b0705e6116bda9&app=513819630&xn=123061818&gid=Draw

                                对不起，很久没回你。是

http://you100.me:8001/api/i?&m=clearUserNumber&xn=139755625&gid=Draw


http://you100.me:8001/api/i?&m=clearUserNumber&xn=123302887&gid=Draw


http://you100.me:8001/api/i?&m=generateNumber&tp=1&prefix=139&gid=Draw
http://you100.me:8001/api/i?&m=generateNumber&tp=2&prefix=123&gid=Draw

http://you100.me:8001/api/i?&m=generateNumber&tp=1&prefix=111&gid=Sing

http://you100.me:8001/api/i?&m=generateNumber&tp=2&prefix=123&gid=Sing


http://you100.me:8001/api/i?&m=setUserNumber&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&xn=100000001
http://you100.me:8001/api/i?&m=setUserNumber&uid=4f95717e260967aa715a5af4&app=513819630&xn=100000002
http://you100.me:8001/api/i?&m=setUserNumber&uid=4f960e3a260967aa715a5c92&app=513819630&xn=100666666
http://you100.me:8001/api/i?&m=setUserNumber&uid=4f975eab260967aa715a6945&app=513819630&xn=123000000
http://you100.me:8001/api/i?&m=setUserNumber&uid=4ff777e326096dae5cd9e7aa&app=513819630&xn=123000013
http://you100.me:8001/api/i?&m=setUserNumber&uid=4f8bcc9b260958163895b97a&app=513819630&xn=123123123

http://you100.me:8001/api/i?&m=setUserNumber&uid=50d5ca23e4b0d73d234e6bbb&app=513819630&xn=123456789
http://you100.me:8001/api/i?&m=setUserNumber&uid=5087b337e4b0e39b1782d683&app=513819630&xn=123454321
http://you100.me:8001/api/i?&m=setUserNumber&uid=51191487e4b098c397bc56be&app=513819630&xn=123978469


http://you100.me:8001/api/i?&m=setUserNumber&uid=50f549eae4b05bb0f07ebdcc&app=513819630&number=123861014

// 设置和清除小吉号码
http://you100.me:8001/api/i?&m=setUserNumber&uid=52109d7ae4b030c9cfe4cf2a&app=513819630&xn=123140202&gid=Draw
http://you100.me:8001/api/i?&m=clearUserNumber&xn=123140202&gid=Draw

 Month
 5167ffc5e4b0f766701a11d5
 51335a6ae4b0f6620a5e9a1b

http://58.215.160.100:8001/api/i?&m1=purchaseVip&uid=52e13771e4b075ea90810992&app=513819630&gid=Draw&tp=1&format=pb&ts=1391426105&mac=yHBfiTLl0RYbVlx5sBMC2A%3D%3D&v=8.0
http://58.215.160.100:8001/api/i?&m1=purchaseVip&uid=51335a6ae4b0f6620a5e9a1b&app=513819630&gid=Draw&tp=1&format=pb&ts=1391426105&mac=yHBfiTLl0RYbVlx5sBMC2A%3D%3D&v=8.0

http://58.215.160.100:8001/api/i?&m1=purchaseVip&uid=51715745e4b00b755053ebf8&app=513819630&gid=Draw&tp=1&format=pb&ts=1391426105&mac=yHBfiTLl0RYbVlx5sBMC2A%3D%3D&v=8.0
51715745e4b00b755053ebf8



 Year
 516d4d6ee4b0e33f70c1cd7d
 52b97d94e4b035f5ca9225a9
 51191487e4b098c397bc56be
 50f57a6de4b05bb0f07ed051
 5186ed0de4b0c45a53ff5ebd

http://58.215.160.100:8001/api/i?&m1=purchaseVip&uid=51a95165e4b02db4a70e7309&app=513819630&gid=Draw&tp=2&format=pb&ts=1391426186&mac=BbRY%2FdCMcqiUzlxyDV5%2BEA%3D%3D&v=8.0

http://58.215.160.100:8001/api/i?&m1=purchaseVip&uid=516d4d6ee4b0e33f70c1cd7d&app=513819630&gid=Draw&tp=2&format=pb&ts=1391426186&mac=BbRY%2FdCMcqiUzlxyDV5%2BEA%3D%3D&v=8.0
http://58.215.160.100:8001/api/i?&m1=purchaseVip&uid=52b97d94e4b035f5ca9225a9&app=513819630&gid=Draw&tp=2&format=pb&ts=1391426186&mac=BbRY%2FdCMcqiUzlxyDV5%2BEA%3D%3D&v=8.0
http://58.215.160.100:8001/api/i?&m1=purchaseVip&uid=51191487e4b098c397bc56be&app=513819630&gid=Draw&tp=2&format=pb&ts=1391426186&mac=BbRY%2FdCMcqiUzlxyDV5%2BEA%3D%3D&v=8.0
http://58.215.160.100:8001/api/i?&m1=purchaseVip&uid=50f57a6de4b05bb0f07ed051&app=513819630&gid=Draw&tp=2&format=pb&ts=1391426186&mac=BbRY%2FdCMcqiUzlxyDV5%2BEA%3D%3D&v=8.0


// 恢复某个作品（给定作品ID）
http://place100.com:8100/api/i?&m=recoverOpus&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&opid=
http://place100.com:8100/api/i?&m=recoverOpus&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&opid=524317c3e4b079eef7c6be11


http://place100.com:8100/api/i?&m=recoverOpus&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&opid=5430cc86e4b0379a6eade0a5


http://place100.com:8100/api/i?&m=recoverOpus&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&opid=53e05c57e4b09de94bf73bfe

52c7f4b2e4b07623a5e9c3a4

http://place100.com:8100/api/i?&m=recoverOpus&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&opid=525bd193e4b04ca33fe26df5

// 重建用户作品索引（给定用户ID）
http://place100.com:8100/api/i?&m=recreateOpus&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tid=519946f0e4b04eaabf7d5911


http://place100.com:8100/api/i?&m=recreateOpus&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tid=50f4ad58e4b05bb0f07e9a08

// 重新计算热榜得分
http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630

// 重新计算Contest得分
http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tp=2&cid=888888888888888888890008


http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tp=2&cid=52d681f1e4b0d0720d93ca5b
http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tp=2&cid=52d8fd4de4b02d268ddea4f7
http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tp=2&cid=52d892b5e4b0307a9b75ee49
http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tp=2&cid=52d7f9c5e4b0307a9b75c89e
http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tp=2&cid=52d9158be4b02d268ddec0bb
http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tp=2&cid=52d3e9e2e4b016b6cd2da025
http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tp=2&cid=52d3af4de4b04609d1a2abe8
http://www.place100.com:8100/api/i?&m=recalculateScore&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&tp=2&cid=52d8f357e4b02d268dde99d6


http://you100.me:8001/api/i?&m=setUserNumber&uid=50f549eae4b05bb0f07ebdcc&app=513819630&number=123861014


http://you100.me:8001/api/i?&m=setUserNumber&uid=4fc3089a26099b2ca8c7a4ab&app=513819630&number=100000001

db.group_info.update({_id:ObjectId("52d8f714e4b02d268dde9e0b")},{$set:
{
	"action_count" : 264,
	"admin_list" : [
                    ObjectId("4f8bcc9b260958163895b97a"),
                    ObjectId("4ff7212126096dae5cd9e34b"),
                    ObjectId("50f549eae4b05bb0f07ebdcc"),
                    ObjectId("50f4ad58e4b05bb0f07e9a08"),
                    ObjectId("51253f21e4b0a683e0916c4b"),
                    ObjectId("51b4b212e4b08ab7e19d17db"),
                    ObjectId("50d9a608e4b0d73d234eeb4e"),
                    ObjectId("5111e70be4b0e1a5aa6d3126"),
                    ObjectId("5031ee532609ffed65ea5e5a"),
                    ObjectId("5087b337e4b0e39b1782d683"),
                    ObjectId("524f73a1e4b04dbda268dbc2"),
                    ObjectId("4f87c350260958163895b95a")
                    ],
	"background" : "20140204/2fb32a70-8d83-11e3-a4c7-00163e017d23.jpg",
	"balance" : 3000000,
	"c_date" : ISODate("2014-01-17T09:25:40.628Z"),
	"create_uid" : ObjectId("4f8bcc9b260958163895b97a"),
	"desc" : "✨👴✨这是个很懒的幼儿园！所以管理员不受理任何业务！   以上😌👆",
	"fan_count" : 381,
	"game_id" : "Draw",
	"guest_size" : 5,
	"guest_uid" : [
                   ObjectId("4fc3089a26099b2ca8c7a4ab"),
                   ObjectId("51191487e4b098c397bc56be"),
                   ObjectId("4f960e3a260967aa715a5c92"),
                   ObjectId("4fed4c39260905b0de8e6e66")
                   ],
	"image" : "20140204/c0d4ade0-8d5f-11e3-b27f-00163e017d23.jpg",
	"level" : 10,
	"name" : "春天花花幼儿园",
	"post_count" : 16,
	"signature" : "✌✨👴✨🏃🏃🏃🏃🏃🚶🚶🚶🚶🚶👫👭👫",
	"size" : 16,
	"status" : 0
}});

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$set:{"max_flower_per_opus" : 5, "max_flower_per_contest" : 10}});

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$set:{"submit_count":5}});

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$set:{"contestants_only":true}, $addToSet:{"contestants":"51f33aabe4b0705e6117115c"}});
db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$set:{"contestants_only":true}, $addToSet:{"contestants":"4fc3089a26099b2ca8c7a4ab"}});

db.contest.update({"_id" : ObjectId("988888888888888820130822")},{$set:{"contestants_only":true}, $addToSet:{"judges":"4fc3089a26099b2ca8c7a4ab"}});

db.contest.update({"_id" : ObjectId("988888888888888820130902")},{$set:{e_date:ISODate("2013-11-04T16:00:00Z"), vote_end_date:ISODate("2013-11-30T16:00:00Z")}});


db.contest.update({"_id" : ObjectId("988888888888888820130902")},{$set:{e_date:ISODate("2013-08-30T16:00:00Z"), vote_end_date:ISODate("2013-09-30T16:00:00Z")}});



db.contest.update({"_id" : ObjectId("888888888888888888890007")},{$set:{"rank_types" : {"1":"名次", "2" : "最佳创意奖", "3" : "潜力奖"},

"award_list" : [ { type:2, name:"最佳创意奖", "opus_id":"51d3d03ee4b012daa1b48a0b", "uid":"516b4febe4b0f766701ae48d", "rank":1, "score":1234, "award_coins":5000, "contest_id":"888888888888888888890007" },
{ type:3, name:"潜力奖", "opus_id":"51d2e7d8e4b0240d146a5ca7", "uid":"51b1da0fe4b08ab7e19c6fbb", "rank":1, "score":1234, "award_coins":5000, "contest_id":"888888888888888888890007" },
 ]

}});


db.contest.update({"_id" : ObjectId("888888888888888888890006")},{$set:{"s_date" : ISODate("2013-05-29T00:00:00Z")}})

db.bulletin.insert({"date":new Date(), "type":1, "game_id":"Draw","function":"contest","content":"梦想小屋大赛"})

db.bulletin.insert({"date":new Date(), "type":0, "game_id":"Draw","function":"","content":"猜猜画画5.8版本很快就要发布了，解决了回放竖线显示问题，以及5.6版本画画大赛无法提交作品的问题，记得到时候一定要升级"});

db.bulletin.insert({"date":new Date(), "type":0, "game_id":"Draw","function":"","content":"蛇年到了，猜猜画画祝大家新年快乐，释放灵感，放飞梦想，创作更多美好的作品！"});

db.bulletin.insert({"date":new Date(), "type":0, "game_id":"Draw","function":"","content":"猜猜画画5.8版本已经发布了，送给大家新年的礼物！请注意要参加下来的画画大赛一定要升级到5.8版本，否则无法提交作品。"});


//插入contest数据
db.contest.insert(
		{
			"_id" : ObjectId("888888888888888888890003"),
			"opus_count" : 0,
			"participant_count" : 0,
			"language" : 1,
			"type" : 2,
			"title" : "画出你的爱",
			"contest_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890003_contest_ipad.jpg",
			"statement_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890003_rule_ipad.jpg",
			"submit_count" : 1,
			"s_date" : ISODate("2013-02-14T07:00:00Z"),
			"e_date" : ISODate("2013-03-01T09:00:00Z"),
			"contest_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890003_contest_ipad.jpg",
			"statement_ipad_url" : "http://58.215.184.18:8080/contest/image/888888888888888888890003_rule_ipad.jpg"
		});
		
db.contest.update({"_id" : ObjectId("888888888888888888890003")},{$set:{"s_date" : ISODate("2013-02-14T06:00:00Z")}});

db.contest.update({"_id" : ObjectId("888888888888888888890003")},{$set:{"s_date" : ISODate("2013-02-14T06:00:00Z")}});

db.bulletin.insert({"date":new Date(), "type":1, "game_id":"Draw","function":"contest","content":"今天是情人节，猜猜画画蛇年第一场画画大赛开幕了，主题是［画出你的爱］，新年一定要有爱，有爱就有幸福！［温馨提示］一定要升级到5.8才能提交作品"});
db.bulletin.insert({"date":new Date(), "type":1, "game_id":"Draw","function":"contest","content":"［再次温馨提示］一定要升级到5.8才能提交作品，使用5.6版本保存作品后，无法提交比赛"});

db.bulletin.insert({"date":new Date(), "type":1, "game_id":"Draw","function":"contest","content":"[重要比赛公告] 为了保证画画大赛公平，现规定如下：1）禁止求互赠鲜花；2）禁止悬赏求花；3）禁止以丢番茄威胁求花。 从本公告起实施，违者可能被禁止作品评选资格"});

db.bulletin.insert({"date":new Date(), "type":1, "game_id":"Draw","function":"free_ingot","content":"[公告] 下载免费应用可获得元宝，购买更多画画道具和工具"});
db.bulletin.insert({"date":new Date(), "type":1, "game_id":"Draw","function":"shop","content":"[公告] 商店促销本周末将结束，需要购买的用户尽快了"});


// 根据当前message表创建user_message表
function create_user_message_table() {
	var cursor=db.message.find();
	while (cursor.hasNext()) {
       var x=cursor.next();
       if ( x.fromUserId.length != 24 || x.toUserId.length != 24 ) {
    	     continue;
    	 }

       var userId = ObjectId(x.fromUserId);
       var relatedUserId = ObjectId(x.toUserId);
       var messageId = x._id;
       if (x.sender_del_flag != 1) {
          db.user_message.insert({"user_id":userId, "related_user_id":relatedUserId, 
        	     "message_id":messageId, "c_date":x.c_date,"type":1});
         }
       if (x.reciever_del_flag != 1) {
           db.user_message.insert({"user_id":relatedUserId, "related_user_id":userId, 
         	     "message_id":messageId, "c_date":x.c_date,"type":2});
         }
    }
}
db.user_message.ensureIndex({user_id:1, related_user_id:1, message_id:1, c_date:1, type:1},{name:"getUserMessageIndex"})




//一次性更新总榜得分。

db.contest_latest_opus.ensureIndex({contest_id:1, opus_id:-1});
db.contest_my_opus.ensureIndex({user_id:1, contest_id:1});

var cursor=db.action.find({"type":9});
var count=0;
while(cursor.hasNext()){
 var x=cursor.next();
 count++;
 print(count+" index on "+x.create_uid+" "+x.contest_id);

        // insert to contest my opus
        db.contest_my_opus.update({"user_id":x.create_uid, "contest_id":x.contest_id},
        {
                "$set":
                        {"user_id":x.create_uid, "contest_id":x.contest_id}
                ,
                "$push":
                        {"opus_id_list":x._id}
        }
        , true, false);
        
        // insert to contest latest
        db.contest_latest_opus.insert({"contest_id":x.contest_id, "opus_id":x._id});
}

print("total "+count+" opuses processed");



db.contest_latest_opus.ensureIndex({contest_id:1, opus_id:-1});
db.contest_my_opus.ensureIndex({user_id:1, contest_id:1});

db.learn_draw.ensureIndex({sell_type:1, opus_id:1});
db.learn_draw.ensureIndex({sell_type:1, type:1, id:-1});
db.learn_draw.ensureIndex({sell_type:1, type:1, buy_times:-1});
db.learn_draw.ensureIndex({sell_type:1, type:1, price:1});

db.learn_draw_index.ensureIndex({uid:1, sell_type:1});
db.learn_draw_index.ensureIndex({opus_id:1, sell_type:1});


db.learn_draw.update({}, {$set:{sell_type:1}}, false, true);
db.learn_draw_index.update({}, {$set:{sell_type:1}}, false, true);


//一次性更新用户等级信息
//var cursor=db.user.find({_id:ObjectId("50d64b76e4b0d73d234e7948")});
var cursor=db.user.find();
var count=0;
var update_count = 0;
while(cursor.hasNext()){
 var x=cursor.next();
 count++;
   if(x.device_id != undefined){

     x.device_ids = new Array();
     x.device_ids.push(x.device_id);

     x.device_tokens = new Array();
     x.device_tokens.push(x.device_token);

     x.devices = new Array();
     x.devices.add({"device_id":x.device_id, "device_token":x.device_token, "device_model":x.device_model, "device_os":x.device_os, "device_type":x.device_type});

     db.user.update({_id:x._id},{$set:{"device_ids":x.device_ids, "device_tokens":x.device_tokens, "devices":x.devices}});
     update_count++;
   }

  if (count%100 == 0){
    print("total "+count+" processed, total "+update_count+" updated");
  }
}

print("completed! total "+count+" processed, total "+update_count+" updated");


 db.action.update({_id:ObjectId("526922a5e4b04ccc8828744b")}, {$set:{target_uid:"4f9cd143260967aa715a91c8",target_nick:"💀☠OP💫第三番队队长'😱你妹儿！☠💀",type:5}})
 db.action.update({_id:ObjectId("526922b6e4b04ccc88287458")}, {$set:{target_uid:"4f9cd143260967aa715a91c8",target_nick:"💀☠OP💫第三番队队长'😱你妹儿！☠💀",type:5}})
 db.action.update({_id:ObjectId("52692287e4b04ccc88287433")}, {$set:{target_uid:"4f9cd143260967aa715a91c8",target_nick:"💀☠OP💫第三番队队长'😱你妹儿！☠💀",type:5}})

db.draw_to_user_opus_draw.update({owner:ObjectId("4f9cd143260967aa715a91c8")}, {"$pushAll":{list:[ObjectId("52692309e4b04ccc882874ca"), ObjectId("526922b6e4b04ccc88287458"), ObjectId("526922a5e4b04ccc8828744b"), ObjectId("52692296e4b04ccc88287439"), ObjectId("52692287e4b04ccc88287433")]}}})

db.draw_to_user_opus_draw.update({owner:ObjectId("4f9cd143260967aa715a91c8")}, {"$pull":{list:[ObjectId("52692309e4b04ccc882874ca"), ObjectId("526922b6e4b04ccc88287458"), ObjectId("526922a5e4b04ccc8828744b"), ObjectId("52692296e4b04ccc88287439"), ObjectId("52692287e4b04ccc88287433")]}})

db.opus.update({_id:ObjectId("528a856ae4b00e39f04a0ed9")}, {$set:{target_uid:"5087b337e4b0e39b1782d683",target_nick:"箫儿",type:5}})
db.opus.update({_id:ObjectId("5287944ae4b00e39f0492dff")}, {$set:{target_uid:"5087b337e4b0e39b1782d683",target_nick:"箫儿",type:5}})

db.draw_to_user_opus_draw.update({owner:ObjectId("5087b337e4b0e39b1782d683")}, {"$pushAll":{list:[ObjectId("528a856ae4b00e39f04a0ed9"), ObjectId("5287944ae4b00e39f0492dff")]}})



//增加一个板块
db.bbs_board.insert({
     "c_date" : ISODate("2013-02-21T01:22:44.270Z"),
     "game_id" : "Draw",
     "icon" : "http://58.215.160.100:8080/bbs/icon/b_young.png",
     "index" : 700,
     "name" : "XXXX",
     "parent_boardid" : ObjectId("50bd8c62e4b0e970bbebc747"),
     "status" : 0,
     "type" : 2
})

index 用来排列
type 2表示子版块，1表示父板块（猜猜画画区，大话骰区，这些用1，其他例如小学区用2）

//删除一个板块

db.bbs_board.update({game_id:”Deleted”})

//恢复删除的板块，设置正确的 game_id 即可

db.bbs_board.update({game_id:”Draw”})



//删除一个人所有的权限
db.bbs_permission.remove({uid:ObjectId("506239e72609877a698a587a")})
甘米  11:41:52

//修改用户权限
tp:2 版主 4:管理员 空为移除版主
其他：变成普通用户
bid：board id
tid: target uid

http://58.215.184.18:8100/?m=cbr&uid=4f95717e260967aa715a5af4&app=513819630&dt=&bid=50f66a804868709cee3eef50&tp=2&tid=4f9cd143260967aa715a91c8
