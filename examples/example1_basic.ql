import "json:json.ql"

string jsonStr = "{\"a\": \"Hello\", \"c\": 5}"

JsonConverter json = JsonConverter.createJsonConverter(true)

Map map = json.fromJson(jsonStr)
print("a = " + map.get("a"))
print("c = " + map.get("c"))

int c = map.get("c")

print("cint = " + c)

map.put("b", "World")
map.put("c", map.get("c") + 5)

print(json.toJson(map))
