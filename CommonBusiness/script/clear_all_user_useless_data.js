
//一次性更新用户等级信息
var cursor=db.user.find({xiaoji:"100000001"});
var cursor=db.user.find();
var count=0;
var update_count = 0;
while(cursor.hasNext()){
 var x=cursor.next();
 count++;
// if (x.follows != undefined || x.fans != undefined){
//    db.user.update({_id:x._id},{$unset:{"follows":1, "fans":1}});
//    update_count ++;
// }

  if (x.app_list != undefined){
    db.user.update({_id:x._id},{$set:{"award_apps":x.app_list}});
  }


//    if (x.device_ids != undefined){
//        var size = x.device_ids.length;
//        // keep size in deivces
//        var devices = new Array();
//        if (x.devices != undefined){
//            var devices_size = x.devices.length;
//            if (devices_size < size){
//                size = devices_size;
//            }
//            for (var i=0; i<size ; i++){
//                devices.push(x.devices[i]);
//            }
//            db.user.update({_id:x._id},{$set:{"devices":devices}});
//            update_count++;
//        }
//    }

  if (count%100 == 0){
    print("total "+count+" processed, total "+update_count+" updated");
  }
}

print("completed! total "+count+" processed, total "+update_count+" updated");

//一次性更新用户等级信息
//var cursor=db.contest.find({_id:ObjectId("52f49d24e4b0b86f7a7cbde7")});
var cursor=db.contest.find();
var count=0;
var update_count = 0;
while(cursor.hasNext()){
 var x=cursor.next();
 count++;

  if (x.group && x.award_total == undefined && x.award_rules != undefined){
    var total = 0;
    var size = x.award_rules.length;
    for (var i=0; i<size; i++){
        total = total + x.award_rules[i];
    }

    // insert
    print("update "+x._id+" total award set to "+total);
    db.contest.update({_id:x._id},{$set:{"award_total":total}});
    update_count++;
  }

  if (count%100 == 0){
    print("total "+count+" processed, total "+update_count+" updated");
  }
}

print("completed! total "+count+" processed, total "+update_count+" updated");