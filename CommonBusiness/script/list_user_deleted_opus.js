var user_id = "";
db.action.find({"create_uid":user_id, "type":{"$in":[1,5,7]}, "opus_status":1},{"word":1,"opus_status":1});
