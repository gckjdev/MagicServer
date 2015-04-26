#!/bin/bash


#create index

echo "delete game index"
curl -XDELETE 'http://localhost:9200/game'


echo "create game index"
curl -XPUT 'http://localhost:9200/game'


#delete and create post mapping

echo "delete game.post index"

#curl -XDELETE 'http://localhost:9200/game/post/_mapping'


echo "create game.user index"

curl -XPUT 'http://localhost:9200/game/post/_mapping' -d '
{
    "post" : {
        "properties" : {
            "content" : {"type" : "string", "analyzer" : "ik"},
            "nick_name" : {"type" : "string", "analyzer" : "ik"}
        }
    }
}
'



#delete and create song mapping

echo "delete game.song index"

#curl -XDELETE 'http://localhost:9200/game/song/_mapping'


echo "create game.song index"

curl -XPUT 'http://localhost:9200/game/song/_mapping' -d '
{
    "song":
    {
    "properties":{
        "album":{"type":"string","analyzer":"ik"},
        "author":{"type":"string","analyzer":"ik"},
        "name":{"type":"string","analyzer":"ik"},
        "song_id":{"type":"string","analyzer":"ik"},
        "tags":{"type":"string","analyzer":"ik"}
        }
    }
}
'



#delete and create user mapping
echo "delete game.user index"

#curl -XDELETE 'http://localhost:9200/game/user/_mapping'

echo "create game.user index"

curl -XPUT 'http://localhost:9200/game/user/_mapping' -d '
{
	"user":{
		"properties":{
			"email": {"type":"string","analyzer":"ik"},
			"facebook_id":{"type":"string","analyzer":"ik"},
			"nick_name":{"type":"string","analyzer":"ik"},
			"qq_id":{"type":"string","analyzer":"ik"},
			"qq_nick":{"type":"string","analyzer":"ik"},
			"signature":{"type":"string","analyzer":"ik"},
			"sina_id":{"type":"string","analyzer":"ik"},
			"sina_nick":{"type":"string","analyzer":"ik"},
			"user_id":{"type":"string"},
			"xiaoji":{"type":"string"},
			"index_mask":{"type":"long"}
		}
	}
}'

#delete and create group mapping
echo "delete game.group index"
#curl -XDELETE 'http://localhost:9200/game/group/_mapping'

echo "create game.group index"

curl -XPUT 'http://localhost:9200/game/group/_mapping' -d '
{
    "group" : {
        "properties" : {
            "name" : {"type" : "string", "analyzer" : "ik"},
            "description" : {"type" : "string", "analyzer" : "ik"},
            "signature" : {"type" : "string", "analyzer" : "ik"}
        }
    }
}
'

echo "delete game.topic index"
#curl -XDELETE 'http://localhost:9200/game/topic/_mapping'

echo "create game.topic index"

curl -XPUT 'http://localhost:9200/game/topic/_mapping' -d '
{
    "topic" : {
        "properties" : {
            "content" : {"type" : "string", "analyzer" : "ik"},
            "nick_name" : {"type" : "string", "analyzer" : "ik"},
            "group_id" : {"type" : "string"},
            "post_id" : {"type" : "string"}
        }
    }
}
'

echo "Dome!!"
