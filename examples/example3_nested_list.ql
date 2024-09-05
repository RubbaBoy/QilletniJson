import "json:json.ql"

string jsonStr = "{\"a\": [1, 2, 3.3, 4, [\"a\", \"b\", \"c\"], 5]}"

print("jsonStr = " + jsonStr)

JsonConverter json = JsonConverter.createJsonConverter(true)

Map map = json.fromJson(jsonStr)
print("a = " + map.get("a"))

for (var : map.get("a")) {
    if (var is int) {
        print("var is int: " + var)    
    } else if (var is double) {
        print("var is double: " + var)
    } else if (var is []) {
        print("var is list: " + var)
    }
}

any[] someList = [1, 2, 3, ["a", "b", "c"], 5]
Map jsonMap = new Map()
jsonMap.put("a", someList)

print(json.toJson(jsonMap))
