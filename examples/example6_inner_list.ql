import "json:json.ql"

Map map = Map.fromList(any["one", "two", "three", any[1, 2, 3, 4]])

JsonConverter json = JsonConverter.createJsonConverter(true)

print(json.toJson(map))
