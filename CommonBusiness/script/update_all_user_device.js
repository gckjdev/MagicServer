//一次性更新用户等级信息
//var cursor=db.user.find({_id:ObjectId("50d64b76e4b0d73d234e7948")});
var cursor=db.user.find();
var count=0;
var update_count = 0;
while(cursor.hasNext()){
 var x=cursor.next();
 count++;
 if (x.device_ids == undefined){
     x.device_ids = new Array();
     if (x.device_id != undefined){
        x.device_ids.push(x.device_id);
     }

     x.device_tokens = new Array();
     if (x.device_token != undefined && x.device_token.length > 0){
        x.device_tokens.push(x.device_token);
     }

     x.devices = new Array();
     if (x.device_id != undefined){
        var device = {"device_id":x.device_id, "device_token":x.device_token, "device_model":x.device_model, "device_os":x.device_os, "device_type":x.device_type};
        x.devices.push(device);
     }

     db.user.update({_id:x._id},{$set:{"device_ids":x.device_ids, "device_tokens":x.device_tokens, "devices":x.devices}});
     update_count++;
  }
  else{
//    print("skip");
  }
  if (count%100 == 0){
    print("total "+count+" processed, total "+update_count+" updated");
  }
}

print("completed! total "+count+" processed, total "+update_count+" updated");
