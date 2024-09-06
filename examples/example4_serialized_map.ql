import "json:json.ql"

string jsonStr = "{\"a\": {\"b\": \"b's value\"}, \"d\": 123, \"e\": [1, 2, 3, 4, 5]}"

print("jsonStr = " + jsonStr)

JsonConverter json = JsonConverter.createJsonConverter(true)

Map map = json.fromJson(jsonStr)

print("map = " + map)

string reserialized = json.toJson(map)
print("reserialized = " + reserialized)

for (key : map.keys()) {
    print("key = " + key)
    any value = map.get(key)
    
    if (value is string) {
        print("value = " + value)
    } else if (value is int) {
        print("value = " + value)
    } else if (value is []) {
        print("value = " + value)
        for (listVal : value) {
            print("  listVal = " + listVal)
        }
    } else if (value is Map) {
        print("value = " + value)
    } else {
        print("value = " + value)
    }
}
