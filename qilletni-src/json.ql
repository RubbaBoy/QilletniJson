entity JsonConverter {
    
    java _gson
    boolean prettyPrint
    
    JsonConverter(_gson, prettyPrint)
    
    /**
     * Creates a JsonConverter with the pretty print option.
     *
     * @param[@type boolean] prettyPrint If the JSON should be pretty printed when deserialized
     * @returns[@type json.JsonConverter] The created JsonConverter
     */
    native static fun createJsonConverter(prettyPrint)
    
    /**
     * Converts an object to a JSON string.
     *
     * @param obj The object to convert to JSON, of any type
     * @returns[@type string] The JSON string
     */
    native fun toJson(obj)
    
    /**
     * Converts a JSON string to an object.
     *
     * @param[@type string] str The JSON string to convert to an object
     * @returns The object from the JSON string, of any type
     */
    native fun fromJson(str)
    
}