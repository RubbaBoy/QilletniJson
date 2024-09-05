import "json:json.ql"

//string jsonStr = "{\"a\": [\"one\", \"two\", \"three\", 5]}"
string jsonStr = "{\"a\": [1, 2, 3.3, 4, 5]}"

print("jsonStr = " + jsonStr)

JsonConverter json = JsonConverter.createJsonConverter(true)

Map map = json.fromJson(jsonStr)
print("a = " + map.get("a"))

for (var : map.get("a")) {
    double aa = var + 2
    print("var = " + aa)
}
