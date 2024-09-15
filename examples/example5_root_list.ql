import "json:json.ql"

//string jsonStr = "{\"a\": [\"one\", \"two\", \"three\", 5]}"
string jsonStr = "[1, 2, 3, 4, 5]"

print("jsonStr = " + jsonStr)

JsonConverter json = JsonConverter.createJsonConverter(true)

any[] list = json.fromJson(jsonStr)

for (var : list) {
    if (var is int) {
        print("var is int: " + var)    
    } else if (var is double) {
        print("var is double: " + var)
    } else {
        print("var is not int or double: " + var)
    }
}

print("toJson = " + json.toJson(list))
